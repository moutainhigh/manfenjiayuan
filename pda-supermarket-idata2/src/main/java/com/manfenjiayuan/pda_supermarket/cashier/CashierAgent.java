package com.manfenjiayuan.pda_supermarket.cashier;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.CashierShopcartEntity;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosOrderEntity;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosOrderItemEntity;
import com.manfenjiayuan.pda_supermarket.cashier.database.service.PosOrderItemService;
import com.manfenjiayuan.pda_supermarket.cashier.database.service.PosOrderPayService;
import com.manfenjiayuan.pda_supermarket.cashier.database.service.PosOrderService;
import com.manfenjiayuan.pda_supermarket.cashier.model.CashierOrderInfo;
import com.manfenjiayuan.pda_supermarket.cashier.model.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.cashier.model.wrapper.CouponRule;
import com.manfenjiayuan.pda_supermarket.cashier.model.wrapper.LastOrderInfo;
import com.manfenjiayuan.pda_supermarket.cashier.model.wrapper.OrderPayInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.commonuseraccount.PayItem;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrder;
import com.mfh.framework.api.pmcstock.MarketRules;
import com.mfh.framework.api.pmcstock.RuleBean;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import net.tsz.afinal.db.table.KeyValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 收银
 * Created by bingshanguxue on 7/6/16.
 */
public class CashierAgent {


    /**
     * 上一订单信息
     */
    public static LastOrderInfo genLastOrderInfo(PosOrderEntity orderEntity) {
        LastOrderInfo lastOrderInfo = null;

        if (orderEntity != null) {
            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());

            lastOrderInfo = new LastOrderInfo();
            lastOrderInfo.setOrderId(orderEntity.getId());
            lastOrderInfo.setFinalAmount(lastOrderInfo.getFinalAmount() + orderEntity.getFinalAmount());
            lastOrderInfo.setbCount(lastOrderInfo.getbCount() + orderEntity.getBcount());

            lastOrderInfo.setPayType(lastOrderInfo.getPayType() | payWrapper.getPayType());
            lastOrderInfo.setDiscountAmount(lastOrderInfo.getDiscountAmount()
                    + payWrapper.getVipDiscount() + payWrapper.getPromotionDiscount()
                    + payWrapper.getCouponDiscount());
            lastOrderInfo.setChangeAmount(lastOrderInfo.getChangeAmount() + payWrapper.getChange());
        }

        return lastOrderInfo;
    }

    /**
     * 订单结算
     *
     * @param subType     业务子类型
     * @param orderBarcode     本地订单交易流水条码
     * @param outTradeNo       外部订单编号
     * @param shopcartEntities 订单明细
     */
    private static void simpleSettle(Integer subType,
                                     String orderBarcode, String outTradeNo,
                                     List<CashierShopcartEntity> shopcartEntities, boolean isCash) {
        //更新收银订单信息
        Date rightNow = TimeUtil.getCurrentDate();
        PosOrderEntity orderEntity = CashierProvider.fetchOrderEntity(orderBarcode);
        if (orderEntity == null) {
            orderEntity = new PosOrderEntity();
            orderEntity.setBizType(BizType.POS);
            orderEntity.setBarCode(orderBarcode);
            orderEntity.setSellerId(MfhLoginService.get().getSpid());
            orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
            orderEntity.setCreatedBy(MfhLoginService.get().getGuid());
            orderEntity.setPosId(SharedPrefesManagerFactory.getTerminalId());//设备编号
            orderEntity.setFlowId(CashierProvider.nextFlowId());
            orderEntity.setCreatedDate(rightNow);
        }
        orderEntity.setSubType(subType);
        orderEntity.setOuterTradeNo(outTradeNo);
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);//订单状态
        orderEntity.setUpdatedDate(rightNow);
        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("结算收银订单:%s_%d:\n%s", orderBarcode,
                orderEntity.getId(), JSONObject.toJSONString(orderEntity)));

        //有可能会有脏数据，订单编号一样但是流水号不一样
        PosOrderItemService.get().deleteBy(String.format("orderBarCode = '%s' or orderId = '%d'",
                orderBarcode, orderEntity.getId()));
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity goods : shopcartEntities) {
                //保存订单明细
                PosOrderItemService.get().saveOrUpdate(orderBarcode,
                        orderEntity.getId(), goods, isCash);
            }
        }
    }

    /**
     * 结算订单
     * @param subType 业务子类型
     * @param orderBarCode 订单条码
     * @param outTradeNo 订单交易编号
     * @param status 订单状态
     * @param shopcartEntities 订单明细
     */
    public static CashierOrderInfo settle(Integer subType, String orderBarCode,
                                          String outTradeNo, int status,
                                          List<CashierShopcartEntity> shopcartEntities) {
        //创建or更新订单，保存or更新订单明细
        simpleSettle(subType, orderBarCode, outTradeNo, shopcartEntities, true);
        //生成订单支付信息
        CashierOrderInfo cashierOrderInfo = CashierProvider.createCashierOrderInfo(orderBarCode, null);
        // 7/5/16  修复初始状态，订单金额为空的问题。
        updateCashierOrder(cashierOrderInfo, null, status);

        return cashierOrderInfo;
    }

    public static CashierOrderInfo settle(Integer subType, String orderBarCode,
                                          String outTradeNo, int status,
                                          List<CashierShopcartEntity> shopcartEntities, boolean isCash) {
        //创建or更新订单，保存or更新订单明细
        simpleSettle(subType, orderBarCode, outTradeNo, shopcartEntities, isCash);
        //生成订单支付信息
        CashierOrderInfo cashierOrderInfo = CashierProvider.createCashierOrderInfo(orderBarCode, null);
        // 7/5/16  修复初始状态，订单金额为空的问题。
        updateCashierOrder(cashierOrderInfo, null, status);

        return cashierOrderInfo;
    }

    /**
     * 调单1
     */
    public static List<PosOrderItemEntity> resume(String orderBarCode) {
        PosOrderEntity orderEntity = CashierProvider.fetchOrderEntity(orderBarCode);
        if (orderEntity == null) {
            return null;
        }

        //修改订单状态：挂单改为待支付
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
//        orderEntity.setUpdatedDate(new Date());
        PosOrderService.get().saveOrUpdate(orderEntity);

        //加载订单明细
        return PosOrderItemService.get()
                .queryAllBy(String.format("orderId = '%d'", orderEntity.getId()));
    }

    /**
     * 订单结算信息
     *
     * @param invSendIoOrder 采购收货单
     */
    public static CashierOrderInfo makeCashierOrderInfo(InvSendIoOrder invSendIoOrder) {
        if (invSendIoOrder == null) {
            return null;
        }

        Human human = new Human();
        human.setId(MfhLoginService.get().getUserId());
        human.setGuid(String.valueOf(MfhLoginService.get().getGuid()));
        human.setHeadimageUrl(MfhLoginService.get().getHeadimage());

        //当前收银信息
        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.setOrderId(invSendIoOrder.getId());
        cashierOrderInfo.setbCount(1D);
        cashierOrderInfo.setRetailAmount(invSendIoOrder.getCommitPrice());
        cashierOrderInfo.setFinalAmount(invSendIoOrder.getCommitPrice());
        cashierOrderInfo.setAdjustAmount(0D);
        cashierOrderInfo.setDiscountRate(1D);
        cashierOrderInfo.setBody(String.format("收货单%s支付", invSendIoOrder.getOrderName()));
        cashierOrderInfo.setProductsInfo(null);
        cashierOrderInfo.setBizType(BizType.STOCK);
        cashierOrderInfo.setSubject("支付采购收货单");
        cashierOrderInfo.setVipMember(human);

        return cashierOrderInfo;
    }

    /**
     * 订单结算信息
     *
     * @param bizType
     * @param orderBarCode           订单流水号
     * @param vipMember 会员
     */
    public static CashierOrderInfo makeCashierOrderInfo(ScOrder scOrder, Double amount) {
        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.setBizType(scOrder.getBtype());
        cashierOrderInfo.setPosTradeNo(scOrder.getBarcode());
        cashierOrderInfo.setSubject(String.format("订单信息：流水号：%s，交易类型：%s",
                scOrder.getBarcode(), BizType.name(scOrder.getBtype())));

        if (scOrder != null){
            Double bCount = 0D;
            Double retailAmount = 0D;
            Double finalAmount = 0D;
            StringBuilder sbBody = new StringBuilder();
            JSONArray productsInfo = new JSONArray();

            List<ScOrderItem> items = scOrder.getItems();
            if (items != null && items.size() > 0) {
                for (ScOrderItem item : items) {
                    bCount += item.getBcount();
                    retailAmount += item.getAmount();
                    finalAmount += item.getCommitAmount();

                    if (sbBody.length() > 0) {
                        sbBody.append(",");
                    }
                    sbBody.append(item.getProductName());

                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("goodsId", item.getgo());
                    jsonObject.put("skuId", item.getSkuId());
                    jsonObject.put("bcount", item.getCommitCount());
                    jsonObject.put("price", item.getPrice());
                    jsonObject.put("factAmount", item.getCommitAmount());
                    jsonObject.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,
                    productsInfo.add(item);
                }
            }

            Double adjustAmount = retailAmount - finalAmount;
            Double discountRate;
            if (retailAmount == 0D) {
                discountRate = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
            } else {
                discountRate = finalAmount / retailAmount;
            }

            cashierOrderInfo.setOrderId(scOrder.getId());
            cashierOrderInfo.setbCount(bCount);
            cashierOrderInfo.setRetailAmount(retailAmount);
            cashierOrderInfo.setFinalAmount(amount);//使用后台返回的金额
            cashierOrderInfo.setAdjustAmount(adjustAmount);
            cashierOrderInfo.setDiscountRate(discountRate);
            cashierOrderInfo.setBody(sbBody.length() > 20 ? sbBody.substring(0, 20) : sbBody.toString());
            cashierOrderInfo.setProductsInfo(productsInfo);
        }

        return cashierOrderInfo;
    }


    /**
     * 结束订单
     *
     * @param cashierOrderInfo 订单支付信息
     * @param status           订单状态
     */
    public static boolean updateCashierOrder(CashierOrderInfo cashierOrderInfo,
                                             PayAmount payAmount, int status) {
        if (cashierOrderInfo == null) {
            return false;
        }
        PosOrderEntity orderEntity = CashierProvider.fetchOrderEntity(cashierOrderInfo.getPosTradeNo());
        if (orderEntity == null) {
            return false;
        }

        Date rightNow = TimeUtil.getCurrentDate();

        orderEntity.setRetailAmount(cashierOrderInfo.getRetailAmount());
        orderEntity.setFinalAmount(cashierOrderInfo.getFinalAmount());
        orderEntity.setDiscountAmount(cashierOrderInfo.getAdjustAmount());//折扣价
        orderEntity.setBcount(cashierOrderInfo.getbCount());

        orderEntity.setRpDisAmountMap(null);
        List<KeyValue> keyValues1 = new ArrayList<>();
        keyValues1.add(new KeyValue("vipAmount", 0D));
        keyValues1.add(new KeyValue("ruleAmountMap", null));
        PosOrderItemService.get().update(keyValues1, String.format("orderId = '%d'", orderEntity.getId()));

        if (payAmount != null) {
            Map<String, Double> rpDisAmountMap = payAmount.getRpDisAmountMap();
            List<PayItem> payItemList = payAmount.getPayItemList();

            if (rpDisAmountMap != null) {
                orderEntity.setRpDisAmountMap(JSONObject.toJSONString(rpDisAmountMap));
            }

            if (payItemList != null && payItemList.size() > 0) {
                for (PayItem payItem : payItemList) {
                    List<KeyValue> keyValues2 = new ArrayList<>();
                    keyValues2.add(new KeyValue("vipAmount",
                            MathCompact.sub(payItem.getFactAmount(), payItem.getSaleAmount())));
                    keyValues2.add(new KeyValue("ruleAmountMap",
                            JSONObject.toJSONString(payItem.getRuleAmountMap())));
                    PosOrderItemService.get().update(keyValues2,
                            String.format("orderId = '%d' and goodsId = '%d'",
                                    orderEntity.getId(), payItem.getGoodsId()));
                }
            }
        }

        Human human = cashierOrderInfo.getVipMember();
        if (human != null) {
            orderEntity.setHumanId(human.getId());
//            orderEntity.setScore(0D);//会员积分
        }

        //支付完成
        orderEntity.setStatus(status);
        if (status == PosOrderEntity.ORDER_STATUS_FINISH) {
            orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_YES);
            orderEntity.setUpdatedDate(rightNow);
        } else {
            orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_NO);
        }

        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("更新订单 %s_%d:\n%s", orderEntity.getBarCode(),
                orderEntity.getId(), JSONObject.toJSONString(orderEntity)));
        return true;
    }

    /**
     * 保存支付记录并更新支付订单
     *
     * @param orderBarCode 订单流水号
     * @param vipMember    会员信息
     * @param paymentInfo  订单支付信息
     *                     适用场景：收银支付金额或者状态发生改变
     */
    public static boolean updateCashierOrder(String orderBarCode, Human vipMember,
                                             PaymentInfo paymentInfo) {
        PosOrderEntity orderEntity = CashierProvider.fetchOrderEntity(orderBarCode);
        if (orderEntity == null) {
            ZLogger.d("订单不存在");
            return false;
        }

        Date rightNow = TimeUtil.getCurrentDate();

        //保存支付记录
        PosOrderPayService.get().savePayInfo(orderEntity.getId(), paymentInfo, vipMember);

        //更新订单信息
        if (vipMember != null) {
            orderEntity.setHumanId(vipMember.getId());
        }
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
        orderEntity.setUpdatedDate(rightNow);
        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("更新订单：\n%s", JSONObject.toJSONString(orderEntity)));

        return true;
    }


    /**
     * 获取选中的优惠券
     */
    public static String getSelectCouponIds(List<CouponRule> couponRules) {
        if (couponRules == null || couponRules.size() <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = couponRules.size();
        for (int i = 0; i < len; i++) {
            CouponRule coupon = couponRules.get(i);
            if (ObjectsCompact.equals(coupon.getType(), CouponRule.TYPE_RULE) ||
                    !coupon.isSelected()) {
                continue;
            }

            if (i > 0) {
                sb.append(",");
            }
            sb.append(coupon.getCouponsId());
        }

        return sb.toString();
    }

    /**
     * 获取规则ID列表，逗号分隔
     */
    public static String getRuleIds(MarketRulesWrapper mOrderMarketRules) {
        if (mOrderMarketRules == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        List<RuleBean> ruleBeans = new ArrayList<>();
        List<MarketRules> marketRulesList = mOrderMarketRules.getResults();
        if (marketRulesList != null && marketRulesList.size() > 0) {
            for (MarketRules marketRules : marketRulesList) {
                if (marketRules.getRuleBeans() != null) {
                    ruleBeans.addAll(marketRules.getRuleBeans());
                }
            }
        }

        int len = ruleBeans.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                RuleBean bean = ruleBeans.get(i);

                if (i > 0) {
                    sb.append(",");
                }
                sb.append(bean.getId());
            }
        }

        return sb.toString();
    }

    /**
     * 计算价格折扣
     */
    public static Double calculatePriceDiscount(Double denominator, Double numerator) {

        Double discount = 0D;
        if (denominator != null && denominator != 0D) {
            discount = numerator / denominator;

//            MathCompact.div()
        }

        return discount;
    }

}

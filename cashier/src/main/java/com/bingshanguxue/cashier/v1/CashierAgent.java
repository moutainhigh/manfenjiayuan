package com.bingshanguxue.cashier.v1;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.bingshanguxue.cashier.model.wrapper.LastOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.commonuseraccount.PayItem;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrder;
import com.mfh.framework.api.pmcstock.MarketRules;
import com.mfh.framework.api.pmcstock.RuleBean;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

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
     * 查询收银订单
     *
     * @param orderBarCode POS唯一订单号
     */
    public static PosOrderEntity fetchOrderEntity(String orderBarCode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and barCode = '%s'",
                MfhLoginService.get().getSpid(), BizType.POS, orderBarCode);
        List<PosOrderEntity> entities = PosOrderService.get().queryAllBy(sqlOrder);
        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        }
        return null;
    }

    public static List<PosOrderEntity> fetchOrderEntities(Integer bizType, int status) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and status = '%d'",
                MfhLoginService.get().getSpid(), bizType, status);
        return PosOrderService.get().queryAllBy(sqlOrder);
    }

    /**
     * 获取订单明细
     */
    public static List<PosOrderItemEntity> fetchOrderItems(PosOrderEntity orderEntity) {
        return PosOrderItemService.get()
                .queryAllBy(String.format("orderId = '%d'", orderEntity.getId()));
    }

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
     * 订单结算（后台拆分订单）
     *
     * @param orderBarCode     本地订单交易流水条码
     * @param outTradeNo       外部订单编号
     * @param shopcartEntities 订单明细
     */
    private static boolean simpleSettle(Integer subType,
                                        String orderBarCode, String outTradeNo,
                                        List<CashierShopcartEntity> shopcartEntities) {
        Date rightNow = new Date();
        PosOrderEntity orderEntity = fetchOrderEntity(orderBarCode);
        if (orderEntity == null) {
            orderEntity = new PosOrderEntity();
            orderEntity.setFlowId(CashierDesktopObservable.getInstance().getNextFlowId());
            orderEntity.setSellerId(MfhLoginService.get().getSpid());// 需要登录
            orderEntity.setBizType(BizType.POS);
            orderEntity.setBarCode(orderBarCode);
            orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
            orderEntity.setCreatedBy(MfhLoginService.get().getGuid());
            orderEntity.setPosId(SharedPrefesManagerFactory.getTerminalId());//设备编号
            orderEntity.setCreatedDate(rightNow);
        }
        orderEntity.setSubType(subType);
        orderEntity.setOuterTradeNo(outTradeNo);
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);//订单状态
        orderEntity.setUpdatedDate(rightNow);
        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("结算收银订单:%s_%d:\n%s", orderBarCode,
                orderEntity.getId(), JSONObject.toJSONString(orderEntity)));

        //有可能会有脏数据，订单编号一样但是流水号不一样
        PosOrderItemService.get().deleteBy(String.format("orderBarCode = '%s' or orderId = '%d'",
                orderBarCode, orderEntity.getId()));
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity goods : shopcartEntities) {
                PosOrderItemService.get().saveOrUpdate(orderEntity.getBarCode(),
                        orderEntity.getId(), goods);
            }
        }

        return true;
    }

    /**
     * 收银订单结算信息
     *
     * @param orderBarCode 订单流水号
     * @param vipMember    会员
     */
    public static CashierOrderInfo makeCashierOrderInfo(String orderBarCode,
                                                        Human vipMember) {
        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();

        cashierOrderInfo.setBizType(BizType.POS);
        cashierOrderInfo.setPosTradeNo(orderBarCode);
        cashierOrderInfo.setVipMember(vipMember);
        cashierOrderInfo.setSubject(String.format("收银订单：流水号：%s", orderBarCode));

        PosOrderEntity orderEntity = fetchOrderEntity(orderBarCode);
        if (orderEntity != null) {
            Double bCount = 0D;
            Double retailAmount = 0D;
            Double finalAmount = 0D;
            StringBuilder sbBody = new StringBuilder();
            JSONArray productsInfo = new JSONArray();
            //订单明细
            List<PosOrderItemEntity> orderItemEntityList = fetchOrderItems(orderEntity);
            if (orderItemEntityList != null && orderItemEntityList.size() > 0) {
                for (PosOrderItemEntity itemEntity : orderItemEntityList) {
                    bCount += itemEntity.getBcount();
                    retailAmount += itemEntity.getAmount();
                    finalAmount += itemEntity.getFinalAmount();

                    if (sbBody.length() > 0) {
                        sbBody.append(",");
                    }
                    sbBody.append(itemEntity.getSkuName());

                    JSONObject item = new JSONObject();
                    item.put("goodsId", itemEntity.getGoodsId());
                    item.put("skuId", itemEntity.getProSkuId());
                    item.put("bcount", itemEntity.getBcount());
                    item.put("price", itemEntity.getCostPrice());
                    item.put("factAmount", itemEntity.getFinalAmount());
                    item.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,
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

            cashierOrderInfo.setOrderId(orderEntity.getId());
            cashierOrderInfo.setbCount(bCount);
            cashierOrderInfo.setRetailAmount(retailAmount);
            cashierOrderInfo.setFinalAmount(finalAmount);
            cashierOrderInfo.setAdjustAmount(adjustAmount);
            cashierOrderInfo.setDiscountRate(discountRate);
            cashierOrderInfo.setBody(sbBody.length() > 20 ? sbBody.substring(0, 20) : sbBody.toString());
            cashierOrderInfo.setProductsInfo(productsInfo);

            //读取支付记录
            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
            if (payWrapper != null) {
                cashierOrderInfo.setPayType(payWrapper.getPayType());
                cashierOrderInfo.setPaidAmount(payWrapper.getPaidAmount()
                        + payWrapper.getVipDiscount() + payWrapper.getPromotionDiscount()
                        + payWrapper.getCouponDiscount());
            }
        }

        return cashierOrderInfo;
    }

    /**
     * 生成结算信息
     *
     * @param orderEntity
     * @param vipMember
     */
    public static CashierOrderInfo makeCashierOrderInfo(PosOrderEntity orderEntity,
                                                        Human vipMember) {
        if (orderEntity == null) {
            ZLogger.df("生成结算信息失败:订单无效。");
            return null;
        }

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();

        cashierOrderInfo.setBizType(orderEntity.getBizType());
        cashierOrderInfo.setPosTradeNo(orderEntity.getBarCode());
        cashierOrderInfo.setVipMember(vipMember);
        cashierOrderInfo.setSubject(String.format("订单信息：流水号：%s，交易类型：%s",
                orderEntity.getBarCode(), BizType.name(orderEntity.getBizType())));

        Double bCount = 0D;
        Double retailAmount = 0D;
        Double finalAmount = 0D;
        StringBuilder sbBody = new StringBuilder();
        JSONArray productsInfo = new JSONArray();
        List<PosOrderItemEntity> orderItemEntityList = fetchOrderItems(orderEntity);
        if (orderItemEntityList != null && orderItemEntityList.size() > 0) {
            for (PosOrderItemEntity itemEntity : orderItemEntityList) {
                bCount += itemEntity.getBcount();
                retailAmount += itemEntity.getAmount();
                finalAmount += itemEntity.getFinalAmount();

                if (sbBody.length() > 0) {
                    sbBody.append(",");
                }
                sbBody.append(itemEntity.getSkuName());

                JSONObject item = new JSONObject();
                item.put("goodsId", itemEntity.getGoodsId());
                item.put("skuId", itemEntity.getProSkuId());
                item.put("bcount", itemEntity.getBcount());
                item.put("price", itemEntity.getCostPrice());
                item.put("factAmount", itemEntity.getFinalAmount());
                item.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,
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

        cashierOrderInfo.setOrderId(orderEntity.getId());
        cashierOrderInfo.setbCount(bCount);
        cashierOrderInfo.setRetailAmount(retailAmount);
        cashierOrderInfo.setFinalAmount(finalAmount);
        cashierOrderInfo.setAdjustAmount(adjustAmount);
        cashierOrderInfo.setDiscountRate(discountRate);
        cashierOrderInfo.setBody(sbBody.length() > 20 ? sbBody.substring(0, 20) : sbBody.toString());
        cashierOrderInfo.setProductsInfo(productsInfo);

        //读取支付记录
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        if (payWrapper != null) {
            cashierOrderInfo.setPayType(payWrapper.getPayType());
            cashierOrderInfo.setPaidAmount(payWrapper.getPaidAmount() + payWrapper.getVipDiscount()
                    + payWrapper.getPromotionDiscount() + payWrapper.getCouponDiscount());
        }

        return cashierOrderInfo;
    }

    /**
     * 结算
     */
    public static CashierOrderInfo settle(Integer subType, String orderBarCode,
                                          String outTradeNo, int status,
                                          List<CashierShopcartEntity> shopcartEntities) {
        //创建or更新订单，保存or更新订单明细
        simpleSettle(subType, orderBarCode, outTradeNo, shopcartEntities);
        //生成订单支付信息
        CashierOrderInfo cashierOrderInfo = makeCashierOrderInfo(orderBarCode, null);
        // 7/5/16  修复初始状态，订单金额为空的问题。
        updateCashierOrder(cashierOrderInfo, null, status);

        return cashierOrderInfo;
    }

    /**
     * 调单1
     */
    public static List<PosOrderItemEntity> resume(String orderBarCode) {
        PosOrderEntity orderEntity = fetchOrderEntity(orderBarCode);
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
        PosOrderEntity orderEntity = fetchOrderEntity(cashierOrderInfo.getPosTradeNo());
        if (orderEntity == null) {
            return false;
        }

        orderEntity.setRetailAmount(cashierOrderInfo.getRetailAmount());
        orderEntity.setFinalAmount(cashierOrderInfo.getFinalAmount());
        orderEntity.setDiscountAmount(cashierOrderInfo.getAdjustAmount());//折扣价
        orderEntity.setBcount(cashierOrderInfo.getbCount());

        if (payAmount != null) {
            Map<String, Double> rpDisAmountMap = payAmount.getRpDisAmountMap();
            List<PayItem> payItemList = payAmount.getPayItemList();

            if (rpDisAmountMap != null) {
                orderEntity.setRpDisAmountMap(JSONObject.toJSONString(rpDisAmountMap));
            } else {
                orderEntity.setRpDisAmountMap(null);
            }

            if (payItemList != null && payItemList.size() > 0) {
                for (PayItem payItem : payItemList) {
                    List<PosOrderItemEntity> orderItemEntities = PosOrderItemService.get()
                            .queryAllBy(String.format("goodsId = '%d'", payItem.getGoodsId()));
                    if (orderItemEntities != null) {
                        for (PosOrderItemEntity orderItemEntity : orderItemEntities) {
                            //计算实际会员规则优惠金额
                            Double vipAmount = MathCompact.sub(payItem.getFactAmount(), payItem.getSaleAmount());
                            orderItemEntity.setVipAmount(vipAmount);
                            orderItemEntity.setRuleAmountMap(JSONObject.toJSONString(payItem.getRuleAmountMap()));
                            PosOrderItemService.get().saveOrUpdate(orderItemEntity);
                        }
                    } else {
                        ZLogger.d("未找到订单商品明细");
                    }
                }
            }
        } else {
            orderEntity.setRpDisAmountMap(null);
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
        } else {
            orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_NO);
        }

        orderEntity.setUpdatedDate(new Date());
        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("更新订单 %s_%d:\n%s", orderEntity.getBarCode(),
                orderEntity.getId(), JSONObject.toJSONString(orderEntity)));
        return true;
    }

    /**
     * 保存支付记录并更新支付订单
     *
     * @param bizType      业务类型
     * @param orderBarCode 订单流水号
     * @param vipMember    会员信息
     * @param paymentInfo  订单支付信息
     *                     适用场景：收银支付金额或者状态发生改变
     */
    public static boolean updateCashierOrder(String orderBarCode, Human vipMember,
                                             PaymentInfo paymentInfo) {

        //参数检查
        if (paymentInfo == null) {
            return false;
        }

        PosOrderEntity orderEntity = fetchOrderEntity(orderBarCode);
        if (orderEntity == null) {
            ZLogger.df("订单不存在");
            return false;
        }

        //保存支付记录
        PosOrderPayService.get().savePayInfo(orderEntity.getId(), paymentInfo, vipMember);


        //更新订单信息
        if (vipMember != null) {
            orderEntity.setHumanId(vipMember.getId());
        }
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
        orderEntity.setUpdatedDate(new Date());
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
    public static Double calculatePriceDiscount(Double costPrice, Double finalPrice) {

        Double discount = 0D;
        if (costPrice != null && costPrice != 0D) {
            discount = finalPrice / costPrice;
        }

        return discount;
    }

}

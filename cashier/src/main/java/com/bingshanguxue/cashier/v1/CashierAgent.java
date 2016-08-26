package com.bingshanguxue.cashier.v1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.MarketRules;
import com.bingshanguxue.cashier.model.OrderMarketRules;
import com.bingshanguxue.cashier.model.RuleBean;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.bingshanguxue.cashier.model.wrapper.DiscountInfo;
import com.bingshanguxue.cashier.model.wrapper.LastOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfo;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfoImpl;
import com.bingshanguxue.vector_user.bean.Human;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;

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
     * 查询订单
     */
    public static PosOrderEntity fetchOrderEntity(Integer bizType, String orderBarCode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and barCode = '%s'",
                MfhLoginService.get().getSpid(), bizType, orderBarCode);
        List<PosOrderEntity> entities = PosOrderService.get().queryAllBy(sqlOrder);
        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        }
        return null;
    }

    /**
     * 获取订单明细
     * */
    public static List<PosOrderItemEntity> fetchOrderItems(PosOrderEntity orderEntity) {
        return PosOrderItemService.get()
                .queryAllBy(String.format("orderId = '%s'", orderEntity.getId()));
    }

    /**
     * 上一订单信息
     * */
    public static LastOrderInfo genLastOrderInfo(PosOrderEntity orderEntity){
        LastOrderInfo lastOrderInfo = null;

        if (orderEntity != null) {
            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());

            lastOrderInfo = new LastOrderInfo();
            lastOrderInfo.setPayType(lastOrderInfo.getPayType() | payWrapper.getPayType());
            lastOrderInfo.setFinalAmount(lastOrderInfo.getFinalAmount() + orderEntity.getFinalAmount());
            lastOrderInfo.setbCount(lastOrderInfo.getbCount() + orderEntity.getBcount());
            lastOrderInfo.setDiscountAmount(lastOrderInfo.getDiscountAmount() + payWrapper.getRuleDiscount());
            lastOrderInfo.setChangeAmount(lastOrderInfo.getChangeAmount() + payWrapper.getChange());
        }

        return lastOrderInfo;
    }

    /**
     * 订单结算（后台拆分订单）
     *
     * @param orderBarCode     订单交易流水条码
     * @param shopcartEntities 订单明细
     */
    public static boolean simpleSettle(String orderBarCode, List<CashierShopcartEntity> shopcartEntities) {
        //保存or更新订单
        PosOrderEntity orderEntity = fetchOrderEntity(BizType.POS, orderBarCode);
        if (orderEntity == null) {
            orderEntity = new PosOrderEntity();
            orderEntity.setSellerId(MfhLoginService.get().getSpid());// 需要登录
            orderEntity.setBizType(BizType.POS);
            orderEntity.setBarCode(orderBarCode);
            orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
            orderEntity.setCreatedBy(String.valueOf(MfhLoginService.get().getCurrentGuId()));
            orderEntity.setPosId(SharedPreferencesManager.getTerminalId());//设备编号
            orderEntity.setCreatedDate(new Date());
        }
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);//订单状态
        orderEntity.setUpdatedDate(new Date());
        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("结算订单:%s_%d:\n%s", orderBarCode,
                orderEntity.getId(), JSONObject.toJSONString(orderEntity)));

        //保存or更新订单明细
        PosOrderItemService.get().deleteBy(String.format("orderBarCode = '%s'",
                orderBarCode));
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity goods : shopcartEntities) {
                PosOrderItemService.get().saveOrUpdate(orderEntity.getBarCode(),
                        orderEntity.getId(), goods);
            }
        }

        return true;
    }

    /**
     * 订单结算信息
     *
     * @param bizType
     * @param orderBarCode           订单流水号
     * @param customerMembershipInfo 会员
     */
    public static CashierOrderInfo makeCashierOrderInfo(Integer bizType,
                                                        String orderBarCode,
                                                        Human customerMembershipInfo) {
        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();

        String subject = String.format("订单信息：流水号：%s，交易类型：%s",
                orderBarCode, BizType.name(bizType));
        //加载拆分订单
        List<CashierOrderItemInfo> cashierOrderItemInfos = new ArrayList<>();
        Double paidAmount = 0D;
        PosOrderEntity orderEntity = fetchOrderEntity(bizType, orderBarCode);
        if (orderEntity != null){
            //加载订单明细
            cashierOrderItemInfos.add(genCashierorderItemInfo(orderEntity));
            //加载已支付金额
            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
            paidAmount += payWrapper.getPaidAmount();
        }

        cashierOrderInfo.initCashierSetle(orderBarCode, bizType, cashierOrderItemInfos,
                subject, customerMembershipInfo, paidAmount);

        return cashierOrderInfo;
    }

    /**
     * 生成订单结算明细信息
     * @param orderEntity 收银订单
     */
    public static CashierOrderItemInfo genCashierorderItemInfo(PosOrderEntity orderEntity) {
        Double bCount = 0D;
        Double retailAmount = 0D;
        Double finalAmount = 0D;
        Double discountAmount = 0D;
        Double discountRate = 0D;
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
                sbBody.append(itemEntity.getName());

                JSONObject item = new JSONObject();
                item.put("goodsId", itemEntity.getGoodsId());
                item.put("skuId", itemEntity.getProSkuId());
                item.put("bcount", itemEntity.getBcount());
                item.put("price", itemEntity.getCostPrice());
                item.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,
                productsInfo.add(item);
            }
        }
        discountAmount = retailAmount - finalAmount;
        if (retailAmount == 0D) {
            discountRate = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
        } else {
            discountRate = finalAmount / retailAmount;
        }

        CashierOrderItemInfo orderItemInfo = new CashierOrderItemInfo();
        orderItemInfo.setOrderId(orderEntity.getId());
        orderItemInfo.setbCount(bCount);
        orderItemInfo.setRetailAmount(retailAmount);
        orderItemInfo.setFinalAmount(finalAmount);
        orderItemInfo.setAdjustDiscountAmount(discountAmount);
        orderItemInfo.setDiscountRate(discountRate);
        orderItemInfo.setBrief(sbBody.length() > 20 ? sbBody.substring(0, 20) : sbBody.toString());
        orderItemInfo.setProductsInfo(productsInfo);
        orderItemInfo.setDiscountInfo(new DiscountInfo(orderEntity.getId()));

        //读取支付记录
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        if (payWrapper != null){
            orderItemInfo.setPayType(payWrapper.getPayType());
            orderItemInfo.setPaidAmount(payWrapper.getPaidAmount());
            orderItemInfo.setChange(payWrapper.getChange());
        }

        ZLogger.d(JSON.toJSONString(orderItemInfo));
        return orderItemInfo;
    }

    /**
     * 结算
     */
    public static CashierOrderInfo settle(String orderBarCode, int status,
                                          List<CashierShopcartEntity> shopcartEntities) {
        //创建or更新订单，保存or更新订单明细
        simpleSettle(orderBarCode, shopcartEntities);
        CashierOrderInfo cashierOrderInfo = makeCashierOrderInfo(BizType.POS,
                orderBarCode, null);
        // 7/5/16  修复初始状态，订单金额为空的问题。
        updateCashierOrder(cashierOrderInfo, status);

        return cashierOrderInfo;
    }

    /**
     * 调单1
     */
    public static List<PosOrderItemEntity> resume(String orderBarCode) {
        List<PosOrderEntity> orderEntities = CashierFactory
                .fetchActiveOrderEntities(BizType.POS, orderBarCode);
        if (orderEntities == null || orderEntities.size() < 1) {
            return null;
        }

        List<PosOrderItemEntity> itemEntities = new ArrayList<>();

        for (PosOrderEntity orderEntity : orderEntities) {
            //修改订单状态：挂单改为待支付
            orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
            PosOrderService.get().saveOrUpdate(orderEntity);

            //加载订单明细
            List<PosOrderItemEntity> temp = PosOrderItemService.get()
                    .queryAllBy(String.format("orderId = '%d'", orderEntity.getId()));
            if (temp != null) {
                itemEntities.addAll(temp);
            }
        }

        return itemEntities;
    }

    /**
     * 更新收银订单
     *
     * @param cashierOrderInfo 订单支付信息
     * @param status           订单状态
     */
    public static boolean updateCashierOrder(CashierOrderInfo cashierOrderInfo, int status) {
        if (cashierOrderInfo == null) {
            return false;
        }
        PosOrderEntity orderEntity = fetchOrderEntity(cashierOrderInfo.getBizType(),
                        cashierOrderInfo.getPosTradeNo());
        if (orderEntity == null) {
            return false;
        }

        CashierOrderItemInfo cashierOrderItemInfo = CashierOrderInfoImpl
                .getCashierOrderItemInfo(cashierOrderInfo, orderEntity.getId());
        if (cashierOrderItemInfo == null) {
            return false;
        }

        orderEntity.setUpdatedDate(new Date());
        orderEntity.setRetailAmount(cashierOrderItemInfo.getRetailAmount());
        orderEntity.setFinalAmount(cashierOrderItemInfo.getFinalAmount());
        orderEntity.setDiscountAmount(cashierOrderItemInfo.getAdjustDiscountAmount());//折扣价
        orderEntity.setBcount(cashierOrderItemInfo.getbCount());
//        orderEntity.setBizType(cashierOrderInfo.getBizType());
//            orderEntity.setCouponsIds(cashierOrderItemInfo.getCouponsIds());
//        orderEntity.setRuleIds(cashierOrderInfo.getRuleIds());
//            orderEntity.setPayType(cashierOrderInfo.getPayType());

        Human human = cashierOrderInfo.getVipMember();
        if (human != null) {
            orderEntity.setHumanId(human.getId());
            orderEntity.setScore(0D);//会员积分
        }

        //支付完成
        orderEntity.setStatus(status);
        if (status == PosOrderEntity.ORDER_STATUS_FINISH) {
//            ZLogger.d("订单已经支付");
            orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_YES);
        } else {
            orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_NO);
//            ZLogger.d("订单未支付");
        }

        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("更新订单 %s_%d:\n%s", orderEntity.getBarCode(),
                orderEntity.getId(), JSONObject.toJSONString(orderEntity)));
        return true;
    }

    /**
     * 保存支付记录并更新支付订单
     *
     * @param cashierOrderInfo 订单结算信息
     * @param paymentInfo      订单支付信息
     *                         适用场景：收银支付金额或者状态发生改变
     */
    public static boolean updateCashierOrder(CashierOrderInfo cashierOrderInfo,
                                             PaymentInfo paymentInfo) {

        //参数检查
        if (cashierOrderInfo == null || paymentInfo == null) {
            return false;
        }

        //更新日期
        Date updateDate = new Date();
        //订单流水号
        String orderBarCode = cashierOrderInfo.getPosTradeNo();
        //会员信息
        Human vipMember = cashierOrderInfo.getVipMember();


        PosOrderEntity orderEntity = null;
        List<PosOrderEntity> orderEntities = CashierFactory
                .fetchActiveOrderEntities(cashierOrderInfo.getBizType(), orderBarCode);
        if (orderEntities != null && orderEntities.size() > 0) {
            orderEntity = orderEntities.get(0);
        }
        if (orderEntity == null) {
            ZLogger.df("订单不存在");
            return false;
        }

        //保存支付记录
        PaymentInfoImpl.saveOrUpdate(paymentInfo, orderEntity, vipMember);

        //更新订单信息
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        orderEntity.setPayType(payWrapper.getPayType());
        orderEntity.setChange(payWrapper.getChange());
        orderEntity.setPaidAmount(payWrapper.getPaidAmount());
        orderEntity.setRuleDiscountAmount(payWrapper.getRuleDiscount());
        orderEntity.setCouponsIds(payWrapper.getCouponsIds());
        orderEntity.setRuleIds(payWrapper.getRuleIds());
        if (vipMember != null) {
            orderEntity.setHumanId(vipMember.getId());
        }
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
        orderEntity.setUpdatedDate(updateDate);

        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("更新订单：\n%s", JSONObject.toJSONString(orderEntity)));

        return true;
    }

    public static String getSelectCouponIds(Map<Long, List<CouponRule>> couponsMap,
                                            Long splitOrderId) {
        if (couponsMap == null || splitOrderId == null) {
            return null;
        }
        return getSelectCouponIds(couponsMap.get(splitOrderId));
    }

    public static String getSelectCouponIds(List<CouponRule> set) {
        StringBuilder sb = new StringBuilder();
        if (set != null && set.size() > 0) {
            int len = set.size();
            for (int i = 0; i < len; i++) {
                CouponRule coupon = set.get(i);
                if (ObjectsCompact.equals(coupon.getType(), CouponRule.TYPE_RULE) ||
                        !coupon.isSelected()) {
                    continue;
                }

                if (i > 0) {
                    sb.append(",");
                }
                sb.append(coupon.getCouponsId());
            }
        }

        return sb.toString();
    }

    /**
     * 获取规则ID列表，逗号分隔
     */
    public static String getRuleIds(OrderMarketRules mOrderMarketRules) {
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

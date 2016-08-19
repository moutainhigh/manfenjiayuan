package com.bingshanguxue.cashier;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.MarketRules;
import com.bingshanguxue.cashier.model.OrderMarketRules;
import com.bingshanguxue.cashier.model.RuleBean;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfoImpl;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderItemInfo;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfo;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfoImpl;
import com.bingshanguxue.vector_user.bean.Human;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.logger.ZLogger;
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
     * 订单结算（后台拆分订单）
     * @param orderBarCode 订单交易流水条码
     * @param shopcartEntities 订单明细
     */
    public static boolean simpleSettle(String orderBarCode, List<CashierShopcartEntity> shopcartEntities) {
        PosOrderEntity orderEntity;

        List<PosOrderEntity> orderEntities = CashierFactory.fetchOrderEntities(BizType.POS, orderBarCode);
        if (orderEntities != null && orderEntities.size() > 0) {
            orderEntity = orderEntities.get(0);
        } else {
            orderEntity = new PosOrderEntity();
            orderEntity.setBarCode(orderBarCode);
            orderEntity.setBizType(BizType.POS);
            orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
            orderEntity.setSellerId(MfhLoginService.get().getSpid());// 需要登录
            orderEntity.setCreatedBy(String.valueOf(MfhLoginService.get().getCurrentGuId()));
            orderEntity.setPosId(SharedPreferencesManager.getTerminalId());//设备编号
            orderEntity.setCreatedDate(new Date());
        }
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);//订单状态
        orderEntity.setUpdatedDate(new Date());
        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("订单%s_%d:\n%s", orderBarCode,
                orderEntity.getId(), JSONObject.toJSONString(orderEntity)));

        //更新订单明细
        PosOrderItemService.get().deleteBy(String.format("orderBarCode = '%s'",
                orderBarCode));
        for (CashierShopcartEntity goods : shopcartEntities) {
            PosOrderItemService.get().saveOrUpdate(orderEntity.getBarCode(),
                    orderEntity.getId(), goods);
        }

        return true;
    }

    /**
     * 订单结算信息
     * @param bizType
     * @param orderBarCode 订单流水号
     * @param customerMembershipInfo 会员
     * */
    public static CashierOrderInfo makeCashierOrderInfo(Integer bizType,
                                                        String orderBarCode,
                                                        Human customerMembershipInfo) {

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();

        String subject = String.format("订单信息：流水号：%s，交易类型：%s",
                orderBarCode, BizType.name(bizType));
        //加载拆分订单
        List<CashierOrderItemInfo> cashierOrderItemInfos = new ArrayList<>();
        Double paidAmount = 0D;
        List<PosOrderEntity> orderEntities = CashierFactory.fetchActiveOrderEntities(bizType, orderBarCode);
        if (orderEntities != null && orderEntities.size() > 0){
            PosOrderEntity orderEntity = orderEntities.get(0);
            //加载订单明细
            cashierOrderItemInfos.add(CashierFactory.genCashierorderItemInfo(orderEntity));
            //加载已支付金额
            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
            paidAmount += payWrapper.getPaidAmount();
        }

        cashierOrderInfo.initCashierSetle(orderBarCode, bizType, cashierOrderItemInfos,
                subject, customerMembershipInfo, paidAmount);

        return cashierOrderInfo;
    }

    /**
     * 结算
     */
    public static CashierOrderInfo settle(String orderBarCode, int status,
                                          List<CashierShopcartEntity> shopcartEntities) {
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
        List<PosOrderEntity> orderEntities = CashierFactory
                .fetchActiveOrderEntities(cashierOrderInfo.getBizType(),
                        cashierOrderInfo.getPosTradeNo());
        if (orderEntities == null || orderEntities.size() <= 0) {
            return false;
        }

        Date updateDate = new Date();
        for (PosOrderEntity orderEntity : orderEntities) {
            CashierOrderItemInfo cashierOrderItemInfo = CashierOrderInfoImpl
                    .getCashierOrderItemInfo(cashierOrderInfo, orderEntity.getId());
            if (cashierOrderItemInfo == null) {
                continue;
            }

            orderEntity.setUpdatedDate(updateDate);
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
        }
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
        if (orderEntity == null){
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

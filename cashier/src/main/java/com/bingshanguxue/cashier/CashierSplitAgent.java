package com.bingshanguxue.cashier;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfoImpl;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderItemInfo;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.vector_user.bean.Human;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单拆分机器
 * Created by bingshanguxue on 8/19/16.
 */
@Deprecated
public class CashierSplitAgent {

    /**
     * 结算（POS拆分订单）
     *
     * @param posTradeNo
     * @param shopcartEntities <ol>
     *                         结算
     *                         <li>判断当前收银台购物车的商品是否为空，若不为空，则继续第2步，否则结束；</li>
     *                         <li>生成订单,［并拆单］；</li>
     *                         <li>更新订单明细（需要删除历史记录）；</li>
     *                         <li>结束</li>
     *                         <li>注意，这里保存后的订单里的金额可能还和明细没有对得上，还需要生成结算信息，再次保存。</li>
     *                         </ol>
     */
    public static boolean splitSettle(String orderBarCode, List<CashierShopcartEntity> shopcartEntities) {
        //Step 1 按产品线拆单
        Map<Integer, List<CashierShopcartEntity>> splitMap = new HashMap<>();

        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity shopcartEntity : shopcartEntities) {
                Integer prodLineId = shopcartEntity.getProdLineId();
                List<CashierShopcartEntity> tempEntities = splitMap.get(prodLineId);
                if (tempEntities == null) {
                    tempEntities = new ArrayList<>();
                }
                tempEntities.add(shopcartEntity);

                splitMap.put(prodLineId, tempEntities);
                ZLogger.df(String.format("拆单--产品线编号:%d, %s/%s", prodLineId,
                        shopcartEntity.getBarcode(), shopcartEntity.getName()));
            }
        }

        //Step 3.1 更新已有订单
        List<PosOrderEntity> orderEntities = CashierFactory.fetchOrderEntities(BizType.POS, orderBarCode);
        if (orderEntities != null && orderEntities.size() > 0) {
            for (PosOrderEntity orderEntity : orderEntities) {
                Integer subType = orderEntity.getSubType();
                if (splitMap.containsKey(subType)) {
                    orderEntity.setIsActive(1);
                    orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
                    PosOrderService.get().saveOrUpdate(orderEntity);

                    //更新订单明细
                    PosOrderItemService.get().deleteBy(String.format("orderId = '%d'",
                            orderEntity.getId()));
                    List<CashierShopcartEntity> goodsEntities = splitMap.get(subType);
                    for (CashierShopcartEntity goods : goodsEntities) {
                        PosOrderItemService.get().saveOrUpdate(orderEntity.getBarCode(),
                                orderEntity.getId(), goods);
                    }

                    // TODO: 7/7/16 订单明细统计数据保存到订单
                    splitMap.remove(subType);
                } else {
                    //关闭订单，订单明细不清空
                    orderEntity.setIsActive(0);
                    PosOrderService.get().saveOrUpdate(orderEntity);
                }

                ZLogger.df(String.format("订单%s_%d:\n%s", orderBarCode,
                        orderEntity.getId(), JSONObject.toJSONString(orderEntity)));
            }
        }

        //Step 3.2 保存新订单和商品明细
        for (Integer subType : splitMap.keySet()) {
            PosOrderEntity orderEntity = new PosOrderEntity();
            orderEntity.setBarCode(orderBarCode);
            orderEntity.setBizType(BizType.POS);
            orderEntity.setSubType(subType);
            orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
            orderEntity.setSellerId(MfhLoginService.get().getSpid());// 需要登录
            orderEntity.setCreatedBy(String.valueOf(MfhLoginService.get().getCurrentGuId()));
            orderEntity.setPosId(SharedPreferencesManager.getTerminalId());//设备编号
            orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);//订单状态
            orderEntity.setCreatedDate(new Date());
            orderEntity.setUpdatedDate(new Date());
            PosOrderService.get().saveOrUpdate(orderEntity);
            ZLogger.df(String.format("订单%s_%d:\n%s", orderBarCode,
                    orderEntity.getId(), JSONObject.toJSONString(orderEntity)));

            List<CashierShopcartEntity> goodsEntities = splitMap.get(subType);
            for (CashierShopcartEntity goods : goodsEntities) {
                PosOrderItemService.get().saveOrUpdate(orderEntity.getBarCode(),
                        orderEntity.getId(), goods);
            }
        }

        return true;
    }

    /**
     * 订单结算信息
     * @param orderBarCode 订单流水号
     * */
    public static CashierOrderInfo makeCashierOrderInfo(Integer bizType,
                                                        String orderBarCode,
                                                        Human customerMembershipInfo) {
        String subject = String.format("订单信息：流水号：%s，交易类型：%s",
                orderBarCode, BizType.name(bizType));

        //加载拆分订单
        List<CashierOrderItemInfo> cashierOrderItemInfos = new ArrayList<>();
        Double paidAmount = 0D;
        List<PosOrderEntity> orderEntities = CashierFactory.fetchActiveOrderEntities(bizType, orderBarCode);
        for (PosOrderEntity orderEntity : orderEntities) {
            cashierOrderItemInfos.add(CashierFactory.genCashierorderItemInfo(orderEntity));

            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
            paidAmount += payWrapper.getPaidAmount();
        }

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.initCashierSetle(orderBarCode, bizType, cashierOrderItemInfos,
                subject, customerMembershipInfo, paidAmount);

        return cashierOrderInfo;
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
}

package com.bingshanguxue.cashier.v1;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.HangupOrder;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesUltimate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 收银信息
 * Created by bingshanguxue on 7/2/16.
 */
public class CashierProvider {
    public static final String PREF_NAME_CASHIER = "pref_cashier_base";
    private static final String PK_LAST_CASHIER_DATETIME = "last_cashier_datetime";  //上一次收银时间
    private static final String PK_LAST_CASHIER_FLOWNUMBER = "last_cashier_flownumber";   //上一次收银流水编号

    /**
     * 获取未支付的金额
     */
    public static Double getUnpayAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        return cashierOrderInfo.getFinalAmount() - cashierOrderInfo.getPaidAmount();
    }

    /**
     * 计算当前应该支付金额, >0,表示应收金额; <0,表示找零金额
     * 公式：应收金额＝（商品总金额－价格调整）－已付金额 －促销优惠 －卡券优惠
     */
    public static Double getHandleAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        Double amount = MathCompact.sub(cashierOrderInfo.getFinalAmount(), cashierOrderInfo.getPaidAmount());
        PayAmount payAmount = cashierOrderInfo.getPayAmount();
        if (payAmount != null) {
            amount -= payAmount.getRuleAmount();
//            amount -= payAmount.getPackRuleAmount();
            amount -= payAmount.getCoupAmount();
        }

        //实际场景中应付金额不会小于1分钱，所以这里要保留两位小数
        //2016-07-04，判断需要放在循环里，因为折扣券是对拆分后的子订单生效，不是整个大订单
        //2016-08－15，现金支付完成后，重新计算应付金额，负数被忽略导致支付窗口没有关闭。
        if (amount < 0.01) {
            amount = 0D;
        }

        //精确到分
        return Double.valueOf(String.format("%.2f", amount));
    }

    /**
     * 更新并返回最新的流水编号
     */
    public static Long nextFlowId() {
        Date rightNow = TimeUtil.getCurrentDate();
        Long flowId;

        Date saveDate;
        String saveDateStr = SharedPrefesUltimate
                .getString(PREF_NAME_CASHIER, PK_LAST_CASHIER_DATETIME);
        if (StringUtils.isEmpty(saveDateStr)) {
            saveDate = rightNow;
            flowId = 1L;
        } else {
            saveDate = TimeUtil.parse(saveDateStr, TimeUtil.FORMAT_YYYYMMDDHHMMSS);
            if (TimeUtil.isSameDay(rightNow, saveDate)) {
                ZLogger.d("上一次收银时间和当前时间是同一天，流水编号 ＋1");
                Long saveId = SharedPrefesUltimate
                        .getLong(PREF_NAME_CASHIER, PK_LAST_CASHIER_FLOWNUMBER, 1L);

                flowId = saveId + 1;
            } else {
                ZLogger.d("上一次收银时间和当前时间不是同一天，需要重置流水编号");
                flowId = 1L;
            }
        }

        SharedPrefesUltimate
                .set(PREF_NAME_CASHIER, PK_LAST_CASHIER_DATETIME
                        , TimeUtil.format(saveDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        SharedPrefesUltimate
                .set(PREF_NAME_CASHIER, PK_LAST_CASHIER_FLOWNUMBER, flowId);

        return flowId;
    }

    /**
     * 查询收银订单
     *
     * @param orderBarcode POS唯一订单号
     */
    public static PosOrderEntity fetchOrderEntity(String orderBarcode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and barCode = '%s'",
                MfhLoginService.get().getSpid(), BizType.POS, orderBarcode);
        List<PosOrderEntity> entities = PosOrderService.get().queryAllBy(sqlOrder);
        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        }
        return null;
    }

    /**
     * 获取订单明细
     */
    public static List<PosOrderItemEntity> fetchOrderItems(PosOrderEntity orderEntity) {
        return PosOrderItemService.get()
                .queryAllBy(String.format("orderId = '%d'", orderEntity.getId()));
    }

    /**
     * 查询挂单状态的收银订单
     */
    public static List<HangupOrder> fetchHangupOrders() {
        List<HangupOrder> hangupOrders = new ArrayList<>();

        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and status = '%d'",
                MfhLoginService.get().getSpid(), BizType.POS, PosOrderEntity.ORDER_STATUS_HANGUP);
        List<PosOrderEntity> orderEntities = PosOrderService.get().queryAllBy(sqlOrder);

        if (orderEntities != null && orderEntities.size() > 0) {
            for (PosOrderEntity orderEntity : orderEntities) {
                HangupOrder hangupOrder = new HangupOrder();
                hangupOrder.setOrderTradeNo(orderEntity.getBarCode());
                hangupOrder.setFinalAmount(orderEntity.getFinalAmount());
                hangupOrder.setUpdateDate(orderEntity.getUpdatedDate());

                hangupOrders.add(hangupOrder);
            }
        }

        return hangupOrders;
    }

    /**
     * 收银订单结算信息
     *
     * @param orderBarCode 订单流水号
     * @param vipMember    会员
     */
    public static CashierOrderInfo createCashierOrderInfo(String orderBarCode,
                                                        Human vipMember) {
        PosOrderEntity orderEntity = fetchOrderEntity(orderBarCode);
        if (orderEntity == null) {
            return null;
        }

        return createCashierOrderInfo(orderEntity, vipMember);
    }

    public static CashierOrderInfo createCashierOrderInfo(PosOrderEntity orderEntity,
                                                          Human vipMember) {
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

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.setBizType(BizType.POS);
        cashierOrderInfo.setPosTradeNo(orderEntity.getBarCode());
        cashierOrderInfo.setVipMember(vipMember);
        cashierOrderInfo.setSubject(String.format("订单流水号：%s", orderEntity.getBarCode()));
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

        ZLogger.df(String.format("订单支付信息:%s", JSONObject.toJSONString(cashierOrderInfo)));
        return cashierOrderInfo;
    }
}

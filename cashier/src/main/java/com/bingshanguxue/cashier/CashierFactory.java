package com.bingshanguxue.cashier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderItemInfo;
import com.bingshanguxue.cashier.model.wrapper.DiscountInfo;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.mfh.framework.api.constant.BizType;
import com.bingshanguxue.vector_user.bean.Human;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 7/7/16.
 */
public class CashierFactory {

    /**
     * 获取订单列表*/
    public static List<PosOrderEntity> fetchOrderEntities(Integer bizType,
                                                                String orderBarCode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' " +
                        "and barCode = '%s'",
                MfhLoginService.get().getSpid(), bizType, orderBarCode);
        return PosOrderService.get().queryAllBy(sqlOrder);
    }

    /**
     * 查找激活状态的订单
     * @param orderBarCode 订单流水号
     * */
    public static List<PosOrderEntity> fetchActiveOrderEntities(Integer bizType,
                                                                String orderBarCode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' " +
                        "and barCode = '%s' and isActive = '%d'",
                MfhLoginService.get().getSpid(), bizType, orderBarCode, PosOrderEntity.ACTIVE);
        return PosOrderService.get().queryAllBy(sqlOrder);
    }

    public static List<PosOrderEntity> fetchActiveOrderEntities(Integer bizType, int status) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' " +
                        "and isActive = '%d' and status = '%d'",
                MfhLoginService.get().getSpid(), bizType, PosOrderEntity.ACTIVE, status);
        return PosOrderService.get().queryAllBy(sqlOrder);
    }


    /**
     * 查找激活状态的订单明细
     * @param orderBarCode 订单流水号
     * */
    public static List<PosOrderItemEntity> fetchActiveOrderItems(String orderBarCode) {
        return fetchOrderItems(CashierFactory.fetchActiveOrderEntities(BizType.POS, orderBarCode));
    }

    public static List<PosOrderItemEntity> fetchOrderItems(List<PosOrderEntity> orderEntities) {
        if (orderEntities == null || orderEntities.size() < 1) {
            return null;
        }

        List<PosOrderItemEntity> itemEntities = new ArrayList<>();

        for (PosOrderEntity orderEntity : orderEntities) {
            //加载订单明细
            List<PosOrderItemEntity> temp = PosOrderItemService.get()
                    .queryAllBy(String.format("orderId = '%s'", orderEntity.getId()));
            if (temp != null){
                itemEntities.addAll(temp);
            }
        }

        return itemEntities;
    }

    public static List<PosOrderItemEntity> fetchOrderItems(PosOrderEntity orderEntity) {
        return PosOrderItemService.get()
                .queryAllBy(String.format("orderId = '%s'", orderEntity.getId()));
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
        List<PosOrderEntity> orderEntities = fetchActiveOrderEntities(bizType, orderBarCode);
        for (PosOrderEntity orderEntity : orderEntities) {
            cashierOrderItemInfos.add(genCashierorderItemInfo(orderEntity));

            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
            paidAmount += payWrapper.getPaidAmount();
        }

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
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
     * 计算拆分子订单实际分配的支付金额,最多两位小数
     * 公式＝拆分订单金额/流水订单金额 * 实际支付金额
     */
    public static Double allocationPayableAmount(Double numerator, Double denominator, Double factor) {
        if (denominator == null || denominator.compareTo(0D) == 0
                || numerator == null || numerator.compareTo(0D) == 0
                || factor == null || factor.compareTo(0D) == 0) {
//            ZLogger.df(String.format("(%.2f / %.2f) * %.2f = %.2f",
//                    numerator, denominator, factor, 0D));
            return 0D;
        }

        Double result = (numerator / denominator) * factor;
        ZLogger.df(String.format("(%.2f / %.2f) * %.2f = %.2f",
                numerator, denominator, factor, result));
        return Double.valueOf(String.format("%.2f", result));
    }


    /**
     * 生成商户订单号(64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。)
     * 终端号_订单编号_时间戳(13位)
     * @param orderId 订单编号
     * @param timeStampEnabled 是否添加时间戳在后面
     *
     * */
    public static String genTradeNo(Long orderId, boolean timeStampEnabled){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%d", SharedPreferencesManager.getTerminalId(),
                orderId));
        if (timeStampEnabled){
            sb.append("_").append(System.currentTimeMillis());
        }

        return sb.toString();
    }

    /**
     * 生成商户订单号(64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。)
     * 格式：终端号_业务类型_支付类型_订单编号_时间戳(13位)
     * @param orderId 订单编号
     * @param timeStampEnabled 是否添加时间戳在后面
     * */
    public static String genTradeNo(Integer bizType, Integer payType, Long orderId, boolean timeStampEnabled){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%d_%d_%d", SharedPreferencesManager.getTerminalId(),
                bizType, payType, orderId));
        if (timeStampEnabled){
            sb.append("_").append(System.currentTimeMillis());
        }

        return sb.toString();
    }

}

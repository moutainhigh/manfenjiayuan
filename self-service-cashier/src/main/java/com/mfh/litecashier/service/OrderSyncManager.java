package com.mfh.litecashier.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;

import java.util.Date;
import java.util.List;

/**
 * POS-- 订单同步
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class OrderSyncManager {
    public static final int MAX_SYNC_ORDER_PAGESIZE = 2;

    /**
     * 生成订单同步数据结构
     */
    public JSONObject generateOrderJson(PosOrderEntity orderEntity) {
        JSONObject order = new JSONObject();

        order.put("id", orderEntity.getId());
        order.put("barCode", orderEntity.getBarCode());
        order.put("status", orderEntity.getStatus());
        order.put("humanId", orderEntity.getHumanId());//会员支付
        order.put("remark", orderEntity.getRemark());
        order.put("bcount", orderEntity.getBcount());
        order.put("adjPrice", orderEntity.getRetailAmount() - orderEntity.getFinalAmount()); //调价金额
        order.put("paystatus", orderEntity.getPaystatus());
        order.put("subType", orderEntity.getSubType());
        order.put("posId", orderEntity.getPosId());//设备编号
        order.put("sellOffice", orderEntity.getSellOffice());//curoffice id
        order.put("sellerId", orderEntity.getSellerId());//spid
//        if (orderEntity.getRetailAmount() == 0D) {
//            order.put("discount", Double.valueOf(String.valueOf(Integer.MAX_VALUE)));
//        } else {
//            order.put("discount", (orderEntity.getRetailAmount() - orderEntity.getDiscountAmount())
//                    / orderEntity.getRetailAmount());
//        }

        order.put("createdBy", orderEntity.getCreatedBy());

        //使用订单最后更新日期作为订单生效日期
        Date createdDate = orderEntity.getUpdatedDate();
        if (createdDate == null) {
            createdDate = new Date();
        }
        order.put("createdDate", TimeUtil.format(createdDate, TimeCursor.FORMAT_YYYYMMDDHHMMSS));

        //读取订单商品明细
        List<PosOrderItemEntity> orderItemEntities = CashierFactory.fetchOrderItems(orderEntity);
        JSONArray items = new JSONArray();
        for (PosOrderItemEntity entity : orderItemEntities) {
            JSONObject item = new JSONObject();
            item.put("goodsId", entity.getGoodsId());
            item.put("productId", entity.getProductId());
            item.put("skuId", entity.getProSkuId());
            item.put("barcode", entity.getBarcode());
            item.put("bcount", entity.getBcount());
            item.put("price", entity.getCostPrice());
            item.put("amount", entity.getBcount() * entity.getCostPrice());
            item.put("cateType", entity.getCateType());//按类目进行账务清分
            items.add(item);
        }
        order.put("items", items);

        //2016-07-01 上传订单支付记录到后台
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        if (payWrapper != null){
            order.put("payWays", payWrapper.getPayWays());
            order.put("disAmount", payWrapper.getRuleDiscount()); //优惠金额
            //卡券核销
            order.put("couponsIds", payWrapper.getCouponsIds());
            order.put("ruleIds", payWrapper.getRuleIds());
            order.put("payType", payWrapper.getPayType());
            Double amount = orderEntity.getFinalAmount() - payWrapper.getRuleDiscount();
            order.put("amount", amount);//负数表示退单
            if (amount >= 0.01){
                order.put("score", amount / 2);
            }
            else{
                order.put("score", 0D);
            }
        }
        else{
            order.put("amount", orderEntity.getFinalAmount());//实际支付金额
        }

        return order;
    }
}

package com.mfh.litecashier.utils;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartSplitOrder;
import com.mfh.litecashier.database.entity.PurchaseShopcartEntity;
import com.mfh.litecashier.database.logic.PurchaseShopcartService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 5/5/16.
 */
public class FruitShopcartHelper {
    private Map<Long, PurchaseShopcartSplitOrder> orderMaps;

    private static FruitShopcartHelper instance;

    public static FruitShopcartHelper getInstance() {
        if (instance == null) {
            synchronized (FruitShopcartHelper.class) {
                if (instance == null) {
                    instance = new FruitShopcartHelper();
                }
            }
        }
        return instance;
    }

    public Map<Long, PurchaseShopcartSplitOrder> getOrderMaps() {
        return orderMaps;
    }

    /**
     * */
    public void reloadAndInit(){
        if (orderMaps == null){
            orderMaps = new HashMap<>();
        }
        else {
            orderMaps.clear();
        }

        List<PurchaseShopcartEntity> entityList = PurchaseShopcartService.getInstance().getFreshGoodsList();
        if (entityList != null && entityList.size() > 0)
            for (PurchaseShopcartEntity entity : entityList) {
                PurchaseShopcartSplitOrder splitOrder = orderMaps.get(entity.getProviderId());
                if (splitOrder == null) {
                    splitOrder = new PurchaseShopcartSplitOrder();
                    splitOrder.setProviderId(entity.getProviderId());
                    splitOrder.setProviderName(entity.getProviderName());
                }

                List<PurchaseShopcartEntity> temp = splitOrder.getGoodsList();
                if (temp == null) {
                    temp = new ArrayList<>();
                }
                temp.add(entity);
                splitOrder.setGoodsList(temp);
                orderMaps.put(entity.getProviderId(), splitOrder);
            }
        else{
            ZLogger.d("暂无订单");
        }
    }

    public List<PurchaseShopcartSplitOrder> getSplitOrders(){
        List<PurchaseShopcartSplitOrder> orderList = new ArrayList<>();
        if (orderMaps != null && orderMaps.size() > 0) {
            for (Long key : orderMaps.keySet()) {
                orderList.add(orderMaps.get(key));
            }
        }

        return orderList;
    }
    public int getOrderTotalCount(){
        return orderMaps != null ? orderMaps.size() : 0;
    }
    public Double getTotalGoodsCount(){
        Double amount = 0D;
        if (orderMaps != null && orderMaps.size() > 0){
            for (Long key : orderMaps.keySet()) {
                PurchaseShopcartSplitOrder splitOrder = orderMaps.get(key);

                amount += splitOrder.getGoodsNum();
            }
        }

        return amount;
    }


    public Double getTotalOrderAmount(){
        Double amount = 0D;
        if (orderMaps != null && orderMaps.size() > 0){
            for (Long key : orderMaps.keySet()) {
                PurchaseShopcartSplitOrder splitOrder = orderMaps.get(key);

                amount += splitOrder.getOrderAmount();
            }
        }

        return amount;
    }
}

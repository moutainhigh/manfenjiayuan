package com.mfh.litecashier.utils;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购商品－－购物车
 * Created by bingshanguxue on 15/12/15.
 */
public class PurchaseShopcartHelper {
    private List<PurchaseShopcartOrder> orderList;
    private Double amount = 0D;//订单总金额
    private Double totalCount;//商品总数
    private int itemCount;//

    private static PurchaseShopcartHelper instance;

    public static PurchaseShopcartHelper getInstance() {
        if (instance == null) {
            synchronized (PurchaseShopcartHelper.class) {
                if (instance == null) {
                    instance = new PurchaseShopcartHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 清空商品采购购物车
     * */
    public void clear(){
        if (orderList != null){
            orderList.clear();
        }

        onDataSetChanged();
    }

    /**
     * 加入购物车(采购)
     * */
    public synchronized void addToShopcart(PurchaseShopcartGoodsWrapper goods){
        //检查商品是否有效
        if (goods == null){
            ZLogger.d("加入购物车失败，商品无效");
            return;
        }
        if (goods.getChainSkuId() == null){
            ZLogger.d("加入购物车失败，商品chainSkuId为空");
            return;
        }
        if (goods.getSupplyId() == null){
            ZLogger.d("加入购物车失败，商品无批发商信息，暂时无法加入购物车");
            return;
        }

        if (goods.getBuyPrice() == null){
            ZLogger.d("加入购物车失败，商品采购价为空");
            return;
        }

        try{
            PurchaseShopcartOrder order = query(goods);
            if (order != null){
                order.addGoods(goods);
            }
            else{
                order = new PurchaseShopcartOrder();
                order.setSupplyId(goods.getSupplyId());
                order.setSupplyName(goods.getSupplyName());
                order.setProviderContactName(MfhLoginService.get().getHumanName());
                order.setProvicerContactPhone(MfhLoginService.get().getTelephone());
                order.setIsPrivate(goods.getIsPrivate());
                order.addGoods(goods);

                if (orderList == null){
                    orderList = new ArrayList<>();
                }
                orderList.add(order);
            }

            onDataSetChanged();
            ZLogger.d(String.format("添加商品成功: %d/%d", getOrderCount(), getItemCount()));
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }
    }

    public synchronized void remove(PurchaseShopcartGoodsWrapper goods){
        if (goods == null){
            ZLogger.d("商品无效");
            return;
        }

        PurchaseShopcartOrder order = query(goods);
        if (order != null){
            order.removeGoods(goods);
//            orderList.set(order);

            //删除无效订单
            if (order.getOrderItemCount() <= 0){
                orderList.remove(order);
            }
            onDataSetChanged();
            ZLogger.d(String.format("删除商品成功: %d", orderList.size()));
        }
    }


    /**
     * 检查是否存在指定供应链或供应商的订单
     * */
    private PurchaseShopcartOrder query(PurchaseShopcartGoodsWrapper goods){
        if (orderList != null && orderList.size() > 0){
            for (PurchaseShopcartOrder order : orderList){
                if (ObjectsCompact.equals(order.getIsPrivate(), goods.getIsPrivate())
                        && ObjectsCompact.equals(order.getSupplyId(), goods.getSupplyId())){
                    ZLogger.d("订单已经存在");
                    return order;
                }
            }
        }

        return null;
    }

    /**
     * 订单数*/
    public int getOrderCount(){
        return orderList == null ? 0 : orderList.size();
    }

//    java.util.ConcurrentModificationException
    public boolean onDataSetChanged(){
        Double amount = 0D;
        Double totalCount = 0D;
        int count = 0;
        boolean isOrderListChanged = false;//订单数目改变
        List<PurchaseShopcartOrder> newOrderList = new ArrayList<>();
        if (orderList != null && orderList.size() > 0){
            for (PurchaseShopcartOrder order : orderList){
                if (order == null || order.getOrderItemCount() <= 0){
                    ZLogger.d("订单无效");
                    isOrderListChanged = true;
                    continue;
                }

                totalCount += order.getGoodsNum();
                count += order.getOrderItemCount();
                amount += order.getOrderAmount();
                newOrderList.add(order);
            }
        }
        this.orderList = newOrderList;
        this.totalCount = totalCount;
        this.itemCount = count;
        this.amount = amount;
        return isOrderListChanged;
    }

    public List<PurchaseShopcartOrder> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<PurchaseShopcartOrder> orderList) {
        this.orderList = orderList;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public Double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
    }
}

package com.mfh.enjoycity.bean;

import com.mfh.enjoycity.database.ShoppingCartEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺＋商品列表（购物车&订单）
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class ShopProductBean {

    private Long shopId;
    private List<ShoppingCartEntity> entityList;


    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public List<ShoppingCartEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<ShoppingCartEntity> entityList) {
        this.entityList = entityList;
    }

    public void addProductEntity(ShoppingCartEntity entity){
        if (entity == null){
            return;
        }

        if (this.entityList == null){
            this.entityList = new ArrayList<>();
        }
        this.entityList.add(entity);
    }

    /**
     * 店铺商品总价*/
    public double getTotalAmount(){
        double amount = 0.0;
        if (entityList != null && entityList.size() > 0){
            for (ShoppingCartEntity entity : entityList){
                amount += entity.getTotalAmount();
            }
        }
        return amount;
    }

    public int getTotalProductCount(){
        return (entityList == null ? 0 : entityList.size());
    }
}

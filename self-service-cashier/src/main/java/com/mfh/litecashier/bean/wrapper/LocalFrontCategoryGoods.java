package com.mfh.litecashier.bean.wrapper;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;

/**
 * Created by bingshanguxue on 8/16/16.
 */
public class LocalFrontCategoryGoods extends PosProductEntity{
    private int type = 0;//0商品；1动作

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static LocalFrontCategoryGoods create(PosProductEntity productEntity) {
        LocalFrontCategoryGoods goods = new LocalFrontCategoryGoods();
        //复制商品属性
        goods.setId(productEntity.getId());
        goods.setProSkuId(productEntity.getProSkuId());
        goods.setProductId(productEntity.getProductId());
        goods.setBarcode(productEntity.getBarcode());
        goods.setName(productEntity.getName());
        goods.setSkuName(productEntity.getSkuName());
        goods.setShortName(productEntity.getShortName());
        goods.setProviderId(productEntity.getProviderId());
        goods.setCostPrice(productEntity.getCostPrice());
        goods.setCustomerPrice(productEntity.getCustomerPrice());
        goods.setUnit(productEntity.getUnit());
        goods.setPriceType(productEntity.getPriceType());
        goods.setProdLineId(productEntity.getProdLineId());
        goods.setNeedWait(productEntity.getNeedWait());
        goods.setStatus(productEntity.getStatus());
        //自定义属性
        goods.setType(0);

        return goods;
    }
}

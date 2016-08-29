package com.mfh.litecashier.bean.wrapper;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.utils.ObjectsCompact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购商品订单
 * */
public class PurchaseShopcartOrder implements Serializable {
    private Long supplyId;//批发商ID
    private String supplyName;//批发商名称
    private String providerContactName;
    private String provicerContactPhone;
    private Integer isPrivate = IsPrivate.PLATFORM;

    private List<PurchaseShopcartGoodsWrapper> goodsList;//采购商品

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public String getSupplyName() {
        return supplyName;
    }

    public void setSupplyName(String supplyName) {
        this.supplyName = supplyName;
    }

    public List<PurchaseShopcartGoodsWrapper> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<PurchaseShopcartGoodsWrapper> goodsList) {
        this.goodsList = goodsList;
    }


    public String getProviderContactName() {
        if (provicerContactPhone == null){
            return "";
        }
        return providerContactName;
    }

    public void setProviderContactName(String providerContactName) {
        this.providerContactName = providerContactName;
    }

    public String getProvicerContactPhone() {
        if (provicerContactPhone == null){
            return "";
        }
        return provicerContactPhone;
    }

    public void setProvicerContactPhone(String provicerContactPhone) {
        this.provicerContactPhone = provicerContactPhone;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void addGoods(PurchaseShopcartGoodsWrapper goods) {
        if (goods == null || goods.getChainSkuId() == null){
            ZLogger.d("添加商品到购物车失败－－参数无效");
            return;
        }

        if (this.goodsList == null) {
            this.goodsList = new ArrayList<>();
        }

        PurchaseShopcartGoodsWrapper entity = query(goods.getChainSkuId());
        if (entity == null){
            ZLogger.d("添加商品到购物车");
            this.goodsList.add(goods);
        }
        else{
            ZLogger.d("商品已经在购物车中");
            //TODO,这里没有修改商品数量，需要手动在购物车中修改。
        }
    }

    public void removeGoods(PurchaseShopcartGoodsWrapper goods) {
        if (goods == null){
            return;
        }

        if (this.goodsList != null){
            this.goodsList.remove(goods);
        }
    }

    private PurchaseShopcartGoodsWrapper query(Long chainSkuId) {
        if (goodsList != null && goodsList.size() > 0) {
            for (PurchaseShopcartGoodsWrapper entity : goodsList) {
                Long existChainSkuId = entity.getChainSkuId();

                if (ObjectsCompact.equals(existChainSkuId, chainSkuId)){
                    return entity;
                }
            }
        }

        return null;
    }

    //TODO
    public int getOrderItemCount(){
        return goodsList == null ? 0 : getGoodsList().size();
    }

    public Double getGoodsNum(){
        Double num = 0D;
        if (goodsList != null && goodsList.size() > 0){
            for (PurchaseShopcartGoodsWrapper goods : goodsList){
                if (goods == null){
                    ZLogger.d("商品无效");
                    continue;
                }

                num += goods.getQuantityCheck();
            }
        }

        return num;
    }


    public Double getOrderAmount(){
        Double amount = 0D;
        if (goodsList != null && goodsList.size() > 0){
            for (PurchaseShopcartGoodsWrapper goods : goodsList){
                if (goods == null || goods.getBuyPrice() == null){
                    ZLogger.d("商品无效");
                    continue;
                }

                amount += goods.getBuyPrice() * goods.getQuantityCheck();
            }
        }

        return amount;
    }


    /**
     * 判断是否相等
     * */
    public static boolean isEqual(PurchaseShopcartOrder a, PurchaseShopcartOrder b) {
        return !(a == null || b == null) && (ObjectsCompact.equals(a.getIsPrivate(), b.getIsPrivate()) && ObjectsCompact.equals(a.getSupplyId(), b.getSupplyId()));

    }
}
package com.mfh.litecashier.bean.wrapper;

import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.litecashier.database.entity.PurchaseShopcartEntity;
import com.mfh.litecashier.database.logic.PurchaseShopcartService;

import java.io.Serializable;
import java.util.List;

/**
 * 采购商品订单拆分
 * */
public class PurchaseShopcartSplitOrder implements Serializable {
    private Long providerId;//批发商ID
    private String providerName;//批发商名称
    private String providerContact;
    private String providerPhone;
    private Integer isPrivate = IsPrivate.PLATFORM;
    private List<PurchaseShopcartEntity> goodsList;//订单明细
    private Double totalAmount = 0D;//总金额

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getProviderContact() {
        return providerContact;
    }

    public void setProviderContact(String providerContact) {
        this.providerContact = providerContact;
    }

    public String getProviderPhone() {
        return providerPhone;
    }

    public void setProviderPhone(String providerPhone) {
        this.providerPhone = providerPhone;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<PurchaseShopcartEntity> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<PurchaseShopcartEntity> goodsList) {
        this.goodsList = goodsList;
    }


    public void reloadAndInt(){
        goodsList = PurchaseShopcartService.getInstance().getFreshGoodsList(providerId);
    }

    //TODO
    public int getOrderItemCount(){
        return goodsList == null ? 0 : getGoodsList().size();
    }

    public Double getGoodsNum(){
        Double num = 0D;
        if (goodsList != null && goodsList.size() > 0){
            for (PurchaseShopcartEntity  goods : goodsList){
                if (goods == null){
                    continue;
                }

                num += goods.getQuantity();
            }
        }

        return num;
    }


    public Double getOrderAmount(){
        Double amount = 0D;
        if (goodsList != null && goodsList.size() > 0){
            for (PurchaseShopcartEntity goods : goodsList){
                if (goods == null){
                    continue;
                }

                amount += goods.getPrice() * goods.getQuantity();
            }
        }

        return amount;
    }


    /**
     * 判断是否相等
     * */
    public static boolean isEqual(PurchaseShopcartSplitOrder a, PurchaseShopcartSplitOrder b) {
        return !(a == null || b == null) &&
                (ObjectsCompact.equals(a.getIsPrivate(), b.getIsPrivate())
                        && ObjectsCompact.equals(a.getProviderId(), b.getProviderId()));

    }
}
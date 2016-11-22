package com.mfh.litecashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.invOrder.InvOrderApi;

/**
 *
 * <ol>
 *     采购订单购物车商品明细，对应{@link InvOrderApi#URL_INVSENDORDER_ASK_SENDORDER}
 *     <li>生鲜采购</li>
 * </ol>
 * Created by <bingshanguxue> on 16-06-02
 */
@Table(name="tb_purchase_order_v0100")
public class PurchaseOrderEntity extends MfhEntity<Long> implements ILongId{

    public static final Integer PURCHASE_TYPE_MANUAL= 0;//手动订货
    public static final Integer PURCHASE_TYPE_INTELLIGENT = 1;//智能订货
    /**
     * 采购类型，用来区别该商品属于哪类订单
     * */
    private Integer purchaseType = PURCHASE_TYPE_MANUAL;//采购类型

    /**
     * 批发商编号，用来支持按批发商拆分订单
     * */
    private Long providerId;//商品供应商编号
    private String providerName;

    /**{@link com.mfh.framework.api.constant.IsPrivate}*/
    private Integer isPrivate;

    private Double amount = 0D;   //订单总金额
    private Integer goodsNumber = 0;    //商品数量（这里指商品种类数量，不是单个商品的总数量和）

    public Integer getPurchaseType() {
        if (purchaseType == null){
            purchaseType = PURCHASE_TYPE_MANUAL;
        }
        return purchaseType;
    }

    public void setPurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
    }

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

    public Double getAmount() {
        if (amount == null){
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getGoodsNumber() {
        return goodsNumber;
    }

    public void setGoodsNumber(Integer goodsNumber) {
        this.goodsNumber = goodsNumber;
    }
}

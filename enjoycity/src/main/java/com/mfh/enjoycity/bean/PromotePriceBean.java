package com.mfh.enjoycity.bean;

/**
 * Created by Administrator on 2015/6/9.
 */
public class PromotePriceBean implements java.io.Serializable {
    private Long productId;
    private String discount;//0~10

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}

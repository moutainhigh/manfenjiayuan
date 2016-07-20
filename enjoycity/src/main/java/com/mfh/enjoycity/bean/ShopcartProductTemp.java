package com.mfh.enjoycity.bean;

/**
 * 购物车－－ 商品
 * Created by NAT.ZZN on 2015/5/14.
 *
 */
public class ShopcartProductTemp implements java.io.Serializable{
    private Long productId; //商品编号
    private String productName;//商品名
    private Integer productCount; //商品数目
    private double productPrice; //商品单价
    private String productImageUrl;//商品图片链接

    private Long shopId; //商铺编号

    public ShopcartProductTemp(){
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }


    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }


    public double getTotalAmount(){
        return productCount * productPrice;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
}

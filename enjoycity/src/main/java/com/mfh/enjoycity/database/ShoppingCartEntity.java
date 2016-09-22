package com.mfh.enjoycity.database;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.IStringId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * 购物车 商品(店铺ID+商品ID == ID)
 * Created by Administrator on 14-5-6.
 */
@Table(name="shopping_cart")
public class ShoppingCartEntity extends MfhEntity<String> implements IStringId {
    private Long productId; //商品编号
    private String productName;//商品名
    private Integer productCount; //商品数目
    private double productPrice; //商品单价
    private String productImageUrl;//商品图片链接
    private String description;//商品描述
//    private double discount;//商品折扣

    //所属商铺
    private Long shopId; //商铺编号


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


    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getShopId() {
        return shopId;
    }

    public double getTotalAmount(){
        return productCount * productPrice;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public double getDiscount() {
//        return discount;
//    }
//
//    public void setDiscount(double discount) {
//        this.discount = discount;
//    }
}

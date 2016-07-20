package com.mfh.enjoycity.bean;


/**
 * 商品
 * Created by Nat.ZZN on 2015/5/14.
 *
 */
public class HotSaleProductBean implements java.io.Serializable{

//    private Long id;//商品编号
    private Long productId;
    private String productName;//商品名称
//    private double price;//后台可能会有null数据，解析double异常
    private String price;
    private String imgUrl;//商品图片
    private Long shopId;

    //折扣信息
    private double discount;
    private Long proLabels;//1-爆款 2-进口 0 没有标签

//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }


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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }


    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Long getProLabels() {
        return proLabels;
    }

    public void setProLabels(Long proLabels) {
        this.proLabels = proLabels;
    }
}

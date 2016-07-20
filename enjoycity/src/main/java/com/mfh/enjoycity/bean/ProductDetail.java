package com.mfh.enjoycity.bean;

import java.util.List;

/**
 * 商品详情
 * Created by Nat.ZZN on 2015/5/14.
 *
 */
public class ProductDetail implements java.io.Serializable{
    private String oldPrice;//
//    private double costPrice;//原价，历史价格
    private String costPrice;
    private List<ProductAtt> attList;
    private Product product;//商品信息
    private String thumbnail;//商品图片


    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<ProductAtt> getAttList() {
        return attList;
    }

    public void setAttList(List<ProductAtt> attList) {
        this.attList = attList;
    }
//
//    public double getCostPrice() {
//        return costPrice;
//    }
//
//    public void setCostPrice(double costPrice) {
//        this.costPrice = costPrice;
//    }


    public String getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(String costPrice) {
        this.costPrice = costPrice;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (product != null){
            sb.append(String.format("\nid:%d", product.getId()));
            sb.append(String.format("\nname:%s", product.getName()));
        }
        sb.append(String.format("\ncostPrice:%s", costPrice));
        sb.append(String.format("\noldPrice:%s", oldPrice));
        if (attList != null && attList.size() > 0){
            for (ProductAtt att : attList){
                sb.append(String.format("\npathUrl:%s", att.getPathUrl()));
            }
        }
        return sb.toString();
    }
}

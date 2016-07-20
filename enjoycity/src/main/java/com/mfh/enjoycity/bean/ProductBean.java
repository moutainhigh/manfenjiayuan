package com.mfh.enjoycity.bean;

import java.util.List;

/**
 * 商品
 * Created by Nat.ZZN(bingshanguxue) on 2015/5/14.
 *
 */
public class ProductBean implements java.io.Serializable{

    private Long id;//商品编号
    private String name;//商品名称
    private String oldPrice;//
    private double costPrice;//原价，历史价格
    private List<ProductAtt> attList;
    private String thumbnail;//商品图片
    private String description;//商品描述

    //折扣信息
    private double discount;
    private Long label;//1-爆款 2-进口 0 没有标签

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductAtt> getAttList() {
        return attList;
    }

    public void setAttList(List<ProductAtt> attList) {
        this.attList = attList;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Long getLabel() {
        return label;
    }

    public void setLabel(Long label) {
        this.label = label;
    }
}

package com.mfh.litecashier.bean;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 5/27/16.
 */
public class GrouponDetail implements Serializable{
    private String pageUrl;
    private String name;
    private Double price;
    private String origin;//产地
    private String weight;//重量
    private String specifications;//规格
    private String packageMethod;//包装

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getPackageMethod() {
        return packageMethod;
    }

    public void setPackageMethod(String packageMethod) {
        this.packageMethod = packageMethod;
    }
}

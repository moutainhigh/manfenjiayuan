package com.mfh.enjoycity.bean;

import java.util.List;

/**
 * Created by Administrator on 2015/8/5.
 */
public class CategoryProductBean {
    private String categoryName;
    private List<ProductDetail> productBeans;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<ProductDetail> getProductBeans() {
        return productBeans;
    }

    public void setProductBeans(List<ProductDetail> productBeans) {
        this.productBeans = productBeans;
    }
}

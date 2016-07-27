package com.mfh.litecashier.bean.wrapper;

import com.mfh.framework.api.companyInfo.CompanyInfo;

import java.io.Serializable;

/**
 * 订货搜索条件
 */
public class SearchParamsWrapper implements Serializable {
    //排序
    public static final int SORT_BY_NONE = -1;
    public static final int SORT_BY_STOCK_QUANTITY_DESC = 0;
    public static final int SORT_BY_STOCK_QUANTITY_ASC = 1;
    public static final int SORT_BY_MONTHLY_SALES_DESC = 2;
    public static final int SORT_BY_MONTHLY_SALES_ASC = 3;
    public static final int SORT_BY_DAILY_SALES_DESC = 4;
    public static final int SORT_BY_DAILY_SALES_ASC = 5;

    //价格类型
    public static final String PRICE_TYPE_NAME_NA = "全部商品";
    public static final String PRICE_TYPE_NAME_NUMBER = "计件";
    public static final String PRICE_TYPE_NAME_WEIGHT = "计重";

    private String barcode = "";//商品条码
    private String productName = "";//商品名称
    private String brand = "";//品牌
    private String categoryId = "";//类目
    private String categoryName = "";
    //价格类型0-计件 1-计重
    private String priceType = PRICE_TYPE_NAME_NA;//价格类型

    //批发商信息
    private CompanyInfo mCompanyInfo = null;

    private int sortType = SORT_BY_NONE;

    public String getBarcode() {
        if (barcode == null) {
            return "";
        }
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductName() {
        if (productName == null) {
            return "";
        }
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }


    public CompanyInfo getCompanyInfo() {
        return mCompanyInfo;
    }

    public void setCompanyInfo(CompanyInfo companyInfo) {
        mCompanyInfo = companyInfo;
    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

//        /**
//         * 切换供应商
//         */
//        public void changeProvider(int providerType, Long providerId) {
//            this.providerType = providerType;
//            this.providerId = providerId;
//        }
//
//        /**
//         * 切换类目
//         */
//        public void changeCategory(String categoryId, String categoryName) {
//            this.categoryId = categoryId;
//            this.categoryName = categoryName;
//        }
}
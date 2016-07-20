package com.mfh.enjoycity.adapter;

import com.mfh.enjoycity.bean.BannerBean;
import com.mfh.enjoycity.bean.CategoryMenuBean;
import com.mfh.enjoycity.bean.CategoryProductBean;
import com.mfh.enjoycity.bean.ProductDetail;

import java.util.List;

/**
 * Created by Administrator on 2015/8/5.
 */
public class HomeAdapterData {
    private Long shopId;
    private String shopName;

    private List<BannerBean> bannerBeans;
    private List<ProductDetail> discountProductBeans;
    private List<CategoryMenuBean> categoryMenus;
    private List<CategoryProductBean> categoryProductBeans;


    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public List<BannerBean> getBannerBeans() {
        return bannerBeans;
    }

    public void setBannerBeans(List<BannerBean> bannerBeans) {
        this.bannerBeans = bannerBeans;
    }

    public List<ProductDetail> getDiscountProductBeans() {
        return discountProductBeans;
    }

    public void setDiscountProductBeans(List<ProductDetail> discountProductBeans) {
        this.discountProductBeans = discountProductBeans;
    }

    public List<CategoryMenuBean> getCategoryMenus() {
        return categoryMenus;
    }

    public void setCategoryMenus(List<CategoryMenuBean> categoryMenus) {
        this.categoryMenus = categoryMenus;
    }

    public List<CategoryProductBean> getCategoryProductBeans() {
        return categoryProductBeans;
    }

    public void setCategoryProductBeans(List<CategoryProductBean> categoryProductBeans) {
        this.categoryProductBeans = categoryProductBeans;
    }
}

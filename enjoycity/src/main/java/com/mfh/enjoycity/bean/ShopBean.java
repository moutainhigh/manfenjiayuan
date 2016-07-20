package com.mfh.enjoycity.bean;

/**
 * 店铺
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/14.
 *
 */
public class ShopBean implements java.io.Serializable{
    private Long id;//店铺编号
    private String shopName;//店铺名
    private String shopLogoUrl;//店铺LOGO链接
    private Long tenantId;//租户编号


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopLogoUrl() {
        return shopLogoUrl;
    }

    public void setShopLogoUrl(String shopLogoUrl) {
        this.shopLogoUrl = shopLogoUrl;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}

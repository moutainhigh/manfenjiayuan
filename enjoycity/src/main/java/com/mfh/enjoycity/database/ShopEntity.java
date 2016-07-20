package com.mfh.enjoycity.database;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * 店铺
 * Created by Administrator on 14-5-6.
 */
@Table(name="shop_v1")
public class ShopEntity extends MfhEntity<Long> implements ILongId {

    //所属商铺
//    private Long id; //商铺编号
    private String shopName; //商铺名称
    private String shopLogoUrl; //商铺图标链接
    private Long tenantId;//租户编号

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopName() {
        return shopName;
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

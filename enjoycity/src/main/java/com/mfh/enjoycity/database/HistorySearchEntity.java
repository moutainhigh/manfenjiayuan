package com.mfh.enjoycity.database;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.IStringId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * 注册用户·地址表
 * Created by Nat.ZZN on 15-8-6..
 */
@Table(name="history_searh")
public class HistorySearchEntity extends MfhEntity<String> implements IStringId{
    private String shopId; //商铺编号
    private String shopName; //商铺名称
    private String queryContent;

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getQueryContent() {
        return queryContent;
    }

    public void setQueryContent(String queryContent) {
        this.queryContent = queryContent;
    }
}

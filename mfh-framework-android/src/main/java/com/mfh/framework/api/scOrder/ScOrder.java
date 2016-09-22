package com.mfh.framework.api.scOrder;

import com.mfh.framework.api.abs.AbsOnlineOrder;

import java.util.List;

/**
 * 商城订单
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrder extends AbsOnlineOrder {

    /**
     * 客户
     * */
    private String buyerName;//客户(也就是买家)昵称,客户不一定是收件人

    /**
     * 小伙伴(买手)*/
    private String serviceHumanName;//小伙伴(买手)名称
    private String serviceMobile;//小伙伴手机

    private String companyName;//供应商名称
    private String sellerName;// 销售方租户名称
    private String officeName;// 下单网点
    private List<ScOrderItem> items;//订单明细

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getServiceHumanName() {
        return serviceHumanName;
    }

    public void setServiceHumanName(String serviceHumanName) {
        this.serviceHumanName = serviceHumanName;
    }

    public String getServiceMobile() {
        return serviceMobile;
    }

    public void setServiceMobile(String serviceMobile) {
        this.serviceMobile = serviceMobile;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public List<ScOrderItem> getItems() {
        return items;
    }

    public void setItems(List<ScOrderItem> items) {
        this.items = items;
    }
}

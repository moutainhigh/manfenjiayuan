package com.mfh.framework.api.scOrder;

import com.mfh.framework.api.abs.AbsOnlineOrder;

import java.util.List;

/**
 * 商城订单
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrder extends AbsOnlineOrder {

    public static final Integer MFHORDER_STATUS_ORDERED = 0;//已创建（买手可以抢单拣货，组货）
    public static final Integer MFHORDER_STATUS_PREPARE = 3;//已发货（骑手可以揽件）
    public static final Integer MFHORDER_STATUS_SENDED = 6;//配送中（骑手可以妥投）
    public static final Integer MFHORDER_STATUS_INSTOCK = 9;//已到达
    public static final Integer MFHORDER_STATUS_OUTSTOCK = 12;//已签收

    /**
     * 客户
     * */
    private String buyerName;//客户(也就是买家)昵称,客户不一定是收件人

    /**
     * 小伙伴(买手)*/
    private Long guideHumanId;//买手人员编号
    private String serviceHumanName;//小伙伴(买手)名称
    private String serviceMobile;//小伙伴手机
    private String serviceHumanImg;//买手头像

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

    public Long getGuideHumanId() {
        return guideHumanId;
    }

    public void setGuideHumanId(Long guideHumanId) {
        this.guideHumanId = guideHumanId;
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

    public String getServiceHumanImg() {
        return serviceHumanImg;
    }

    public void setServiceHumanImg(String serviceHumanImg) {
        this.serviceHumanImg = serviceHumanImg;
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

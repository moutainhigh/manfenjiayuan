package com.bingshanguxue.cashier.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 卡券规则
 * Created by Nat.ZZN(bingshanguxue) on 15/9/30.
 */
public class RuleBean implements Serializable {
//    "ruleBeans": [
//    {
//        "planTypeCaption": "满额优惠",
//            "execTypeCaption": "减金额",
//            "otherIdCaption": null,
//            "tenantName": "满分家园",
//            "title": "西湖牌优惠",
//            "planType": 2,
//            "packageId": null,
//            "execType": 5,
//            "execNum": 3,
//            "execNumType": 0,
//            "otherId": null,
//            "status": 1,
//            "finishDate": "2015-12-09 17:54:38",
//            "tenantId": 130222,
//            "id": 116,
//            "createdBy": "",
//            "createdDate": "2015-12-09 18:38:44",
//            "updatedBy": "131291",
//            "updatedDate": "2015-12-09 18:38:44"
//    }
//    ]

    private Long id;
    private String title; //计划名称
    private Integer planType; //促销类型，间接决定exec_type
    private Integer packageId; //所属套餐
    /**执行类型 4-折扣 5-减金额 6-返金额 7-赠送购买商品 8-赠送其他商品 9-送卡券 10-多倍积分*/
    private Integer execType;
    private Double execNum; //执行数量
    private Integer execNumType; //数量类型 0-绝对值 1-百分比如10则代表0.1
    private String otherId; //相关信息，如赠送商品编号、卡券编号等,多个可逗号分隔
    private Integer status; //状态 0-无效 1-有效
    private Date finishDate;//截止日期，冗余，其实维度表中也有定义
    private Long tenantId; //促销计划所属租户
    private String planTypeCaption;//促销计划类型名称
    private String execTypeCaption;//促销执行类型名称
    private String otherIdCaption;//执行中涉及到赠送的商品、卡券名称等
    private String tenantName;//租户名字

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPlanType() {
        return planType;
    }

    public void setPlanType(Integer planType) {
        this.planType = planType;
    }

    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public Integer getExecType() {
        return execType;
    }

    public void setExecType(Integer execType) {
        this.execType = execType;
    }

    public Double getExecNum() {
        return execNum;
    }

    public void setExecNum(Double execNum) {
        this.execNum = execNum;
    }

    public Integer getExecNumType() {
        return execNumType;
    }

    public void setExecNumType(Integer execNumType) {
        this.execNumType = execNumType;
    }

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getPlanTypeCaption() {
        return planTypeCaption;
    }

    public void setPlanTypeCaption(String planTypeCaption) {
        this.planTypeCaption = planTypeCaption;
    }

    public String getExecTypeCaption() {
        return execTypeCaption;
    }

    public void setExecTypeCaption(String execTypeCaption) {
        this.execTypeCaption = execTypeCaption;
    }

    public String getOtherIdCaption() {
        return otherIdCaption;
    }

    public void setOtherIdCaption(String otherIdCaption) {
        this.otherIdCaption = otherIdCaption;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}

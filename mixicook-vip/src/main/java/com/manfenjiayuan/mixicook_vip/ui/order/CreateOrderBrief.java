package com.manfenjiayuan.mixicook_vip.ui.order;

import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.api.shoppingCart.CartPack;

import java.io.Serializable;
import java.util.List;

/**
 * 下单
 * Created by bingshanguxue on 10/10/2016.
 */

public class CreateOrderBrief implements Serializable {
    /**以下信息来自购物车*/
    private Integer bizType = BizType.SC;
    private Reciaddr reciaddr;
    private CompanyInfo mCompanyInfo;
    private List<CartPack> packs;//购物车明细
    private Double transFee = 0D;//下单前在购物车中会根据运费规则计算
    private Long humanId;
    /**以下信息来自下单*/
    private String dueDate;
    private String dueDateEnd;
    private String dueDateSpan;
    private String remark;

    public Reciaddr getReciaddr() {
        return reciaddr;
    }

    public void setReciaddr(Reciaddr reciaddr) {
        this.reciaddr = reciaddr;
    }

    public List<CartPack> getPacks() {
        return packs;
    }

    public void setPacks(List<CartPack> packs) {
        this.packs = packs;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueDateEnd() {
        return dueDateEnd;
    }

    public void setDueDateEnd(String dueDateEnd) {
        this.dueDateEnd = dueDateEnd;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public String getDueDateSpan() {
        return dueDateSpan;
    }

    public void setDueDateSpan(String dueDateSpan) {
        this.dueDateSpan = dueDateSpan;
    }

    public CompanyInfo getCompanyInfo() {
        return mCompanyInfo;
    }

    public void setCompanyInfo(CompanyInfo companyInfo) {
        mCompanyInfo = companyInfo;
    }

    public Double getTransFee() {
        return transFee;
    }

    public void setTransFee(Double transFee) {
        this.transFee = transFee;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Long getHumanId() {
        return humanId;
    }

    public void setHumanId(Long humanId) {
        this.humanId = humanId;
    }
}

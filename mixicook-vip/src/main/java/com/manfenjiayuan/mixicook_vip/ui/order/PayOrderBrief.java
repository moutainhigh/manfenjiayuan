package com.manfenjiayuan.mixicook_vip.ui.order;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 10/10/2016.
 */

public class PayOrderBrief implements Serializable {
    private String orderIds;//订单编号
    private Double amount;//订单金额
    private Integer bizType;//业务类型

    public String getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(String orderIds) {
        this.orderIds = orderIds;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }
}

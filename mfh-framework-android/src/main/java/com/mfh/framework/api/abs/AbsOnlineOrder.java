package com.mfh.framework.api.abs;

import java.util.Date;

/**
 * 线上订单
 * Created by bingshanguxue on 9/22/16.
 */

public class AbsOnlineOrder extends AbsOrder{
    /**
     * 这部分是线上订单特有的
     * */
    private String address; //完整的收件地址(冗余)，也可能是驿站的地址（驿站地址的话要再加上电话号码）
    private String receiveName;// 最终收件人名称
    private String receivePhone;// 最终收件人手机
    private Double transFee;//订单总金额中的其中物流费用
    private Date dueDate;//送达时间-开始
    private Date dueDateEnd;//送达时间-结束

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public String getReceivePhone() {
        return receivePhone;
    }

    public void setReceivePhone(String receivePhone) {
        this.receivePhone = receivePhone;
    }

    public Double getTransFee() {
        if (transFee == null){
            return 0D;
        }
        return transFee;
    }

    public void setTransFee(Double transFee) {
        this.transFee = transFee;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDueDateEnd() {
        return dueDateEnd;
    }

    public void setDueDateEnd(Date dueDateEnd) {
        this.dueDateEnd = dueDateEnd;
    }
}

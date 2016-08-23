package com.bingshanguxue.cashier.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class PayOrder implements Serializable {
    private Date updatedDate;
    private Double totalFee = 0D;//单位是分

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Double getTotalFee() {
        if (totalFee == null){
            return 0D;
        }
        return totalFee;
    }

    public void setTotalFee(Double totalFee) {
        this.totalFee = totalFee;
    }
}

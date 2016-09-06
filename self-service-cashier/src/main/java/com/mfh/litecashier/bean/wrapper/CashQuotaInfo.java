package com.mfh.litecashier.bean.wrapper;

import com.bingshanguxue.cashier.model.OrderPayWay;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class CashQuotaInfo implements Serializable {
    private Double limit = 0D;
    private Double unpaid = 0D;
    private List<OrderPayWay> mOrderPayWays;
    private List<OrderPayWay> mPosOrders;


    public Double getLimit() {
        if (limit == null){
            return 0D;
        }
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }

    public Double getUnpaid() {
        if (unpaid == null){
            return 0D;
        }
        return unpaid;
    }

    public void setUnpaid(Double unpaid) {
        this.unpaid = unpaid;
    }

    public List<OrderPayWay> getOrderPayWays() {
        return mOrderPayWays;
    }

    public void setOrderPayWays(List<OrderPayWay> orderPayWays) {
        mOrderPayWays = orderPayWays;
    }

    public List<OrderPayWay> getPosOrders() {
        return mPosOrders;
    }

    public void setPosOrders(List<OrderPayWay> posOrders) {
        mPosOrders = posOrders;
    }
}

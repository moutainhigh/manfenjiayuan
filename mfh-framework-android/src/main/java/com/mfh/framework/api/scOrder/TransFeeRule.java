package com.mfh.framework.api.scOrder;

import java.io.Serializable;

/**
 * 网点的配送规则
 * Created by bingshanguxue on 12/10/2016.
 */

public class TransFeeRule implements Serializable{
    private Double orderTransFee = 0D;//运费
    private Double orderNoTransFeeLimit = 0D;//满**免运费
    private Double orderMinLimit = 0D;//满**起送

    public Double getOrderTransFee() {
        if (orderTransFee == null){
            return 0D;
        }
        return orderTransFee;
    }

    public void setOrderTransFee(Double orderTransFee) {
        this.orderTransFee = orderTransFee;
    }

    public Double getOrderNoTransFeeLimit() {
        if (orderNoTransFeeLimit == null){
            return 0D;
        }
        return orderNoTransFeeLimit;
    }

    public void setOrderNoTransFeeLimit(Double orderNoTransFeeLimit) {
        this.orderNoTransFeeLimit = orderNoTransFeeLimit;
    }

    public Double getOrderMinLimit() {
        if (orderMinLimit == null){
            return 0D;
        }
        return orderMinLimit;
    }

    public void setOrderMinLimit(Double orderMinLimit) {
        this.orderMinLimit = orderMinLimit;
    }
}

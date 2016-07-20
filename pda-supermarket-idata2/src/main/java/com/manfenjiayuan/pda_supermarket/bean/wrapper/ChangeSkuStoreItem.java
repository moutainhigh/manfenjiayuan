package com.manfenjiayuan.pda_supermarket.bean.wrapper;

import java.io.Serializable;

/**
 * 库存转换明细
 * Created by bingshanguxue on 5/19/16.
 */
public class ChangeSkuStoreItem implements Serializable{
    private Long id;
    private Double quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

}

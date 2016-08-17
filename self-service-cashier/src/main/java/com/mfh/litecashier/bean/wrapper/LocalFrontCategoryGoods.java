package com.mfh.litecashier.bean.wrapper;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;

/**
 * Created by bingshanguxue on 8/16/16.
 */
public class LocalFrontCategoryGoods extends PosProductEntity{
    private int type = 0;//0商品；1动作

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

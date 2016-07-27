package com.mfh.litecashier.event;

import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;

/**
 * 事务
 * Created by bingshanguxue on 15/9/23.
 */
public class AddCommonlyGoodsEvent {

    private CommonlyGoodsEntity goods;

    public AddCommonlyGoodsEvent(CommonlyGoodsEntity goods) {
        this.goods = goods;
    }

    public CommonlyGoodsEntity getGoods() {
        return goods;
    }
}

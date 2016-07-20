package com.mfh.litecashier.event;

import com.manfenjiayuan.business.bean.ChainGoodsSku;

/**
 * 事务
 * Created by kun on 15/9/23.
 */
public class AddLaunchGoodsEvent {

    private ChainGoodsSku goods;

    public AddLaunchGoodsEvent(ChainGoodsSku goods) {
        this.goods = goods;
    }

    public ChainGoodsSku getGoods() {
        return goods;
    }
}

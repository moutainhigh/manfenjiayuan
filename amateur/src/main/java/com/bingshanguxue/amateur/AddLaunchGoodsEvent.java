package com.mfh.litecashier.event;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;

/**
 * 事务
 * Created by bingshanguxue on 15/9/23.
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

package com.mfh.litecashier.event;

import com.manfenjiayuan.business.bean.ScGoodsSku;

/**
 * 事务
 * Created by kun on 15/9/23.
 */
public class AddCategoryGoodsEvent {

    private ScGoodsSku goods;

    public AddCategoryGoodsEvent(ScGoodsSku goods) {
        this.goods = goods;
    }

    public ScGoodsSku getGoods() {
        return goods;
    }
}

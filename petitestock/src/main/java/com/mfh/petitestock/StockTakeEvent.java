package com.mfh.petitestock;

import com.mfh.petitestock.bean.StockGoods;

/**
 * 事务
 * Created by kun on 15/9/23.
 */
public class StockTakeEvent {

    private StockGoods stockGoods;

    public StockTakeEvent(StockGoods stockGoods) {
        this.stockGoods = stockGoods;
    }

    public StockGoods getStockGoods() {
        return stockGoods;
    }
}

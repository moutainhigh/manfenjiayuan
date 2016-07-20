package com.mfh.litecashier.mode;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;

/**
 * 库存商品：库存成本，批次流水，库存调拨
 * Created by bingshanguxue on 16/3/17.
 */
public interface ICashierMode {

    /**
     * 查询商品,如果有多个，返回第一个商品
     * @param barcode 商品条码
     * */
    PosProductEntity findGoods(String barcode);

    /**
     * 查询商品主条码
     * */
    String findMainBarcode(String otherBarcode);
}

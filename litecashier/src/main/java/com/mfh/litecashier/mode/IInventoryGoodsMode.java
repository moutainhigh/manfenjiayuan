package com.mfh.litecashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 库存商品：库存成本，批次流水，库存调拨
 * Created by bingshanguxue on 16/3/17.
 */
public interface IInventoryGoodsMode<D> {

    /**
     * 获取库存商品
     * @param categoryId 类目编号
     * */
    void loadInventoryGoods(PageInfo pageInfo, String categoryId, String barcode,
                            String name, int sortType, String priceType,
                            OnPageModeListener<D> listener);


    /**
     * 获取采购商品
     * @param categoryId 类目编号
     * */
    void loadPurchaseGoods(PageInfo pageInfo, String categoryId, Long otherTenantId,
                           String barcode, String nameLike, int sortType, String priceType,
                           OnPageModeListener<D> listener);


    /**
     * 根据条码查找租户是否已经发布过该商品，若存在返回信息
     * */
    void checkWithBuyInfoByBarcode(String barcode, OnModeListener<D> listener);
}

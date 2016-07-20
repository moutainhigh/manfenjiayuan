package com.mfh.litecashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 库存调拨订单
 * Created by bingshanguxue on 16/3/17.
 */
public interface IInvTransOrderMode<D> {

    /**
     * 加载采购收货订单列表
     * @param frontCategoryId 类目编号
     * */
    void loadOrders(PageInfo pageInfo, boolean netFlag, OnPageModeListener<D> listener);

}

package com.manfenjiayuan.business.mode;

import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 采购订单明细
 * Created by bingshanguxue on 16/3/17.
 */
public interface IInvSendOrderItemMode<D> {

    /**
     * 加载采购订单明细
     * @param id 订单编号
     * */
    void loadOrderItems(Long id, OnPageModeListener<D> listener);

}

package com.manfenjiayuan.pda_supermarket.mode;

import com.mfh.framework.mvp.OnModeListener;

/**
 * 采购订单明细
 * Created by bingshanguxue on 16/3/17.
 */
public interface IInvSendIoOrderItemMode<D> {

    /**
     * 加载采购订单明细
     * @param id 订单编号
     *
     * */

    void loadOrderItemsByBarcode(String barcode, OnModeListener<D> listener);

}

package com.mfh.litecashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 采购退货订单
 * Created by bingshanguxue on 16/3/17.
 */
public interface IntelligentOrderMode<D> {

    /**
     * 加载采购收货订单列表
     * @param frontCategoryId 类目编号
     * */
    void loadOrders(PageInfo pageInfo, String status, String payStatus, String sendTenantId, OnPageModeListener<D> listener);

}

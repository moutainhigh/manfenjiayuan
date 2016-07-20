package com.manfenjiayuan.business.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public interface IInvSendOrderMode<D> {

    /**
     * 加载订单列表
     *
     * @param frontCategoryId 类目编号
     */
    void loadOrders(PageInfo pageInfo, boolean netFlag, Long netId, String sendTenantId,
                    String status, OnPageModeListener<D> listener);

}

package com.manfenjiayuan.business.hostserver;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.tenant.TenantInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface ITenantView extends MvpView {
    void onITenantViewProcess();
    void onITenantViewError(String errorMsg);
    void onITenantViewSuccess(PageInfo pageInfo, List<TenantInfo> data);
}

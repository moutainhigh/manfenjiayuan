package com.manfenjiayuan.business.mvp.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.invSkuStore.InvSkuBizBean;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvSkuBizView extends MvpView {
    void onIInvSkuBizViewProcess();
    void onIInvSkuBizViewError(String errorMsg);
    void onIInvSkuBizViewSuccess(InvSkuBizBean data);
    void onIInvSkuBizViewSuccess(PageInfo pageInfo, List<InvSkuBizBean> dataList);

}

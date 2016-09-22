package com.manfenjiayuan.loveshopping.mvp;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.account.Subdis;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface ISubdisListView extends MvpView {
    void onQuerySubdisProcess();
    void onQuerySubdisError(String errorMsg);
    void onQuerySubdisSuccess(PageInfo pageInfo, List<Subdis> dataList);
}

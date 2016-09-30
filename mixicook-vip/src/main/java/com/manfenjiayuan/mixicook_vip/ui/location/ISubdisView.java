package com.manfenjiayuan.mixicook_vip.ui.location;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.account.Subdis;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface ISubdisView extends MvpView {
    void onISubdisViewProcess();

    void onISubdisViewError(String errorMsg);

    void onISubdisViewSuccess(PageInfo pageInfo, List<Subdis> dataList);

}

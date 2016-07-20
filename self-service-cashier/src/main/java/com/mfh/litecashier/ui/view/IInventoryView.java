package com.mfh.litecashier.ui.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;
import com.manfenjiayuan.business.bean.ScGoodsSku;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface IInventoryView extends MvpView {
    void onProcess();
    void onError(String errorMsg);
    void onData(ScGoodsSku data);
    void onList(PageInfo pageInfo, List<ScGoodsSku> dataList);
}

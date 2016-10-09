package com.manfenjiayuan.mixicook_vip.ui.address;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 购物车
 * Created by bingshanguxue on 16/3/17.
 */
public interface IReciaddrView extends MvpView {
    void onIReciaddrViewProcess();

    void onIReciaddrViewError(String errorMsg);

    void onIReciaddrViewSuccess(PageInfo pageInfo, List<Reciaddr> dataList);

    void onIReciaddrViewSuccess(Reciaddr data);
}

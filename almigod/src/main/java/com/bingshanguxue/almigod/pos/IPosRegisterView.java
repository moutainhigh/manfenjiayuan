package com.bingshanguxue.almigod.pos;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.posRegister.PosRegister;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

public interface IPosRegisterView extends MvpView {
    void onIPosRegisterViewProcess();
    void onIPosRegisterViewError(String errorMsg);
    void onIPosRegisterViewSuccess(PageInfo pageInfo, List<PosRegister> dataList);
}
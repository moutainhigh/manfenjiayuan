package com.manfenjiayuan.business.view;

import com.mfh.framework.mvp.MvpView;

/**
 * Created by bingshanguxue on 16/3/21.
 */
public interface IPosRegisterView extends MvpView {
    void onRegisterPlatProcess();
    void onRegisterPlatError(String errorMsg);
    void onRegisterPlatSuccess(String terminalId);
    void onPlatUpdate();
}

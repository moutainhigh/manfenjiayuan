package com.manfenjiayuan.business.mvp.view;

import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.mvp.MvpView;

/**
 * 充值
 * Created by bingshanguxue on 16/3/21.
 */
public interface ICustomerTopupView extends MvpView {
    void onICustomerTopupViewLoading();
    void onICustomerTopupViewError(int type, String content, String errorMsg);
    void onICustomerTopupViewSuccess(int type, String content, UserAccount human);
}

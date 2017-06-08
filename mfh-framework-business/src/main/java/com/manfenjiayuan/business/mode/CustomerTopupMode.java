package com.manfenjiayuan.business.mode;

import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;

import java.util.Map;

import rx.Subscriber;

/**
 * 供应商商品
 * Created by bingshanguxue on 16/3/17.
 */
public class CustomerTopupMode {

    /**
     * 加载会员信息
     */
    public void transferFromMyAccount(Map<String, String> options, final OnModeListener<UserAccount> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        CommonUserAccountHttpManager.getInstance().transferFromMyAccount(options,
                new Subscriber<UserAccount>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(UserAccount userAccount) {
                        if (userAccount == null) {
                            if (listener != null) {
                                listener.onError("充值失败：");
                            }
                        } else {
                            if (listener != null) {
                                listener.onSuccess(userAccount);
                            }
                        }
                    }


                });
    }

}

package com.manfenjiayuan.business.mvp.mode;

import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.httpmgr.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.subscriber.MSubscriber;

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
                new MSubscriber<UserAccount>() {
//                    @Override
//                    public void onError(Throwable e) {
//                        if (listener != null) {
//                            listener.onError(e.getMessage());
//                        }
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
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

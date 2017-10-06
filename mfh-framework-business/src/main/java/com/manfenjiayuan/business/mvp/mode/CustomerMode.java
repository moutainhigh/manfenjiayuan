package com.manfenjiayuan.business.mvp.mode;

import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MSubscriber;

import java.util.Map;

import rx.Subscriber;

/**
 * 会员
 * Created by bingshanguxue on 16/3/17.
 */
public class CustomerMode {

    /**
     * 加载会员信息
     */
    public void getCustomerByOther(Map<String, String> options, final OnModeListener<Human> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        RxHttpManager.getInstance().getCustomerByOther(options,
                new MSubscriber<Human>() {
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
                    public void onNext(Human human) {
                        if (listener != null) {
                            listener.onSuccess(human);
                        }
                    }
                });
    }

}

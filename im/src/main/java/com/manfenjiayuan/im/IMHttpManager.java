package com.manfenjiayuan.im;

import com.manfenjiayuan.im.bean.BizMsgParamWithSession;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class IMHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final IMHttpManager INSTANCE = new IMHttpManager();
    }

    //获取单例
    public static IMHttpManager getInstance() {
        return IMHttpManager.SingletonHolder.INSTANCE;
    }

    private interface IMService{
        /**注册消息桥*/
        @GET("msgcore/mobile/api")
        Observable<MResponse<BizMsgParamWithSession>> registerMessageBridge(@QueryMap Map<String, String> options);

    }

    public void registerMessageBridge(String baseUrl, Map<String, String> options,
                                      Subscriber<BizMsgParamWithSession> subscriber) {
        IMService mfhApi = RxHttpManager.createService(IMService.class, baseUrl);
        Observable observable = mfhApi.registerMessageBridge(options)
                .map(new MResponseFunc<BizMsgParamWithSession>());
        toSubscribe(observable, subscriber);
    }




}

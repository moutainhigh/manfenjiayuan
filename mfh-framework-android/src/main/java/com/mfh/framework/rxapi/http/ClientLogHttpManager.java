package com.mfh.framework.rxapi.http;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class ClientLogHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ClientLogHttpManager INSTANCE = new ClientLogHttpManager();
    }

    //获取单例
    public static ClientLogHttpManager getInstance() {
        return ClientLogHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ClientLogService {

        /**
         * 创建日志
         */
        @GET("clientLog/create")
        Observable<MResponse<MValue<Long>>> create(@QueryMap Map<String, String> options);
    }

    public void create(Map<String, String> options, MValueSubscriber<Long> subscriber) {
        ClientLogService mfhApi = RxHttpManager.createService(ClientLogService.class);
        Observable observable = mfhApi.create(options)
                .map(new MValueResponseFunc<Long>());
        toSubscribe(observable, subscriber);
    }


}

package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.ProductStructure;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class ScProductStructureHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ScProductStructureHttpManager INSTANCE = new ScProductStructureHttpManager();
    }

    //获取单例
    public static ScProductStructureHttpManager getInstance() {
        return ScProductStructureHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ScProductStructureService{
        /**查询用户：
         * /scProductStructure/list?masterSkuId*/
        @GET("scProductStructure/list")
        Observable<MResponse<MRspQuery<ProductStructure>>> list(@QueryMap Map<String, String> options);
    }

    public void list(Map<String, String> options,
                     MQuerySubscriber<ProductStructure> subscriber) {
        ScProductStructureService mfhApi = RxHttpManager.createService(ScProductStructureService.class);
        Observable observable = mfhApi.list(options)
                .map(new MQueryResponseFunc<ProductStructure>());
        toSubscribe(observable, subscriber);
    }



}

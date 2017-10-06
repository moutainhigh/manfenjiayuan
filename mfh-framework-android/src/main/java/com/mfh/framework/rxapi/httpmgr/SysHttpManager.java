package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.rxapi.bean.Human;
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

public class SysHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final SysHttpManager INSTANCE = new SysHttpManager();
    }

    //获取单例
    public static SysHttpManager getInstance() {
        return SysHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvSkuStoreService{
        /**查询用户：
         * /pmc/sys/human/getHumanByIdentity?mobile=2123&humanId=31323*/
        @GET("sys/human/getHumanByIdentity")
        Observable<MResponse<Human>> getHumanByIdentity(@QueryMap Map<String, String> options);
    }

    public void getHumanByIdentity(Map<String, String> options, Subscriber<Human> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.getHumanByIdentity(options)
                .map(new MResponseFunc<Human>());
        toSubscribe(observable, subscriber);
    }



}

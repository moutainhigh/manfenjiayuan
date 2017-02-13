package com.mfh.framework.rxapi.http;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.func.MResponseFunc;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class EmbWxUserRegisterHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final EmbWxUserRegisterHttpManager INSTANCE = new EmbWxUserRegisterHttpManager();
    }

    //获取单例
    public static EmbWxUserRegisterHttpManager getInstance() {
        return EmbWxUserRegisterHttpManager.SingletonHolder.INSTANCE;
    }

    private interface UserRegisterService{
        /**
         * 首次对某个临时用户发送手机短信验证码
         * /embWxUserRegister/beginAuthenBysms?mobile=&sourceType=10, 注意返回一个userTmpId备用
         * 返回：{"code":"0","msg":"操作成功!","version":"1","data":6696}
         */
        @GET("embWxUserRegister/beginAuthenBysms")
        Observable<MResponse<Long>> beginAuthenBysms(@QueryMap Map<String, String> options);

        /**
         * 对某个临时用户重新发送手机短信验证码
         * embWxUserRegister/retryAuthenBysms?jsonStr={"userTmpId":6696,"mobile":"15250065084"},
         *
         * 返回：{"code":"0","msg":"操作成功!","version":"1","data":""}
         */
        @GET("embWxUserRegister/retryAuthenBysms")
        Observable<MResponse<String>> retryAuthenBysms(@QueryMap Map<String, String> options);

        /**
         * 对接收到的手机验证码进行验证。
         * embWxUserRegister/doAuthenBysms?jsonStr={"sourceType":10,"token":"11"}
         *
         * 返回：{"code":"1","msg":"缺少渠道端点标识！","data":null,"version":1}
         */
        @GET("embWxUserRegister/doAuthenBysms")
        Observable<MResponse<String>> doAuthenBysms(@QueryMap Map<String, String> options);

    }

    public void beginAuthenBysms(Map<String, String> options, Subscriber<Long> subscriber) {
        UserRegisterService mfhApi = RxHttpManager.createService(UserRegisterService.class);
        Observable observable = mfhApi.beginAuthenBysms(options)
                .map(new MResponseFunc<Long>());
        toSubscribe(observable, subscriber);
    }

    public void retryAuthenBysms(Map<String, String> options, Subscriber<String> subscriber) {
        UserRegisterService mfhApi = RxHttpManager.createService(UserRegisterService.class);
        Observable observable = mfhApi.retryAuthenBysms(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void doAuthenBysms(Map<String, String> options, Subscriber<String> subscriber) {
        UserRegisterService mfhApi = RxHttpManager.createService(UserRegisterService.class);
        Observable observable = mfhApi.doAuthenBysms(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }



}

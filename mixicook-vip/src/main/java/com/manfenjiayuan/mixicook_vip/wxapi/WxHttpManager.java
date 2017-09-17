package com.manfenjiayuan.mixicook_vip.wxapi;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * <a href="https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317853&lang=zh_CN">OAuth</a>
 * Created by bingshanguxue on 25/01/2017.
 */

public class WxHttpManager extends BaseHttpManager{

    public static String API_BASE_URL = "https://api.weixin.qq.com/";
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final WxHttpManager INSTANCE = new WxHttpManager();
    }

    //获取单例
    public static WxHttpManager getInstance() {
        return WxHttpManager.SingletonHolder.INSTANCE;
    }

    private interface WxService{
        /**
         * 通过code获取access_token
         * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
         */
        @GET("sns/oauth2/access_token")
        Observable<MResponse<WxAccessToken>> accessToken(@QueryMap Map<String, String> options);

        /**
         * 获取用户个人信息（UnionID机制）
         * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
         */
        @GET("sns/userinfo")
        Observable<MResponse<WxAccessToken>> userinfo(@QueryMap Map<String, String> options);
    }

    public void accessToken(Map<String, String> options, Subscriber<WxAccessToken> subscriber) {
        WxService mfhApi = RxHttpManager.createService(WxService.class, API_BASE_URL);
        Observable observable = mfhApi.accessToken(options)
                .map(new MResponseFunc<WxAccessToken>());
        toSubscribe(observable, subscriber);
    }

    public void userinfo(Map<String, String> options, Subscriber<WxAccessToken> subscriber) {
        WxService mfhApi = RxHttpManager.createService(WxService.class, API_BASE_URL);
        Observable observable = mfhApi.accessToken(options)
                .map(new MResponseFunc<WxAccessToken>());
        toSubscribe(observable, subscriber);
    }


}

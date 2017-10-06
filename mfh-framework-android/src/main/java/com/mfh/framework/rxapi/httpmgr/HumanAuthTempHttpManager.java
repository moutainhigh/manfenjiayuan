package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 26/01/2017.
 */

public class HumanAuthTempHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final HumanAuthTempHttpManager INSTANCE = new HumanAuthTempHttpManager();
    }

    //获取单例
    public static HumanAuthTempHttpManager getInstance() {
        return HumanAuthTempHttpManager.SingletonHolder.INSTANCE;
    }

    private interface HumanAuthTempService{
        /**判断该笔支付者有无绑定过平台用户，其中payType为支付类型（2-支付宝扫码付，256-微信扫码付），
         * outTradeNo为pos机生成的支付流水号
         * /humanAuthTemp/checkPayTempUserBindHuman?payType=256&outTradeNo=66_1001354_1467021942455
         * 成功返回：{"code":"0","msg":"操作成功!","version":"1","data":{"val":"true"}}
         */
        @GET("humanAuthTemp/checkPayTempUserBindHuman?")
        Observable<MResponse<MValue<String>>> checkPayTempUserBindHuman(@QueryMap Map<String, String> options);

        /**
         * 查询该笔支付绑定的平台用户，其中payType为支付类型（2-支付宝扫码付，256-微信扫码付），
         * outTradeNo为pos机生成的支付流水号
         * /humanAuthTemp/getPayTempUserBindHuman?payType=256&outTradeNo=66_1001354_1467021942455
         *
         * 成功返回：{"code":"0","msg":"操作成功!","version":"1","data":{"val":"136671"}}
         */
        @GET("humanAuthTemp/getPayTempUserBindHuman?")
        Observable<MResponse<MValue<String>>> getPayTempUserBindHuman(@QueryMap Map<String, String> options);


        /**
         * 绑定支付结果到平台账户
         * 成功返回： {"code":"0","msg":"操作成功!","version":"1","data":{"val":"136060"}}
         * */
        @GET("humanAuthTemp/bindPayTempUserBindHuman??")
        Observable<MResponse<MValue<String>>> bindPayTempUserBindHuman(@QueryMap Map<String, String> options);

    }

    public void checkPayTempUserBindHuman(Map<String, String> options, MValueSubscriber<String> subscriber) {
        HumanAuthTempService mfhApi = RxHttpManager.createService(HumanAuthTempService.class);
        Observable observable = mfhApi.checkPayTempUserBindHuman(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void getPayTempUserBindHuman(Map<String, String> options, MValueSubscriber<String> subscriber) {
        HumanAuthTempService mfhApi = RxHttpManager.createService(HumanAuthTempService.class);
        Observable observable = mfhApi.getPayTempUserBindHuman(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void bindPayTempUserBindHuman(Map<String, String> options, MValueSubscriber<String> subscriber) {
        HumanAuthTempService mfhApi = RxHttpManager.createService(HumanAuthTempService.class);
        Observable observable = mfhApi.bindPayTempUserBindHuman(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

}

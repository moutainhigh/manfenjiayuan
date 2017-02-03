package com.mfh.framework.rxapi.http;

import android.util.Base64;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mfh.framework.api.CompanyHuman;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.category.CategoryQueryInfo;
import com.mfh.framework.api.pmcstock.PosOrder;
import com.mfh.framework.api.scGoodsSku.ProductSkuBarcode;
import com.mfh.framework.api.tenant.SassInfo;
import com.mfh.framework.api.tenant.TenantInfo;
import com.mfh.framework.rxapi.AccessToken;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.interceptor.MfhHttpLoggingInterceptor;
import com.mfh.framework.rxapi.interceptor.MfhOAuthInterceptor;
import com.mfh.framework.rxapi.interceptor.MfhRequestInterceptor;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by bingshanguxue on 8/29/16.
 */

public class RxHttpManager {
    public static boolean RELEASE = true;
    public static boolean isUseRx = false;

    //baseUrl must end of '/'.
    public static String API_BASE_URL = "http://admin.mixicook.com/pmc/";

    //请求超时时间
    private static final int DEFAULT_TIMEOUT = 5;
//    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder().connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    // @formatter:off
    final static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();
    // @formatter:on

    private RxMfhService mMfhApi;

    /**
     * Create an instance of YOUR API interface.
     */
    public static <S> S createService(Class<S> serviceClass) {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new MfhRequestInterceptor())
                .addInterceptor(new MfhHttpLoggingInterceptor().setLevel(MfhHttpLoggingInterceptor.Level.BODY))
                .build();

        // Create a very simple REST adapter which points the API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build();

        return retrofit.create(serviceClass);
    }

    /**
     * Create an instance of YOUR API interface.
     */
    public static <S> S createService(Class<S> serviceClass, String apiBaseUrl) {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new MfhRequestInterceptor())
                .addInterceptor(new MfhHttpLoggingInterceptor().setLevel(MfhHttpLoggingInterceptor.Level.BODY))
                .build();

        // Create a very simple REST adapter which points the API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build();

        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String username, String password) {
        if (username != null && password != null) {
            String credentials = username + ":" + password;
            final String basic =
                    "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(new MfhOAuthInterceptor())
//                    .addInterceptor(new Interceptor() {
//                        @Override
//                        public Response intercept(Interceptor.Chain chain) throws IOException {
//                            Request original = chain.request();
//
//                            Request.Builder requestBuilder = original.newBuilder()
//                                    .header("Authorization", basic)
//                                    .header("Accept", "application/json")
//                                    .header("Set-Cookie", "")
//                                    .header("Cookie", "")
//                                    .method(original.method(), original.body());
//
//                            Request request = requestBuilder.build();
//                            return chain.proceed(request);
//                        }
//                    })
                    .addInterceptor(new MfhHttpLoggingInterceptor().setLevel(MfhHttpLoggingInterceptor.Level.BODY))
                    .build();

            // Create a very simple REST adapter which points the API.
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(httpClient)
                    .build();

            return retrofit.create(serviceClass);
        } else {
            return createService(serviceClass);
        }
    }

    public static <S> S createService(Class<S> serviceClass, final AccessToken token) {
        if (token != null) {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(new MfhRequestInterceptor())
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            Request original = chain.request();

                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Accept", "application/json")
                                    .header("Authorization",
                                            token.getTokenType() + " " + token.getAccessToken())
                                    .method(original.method(), original.body());

                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .addInterceptor(new MfhHttpLoggingInterceptor().setLevel(MfhHttpLoggingInterceptor.Level.BODY))
                    .build();

            // Create a very simple REST adapter which points the API.
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(httpClient)
                    .build();


            return retrofit.create(serviceClass);
        } else {
            return createService(serviceClass);
        }
    }

    //构造方法私有
    private RxHttpManager() {
        mMfhApi = createService(RxMfhService.class);
    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final RxHttpManager INSTANCE = new RxHttpManager();
    }

    //获取单例
    public static RxHttpManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 添加线程管理并订阅
     */
    public <T> void toSubscribe(Observable<T> o, Subscriber<T> s) {
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }

    /**
     * 登录
     *
     * @param subscriber 由调用者传过来的观察者对象
     * @param username   用户名
     * @param password   密码
     */
    public void login(Subscriber<UserMixInfo> subscriber, String username, String password) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class, username, password);
        Observable observable = mfhApi.login(username, password)
                .map(new MResponseFunc<UserMixInfo>());
        toSubscribe(observable, subscriber);
    }

    public void isSessionValid(String JSESSIONID, Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.isSessionValid(JSESSIONID)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void posRegisterCreate(String jsonStr, Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.posRegisterCreate(jsonStr)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void haveNoMoneyEnd(String JSESSIONID, MValueSubscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.haveNoMoneyEnd(JSESSIONID)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void needLockPos(String JSESSIONID, Long netId, Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.needLockPos(JSESSIONID, netId)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void getSaasInfo(Long id, Subscriber<SassInfo> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.getSaasInfo(id)
                .map(new MResponseFunc<SassInfo>());
        toSubscribe(observable, subscriber);
    }
    public void getSaasInfo2(String baseUrl, Long id, Subscriber<SassInfo> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class, baseUrl);
        Observable observable = mfhApi.getSaasInfo(id)
                .map(new MResponseFunc<SassInfo>());
        toSubscribe(observable, subscriber);
    }

    public void queryPrivList(String JSESSIONID, Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.queryPrivList(JSESSIONID)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void findCompUserPwdInfo(Map<String, String> options,
                                    MQuerySubscriber<CompanyHuman> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.findCompUserPwdInfo(options)
                .map(new MQueryResponseFunc<CompanyHuman>());
        toSubscribe(observable, subscriber);
    }
    public void listWhole(Map<String, String> options,
                                    MQuerySubscriber<TenantInfo> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.listWhole(options)
                .map(new MQueryResponseFunc<TenantInfo>());
        toSubscribe(observable, subscriber);
    }

    public void autoDateEnd(Map<String, String> options, Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.autoDateEnd(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
    public void analysisAggDateList(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<AggItem>> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.analysisAggDateList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AggItem>>());
        toSubscribe(observable, subscriber);
    }
    public void analysisAccDateList(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<AccItem>> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.analysisAccDateList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AccItem>>());
        toSubscribe(observable, subscriber);
    }
    public void autoShiftAnalysis(Map<String, String> options, Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.autoShiftAnalysis(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
    public void analysisAggShiftList(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<AggItem>> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.analysisAggShiftList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AggItem>>());
        toSubscribe(observable, subscriber);
    }
    public void accAnalysisAggShiftList(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<AccItem>> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.accAnalysisAggShiftList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AccItem>>());
        toSubscribe(observable, subscriber);
    }
    public void findGoodsOrderList(Map<String, String> options,
                                    MQuerySubscriber<PosOrder> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.findGoodsOrderList(options)
                .map(new MQueryResponseFunc<PosOrder>());
        toSubscribe(observable, subscriber);
    }

    public void findShopOtherBarcodes(Map<String, String> options, MQuerySubscriber<ProductSkuBarcode> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.findShopOtherBarcodes(options)
                .map(new MQueryResponseFunc<ProductSkuBarcode>());
        toSubscribe(observable, subscriber);
    }

    public void commintCashAndTrigDateEnd(String outTradeNo, Subscriber<String> subscriber) {
        RxAnalysisService mfhApi = RxHttpManager.createService(RxAnalysisService.class);
        Observable observable = mfhApi.commintCashAndTrigDateEnd(outTradeNo)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void comnQuery(Map<String, String> options, Subscriber<CategoryQueryInfo> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.comnQuery(options)
                .map(new MResponseFunc<CategoryQueryInfo>());
        toSubscribe(observable, subscriber);
    }
    public void getTopFrontId(Map<String, String> options, Subscriber<CategoryInfo> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.getTopFrontId(options)
                .map(new MResponseFunc<CategoryInfo>());
        toSubscribe(observable, subscriber);
    }
    public void getCodeValue(Map<String, String> options, Subscriber<List<CategoryInfo>> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.getCodeValue(options)
                .map(new MResponseFunc<List<CategoryInfo>>());
        toSubscribe(observable, subscriber);
    }
    public void create(String JSESSIONID, JSONObject jsonStr, MValueSubscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.create(JSESSIONID, jsonStr.toJSONString())
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
    public void alipayBarPay(Map<String, String> options, Subscriber<MResponse<String>> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.alipayBarPay(options);
//                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void wepayBarPay(Map<String, String> options, Subscriber<MResponse<String>> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.wepayBarPay(options);
//                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
    public void createPayOrder(Map<String, String> options, MValueSubscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.createPayOrder(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void batchInOrders(String JSESSIONID, JSONArray jsonStr,
                              Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.batchInOrders(JSESSIONID, jsonStr.toJSONString())
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void getMaxPosOrderId(String posId, MValueSubscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.getMaxPosOrderId(posId)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void exit(String JSESSIONID,
                              Subscriber<String> subscriber) {
        RxMfhService mfhApi = RxHttpManager.createService(RxMfhService.class);
        Observable observable = mfhApi.exit(JSESSIONID)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

//    public void posRegisterList(Subscriber<MQueryResponse<PosRegister>> subscriber) {
//        MfhApi mfhClient = MfhService.createService(MfhApi.class);
//        Observable observable = mfhClient.posRegisterList();
//
//        RetrofitHttp.getInstance().toSubscribe(observable, subscriber);
//    }

//    public void posRegisterList2(Subscriber<MQueryResponse<PosRegister>> subscriber) {
//        RxMfhApi mfhApi = MfhService.createService(RxMfhApi.class);
//        Observable observable = mfhApi.posRegisterList2();
//
//        RetrofitHttp.getInstance().toSubscribe(observable, subscriber);
//    }
}

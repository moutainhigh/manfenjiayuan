package com.mfh.framework.rxapi;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by [bingshanguxue@gmail.com] on 15/8/5.
 * */
public class RetrofitHttp {

    //example: "http://www.bingshanguxue.me/", must end of '/'.
    public static String API_BASE_URL = "http://bingshanguxue.github.io/";

    private static final int DEFAULT_TIMEOUT = 30;

    // @formatter:off
    final static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();
    // @formatter:on

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, API_BASE_URL);
    }

    /**
     * Create an instance of YOUR API interface.
     *
     * @param baseUrl must end of '/'.
     * */
    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new HttpLoggingInterceptor())
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();

        // Create a very simple REST adapter which points the API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();


        return retrofit.create(serviceClass);
    }

    public RetrofitHttp() {
        //TODO
    }

    private static class SingletonHolder{
        private static final RetrofitHttp INSTANCE = new RetrofitHttp();
    }

    public static RetrofitHttp getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 添加线程管理并订阅
     * */
    public <T> void toSubscribe(Observable<T> o, Subscriber<T> s){
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }
}
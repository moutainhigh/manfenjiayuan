package com.mfh.framework.rxapi.http;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * <ul>
 *     QueryMap 字段值不能为空
 *     <li>java.lang.IllegalArgumentException: Query map contained null value for key 'couponsIds'.</li>
 * </ul>
 * Created by bingshanguxue on 25/01/2017.
 */

public class BaseHttpManager {

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
     * 其实也是将File封装成RequestBody，然后再封装成Part，<br>
     * 不同的是使用MultipartBody.Builder来构建MultipartBody
     * @param key 同上
     * @param filePaths 同上
     * @param imageType 同上
     */
    public static MultipartBody filesToMultipartBody(String key,
                                                     String[] filePaths,
                                                     MediaType imageType) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (String filePath : filePaths) {
            File file = new File(filePath);
            RequestBody requestBody = RequestBody.create(imageType, file);
            builder.addFormDataPart(key, file.getName(), requestBody);
        }
        builder.setType(MultipartBody.FORM);
        return builder.build();
    }

    /**
     * 其实也是将File封装成RequestBody，然后再封装成Part，<br>
     * 不同的是使用MultipartBody.Builder来构建MultipartBody
     * @param key 同上
     * @param filePaths 同上
     * @param imageType 同上
     */
    public static MultipartBody fileToMultipartBody(String key,
                                                     File file,
                                                     MediaType imageType) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        RequestBody requestBody = RequestBody.create(imageType, file);
        builder.addFormDataPart(key, null, requestBody);
        builder.setType(MultipartBody.FORM);
        return builder.build();
    }
    public static MultipartBody fileToMultipartBody2(String key,
                                                    File file,
                                                    MediaType imageType) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        RequestBody requestBody = RequestBody.create(imageType, file);
        builder.addFormDataPart(key, file.getName(), requestBody);
        builder.setType(MultipartBody.FORM);
        return builder.build();
    }
}

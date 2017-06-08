package com.mfh.framework.rxapi.http;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class ResHttpManager extends BaseHttpManager{
    public static final MediaType FILE = MediaType.parse("multipart/form-data");
    public static final MediaType STREAM = MediaType.parse("application/octet-stream");
    public static final MediaType ZIP = MediaType.parse("application/zip");
    public static final MediaType JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType TEXT = MediaType.parse("text/plain");

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ResHttpManager INSTANCE = new ResHttpManager();
    }

    //获取单例
    public static ResHttpManager getInstance() {
        return ResHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ResService{
        /**
         * 图片上传修改
         * 采用multipart/form-data或post方式提交图片。
         * /res/remotesave/upload? responseType=1
         */
        @Multipart
        @POST("res/remotesave/upload")
        Observable<MResponse<MValue<Long>>> upload(@Part List<MultipartBody.Part> parts);


        @Multipart
        @POST("res/remotesave/upload")
        Observable<MResponse<MValue<Long>>> upload2(@Part MultipartBody.Part responseType,
                                                    @Part MultipartBody.Part file);

//        @Body parameters cannot be used with form or multi-part encoding.
        @Multipart
        @POST("res/remotesave/upload")
        Observable<MResponse<MValue<Long>>> upload3(@Body RequestBody responseType,
                                                    @Body RequestBody fileToUpload);

        @FormUrlEncoded
        @POST("res/remotesave/upload")
        Observable<MResponse<MValue<Long>>> upload4(@Field("responseType") String responseType,
                                                    @Field("fileToUpload") File fileToUpload);
    }

    public void upload(List<MultipartBody.Part> parts, MValueSubscriber<Long> subscriber) {


//            MultipartBody.Builder builder = new MultipartBody.Builder();
//            builder.setType(MultipartBody.FORM);
//            builder.addFormDataPart("responseType", "1");
//            RequestBody requestBody = RequestBody.create(ResHttpManager.ZIP, file);
//            builder.addFormDataPart("fileToUpload", file.getName(), requestBody);

//        RequestBody responseType =
//                RequestBody.create(
//                        MediaType.parse("multipart/form-data"), "1");
//
//        String mimeType = FileUtil.getMimeType(file);
//        ZLogger.d("mimeType: " + mimeType);
//        // create RequestBody instance from file
////        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
//        RequestBody requestFile = RequestBody.create(MediaType.parse("application/zip"), file);
////        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        // MultipartBody.Part is used to send also the actual file name
//        MultipartBody.Part body = MultipartBody.Part.createFormData("fileToUpload", file.getName(), requestFile);

        ResService mfhApi = RxHttpManager.createService2(ResService.class);
        Observable observable = mfhApi.upload(parts)
                .map(new MValueResponseFunc<Long>());
        toSubscribe(observable, subscriber);
    }
    public void upload2(File file, MValueSubscriber<Long> subscriber) {
        MultipartBody.Part responseTypePart = MultipartBody.Part.createFormData("responseType", "1");
        RequestBody requestBody = RequestBody.create(ResHttpManager.ZIP, file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("fileToUpload", file.getName(),
                requestBody);

        ResService mfhApi = RxHttpManager.createService2(ResService.class);
        Observable observable = mfhApi.upload2(responseTypePart, filePart)
                .map(new MValueResponseFunc<Long>());
        toSubscribe(observable, subscriber);
    }

    public void upload3(RequestBody responseType, RequestBody fileToUpload, MValueSubscriber<Long> subscriber) {
        ResService mfhApi = RxHttpManager.createService2(ResService.class);
        Observable observable = mfhApi.upload3(responseType, fileToUpload)
                .map(new MValueResponseFunc<Long>());
        toSubscribe(observable, subscriber);
    }
    public void upload4(String responseType, File fileToUpload, MValueSubscriber<Long> subscriber) {
        ResService mfhApi = RxHttpManager.createService2(ResService.class);
        Observable observable = mfhApi.upload4(responseType, fileToUpload)
                .map(new MValueResponseFunc<Long>());
        toSubscribe(observable, subscriber);
    }
//    public void upload(Map<String, String> options, File file, MValueSubscriber<Long> subscriber) {
//        ResService mfhApi = RxHttpManager.createService(ResService.class);
//        Observable observable = mfhApi.upload(options,
//                fileToMultipartBody("fileToUpload", file, MediaType.parse("text/plain")))
//                .map(new MValueResponseFunc<Long>());
//        toSubscribe(observable, subscriber);
//    }



}

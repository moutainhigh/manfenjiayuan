package com.mfh.framework.rxapi.subscriber;

import android.content.Intent;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.http.ExceptionHandle;

import java.util.List;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 26/12/2016.
 */

public class MQuerySubscriber<T> extends Subscriber<MRspQuery<T>> {

    private PageInfo mPageInfo;


    public MQuerySubscriber(PageInfo pageInfo) {
        mPageInfo = pageInfo;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        ZLogger.e("MQuerySubscriber.throwable =" + e.toString());
        ZLogger.e("MQuerySubscriber.throwable =" + e.getMessage());

        ExceptionHandle.ResponeThrowable throwable;
        if (e instanceof Exception) {
            ZLogger.d("Exception");
            //访问获得对应的Exception
            throwable = ExceptionHandle.handleException(e);
            if (throwable.code == ExceptionHandle.ERROR.HTTP_ERROR_RETRY_LOGIN) {
                MfhApplication.getAppContext().sendBroadcast(new Intent("com.mfh.litecashier.service.ACTION_UNAUTHORIZED"));
            }
        } else {
            ZLogger.d("ExceptionHandle");
            //将Throwable 和 未知错误的status code返回
            throwable = new ExceptionHandle.ResponeThrowable(e,
                    ExceptionHandle.ERROR.UNKNOWN);
            if (throwable.code == ExceptionHandle.ERROR.HTTP_ERROR_RETRY_LOGIN) {
                MfhApplication.getAppContext().sendBroadcast(new Intent("com.mfh.litecashier.service.ACTION_UNAUTHORIZED"));
            }
        }
    }

    @Override
    public void onNext(MRspQuery<T> rspQuery) {
        if (rspQuery != null){
            ZLogger.d(String.format("一共 %d 条记录", rspQuery.getTotal()));
            if (mPageInfo != null){
                mPageInfo.setTotalCount((int)rspQuery.getTotal());
            }
            onQueryNext(mPageInfo, rspQuery.getRows());
        }
        else{
            ZLogger.d("未查询到结果");
            onQueryNext(mPageInfo, null);
        }
    }

    public void onQueryNext(PageInfo pageInfo, List<T> dataList){

    }


}

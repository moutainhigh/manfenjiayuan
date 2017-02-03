package com.mfh.framework.rxapi.subscriber;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.entity.MRspQuery;

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

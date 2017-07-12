package com.mfh.framework.rxapi.http;

import com.mfh.framework.rxapi.bean.CommonAccountFlow;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * 会员账户消费记录
 * Created by bingshanguxue on 03/07/2017.
 */

public class CommonAccountHistoryHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final CommonAccountHistoryHttpManager INSTANCE = new CommonAccountHistoryHttpManager();
    }

    //获取单例
    public static CommonAccountHistoryHttpManager getInstance() {
        return CommonAccountHistoryHttpManager.SingletonHolder.INSTANCE;
    }

    private interface CommonAccountHistoryService {
        /**
         * 门店工作人员查询会员账户消费流水
         * /commonAccountHistory/queryCustomerFlow?humanId|mobile|cardNo=&createdDate=&createdDate_fan1=&bizType=&subjectId=&wayType=
         * 其中关于bizType、wayType和subjectId的取值范围参见/commonAccountHistory/getAbility?kind=query
         */
        @GET("commonAccountHistory/queryCustomerFlow")
        Observable<MResponse<MRspQuery<CommonAccountFlow>>> queryCustomerFlow(@QueryMap Map<String, String> options);

    }

    public void queryCustomerFlow(Map<String, String> options, MQuerySubscriber<CommonAccountFlow> subscriber) {
        CommonAccountHistoryService mfhApi = RxHttpManager.createService(CommonAccountHistoryService.class);
        Observable observable = mfhApi.queryCustomerFlow(options)
                .map(new MQueryResponseFunc<CommonAccountFlow>());
        toSubscribe(observable, subscriber);
    }


}

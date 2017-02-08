package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class PmcStockHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final PmcStockHttpManager INSTANCE = new PmcStockHttpManager();
    }

    //获取单例
    public static PmcStockHttpManager getInstance() {
        return PmcStockHttpManager.SingletonHolder.INSTANCE;
    }

    private interface PmcStockService{
        /**查询卡券,订单提交前*/
        @GET("pmcstock/findMarketRulesByOrderInfos")
        Observable<MResponse<MRspQuery<MarketRulesWrapper>>> findMarketRulesByOrderInfos(@QueryMap Map<String, String> options);

    }

    public void findMarketRulesByOrderInfos(Map<String, String> options,
                          MQuerySubscriber<MarketRulesWrapper> subscriber) {
        PmcStockService mfhApi = RxHttpManager.createService(PmcStockService.class);
        Observable observable = mfhApi.findMarketRulesByOrderInfos(options)
                .map(new MQueryResponseFunc<MarketRulesWrapper>());
        toSubscribe(observable, subscriber);
    }


}

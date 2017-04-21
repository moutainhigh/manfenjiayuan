package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.ProductAggDate;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * 销售记录
 * Created by bingshanguxue on 25/01/2017.
 */

public class ProductAggDateHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ProductAggDateHttpManager INSTANCE = new ProductAggDateHttpManager();
    }

    //获取单例
    public static ProductAggDateHttpManager getInstance() {
        return ProductAggDateHttpManager.SingletonHolder.INSTANCE;
    }

    private interface AnalysisService {

        /**
         * 查询商品销量记录
         *
         * <p>
         *     /productAggDate/list?officeId=135852&proSkuId=38968
         * officeId代表当前登录网点， proSkuId是产品sku编号
         * </p>
         * <p>
         *  按产品线日对账：
         /productAggDate/list?aggDateStr=2017-04-15&officeId=&bizType=7&subType=
         其中subType是产品线类型，返回的productNum是数量小计，turnover是营业额
         * </p>
         *
         */
        @GET("productAggDate/list")
        Observable<MResponse<MRspQuery<MEntityWrapper<ProductAggDate>>>> list(@QueryMap Map<String, String> options);

    }
    

    public void list(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<ProductAggDate>> subscriber) {
        AnalysisService mfhApi = RxHttpManager.createService(AnalysisService.class);
        Observable observable = mfhApi.list(options)
                .map(new MQueryResponseFunc<MEntityWrapper<ProductAggDate>>());
        toSubscribe(observable, subscriber);
    }



}

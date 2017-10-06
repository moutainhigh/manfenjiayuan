package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.pmcstock.GoodsItem;
import com.mfh.framework.rxapi.bean.GoodsOrder;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class PmcStockHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final PmcStockHttpManager INSTANCE = new PmcStockHttpManager();
    }

    //获取单例
    public static PmcStockHttpManager getInstance() {
        return PmcStockHttpManager.SingletonHolder.INSTANCE;
    }

    private interface PmcStockService{
        /**查询多条订单的规则和优惠券信息,订单提交前
         * 适用于客户端拆单的情况*/
        @GET("pmcstock/findMarketRulesByOrderInfos")
        Observable<MResponse<MRspQuery<MarketRulesWrapper>>> findMarketRulesByOrderInfos(@QueryMap Map<String, String> options);

        /**查询商品流水*/
        @GET("pmcstock/findGoodsItemList")
        Observable<MResponse<MRspQuery<GoodsItem>>> findGoodsItemList(@QueryMap Map<String, String> options);

        /**
         * 查询订单列表，包括pos、团购等；若指定btype则只查询此类订单。
         * /pmcstock/getGoodsOrderListByHuman?humanId=&btype=&subTypes=&orderStatus=&sellOffices=
         * */
        @GET("pmcstock/findGoodsOrderList")
        Observable<MResponse<MRspQuery<GoodsOrder>>> findGoodsOrderList(@QueryMap Map<String, String> options);

        /**
         * 查询一个人的订单列表，包括pos、团购等；若指定btype则只查询此类订单。
         * /pmcstock/getGoodsOrderListByHuman?humanId=&btype=
         * */
        @GET("pmcstock/getGoodsOrderListByHuman")
        Observable<MResponse<MRspQuery<GoodsOrder>>> getGoodsOrderListByHuman(@QueryMap Map<String, String> options);

    }

    public void findMarketRulesByOrderInfos(Map<String, String> options,
                          MQuerySubscriber<MarketRulesWrapper> subscriber) {
        PmcStockService mfhApi = RxHttpManager.createService(PmcStockService.class);
        Observable observable = mfhApi.findMarketRulesByOrderInfos(options)
                .map(new MQueryResponseFunc<MarketRulesWrapper>());
        toSubscribe(observable, subscriber);
    }

    public void findGoodsItemList(Map<String, String> options,
                                            MQuerySubscriber<GoodsItem> subscriber) {
        PmcStockService mfhApi = RxHttpManager.createService(PmcStockService.class);
        Observable observable = mfhApi.findGoodsItemList(options)
                .map(new MQueryResponseFunc<GoodsItem>());
        toSubscribe(observable, subscriber);
    }

    /**
     * 订单预支付
     * @param btype 业务类型, 3-商城(必填)
     * @param subTypes 子业务类型,2,3,4
     * @param orderStatus 订单状态
     * @param sellOffices 销售网点
     * @param pageInfo 分页信息
     * */
    public void findGoodsOrderList(Map<String, String> options,
                                   MQuerySubscriber<GoodsOrder> subscriber) {
        PmcStockService mfhApi = RxHttpManager.createService(PmcStockService.class);
        Observable observable = mfhApi.findGoodsOrderList(options)
                .map(new MQueryResponseFunc<GoodsOrder>());
        toSubscribe(observable, subscriber);
    }

    public void getGoodsOrderListByHuman(Map<String, String> options,
                                         MQuerySubscriber<GoodsItem> subscriber) {
        PmcStockService mfhApi = RxHttpManager.createService(PmcStockService.class);
        Observable observable = mfhApi.getGoodsOrderListByHuman(options)
                .map(new MQueryResponseFunc<GoodsOrder>());
        toSubscribe(observable, subscriber);
    }


}

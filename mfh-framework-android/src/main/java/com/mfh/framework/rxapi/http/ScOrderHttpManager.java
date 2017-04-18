package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.func.MResponseFunc;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

import static com.mfh.framework.api.scOrder.ScOrderApi.URL_SCORDER;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class ScOrderHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ScOrderHttpManager INSTANCE = new ScOrderHttpManager();
    }

    //获取单例
    public static ScOrderHttpManager getInstance() {
        return ScOrderHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ScOrderService{
        /**
         * 查询订单
         * /scOrder/getByCode?barcode=9903000000273899
         */
        @GET("scOrder/getByCode")
        Observable<MResponse<ScOrder>> getByCode(@QueryMap Map<String, String> options);

        /**
         * 替换{@link #URL_UPDATECOMMITINFO_WHENPREPAIRED}接口，
         * 支持：“增加“妥投”,扫描订单条码,可以显示订单商品,点击打勾按钮,即可完成妥投。
         * 支持单品的退单,删除单品后,订单价格会发⽣生变化”，也使用这个接口updateCommitInfo，退单时这个明细的数量为0即可。
         * /scOrder/updateCommitInfo?id=&jsonStr=[{skuId:11, bcount:10}, {skuId:12, bcount:10}]
         */
        @GET("scOrder/updateCommitInfo")
        Observable<MResponse<String>> updateCommitInfo(@QueryMap Map<String, String> options);

        /**
         * 当前登录人员即买手或发货人员，选择一个骑手并进行发货，并且通知骑手，
         * 其中transHumanId为骑手编号，orderId为订单编号
         * (针对已组货的订单)
         * /scOrder/prepareOrder?orderId=&transHumanId=
         */
        @GET("scOrder/prepareOrder")
        Observable<MResponse<String>> prepareOrder(@QueryMap Map<String, String> options);

    }

    public void getByCode(Map<String, String> options, Subscriber<ScOrder> subscriber) {
        ScOrderService mfhApi = RxHttpManager.createService(ScOrderService.class);
        Observable observable = mfhApi.getByCode(options)
                .map(new MResponseFunc<ScOrder>());
        toSubscribe(observable, subscriber);
    }

    /**
     * 当前登录人员即买手或发货人员，选择一个骑手并进行发货，并且通知骑手
     * @param id 订单编号
     * @param jsonStr 订单明细
     */
    public void updateCommitInfo(Map<String, String> options, Subscriber<String> subscriber) {
        ScOrderService mfhApi = RxHttpManager.createService(ScOrderService.class);
        Observable observable = mfhApi.updateCommitInfo(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void prepareOrder(Map<String, String> options, Subscriber<String> subscriber) {
        ScOrderService mfhApi = RxHttpManager.createService(ScOrderService.class);
        Observable observable = mfhApi.prepareOrder(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }



}

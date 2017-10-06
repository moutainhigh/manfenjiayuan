package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.rxapi.bean.GroupBuyActivity;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * 团购活动
 * Created by bingshanguxue on 03/07/2017.
 */

public class GroupBuyOrderHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final GroupBuyOrderHttpManager INSTANCE = new GroupBuyOrderHttpManager();
    }

    //获取单例
    public static GroupBuyOrderHttpManager getInstance() {
        return GroupBuyOrderHttpManager.SingletonHolder.INSTANCE;
    }

    private interface GroupBuyOrderService {
        /**
         * 门店人员查询需要到本店提货的团购活动列表
         */
        @GET("groupBuyOrder/queryNetBeans")
        Observable<MResponse<MRspQuery<GroupBuyActivity>>> queryNetBeans(@QueryMap Map<String, String> options);

        /**
         * 门店人员查询指定团购活动中需要到本店提货的订单列表，按订单状态排序；activityId是团购活动编号
         * /groupBuyOrder/queryNetOrder?activityId=
         */
        @GET("groupBuyOrder/queryNetOrder")
        Observable<MResponse<MRspQuery<GroupBuyOrder>>> queryNetOrder(@QueryMap Map<String, String> options);

        /**
         * 门店人员通知单个订单客户提货，其中id是单个订单编号
         */
        @POST("groupBuyOrder/notifyHumanTakeGood")
        Observable<MResponse<String>> notifyHumanTakeGood(@QueryMap Map<String, String> options);

        /**
         * 门店人员通知团购活动的所有订单客户提货，其中id是团购活动编号
         */
        @POST("groupBuyOrder/notifyTakeGoods")
        Observable<MResponse<String>> notifyTakeGoods(@QueryMap Map<String, String> options);

        /**
         * 门店人员扫码用户会员码得到该用户在本店的待提团购订单列表
         */
        @GET("groupBuyOrder/queryHumanOrder")
        Observable<MResponse<MRspQuery<GroupBuyOrder>>> queryHumanOrder(@QueryMap Map<String, String> options);

        /**
         * 门店人员通知单个订单客户提货，其中id是单个订单编号
         */
        @GET("groupBuyOrder/receiveAndFinishOrder")
        Observable<MResponse<String>> receiveAndFinishOrder(@QueryMap Map<String, String> options);
    }

    public void queryNetBeans(Map<String, String> options, MQuerySubscriber<GroupBuyActivity> subscriber) {
        GroupBuyOrderService mfhApi = RxHttpManager.createService(GroupBuyOrderService.class);
        Observable observable = mfhApi.queryNetBeans(options)
                .map(new MQueryResponseFunc<GroupBuyActivity>());
        toSubscribe(observable, subscriber);
    }

    public void queryNetOrder(Map<String, String> options,
                              MQuerySubscriber<GroupBuyOrder> subscriber) {
        GroupBuyOrderService mfhApi = RxHttpManager.createService(GroupBuyOrderService.class);
        Observable observable = mfhApi.queryNetOrder(options)
                .map(new MQueryResponseFunc<GroupBuyOrder>());
        toSubscribe(observable, subscriber);
    }

    public void notifyHumanTakeGood(Map<String, String> options, Subscriber<String> subscriber) {
        GroupBuyOrderService mfhApi = RxHttpManager.createService(GroupBuyOrderService.class);
        Observable observable = mfhApi.notifyHumanTakeGood(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void notifyTakeGoods(Map<String, String> options, Subscriber<String> subscriber) {
        GroupBuyOrderService mfhApi = RxHttpManager.createService(GroupBuyOrderService.class);
        Observable observable = mfhApi.notifyTakeGoods(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void receiveAndFinishOrder(Map<String, String> options, Subscriber<String> subscriber) {
        GroupBuyOrderService mfhApi = RxHttpManager.createService(GroupBuyOrderService.class);
        Observable observable = mfhApi.receiveAndFinishOrder(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void queryHumanOrder(Map<String, String> options, MQuerySubscriber<GroupBuyOrder> subscriber) {
        GroupBuyOrderService mfhApi = RxHttpManager.createService(GroupBuyOrderService.class);
        Observable observable = mfhApi.queryHumanOrder(options)
                .map(new MQueryResponseFunc<GroupBuyOrder>());
        toSubscribe(observable, subscriber);
    }


}

package com.manfenjiayuan.business.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.bean.GroupBuyActivity;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.rxapi.http.GroupBuyOrderHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 19/11/2016.
 */

public class GroupBuyOrderMode {

    /**
     * 盘点列表
     */
    public void queryNetBeans(PageInfo pageInfo, final OnPageModeListener<GroupBuyActivity> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
//        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
//        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        GroupBuyOrderHttpManager.getInstance().queryNetBeans(options,
                new MQuerySubscriber<GroupBuyActivity>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<GroupBuyActivity> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.e("加载报团购活动列表失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }
                });
    }

    /**
     * 盘点列表
     */
    public void queryNetOrder(Long activityId, PageInfo pageInfo, final OnPageModeListener<GroupBuyOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
//        options.put("wrapper", "true");
        options.put("activityId", String.valueOf(activityId));
//        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        GroupBuyOrderHttpManager.getInstance().queryNetOrder(options,
                new MQuerySubscriber<GroupBuyOrder>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<GroupBuyOrder> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.e("加载报团购活动列表失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }
                });
    }

    /**
     * 门店人员通知单个订单客户提货，其中id是单个订单编号
     */
    public void notifyHumanTakeGood(Long id, final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(id));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        GroupBuyOrderHttpManager.getInstance().notifyHumanTakeGood(options,
                new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e("加载报团购活动列表失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(String s) {
                        if (listener != null) {
                            listener.onSuccess(s);
                        }
                    }
                });
    }

    /**
     * 门店人员通知团购活动的所有订单客户提货，其中id是团购活动编号
     */
    public void notifyTakeGoods(Long id, final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(id));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        GroupBuyOrderHttpManager.getInstance().notifyTakeGoods(options,
                new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e("加载报团购活动列表失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(String s) {
                        if (listener != null) {
                            listener.onSuccess(s);
                        }
                    }
                });
    }

    /**
     * 门店人员通知单个订单客户提货，其中id是单个订单编号
     */
    public void receiveAndFinishOrder(Long id, final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(id));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        GroupBuyOrderHttpManager.getInstance().receiveAndFinishOrder(options,
                new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e("门店人员通知单个订单客户提货:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(String s) {
                        if (listener != null) {
                            listener.onSuccess(s);
                        }
                    }
                });
    }


    /**
     * 盘点列表
     */
    public void queryHumanOrder(Long humanId, PageInfo pageInfo, final OnPageModeListener<GroupBuyOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
//        options.put("wrapper", "true");
        options.put("humanId", String.valueOf(humanId));
//        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        GroupBuyOrderHttpManager.getInstance().queryHumanOrder(options,
                new MQuerySubscriber<GroupBuyOrder>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<GroupBuyOrder> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.e("加载报团购活动列表失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }
                });
    }
}

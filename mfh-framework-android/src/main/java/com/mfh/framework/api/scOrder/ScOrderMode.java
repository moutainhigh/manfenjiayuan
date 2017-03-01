package com.mfh.framework.api.scOrder;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.ScOrderHttpManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

/**
 * 商城订单
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrderMode {
    /**
     * 查询订单
     */
    public void getByCode(String barcode, final OnModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack queryResCallback = new NetCallBack.NetTaskCallBack<ScOrder,
                NetProcessor.Processor<ScOrder>>(
                new NetProcessor.Processor<ScOrder>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        ScOrder scOrder = null;
                        if (rspData != null) {
                            RspBean<ScOrder> retValue = (RspBean<ScOrder>) rspData;
                            scOrder = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(scOrder);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询失败: " + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , ScOrder.class
                , MfhApplication.getAppContext()) {
        };

        ScOrderApi.getByCode(barcode, queryResCallback);
    }

    /**
     * 查询订单
     */
    public void getByBarcode(String barcode, Integer status, boolean isNeedDetail,
                             final OnModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("barcode", barcode);
        if (status != null){
            options.put("status", String.valueOf(status));
        }
        options.put("needDetail", String.valueOf(isNeedDetail));

        ScOrderHttpManager.getInstance().getByCode(options,
                new Subscriber<ScOrder>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.df("查询失败: " + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(ScOrder scOrder) {
                        if (listener != null) {
                            listener.onSuccess(scOrder);
                        }
                    }

                });
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     */
    public void findPrepareAbleOrders(PageInfo pageInfo, final OnPageModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScOrder>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScOrder> rs) {
                //此处在主线程中执行。
                List<ScOrder> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScOrder> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载待拣货订单失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScOrder.class, MfhApplication.getAppContext());

        ScOrderApiImpl.findPrepareAbleOrders(pageInfo, queryRsCallBack);
    }


    /**
     *  获取当前登录骑手待配送的订单列表
     */
    public void findSendAbleOrders(PageInfo pageInfo, final OnPageModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScOrder> rs) {
                        //此处在主线程中执行。
                        List<ScOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ScOrder> wrapper : rs.getRowDatas()) {
                                entityList.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载待拣货订单失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ScOrder.class, MfhApplication.getAppContext());

        ScOrderApiImpl.findSendAbleOrders(pageInfo, queryRsCallBack);
    }


    /**
     *  获取指定网点可配送抢单的订单列表
     */
    public void findAcceptAbleSendOrders(PageInfo pageInfo, final OnPageModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScOrder> rs) {
                        //此处在主线程中执行。
                        List<ScOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ScOrder> wrapper : rs.getRowDatas()) {
                                entityList.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载待拣货订单失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ScOrder.class, MfhApplication.getAppContext());

        ScOrderApiImpl.findAcceptAbleSendOrders(pageInfo, queryRsCallBack);
    }

    /**
     *
     */
    public void findServicingOrders(PageInfo pageInfo, int roleType,
                                    final OnPageModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScOrder> rs) {
                        //此处在主线程中执行。
                        List<ScOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ScOrder> wrapper : rs.getRowDatas()) {
                                entityList.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载待拣货订单失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ScOrder.class, MfhApplication.getAppContext());

        ScOrderApiImpl.findServicingOrders(pageInfo, roleType, queryRsCallBack);
    }

    /**
     *
     */
    public void findServicedOrders(PageInfo pageInfo, int roleType, String status,
                                   final OnPageModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScOrder> rs) {
                        //此处在主线程中执行。
                        List<ScOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ScOrder> wrapper : rs.getRowDatas()) {
                                entityList.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载待拣货订单失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ScOrder.class, MfhApplication.getAppContext());

        ScOrderApiImpl.findServicedOrders(pageInfo, roleType, status, queryRsCallBack);
    }

    /**
     *
     */
    public void findCancelOrders(PageInfo pageInfo, int roleType,
                                   final OnPageModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScOrder> rs) {
                        //此处在主线程中执行。
                        List<ScOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ScOrder> wrapper : rs.getRowDatas()) {
                                entityList.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载待拣货订单失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ScOrder.class, MfhApplication.getAppContext());

        ScOrderApiImpl.findCancelOrders(pageInfo, roleType, queryRsCallBack);
    }

}

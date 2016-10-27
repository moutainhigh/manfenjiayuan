package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.view.IScOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderMode;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;


/**
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrderPresenter {
    private IScOrderView mIScOrderView;
    private ScOrderMode mScOrderMode;

    public ScOrderPresenter(IScOrderView iScOrderView) {
        this.mIScOrderView = iScOrderView;
        this.mScOrderMode = new ScOrderMode();
    }

    /**
     * 查询订单
     * */
    public void getByCode(String barcode) {
        mScOrderMode.getByCode(barcode, new OnModeListener<ScOrder>() {
            @Override
            public void onProcess() {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(ScOrder data) {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewError(errorMsg);
                }
            }
        });
    }

    /**
     * 查询订单
     * */
    public void getByBarcode(String barcode, Integer status, boolean isNeedDetail) {
        mScOrderMode.getByBarcode(barcode, status, isNeedDetail, new OnModeListener<ScOrder>() {
            @Override
            public void onProcess() {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(ScOrder data) {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewError(errorMsg);
                }
            }
        });
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     * */
    public void findPrepareAbleOrders(PageInfo pageInfo){
        mScOrderMode.findPrepareAbleOrders(pageInfo,
                new OnPageModeListener<ScOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 获取当前登录骑手待配送的订单列表
     * */
    public void findSendAbleOrders(PageInfo pageInfo){
        mScOrderMode.findSendAbleOrders(pageInfo,
                new OnPageModeListener<ScOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewError(errorMsg);
                        }
                    }
                });
    }
    /**
     * 获取指定网点可配送抢单的订单列表
     * */
    public void findAcceptAbleSendOrders(PageInfo pageInfo){
        mScOrderMode.findAcceptAbleSendOrders(pageInfo,
                new OnPageModeListener<ScOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     *
     * */
    public void findServicingOrders(PageInfo pageInfo, int roleType){
        mScOrderMode.findServicingOrders(pageInfo, roleType,
                new OnPageModeListener<ScOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     *
     * */
    public void findServicedOrders(PageInfo pageInfo, int roleType, String status){
        mScOrderMode.findServicedOrders(pageInfo, roleType, status,
                new OnPageModeListener<ScOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewError(errorMsg);
                        }
                    }
                });
    }
    /**
     *
     * */
    public void findCancelOrders(PageInfo pageInfo, int roleType){
        mScOrderMode.findCancelOrders(pageInfo, roleType,
                new OnPageModeListener<ScOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScOrderView != null) {
                            mIScOrderView.onIScOrderViewError(errorMsg);
                        }
                    }
                });
    }
}

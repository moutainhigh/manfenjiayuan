package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.mode.GroupBuyOrderMode;
import com.manfenjiayuan.business.view.IGroupBuyOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.rxapi.bean.GroupBuyActivity;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;

import java.util.List;


/**
 * 团购活动
 * Created by bingshanguxue on 9/22/16.
 */

public class GroupBuyOrderPresenter {
    private IGroupBuyOrderView mIGroupBuyOrderView;
    private GroupBuyOrderMode mGroupBuyOrderMode;

    public GroupBuyOrderPresenter(IGroupBuyOrderView iGroupBuyOrderView) {
        this.mIGroupBuyOrderView = iGroupBuyOrderView;
        this.mGroupBuyOrderMode = new GroupBuyOrderMode();
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     * */
    public void  queryNetBeans(PageInfo pageInfo){
        mGroupBuyOrderMode.queryNetBeans(pageInfo,
                new OnPageModeListener<GroupBuyActivity>() {
                    @Override
                    public void onProcess() {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<GroupBuyActivity> dataList) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onQueryGroupBuyActitiySuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     * */
    public void queryNetOrder(Long activityId, PageInfo pageInfo){
        mGroupBuyOrderMode.queryNetOrder(activityId, pageInfo,
                new OnPageModeListener<GroupBuyOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<GroupBuyOrder> dataList) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onQueryGroupBuyOrderSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 门店人员通知单个订单客户提货，其中id是单个订单编号
     */
    public void notifyHumanTakeGood(Long id){
        mGroupBuyOrderMode.notifyHumanTakeGood(id,
                new OnModeListener<String>() {
                    @Override
                    public void onProcess() {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(String data) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onNotifyTakeGoodsSuccess(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 门店人员通知团购活动的所有订单客户提货，其中id是团购活动编号
     */
    public void notifyTakeGoods(Long id){
        mGroupBuyOrderMode.notifyTakeGoods(id,
                new OnModeListener<String>() {
                    @Override
                    public void onProcess() {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(String data) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onNotifyTakeGoodsSuccess(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 门店人员通知单个订单客户提货，其中id是单个订单编号
     */
    public void receiveAndFinishOrder(Long id){
        mGroupBuyOrderMode.receiveAndFinishOrder(id,
                new OnModeListener<String>() {
                    @Override
                    public void onProcess() {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(String data) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onNotifyReceiveAndFinishOrderSuccess(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     * */
    public void  queryHumanOrder(Long humanId, PageInfo pageInfo){
        mGroupBuyOrderMode.queryHumanOrder(humanId, pageInfo,
                new OnPageModeListener<GroupBuyOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<GroupBuyOrder> dataList) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onQueryGroupBuyOrderSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIGroupBuyOrderView != null) {
                            mIGroupBuyOrderView.onIGroupBuyOrderViewError(errorMsg);
                        }
                    }
                });
    }

}

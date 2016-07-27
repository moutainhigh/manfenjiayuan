package com.mfh.framework.api.invSendIoOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendIoOrderPresenter {
    private IInvSendIoOrderView mIInvSendIoOrderView;
    private InvSendIoOrderMode mInvSendIoOrderMode;
//    private InvSendOrderItemMode invSendOrderItemMode;

    public InvSendIoOrderPresenter(IInvSendIoOrderView invRecvOrderView) {
        this.mIInvSendIoOrderView = invRecvOrderView;

        this.mInvSendIoOrderMode = new InvSendIoOrderMode();
//        this.invSendOrderItemMode = new InvSendOrderItemMode();
    }

    /**
     * 加载采购订单
     * @param netFlag
     * */
    public void loadTransOrders(PageInfo pageInfo, boolean netFlag){
        mInvSendIoOrderMode.loadTransOrders(pageInfo, netFlag, new OnPageModeListener<InvSendIoOrder>() {
            @Override
            public void onProcess() {
                if (mIInvSendIoOrderView != null) {
                    mIInvSendIoOrderView.onQueryOrderProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {
                if (mIInvSendIoOrderView != null) {
                    mIInvSendIoOrderView.onQueryOrderSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIInvSendIoOrderView != null) {
                    mIInvSendIoOrderView.onQueryOrderError(errorMsg);
                }
            }
        });
    }

    /**
     * 加载采购订单
     * @param status 订单状态
     * */
    public void loadReturnOrders(PageInfo pageInfo, String status, String payStatus, String sendTenantId){
        mInvSendIoOrderMode.loadReturnOrders(pageInfo, status, payStatus, sendTenantId,
                new OnPageModeListener<InvSendIoOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIInvSendIoOrderView != null) {
                            mIInvSendIoOrderView.onQueryOrderProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {
                        if (mIInvSendIoOrderView != null) {
                            mIInvSendIoOrderView.onQueryOrderSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIInvSendIoOrderView != null) {
                            mIInvSendIoOrderView.onQueryOrderError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 加载采购订单
     * @param status 订单状态
     * */
    public void loadOrders(PageInfo pageInfo, String status, String payStatus, String sendTenantId){
        mInvSendIoOrderMode.loadRecvOrders(pageInfo, status, payStatus, sendTenantId, new OnPageModeListener<InvSendIoOrder>() {
            @Override
            public void onProcess() {
                if (mIInvSendIoOrderView != null) {
                    mIInvSendIoOrderView.onQueryOrderProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {
                if (mIInvSendIoOrderView != null) {
                    mIInvSendIoOrderView.onQueryOrderSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIInvSendIoOrderView != null) {
                    mIInvSendIoOrderView.onQueryOrderError(errorMsg);
                }
            }
        });
    }

    public void loadOrderItems(Long orderId) {
//        invSendOrderItemMode.loadOrderItems(orderId, new OnModeListener<InvSendIoOrderItem>() {
//            @Override
//            public void onProcess() {
//
//                if (invRecvOrderView != null) {
//                    invRecvOrderView.onQueryOrderProcess();
//                }
//            }
//
//            @Override
//            public void onSuccess(PageInfo pageInfo, List<InvSendOrderItem> dataList) {
//
//                if (invRecvOrderView != null) {
//                    invRecvOrderView.onQueryOrderItemsSuccess(dataList);
//                }
//            }
//
//            @Override
//            public void onError(String errorMsg) {
//
//                if (invRecvOrderView != null) {
//                    invRecvOrderView.onQueryOrderError(errorMsg);
//                }
//            }
//        });
    }

}

package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.bean.InvSendOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendOrderItem;
import com.manfenjiayuan.business.mode.InvSendOrderItemMode;
import com.manfenjiayuan.business.mode.InvSendOrderMode;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendOrderPresenter {
    private IInvSendOrderView invSendOrderView;
    private InvSendOrderMode invSendOrderMode;
    private InvSendOrderItemMode invSendOrderItemMode;

    public InvSendOrderPresenter(IInvSendOrderView invSendOrderView) {
        this.invSendOrderView = invSendOrderView;

        this.invSendOrderMode = new InvSendOrderMode();
        this.invSendOrderItemMode = new InvSendOrderItemMode();
    }

    /**
     * 加载采购订单
     * @param netFlag 类目编号
     * */
    public void loadOrders(PageInfo pageInfo, boolean netFlag, Long netId, String sendTenantId, String status){
        invSendOrderMode.loadOrders(pageInfo, netFlag, netId, sendTenantId, status, new OnPageModeListener<InvSendOrder>() {
            @Override
            public void onProcess() {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewError(errorMsg);
                }
            }
        });
    }

    /**
     * 适用场景：门店加载采购订单&客户生鲜预定订单
     * @param
     * */
    public void listInvSendOrders2(PageInfo pageInfo, Long receiveNetId, String sendType, String status){
        invSendOrderMode.listInvSendOrders2(pageInfo, receiveNetId, sendType, status, new OnPageModeListener<InvSendOrder>() {
            @Override
            public void onProcess() {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewError(errorMsg);
                }
            }
        });
    }
    public void listInvSendOrders2(PageInfo pageInfo, Long receiveNetId, String sendType,
                                   String status, String receiveMobile){
        invSendOrderMode.listInvSendOrders2(pageInfo, receiveNetId, sendType, status, receiveMobile,
                new OnPageModeListener<InvSendOrder>() {
            @Override
            public void onProcess() {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewError(errorMsg);
                }
            }
        });
    }

    /**
     * 加载采购订单明细
     * @param orderId 订单编号
     * */
    public void loadOrderItems(Long orderId) {
        invSendOrderItemMode.loadOrderItems(orderId, new OnPageModeListener<InvSendOrderItem>() {
            @Override
            public void onProcess() {

                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendOrderItem> dataList) {

                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewItemsSuccess(dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {

                if (invSendOrderView != null) {
                    invSendOrderView.onIInvSendOrderViewError(errorMsg);
                }
            }
        });
    }



}

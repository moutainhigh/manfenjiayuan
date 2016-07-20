package com.mfh.litecashier.presenter;

import com.manfenjiayuan.business.bean.InvSendIoOrder;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.litecashier.mode.InvRecvOrderMode;
import com.mfh.litecashier.ui.view.IInvRecvOrderView;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvRecvOrderPresenter {
    private IInvRecvOrderView invRecvOrderView;
    private InvRecvOrderMode invRecvOrderMode;
//    private InvSendOrderItemMode invSendOrderItemMode;

    public InvRecvOrderPresenter(IInvRecvOrderView invRecvOrderView) {
        this.invRecvOrderView = invRecvOrderView;

        this.invRecvOrderMode = new InvRecvOrderMode();
//        this.invSendOrderItemMode = new InvSendOrderItemMode();
    }

    /**
     * 加载采购订单
     * @param frontCategoryId 类目编号
     * */
    public void loadOrders(PageInfo pageInfo, String status, String payStatus, String sendTenantId){
        invRecvOrderMode.loadOrders(pageInfo, status, payStatus, sendTenantId, new OnPageModeListener<InvSendIoOrder>() {
            @Override
            public void onProcess() {
                if (invRecvOrderView != null) {
                    invRecvOrderView.onQueryOrderProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {
                if (invRecvOrderView != null) {
                    invRecvOrderView.onQueryOrderSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (invRecvOrderView != null) {
                    invRecvOrderView.onQueryOrderError(errorMsg);
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

package com.mfh.litecashier.presenter;

import com.manfenjiayuan.business.bean.InvSendIoOrder;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.litecashier.mode.InvReturnOrderMode;
import com.mfh.litecashier.ui.view.IInvReturnOrderView;

import java.util.List;

/**
 * 采购退货订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvReturnOrderPresenter {
    private IInvReturnOrderView invReturnOrderView;
    private InvReturnOrderMode invReturnOrderMode;
//    private InvSendOrderItemMode invSendOrderItemMode;

    public InvReturnOrderPresenter(IInvReturnOrderView invReturnOrderView) {
        this.invReturnOrderView = invReturnOrderView;

        this.invReturnOrderMode = new InvReturnOrderMode();
//        this.invSendOrderItemMode = new InvSendOrderItemMode();
    }

    /**
     * 加载采购订单
     * @param frontCategoryId 类目编号
     * */
    public void loadOrders(PageInfo pageInfo, String status, String payStatus, String sendTenantId){
        invReturnOrderMode.loadOrders(pageInfo, status, payStatus, sendTenantId,
                new OnPageModeListener<InvSendIoOrder>() {
            @Override
            public void onProcess() {
                if (invReturnOrderView != null) {
                    invReturnOrderView.onQueryOrderProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {
                if (invReturnOrderView != null) {
                    invReturnOrderView.onQueryOrderSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (invReturnOrderView != null) {
                    invReturnOrderView.onQueryOrderError(errorMsg);
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

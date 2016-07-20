package com.mfh.litecashier.presenter;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.litecashier.bean.InvTransOrder;
import com.mfh.litecashier.mode.InvTransOrderMode;
import com.mfh.litecashier.ui.view.IInvTransOrderView;

import java.util.List;

/**
 * 库存调拨订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvTransOrderPresenter {
    private IInvTransOrderView invTransOrderView;
    private InvTransOrderMode invTransOrderMode;
//    private InvSendOrderItemMode invSendOrderItemMode;

    public InvTransOrderPresenter(IInvTransOrderView invTransOrderView) {
        this.invTransOrderView = invTransOrderView;

        this.invTransOrderMode = new InvTransOrderMode();
//        this.invSendOrderItemMode = new InvSendOrderItemMode();
    }

    /**
     * 加载采购订单
     * @param frontCategoryId 类目编号
     * */
    public void loadOrders(PageInfo pageInfo, boolean netFlag){
        invTransOrderMode.loadOrders(pageInfo, netFlag, new OnPageModeListener<InvTransOrder>() {
            @Override
            public void onProcess() {
                if (invTransOrderView != null) {
                    invTransOrderView.onQueryOrderProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvTransOrder> dataList) {
                if (invTransOrderView != null) {
                    invTransOrderView.onQueryOrderSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (invTransOrderView != null) {
                    invTransOrderView.onQueryOrderError(errorMsg);
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

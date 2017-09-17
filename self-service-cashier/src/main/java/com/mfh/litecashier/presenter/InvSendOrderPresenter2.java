package com.mfh.litecashier.presenter;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpBasePresenter;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.api.invSendOrder.InvSendOrderItemMode;
import com.mfh.framework.api.invSendOrder.InvSendOrderMode;
import com.manfenjiayuan.business.mvp.view.IInvSendOrderView;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendOrderPresenter2 extends MvpBasePresenter<IInvSendOrderView> {
    private InvSendOrderMode invSendOrderMode = new InvSendOrderMode();
    private InvSendOrderItemMode invSendOrderItemMode = new InvSendOrderItemMode();

    /**
     * 加载采购订单
     * @param  netId
     * */
    public void loadOrders(PageInfo pageInfo, boolean netFlag, Long netId,
                           String sendTenantId, String status){
        invSendOrderMode.loadOrders(pageInfo, netFlag, netId, sendTenantId,
                status, new OnPageModeListener<InvSendOrder>() {
            @Override
            public void onProcess() {
                if (getView() != null) {
                    getView().onIInvSendOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
                if (getView() != null) {
                    getView().onIInvSendOrderViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (getView() != null) {
                    getView().onIInvSendOrderViewError(errorMsg);
                }
            }
        });
    }

    public void loadOrderItems(Long orderId) {
        invSendOrderItemMode.loadOrderItems(orderId, new OnPageModeListener<InvSendOrderItem>() {
            @Override
            public void onProcess() {

                if (getView() != null) {
                    getView().onIInvSendOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSendOrderItem> dataList) {

                if (getView() != null) {
                    getView().onIInvSendOrderViewItemsSuccess(dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {

                if (getView() != null) {
                    getView().onIInvSendOrderViewError(errorMsg);
                }
            }
        });
    }

}

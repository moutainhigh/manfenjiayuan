package com.manfenjiayuan.pda_supermarket.presenter;

import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.manfenjiayuan.pda_supermarket.mode.InvSendIoOrderItemMode;
import com.manfenjiayuan.pda_supermarket.ui.IInvSendIoOrderView;
import com.mfh.framework.mvp.OnModeListener;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendIoOrderPresenter {
    private IInvSendIoOrderView invSendOrderView;
    private InvSendIoOrderItemMode invSendOrderItemMode;

    public InvSendIoOrderPresenter(IInvSendIoOrderView invSendOrderView) {
        this.invSendOrderView = invSendOrderView;
        this.invSendOrderItemMode = new InvSendIoOrderItemMode();
    }

    public void loadOrderItemsByBarcode(String barcode) {
        invSendOrderItemMode.loadOrderItemsByBarcode(barcode, new OnModeListener<InvSendIoOrderItemBrief>() {
            @Override
            public void onProcess() {

                if (invSendOrderView != null) {
                    invSendOrderView.onQueryInvSendIoOrderProcess();
                }
            }

            @Override
            public void onSuccess(InvSendIoOrderItemBrief data) {

                if (invSendOrderView != null) {
                    invSendOrderView.onQueryInvSendIoOrderSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {

                if (invSendOrderView != null) {
                    invSendOrderView.onQueryInvSendIoOrderError(errorMsg);
                }
            }
        });
    }


}

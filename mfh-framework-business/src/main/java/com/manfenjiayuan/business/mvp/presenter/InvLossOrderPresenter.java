package com.manfenjiayuan.business.mvp.presenter;

import com.manfenjiayuan.business.mvp.view.IInvLossOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.mfh.framework.api.invLossOrder.InvLossOrderMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;


/**
 * Created by bingshanguxue on 9/22/16.
 */

public class InvLossOrderPresenter {
    private IInvLossOrderView mIInvCheckOrderView;
    private InvLossOrderMode mInvCheckOrderMode;

    public InvLossOrderPresenter(IInvLossOrderView iInvCheckOrderView) {
        this.mIInvCheckOrderView = iInvCheckOrderView;
        this.mInvCheckOrderMode = new InvLossOrderMode();
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     * */
    public void list(PageInfo pageInfo){
        mInvCheckOrderMode.list(pageInfo,
                new OnPageModeListener<InvLossOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIInvCheckOrderView != null) {
                            mIInvCheckOrderView.onIInvLossOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<InvLossOrder> dataList) {
                        if (mIInvCheckOrderView != null) {
                            mIInvCheckOrderView.onIInvLossOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIInvCheckOrderView != null) {
                            mIInvCheckOrderView.onIInvLossOrderViewError(errorMsg);
                        }
                    }
                });
    }

}

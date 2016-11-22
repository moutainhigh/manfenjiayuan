package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.view.IInvCheckOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.invCheckOrder.InvCheckOrder;
import com.mfh.framework.api.invCheckOrder.InvCheckOrderMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;


/**
 * Created by bingshanguxue on 9/22/16.
 */

public class InvCheckOrderPresenter {
    private IInvCheckOrderView mIInvCheckOrderView;
    private InvCheckOrderMode mInvCheckOrderMode;

    public InvCheckOrderPresenter(IInvCheckOrderView iInvCheckOrderView) {
        this.mIInvCheckOrderView = iInvCheckOrderView;
        this.mInvCheckOrderMode = new InvCheckOrderMode();
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     * */
    public void list(PageInfo pageInfo){
        mInvCheckOrderMode.list(pageInfo,
                new OnPageModeListener<InvCheckOrder>() {
                    @Override
                    public void onProcess() {
                        if (mIInvCheckOrderView != null) {
                            mIInvCheckOrderView.onIInvCheckOrderViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<InvCheckOrder> dataList) {
                        if (mIInvCheckOrderView != null) {
                            mIInvCheckOrderView.onIInvCheckOrderViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIInvCheckOrderView != null) {
                            mIInvCheckOrderView.onIInvCheckOrderViewError(errorMsg);
                        }
                    }
                });
    }

}

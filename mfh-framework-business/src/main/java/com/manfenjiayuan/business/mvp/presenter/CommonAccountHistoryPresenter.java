package com.manfenjiayuan.business.mvp.presenter;

import com.manfenjiayuan.business.mvp.mode.CommonAccountHistoryMode;
import com.manfenjiayuan.business.mvp.view.ICommonAccountHistoryView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.rxapi.bean.CommonAccountFlow;

import java.util.List;


/**
 * 团购活动
 * Created by bingshanguxue on 9/22/16.
 */

public class CommonAccountHistoryPresenter {
    private ICommonAccountHistoryView mICommonAccountHistoryView;
    private CommonAccountHistoryMode mCommonAccountHistoryMode;

    public CommonAccountHistoryPresenter(ICommonAccountHistoryView iCommonAccountHistoryView) {
        this.mICommonAccountHistoryView = iCommonAccountHistoryView;
        this.mCommonAccountHistoryMode = new CommonAccountHistoryMode();
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表
     * */
    public void  queryCustomerFlow(Long humanId, PageInfo pageInfo){
        mCommonAccountHistoryMode.queryCustomerFlow(humanId, pageInfo,
                new OnPageModeListener<CommonAccountFlow>() {
                    @Override
                    public void onProcess() {
                        if (mICommonAccountHistoryView != null) {
                            mICommonAccountHistoryView.onICommonAccountHistoryProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<CommonAccountFlow> dataList) {
                        if (mICommonAccountHistoryView != null) {
                            mICommonAccountHistoryView.onICommonAccountHistorySuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mICommonAccountHistoryView != null) {
                            mICommonAccountHistoryView.onICommonAccountHistoryError(errorMsg);
                        }
                    }
                });
    }


}

package com.bingshanguxue.almigod.pos;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.posRegister.PosRegister;
import com.mfh.framework.api.posRegister.PosRegisterMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

public class PosRegisterPresenter {
    private IPosRegisterView mIPosRegisterView;
    private PosRegisterMode mPosRegisterMode;

    public PosRegisterPresenter(IPosRegisterView IPosRegisterView) {
        this.mIPosRegisterView = IPosRegisterView;
        this.mPosRegisterMode = new PosRegisterMode();
    }

    /**
     * 获取门店
     */
    public void list(PageInfo pageInfo) {
        mPosRegisterMode.list(pageInfo,
                new OnPageModeListener<PosRegister>() {
            @Override
            public void onProcess() {
                if (mIPosRegisterView != null) {
                    mIPosRegisterView.onIPosRegisterViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<PosRegister> dataList) {
                if (mIPosRegisterView != null) {
                    mIPosRegisterView.onIPosRegisterViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIPosRegisterView != null) {
                    mIPosRegisterView.onIPosRegisterViewError(errorMsg);
                }
            }
        });

    }
}
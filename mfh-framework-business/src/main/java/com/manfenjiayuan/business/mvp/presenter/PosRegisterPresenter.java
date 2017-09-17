package com.manfenjiayuan.business.mvp.presenter;

import com.manfenjiayuan.business.mvp.view.IPosRegisterView;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.posRegister.PosRegisterMode;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;

/**
 * 注册设备
 * Created by bingshanguxue on 16/3/17.
 */
public class PosRegisterPresenter {
    private IPosRegisterView mPosRegisterView;
    private PosRegisterMode mPosRegisterMode;

    public PosRegisterPresenter(IPosRegisterView mPosRegisterView) {
        this.mPosRegisterView = mPosRegisterView;
        this.mPosRegisterMode = new PosRegisterMode();
    }

    /**
     * 注册设备
     *
     */
    public void create() {
        mPosRegisterMode.create(MfhApi.CHANNEL_ID, IMConfig.getPushClientId(),
                MfhLoginService.get().getCurOfficeId(),
                new OnModeListener<String>() {
                    @Override
                    public void onProcess() {
                        if (mPosRegisterView != null) {
                            mPosRegisterView.onRegisterPlatProcess();
                        }
                    }

                    @Override
                    public void onSuccess(String data) {
                        if (mPosRegisterView != null) {
                            mPosRegisterView.onRegisterPlatSuccess(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mPosRegisterView != null) {
                            mPosRegisterView.onRegisterPlatError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 更新设备注册信息
     *
     */
    public void update(String terminalId) {
        mPosRegisterMode.update(terminalId, MfhApi.CHANNEL_ID, IMConfig.getPushClientId(),
                MfhLoginService.get().getCurOfficeId(),
                new OnModeListener<String>() {
                    @Override
                    public void onProcess() {
                        if (mPosRegisterView != null) {
                            mPosRegisterView.onPlatUpdate();
                        }
                    }

                    @Override
                    public void onSuccess(String data) {
                        if (mPosRegisterView != null) {
                            mPosRegisterView.onRegisterPlatSuccess(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mPosRegisterView != null) {
                            mPosRegisterView.onRegisterPlatError(errorMsg);
                        }
                    }
                });
    }

    public void register(boolean coverEnabled) {
        String teminalId = SharedPrefesManagerFactory.getTerminalId();
        if (StringUtils.isEmpty(teminalId) || coverEnabled){
            create();
        }
        else{
            update(teminalId);
        }
    }

}

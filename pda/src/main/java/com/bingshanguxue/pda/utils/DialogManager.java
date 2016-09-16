package com.bingshanguxue.pda.utils;

import android.app.Activity;

/**
 * Created by bingshanguxue on 8/23/16.
 */
public class DialogManager {
    private PosRegisterDialog mPosRegisterDialog = null;

    private static DialogManager instance = null;

    /**
     * 返回 DialogManager 实例
     *
     * @return DialogManager
     */
    public static DialogManager getInstance() {
        if (instance == null) {
            synchronized (DialogManager.class) {
                if (instance == null) {
                    instance = new DialogManager();
                }
            }
        }
        return instance;
    }

    public void reset() {
        if (mPosRegisterDialog != null) {
            mPosRegisterDialog.dismiss();
            mPosRegisterDialog = null;
        }
    }

    /**
     * 注册设备
     */
    public void registerPos(Activity context) {
        if (context == null) {
            return;
        }

        if (mPosRegisterDialog == null || !mPosRegisterDialog.getContext().equals(context)) {
            mPosRegisterDialog = new PosRegisterDialog(context);
            mPosRegisterDialog.setCancelable(false);
            mPosRegisterDialog.setCanceledOnTouchOutside(false);
        }

        mPosRegisterDialog.init("注册设备", false, new PosRegisterDialog.DialogClickListener() {
                    @Override
                    public void onProcess() {

                    }

                    @Override
                    public void onSuccess() {
                        // TODO: 8/23/16 在这里通知注册成功 
//                        if (BizConfig.RELEASE) {
//                            ValidateManager.get().stepValidate(ValidateManager.STEP_REGISTER_PLAT);
//                        }
                    }

                    @Override
                    public void onError() {

                    }

                    @Override
                    public void onCancel() {

                    }
                });

        if (!mPosRegisterDialog.isShowing()) {
            mPosRegisterDialog.show();
        }
    }

}

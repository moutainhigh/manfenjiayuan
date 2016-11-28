package com.mfh.litecashier.utils;

import android.app.Activity;

import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.mfh.litecashier.ui.dialog.PosRegisterDialog;

/**
 * Created by bingshanguxue on 15/9/9.
 */
public class GlobalInstance {

    private Double netWeight;//净重(单位kg)
    private PosRegisterDialog mPosRegisterDialog = null;
    private CashierOrderInfo mCashierOrderInfo;

    private static GlobalInstance instance;

    public static GlobalInstance getInstance() {
        if (instance == null) {
            synchronized (GlobalInstance.class) {
                if (instance == null) {
                    instance = new GlobalInstance();
                }
            }
        }
        return instance;
    }

    private void init() {
        netWeight = 0D;
    }


    public void reset(){
        netWeight = 0D;
    }

    public synchronized Double getNetWeight() {
        if (netWeight == null) {
            netWeight = 0D;
        }
        return netWeight;
    }

    public synchronized void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }


    public CashierOrderInfo getCashierOrderInfo() {
        return mCashierOrderInfo;
    }

    public void setCashierOrderInfo(CashierOrderInfo cashierOrderInfo) {
        mCashierOrderInfo = cashierOrderInfo;
    }

    /**
     * 注册设备
     */
    public void registerPos(Activity context) {
        if (context == null) {
            return;
        }

        if (mPosRegisterDialog == null ||
                !mPosRegisterDialog.getContext().equals(context)) {
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

    /**
     * 取消注册设备
     * */
    public void cancelRegisterPos(){
        if (mPosRegisterDialog != null) {
            mPosRegisterDialog.dismiss();
            mPosRegisterDialog = null;
        }
    }

}

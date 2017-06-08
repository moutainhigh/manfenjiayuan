package com.mfh.litecashier.utils;

import android.app.Activity;

import com.bingshanguxue.cashier.hardware.scale.ScaleProvider;
import com.mfh.litecashier.ui.dialog.PosRegisterDialog;

/**
 * Created by bingshanguxue on 15/9/9.
 */
public class GlobalInstance {

    private Double netWeight;//净重(单位kg)
    private int stableCount = 0;//稳定发送次数

    private PosRegisterDialog mPosRegisterDialog = null;

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
        stableCount = 0;
    }

    public synchronized Double getNetWeight() {
        Double temp = 0D;
        if (ScaleProvider.getScaleType() == ScaleProvider.SCALE_TYPE_DS_781A) {
            if (stableCount >= 2) {
                temp = netWeight;
            }
        } else {
            temp = netWeight;
        }

        //取出重量后自动重置
//        reset();

        return temp != null ? temp : 0D;
    }

    public synchronized void setNetWeight(Double netWeight) {
        //由于收到一些不完整的数据或者不是期望的格式数据会导致重量信息为null,
        // 由于串口的不稳定性，如果直接将次数清零，可能会导致永远不会收到3次稳定的数据，所以暂时注视掉。
        //以后稳定后再说。
        if (netWeight == null) {
//            stableCount = 0;
            return;
        } else if (this.netWeight != null && this.netWeight.compareTo(netWeight) == 0){
            stableCount += 1;
        } else {
            stableCount = 1;
        }
        this.netWeight = netWeight;
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

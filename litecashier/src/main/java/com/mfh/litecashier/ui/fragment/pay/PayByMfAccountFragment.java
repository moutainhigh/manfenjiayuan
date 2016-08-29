package com.mfh.litecashier.ui.fragment.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.database.entity.PosOrderPayEntity;
import com.mfh.litecashier.event.MfPayEvent;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.io.Serializable;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByMfAccountFragment extends BasePayFragment {

    @Bind(R.id.inlv_paidmoney)
    InputNumberLabelView inlvPaidMoney;
    @Bind(R.id.et_barCode)
    EditText etBarCode;

    private Human mHuman = null;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_mfaccount;
    }

    @Override
    protected int getPayType() {
        return WayType.MFACCOUNT;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

//        //for Fragment.instantiate
//        Bundle args = getArguments();
//        if (args != null) {
//            orderId = args.getString(EXTRA_KEY_ORDER_ID, "");
//            bizType = args.getString(EXTRA_KEY_BIZ_TYPE, "");
//        }

        initPaidMontyInput();
        initBarCodeInput();
    }

    @Override
    public void onResume() {
        super.onResume();

        etBarCode.requestFocus();

        //TODO,主动去请求当前价格
//        EventBus.getDefault().post(new MfPayEvent(CashierConstants.PAYTYPE_MFACCOUNT, MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initPaidMontyInput() {
        inlvPaidMoney.setEnterKeySubmitEnabled(true);
        inlvPaidMoney.setSoftKeyboardEnabled(false);
        inlvPaidMoney.setDigits(2);
        inlvPaidMoney.requestFocus();
        inlvPaidMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateCharge();
            }
        });
        inlvPaidMoney.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        etBarCode.requestFocus();
                    }

                    return true;
                }
                //Press “＋”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_ADD) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //输入满分账户信息
                        EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_READ_MFACCOUNT));
                    }
                    return true;
                }


                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }


    private void initBarCodeInput() {
        etBarCode.setHint("请输入支付密码");
        etBarCode.setFocusable(true);
        etBarCode.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        etBarCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etBarCode);
                }
                etBarCode.requestFocus();
                etBarCode.setSelection(etBarCode.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etBarCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submitOrder();

                        etBarCode.requestFocus();
                        etBarCode.setSelection(etBarCode.length());
                    }
                    return true;
                }
                //Press “＋”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_ADD) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //输入满分账户信息
                        EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_READ_MFACCOUNT));
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    /**
     * 注册监听器
     */
    @Override
    protected void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED_MFACCOUNT);
        intentFilter.addAction(Constants.BA_SEND_PAY_REQ_MFACCOUNT);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZLogger.d("PayByMfCardFragment.onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_MFACCOUNT)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        mHuman = (Human)extras.getSerializable(EXTRA_KEY_MEMBERINFO);
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        inlvPaidMoney.setInputString(MUtils.formatDouble(handleAmount, ""));
                        inlvPaidMoney.setEnabled(SharedPreferencesHelper
                                .getBoolean(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, false));
                        etBarCode.setText("");
                        etBarCode.requestFocus();
                        calculateCharge();
                    }
                } else if (intent.getAction().equals(Constants.BA_SEND_PAY_REQ_MFACCOUNT)) {
                    ZLogger.d("解析会员信息");
                    Bundle extras = intent.getExtras();
                    if (extras != null){
                        Serializable serializable = extras.getSerializable(EXTRA_KEY_MEMBERINFO);
                        if (serializable != null){
                            mHuman = (Human)serializable;
                            ZLogger.d("已经检测到会员信息");
                        }
                        else{
                            mHuman = null;
                            ZLogger.d("未检测到会员信息");
                        }
                    }
                    else{
                        mHuman = null;
                        ZLogger.d("未检测到会员信息");
                    }

                    submitOrder();
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void calculatePaidAmount() {
        try {
            String cpaidMoneyStr = inlvPaidMoney.getInputString();
            if (!StringUtils.isEmpty(cpaidMoneyStr)) {
                paidAmount = Double.valueOf(cpaidMoneyStr);
            } else {
                paidAmount = 0D;
            }
        } catch (Exception e) {
//                        java.lang.NumberFormatException: Invalid double: "88.0.08"
            ZLogger.e(e.toString());
        }
    }

    /**
     * 提交订单
     */
    @Override
    protected void submitOrder() {
        if (bPayProcessing) {
            ZLogger.df("正在进行账户支付，不用重复发起请求");
            return;
        }

        if (mHuman == null) {
            bPayProcessing = false;
            etBarCode.getText().clear();
            //输入满分账户信息
            EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_READ_MFACCOUNT));
            return;
        }

        String paidMoney = inlvPaidMoney.getInputString();
        if (StringUtils.isEmpty(paidMoney)) {
            bPayProcessing = false;
            DialogUtil.showHint("请输入支付金额");
            return;
        }

        String password = etBarCode.getText().toString();
        if (StringUtils.isEmpty(password)) {
            DialogUtil.showHint("请输入支付密码");
            bPayProcessing = false;
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            bPayProcessing = false;
            return;
        }

        bPayProcessing = true;
        calculatePaidAmount();
        super.submitOrder();

        //支付订单
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在处理订单...", false);

        Bundle args = new Bundle();
        args.putString(MfPayEvent.KEY_OUTTRADENO, outTradeNo);
        args.putInt(MfPayEvent.KEY_PAY_TYPE, payType);
        args.putDouble(MfPayEvent.KEY_AMOUNT, paidAmount);
        args.putInt(MfPayEvent.KEY_PAY_STATUS, PosOrderPayEntity.PAY_STATUS_PROCESS);
        EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_PAY_PROCESSING, args));

        CashierApiImpl.accountPayByPOS(mHuman.getGuid(), password,
                MUtils.formatDouble(paidAmount, ""), bizType, orderId, payRespCallback);
    }

    NetCallBack.NetTaskCallBack payRespCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"操作成功!","version":"1","data":""}
//                        com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    String retStr = retValue.getValue();

                    ZLogger.df(String.format("满分账户支付成功:%s", retStr));
                    bPayProcessing = false;
                    hideProgressDialog();
                    Bundle args = new Bundle();
                    args.putString(MfPayEvent.KEY_OUTTRADENO, outTradeNo);
                    args.putInt(MfPayEvent.KEY_PAY_TYPE, payType);
                    args.putDouble(MfPayEvent.KEY_AMOUNT, paidAmount);
                    args.putInt(MfPayEvent.KEY_PAY_STATUS, PosOrderPayEntity.PAY_STATUS_FINISH);
                    EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_PAY_SUCCEE, args));
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知
                    ZLogger.df("满分账户支付异常:" + errMsg);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    bPayProcessing = false;
//                    hideProgressDialog();
                    Bundle args = new Bundle();
                    args.putString(MfPayEvent.KEY_OUTTRADENO, outTradeNo);
                    args.putInt(MfPayEvent.KEY_PAY_TYPE, payType);
                    args.putDouble(MfPayEvent.KEY_AMOUNT, paidAmount);
                    args.putInt(MfPayEvent.KEY_PAY_STATUS, PosOrderPayEntity.PAY_STATUS_FAILED);
                    EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_PAY_FAILED, args));
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

}

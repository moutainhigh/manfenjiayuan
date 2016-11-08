package com.mfh.litecashier.ui.fragment.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.bingshanguxue.cashier.v1.PaymentInfo;
import com.bingshanguxue.cashier.v1.PaymentInfoImpl;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.FontFitTextView;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 支付--现金支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class PayByCashFragment extends BasePayFragment {
    @Bind(R.id.inlv_paidmoney)
    InputNumberLabelView inlvPaidMoney;
    @Bind(R.id.tv_charge)
    FontFitTextView tvCharge;

    private NumberInputDialog barcodeInputDialog = null;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_cash;
    }

    @Override
    protected int getPayType() {
        return WayType.CASH;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        initPaidMoneyInput();
        onInitializeMode();
    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO,主动去请求当前价格
        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_WAYTYPE_UPDATED, null));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onInitializeMode() {
        super.onInitializeMode();
        if (inlvPaidMoney != null){
            inlvPaidMoney.clear();
            inlvPaidMoney.requestFocusEnd();
            inlvPaidMoney.setEnabled(true);
        }
    }

    @Override
    public void onActiveMode() {
        super.onActiveMode();
        inlvPaidMoney.requestFocusEnd();
        inlvPaidMoney.setEnabled(true);
    }

    @Override
    public void onDeactiveMode() {
        super.onDeactiveMode();
        inlvPaidMoney.setEnabled(false);
    }

    private void initPaidMoneyInput() {
        inlvPaidMoney.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    submitOrder();
                }
            }
        });
//        inlvPaidMoney.setSoftKeyboardEnabled(false);
        inlvPaidMoney.setDigits(2);
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
        inlvPaidMoney.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                inlvPaidMoney.requestFocusEnd();

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()
                            || inlvPaidMoney.isSoftKeyboardEnabled()) {
                        showBarcodeKeyboard();
                    }
                }

                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    @Override
    protected void registerReceiver() {
        super.registerReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZLogger.d("onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED)) {
                    Bundle extras = intent.getExtras();
                    ZLogger.d(StringUtils.decodeBundle(extras));
                    if (extras != null && extras.containsKey(EXTRA_KEY_HANDLE_AMOUNT)) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        onActiveMode();

                        calculateCharge();
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * 现金支付
     */
    @Override
    protected void submitOrder() {
        super.submitOrder();
        //混合支付aas
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, false)
                && rechargeAmount < 0) {
            ZLogger.df("收取金额不足");
//            DialogUtil.showHint("收取金额不足");
            return;
        }

        if (rechargeAmount.compareTo(100D) >= 0) {
            //找零超过100元，提示是否支付
            ZLogger.df("收取金额过大，请确认是否支付");
            showConfirmDialog(String.format("找零: %.2f, 确认支付吗？", rechargeAmount),
                    getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            onPaidSucceed();
                        }
                    }, getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ZLogger.df("取消现金支付");
                        }
                    });
        } else {
            onPaidSucceed();
        }
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

    @Override
    protected void calculateCharge() {
        super.calculateCharge();
        try {
            if (rechargeAmount >= 0) {
                tvCharge.setText(MUtils.formatDouble(rechargeAmount, ""));
                tvCharge.setTextColor(AppHelper.getOkTextColor());
            } else {
                //混合支付
                if (SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, false)) {
                    tvCharge.setText(R.string.default_amount);
                    tvCharge.setTextColor(Color.parseColor("#000000"));
                } else {
                    tvCharge.setText("金额不足");
                    tvCharge.setTextColor(AppHelper.getErrorTextColor());
                }
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 显示条码输入界面
     * 相当于扫描条码
     */
    private void showBarcodeKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getActivity());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeDecimalNumber(EditInputType.PRICE, "现金",
                inlvPaidMoney.getInputString(), 2, "元",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {

                    }

                    @Override
                    public void onNext(Double value) {
                        if (value != null){
                            inlvPaidMoney.setInputString(String.valueOf(value));

                            submitOrder();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }

                });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    private void onPaidSucceed(){
        PaymentInfo paymentInfo = PaymentInfoImpl.genPaymentInfo(outTradeNo, payType,
                PosOrderPayEntity.PAY_STATUS_FINISH,
                handleAmount, handleAmount, rechargeAmount);
        ZLogger.df(String.format("现金支付成功，收取金额：%.2f\n%s",
                paidAmount, JSON.toJSONString(paymentInfo)));

        //清空之前，先计算找零金额
        onInitializeMode();

        Bundle args = new Bundle();
        args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO,
                paymentInfo);
        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, args));
    }

}

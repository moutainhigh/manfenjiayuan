package com.mfh.litecashier.components.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.FontFitTextView;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.AppHelper;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;


/**
 * 支付--现金支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class PayByCashFragment extends BasePayFragment {
    @BindView(R.id.inlv_paidmoney)
    InputNumberLabelView inlvPaidMoney;
    @BindView(R.id.tv_charge)
    FontFitTextView tvCharge;

    private NumberInputDialog priceDialog = null;

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
//        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_WAYTYPE_UPDATED, null));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onInitializeMode() {
        super.onInitializeMode();
        if (inlvPaidMoney != null) {
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
                        KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            submitOrder();
                        }
                    }
                });
        inlvPaidMoney.registerOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onClickAction1(String text) {
                submitOrder();
            }

            @Override
            public void onLongClickAction1(String text) {

            }
        });
//        inlvPaidMoney.setSoftKeyboardEnabled(false);
//        inlvPaidMoney.setDigits(2);
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
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
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
                    if (extras != null) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT);
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
        if (rechargeAmount == null || rechargeAmount < 0) {
            ZLogger.d("收取金额不足,请重试！！！");
            DialogUtil.showHint("收取金额不足");
            return;
        }

        super.submitOrder();
        //找零超过100元，提示是否支付
        if (rechargeAmount.compareTo(100D) >= 0) {
            ZLogger.w("收取金额过大，请确认是否支付...");
            showConfirmDialog(String.format("找零: %.2f, 确认支付吗？", rechargeAmount),
                    getString(R.string.dialog_button_ok),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            ZLogger.d("继续支付");
                            onPaidSucceed();
                        }
                    },
                    getString(R.string.dialog_button_cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ZLogger.d("取消现金支付");
                            inlvPaidMoney.clear();
                            inlvPaidMoney.requestFocusEnd();

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
            if (rechargeAmount != null && rechargeAmount >= 0) {
                tvCharge.setText(MUtils.formatDouble(rechargeAmount, ""));
                tvCharge.setTextColor(AppHelper.getOkTextColor());
            } else {
                tvCharge.setText("金额不足");
                tvCharge.setTextColor(AppHelper.getErrorTextColor());
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
        if (priceDialog == null) {
            priceDialog = new NumberInputDialog(getActivity());
            priceDialog.setCancelable(true);
            priceDialog.setCanceledOnTouchOutside(true);
        }
        priceDialog.initializeDecimalNumber(EditInputType.PRICE, "现金",
                inlvPaidMoney.getInputString(), 2, "元",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {

                    }

                    @Override
                    public void onNext(Double value) {
                        if (value != null) {
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
        if (!priceDialog.isShowing()) {
            priceDialog.show();
        }
    }

    /**
     * 支付成功
     */
    private void onPaidSucceed() {
        PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, payType,
                PosOrderPayEntity.PAY_STATUS_FINISH,
                handleAmount, handleAmount, rechargeAmount, null);
        ZLogger.i(String.format("现金支付成功，收取金额：%.2f\n%s",
                paidAmount, JSON.toJSONString(paymentInfo)));

        //清空之前，先计算找零金额
        onInitializeMode();

        Bundle args = new Bundle();
        args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO,
                paymentInfo);
        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, args));
    }

}

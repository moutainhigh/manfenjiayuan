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

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.widget.FontFitTextView;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PosOrderPayEntity;
import com.mfh.litecashier.event.MfPayEvent;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
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
    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO,主动去请求当前价格
        EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initPaidMoneyInput() {
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
//                ZLogger.d("setOnKeyListener(PayByCashFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submitOrder();
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

    @Override
    protected void registerReceiver() {
        super.registerReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
        intentFilter.addAction(Constants.BA_SEND_PAY_REQ_CASH);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZLogger.d("PayByCashFragment.onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.containsKey(EXTRA_KEY_HANDLE_AMOUNT)) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        inlvPaidMoney.clear();

                        calculateCharge();
                    }
                } else if (intent.getAction().equals(Constants.BA_SEND_PAY_REQ_CASH)) {
//                    Bundle extras = intent.getExtras();
//                    if (extras != null && extras.containsKey(Constants.EXTRA_KEY_HANDLE_AMOUNT)) {
//                        handleAmount = extras.getDouble(Constants.EXTRA_KEY_HANDLE_AMOUNT, 0);
//                    }
                    submitOrder();
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
        //混合支付
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, false)
                && paidAmount.compareTo(handleAmount) < 0) {
//            DialogUtil.showHint("收取金额不足");
            return;
        }

        if (rechargeAmount.compareTo(MAX_RECHARGE_AMOUNT) >= 0) {
            //找零超过100元，提示是否支付
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
                tvCharge.setTextColor(Color.parseColor("#009B4E"));
            } else {
                //混合支付
                if (SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, false)) {
                    tvCharge.setText(R.string.default_amount);
                    tvCharge.setTextColor(Color.parseColor("#000000"));
                } else {
                    tvCharge.setText("金额不足");
                    tvCharge.setTextColor(Color.parseColor("#FE5000"));
                }
//            return cashVal - handleAmount;//有负数的时候
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }


    private void onPaidSucceed(){
        ZLogger.df(String.format("现金支付成功，找零：%.2f", rechargeAmount));
        Bundle args = new Bundle();
        args.putString(MfPayEvent.KEY_OUTTRADENO, outTradeNo);
        args.putInt(MfPayEvent.KEY_PAY_TYPE, payType);
        args.putDouble(MfPayEvent.KEY_AMOUNT, paidAmount);
        args.putInt(MfPayEvent.KEY_PAY_STATUS, PosOrderPayEntity.PAY_STATUS_FINISH);
        EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_PAY_SUCCEE, args));

        //清空之前，先计算找零金额
        inlvPaidMoney.clear();
    }

}

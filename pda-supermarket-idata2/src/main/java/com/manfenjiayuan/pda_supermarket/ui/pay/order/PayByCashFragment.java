package com.manfenjiayuan.pda_supermarket.ui.pay.order;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.vector_uikit.FontFitTextView;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.cashier.PaymentInfoImpl;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderPayEntity;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayStep1Event;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.StringUtils;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 支付--现金支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class PayByCashFragment extends BasePayFragment {
    @Bind(R.id.label_paidmoney)
    EditLabelView inlvPaidMoney;
    @Bind(R.id.tv_charge)
    FontFitTextView tvCharge;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_cash;
    }

    @Override
    protected void onScanCode(String code) {

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

        //TODO,主动去请求当前价格
        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_WAYTYPE_UPDATED, null));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onInitializeMode() {
        super.onInitializeMode();
        if (inlvPaidMoney != null) {
            inlvPaidMoney.setInput("");
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
        inlvPaidMoney.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            submitOrder();
                        }
                    }
                });
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
    }

    @Override
    protected void registerReceiver() {
        super.registerReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();
                ZLogger.d(String.format("onReceive.action=%s\n%s",
                        action, StringUtils.decodeBundle(extras)));
                if (StringUtils.isEmpty(action) || extras == null){
                    return;
                }

                if (action.equals(Constants.BA_HANDLE_AMOUNT_CHANGED)) {
                    if (extras.containsKey(EXTRA_KEY_HANDLE_AMOUNT)) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        inlvPaidMoney.setInput("");
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
        if (rechargeAmount < 0) {
            ZLogger.df("收取金额不足");
//            DialogUtil.showHint("收取金额不足");
            return;
        }

        if (rechargeAmount.compareTo(100D) >= 0) {
            //找零超过100元，提示是否支付
            ZLogger.df("收取金额过大，请确认是否支付");
            showConfirmDialog(String.format("找零: %.2f, 确认支付吗？", rechargeAmount),
                    getString(R.string.dialog_button_ok),
                    new DialogInterface.OnClickListener() {

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
            String cpaidMoneyStr = inlvPaidMoney.getInput();
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
                tvCharge.setText("金额不足");
                tvCharge.setTextColor(AppHelper.getErrorTextColor());
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }


    private void onPaidSucceed() {
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

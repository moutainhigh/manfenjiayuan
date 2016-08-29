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

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.database.entity.PosOrderPayEntity;
import com.mfh.litecashier.event.MfPayEvent;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.DataCacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 支付－－赊账
 * Created by NAT.ZZN(bingshanguxue) on 15/8/31.
 */
public class PayByCreditFragment extends BasePayFragment {

    @Bind(R.id.inlv_paidmoney)
    InputNumberLabelView inlvPaidMoney;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_credit;
    }

    @Override
    protected int getPayType() {
        return WayType.CREDIT;
    }

//    @Override
//    protected void handleIntent() {//for Fragment.instantiate
////        Bundle args = getArguments();
////        if (args != null) {
////            orderId = args.getString(EXTRA_KEY_ORDER_ID, "");
////            bizType = args.getString(EXTRA_KEY_BIZ_TYPE, "");
////        }
//    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        initPaidMontyInput();
    }

    @Override
    public void onResume() {
        super.onResume();

//        etPaidMoney.requestFocus();

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


    /**
     * 注册监听器
     */
    @Override
    protected void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED_CREDIT);
        intentFilter.addAction(Constants.BA_SEND_PAY_REQ_CREDIT);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                ZLogger.d("PayByCreditFragment.onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_CREDIT)) {
                    Bundle extras = intent.getExtras();
                    if(extras != null && extras.containsKey(EXTRA_KEY_HANDLE_AMOUNT)){
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        inlvPaidMoney.setInputString(MUtils.formatDouble(handleAmount, ""));
                        inlvPaidMoney.setEnabled(SharedPreferencesHelper
                                .getBoolean(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, false));
                        inlvPaidMoney.requestFocus();
                        calculateCharge();
                    }
                }else if (intent.getAction().equals(Constants.BA_SEND_PAY_REQ_CREDIT)){
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
     * */
    @Override
    protected void submitOrder(){
        if (bPayProcessing){
            ZLogger.df("正在进行赊账支付，不用重复发起请求");
            return;
        }

        final Human memberInfo = DataCacheHelper.getInstance().getMfMemberInfo();
        if (memberInfo == null){
            bPayProcessing = false;
            //输入满分账户信息
            EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_READ_MFACCOUNT));
            return;
        }

        String paidMoney = inlvPaidMoney.getInputString();
        if (StringUtils.isEmpty(paidMoney)){
            bPayProcessing = false;
            DialogUtil.showHint("请输入支付金额");
            return;
        }

        bPayProcessing = true;
        calculatePaidAmount();
        super.submitOrder();

        bPayProcessing = false;
        Bundle args = new Bundle();
        args.putString(MfPayEvent.KEY_OUTTRADENO, outTradeNo);
        args.putInt(MfPayEvent.KEY_PAY_TYPE, payType);
        args.putDouble(MfPayEvent.KEY_AMOUNT, paidAmount);
        args.putInt(MfPayEvent.KEY_PAY_STATUS, PosOrderPayEntity.PAY_STATUS_FINISH);
        EventBus.getDefault().post(new MfPayEvent(MfPayEvent.EVENT_ID_PAY_SUCCEE, args));
    }
}

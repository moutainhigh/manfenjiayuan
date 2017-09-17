package com.mfh.litecashier.components.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.bingshanguxue.cashier.pay.BaseWepayFragment;
import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.PayProcessView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;


/**
 * 微信刷卡支付
 *
 * @see <a href="https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=5_4">微信刷卡支付支付流程</a>
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByWxpayFragment extends BaseWepayFragment {

    @BindView(R.id.et_barCode)
    EditText etBarCode;//扫码枪扫描到的用户手机钱包中的付款条码
    @BindView(R.id.ll_pay_info)
    LinearLayout llPayInfo;
    @BindView(R.id.payProcessView)
    PayProcessView payProcessView;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_wx;
    }


//    @Override
//    protected void handleIntent() {
//        //for Fragment.instantiate
//        Bundle args = getArguments();
//        if (args != null) {
//            subject = args.getString(EXTRA_KEY_SUBJECT, "");
//            body = args.getString(EXTRA_KEY_BODY, "");
//            orderId = args.getString(EXTRA_KEY_ORDER_ID, "");
//            bizType = args.getString(EXTRA_KEY_BIZ_TYPE, "");
//        }
//    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        initBarCodeInput();

        payProcessView.init(120, "点击重试", "查询订单", "撤销订单",
                new PayProcessView.onCustomerViewListener() {

                    @Override
                    public void onAction1() {
//                        payProcessView.setState(PayProcessView.STATE_INIT, null);
                        payProcessView.setVisibility(View.GONE);
                    }

                    /**
                     * 查询订单状态
                     * 因网络或系统异常导致支付状态不明时调用
                     */
                    @Override
                    public void onAction2() {
                        queryOrder(outTradeNo, lastPaidAmount);
                    }

                    /**
                     * 撤单
                     * 因网络或系统异常导致支付状态不明时调用
                     */
                    @Override
                    public void onAction3() {
                        cancelOrder(outTradeNo);
                    }

                });

    }

    @Override
    public void onResume() {
        super.onResume();

        etBarCode.requestFocus();

        //TODO,主动去请求当前价格
//        EventBus.getDefault().post(new MfPayEvent(CashierConstants.PAYTYPE_ALIPAY, MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (payProcessView != null) {
            payProcessView.onDestory();
        }
    }


    private void initBarCodeInput() {
        etBarCode.setFocusable(true);
        etBarCode.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        etBarCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
                        DeviceUtils.showSoftInput(CashierApp.getAppContext(), etBarCode);
                    } else {
                        DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etBarCode);
                    }
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
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED_WX);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZLogger.d("onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_WX)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT);
                        etBarCode.setText("");
                        etBarCode.requestFocus();
                        payProcessView.setState(PayProcessView.STATE_INIT, null);

                        calculateCharge();
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * 微信条码支付--POS发起支付请求，后台像微信请求支付
     * 免密支付,直接返回支付结果，
     * 验密支付,返回10003(支付处理中)状态,然后POS轮询查询订单状态
     */
    @Override
    protected void submitOrder() {
        if (bPayProcessing) {
            ZLogger.d("正在进行微信支付，不用重复发起请求");
            return;
        }

//        支付授权码(条码)
        String authCode = etBarCode.getText().toString();
        if (StringUtils.isEmpty(authCode)) {
            bPayProcessing = false;
            DialogUtil.showHint("请输入授权码");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            bPayProcessing = false;
            return;
        }

        super.submitOrder();

        wepayBarPay(authCode);
    }


    /**
     * 正在取消支付订单
     * "正在取消支付订单..."
     */
    @Override
    protected void onBarpayProcessing(String msg) {
        llPayInfo.setVisibility(View.GONE);
        payProcessView.setState(PayProcessView.STATE_PROCESS, msg);
    }

    /**
     * 支付成功
     */
    @Override
    protected void onBarpayFinished(final Double paidAmount, final String msg, int color) {
        etBarCode.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle args = new Bundle();
                args.putSerializable(PayActionEvent.KEY_PAYMENT_INFO,
                        PaymentInfo.create(outTradeNo, payType,
                                PosOrderPayEntity.PAY_STATUS_FINISH,
                                paidAmount, paidAmount, 0D, null));
                EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, args));

                llPayInfo.setVisibility(View.VISIBLE);
                payProcessView.setState(PayProcessView.STATE_SUCCESS, msg);

                bPayProcessing = false;
            }
        }, 300);
    }

    /**
     * 交易失败
     */
    @Override
    protected void onBarpayFailed(final int payStatus, final String msg, int color, boolean isException) {
        ZLogger.w("微信条码支付失败:" + msg);
        try {
            if (isException) {
                payProcessView.setState(PayProcessView.STATE_ERROR, msg);
            } else {
                payProcessView.setState(PayProcessView.STATE_FAILED, msg);
            }

            etBarCode.getText().clear();//清空授权码

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bundle args = new Bundle();
                    args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO,
                            PaymentInfo.create(outTradeNo, payType, payStatus,
                                    paidAmount, paidAmount, 0D, null));
                    args.putString(PayStep1Event.KEY_ERROR_MESSAGE, msg);
                    EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FAILED, args));

                    llPayInfo.setVisibility(View.VISIBLE);

                    etBarCode.requestFocus();
                    etBarCode.setSelection(etBarCode.length());

                    bPayProcessing = false;
                }
            }, 1500);
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }

    }
}

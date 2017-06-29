package com.manfenjiayuan.pda_supermarket.ui.pay.instock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosOrderPayEntity;
import com.manfenjiayuan.pda_supermarket.cashier.model.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.cashier.pay.BaseWepayFragment;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayEvent;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayStep1Event;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.scOrder.ScOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;



/**
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByWxpayFragment extends BaseWepayFragment {

    @BindView(R.id.et_barCode)
    EditLabelView etBarCode;//扫码枪扫描到的用户手机钱包中的付款条码
    @BindView(R.id.ll_pay_info)
    LinearLayout llPayInfo;
    @BindView(R.id.ll_pay_loading)
    LinearLayout llPayLoading;
    @BindView(R.id.animProgress)
    ProgressBar progressBar;
    @BindView(R.id.tv_process)
    TextView tvProcess;
    @BindView(R.id.tv_countdown)
    TextView tvCountdown;
    @BindView(R.id.button_query_orderstatus)
    Button btnQueryOrderStatus;
    @BindView(R.id.button_cancel_order)
    Button btnCancelAliBarPay;

    private BarPayCountDownTimer payCountDownTimer;
    private boolean payTimerRunning;
//    private boolean bPayProcessing = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_wx;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        initBarCodeInput();

        payCountDownTimer = new BarPayCountDownTimer(30 * 1000, 1000);
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
        etBarCode.setInput(code);
        etBarCode.requestFocusEnd();
        if (!StringUtils.isEmpty(code)) {
            submitOrder();
        } else {
            isAcceptBarcodeEnabled = true;
        }
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
        if (payCountDownTimer != null) {
            payCountDownTimer.cancel();
            payCountDownTimer = null;
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 查询订单状态
     * 因网络或系统异常导致支付状态不明时调用
     */
    @OnClick(R.id.button_query_orderstatus)
    public void queryAliBarpayStatus() {
        queryOrder(outTradeNo, lastPaidAmount);
    }

    /**
     * 撤单
     * 因网络或系统异常导致支付状态不明时调用
     */
    @OnClick(R.id.button_cancel_order)
    public void cancelAliBarPay() {
        cancelOrder(outTradeNo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PayEvent event) {
        int action = event.getAction();
        Bundle extras = event.getArgs();
        ZLogger.d(String.format("PayEvent:%d\n%s",
                action, StringUtils.decodeBundle(extras)));
        if (extras == null) {
            return;
        }

        switch (event.getAction()) {
            case PayEvent.EVENT_ID_SCAN_PAYCODE: {
                int wayType = extras.getInt(EXTRA_KEY_WAYTYPE, WayType.NA);
                if (payType == wayType) {
                    onScanCode(extras.getString(EXTRA_KEY_SCANCODE));
                }
            }
            break;
        }
    }

    private void initBarCodeInput() {
        etBarCode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            submitOrder();
                        }
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
        intentFilter.addAction(Constants.BA_HANDLE_SCANBARCODE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();
                ZLogger.d(String.format("onReceive.action=%s\nextras:%s",
                        action, StringUtils.decodeBundle(extras)));
                if (StringUtils.isEmpty(action) || extras == null) {
                    return;
                }

                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_WX)) {
                    etBarCode.clearInput();
                    etBarCode.requestFocusEnd();
                    calculateCharge();
                } else if (Constants.BA_HANDLE_SCANBARCODE.equals(intent.getAction())) {
                    int wayType = extras.getInt(EXTRA_KEY_WAYTYPE, WayType.NA);
                    if (payType == wayType) {
                        onScanCode(extras.getString(EXTRA_KEY_SCANCODE));
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
            ZLogger.df("正在进行微信支付，不用重复发起请求");
            return;
        }

//        支付授权码(条码)
        String authCode = etBarCode.getInput();
        if (StringUtils.isEmpty(authCode)) {
            bPayProcessing = false;
            DialogUtil.showHint("请输入授权码");
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
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
        llPayLoading.setVisibility(View.VISIBLE);
        tvProcess.setText(msg);
        tvProcess.setTextColor(Color.parseColor("#FF000000"));
        progressBar.setVisibility(View.VISIBLE);
        btnCancelAliBarPay.setVisibility(View.GONE);
        btnQueryOrderStatus.setVisibility(View.GONE);

        if (!payTimerRunning) {
            payCountDownTimer.start();
            payTimerRunning = true;
        }
    }

    /**
     * 补差额
     */
    @Override
    protected void onBarpayFinished(final Double paidAmount, final String msg, final int color) {
        NetCallBack.NetTaskCallBack checkAndReturnOddAmountRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":null}
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                        }
                        notifyPayFinished(paidAmount, msg, color);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, errMsg, getErrorTextColor(), false);
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };
        ScOrderApiImpl.checkAndReturnOddAmount(orderId, WayType.WX_F2F, checkAndReturnOddAmountRC);

    }

    protected void notifyPayFinished(final Double paidAmount, String msg, int color) {

        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);
        btnCancelAliBarPay.setVisibility(View.GONE);
        btnQueryOrderStatus.setVisibility(View.GONE);

        tvCountdown.setText("");
        payCountDownTimer.cancel();
        payTimerRunning = false;

        etBarCode.setInput("");//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, null));

                llPayInfo.setVisibility(View.VISIBLE);
                llPayLoading.setVisibility(View.GONE);

                bPayProcessing = false;
                isAcceptBarcodeEnabled = true;        //验证参数

            }
        }, 500);
    }
        /**
         * 交易失败
         */
    @Override
    protected void onBarpayFailed(final int payStatus, final String msg, int color, boolean isException) {
        ZLogger.df("微信条码支付失败:" + msg);
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);

        if (isException) {
            btnCancelAliBarPay.setVisibility(View.VISIBLE);
            btnQueryOrderStatus.setVisibility(View.VISIBLE);
        } else {
            btnCancelAliBarPay.setVisibility(View.GONE);
            btnQueryOrderStatus.setVisibility(View.GONE);
        }

        tvCountdown.setText("");
        payCountDownTimer.cancel();
        payTimerRunning = false;

        etBarCode.clearInput();//清空授权码

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
                llPayLoading.setVisibility(View.GONE);

                bPayProcessing = false;
                isAcceptBarcodeEnabled = true;        //验证参数

            }
        }, 2000);
    }

    /**
     * 倒计时
     */
    public class BarPayCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public BarPayCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvCountdown.setText(String.format("%d秒", millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            tvCountdown.setText("");
//            btnCancelAliBarPay.setVisibility(View.VISIBLE);
        }
    }

}

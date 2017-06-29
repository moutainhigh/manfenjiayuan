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
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosOrderPayEntity;
import com.manfenjiayuan.pda_supermarket.cashier.model.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.cashier.pay.BaseAlipayFragment;
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
 * 支付--支付宝条码支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 * {@see <a href="https://doc.open.alipay.com/doc2/detail.htm?spm=0.0.0.0.6L5pUZ&treeId=26&articleId=104040&docType=1"></a>}
 * <p>
 * 商户收银系统(POS)
 * 1--收银系统轮询时，需出现等待支付结果的界面
 * 当收银系统进入轮询查询时，收银系统需要出现等待界面，此时不允许其他收单操作，同时等待界面上需有倒计时或滚动条，
 * 便于收银员了解当前订单进度，同时当等待超时后若支付结果还是未付款状态，收银系统应调用撤销订单 API。
 * 2--轮询时出现等待界面，需要提供取消等待的功能
 * 收银系统出现轮询等待界面时，界面上需有取消等待的按钮，便于收银员及时取消无效或者异常的订单，以提高收银效率；
 * 取消等待操作后，收银系统应调用撤 销订单 API，关闭或者冲正无效或异常的订单。
 * 3--未联网时需禁止支付宝收款功能
 * 商户收银系统在未联网的情况下应禁止收银员使用支付宝收款，并告知收银员网络异常。
 *
 * @link {https://app.alipay.com/market/document.htm?name=tiaomazhifu#page-6}
 */
public class PayByAlipayFragment extends BaseAlipayFragment {

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

    public static PayByAlipayFragment newInstance(Bundle args) {
        PayByAlipayFragment fragment = new PayByAlipayFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_alipay;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        //for Fragment.instantiate
//        Bundle args = getArguments();
//        if (args != null) {
//            subject = args.getString(EXTRA_KEY_SUBJECT, "");
//            body = args.getString(EXTRA_KEY_BODY, "");
////            orderId = args.getString(EXTRA_KEY_ORDER_ID, "");
//            bizType = args.getString(EXTRA_KEY_BIZ_TYPE, "");
//        }

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
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }
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
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED_ALIPAY);
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

                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_ALIPAY)) {
                    etBarCode.setInput("");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PayEvent event) {
        int action = event.getAction();
        Bundle extras = event.getArgs();
        ZLogger.d(String.format("PayEvent:%d\n%s",
                action, StringUtils.decodeBundle(extras)));
        if (extras == null){
            return;
        }

        switch (event.getAction()) {
            case PayEvent.EVENT_ID_SCAN_PAYCODE: {
                int wayType = extras.getInt(EXTRA_KEY_WAYTYPE, WayType.NA);
                if (payType == wayType){
                    onScanCode(extras.getString(EXTRA_KEY_SCANCODE));
                }
            }
            break;
        }
    }

    /**
     * 支付宝条码支付--POS发起支付请求，后台向支付宝请求支付<br>
     * <b>应用场景实例：</b>收银员使用扫码设备读取用户手机支付宝“付款码”后，将二维码或条码信息通过本接口上送至支付宝发起支付。<br>
     * 免密支付,直接返回支付结果，
     * 验密支付,返回10003(支付处理中)状态,然后POS轮询查询订单状态
     */
    @Override
    protected void submitOrder() {
        if (bPayProcessing) {
            ZLogger.df("正在进行支付宝支付，不用重复发起请求");
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
            DialogUtil.showHint(R.string.toast_network_error);
            bPayProcessing = false;
            return;
        }

        super.submitOrder();

        alipayBarPay(authCode);
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
     * 支付成功
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
                        onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED,
                                errMsg, AppHelper.getErrorTextColor(), false);
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        //补差额
        ScOrderApiImpl.checkAndReturnOddAmount(orderId, WayType.ALI_F2F, checkAndReturnOddAmountRC);
    }


    private void notifyPayFinished(final Double paidAmount, String msg, int color) {
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);
        btnCancelAliBarPay.setVisibility(View.GONE);
        btnQueryOrderStatus.setVisibility(View.GONE);

        tvCountdown.setText("");
        payCountDownTimer.cancel();
        payTimerRunning = false;

        etBarCode.clearInput();//清空授权码

        hideProgressDialog();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Bundle args = new Bundle();
                args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO,
                        PaymentInfo.create(outTradeNo, payType,
                                PosOrderPayEntity.PAY_STATUS_FINISH,
                                paidAmount, paidAmount, 0D, null));
                EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, args));

                llPayInfo.setVisibility(View.VISIBLE);
                llPayLoading.setVisibility(View.GONE);

                etBarCode.requestFocusEnd();

                bPayProcessing = false;
                isAcceptBarcodeEnabled = true;        //验证参数
            }
        }, 500);
    }

    /**
     * 交易失败
     */
    @Override
    protected  void onBarpayFailed(final int payStatus, final String msg, int color, boolean isException) {
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

        etBarCode.setInput("");//清空授权码

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
                etBarCode.requestFocusEnd();

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

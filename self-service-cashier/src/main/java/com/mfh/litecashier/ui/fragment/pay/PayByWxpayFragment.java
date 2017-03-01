package com.mfh.litecashier.ui.fragment.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.bingshanguxue.cashier.v1.PaymentInfoImpl;
import com.mfh.comn.net.ResponseBody;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.EmptyEntity;
import com.mfh.litecashier.utils.AppHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

import static com.mfh.framework.api.pay.PayApi.WXPAY_CHANNEL_ID;

/**
 * 微信刷卡支付
 * @see <a href="https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=5_4">微信刷卡支付支付流程</a>
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByWxpayFragment extends BasePayFragment {

    @BindView(R.id.et_barCode)
    EditText etBarCode;//扫码枪扫描到的用户手机钱包中的付款条码
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

    private Double lastPaidAmount = 0D;//上一次支付金额,支付异常时查询订单状态

    private BarPayCountDownTimer payCountDownTimer;
    private boolean payTimerRunning;
//    private boolean bPayProcessing = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_wx;
    }

    @Override
    protected int getPayType() {
        return WayType.WX_F2F;
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

        payCountDownTimer = new BarPayCountDownTimer(30 * 1000, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();

        etBarCode.requestFocus();

        //TODO,主动去请求当前价格
//        EventBus.getDefault().post(new MfPayEvent(CashierConstants.PAYTYPE_ALIPAY, MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT));
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (payCountDownTimer != null) {
            payCountDownTimer.cancel();
            payCountDownTimer = null;
        }
    }

    @Override
    protected void calculatePaidAmount() {
        paidAmount = handleAmount;
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

    private void initBarCodeInput() {
        etBarCode.setFocusable(true);
        etBarCode.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        etBarCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()){
                        DeviceUtils.showSoftInput(CashierApp.getAppContext(), etBarCode);
                    }
                    else{
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
                    if (extras != null && extras.containsKey(EXTRA_KEY_HANDLE_AMOUNT)) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        etBarCode.setText("");
                        etBarCode.requestFocus();
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
            ZLogger.df("正在进行微信支付，不用重复发起请求");
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

        bPayProcessing = true;
        onBarpayProcessing("正在发送支付请求...");
        lastPaidAmount = paidAmount;
        super.submitOrder();

        Bundle args = new Bundle();
        args.putSerializable(PayActionEvent.KEY_PAYMENT_INFO,
                PaymentInfoImpl.genPaymentInfo(outTradeNo, payType,
                        PosOrderPayEntity.PAY_STATUS_PROCESS,
                        paidAmount, paidAmount, 0D));
        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_PROCESS, args));

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", generateOrderInfo(lastPaidAmount, authCode).toJSONString());
        options.put("bizType", bizType);
        options.put("chId", WXPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().wepayBarPay(options,
                new Subscriber<MResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.df("微信条码支付异常:" + e.toString());
                        onBarpayFailed(e.toString(), AppHelper.getErrorTextColor(), true);

                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null){
                            ZLogger.df("微信条码支付失败");
                            onBarpayFailed("微信条码支付失败，无响应", AppHelper.getErrorTextColor(), false);
                            return;
                        }
                        ZLogger.df(String.format("微信条码支付:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));
                        switch (stringMResponse.getCode()) {
                            //{"code":"0","msg":"Success","version":"1","data":""}
                            //10000--业务处理成功（订单支付成功）
                            case 0: {
                                onBarpayFinished(lastPaidAmount, "支付成功", AppHelper.getOkTextColor());
                            }
                            break;
                            //下单成功等待用户输入密码
                            //{"code":"1","msg":" order success pay inprocess","version":"1","data":""}
                            //订单创建成功支付处理中(验密支付)
                            //10003，业务处理中,该结果码只有在条码支付请求 API 时才返回，代表付款还在进行中，需要调用查询接口查询最终的支付结果
                            // 条码支付请求 API 返回支付处理中(返回码 10003)时，此时若用户支付宝钱包在线则会唤起支付宝钱包的快捷收银台，
                            // 用户可输入密码支付。商户需要在设定的轮询时间内，通过订单查询 API 查询订单状态，若返回付款成功，则表示支付成功。
                            case 1: {
                                queryOrder(outTradeNo, lastPaidAmount);
                            }
                            break;
                            ////交易创建失败
                            //40004--错误码：ACQ.INVALID_PARAMETER错误描述：支付失败，交易参数异常，请顾客刷新付款码后重新收款。如再次收款失败，请联系管理员处理。[INVALID_PARAMETER]
                            //40004--错误码：ACQ.PAYMENT_AUTH_CODE_INVALID错误描述：支付失败，获取顾客账户信息失败，请顾客刷新付款码后重新收款，如再次收款失败，请联系管理员处理。[SOUNDWAVE_PARSER_FAIL]
                            default: {//-1
                                onBarpayFailed(stringMResponse.getMsg(), AppHelper.getErrorTextColor(), false);
//                                onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FE5000"), true);
//                                queryOrder(outTradeNo, lastPaidAmount);
                            }
                            break;
                        }
                    }
                });
    }


    /**
     * 微信支付--轮询查询订单状态
     * <b>应用场景实例：</b>本接口提供微信支付订单的查询的功能，商户可以通过本接口主动查询订单状态，完成下一步的业务逻辑。<br>
     * 需要调用查询接口的情况：<br>
     * 1. 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；<br>
     * 2. 调用扫码支付支付接口后，返回系统错误或未知交易状态情况；<br>
     * 3. 调用扫码支付请求后，如果结果返回处理中（返回结果中的code等于10003）的状态；<br>
     * 4. 调用撤销接口API之前，需确认该笔交易目前支付状态。<br>
     */
    private void queryOrder(final String outTradeNo, final Double paidAmount) {
        NetCallBack.RawNetTaskCallBack payRespCallback = new NetCallBack.RawNetTaskCallBack<EmptyEntity,
                NetProcessor.RawProcessor<EmptyEntity>>(
                new NetProcessor.RawProcessor<EmptyEntity>() {

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("微信条码支付状态查询:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));

                        //业务处理成功
                        // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                        switch (rspBody.getRetCode()) {
                            case "0":
                                onBarpayFinished(paidAmount, "支付成功", AppHelper.getOkTextColor());
                                break;
                            //{"code":"-1","msg":"继续查询","version":"1","data":""}
                            // 支付结果不明确，需要收银员继续查询或撤单
                            case "-1": //-1
                                onBarpayFailed(rspBody.getReturnInfo(), AppHelper.getErrorTextColor(), true);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default: //-2
                                onBarpayFailed(rspBody.getReturnInfo(), AppHelper.getErrorTextColor(), false);
                                break;
                        }
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询订单状态失败:" + errMsg);
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onBarpayFailed(errMsg, AppHelper.getErrorTextColor(), true);
                    }
                }
                , EmptyEntity.class
                , CashierApp.getAppContext()) {
        };

        onBarpayProcessing("正在查询订单状态...");
        PayApi.queryWxBarpayStatus(outTradeNo, payRespCallback);
    }

    /**
     * 微信支付--撤单
     * <b>应用场景实例：</b>调用微信支付接口时未返回明确的返回结果（如系统错误或网络异常），可使用本接口将交易进行撤销。<br>
     * 如果用户支付失败，微信会将此订单关闭；如果用户支付成功，微信会将支付的资金退还给用户。<br>
     * 撤销只支持24小时内的交易，超过24小时要退款可以调用申请退款接口，如果需要明确订单状态可以调用查询订单接口。<br>
     * 只有发生支付系统超时或者支付结果未知时可调用撤销，其他正常支付 的单如需实现相同功能请调用申请退款 API。提交支付交易后调用【查询订单 API】， 没有明确的支付结果再调用【撤销订单 API】。
     */
    private void cancelOrder(String outTradeNo) {
        onBarpayProcessing("正在发送撤单请求...");
        NetCallBack.RawNetTaskCallBack payRespCallback = new NetCallBack.RawNetTaskCallBack<EmptyEntity,
                NetProcessor.RawProcessor<EmptyEntity>>(
                new NetProcessor.RawProcessor<EmptyEntity>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("撤单失败:" + errMsg);
                        onBarpayFailed(errMsg, AppHelper.getErrorTextColor(), true);
                    }

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("微信条码支付取消订单:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));

                        //业务处理成功
                        // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                        if (rspBody.getRetCode().equals("0")) {
                            onBarpayFailed("订单已取消", AppHelper.getOkTextColor(), false);
                        }
                        //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                        //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                        //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                        // 处理失败,交易不存在
                        //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                        else {//-1
                            onBarpayFailed(rspBody.getReturnInfo(),
                                    AppHelper.getErrorTextColor(), true);
                        }
                    }
                }
                , EmptyEntity.class
                , CashierApp.getAppContext()) {
        };

        PayApi.cancelWxBarpay(outTradeNo, payRespCallback);
    }

    /**
     * 正在取消支付订单
     * "正在取消支付订单..."
     */
    private void onBarpayProcessing(String msg) {
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
    private void onBarpayFinished(final Double paidAmount, String msg, int color) {
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);
        btnCancelAliBarPay.setVisibility(View.GONE);
        btnQueryOrderStatus.setVisibility(View.GONE);

        tvCountdown.setText("");
        if (payCountDownTimer != null) {
            payCountDownTimer.cancel();
        }
        payTimerRunning = false;

        etBarCode.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Bundle args = new Bundle();
                args.putSerializable(PayActionEvent.KEY_PAYMENT_INFO,
                        PaymentInfoImpl.genPaymentInfo(outTradeNo, payType,
                                PosOrderPayEntity.PAY_STATUS_FINISH,
                                paidAmount, paidAmount, 0D));
                EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, args));

                llPayInfo.setVisibility(View.VISIBLE);
                llPayLoading.setVisibility(View.GONE);

                bPayProcessing = false;
            }
        }, 500);
    }

    /**
     * 交易失败
     */
    private void onBarpayFailed(String msg, int color, boolean isException) {
        ZLogger.df("微信条码支付失败:" + msg);
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);

        final Bundle args = new Bundle();
        if (isException) {
            args.putSerializable(PayActionEvent.KEY_PAYMENT_INFO,
                    PaymentInfoImpl.genPaymentInfo(outTradeNo, payType,
                            PosOrderPayEntity.PAY_STATUS_EXCEPTION,
                            paidAmount, paidAmount, 0D));

            btnCancelAliBarPay.setVisibility(View.VISIBLE);
            btnQueryOrderStatus.setVisibility(View.VISIBLE);
        } else {
            args.putSerializable(PayActionEvent.KEY_PAYMENT_INFO,
                    PaymentInfoImpl.genPaymentInfo(outTradeNo, payType,
                            PosOrderPayEntity.PAY_STATUS_FAILED,
                            paidAmount, paidAmount, 0D));

            btnCancelAliBarPay.setVisibility(View.GONE);
            btnQueryOrderStatus.setVisibility(View.GONE);
        }
        args.putString(PayStep1Event.KEY_ERROR_MESSAGE, msg);

        tvCountdown.setText("");
        payCountDownTimer.cancel();
        payTimerRunning = false;

        etBarCode.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault()
                        .post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FAILED, args));

                llPayInfo.setVisibility(View.VISIBLE);
                llPayLoading.setVisibility(View.GONE);

                bPayProcessing = false;
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

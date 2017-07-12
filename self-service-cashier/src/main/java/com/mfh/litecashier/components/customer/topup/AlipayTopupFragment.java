package com.mfh.litecashier.components.customer.topup;

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
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.api.constant.WayType;
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
import com.mfh.framework.rxapi.http.AliPayHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.AppHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * 支付--支付宝条码支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 *
 * @see <a href="https://doc.open.alipay.com/doc2/detail.htm?spm=0.0.0.0.6L5pUZ&treeId=26&articleId=104040&docType=1">支付宝条码支付</a>
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
 * @see <a href="https://app.alipay.com/market/document.htm?name=tiaomazhifu#page-6">支付宝条码支付</a>
 */
public class AlipayTopupFragment extends BaseTopupFragment {

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

    private BarPayCountDownTimer payCountDownTimer;
    private boolean payTimerRunning;

    public static AlipayTopupFragment newInstance(Bundle args) {
        AlipayTopupFragment fragment = new AlipayTopupFragment();

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
    protected void onPayProcess(String message) {
        llPayInfo.setVisibility(View.GONE);
        llPayLoading.setVisibility(View.VISIBLE);
        tvProcess.setText(message);
        tvProcess.setTextColor(Color.parseColor("#FF000000"));
        progressBar.setVisibility(View.VISIBLE);
        btnCancelAliBarPay.setVisibility(View.GONE);
        btnQueryOrderStatus.setVisibility(View.GONE);

        if (!payTimerRunning) {
            payCountDownTimer.start();
            payTimerRunning = true;
        }
    }

    @Override
    protected void onPayFailed(int payStatus, String msg, int color, boolean isException) {
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

        etBarCode.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new TopupActionEvent(TopupActionEvent.TOPUP_ERROR, null));

                llPayInfo.setVisibility(View.VISIBLE);
                llPayLoading.setVisibility(View.GONE);
                etBarCode.requestFocus();
                etBarCode.setSelection(etBarCode.length());

                bPayProcessing = false;
            }
        }, 2000);
    }

    @Override
    protected void onPayFinished(Double paidAmount, String msg, int color) {
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);
        btnCancelAliBarPay.setVisibility(View.GONE);
        btnQueryOrderStatus.setVisibility(View.GONE);

        tvCountdown.setText("");
        payCountDownTimer.cancel();
        payTimerRunning = false;

        etBarCode.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new TopupActionEvent(TopupActionEvent.TOPUP_SUCCEED, null));

                llPayInfo.setVisibility(View.VISIBLE);
                llPayLoading.setVisibility(View.GONE);

                etBarCode.requestFocus();
                etBarCode.setSelection(etBarCode.length());

                bPayProcessing = false;
            }
        }, 500);
    }

    @Override
    protected int getPayType() {
        return WayType.ALI_F2F;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

        subject = "支付宝充值";
        body = "充值";

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

    /**
     * 注册监听器
     */
    @Override
    protected void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_TOPUPAMOUNT_CHANGED_ALIPAY);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZLogger.d("onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_TOPUPAMOUNT_CHANGED_ALIPAY)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        etBarCode.setText("");
                        etBarCode.requestFocus();
                        paidAmount = extras.getDouble(EXTRA_KEY_TOTAL_AMOUNT);
                        customerId = extras.getLong(EXTRA_KEY_CUSTOMER_ID);
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * 查询订单状态
     * 因网络或系统异常导致支付状态不明时调用
     */
    @OnClick(R.id.button_query_orderstatus)
    public void queryAliBarpayStatus() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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
                        submitOrder(etBarCode.getText().toString());

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


    /**
     * 支付宝条码支付--POS发起支付请求，后台向支付宝请求支付<br>
     * <b>应用场景实例：</b>收银员使用扫码设备读取用户手机支付宝“付款码”后，将二维码或条码信息通过本接口上送至支付宝发起支付。<br>
     * 免密支付,直接返回支付结果，
     * 验密支付,返回10003(支付处理中)状态,然后POS轮询查询订单状态
     * @param authCode 支付宝支付授权码
     */
    @Override
    protected void submitOrder(String authCode) {
        if (bPayProcessing) {
            ZLogger.d("正在进行支付宝支付，不用重复发起请求");
            return;
        }

        if (StringUtils.isEmpty(authCode)) {
            bPayProcessing = false;
            DialogUtil.showHint("请输入授权码");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            bPayProcessing = false;
            return;
        }

        bPayProcessing = true;
        onPayProcess("正在充值...");
        lastPaidAmount = paidAmount;
        EventBus.getDefault().post(new TopupActionEvent(TopupActionEvent.TOPUP_PROCESS, null));

        generateOutTradeNo();

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", generateOrderInfo(lastPaidAmount, authCode).toJSONString());
        options.put("bizType", bizType);
        options.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().alipayBarPay(options,
                new Subscriber<MResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef("【充值】支付宝条码支付异常:" + e.toString());
                        onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, e.getMessage(),
                                AppHelper.getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            ZLogger.e("【充值】支付宝支付失败");
                            onPayFailed(PosOrderPayEntity.PAY_STATUS_FAILED,
                                    "支付宝支付失败，无响应", AppHelper.getErrorTextColor(), false);
                            return;
                        }
                        ZLogger.i(String.format("【充值】支付宝条码支付:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));
                        switch (stringMResponse.getCode()) {
                            //{"code":"0","msg":"Success","version":"1","data":""}
                            //10000--业务处理成功（订单支付成功）
                            case 0: {
                                onPayFinished(lastPaidAmount, "支付成功", AppHelper.getOkTextColor());
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
                                onPayFailed(PosOrderPayEntity.PAY_STATUS_FAILED,
                                        stringMResponse.getMsg(), AppHelper.getErrorTextColor(), false);
//                                onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FE5000"), true);
//                                queryOrder(outTradeNo, lastPaidAmount);
                            }
                            break;
                        }
                    }
                });
    }

    /**
     * 支付宝支付--轮询查询订单状态
     * <b>应用场景实例：</b>本接口提供支付宝支付订单的查询的功能，商户可以通过本接口主动查询订单状态，完成下一步的业务逻辑。<br>
     * 需要调用查询接口的情况：<br>
     * 1. 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；<br>
     * 2. 调用扫码支付支付接口后，返回系统错误或未知交易状态情况；<br>
     * 3. 调用扫码支付请求后，如果结果返回处理中（返回结果中的code等于10003）的状态；<br>
     * 4. 调用撤销接口API之前，需确认该笔交易目前支付状态。<br>
     */
    protected void queryOrder(final String outTradeNo, final Double paidAmount) {
        onPayProcess("正在查询订单状态...");

        Map<String, String> options = new HashMap<>();
        options.put("out_trade_no", outTradeNo);
        options.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AliPayHttpManager.getInstance().query(options,
                new Subscriber<MResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef("支付宝条码支付状态查询:" + e.toString());
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, e.getMessage(),
                                AppHelper.getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            showProgressDialog(ProgressDialog.STATUS_DONE, "支付宝条码支付状态查询，无响应", true);
                            return;
                        }
                        ZLogger.i(String.format("支付宝条码支付状态查询:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));

                        switch (stringMResponse.getCode()) {
                            //业务处理成功
                            // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                            case 0:
                                onPayFinished(paidAmount, "支付成功", AppHelper.getOkTextColor());
                                break;
                            //{"code":"-1","msg":"Success","version":"1","data":""}
                            // 支付结果不明确，需要收银员继续查询或撤单
                            case -1:
                                onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION,
                                        stringMResponse.getMsg(), AppHelper.getErrorTextColor(), true);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default:
                                onPayFailed(PosOrderPayEntity.PAY_STATUS_FAILED,
                                        stringMResponse.getMsg(), AppHelper.getErrorTextColor(), false);
                                break;
                        }
                    }
                });
    }

    /**
     * 支付宝支付--撤单
     * <b>应用场景实例：</b>调用支付宝支付接口时未返回明确的返回结果（如系统错误或网络异常），可使用本接口将交易进行撤销。<br>
     * 如果用户支付失败，支付宝会将此订单关闭；如果用户支付成功，支付宝会将支付的资金退还给用户。<br>
     * 撤销只支持24小时内的交易，超过24小时要退款可以调用申请退款接口，如果需要明确订单状态可以调用查询订单接口。<br>
     * 只有发生支付系统超时或者支付结果未知时可调用撤销，其他正常支付 的单如需实现相同功能请调用申请退款 API。提交支付交易后调用【查询订单 API】， 没有明确的支付结果再调用【撤销订单 API】。
     */
    protected void cancelOrder(String outTradeNo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }
        onPayProcess("正在发送撤单请求...");

        Map<String, String> options = new HashMap<>();
        options.put("out_trade_no", outTradeNo);
        options.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AliPayHttpManager.getInstance().cancelOrder(options,
                new Subscriber<MResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef("撤单失败:" + e.toString());
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, e.getMessage(),
                                AppHelper.getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION,
                                    "支付宝条码支付取消订单，无响应", AppHelper.getErrorTextColor(), true);
                            return;
                        }
                        ZLogger.i(String.format("支付宝条码支付取消订单:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));

                        switch (stringMResponse.getCode()) {
                            case 0:
                                onPayFailed(PosOrderPayEntity.PAY_STATUS_CANCELED, "订单已取消",
                                        AppHelper.getErrorTextColor(), false);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default: //-2
                                onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION,
                                        stringMResponse.getMsg(), AppHelper.getErrorTextColor(), true);
                                break;
                        }
                    }
                });
    }

    /**
     * 支付宝支付--退款(应用场景暂时未确定)
     */
    protected void refund(final Double paidAmount) {
        NetCallBack.NetTaskCallBack payRespCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        try {
//                        {"code":"0","msg":"新增成功!","version":"1","data":{"val":"40513"}}
//                        java.lang.ClassCastException: java.lang.Integer cannot be cast to com.alibaba.fastjson.JSONObject
//                            RspBean<String> retValue = (RspBean<String>) rspData;
//                            String wrapper = retValue.getValue();

//                            if (wrapper != null){
//                                listView.setAdapter(new ExpressCompanyAdapter(getContext(), wrapper.getOptions()));
//                            }

                            ZLogger.d("退款结果:");
                        } catch (Exception ex) {
                            ZLogger.e("退款失败: " + ex.toString());
                        } finally {
                            onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, "",
                                    AppHelper.getErrorTextColor(), true);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);

                        ZLogger.d("退款失败:" + errMsg);
                        onPayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, errMsg,
                                AppHelper.getErrorTextColor(), false);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.refundAlipayOrder(payRespCallback);
    }
}

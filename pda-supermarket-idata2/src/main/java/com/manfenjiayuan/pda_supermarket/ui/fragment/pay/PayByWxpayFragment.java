package com.manfenjiayuan.pda_supermarket.ui.fragment.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.EmptyEntity;
import com.manfenjiayuan.pda_supermarket.cashier.PaymentInfoImpl;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderPayEntity;
import com.mfh.comn.net.ResponseBody;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.PayApi;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * Created by kun on 15/8/31.
 */
public class PayByWxpayFragment extends BasePayFragment {

    @Bind(R.id.et_barCode)
    EditLabelView etBarCode;//扫码枪扫描到的用户手机钱包中的付款条码
    @Bind(R.id.ll_pay_info)
    LinearLayout llPayInfo;
    @Bind(R.id.ll_pay_loading)
    LinearLayout llPayLoading;
    @Bind(R.id.animProgress)
    ProgressBar progressBar;
    @Bind(R.id.tv_process)
    TextView tvProcess;
    @Bind(R.id.tv_countdown)
    TextView tvCountdown;
    @Bind(R.id.button_query_orderstatus)
    Button btnQueryOrderStatus;
    @Bind(R.id.button_cancel_order)
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
        etBarCode.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submitOrder();
            }

            @Override
            public void onScan() {

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
                if (StringUtils.isEmpty(action) || extras == null){
                    return;
                }

                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_WX)) {
                    if (extras.containsKey(EXTRA_KEY_HANDLE_AMOUNT)) {
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT, 0);
                        etBarCode.clearInput();
                        etBarCode.requestFocusEnd();
                        calculateCharge();
                    }
                }
                else if (Constants.BA_HANDLE_SCANBARCODE.equals(intent.getAction())){
                    int wayType = extras.getInt(EXTRA_KEY_WAYTYPE, WayType.NA);
                    if (payType == wayType){
                        onScanCode(extras.getString(EXTRA_KEY_SCANCODE));
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * 微信支付--创建支付订单
     * 1_100014_1445935035219
     */
    private JSONObject generateOrderInfo(Double paidAmount, String authCode) {
        // 商户订单号
//        outTradeNo = String.format("%s_%d", orderId, System.currentTimeMillis());

        JSONObject orderInfo = new JSONObject();
        orderInfo.put("out_trade_no", outTradeNo);
        orderInfo.put("scene", "bar_code");
        orderInfo.put("auth_code", authCode);
        orderInfo.put("total_amount", MUtils.formatDouble(paidAmount, ""));
//        orderInfo.put("discountable_amount", MStringUtil.formatAmount(discountableAmount));
        orderInfo.put("subject", subject);
        orderInfo.put("body", body);
        orderInfo.put("operator_id", MfhLoginService.get().getCurrentGuId());//商户操作员编号
        orderInfo.put("store_id", MfhLoginService.get().getCurOfficeId());//商户门店编号
        orderInfo.put("terminal_id", SharedPreferencesManager.getTerminalId());
        orderInfo.put("seller_id", MfhLoginService.get().getSpid());//租户ID

        return orderInfo;
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

        //TODO,调用微信支付接口
//        {"code":"0","msg":"操作成功!","version":"1","data":{"code":"40004","msg":"错误码：ACQ.CONTEXT_INCONSISTENT错误描述：支付失败，商户订单号重复，请收银员取消本笔交易并重新收款。[CONTEXT_INCONSISTENT]"}}
        NetCallBack.RawNetTaskCallBack payRespCallback = new NetCallBack.RawNetTaskCallBack<EmptyEntity,
                NetProcessor.RawProcessor<EmptyEntity>>(
                new NetProcessor.RawProcessor<EmptyEntity>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                    }

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("微信条码支付:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));
                        switch (rspBody.getRetCode()) {
                            //10000--业务处理成功（订单支付成功）
                            case "0": {
                                onBarpayFinished(lastPaidAmount, "支付成功",
                                        AppHelper.getOkTextColor());
                            }
                            break;
                            //{"code":"1","msg":"支付等待:需要用户输入支付密码","version":"1","data":""}
                            //订单创建成功支付处理中(验密支付)
                            //10003，业务处理中,该结果码只有在条码支付请求 API 时才返回，代表付款还在进行中，需要调用查询接口查询最终的支付结果
                            // 条码支付请求 API 返回支付处理中(返回码 10003)时，此时若用户微信钱包在线则会唤起微信钱包的快捷收银台，
                            // 用户可输入密码支付。商户需要在设定的轮询时间内，通过订单查询 API 查询订单状态，若返回付款成功，则表示支付成功。
                            case "1": {
                                queryOrder(outTradeNo, lastPaidAmount);
                            }
                            break;
                            //{"code":"1","msg":"bizType参数不能为空!","version":"1","data":null}
                            //{"code":"-1","msg":"参数错误:缺少参数","version":"1","data":""}
                            //{"code":"-1","msg":"支付失败:请扫描微信支付被扫条码/二维码","version":"1","data":""}
                            ////交易创建失败
                            //40004--错误码：ACQ.INVALID_PARAMETER错误描述：支付失败，交易参数异常，请顾客刷新付款码后重新收款。如再次收款失败，请联系管理员处理。[INVALID_PARAMETER]
                            //40004--错误码：ACQ.PAYMENT_AUTH_CODE_INVALID错误描述：支付失败，获取顾客账户信息失败，请顾客刷新付款码后重新收款，如再次收款失败，请联系管理员处理。[SOUNDWAVE_PARSER_FAIL]
                            default: {//-1
                                onBarpayFailed(rspBody.getReturnInfo(), AppHelper.getErrorTextColor(), false);
//                                onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FE5000"), true);
//                                queryOrder(outTradeNo, lastPaidAmount);
                            }
                            break;
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知
                        onBarpayFailed(errMsg, AppHelper.getErrorTextColor(), true);
                    }
                }
                , EmptyEntity.class
                , AppContext.getAppContext()) {
        };

        PayApi.wxBarPay(generateOrderInfo(lastPaidAmount, authCode).toJSONString(), bizType, payRespCallback);
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
                , AppContext.getAppContext()) {
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
                , AppContext.getAppContext()) {
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
        payCountDownTimer.cancel();
        payTimerRunning = false;

        etBarCode.setInput("");//清空授权码

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
                isAcceptBarcodeEnabled = true;        //验证参数

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

        etBarCode.setInput("");//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault()
                        .post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FAILED, args));

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

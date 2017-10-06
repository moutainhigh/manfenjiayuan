package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.service.PosTopupService;
import com.bingshanguxue.cashier.model.PayStatus;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.bingshanguxue.vector_uikit.FontFitTextView;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.http.ErrorCode;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.httpmgr.AliPayHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MSubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.AppHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import rx.Subscriber;


/**
 * <h1>账号操作：锁定/交接班/登录/退出</h1><br>
 * <p/>
 * 1.支付完成 {@link DialogClickListener#onPaySucceed(Double, String)} ()}<br>
 * 2.支付异常 {@link DialogClickListener#onPayCanceled()} ()}<br>
 *
 * @author NAT.ZZN(bingshanguxue)
 */
public class AlipayDialog extends CommonDialog {

    private View rootView;
    private ImageButton btnSync, btnClose;
    private TextView tvTitle, tvSubTitle;
    private FontFitTextView tvHandleAmount;
    private EditText etAuthCode;
    private LinearLayout frameOperation;
    private Button btnQueryOrder, btnCancelOrder;
    private LinearLayout frameProcess;
    private ProgressBar progressBar;
    private TextView tvProcess;

    private QuickPayInfo mQuickPayInfo;
    //设备号＋订单编号＋时间
    private String outTradeNo;//商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。

    private boolean bPayProcessing = false;


    private DoubleInputDialog changePriceDialog = null;

    public interface DialogClickListener {
        /**
         * 支付完成
         */
        void onPaySucceed(QuickPayInfo mQuickPayInfo, String outTradeNo);

        /**
         * 取消支付
         */
        void onPayCanceled();
    }


    private DialogClickListener mListener;

    private AlipayDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private AlipayDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_alipay, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnSync = (ImageButton) rootView.findViewById(R.id.button_sync);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        tvHandleAmount = (FontFitTextView) rootView.findViewById(R.id.tv_handle_amount);
        tvSubTitle = (TextView) rootView.findViewById(R.id.tv_subtitle);
        etAuthCode = (EditText) rootView.findViewById(R.id.et_authcode);
        frameOperation = (LinearLayout) rootView.findViewById(R.id.frame_operation);
        btnQueryOrder = (Button) rootView.findViewById(R.id.button_query_orderstatus);
        btnCancelOrder = (Button) rootView.findViewById(R.id.button_cancel_order);
        frameProcess = (LinearLayout) rootView.findViewById(R.id.frame_process);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        tvProcess = (TextView) rootView.findViewById(R.id.tv_process);

        tvTitle.setText("快捷支付（支付宝）");
        tvHandleAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeAmount();
            }
        });
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                PosTopupService.get().saveOrUpdate(mQuickPayInfo, outTradeNo,
                        WayType.ALI_F2F, PayStatus.CANCELED);
                if (mListener != null) {
                    mListener.onPayCanceled();
                }
            }
        });

        btnQueryOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryOrder(outTradeNo);
            }
        });
        btnCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder(outTradeNo);
            }
        });
        initAuthCodeInput();

        setContent(rootView, 0);
    }

    public AlipayDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }

//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();

        etAuthCode.requestFocus();

        if (mQuickPayInfo == null) {
            dismiss();
        }
    }


    private void initAuthCodeInput() {
        etAuthCode.requestFocus();
        etAuthCode.setFocusable(true);
        etAuthCode.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        etAuthCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etAuthCode);
                }
                etAuthCode.requestFocus();
                etAuthCode.setSelection(etAuthCode.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etAuthCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submitOrder();

                        etAuthCode.requestFocus();
                        etAuthCode.setSelection(etAuthCode.length());
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
     * 修改金额
     */
    private void changeAmount() {
        if (changePriceDialog == null) {
            changePriceDialog = new DoubleInputDialog(getContext());
            changePriceDialog.setCancelable(true);
            changePriceDialog.setCanceledOnTouchOutside(true);
        }
        changePriceDialog.initialzie("修改金额", 2, mQuickPayInfo.getAmount(), "元",
                new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        mQuickPayInfo.setAmount(quantity);

                        tvHandleAmount.setText(String.format(Locale.getDefault(), "%.2f", mQuickPayInfo.getAmount()));
                    }
                });
        changePriceDialog.setMinimumDoubleCheck(mQuickPayInfo.getMinAmount(), true);
        changePriceDialog.show();
    }

    /**
     * 支付宝支付--创建支付订单
     * 1_100014_1445935035219
     */
    private JSONObject generateOrderInfo(Double paidAmount, String authCode) {
        // 商户订单号
        outTradeNo = CashierFactory.genTradeNo(mQuickPayInfo.getBizType(),
                WayType.ALI_F2F, 0L, true);
        ZLogger.d(String.format("支付宝支付－交易编号：%s", outTradeNo));

        JSONObject orderInfo = new JSONObject();
        orderInfo.put("out_trade_no", outTradeNo);
        orderInfo.put("scene", "bar_code");
        orderInfo.put("auth_code", authCode);
        orderInfo.put("total_amount", MUtils.formatDouble(paidAmount, ""));
//        orderInfo.put("discountable_amount", MStringUtil.formatAmount(discountableAmount));
        orderInfo.put("subject", mQuickPayInfo.getSubject());
        orderInfo.put("body", mQuickPayInfo.getBody());
        orderInfo.put("operator_id", MfhLoginService.get().getHumanId());//商户操作员编号
        orderInfo.put("store_id", MfhLoginService.get().getCurOfficeId());//商户门店编号
        orderInfo.put("terminal_id", SharedPrefesManagerFactory.getTerminalId());
        orderInfo.put("seller_id", MfhLoginService.get().getSpid());//租户ID
//        Human member = GlobalInstance.getInstance().getMfMemberInfo();
//        if (member != null) {
//            orderInfo.put("seller_id", member.getGuid());
//        }

        return orderInfo;
    }

    /**
     * 支付宝条码支付--POS发起支付请求，后台向支付宝请求支付<br>
     * <b>应用场景实例：</b>收银员使用扫码设备读取用户手机支付宝“付款码”后，将二维码或条码信息通过本接口上送至支付宝发起支付。<br>
     * 免密支付,直接返回支付结果，
     * 验密支付,返回10003(支付处理中)状态,然后POS轮询查询订单状态
     */
    private void submitOrder() {
        if (bPayProcessing) {
            ZLogger.d("正在进行支付，不用重复发起请求");
            return;
        }

//        支付授权码(条码)
        String authCode = etAuthCode.getText().toString();
        if (StringUtils.isEmpty(authCode)) {
            DialogUtil.showHint("请输入授权码");
            enterStandardMode();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            enterStandardMode();
            return;
        }

        bPayProcessing = true;
        onProcessing("正在发送支付请求...");

        ZLogger.i(String.format("支付宝条码支付：金额:%.2f, 授权码：%s, 业务类型：%s",
                mQuickPayInfo.getAmount(), authCode, mQuickPayInfo.getBizType()));
        JSONObject jsonStr = generateOrderInfo(mQuickPayInfo.getAmount(), authCode);

        PosTopupService.get().saveOrUpdate(mQuickPayInfo, outTradeNo,
                WayType.ALI_F2F, PayStatus.PROCESS);


        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonStr.toJSONString());
        options.put("bizType", String.valueOf(mQuickPayInfo.getBizType()));
        options.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().alipayBarPay(options,
                new MSubscriber<MResponse<String>>() {
//
//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.ef("支付宝条码支付异常:" + e.toString());
//                        onBarpayFailed(e.getMessage(),
//                                AppHelper.getErrorTextColor(), true);
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        ZLogger.ef("支付宝条码支付异常:" + e.toString());
                        onBarpayFailed(e.getMessage(),
                                AppHelper.getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null){
                            ZLogger.w("支付宝支付失败");
                            onBarpayFailed("支付宝支付失败，无响应", AppHelper.getErrorTextColor(), false);
                            return;
                        }
                        ZLogger.d(String.format("支付宝条码支付:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));
                        switch (stringMResponse.getCode()) {
                            //{"code":"0","msg":"Success","version":"1","data":""}
                            //10000--业务处理成功（订单支付成功）
                            case ErrorCode.PAY.SUCCESS: {
                                onBarpayFinished(outTradeNo, "支付成功", Color.parseColor("#FE5000"));
                            }
                            break;
                            //下单成功等待用户输入密码
                            //{"code":"1","msg":" order success pay inprocess","version":"1","data":""}
                            //订单创建成功支付处理中(验密支付)
                            //10003，业务处理中,该结果码只有在条码支付请求 API 时才返回，代表付款还在进行中，需要调用查询接口查询最终的支付结果
                            // 条码支付请求 API 返回支付处理中(返回码 10003)时，此时若用户支付宝钱包在线则会唤起支付宝钱包的快捷收银台，
                            // 用户可输入密码支付。商户需要在设定的轮询时间内，通过订单查询 API 查询订单状态，若返回付款成功，则表示支付成功。
                            case ErrorCode.PAY.PASSWORD: {
                                queryOrder(outTradeNo);
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
     * 支付宝支付--轮询查询订单状态
     * <b>应用场景实例：</b>本接口提供支付宝支付订单的查询的功能，商户可以通过本接口主动查询订单状态，完成下一步的业务逻辑。<br>
     * 需要调用查询接口的情况：<br>
     * 1. 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；<br>
     * 2. 调用扫码支付支付接口后，返回系统错误或未知交易状态情况；<br>
     * 3. 调用扫码支付请求后，如果结果返回处理中（返回结果中的code等于10003）的状态；<br>
     * 4. 调用撤销接口API之前，需确认该笔交易目前支付状态。<br>
     */
    private void queryOrder(final String outTradeNo) {
        onProcessing("正在查询订单状态...");

        Map<String, String> options = new HashMap<>();
        options.put("out_trade_no", outTradeNo);
        options.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AliPayHttpManager.getInstance().query(options,
                new MSubscriber<MResponse<String>>() {

//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.ef("支付宝条码支付状态查询:" + e.toString());
//                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
//                        onBarpayFailed(e.getMessage(),
//                                AppHelper.getErrorTextColor(), true);
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        ZLogger.ef("支付宝条码支付状态查询:" + e.toString());
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onBarpayFailed(e.getMessage(),
                                AppHelper.getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            onBarpayFailed("支付宝条码支付状态查询，无响应", AppHelper.getErrorTextColor(), true);
                            return;
                        }
                        ZLogger.d(String.format("支付宝条码支付状态查询:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));

                        switch (stringMResponse.getCode()) {
                            //业务处理成功
                            // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                            case ErrorCode.SUCCESS:
                                onBarpayFinished(outTradeNo, "支付成功", AppHelper.getOkTextColor());
                                break;
                            //{"code":"-1","msg":"Success","version":"1","data":""}
                            // 支付结果不明确，需要收银员继续查询或撤单
                            case ErrorCode.PAY_ERROR:
                                onBarpayFailed(stringMResponse.getMsg(), AppHelper.getErrorTextColor(), true);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default:
                                onBarpayFailed(stringMResponse.getMsg(), AppHelper.getErrorTextColor(), false);
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
    private void cancelOrder(String outTradeNo) {
        onProcessing("正在发送撤单请求...");

        Map<String, String> options = new HashMap<>();
        options.put("out_trade_no", outTradeNo);
        options.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AliPayHttpManager.getInstance().cancelOrder(options,
                new MSubscriber<MResponse<String>>() {
//
//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.ef("撤单失败:" + e.toString());
//                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
//                        onBarpayFailed(e.getMessage(), AppHelper.getErrorTextColor(), true);
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        ZLogger.ef("撤单失败:" + e.toString());
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onBarpayFailed(e.getMessage(), AppHelper.getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            onBarpayFailed("支付宝条码支付取消订单，无响应", AppHelper.getErrorTextColor(), false);
                            return;
                        }
                        ZLogger.d(String.format("支付宝条码支付取消订单:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));

                        switch (stringMResponse.getCode()) {
                            case ErrorCode.SUCCESS:
                                onBarpayFailed("订单已取消", AppHelper.getOkTextColor(), false);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default: //-2
                                onBarpayFailed(stringMResponse.getMsg(), AppHelper.getErrorTextColor(), false);
                                break;
                        }
                    }
                });
    }

    /**
     * 正在取消支付订单
     * "正在取消支付订单..."
     */
    private void onProcessing(String msg) {
        btnSync.setEnabled(false);
        btnClose.setEnabled(false);
        etAuthCode.setEnabled(false);
        frameOperation.setVisibility(View.GONE);
        frameProcess.setVisibility(View.VISIBLE);
        tvProcess.setText(msg);
        tvProcess.setTextColor(Color.parseColor("#FF000000"));
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 支付成功
     */
    private void onBarpayFinished(final String outTradeNo,
                                  String msg, int color) {

        PosTopupService.get().saveOrUpdate(mQuickPayInfo, outTradeNo,
                WayType.ALI_F2F, PayStatus.FINISH);

        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enterStandardMode();
                dismiss();

                if (mListener != null) {
                    mListener.onPaySucceed(mQuickPayInfo, outTradeNo);
                }
            }
        }, 500);
    }

    /**
     * 交易失败
     */
    private void onBarpayFailed(String msg, int color, boolean isException) {
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);
        if (isException) {
            PosTopupService.get().saveOrUpdate(mQuickPayInfo, outTradeNo,
                    WayType.ALI_F2F, PayStatus.EXCEPTION);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enterExceptionMode();
                }
            }, 2000);

        } else {
            PosTopupService.get().saveOrUpdate(mQuickPayInfo, outTradeNo,
                    WayType.ALI_F2F, PayStatus.FAILED);
            PosTopupService.get().saveOrUpdate(mQuickPayInfo, outTradeNo,
                    WayType.ALI_F2F, PayStatus.EXCEPTION);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enterStandardMode();
                }
            }, 2000);
        }
    }

    /**
     * 初始化
     */
    public void initialize(QuickPayInfo quickPayInfo, boolean isCancelAbled,
                           DialogClickListener callback) {
        initialize(quickPayInfo, isCancelAbled, true, callback);
    }

    public void initialize(QuickPayInfo quickPayInfo, boolean isCancelAbled, boolean isRefreshEnabled,
                           DialogClickListener callback) {
        this.mQuickPayInfo = quickPayInfo;
        this.mListener = callback;
        if (quickPayInfo != null) {
            this.tvTitle.setText(quickPayInfo.getSubject());
            this.tvSubTitle.setText(quickPayInfo.getBody());
            this.tvHandleAmount.setText(String.format(Locale.getDefault(), "%.2f", mQuickPayInfo.getAmount()));
        }

        if (isCancelAbled) {
            this.btnClose.setVisibility(View.VISIBLE);
        } else {
            this.btnClose.setVisibility(View.GONE);
        }

        if (isRefreshEnabled) {
            this.btnSync.setVisibility(View.VISIBLE);
        } else {
            this.btnSync.setVisibility(View.GONE);
        }

        enterStandardMode();
    }


    /**
     * 标准
     */
    private void enterStandardMode() {
        bPayProcessing = false;
        btnSync.setEnabled(true);
        btnClose.setEnabled(true);

        etAuthCode.getText().clear();//清空授权码
        etAuthCode.setEnabled(true);
        etAuthCode.requestFocus();
        frameProcess.setVisibility(View.GONE);
        frameOperation.setVisibility(View.GONE);
    }

    /**
     * 支付
     */
    private void enterPayMode() {
        bPayProcessing = true;
        btnSync.setEnabled(false);
        btnClose.setEnabled(false);
    }

    /**
     * 异常
     */
    private void enterExceptionMode() {
        bPayProcessing = false;
        btnSync.setEnabled(true);
        btnClose.setEnabled(true);

        etAuthCode.getText().clear();
        etAuthCode.setEnabled(true);
        etAuthCode.requestFocus();
        frameProcess.setVisibility(View.GONE);
        frameOperation.setVisibility(View.VISIBLE);
    }

    private void reload() {
        if (mQuickPayInfo == null) {
            return;
        }

        Integer subBizType = mQuickPayInfo.getSubBizType();
        if (BizType.CASH_QUOTA.equals(subBizType)) {
            needLockPos();
        } else {
            haveNoMoneyEnd();
        }
    }

    /**
     * 针对当前用户所属网点判断是否存在过清分时余额不足情况
     * /analysisAccDate/haveNoMoneyEnd?date=2016-02-02
     *
     * @param request date可空,默认是昨天。代表昨天包括昨天以前的时间内有无存在余额不足情况。
     */
    private void haveNoMoneyEnd() {
        onProcessing("加载数据...");
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            enterStandardMode();
            return;
        }

        RxHttpManager.getInstance().haveNoMoneyEnd(MfhLoginService.get().getCurrentSessionId(),
                new MValueSubscriber<String>() {

                    @Override
                    public void onError(Throwable e) {
                        enterStandardMode();
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        if (data != null) {
                            try {
                                Double amount = Double.valueOf(data);
                                if (amount >= 0.01) {
                                    if (mQuickPayInfo != null) {
                                        mQuickPayInfo.setMinAmount(amount);
                                        mQuickPayInfo.setAmount(amount);
                                        tvHandleAmount.setText(String.format(Locale.getDefault(), "%.2f", mQuickPayInfo.getAmount()));
                                    }
                                    enterStandardMode();
                                } else {
                                    ZLogger.i2f(String.format("清分完成: %.2f, 可以正常使用POS机", amount));
                                    dismiss();
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                enterStandardMode();
                            }
                        } else {
                            enterStandardMode();
                        }
                    }

                });
    }


    /**
     * 判断是否需要锁定pos
     */
    private void needLockPos() {
        onProcessing("加载数据...");
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {

            enterStandardMode();
            return;
        }

        RxHttpManager.getInstance().needLockPos(MfhLoginService.get().getCurrentSessionId(),
                MfhLoginService.get().getCurOfficeId(),
                new MSubscriber<String>() {

//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.ef("判读是否锁定POS机失败：" + e.toString());
//                        enterStandardMode();
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        ZLogger.ef("判读是否锁定POS机失败：" + e.toString());
                        enterStandardMode();
                    }

                    @Override
                    public void onNext(String data) {
                        ZLogger.d("判断是否需要锁定POS机:" + data);

                        if (StringUtils.isEmpty(data)){
                            enterStandardMode();

                            return;
                        }
                        String[] ret = data.split(",");
                        if (ret.length >= 2) {
//                                Boolean.parseBoolean()1
//                                boolean isNeedLock = Boolean.valueOf(ret[0]).booleanValue();
                            boolean isNeedLock = Boolean.parseBoolean(ret[0]);
                            Double amount = Double.valueOf(ret[1]);

//                            ZLogger.df(String.format("判断是否需要锁定POS机，isNeedLock=%b, amount=%.2f",
//                                    isNeedLock, amount));
                            if (isNeedLock && amount >= 0.01) {
                                if (mQuickPayInfo != null) {
                                    mQuickPayInfo.setMinAmount(amount);
                                    mQuickPayInfo.setAmount(amount);
                                    tvHandleAmount.setText(String.format(Locale.getDefault(),
                                            "%.2f", mQuickPayInfo.getAmount()));
                                }
                                enterStandardMode();
                            } else {
                                dismiss();
                            }
                        } else {
                            enterStandardMode();

                        }
                    }
                });
    }
}

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
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.net.ResponseBody;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.PayApi;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.EmptyEntity;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;


/**
 * <h1>账号操作：锁定/交接班/登录/退出</h1><br>
 *
 * 1.支付完成 {@link DialogClickListener#onPaySucceed()}<br>
 * 2.支付异常 {@link DialogClickListener#onPayException()}<br>
 *
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class AlipayDialog extends CommonDialog {

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private TextView tvHandleAmount;
    private EditText etAuthCode;
    private LinearLayout frameOperation;
    private Button btnQueryOrder, btnCancelOrder;
    private LinearLayout frameProcess;
    private ProgressBar progressBar;
    private TextView tvProcess;

    private CashierOrderInfo cashierOrderInfo;
    protected int payType;//支付类型
//    protected Double handleAmount = 0D;//应收金额
    protected Double paidAmount = 0D;//实收金额
    protected Double rechargeAmount = 0D;//找零金额
//    private String subject;//订单标题
//    private String body;//对交易或商品的描述
//    private String orderId;//设备号＋订单编号
    //设备号＋订单编号＋时间
    private String outTradeNo;//商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
//    private String bizType;

    private boolean bPayProcessing = false;


    public interface DialogClickListener {
        /**支付处理中*/
        void onPayProcess(Double amount, String outTradeNo);
        /**支付完成*/
        void onPaySucceed(Double amount, String outTradeNo);
        /**支付异常*/
        void onPayException(Double amount, String outTradeNo);
        /**支付失败*/
        void onPayFailed(Double amount, String outTradeNo);
        /**取消支付*/
        void onPayCanceled();
    }


    private DialogClickListener mListener;

    private AlipayDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private AlipayDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_alipay, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        tvHandleAmount = (TextView) rootView.findViewById(R.id.tv_handle_amount);
        etAuthCode = (EditText) rootView.findViewById(R.id.et_authcode);
        frameOperation = (LinearLayout) rootView.findViewById(R.id.frame_operation);
        btnQueryOrder = (Button) rootView.findViewById(R.id.button_query_orderstatus);
        btnCancelOrder = (Button) rootView.findViewById(R.id.button_cancel_order);
        frameProcess = (LinearLayout) rootView.findViewById(R.id.frame_process);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        tvProcess = (TextView) rootView.findViewById(R.id.tv_process);

        tvTitle.setText("快捷支付（支付宝）");
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onPayCanceled();
                }
            }
        });

        btnQueryOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryOrder(outTradeNo, cashierOrderInfo.getHandleAmount());
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

        getWindow().setGravity(Gravity.CENTER);

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
//        ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
//        tvUsername.setText(MfhLoginService.get().getHumanName());
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
                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));
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
     * 支付宝支付--创建支付订单
     * 1_100014_1445935035219
     */
    private JSONObject generateOrderInfo(Double paidAmount, String authCode) {
        // 商户订单号
        outTradeNo = String.format("%s_%d", cashierOrderInfo.getOrderId(), System.currentTimeMillis());

        JSONObject orderInfo = new JSONObject();
        orderInfo.put("out_trade_no", outTradeNo);
        orderInfo.put("scene", "bar_code");
        orderInfo.put("auth_code", authCode);
        orderInfo.put("total_amount", MUtils.formatDouble(paidAmount, ""));
//        orderInfo.put("discountable_amount", MStringUtil.formatAmount(discountableAmount));
        orderInfo.put("subject", cashierOrderInfo.getSubject());
        orderInfo.put("body", cashierOrderInfo.getBody());
        orderInfo.put("operator_id", MfhLoginService.get().getCurrentGuId());//商户操作员编号
        orderInfo.put("store_id", MfhLoginService.get().getCurOfficeId());//商户门店编号
        orderInfo.put("terminal_id", SharedPreferencesManager.getTerminalId());
        orderInfo.put("seller_id", MfhLoginService.get().getSpid());//租户ID
//        Human member = DataCacheHelper.getInstance().getMfMemberInfo();
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
            ZLogger.df("正在进行支付，不用重复发起请求");
            return;
        }

//        支付授权码(条码)
        String authCode = etAuthCode.getText().toString();
        if (StringUtils.isEmpty(authCode)) {
            bPayProcessing = false;
            DialogUtil.showHint("请输入授权码");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            bPayProcessing = false;
            return;
        }

        bPayProcessing = true;
        onBarpayProcessing("正在发送支付请求...");

        //TODO,调用支付宝支付接口
//        {"code":"0","msg":"操作成功!","version":"1","data":{"code":"40004","msg":"错误码：ACQ.CONTEXT_INCONSISTENT错误描述：支付失败，商户订单号重复，请收银员取消本笔交易并重新收款。[CONTEXT_INCONSISTENT]"}}
        NetCallBack.RawNetTaskCallBack payRespCallback = new NetCallBack.RawNetTaskCallBack<EmptyEntity,
                NetProcessor.RawProcessor<EmptyEntity>>(
                new NetProcessor.RawProcessor<EmptyEntity>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                    }

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("支付宝条码支付:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));
                        switch (rspBody.getRetCode()){
                            //{"code":"0","msg":"Success","version":"1","data":""}
                            //10000--业务处理成功（订单支付成功）
                            case "0":{
                                onBarpayFinished(cashierOrderInfo.getHandleAmount(), outTradeNo,
                                        "支付成功", Color.parseColor("#FE5000"));
                            }
                            break;
                            //下单成功等待用户输入密码
                            //{"code":"1","msg":" order success pay inprocess","version":"1","data":""}
                            //{"code":"1","msg":"错误的pos订单号:4_98_1000004_1456788686475","version":"1","data":null}
                            //订单创建成功支付处理中(验密支付)
                            //10003，业务处理中,该结果码只有在条码支付请求 API 时才返回，代表付款还在进行中，需要调用查询接口查询最终的支付结果
                            // 条码支付请求 API 返回支付处理中(返回码 10003)时，此时若用户支付宝钱包在线则会唤起支付宝钱包的快捷收银台，
                            // 用户可输入密码支付。商户需要在设定的轮询时间内，通过订单查询 API 查询订单状态，若返回付款成功，则表示支付成功。
                            case "1":{
                                queryOrder(outTradeNo, cashierOrderInfo.getHandleAmount());
                            }
                            break;
                            //{"code":"1","msg":"bizType参数不能为空!","version":"1","data":null}
                            ////交易创建失败
                            //40004--错误码：ACQ.INVALID_PARAMETER错误描述：支付失败，交易参数异常，请顾客刷新付款码后重新收款。如再次收款失败，请联系管理员处理。[INVALID_PARAMETER]
                            //40004--错误码：ACQ.PAYMENT_AUTH_CODE_INVALID错误描述：支付失败，获取顾客账户信息失败，请顾客刷新付款码后重新收款，如再次收款失败，请联系管理员处理。[SOUNDWAVE_PARSER_FAIL]
                            default:{//-1
                                onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FF009B4E"), false);
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
                        ZLogger.df("支付宝条码支付异常:" + errMsg);
                        onBarpayFailed(errMsg, Color.parseColor("#FE5000"), true);
                    }
                }
                , EmptyEntity.class
                , CashierApp.getAppContext()) {
        };

        ZLogger.df(String.format("支付宝条码支付：支付金额:%.2f, 授权码：%s, 业务类型：%s",
                cashierOrderInfo.getHandleAmount(), authCode, cashierOrderInfo.getBizType()));
        JSONObject jsonStr = generateOrderInfo(cashierOrderInfo.getHandleAmount(), authCode);
        if (mListener != null){
            mListener.onPayProcess(cashierOrderInfo.getHandleAmount(), outTradeNo);
        }
        PayApi.aliBarPay(jsonStr.toJSONString(),
                String.valueOf(cashierOrderInfo.getBizType()), payRespCallback);
    }

    /**
     * 支付宝支付--轮询查询订单状态
     * <b>应用场景实例：</b>本接口提供支付宝支付订单的查询的功能，商户可以通过本接口主动查询订单状态，完成下一步的业务逻辑。<br>
     * 需要调用查询接口的情况：<br>
     * 1. 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；<br>
     * 2. 调用扫码支付支付接口后，返回系统错误或未知交易状态情况；<br>
     * 3. 调用扫码支付请求后，如果结果返回处理中（返回结果中的code等于10003）的状态；<br>
     * 4. 调用撤销接口API之前，需确认该笔交易目前支付状态。<br>
     * */
    private void queryOrder(final String outTradeNo, final Double paidAmount) {
        NetCallBack.RawNetTaskCallBack payRespCallback = new NetCallBack.RawNetTaskCallBack<EmptyEntity,
                NetProcessor.RawProcessor<EmptyEntity>>(
                new NetProcessor.RawProcessor<EmptyEntity>() {

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("支付宝条码支付状态查询:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));

                        switch (rspBody.getRetCode()) {
                            //业务处理成功
                            // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                            case "0":
                                onBarpayFinished(paidAmount, outTradeNo, "支付成功", Color.parseColor("#FE5000"));
                                break;
                            //{"code":"-1","msg":"Success","version":"1","data":""}
                            // 支付结果不明确，需要收银员继续查询或撤单
                            case "-1":
                                onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FF009B4E"), true);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default:
                                onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FF009B4E"), false);
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
                        //TODO 调用支付宝支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onBarpayFailed(errMsg, Color.parseColor("#FE5000"), true);
                    }
                }
                , EmptyEntity.class
                , CashierApp.getAppContext()) {
        };

        onBarpayProcessing("正在查询订单状态...");
        PayApi.queryAliBarpayStatus(outTradeNo, payRespCallback);
    }

    /**
     * 支付宝支付--撤单
     * <b>应用场景实例：</b>调用支付宝支付接口时未返回明确的返回结果（如系统错误或网络异常），可使用本接口将交易进行撤销。<br>
     * 如果用户支付失败，支付宝会将此订单关闭；如果用户支付成功，支付宝会将支付的资金退还给用户。<br>
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
                        onBarpayFailed(errMsg, Color.parseColor("#FE5000"), true);
                    }

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("支付宝条码支付取消订单:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));

                        //业务处理成功
                        // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                        if (rspBody.getRetCode().equals("0")) {
                            onBarpayFailed("订单已取消", Color.parseColor("#FE5000"), false);
                        }
                        //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                        //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                        //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                        // 处理失败,交易不存在
                        //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                        else {//-1
                            onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FE5000"), true);
                        }
                    }
                }
                , EmptyEntity.class
                , CashierApp.getAppContext()) {
        };

        PayApi.cancelAliBarpay(outTradeNo, payRespCallback);
    }

    /**
     * 正在取消支付订单
     * "正在取消支付订单..."
     */
    private void onBarpayProcessing(String msg) {
        etAuthCode.setVisibility(View.GONE);
        frameOperation.setVisibility(View.GONE);
        frameProcess.setVisibility(View.VISIBLE);
        tvProcess.setText(msg);
        tvProcess.setTextColor(Color.parseColor("#FF000000"));
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 支付成功
     * */
    private void onBarpayFinished(final Double paidAmount, final String outTradeNo,
                                  String msg, int color) {
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);

        etAuthCode.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mListener != null){
                    mListener.onPaySucceed(paidAmount, outTradeNo);
                }

                frameOperation.setVisibility(View.GONE);
                bPayProcessing = false;
                dismiss();
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
        if (isException){
            frameOperation.setVisibility(View.VISIBLE);
            if (mListener != null){
                mListener.onPayException(cashierOrderInfo.getHandleAmount(), outTradeNo);
            }
        }else{
            frameOperation.setVisibility(View.GONE);
            if (mListener != null){
                mListener.onPayFailed(cashierOrderInfo.getHandleAmount(), outTradeNo);
            }
        }

        etAuthCode.getText().clear();//清空授权码



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etAuthCode.setVisibility(View.VISIBLE);
                etAuthCode.requestFocus();
                frameProcess.setVisibility(View.GONE);

                bPayProcessing = false;
            }
        }, 2000);
    }

    public void init(CashierOrderInfo cashierOrderInfo, DialogClickListener callback) {
        this.cashierOrderInfo = cashierOrderInfo;
        this.mListener = callback;

        this.tvHandleAmount.setText(String.format("%.2f", cashierOrderInfo.getHandleAmount()));
    }
}

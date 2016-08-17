package com.manfenjiayuan.business.dialog;


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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.R;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.widget.FontFitTextView;

/**
 * <h1>快捷帐号支付：采购收货单</h1><br>
 *
 * 1.支付完成 {@link DialogClickListener#onPaySucceed()}<br>
 * 2.支付异常 {@link DialogClickListener#onPayException()}<br>
 *
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class AccountQuickPayDialog extends CommonDialog {

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private FontFitTextView tvHandleAmount;
    private EditText etPassword;
    private LinearLayout frameProcess;
    private ProgressBar progressBar;
    private TextView tvProcess;

    private int dialogType = 0;//默认为0，支付订单；1-签收并支付调拨单
    private String orderId;
    private Double handleAmount;

    private boolean bPayProcessing = false;

    public interface DialogClickListener {
        /**支付完成*/
        void onPaySucceed();
        /**支付失败*/
        void onPayFailed();
        /**取消支付*/
        void onPayCanceled();
    }

    private DialogClickListener mListener;

    private AccountQuickPayDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private AccountQuickPayDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_quickpay_account, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        tvHandleAmount = (FontFitTextView) rootView.findViewById(R.id.tv_handle_amount);
        etPassword = (EditText) rootView.findViewById(R.id.et_password);
        frameProcess = (LinearLayout) rootView.findViewById(R.id.frame_process);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        tvProcess = (TextView) rootView.findViewById(R.id.tv_process);

        tvTitle.setText("快捷支付（账号）");
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onPayCanceled();
                }
            }
        });

        initAuthCodeInput();
        tvHandleAmount.setText("");

        setContent(rootView, 0);
    }

    public AccountQuickPayDialog(Context context) {
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

        etPassword.requestFocus();
//        ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
//        tvUsername.setText(MfhLoginService.get().getHumanName());
    }

    /**
     * 初始化
     * */
    public void init(String orderId, Double handleAmount, DialogClickListener callback) {
        init(0, orderId, handleAmount, callback);
    }

    public void init(int dialogType, String orderId, Double handleAmount,  DialogClickListener callback) {
        this.dialogType = dialogType;
        this.orderId = orderId;
        this.handleAmount = handleAmount;
        this.mListener = callback;

        this.tvHandleAmount.setText(MUtils.formatDouble(handleAmount, ""));
        this.bPayProcessing = false;
    }

    private void initAuthCodeInput() {
        etPassword.requestFocus();
        etPassword.setFocusable(true);
        etPassword.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(MfhApplication.getAppContext(), etPassword);
                }
                etPassword.requestFocus();
                etPassword.setSelection(etPassword.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d",
                        keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submitOrder();

                        etPassword.requestFocus();
                        etPassword.setSelection(etPassword.length());
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
        String password = etPassword.getText().toString();
        if (StringUtils.isEmpty(password)) {
            bPayProcessing = false;
            DialogUtil.showHint("请输入支付密码");
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            bPayProcessing = false;
            return;
        }

        onBarpayProcessing("正在发送支付请求...");

        //支付并签收调拨单
        if (dialogType == 1){
            InvSendIoOrderApiImpl.doReceiveInvSendIoOrder(orderId, password, receiveResponseCallback);
        }
        //支付订单
        else {
            InvSendIoOrderApiImpl.doPayInvSendIoOrder(orderId, password, payResponseCallback);
        }
    }

    private final NetCallBack.NetTaskCallBack payResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                            {"code":"0","msg":"操作成功!","version":"1","data":""}
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    String retStr = retValue.getValue();

                    //出库成功:1-556637
                    ZLogger.d("支付采购配送单成功:" + retStr);
                    bPayProcessing = false;

                    onBarpayFinished("支付成功", Color.parseColor("#FE5000"));
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知
                    ZLogger.d("支付采购配送单失败:" + errMsg);
                    bPayProcessing = false;
                    onBarpayFailed(errMsg, Color.parseColor("#FE5000"));
//                        onBarpayFinished(Constants.PAYTYPE_MFACCOUNT, paidAmount, "支付成功", Color.parseColor("#FE5000"));
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    NetCallBack.NetTaskCallBack receiveResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                            {"code":"0","msg":"操作成功!","version":"1","data":""}
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    String retStr = retValue.getValue();

                    //出库成功:1-556637
                    ZLogger.d("调拨单入库成功:" + retStr);

                    bPayProcessing = false;

                    onBarpayFinished("支付成功", Color.parseColor("#FE5000"));
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("调拨单入库失败：" + errMsg);
                    bPayProcessing = false;
                    onBarpayFailed(errMsg, Color.parseColor("#FE5000"));
//                        onBarpayFinished(Constants.PAYTYPE_MFACCOUNT, paidAmount, "支付成功", Color.parseColor("#FE5000"));

                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 正在取消支付订单
     * "正在取消支付订单..."
     */
    private void onBarpayProcessing(String msg) {
        bPayProcessing = true;
        etPassword.setVisibility(View.GONE);
        frameProcess.setVisibility(View.VISIBLE);
        tvProcess.setText(msg);
        tvProcess.setTextColor(Color.parseColor("#FF000000"));
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 支付成功
     * */
    private void onBarpayFinished(String msg, int color) {
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);

        etPassword.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mListener != null){
                    mListener.onPaySucceed();
                }

                etPassword.setVisibility(View.VISIBLE);
                etPassword.requestFocus();
                frameProcess.setVisibility(View.GONE);
                bPayProcessing = false;
                dismiss();
            }
        }, 500);
    }

    /**
     * 交易失败
     */
    private void onBarpayFailed(String msg, int color) {
        tvProcess.setText(msg);
        tvProcess.setTextColor(color);
        progressBar.setVisibility(View.GONE);
        if (mListener != null){
            mListener.onPayFailed();
        }

        etPassword.getText().clear();//清空授权码

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etPassword.setVisibility(View.VISIBLE);
                etPassword.requestFocus();
                frameProcess.setVisibility(View.GONE);

                bPayProcessing = false;
            }
        }, 2000);
    }

}

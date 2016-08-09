package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.io.ByteArrayOutputStream;

/**
 * <p>
 * 对话框 -- 门店用户注册
 * <li>Step 1: 输入手机号，查询用户是否注册满分。</li>
 * <li>Step 2: 如果用户已经注册满分，则发送验证码给用户。（再次发送验证码需要等待60秒）</li>
 * <li>Step 3: 输入用户收到的验证码，然后点击‘验证’按钮验证是否正确。</li>
 * </p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ValidatePhonenumberDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle, tvTip;
    private ImageButton btnClose;
    private EditText etPhoneNumber, etVerifyCode;
    private Button btnVerifyCode, btnSubmit;

    private ProgressBar progressBar;

    private Long userTmpId = null;

    public interface OnValidateListener{
        void onSuccess(String phonenumber);
        void onError();
    }
    private OnValidateListener mOnValidateListener;

    private ValidatePhonenumberDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ValidatePhonenumberDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_validate_phonenumber, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTip = (TextView) rootView.findViewById(R.id.tv_tip);
        etPhoneNumber = (EditText) rootView.findViewById(R.id.et_phoneNumber);
        btnVerifyCode = (Button) rootView.findViewById(R.id.button_get_verifycode);
        etVerifyCode = (EditText) rootView.findViewById(R.id.et_verifyCode);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

        tvTitle.setText("验证手机号");

        etPhoneNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etPhoneNumber);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etPhoneNumber);
                    }
                }
                etPhoneNumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPhoneNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        getVerifyCode();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });

        etVerifyCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etVerifyCode);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etVerifyCode);
                    }
                }
                etVerifyCode.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etVerifyCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submit();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etVerifyCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVerifyCode();
            }
        });
        btnSubmit.setText("下一步");
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public ValidatePhonenumberDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);


        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = d.getHeight();
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

        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void initialize(OnValidateListener onValidateListener) {
        this.mOnValidateListener = onValidateListener;
        firstStep();
    }

    /**
     * <ol>
     *     第一步，输入手机号
     *     <li>手机号验证成功后，自动发送验证码</li>
     * </ol>
     */
    private void firstStep() {
//        etPhoneNumber.setEnabled(true);
        etPhoneNumber.getText().clear();
        etPhoneNumber.requestFocus();

        btnVerifyCode.setText("获取验证码");
        btnVerifyCode.setEnabled(true);
        userTmpId = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        etVerifyCode.setEnabled(false);
        etVerifyCode.getText().clear();

        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 第二步，输入验证码
     */
    private void secondStep() {
//        etPhoneNumber.setEnabled(false);

//        userTmpId = null;
//        if (countDownTimer != null) {
//            countDownTimer.cancel();
//            countDownTimer = null;
//        }
//        btnVerifyCode.setEnabled(false);
//        btnVerifyCode.setText("获取验证码");

        etVerifyCode.requestFocus();
        etVerifyCode.getText().clear();
        etVerifyCode.setEnabled(true);

        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 获取验证码
     */
    private void getVerifyCode() {
        btnVerifyCode.setEnabled(false);
        final String phoneNumber = etPhoneNumber.getText().toString();
        if (StringUtils.isEmpty(phoneNumber)) {
            DialogUtil.showHint("请输入手机号");
            btnVerifyCode.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnVerifyCode.setEnabled(true);
            return;
        }

        if (countDownTimer == null) {
            countDownTimer = new VerifyCodeCountDownTimer(60 * 1000, 1000);
        }
        countDownTimer.start();
        progressBar.setVisibility(View.VISIBLE);

        if (userTmpId == null) {
            CashierApiImpl.beginAuthenBysms(phoneNumber, verifyCodeCallback);
        } else {
            CashierApiImpl.retryAuthenBysms(phoneNumber, userTmpId, verifyCodeCallback);
        }
    }


    private NetCallBack.NetTaskCallBack verifyCodeCallback = new NetCallBack.NetTaskCallBack<Long,
            NetProcessor.Processor<Long>>(
            new NetProcessor.Processor<Long>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    if (rspData != null) {
                        RspValue<Long> retValue = (RspValue<Long>) rspData;
                        userTmpId = retValue.getValue();
                        ZLogger.d(String.format("发送验证码成功:%d", userTmpId));
                        tvTip.setVisibility(View.VISIBLE);
                    }

                    progressBar.setVisibility(View.GONE);
                    secondStep();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                    ZLogger.e(String.format("发送验证码失败:%s", errMsg));
                    DialogUtil.showHint("发送验证码失败");
                    progressBar.setVisibility(View.GONE);
                }
            }
            , Long.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 下一步：验证验证码是否输入正确
     */
    private void submit() {
        btnSubmit.setEnabled(false);
        etVerifyCode.setEnabled(false);
        final String phoneNumber = etPhoneNumber.getText().toString();
        if (StringUtils.isEmpty(phoneNumber)) {
            DialogUtil.showHint("请输入手机号");
            firstStep();
            return;
        }

        String verifyCode = etVerifyCode.getText().toString();
        if (StringUtils.isEmpty(verifyCode)) {
            DialogUtil.showHint("请输入验证码");
            secondStep();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            etVerifyCode.setEnabled(true);
            btnSubmit.setEnabled(true);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        NetCallBack.NetTaskCallBack authCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(final IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":""}
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            ZLogger.d(String.format("验证码验证成功:%s", retValue.getValue()));
                        }

                        progressBar.setVisibility(View.GONE);

                        dismiss();

                        if (mOnValidateListener != null){
                            mOnValidateListener.onSuccess(phoneNumber);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                        //{"code":"1","msg":"短信验证码验证不对，请重新输入!","version":"1","data":null}
                        ZLogger.e(String.format("短信码验证验证失败:%s", errMsg));
                        DialogUtil.showHint("短信码验证验证失败,请重新输入");
                        progressBar.setVisibility(View.GONE);
                        secondStep();
//                        btnVerifyCode.setVisibility(View.VISIBLE);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.doAuthenBysms(verifyCode, userTmpId, authCallback);
    }


    private VerifyCodeCountDownTimer countDownTimer = null;

    /**
     * 倒计时
     */
    public class VerifyCodeCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public VerifyCodeCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btnVerifyCode.setText(String.format("%d秒重试", millisUntilFinished / 1000));
            btnVerifyCode.setEnabled(false);
        }

        @Override
        public void onFinish() {
            btnVerifyCode.setText("获取验证码");
            btnVerifyCode.setEnabled(true);
        }
    }


    //16进制字符串转换为String
    private String hexString = "0123456789ABCDEF";

    public String decode(String bytes) {
        if (bytes.length() != 30) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    // 字符序列转换为16进制字符串
    private static String bytesToHexString(byte[] src, boolean isPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isPrefix) {
            stringBuilder.append("0x");
        }
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (byte aSrc : src) {
            buffer[0] = Character.toUpperCase(Character.forDigit(
                    (aSrc >>> 4) & 0x0F, 16));
            buffer[1] = Character.toUpperCase(Character.forDigit(aSrc & 0x0F,
                    16));
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}

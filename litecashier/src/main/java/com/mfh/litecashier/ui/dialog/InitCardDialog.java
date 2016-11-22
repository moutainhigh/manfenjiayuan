package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invOrder.CashierApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.ActivateAccountResult;
import com.mfh.litecashier.bean.Human;

import java.io.ByteArrayOutputStream;


/**
 * <p>
 *     对话框 -- 开卡
 *     <li>Step 1: 输入手机号，查询用户是否注册满分。</li>
 *     <li>Step 2: 如果用户已经注册满分，则发送验证码给用户。（再次发送验证码需要等待60秒）</li>
 *     <li>Step 3: 输入用户收到的验证码，然后点击‘验证’按钮验证是否正确。</li>
 * </p>
 *
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InitCardDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private ImageButton btnClose;
    private AvatarView ivHeader;
    private TextView tvUsername;
    private EditText etPhoneNumber, etVerifyCode, etCardNumber, etCardId;
    private Button btnVerifyCode, btnSubmit;

    private ProgressBar progressBar;

    private static final int STEP_PHONE         = 0;
    private static final int STEP_VERIFYCODE    = 1;
    private static final int STEP_CARDINFO      = 2;
    private int currentStep = STEP_PHONE;

    private Human mHuman = null;//会员信息
    private Long userTmpId = null;
    private boolean isVerifyCodeValidate = false;

    private InitCardDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private InitCardDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_initcard, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        tvUsername = (TextView) rootView.findViewById(R.id.tv_username);
        etPhoneNumber = (EditText) rootView.findViewById(R.id.et_phoneNumber);
        etCardNumber = (EditText) rootView.findViewById(R.id.et_card_number);
        etCardId = (EditText) rootView.findViewById(R.id.et_card_id);
        etVerifyCode = (EditText) rootView.findViewById(R.id.et_verifyCode);
        btnVerifyCode = (Button) rootView.findViewById(R.id.button_get_verifycode);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

        tvTitle.setText("开卡");
        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));

        etPhoneNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etPhoneNumber);
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
                        queryPhone();
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
                    DeviceUtils.hideSoftInput(getContext(), etVerifyCode);
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
                        authVerifyCode();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etCardNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etCardNumber);
                }
                etCardNumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etCardNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        etCardId.requestFocus();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etCardId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etCardId);
                }
                etCardId.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etCardId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (currentStep == STEP_CARDINFO){
                            submit();
                        }
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVerifyCode();
            }
        });
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

    public InitCardDialog(Context context) {
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

    public void initialize() {
        etVerifyCode.getText().clear();
        etCardNumber.getText().clear();
        etCardId.getText().clear();
        skipToStep(STEP_PHONE);
    }

    /**
     * 步骤*/
    private void skipToStep(int step){
        this.currentStep = step;
        if (step == STEP_PHONE){
            mHuman = null;
            ivHeader.setAvatarUrl("");
            tvUsername.setText("");

            etPhoneNumber.getText().clear();
            etPhoneNumber.setEnabled(true);
            etPhoneNumber.requestFocus();

            isVerifyCodeValidate = false;
            userTmpId = null;
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            etVerifyCode.getText().clear();
            etVerifyCode.setEnabled(false);
            btnVerifyCode.setText("发送验证码");
            btnVerifyCode.setEnabled(false);

            etCardNumber.getText().clear();
            etCardId.getText().clear();

            btnSubmit.setEnabled(false);
            progressBar.setVisibility(View.GONE);
        }
        else if (step == STEP_VERIFYCODE){
            isVerifyCodeValidate = false;
            userTmpId = null;
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            etVerifyCode.getText().clear();
            etVerifyCode.setEnabled(false);
            btnVerifyCode.setText("发送验证码");
            btnVerifyCode.setEnabled(true);

            btnSubmit.setEnabled(false);
        }
        else if (step == STEP_CARDINFO){
            isVerifyCodeValidate = true;
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            etVerifyCode.setEnabled(false);
            btnVerifyCode.setText("发送验证码");
            btnVerifyCode.setEnabled(false);

            etCardNumber.getText().clear();
            etCardNumber.requestFocus();
            etCardId.getText().clear();
            btnSubmit.setEnabled(true);
        }
    }
    /**
     * 刷新会员信息
     */
    private void saveHumanInfo(Human human) {
        if (human == null) {
            skipToStep(STEP_PHONE);
            return;
        }

        mHuman = human;
        ivHeader.setAvatarUrl(human.getHeadimageUrl());
        tvUsername.setText(human.getName());
//        etPhoneNumber.setEnabled(false);

        skipToStep(STEP_VERIFYCODE);

        //自动发送验证码
        getVerifyCode();
    }

    /**
     * */
    private void resetVerifyCode(boolean inputEnabled){

    }

    /**
     * 查询会员
     * */
    private void queryPhone(){
        String phoneNumber = etPhoneNumber.getText().toString();
        if (StringUtils.isEmpty(phoneNumber)){
            DialogUtil.showHint("请输入手机号");
            return;
        }

        if (phoneNumber.length() != 11) {
            DialogUtil.showHint("手机号格式不正确");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        CashierApiImpl.findHumanByPhone(phoneNumber, findMemberResponseCallback);
    }
    //查询会员信息
    private NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
            NetProcessor.Processor<Human>>(
            new NetProcessor.Processor<Human>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    if (rspData == null) {
                        DialogUtil.showHint("未查到会员信息");
                        skipToStep(STEP_PHONE);
                        return;
                    }

                    RspBean<Human> retValue = (RspBean<Human>) rspData;
                    saveHumanInfo(retValue.getValue());
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.e(String.format("未查到会员信息:%s", errMsg));
                    DialogUtil.showHint("未查到会员信息");
                    skipToStep(STEP_PHONE);
                }
            }
            , Human.class
            , CashierApp.getAppContext()) {
    };


    /**
     * 获取验证码
     */
    private void getVerifyCode() {
        btnVerifyCode.setEnabled(false);
        String phoneNumber = etPhoneNumber.getText().toString();
        if (StringUtils.isEmpty(phoneNumber)){
            DialogUtil.showHint("手机号不能为空");
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
        if (userTmpId == null){
            CashierApiImpl.beginAuthenBysms(phoneNumber, verifyCodeCallback);
        }
        else{
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
                    }

                    progressBar.setVisibility(View.GONE);
                    etVerifyCode.setEnabled(true);
                    etVerifyCode.requestFocus();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                    ZLogger.e(String.format("发送验证码失败:%s", errMsg));
                    DialogUtil.showHint("发送验证码失败");
                    progressBar.setVisibility(View.GONE);
//                    if (countDownTimer != null) {
//                        countDownTimer.cancel();
//                        countDownTimer = null;
//                    }
                }
            }
            , Long.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 验证验证码是否输入正确*/
    private void authVerifyCode(){
        etVerifyCode.setEnabled(false);
        String verifyCode = etVerifyCode.getText().toString();
        if (StringUtils.isEmpty(verifyCode)) {
            DialogUtil.showHint("请输入验证码");
            etVerifyCode.setEnabled(true);
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            etVerifyCode.setEnabled(true);
            return;
        }

        CashierApiImpl.doAuthenBysms(verifyCode, userTmpId, authCallback);
    }
    private NetCallBack.NetTaskCallBack authCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(final IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":""}
                    if (rspData!= null) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        ZLogger.d(String.format("验证码验证成功:%s", retValue.getValue()));
                    }

                    progressBar.setVisibility(View.GONE);
                    skipToStep(STEP_CARDINFO);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                    //{"code":"1","msg":"短信验证码验证不对，请重新输入!","version":"1","data":null}
                    ZLogger.e(String.format("短信码验证验证失败:%s", errMsg));
                    DialogUtil.showHint("短信码验证验证失败,请重新输入");
                    progressBar.setVisibility(View.GONE);
                    etVerifyCode.setEnabled(true);
                    etVerifyCode.getText().clear();
                    etVerifyCode.requestFocus();
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };



    /**
     * 提交
     */
    private void submit() {
        btnSubmit.setEnabled(false);

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        if (mHuman == null) {
            DialogUtil.showHint("请重新填写会员信息");
            btnSubmit.setEnabled(true);
            return;
        }

        if (!isVerifyCodeValidate){
            DialogUtil.showHint("请先验证手机号");
            btnSubmit.setEnabled(true);
            return;
        }

        String cardNumber = this.etCardNumber.getText().toString();
        if (StringUtils.isEmpty(cardNumber)) {
            DialogUtil.showHint("请输入卡号");
            btnSubmit.setEnabled(true);
            return;
        }

        String cardId = this.etCardId.getText().toString();
        if (StringUtils.isEmpty(cardId)) {
            DialogUtil.showHint("请重新读取磁卡信息");
            btnSubmit.setEnabled(true);
            return;
        }
        //十六进制：466CAF31
        ZLogger.d("cardId:" + cardId);
        //十进制：1181527857
        String cardId2;
        try {
            cardId2 = String.valueOf(Long.parseLong(cardId, 16));
            ZLogger.d("cardId:" + cardId2);

//            if (!StringUtils.isEmpty(cardId)){
//                String token = cardId.substring(6, 8) +
//                        cardId.substring(4, 6) + cardId.substring(2, 4) + cardId.substring(0, 2);
//                ZLogger.d("token:" + token);
////                ZLogger.d("token.decode:" + decode(rfidId));
//                ZLogger.d("token.long:" + String.valueOf(Long.parseLong(token, 16)));
//            }
        } catch (Exception e) {
            ZLogger.e(e.toString());

            DialogUtil.showHint("请重新读取磁卡信息");
            etCardId.getText().clear();
            etCardId.requestFocus();
            btnSubmit.setEnabled(true);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        CashierApiImpl.activateAccount(cardNumber, cardId2, mHuman.getGuid(), submitCallback);
    }

    //开卡
    private NetCallBack.NetTaskCallBack submitCallback = new NetCallBack.NetTaskCallBack<ActivateAccountResult,
            NetProcessor.Processor<ActivateAccountResult>>(
            new NetProcessor.Processor<ActivateAccountResult>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    ActivateAccountResult result = null;
                    if (rspData != null){
                        RspBean<ActivateAccountResult> retValue = (RspBean<ActivateAccountResult>) rspData;
                        result = retValue.getValue();
                    }

                    if (result == null){
                        DialogUtil.showHint("开卡失败");
                        btnSubmit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                    else{
                        ZLogger.d(String.format("开卡成功:%d-%d", result.getId(), result.getOwnerId()));
                        dismiss();
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"1181527857该卡已被他人使用，请重新确认！","version":"1","data":null}
                    ZLogger.e(String.format("开卡失败:%s", errMsg));
//                    DialogUtil.showHint("未查到会员信息");
//                    refreshHumanInfo(null);
                    btnSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            }
            , ActivateAccountResult.class
            , CashierApp.getAppContext()) {
    };

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
            btnVerifyCode.setText("发送验证码");
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

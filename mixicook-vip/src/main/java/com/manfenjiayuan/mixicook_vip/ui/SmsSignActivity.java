/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.manfenjiayuan.mixicook_vip.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.utils.AppHelper;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.api.sms.HumanAuthTempApi;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;


/**
 * 登录
 * A login screen that offers login via email/password.
 */
public class SmsSignActivity extends BaseActivity {
    private RelativeLayout loginFrame;
    private View loginFormView;
    private EditText etUserName;
    private EditText etPassword;
    private ImageButton ibDel;
    private Button btnSms;
    private Button btnSignIn;

    private View mProgressView;
    private Long userTmpId = null;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_signin_sms;
    }

//    @Override
//    protected boolean isBackKeyEnabled() {
//        return false;
//    }

    @Override
    protected boolean isDoubleClickExitEnabled() {
        return true;
    }

    @Override
    protected void doubleClickExit() {
        setResult(Activity.RESULT_CANCELED, null);
        super.doubleClickExit();
        AppHelper.getInstance().finshAllActivities();
        //退出应用后，会自动重启
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        hideSystemUI();
        super.onCreate(savedInstanceState);
        AppHelper.getInstance().addActivity(this);

        String lastUsername = MfhLoginService.get().getLastLoginName();
        if (!StringUtils.isEmpty(lastUsername)) {
            etUserName.setText(lastUsername);
            etPassword.requestFocus();
        } else {
            etUserName.requestFocus();
        }
    }

    @Override
    protected void initViews() {
        super.initViews();
        loginFrame = (RelativeLayout) findViewById(R.id.frame_login);
        loginFormView = findViewById(R.id.login_form);
        etUserName = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        mProgressView = findViewById(R.id.login_progress);
        ibDel = (ImageButton) findViewById(R.id.ib_del);
        btnSms = (Button) findViewById(R.id.button_sms);
        btnSignIn = (Button) findViewById(R.id.button_sign_in);

        loginFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

//        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });
        etUserName.setOnKeyListener(new EditText.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("setOnKeyListener(etPassword):keyCode=%d, action=%d",
                        event.getKeyCode(), event.getAction()));
//                etQuery.requestFocus();
//                etBarCode.setFocusableInTouchMode(true);
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        etPassword.requestFocus();
                        etPassword.setSelection(etPassword.length());
                    }
                    return true;
                }
                return false;
            }
        });
        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                userTmpId = null;
                if (s.length() > 0){
                    ibDel.setVisibility(View.VISIBLE);
                }
                else{
                    ibDel.setVisibility(View.INVISIBLE);
                }
            }
        });
        etPassword.setOnKeyListener(new EditText.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("setOnKeyListener(etPassword):keyCode=%d, action=%d",
                        event.getKeyCode(), event.getAction()));
//                etQuery.requestFocus();
//                etBarCode.setFocusableInTouchMode(true);
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        attemptLogin();
                    }
                    return true;
                }
                return false;
            }
        });
        ibDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etUserName.getText().clear();
                etUserName.requestFocus();
            }
        });
        btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToken();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        firstStep();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        hideSystemUI();
    }

    public void hideKeyboard() {
        DeviceUtils.hideSoftInputEver(SmsSignActivity.this, etUserName);
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
            btnSms.setText(String.format("%d秒重试", millisUntilFinished / 1000));
            btnSms.setEnabled(false);
        }

        @Override
        public void onFinish() {
            btnSms.setText("获取验证码");
            btnSms.setEnabled(true);
        }
    }

    /**
     * 获取验证码
     */
    private void getToken() {
        btnSms.setEnabled(false);
        final String phoneNumber = etUserName.getText().toString();
        if (StringUtils.isEmpty(phoneNumber)) {
            DialogUtil.showHint("请输入手机号");
            firstStep();
            return;
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSms.setEnabled(true);
            return;
        }

        if (countDownTimer == null) {
            countDownTimer = new VerifyCodeCountDownTimer(60 * 1000, 1000);
        }
        countDownTimer.start();
//        progressBar.setVisibility(View.VISIBLE);

        if (userTmpId == null) {
            HumanAuthTempApi.beginAuthenBysms(phoneNumber, verifyCodeCallback);
        } else {
            HumanAuthTempApi.retryAuthenBysms(phoneNumber, userTmpId, retryRC);
        }
    }

    private NetCallBack.NetTaskCallBack verifyCodeCallback = new NetCallBack.NetTaskCallBack<Long,
            NetProcessor.Processor<Long>>(
            new NetProcessor.Processor<Long>() {
                @Override
                public void processResult(final IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":""}
//                    {"code":"0","msg":"操作成功!","version":"1","data":6696}
                    if (rspData != null) {
                        RspValue<Long> retValue = (RspValue<Long>) rspData;
                        userTmpId = retValue.getValue();
                        DialogUtil.showHint("验证码已经发送成功，请注意查收");
                        ZLogger.d(String.format("发送验证码成功:%d", userTmpId));
                    }

//                    progressBar.setVisibility(View.GONE);
                    secondStep();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                    ZLogger.e(String.format("发送验证码失败:%s", errMsg));
                    DialogUtil.showHint("发送验证码失败");
//                    progressBar.setVisibility(View.GONE);
                }
            }
            , Long.class
            , MfhApplication.getAppContext()) {
    };

    private NetCallBack.NetTaskCallBack retryRC = new NetCallBack.NetTaskCallBack<Long,
            NetProcessor.Processor<Long>>(
            new NetProcessor.Processor<Long>() {
                @Override
                public void processResult(final IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":""}
//                    {"code":"0","msg":"操作成功!","version":"1","data":6696}
//                    if (rspData != null) {
//                        RspValue<Long> retValue = (RspValue<Long>) rspData;
//                        userTmpId = retValue.getValue();
//                        DialogUtil.showHint("验证码已经发送成功，请注意查收");
//                        ZLogger.d(String.format("发送验证码成功:%d", userTmpId));
//                    }

//                    progressBar.setVisibility(View.GONE);
                    secondStep();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                    ZLogger.e(String.format("发送验证码失败:%s", errMsg));
                    DialogUtil.showHint("发送验证码失败");
//                    progressBar.setVisibility(View.GONE);
                }
            }
            , Long.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * <ol>
     *     第一步，输入手机号
     *     <li>手机号验证成功后，自动发送验证码</li>
     * </ol>
     */
    private void firstStep() {
//        etPhoneNumber.setEnabled(true);
        etUserName.getText().clear();
        etUserName.requestFocus();

        btnSms.setText("获取验证码");
        btnSms.setEnabled(true);
        userTmpId = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

//        etPassword.setEnabled(false);
        etPassword.getText().clear();

        btnSignIn.setEnabled(false);
        showProgress(false);
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

        etPassword.requestFocus();
        etPassword.getText().clear();
//        etPassword.setEnabled(true);

        btnSignIn.setEnabled(true);
//        progressBar.setVisibility(View.GONE);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    public void attemptLogin() {
//        btnSignIn.setEnabled(false);
        DeviceUtils.hideSoftInputEver(this, etUserName);

        String username = etUserName.getText().toString();
        String token = etPassword.getText().toString();

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            etUserName.setError("手机号不能为空");
            firstStep();
            return;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(token)) {
            etPassword.setError("请输入验证码");
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            secondStep();
//            btnSignIn.setEnabled(true);
            return;
        }

        if (userTmpId == null){
            DialogUtil.showHint("验证码无效，请重新输入");
            secondStep();
            return;
        }

        if (!NetworkUtils.isConnect(this)) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
//            btnSignIn.setEnabled(true);
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        NetCallBack.NetTaskCallBack authCallback = new NetCallBack.NetTaskCallBack<UserMixInfo,
                NetProcessor.Processor<UserMixInfo>>(
                new NetProcessor.Processor<UserMixInfo>() {
                    @Override
                    public void processResult(final IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":""}
                        if (rspData != null) {
                            RspBean<UserMixInfo> retValue = (RspBean<UserMixInfo>) rspData;
//                Log.d("Nat: loginResponse", String.format("retValue= %s", retValue.toString()));
                            UserMixInfo um = retValue.getValue();
                            MfhLoginService.get().saveUserMixInfo(null, null, um);
                        }

                        showProgress(false);

                        DialogUtil.showHint("登录成功");
//                MainActivity.actionStart(LoginActivity.this, null);

                        IMClient.getInstance().registerBridge();

                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                        //{"code":"1","msg":"短信验证码验证不对，请重新输入!","version":"1","data":null}
                        ZLogger.e(String.format("短信码验证验证失败:%s", errMsg));
                        DialogUtil.showHint("短信码验证验证失败,请重新输入");
                        showProgress(false);
                        userTmpId = null;
                        secondStep();
//                        btnVerifyCode.setVisibility(View.VISIBLE);
                    }
                }
                , UserMixInfo.class
                , MfhApplication.getAppContext()) {
        };

        HumanAuthTempApi.loginBySms(token, userTmpId, authCallback);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}


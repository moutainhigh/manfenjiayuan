/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.manfenjiayuan.business.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bingshanguxue.skinloader.base.SkinBaseActivity;
import com.manfenjiayuan.business.R;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.manfenjiayuan.business.hostserver.HostServerFragment;
import com.manfenjiayuan.business.route.Route;
import com.manfenjiayuan.business.route.RouteActivity;
import com.manfenjiayuan.business.utils.SharedPrefesManagerBase;
import com.manfenjiayuan.im.IMApi;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;


/**
 * 登录
 * A login screen that offers login via email/password.
 */
public class SignInActivity extends SkinBaseActivity {
    private View rootView;
    private View loginFormView;
    private EditText etUserName;
    private EditText etPassword;
    private Button btnSignIn;
    private ImageView ivHostServer;

    private View mProgressView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_sign_in;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        hideSystemUI();
        super.onCreate(savedInstanceState);
        dynamicAddView(rootView, "background", R.color.colorPrimary);
        dynamicAddView(btnSignIn, "background", R.color.colorPrimary);

        refresh();
    }

    @Override
    protected void initViews() {
        super.initViews();
        rootView = findViewById(R.id.rootview);
        loginFormView = findViewById(R.id.login_form);
        etUserName = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        mProgressView = findViewById(R.id.login_progress);
        btnSignIn = (Button) findViewById(R.id.button_sign_in);
        ivHostServer = (ImageView) findViewById(R.id.iv_hostserver);

        rootView.setOnClickListener(new View.OnClickListener() {
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
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        findViewById(R.id.bottomview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect2HostServer();
            }
        });

        findViewById(R.id.tv_retrievePwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(etUserName, "忘记密码", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        findViewById(R.id.tv_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(etUserName, "新用户注册", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        hideSystemUI();
    }

    public void hideKeyboard() {
        DeviceUtils.hideSoftInputEver(SignInActivity.this, etUserName);
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
        String password = etPassword.getText().toString();

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            etUserName.setError(getString(R.string.username_field_required));
            etUserName.requestFocus();
//            btnSignIn.setEnabled(true);
            return;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.password_field_required));
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            etPassword.requestFocus();
//            btnSignIn.setEnabled(true);
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

        MfhLoginService.get().doLoginAsync(username, password, new LoginCallback() {
            @Override
            public void loginSuccess(UserMixInfo user) {
                //登录成功
                DialogUtil.showHint("登录成功");
//                MainActivity.actionStart(LoginActivity.this, null);

                IMClient.getInstance().registerBridge();

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void loginFailed(String errMsg) {
                //登录失败
                ZLogger.d("登录失败：" + errMsg);
                DialogUtil.showHint(errMsg);
                showProgress(false);
            }
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Route.ARC_APP_HOSTSERVER: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    HostServer hostServer = (HostServer) data.getSerializableExtra(HostServerFragment.EXTRA_KEY_HOSTSERVER);
                    if (hostServer != null) {
//                        AppIconManager.changeIcon(SignInActivity.this, hostServer.getActivityAlias());

                        SharedPrefesManagerBase.setHostServer(hostServer);
                        MfhApi.URL_BASE_SERVER = hostServer.getBaseServerUrl();
                        MobileApi.DOMAIN = hostServer.getHost();
//                        IMApi.URL_MOBILE_MESSAGE = hostServer.getBaseMessageUrl();
                        MfhApi.register();
                        IMApi.register();
                        refresh();
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refresh() {
//        dynamicAddView(rootFrame, "background", R.color.colorPrimaryDark);

        String lastUsername = MfhLoginService.get().getLastLoginName();
        if (!StringUtils.isEmpty(lastUsername)) {
            etUserName.setText(lastUsername);
            etPassword.requestFocus();
        } else {
            etUserName.requestFocus();
        }

        HostServer hostServer = SharedPrefesManagerBase.getHostServer();
        if (hostServer == null) {
            ivHostServer.setImageResource(R.mipmap.ic_launcher);
        } else {
            ivHostServer.setImageResource(hostServer.getTextLogoResId());
        }
    }

    public void redirect2HostServer() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putBoolean(BaseActivity.EXTRA_KEY_FULLSCREEN, true);
        extras.putInt(RouteActivity.EXTRA_KEY_FRAGMENT_TYPE, RouteActivity.FT_APP_HOSTSERVER);
        extras.putInt(HostServerFragment.EXTRA_KEY_MODE, 1);

        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtras(extras);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, Route.ARC_APP_HOSTSERVER);
    }

//    @Override
//    public void onThemeUpdate() {
//        super.onThemeUpdate();
//        refresh();
//    }
}


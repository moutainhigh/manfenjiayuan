package com.manfenjiayuan.cashierdisplay.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.manfenjiayuan.cashierdisplay.R;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.entity.UserMixInfo;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoginCallback {

    // UI references.
    @Bind(R.id.email)
    AutoCompleteTextView mEmailView;
    @Bind(R.id.password)
    EditText mPasswordView;
    @Bind(R.id.login_progress)
    View mProgressView;
    @Bind(R.id.login_form)
    View mLoginFormView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_login;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        hideNavigationBar();
        hideSystemUI();

        super.onCreate(savedInstanceState);

        // Set up the login form.
        populateAutoComplete();

//        StatusBarUtil.setTranslucent(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        String lastUsername = MfhLoginService.get().getLastLoginName();
        if (!StringUtils.isEmpty(lastUsername)){
            mEmailView.setText(lastUsername);
            mPasswordView.requestFocus();
        }
        else{
            mPasswordView.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        hideSystemUI();
        super.onResume();
    }

    private void populateAutoComplete() {

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @OnClick(R.id.email_sign_in_button)
    public void attemptLogin() {
        ZLogger.d("准备登录");
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        ZLogger.d("验证密码格式结束");

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        ZLogger.d("验证用户名格式结束");

        if (cancel) {
            ZLogger.d("登录错误");
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            ZLogger.d("登录中。。。");
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            MfhLoginService.get().doLoginAsync(email, password, this);
        }
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
//        return email.contains("@");
        return email.length() > 0;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    public void loginSuccess(UserMixInfo user) {
        //登录成功
        showProgress(false);
        DialogUtil.showHint("登录成功");
//                MainActivity.actionStart(SignInActivity.this, null);
        ZLogger.df("login success.");

        IMClient.getInstance().registerBridge();

        startActivity(new Intent(this, MainActivity.class));

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void loginFailed(String errMsg) {//登录失败
        ZLogger.df("login failed : " + errMsg);
        showProgress(false);
//                DialogUtil.showHint(errMsg);
        Snackbar.make(mLoginFormView, errMsg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        startActivity(new Intent(this, MainActivity.class));

        setResult(RESULT_OK);
        finish();
    }

}


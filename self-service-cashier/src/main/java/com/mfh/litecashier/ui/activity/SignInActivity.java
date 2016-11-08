package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.wrapper.HostServer;
import com.manfenjiayuan.im.IMApi;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.components.HostServerFragment;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 登录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SignInActivity extends BaseActivity {

    public static final String EXTRA_KEY_LOGINMODE = "loginMode";
    public static final int LOGIN_MODE_SPLASH = 0;

    @Bind(R.id.et_username)
    EditText etUserName;
    @Bind(R.id.et_password)
    EditText etPassword;
    @Bind(R.id.button_signin)
    Button btnSignin;
    @Bind(R.id.btn_hostserver)
    Button btnHostserver;
    @Bind(R.id.animProgressBar)
    ProgressBar progressBar;

    private int loginMode = LOGIN_MODE_SPLASH;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SignInActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_sign_in;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        if (intent != null) {
            loginMode = intent.getIntExtra(EXTRA_KEY_LOGINMODE, LOGIN_MODE_SPLASH);
        }

        //TODO 单击屏幕隐藏键盘
//        etUserName.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

//        DeviceUtils.hideSoftInput(this, etUserName);
//        DeviceUtils.hideSoftInput(this, etPassword);

        etUserName.setOnKeyListener(new EditText.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                        event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP && curStep == STEP_NA) {
                        etPassword.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        etPassword.setOnKeyListener(new EditText.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                        event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP && curStep == STEP_NA) {
                        signIn();
                    }
                    return true;
                }
                return false;
            }
        });


        refresh();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @OnClick(R.id.frame_login)
    public void hideKeyboard() {
        DeviceUtils.hideSoftInputEver(this);
    }

    @OnClick(R.id.button_signin)
    public void signIn() {
        setProcessingStep(STEP_PROCESSING);

        DeviceUtils.hideSoftInputEver(this);

        String username = etUserName.getText().toString();
        String password = etPassword.getText().toString();
        if (StringUtils.isEmpty(username)) {
            setProcessingStep(STEP_NA);
            etUserName.requestFocus();
            Snackbar.make(etUserName, R.string.toast_login_username_empty, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }
        if (StringUtils.isEmpty(password)) {
            setProcessingStep(STEP_NA);
            etPassword.requestFocus();
            Snackbar.make(etUserName, R.string.toast_login_password_empty, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            setProcessingStep(STEP_NA);
            Snackbar.make(etUserName, R.string.toast_network_error, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return;
        }

        MfhLoginService.get().doLoginAsync(username, password, new LoginCallback() {
            @Override
            public void loginSuccess(UserMixInfo user) {
//                btnSignin.setEnabled(true);
                //登录成功
                DialogUtil.showHint("登录成功");
//                MainActivity.actionStart(SignInActivity.this, null);
                ZLogger.df("login success.");

                IMClient.getInstance().registerBridge();

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void loginFailed(String errMsg) {
                //登录失败
                ZLogger.df("login failed : " + errMsg);
                setProcessingStep(STEP_NA);
//                DialogUtil.showHint(errMsg);
                Snackbar.make(etUserName, errMsg, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_APP_HOSTSERVER: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    HostServer hostServer = (HostServer) data.getSerializableExtra(HostServerFragment.EXTRA_KEY_HOSTSERVER);
                    if (hostServer != null) {
                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_S_HOSTSERVER,
                                JSONObject.toJSONString(hostServer));
                        MfhApi.URL_BASE_SERVER = hostServer.getBaseServerUrl();
                        IMApi.URL_MOBILE_MESSAGE = hostServer.getBaseMessageUrl();
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

    private void refresh(){
        String lastUsername = MfhLoginService.get().getLastLoginName();
        if (!StringUtils.isEmpty(lastUsername)){
            etUserName.setText(lastUsername);
            etPassword.requestFocus();
        }
        else{
            etUserName.requestFocus();
        }

        String hostServerData = SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_S_HOSTSERVER, null);
        HostServer hostServer = JSONObject.toJavaObject(JSONObject.parseObject(hostServerData), HostServer.class);
        if (hostServer == null){
            btnHostserver.setText("选择租户");
        }
        else{
            btnHostserver.setText(String.format("%s(点击切换)", hostServer.getName()));
        }
    }

    @OnClick(R.id.tv_retrievePwd)
    public void retrievePwd() {
        Snackbar.make(etUserName, "忘记密码", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @OnClick(R.id.tv_signup)
    public void signUp() {
        Snackbar.make(etUserName, "新用户注册", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    private static final int STEP_NA = 0;
    private static final int STEP_PROCESSING = 1;
    private int curStep = STEP_NA;
    private void setProcessingStep(int step){
        this.curStep = step;
        if (step == STEP_PROCESSING){
            btnSignin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            btnSignin.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }
    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
//    public void toggleHideyBar() {
//
//        // The UI options currently enabled are represented by a bitfield.
//        // getSystemUiVisibility() gives us that bitfield.
//        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
//        int newUiOptions = uiOptions;
//        boolean isImmersiveModeEnabled =
//                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
//        if (isImmersiveModeEnabled) {
//            ZLogger.d("Turning immersive mode mode off. ");
//        } else {
//            ZLogger.d("Turning immersive mode mode on.");
//        }
//
//        // Navigation bar hiding:  Backwards compatible to ICS.
//        if (Build.VERSION.SDK_INT >= 14) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        }
//
//        // Status bar hiding: Backwards compatible to Jellybean
//        if (Build.VERSION.SDK_INT >= 16) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//        }
//
//        // Immersive mode: Backward compatible to KitKat.
//        // Note that this flag doesn't do anything by itself, it only augments the behavior
//        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
//        // all three flags are being toggled together.
//        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
//        // Sticky immersive mode differs in that it makes the navigation and status bars
//        // semi-transparent, and the UI flag does not get cleared when the user interacts with
//        // the screen.
//        if (Build.VERSION.SDK_INT >= 18) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        }
//
//        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
//    }

    @OnClick(R.id.btn_hostserver)
    public void redirect2HostServer(){
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE,
                FragmentActivity.FT_APP_HOSTSERVER);
//        extras.putInt(HostServerFragment.EXTRA_KEY_LAUNCHMODE, 0);

        Intent intent = new Intent(SignInActivity.this, FragmentActivity.class);
        intent.putExtras(extras);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, Constants.ARC_APP_HOSTSERVER);
    }
}

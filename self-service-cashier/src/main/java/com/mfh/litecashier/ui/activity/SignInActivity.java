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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bingshanguxue.skinloader.base.SkinBaseActivity;
import com.bingshanguxue.skinloader.listener.ILoaderListener;
import com.bingshanguxue.skinloader.loader.SkinManager;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.manfenjiayuan.business.hostserver.HostServerFragment;
import com.manfenjiayuan.business.route.RouteActivity;
import com.manfenjiayuan.business.utils.SharedPrefesManagerBase;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.ResultCode;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 登录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SignInActivity extends SkinBaseActivity {

    public static final String EXTRA_KEY_LOGINMODE = "loginMode";
    public static final int LOGIN_MODE_SPLASH = 0;

    @BindView(R.id.rootview)
    View rootView;
    @BindView(R.id.et_username)
    EditText etUserName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.button_signin)
    Button btnSignin;
    @BindView(R.id.bottomview)
    LinearLayout bottomView;
    @BindView(R.id.iv_hostserver)
    ImageView ivHostServer;
    @BindView(R.id.animProgressBar)
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

        ZLogger.d("进入登录页面");
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

//        dynamicAddView(rootView, "background", R.color.colorPrimary);
//        dynamicAddView(btnSignin, "background", R.color.colorPrimary);

        refresh();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @OnClick(R.id.rootview)
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

                IMClient.getInstance().registerBridge();

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void loginFailed(String errMsg) {
                //登录失败
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
            case ResultCode.ARC_APP_HOSTSERVER: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    loadSkin(GlobalInstanceBase.getInstance().getSkinName());
                }

                //放在最后，如果没有选择租户，还会自动弹出租户页面
                refresh();
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

        HostServer hostServer = SharedPrefesManagerBase.getHostServer();
        if (hostServer == null){
            ivHostServer.setImageResource(R.mipmap.ic_launcher);
            redirect2HostServer();
        }
        else{
            ivHostServer.setImageResource(getImageResource(hostServer.getDomainUrl()));

//            String domain = hostServer.getDomainUrl();
//            if (!StringUtils.isEmpty(domain)){
//                if (domain.startsWith("admin")){
//                    LauncherManager.changeLauncherIcon(this, LauncherManager.ALIAS_MIXICOOK);
//                }
//                else if (domain.startsWith("lanlj")){
//                    LauncherManager.changeLauncherIcon(this, LauncherManager.ALIAS_LANLJ);
//                }
//                else if (domain.startsWith("qianwj")){
//                    LauncherManager.changeLauncherIcon(this, LauncherManager.ALIAS_QIANWJ);
//                }
//            }
        }
    }
    private int getImageResource(String domain){
        if (!StringUtils.isEmpty(domain)){
            if (domain.startsWith("admin")){
                return R.mipmap.ic_textlogo_mixicook;
            }
            else if (domain.startsWith("lanlj")){
                return R.mipmap.ic_textlogo_lanlj;
            }
            else if (domain.startsWith("qianwj")){
                return R.mipmap.ic_textlogo_qianwj;
            }
        }

        return R.mipmap.ic_launcher;
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

    @OnClick(R.id.bottomview)
    public void redirect2HostServer(){
        ZLogger.d("跳转到选择租户页面");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putBoolean(BaseActivity.EXTRA_KEY_FULLSCREEN, true);
        extras.putInt(RouteActivity.EXTRA_KEY_FRAGMENT_TYPE, RouteActivity.FT_APP_HOSTSERVER);
        extras.putInt(HostServerFragment.EXTRA_KEY_MODE, 0);

        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtras(extras);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, ResultCode.ARC_APP_HOSTSERVER);
    }

    /**
     * 加载皮肤
     * */
    private void loadSkin(String skinName) {
        SkinManager.getInstance().loadSkin(skinName,
                new ILoaderListener() {
                    @Override
                    public void onStart() {
                        ZLogger.df("正在切换主题");
//                        dialog.show();
                    }

                    @Override
                    public void onSuccess() {
                        ZLogger.df("切换主题成功");
                        DialogUtil.showHint("切换租户成功");
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        ZLogger.df("切换主题失败:" + errMsg);
                        DialogUtil.showHint(errMsg);
                    }

                    @Override
                    public void onProgress(int progress) {
                        ZLogger.d("主题皮肤文件下载中:" + progress);
                    }
                }

        );

    }
}

package com.mfh.framework.uikit.base;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mfh.framework.core.location.MfLocationManagerProxy;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.ButterKnife;


/**
 * Template Method: 继承次数最多不超过3次.<br>
 * Activity基类·定位·动画
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_KEY_ANIM_TYPE = "animationType";

    //窗口动画
    public static final int ANIM_TYPE_NEW_NONE = -1;//无动画
    public static final int ANIM_TYPE_NEW_FLOW = 0;//新流程，底部弹出
    protected int animType = ANIM_TYPE_NEW_NONE;
    protected int activityCloseEnterAnimation;
    protected int activityCloseExitAnimation;

    //定位时间间隔
    private static final int LOCATION_INTERVAL = 2;//by seconds
    //满分定位
    private MfLocationManagerProxy mMfLocationManager;


    //进度对话框
    private ProgressDialog mProgressDialog = null;
    //确认对话框
    private CommonDialog confirmDialog = null;


    /**
     * 是否全屏显示
     */
    protected boolean isFullscreenEnabled() {
        return false;
    }

    /**
     * 设置主题
     */
    protected void setTheme() {
    }

    /**
     * 设置布局资源
     */
    protected int getLayoutResId() {
        return 0;
    }

    /**
     * 初始化视图控件
     */
    protected void initViews() {
        ButterKnife.bind(this);
    }

    /**
     * 设置ToolBar
     */
    protected void initToolBar() {
    }

    /**
     * 设置第三方
     */
    protected void setupThirdParty() {
    }

    /**
     * 是否支持满分定位
     */
    protected boolean isMfLocationEnable() {
        return false;
    }

    /**
     * 是否相应返回按键
     */
    protected boolean isBackKeyEnabled() {
        return true;
    }

    /**
     * 是否连续两次按下返回按键退出
     */
    protected boolean isDoubleClickExitEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isFullscreenEnabled()){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        retrieveAnimations();

//        setTheme();
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());

        registerSystemUiVisibilityChangeListener();

        initViews();
        initToolBar();

        setupThirdParty();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isMfLocationEnable()) {
            initMfLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isMfLocationEnable()) {
            stopMfLocation();
        }

        hideProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        hideProgressDialog();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);

        Thread.currentThread().interrupt();//中断当前线程.
    }

    /**
     * 实现再按一次退出提醒
     */
    private long exitTime = 0;

    /**
     * 监听返回--是否退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        屏蔽返回操作
        if (!isBackKeyEnabled()) {
            return (keyCode == KeyEvent.KEYCODE_BACK
                    && event.getAction() == KeyEvent.ACTION_DOWN) || super.onKeyDown(keyCode, event);
        } else {
            if (isDoubleClickExitEnabled()) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN) {

                    if ((System.currentTimeMillis() - exitTime) > 3000) {
                        DialogUtil.showHint("再按一次将退出程序");
                        exitTime = System.currentTimeMillis();
                    } else {
                        finish();
                    }
                    return true;
                }
            }

            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /**
     * Retrieve the animations set in the theme applied to this activity in the manifest..
     */
    private void retrieveAnimations() {
        TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
        activityStyle.recycle();

// Now retrieve the resource ids of the actual animations used in the animation style pointed to by
// the window animation resource id.
        TypedArray activityStyle2 = getTheme().obtainStyledAttributes(windowAnimationStyleResId,
                new int[]{android.R.attr.activityCloseEnterAnimation,
                        android.R.attr.activityCloseExitAnimation});
        activityCloseEnterAnimation = activityStyle2.getResourceId(0, 0);
        activityCloseExitAnimation = activityStyle2.getResourceId(1, 0);
        activityStyle2.recycle();
    }


    /**
     * 初始化定位
     */
    protected void initMfLocation() {
        mMfLocationManager = MfLocationManagerProxy.getInstance(this);
        mMfLocationManager.requestLocationData(LOCATION_INTERVAL * 1000,
                15, mfLocationListener);
    }

    protected void stopMfLocation() {
        if (mMfLocationManager != null) {
            mMfLocationManager.removeUpdates(mfLocationListener);
        }
        mMfLocationManager = null;
    }

    private LocationListener mfLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            ZLogger.d(MfLocationManagerProxy.getLocationInfo(location));
            //TODO,保存位置信息
            MfLocationManagerProxy.saveLocationInfo(BaseActivity.this, location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Provider状态在可用、暂不可用、无服务三个状态之间直接切换时触发此函数
            ZLogger.d(String.format("%s, status:%d", provider, status));
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Provider被enable时触发此函数,比如GPS被打开
            ZLogger.d(provider + " enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Provider被disable时触发此函数,比如GPS被关闭
            ZLogger.d(provider + " disabled");
        }
    };

    private void registerSystemUiVisibilityChangeListener() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        int height = decorView.getHeight();
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            Log.d(TAG, "onSystemUiVisibilityChange: " +
                                    "The system bars are visible. Make any desired, height:" + height);
                            //re-enter immersive mode after 3 seconds.
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideSystemUI();
                                }
                            }, 3000);
                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                            Log.d(TAG, "onSystemUiVisibilityChange:" +
                                    " The system bars are NOT visible. Make any desired, height:" + height);
                        }
                    }
                });
    }

    /**
     * Hide the Navigation Bar
     */
    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void immesiveSticky() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void immesiveSticky2() {
        View decorView = getWindow().getDecorView();

        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = decorView.getSystemUiVisibility();

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //Hide the Status Bar on Android 4.1 and Higher
            uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;

            //Make Content Appear Behind the Status Bar
            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            //Make Content Appear Behind the Navigation Bar
            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

            //help your app maintain a stable layout.
            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//            uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * This snippet hides the system bars.
     */
    public void hideSystemUI() {
        immesiveSticky();
    }

    /**
     * This snippet shows the system bars. It does this by removing all the flags
     * except for the ones that make the content appear under the system bars.
     */
    public void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void initProgressDialog(String processText, String doneText, String errorText) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.init(processText, doneText, errorText);
    }

    /**
     * 显示同步数据对话框
     */
    public void showProgressDialog(int status) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.setProgress(status);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    /**
     * 显示进度对话框
     */
    public void showProgressDialog(int status, String processText,
                                   boolean isAutoHideEnabled) {
        showProgressDialog(status, processText, false, isAutoHideEnabled);
    }

    public void showProgressDialog(int status, String processText,
                                   boolean isCancelAble, boolean isAutoHideEnabled) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(isCancelAble);
        }
        mProgressDialog.setProgress(status, processText, isAutoHideEnabled);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    /**
     * 隐藏进度对话框
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 显示确认对话框
     * */
    public void showConfirmDialog(int message, int positive,
                                  DialogInterface.OnClickListener positiveListener,
                                  int negative,
                                  DialogInterface.OnClickListener negativelistener){
        if (confirmDialog == null){
            confirmDialog = new CommonDialog(this);
        }

        confirmDialog.setMessage(message);
        confirmDialog.setPositiveButton(positive, positiveListener);
        confirmDialog.setNegativeButton(negative, negativelistener);
        confirmDialog.show();
    }

    /**
     * 显示确认对话框
     * */
    public void showConfirmDialog(String message, String positive,
                                  DialogInterface.OnClickListener positiveListener,
                                  String negative,
                                  DialogInterface.OnClickListener negativelistener){
        if (confirmDialog == null){
            confirmDialog = new CommonDialog(this);
        }

        confirmDialog.setMessage(message);
        confirmDialog.setPositiveButton(positive, positiveListener);
        confirmDialog.setNegativeButton(negative, negativelistener);
        confirmDialog.show();
    }

}

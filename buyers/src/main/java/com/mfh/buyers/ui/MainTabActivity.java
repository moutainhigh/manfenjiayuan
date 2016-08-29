package com.mfh.buyers.ui;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TextView;

import com.mfh.buyers.AppContext;
import com.mfh.buyers.R;
import com.mfh.buyers.fragments.ConversationAllFragment;
import com.mfh.buyers.fragments.IndividualFragment;
import com.mfh.buyers.fragments.MfParterFragment;
import com.mfh.buyers.ui.web.H5AuthActivity;
import com.mfh.buyers.utils.Constants;
import com.mfh.buyers.utils.MobileURLConf;
import com.mfh.buyers.utils.UserProfileHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.uikit.widget.OnTabReselectListener;
import com.mfh.framework.login.logic.MfhLoginService;

/**
 * 首页
 */
public class MainTabActivity extends BaseActivity {
    private static final String TAG = MainTabActivity.class.getSimpleName();

    public static final String EXTRA_KEY_TAB_INDEX = "EXTRA_KEY_TAB_INDEX";

    private MyFragmentTabHost mTabHost;
    private Animation bottomInAnim, bottomOutAnim;

    private View backgroundView, backgroundMaskView;
    private BroadcastReceiver receiver;
    AppContext app;

    public static void actionStart(Context context, int tabIndex) {
        Intent intent = new Intent(context, MainTabActivity.class);
        intent.putExtra(EXTRA_KEY_TAB_INDEX, tabIndex);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            // Translucent status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //注释该行，解决底部导航Tab在5.1.1 Nexus手机上和底部状态栏重叠问题。
            // Translucent navigation bar
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        //Android 5.1.1 报错：
//        Caused by: java.lang.IllegalArgumentException: Service Intent must be explicit: Intent { act=com.mfh.owner.service.BackService }
//        startService(new Intent("com.mfh.owner.service.BackService"));
        Intent intent = new Intent(this, BackService.class);
        startService(intent);

        app = (AppContext) getApplication();

        canRedirectToLogin = true;

        initAnims();
        initView();
        toggleBottomBar(true);

        registerReceiver();

        handleIntent(getIntent());


        //检查更新
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AppUpdateManager updateManager = new AppUpdateManager(MainTabActivity.this);
                updateManager.checkServVersionCode(null);
            }
        }, 1000);

        ZLogger.d("MainTabActivity onCreate finished");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
        }
    }

    @Override
    protected void onResume() {
//        boolean isBTEnable = isBlueEnable();
//        if (isBTEnable) {
//            app.startSensoroService();
//        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onBackPressed() {
        //生活·摇一摇页面，不响应返回事件
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            if (currentFragment instanceof MfParterFragment && !((MfParterFragment) currentFragment).isRootWeb()) {
                ((MfParterFragment) currentFragment).backToHistory();//返回生活页面
                return;
            }
        }

        super.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.ACTIVITY_REQUEST_LOGIN_H5) {
            //TODO
            canRedirectToLogin = true;

            if (resultCode == Activity.RESULT_OK) {
                //TODO 判断是从那一个页面返回的
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment != null) {
                    if (currentFragment instanceof ConversationAllFragment) {
                        ((ConversationAllFragment) currentFragment).reloadData();
//                        return;
                    } else if (currentFragment instanceof MfParterFragment) {
                        ((MfParterFragment) currentFragment).loadData();
//                        return;
                    } else if (currentFragment instanceof IndividualFragment) {
                        ((IndividualFragment) currentFragment).reloadData();
//                        return;
                    }
                }
            } else {
                //TODO:显示上一页
                mTabHost.setCurrentTab(0);
                toggleBottomBar(true);

                Fragment currentFragment = getCurrentFragment();
                if (currentFragment instanceof MfParterFragment) {
                    ((MfParterFragment) currentFragment).loadData();
//                        return;
                }
            }
        }
    }

    /**
     * 监听返回--是否退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // TODO,双击退出应用
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 处理传进来的intent
     */
    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        int pageIndex = intent.getIntExtra(EXTRA_KEY_TAB_INDEX, 0);
        mTabHost.setCurrentTab(pageIndex);

        // Detect if the activity was launched by the user clicking on a notification
//            if (intent.getExtras().getBoolean(Constants.EXTRA_HANDLING_NOTIFICATION, false)) {
//                // Notify that the activity was opened by the user clicking on a notification.
//                app.setLaunchMechanism(ComnApplication.LaunchMechanism.NOTIFICATION);
//            }
        String action = intent.getAction();
        if (action != null && action.equalsIgnoreCase("MSG_NOTIFICATION_SESSIOIN")) {//notification
            Long sid = intent.getLongExtra("sessionId", -1L);
//            //这里作为消息通知入口，点击返回按键返回会话列表。
            ChatActivity.actionStart(this, sid);
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_ACTION_TOGGLE_MAIN_TABHOST);
        filter.addAction(Constants.BROADCAST_ACTION_CHANGE_BACKGROUND);
        filter.addAction(Constants.BROADCAST_KEY_RECEIVE_ORDER);
        filter.addAction(Constants.BROADCAST_KEY_NOTIFY_MFPARTER_PEISONG);
        filter.addAction(Constants.ACTION_REDIRECT_TO_LOGIN_H5);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
//                System.out.print(String.format("Nat: onReceive action=%s", action));
                if (Constants.BROADCAST_ACTION_TOGGLE_MAIN_TABHOST.equals(action)) {
                    boolean visible = intent.getBooleanExtra(Constants.BROADCAST_KEY_MAIN_TABHOST_VISIBILITY, true);
                    if (mTabHost.getCurrentTab() == 0) {
                        toggleBottomBar(visible);
                    } else {
                        toggleBottomBar(true);
                    }
                } else if (Constants.BROADCAST_ACTION_CHANGE_BACKGROUND.equals(action)) {
                    boolean visible = intent.getBooleanExtra(Constants.BROADCAST_KEY_BACKGROUND_MASK_VISIBILITY, true);
                    if (visible) {
                        Animation anim = ShakeUtil.getAlphaAnimation(0, 1, 600);
                        backgroundMaskView.startAnimation(anim);
                    } else {
                        Animation anim = ShakeUtil.getAlphaAnimation(1, 0, 600);
                        backgroundMaskView.startAnimation(anim);
                    }
                } else if (action.equals(com.mfh.comna.comn.Constants.ACTION_REDIRECT_TO_LOGIN_H5)) {
                    redirectToLogin();
                } else if (action.equals(Constants.BROADCAST_KEY_RECEIVE_ORDER)) {
                    String content = intent.getStringExtra("content");
                    String orderId = intent.getStringExtra("orderId");

                    MLog.d(String.format("content=%s,orderId=%s", content, orderId));
                    com.mfh.buyers.utils.UIHelper.showReceiveOrderDialog(MainTabActivity.this, content, orderId);
                } else if (action.equals(Constants.BROADCAST_KEY_NOTIFY_MFPARTER_PEISONG)) {
                    String content = intent.getStringExtra("content");
                    String orderIds = intent.getStringExtra("orderIds");
                    String delivererId = intent.getStringExtra("delivererId");

                    MLog.d(String.format("content=%s,orderIds=%s,delivererId=%s", content, orderIds, delivererId));
                    com.mfh.buyers.utils.UIHelper.showDMfeliverDialog(MainTabActivity.this, content, orderIds, delivererId);
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private boolean canRedirectToLogin = true;

    private void redirectToLogin() {
        if (!canRedirectToLogin) {
            return;
        }

        canRedirectToLogin = false;
        //old,在这里执行clear()方法，如果退出失败（比如网络断开），此时数据已经清空，但是也没还没有变化。
        EmbSessionService.get().clearMsgs();
        MfhLoginService.get().clear();//清空
        ServiceFactory.cleanService();
        UserProfileHelper.cleanUserProfile();//清空个人信息

        //TODO,判断当前页是否需要切换登录页面

        String authUrl = MobileURLConf.generateUrl(MobileURLConf.URL_AUTH_INDEX, "redirect=" + MobileURLConf.URL_NATIVIE_REDIRECT_AUTH);
        Intent loginIntent = new Intent(MainTabActivity.this, H5AuthActivity.class);
        loginIntent.putExtra(H5AuthActivity.EXTRA_KEY_REDIRECT_URL, authUrl);
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                        startActivity(intent);
        startActivityForResult(loginIntent, Constants.ACTIVITY_REQUEST_LOGIN_H5);
//        canRedirectToLogin = true;
    }

    /**
     * 初始化视图
     */
    private void initView() {
        backgroundView = findViewById(R.id.frame_background);
        backgroundMaskView = findViewById(R.id.frame_background_mask);

        mTabHost = (MyFragmentTabHost) findViewById(R.id.bottom_tab);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.ll_fragment_container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            mTabHost.getTabWidget().setShowDividers(0);
        }
        initTabs();
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                final int size = mTabHost.getTabWidget().getTabCount();
                for (int i = 0; i < size; i++) {
                    View v = mTabHost.getTabWidget().getChildAt(i);
                    if (i == mTabHost.getCurrentTab()) {
                        v.setSelected(true);
                    } else {
                        v.setSelected(false);
                    }
                }
            }
        });
    }

    private void initTabs() {
        MainTab[] tabs = MainTab.values();
        final int size = tabs.length;
        for (int i = 0; i < size; i++) {
            MainTab tab = tabs[i];
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(tab.getResName()));
            View indicator = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_indicator, null);
            TextView title = (TextView) indicator.findViewById(R.id.tab_title);
            Drawable drawable = this.getResources().getDrawable(tab.getResIcon());
            title.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            title.setText(getString(tab.getResName()));
            tabSpec.setIndicator(indicator);
            tabSpec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return new View(MainTabActivity.this);
                }
            });

//            Bundle bundle = new Bundle();
//            if(tab.getClz().isInstance(MfParterFragment.class)){
//                bundle.putString(MfParterFragment.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_ME_MFHPARTER);
//            }
            mTabHost.addTab(tabSpec, tab.getClz(), null);
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.equals(mTabHost.getCurrentTabView())) {
                        Fragment currentFragment = getCurrentFragment();
                        if (currentFragment != null && currentFragment instanceof OnTabReselectListener) {
                            OnTabReselectListener listener = (OnTabReselectListener) currentFragment;
                            if (listener != null) {
                                listener.onTabReselect();
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
    }

    /**
     * 初始化动画
     */
    private void initAnims() {
        bottomInAnim = AnimationUtils.loadAnimation(this, R.anim.shake_bottom_in);
        bottomInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTabHost.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.shake_bottom_out);
        bottomOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTabHost.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * toggle bottom tab bar
     */
    private void toggleBottomBar(boolean enabled) {
        if (enabled) {
            mTabHost.setVisibility(View.VISIBLE);
//        bottomBar.startAnimation(bottomInAnim);
//            backgroundView.setAlpha(0.7f);

            Animation anim = ShakeUtil.getAlphaAnimation(0, 1, 200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
//            backgroundMaskView.startAnimation(anim);
        } else {
            mTabHost.setVisibility(View.GONE);
//        bottomBar.startAnimation(bottomOutAnim);
//            backgroundView.setAlpha(1.0f);

            Animation anim = ShakeUtil.getAlphaAnimation(1, 0, 200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
//            backgroundMaskView.startAnimation(anim);
        }
    }

}

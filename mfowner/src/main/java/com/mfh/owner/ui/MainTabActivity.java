package com.mfh.owner.ui;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TextView;

import com.manfenjiayuan.im.database.service.IMConversationService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.uikit.widget.OnTabReselectListener;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.owner.AppContext;
import com.mfh.owner.R;
import com.mfh.owner.fragments.IndividualFragment;
import com.mfh.owner.fragments.LifeFragment;
import com.mfh.owner.fragments.OrderFragment;
import com.mfh.owner.service.BackService;
import com.mfh.owner.ui.shake.ShakeHelper;
import com.mfh.owner.ui.shake.ShakeUtil;
import com.mfh.owner.ui.web.H5AuthActivity;
import com.mfh.owner.utils.Constants;
import com.mfh.owner.utils.MobileURLConf;
import com.mfh.owner.utils.UIHelper;
import com.mfh.owner.utils.UserProfileHelper;
import com.umeng.update.UmengUpdateAgent;


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
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            // Translucent status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //注释该行，解决底部导航Tab在5.1.1 Nexus手机上和底部状态栏重叠问题。
            // Translucent navigation bar
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }


        //检查更新
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);

        //Android 5.1.1 报错：
//        Caused by: java.lang.IllegalArgumentException: Service Intent must be explicit: Intent { act=com.mfh.owner.service.BackService }
//        startService(new Intent("com.mfh.owner.service.BackService"));
        Intent intent = new Intent(this, BackService.class);
        startService(intent);

        canRedirectToLogin = true;

        initAnims();
        initView();
        toggleBottomBar(true);

        registerReceiver();

        handleIntent(getIntent());


//        if(!BizConfig.RELEASE){
        //TODO,测试数据
//            ShakeHelper.getInstance().saveShakeConf("", "慢慢用心，面包自己会说话", "bojure面包烘培坊", "");
        ShakeHelper.getInstance().saveShakeConf("", "烘焙是一门艺术，需要用心品味", "bonjour ami朋厨烘培",
                "http://mp.weixin.qq.com/s" +
                        "?__biz=MzA5ODMwNjAyMg==" +
                        "&mid=207203481" +
                        "&idx=1" +
                        "&sn=265b0df3361d135c002dc8d2f2dc5af1"

                        + "&key=c76941211a49ab5898d8da9bbed97e85f9dc45e77a3032d66b199dce9ce02f228ba2f729d75990ccacf70222e1c748cd" +
                        "&ascene=0" +
                        "&uin=NDUyNDk5NTIw" +
                        "&devicetype=iMac+MacBookPro11%2C1+OSX+OSX+10.10.3+build(14D136)" +
                        "&version=11020012" +
                        "&pass_ticket=Jy%2F8aUQND6cBYwH42hsZNYLl9eyxY8g%2BCAH9qipbEkAVoRFaaJq1z6B%2BBegUkSZG"
        );
//        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            //在摇一摇页面，从其他入口返回首页（生活），自动关闭摇一摇页面
            if (currentFragment instanceof LifeFragment
                    && ((LifeFragment) currentFragment).isShakeFragmentVisible()) {
                ((LifeFragment) currentFragment).toggleShakeFragment(false);//返回生活页面
            }
        }
    }

    @Override
    protected void onResume() {
//        boolean isBTEnable = isBlueEnable();
//        if (isBTEnable) {
//            app.startSensoroService();
//        }

        //启动扫描服务
        AppContext.getInstance().startSensoroService();

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
            if (currentFragment instanceof LifeFragment && ((LifeFragment) currentFragment).isShakeFragmentVisible()) {
                ((LifeFragment) currentFragment).toggleShakeFragment(false);//返回生活页面
                return;
            } else if (currentFragment instanceof OrderFragment && !((OrderFragment) currentFragment).isRootWeb()) {
                ((OrderFragment) currentFragment).backToHistory();//返回生活页面
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
//                    if(currentFragment instanceof ConversationAllFragment){
//                        ((ConversationAllFragment) currentFragment).reloadData();
////                        return;
//                    }
//                    else
                    if (currentFragment instanceof OrderFragment) {
                        ((OrderFragment) currentFragment).loadData();
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
//            if (intent.getExtras().getBoolean(IMConstants.EXTRA_HANDLING_NOTIFICATION, false)) {
//                // Notify that the activity was opened by the user clicking on a notification.
//                app.setLaunchMechanism(ComnApplication.LaunchMechanism.NOTIFICATION);
//            }
        String action = intent.getAction();
        if (action != null && action.equalsIgnoreCase("MSG_NOTIFICATION_SESSIOIN")) {//notification
            Long sid = intent.getLongExtra("sessionId", -1L);
            //TODO
//            //这里作为消息通知入口，点击返回按键返回会话列表。
//            ChatActivity.actionStart(this, sid);
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_ACTION_TOGGLE_MAIN_TABHOST);//接收者只有在activity才起作用。
        filter.addAction(Constants.BROADCAST_ACTION_CHANGE_BACKGROUND);//接收者只有在activity才起作用。
        filter.addAction(UIHelper.ACTION_REDIRECT_TO_LOGIN_H5);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
//                System.out.print(String.format("Nat: onReceive action=%s", action));
                if (Constants.BROADCAST_ACTION_TOGGLE_MAIN_TABHOST.equals(action)) {
                    boolean visible = intent.getBooleanExtra(Constants.BROADCAST_KEY_MAIN_TABHOST_VISIBILITY, true);
                    toggleBottomBar(visible);
                } else if (Constants.BROADCAST_ACTION_CHANGE_BACKGROUND.equals(action)) {
                    boolean visible = intent.getBooleanExtra(Constants.BROADCAST_KEY_BACKGROUND_MASK_VISIBILITY, true);
                    if (visible) {
                        Animation anim = ShakeUtil.getAlphaAnimation(0, 1, 600);
                        backgroundMaskView.startAnimation(anim);
                    } else {
                        Animation anim = ShakeUtil.getAlphaAnimation(1, 0, 600);
                        backgroundMaskView.startAnimation(anim);
                    }
                } else if (action.equals(UIHelper.ACTION_REDIRECT_TO_LOGIN_H5)) {
                    redirectToLogin();
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
        IMConversationService.get().clearMsgs();
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
            mTabHost.addTab(tabSpec, tab.getClz(), null);
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.equals(mTabHost.getCurrentTabView())) {
                        Fragment currentFragment = getCurrentFragment();
                        if (currentFragment != null && currentFragment instanceof OnTabReselectListener) {
                            ((OnTabReselectListener) currentFragment).onTabReselect();
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

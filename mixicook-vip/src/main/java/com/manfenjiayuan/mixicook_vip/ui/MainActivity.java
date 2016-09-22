package com.manfenjiayuan.mixicook_vip.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;

import com.manfenjiayuan.business.Constants;
import com.manfenjiayuan.business.presenter.PosRegisterPresenter;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.business.view.IPosRegisterView;
import com.manfenjiayuan.business.widget.MyFragmentTabHost;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.mixicook_vip.MainEvent;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ValidateManager;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartEntity;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartService;
import com.manfenjiayuan.mixicook_vip.ui.my.MyFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.api.account.Office;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.widget.OnTabReselectListener;
import com.tencent.bugly.beta.Beta;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class  MainActivity extends BaseActivity implements IPosRegisterView {

    //    @Bind(R.id.toolbar)
//    Toolbar toolbar;
    @Bind(R.id.bottomNavigationBar)
    MyFragmentTabHost mTabHost;
    private PosRegisterPresenter mPosRegisterPresenter;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
    }

    @Override
    protected void initViews() {
        super.initViews();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        mTabHost.setup(this, getSupportFragmentManager(), R.id.fragment_container);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mPosRegisterPresenter = new PosRegisterPresenter(this);

        ValidateManager.get().batchValidate();

        Beta.checkUpgrade(false, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().post(new MainEvent(MainEvent.EID_SHOPCART_DATASET_CHANGED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTabs() {
        MainTab[] tabs = MainTab.values();
        final int size = tabs.length;
        for (int i = 0; i < size; i++) {
            MainTab tab = tabs[i];
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(tab.getResName()));
            View indicator = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.main_tab_indicator, null);
            TextView title = (TextView) indicator.findViewById(R.id.tab_title);
            Drawable drawable = this.getResources().getDrawable(tab.getResIcon());
            title.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            title.setText(getString(tab.getResName()));
            tabSpec.setIndicator(indicator);
            tabSpec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return new View(MainActivity.this);
                }
            });
            mTabHost.addTab(tabSpec, tab.getClz(), null);
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.equals(mTabHost.getCurrentTabView())) {
                        Fragment currentFragment = getCurrentFragment();

                        if (currentFragment != null) {
                            if (currentFragment instanceof MyFragment && !MfhLoginService.get().haveLogined()) {
                                redirectToLogin();
                            }

                            if (currentFragment instanceof OnTabReselectListener) {
                                ((OnTabReselectListener) currentFragment).onTabReselect();
                                return true;
                            }
                        }

//                        if (currentFragment != null && currentFragment instanceof OnTabReselectListener) {
//                            ((OnTabReselectListener) currentFragment).onTabReselect();
//                            return true;
//                        }
                    }
                    return false;
                }
            });
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
    }

    public void onEventMainThread(MainEvent event) {
        ZLogger.d(String.format("MainEvent(%d)", event.getEventId()));
        if (event.getEventId() == MainEvent.EID_SHOPCART_DATASET_CHANGED) {
            //刷新购物车商品数量
            TextView badgeView = (TextView) mTabHost.getBadgeView(2);
            if (badgeView != null) {
                List<PurchaseShopcartEntity> entities = PurchaseShopcartService.getInstance()
                        .fetchFreshEntites();

                if (entities != null && entities.size() > 0) {
                    badgeView.setText(String.valueOf(entities.size()));
                    badgeView.setVisibility(View.VISIBLE);
                } else {
                    badgeView.setVisibility(View.INVISIBLE);
                }
                ZLogger.d("商品数：" + (entities != null ? String.valueOf(entities.size()) : ""));
            } else {
                ZLogger.d("badgeView is null");
            }
        }
    }


    /**
     * 验证
     */
    public void onEventMainThread(ValidateManager.ValidateManagerEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ValidateManagerEvent(%d)", eventId));
        switch (eventId) {
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_START: {
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_LOGIN: {
                redirectToLogin();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_SESSION_EXPIRED: {
                retryLogin();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_PLAT_NOT_REGISTER: {
                showRegisterPlatDialog();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED: {
                Beta.checkUpgrade(false, false);
            }
            break;
        }
    }

    /**
     * 尝试登录
     */
    private void retryLogin() {
        MfhLoginService.get().doLoginAsync(MfhLoginService.get().getLoginName(),
                MfhLoginService.get().getPassword(), new LoginCallback() {
                    @Override
                    public void loginSuccess(UserMixInfo user) {
                        //登录成功
                        ZLogger.d("重登录成功");
                        DialogUtil.showHint("重登录成功");

                        loadOffices();

                        //注册到消息桥
                        IMClient.getInstance().registerBridge();
                    }

                    @Override
                    public void loginFailed(String errMsg) {
                        //登录失败
                        ZLogger.d("重登录失败：" + errMsg);
                        //TODO,这里即使不跳转至登录，但是登录状态已经失效，仍然需要清空登录信息
                        redirectToLogin();
                    }
                });
    }

    /**
     * 加载网点
     */
    private void loadOffices() {
        Office office = null;
        List<Office> offices = MfhLoginService.get().getOffices();
        if (offices != null && offices.size() > 0) {
            office = offices.get(0);
        }

//        DataCacheHelper.getInstance().setCurrentOffice(office);
//        if (office != null) {
//            addressView.setText(office.getValue());
//        } else {
//            addressView.setText("请选择网点");
//        }
    }

    /**
     * 显示退出提示框
     */
    public void showRegisterPlatDialog() {
        CommonDialog dialog = new CommonDialog(this);
        dialog.setMessage("设备未注册，可能会影响使用，是否立刻注册？");
        dialog.setPositiveButton("立刻注册", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mPosRegisterPresenter.create();

            }
        });
        dialog.setNegativeButton("暂不注册", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 跳转至登录页面
     */
    private void redirectToLogin() {
        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);

//        LoginActivity.actionStart(MainActivity.this, null);
//        finish();
    }

    @Override
    public void onRegisterPlatProcess() {

    }

    @Override
    public void onRegisterPlatError(String errorMsg) {

    }

    @Override
    public void onRegisterPlatSuccess(String terminalId) {

    }

    @Override
    public void onPlatUpdate() {

    }
}

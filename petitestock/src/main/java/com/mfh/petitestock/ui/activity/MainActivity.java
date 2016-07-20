package com.mfh.petitestock.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.manfenjiayuan.im.IMClient;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.UserApi;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.MfhModule;
import com.mfh.framework.login.entity.Office;
import com.mfh.framework.login.entity.UserMixInfo;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.AppHelper;
import com.mfh.petitestock.Constants;
import com.mfh.petitestock.R;
import com.mfh.petitestock.bean.wrapper.HomeMenu;
import com.mfh.petitestock.ui.adapter.HomeAdapter;
import com.mfh.petitestock.utils.DataCacheHelper;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class MainActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    private NaviAddressView addressView;
    @Bind(R.id.menu_option)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HomeAdapter menuAdapter;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, MainActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void setupThirdParty() {
        super.setupThirdParty();

//        EventBus.getDefault().register(this);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
//        MobclickAgent.onProfileSignIn(MfhLoginService.get().getCurrentGuId());
//
//        //初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
//        PushManager.getInstance().initialize(getApplicationContext());
    }

    MenuItem menuLogin = null;
    MenuItem menuLogout = null;

    @Override
    protected void initToolBar() {
        super.initToolBar();

        setSupportActionBar(toolbar);

        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_sign_out) {
                    showLogoutAlert();
                } else if (id == R.id.action_sign_in) {
                    redirectToLogin();
                }
                return true;
            }
        });
        addressView = new NaviAddressView(this);
        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MfhLoginService.get().haveLogined()) {
                    Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                    extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_OFFICELIST);

                    Intent intent = new Intent(MainActivity.this, ServiceActivity.class);
                    intent.putExtras(extras);
                    startActivityForResult(intent, Constants.ARC_OFFICE_LIST);
                } else {
                    DialogUtil.showHint("请先登录");
//                    redirectToLogin();
                }
            }
        });
        toolbar.addView(addressView);

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        super.onCreate(savedInstanceState);

        //hide soft input
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initMenus();

        loadOffices();

        //验证登录状态是否有效
        if (MfhLoginService.get().haveLogined()) {
            validSession();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshToolbar();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ZLogger.d("onConfigurationChanged");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ZLogger.d("onCreateOptionsMenu");
        menuLogin = menu.findItem(R.id.action_sign_in);
        menuLogout = menu.findItem(R.id.action_sign_out);
//        MenuItemCompat.setActionView(settings, R.layout.view_corner_button);
//        final Button btnSettings = (Button) settings.getActionView().findViewById(R.id.corner_button);
//        btnSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UIHelper.redirectToActivity(UserActivity.this, SettingsActivity.class);
//            }
//        });

        refreshToolbar();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_OFFICE_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    refreshOffice();
                }
            }
            break;
            case Constants.ARC_NATIVE_LOGIN: {
                if (resultCode == Activity.RESULT_OK) {
                    DialogUtil.showHint("登录成功");
                    loadOffices();

                    configMenuOptions();
                    //注册到消息桥
                    IMClient.getInstance().registerBridge();
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 实现再按一次退出提醒
     */
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
        return super.onKeyDown(keyCode, event);
    }

    private void refreshToolbar() {
        if (MfhLoginService.get().haveLogined()) {
            if (menuLogin != null) {
                menuLogin.setVisible(false);
            }
            if (menuLogout != null) {
                menuLogout.setVisible(true);
            }
        } else {
            if (menuLogin != null) {
                menuLogin.setVisible(true);
            }
            if (menuLogout != null) {
                menuLogout.setVisible(false);
            }
        }
    }

    /**
     * 初始化快捷菜单
     */
    private void initMenus() {
        mRLayoutManager = new GridLayoutManager(this, 3);
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));
        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

        menuAdapter = new HomeAdapter(this, null);
        menuAdapter.setOnAdapterLitener(new HomeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onCommandSelected(HomeMenu option) {
                processMenuOption(option.getId());
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);

        configMenuOptions();
    }

    private void configMenuOptions() {
        MfhUserManager.getInstance().updateModules();

        List<HomeMenu> menus = new ArrayList<>();

        if (MfhUserManager.getInstance().containsModule(MfhModule.SUPM_MANAGER)) {
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_GOOODS, "门店商品", R.mipmap.ic_store));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_PACKAGE, "包裹", R.mipmap.ic_package));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_TAKE, "盘点", R.mipmap.ic_stocktake));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_DISTRIBUTION, "收货", R.mipmap.ic_distribution));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_WHOLESALER_GOODS_SHELVES,
                    "货架", R.mipmap.ic_shelves));

        } else if (MfhUserManager.getInstance().containsModule(MfhModule.CHAIN_MANAGER)) {
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_WHOLESALER_GOODS, "仓库商品", R.mipmap.ic_warehouse));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_TAKE, "盘点", R.mipmap.ic_stocktake));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_DISTRIBUTION, "收货", R.mipmap.ic_distribution));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_WHOLESALER_GOODS_SHELVES,
                    "货架", R.mipmap.ic_shelves));
        } else {
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_GOOODS, "商品", R.mipmap.ic_goods));
        }

        menus.add(new HomeMenu(HomeMenu.OPTION_ID_ALPHA,
                "拣货", R.mipmap.ic_packing_goods));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_ALPHA,
                "调库", R.mipmap.ic_distribution_goods));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_ALPHA,
                "退货", R.mipmap.ic_return_goods));

        menuAdapter.setOptions(menus);

        //检查更新
        UmengUpdateAgent.update(this);
    }

    private void processMenuOption(Long id) {
        if (id == null) {
            return;
        }
//
        if (!MfhLoginService.get().haveLogined()) {
            DialogUtil.showHint("请先登录");
            return;
        }

        if (id.compareTo(HomeMenu.OPTION_ID_PACKAGE) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_PACKAGE);
            ServiceActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK) == 0) {
            Bundle extras = new Bundle();
            extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_STOCK);
            ServiceActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_TAKE) == 0) {
            Office office = DataCacheHelper.getInstance().getCurrentOffice();
            if (office == null) {
                //TODO,请先选择网点。。。
                DialogUtil.showHint("请先选择网点");
                return;
            }

            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_INVENTORY_CHECK);
            ServiceActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_GOOODS) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_GOODS);
            ServiceActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_DISTRIBUTION) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_DISTRIBUTION);
            ServiceActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_WHOLESALER_GOODS) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FT_WHOLESALER_GOODS);
            ServiceActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_WHOLESALER_GOODS_SHELVES) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FT_WHOLESALER_GOODSSHELVES);
            ServiceActivity.actionStart(MainActivity.this, extras);
        } else {
            DialogUtil.showHint("开发君失踪了...");
        }
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

        DataCacheHelper.getInstance().setCurrentOffice(office);
        if (office != null) {
            addressView.setText(office.getValue());
        } else {
            addressView.setText("请选择网点");
        }
    }

    /**
     * 刷新当前网点
     */
    private void refreshOffice() {
        Office office = DataCacheHelper.getInstance().getCurrentOffice();
        if (office != null) {
            addressView.setText(office.getValue());
        } else {
            addressView.setText("请选择网点");
        }
    }

    /**
     * 跳转至登录页面
     */
    private void redirectToLogin() {
        refreshToolbar();
        MobclickAgent.onProfileSignOff();
        AppHelper.resetMemberAccountData();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_OFFICELIST);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);

//        LoginActivity.actionStart(MainActivity.this, null);
//        finish();
    }

    /**
     * 登录状态验证:进入需要登录的功能时需要
     */
    private void validSession() {
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        ZLogger.d("登录状态有效 ");
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        {"code":"1","msg":"会话已失效，请重新登录","version":"1","data":""}
                        ZLogger.d("登录状态已失效, " + errMsg);

                        //已过期，跳转到登录页面
                        //已过期，自动重登录
//                        animProgress.setVisibility(View.GONE);
                        retryLogin();
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };


        UserApi.validSession(responseCallback);
    }

    /**
     * 尝试登录
     */
    private void retryLogin() {
        MfhLoginService.get().doLoginAsync(MfhLoginService.get().getLoginName(), MfhLoginService.get().getPassword(), new LoginCallback() {
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
     * 显示退出提示框
     */
    public void showLogoutAlert() {
        CommonDialog dialog = new CommonDialog(this);
        dialog.setMessage(R.string.dialog_message_logout);
        dialog.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                logout();

            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 退出当前账号
     */
    private void logout() {
        MfhUserManager.getInstance().logout(new Callback() {
            @Override
            public void onSuccess() {
                redirectToLogin();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                redirectToLogin();
            }
        });
    }

}

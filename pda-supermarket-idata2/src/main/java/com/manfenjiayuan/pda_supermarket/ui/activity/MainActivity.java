package com.manfenjiayuan.pda_supermarket.ui.activity;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.manfenjiayuan.business.presenter.PosRegisterPresenter;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.business.view.IPosRegisterView;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ValidateManager;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.HomeMenu;
import com.manfenjiayuan.pda_supermarket.ui.adapter.HomeAdapter;
import com.manfenjiayuan.pda_supermarket.ui.dialog.SelectOfficeDialog;
import com.manfenjiayuan.pda_supermarket.utils.DataCacheHelper;
import com.mfh.framework.BizConfig;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.entity.Office;
import com.mfh.framework.login.entity.UserMixInfo;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration;
import com.tencent.bugly.beta.Beta;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class MainActivity extends IData95Activity implements IPosRegisterView {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    private NaviAddressView addressView;
    @Bind(R.id.menu_option)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HomeAdapter menuAdapter;
    private SelectOfficeDialog mSelectOfficeDialog = null;

    private PosRegisterPresenter mPosRegisterPresenter;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, MainActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }


//    @Override
//    protected boolean finishScannerWhenDestroyEnabled() {
//        return false;
//    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void setupThirdParty() {
        super.setupThirdParty();

//        EventBus.getDefault().register(this);
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
                    selectOffice();
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

//        hideSystemUI();
        super.onCreate(savedInstanceState);

        //hide soft input
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        EventBus.getDefault().register(this);

        mPosRegisterPresenter = new PosRegisterPresenter(this);

        if (!BizConfig.RELEASE) {
            DialogUtil.showHint("您正在使用的是测试版本，如需切换到正式版本请联系服务商。");
        }

        initMenus();

        loadOffices();

        ValidateManager.get().batchValidate();
        Beta.checkUpgrade(false, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        hideSystemUI();

        refreshToolbar();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

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

        menus.add(new HomeMenu(HomeMenu.OPTION_ID_GOOODS, "商品", R.mipmap.ic_goods));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_TAKE, "盘点", R.mipmap.ic_stocktake));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_BIND_GOODS_2_TAGS,
                "电子价签", R.mipmap.ic_bind_tags));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_DISTRIBUTION,
                "收货", R.mipmap.ic_receive_goods));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER,
                "退货", R.mipmap.ic_return_goods));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER,
                "拣货", R.mipmap.ic_pick_goods));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_OUT,
                "出库", R.mipmap.ic_stock_out));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_IN,
                "入库", R.mipmap.ic_stock_in));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_LOSSORDER,
                "报损", R.mipmap.ic_report_loss));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_PRINT_TAGS,
                "价签打印", R.mipmap.ic_print_tags));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_PACKAGE,
                "取包裹", R.mipmap.ic_package));
        menus.add(new HomeMenu(HomeMenu.OPTION_ID_INV_CONVERT,
                "库存转换", R.mipmap.ic_inv_convert));

        menuAdapter.setEntityList(menus);
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
        if (id.compareTo(HomeMenu.OPTION_ID_GOOODS) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_GOODS);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_PACKAGE) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_PACKAGE);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_TAKE) == 0) {
            Office office = DataCacheHelper.getInstance().getCurrentOffice();
            if (office == null) {
                //TODO,请先选择网点。。。
                DialogUtil.showHint("请先选择网点");
                return;
            }

            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_INVENTORY_CHECK);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_DISTRIBUTION) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_DISTRIBUTION);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_BIND_GOODS_2_TAGS) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_BIND_GOODS_2_TAGS);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_CREATE_INV_LOSSORDER) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_CREATE_INV_LOSSORDER);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_CREATE_INV_RETURNORDER);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_INV_CONVERT) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_INV_CONVERT);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_IN) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_INVIO_IN);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        }  else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_OUT) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_INVIO_OUT);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        }  else if (id.compareTo(HomeMenu.OPTION_ID_PRINT_TAGS) == 0) {
            Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_PRINT_PRICETAGS);
            PrimaryActivity.actionStart(MainActivity.this, extras);
        }  else {
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
     * 选择网点
     */
    private void selectOffice() {
        if (mSelectOfficeDialog == null) {
            mSelectOfficeDialog = new SelectOfficeDialog(this);
            mSelectOfficeDialog.setCancelable(true);
            mSelectOfficeDialog.setCanceledOnTouchOutside(false);
        }
        mSelectOfficeDialog.init(MfhLoginService.get().getOffices(), new SelectOfficeDialog.OnDialogListener() {
            @Override
            public void onItemSelected(Office office) {
                DataCacheHelper.getInstance().setCurrentOffice(office);
                if (office != null) {
                    addressView.setText(office.getValue());
                } else {
                    addressView.setText("请选择网点");
                }
            }
        });
        if (!mSelectOfficeDialog.isShowing()) {
            mSelectOfficeDialog.show();
        }
    }


    /**
     * 跳转至登录页面
     */
    private void redirectToLogin() {
        refreshToolbar();
        AppHelper.resetMemberAccountData();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);

//        LoginActivity.actionStart(MainActivity.this, null);
//        finish();
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
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_SESSION_EXPIRED: {
                retryLogin();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_LOGIN: {
                redirectToLogin();
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

    private void selectReceiveOrderType() {
        final CommonDialog dialog = DialogHelper
                .getPinterestDialogCancelable(this);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                switch (id) {
                    case R.id.tv_invioorder: {
                        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_RECEIVEORDER_INVIOORDER);
//        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

                        UIHelper.startActivity(MainActivity.this, SecondaryActivity.class, extras);
                    }
                    break;
                    case R.id.tv_select_sendorder: {
                        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_SENDORDER);
//        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

                        UIHelper.startActivity(MainActivity.this, SecondaryActivity.class, extras);
                    }
                    break;
                    case R.id.tv_create_neworder: {
                        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_RECVDORDER_CREATE);
//        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

                        UIHelper.startActivity(MainActivity.this, SecondaryActivity.class, extras);
                    }
                    break;
                    default:
                        break;
                }
            }
        };

        View view = LayoutInflater.from(this).inflate(
                R.layout.dialogview_receiveorder_type, null);
        view.findViewById(R.id.tv_create_neworder).setOnClickListener(click);
        view.findViewById(R.id.tv_select_sendorder).setOnClickListener(click);

        dialog.setContent(view, 0);
        dialog.show();
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

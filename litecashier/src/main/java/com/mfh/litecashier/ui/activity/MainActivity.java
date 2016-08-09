package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.im.IMClient;
import com.mfh.comn.config.UConfig;
import com.mfh.framework.BizConfig;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.DrawableUtils;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.entity.UserMixInfo;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.ComBean;
import com.mfh.litecashier.com.SMScaleDigiAgent;
import com.mfh.litecashier.com.SerialHelper;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.entity.CompanyHumanEntity;
import com.mfh.litecashier.database.logic.CommonlyGoodsService;
import com.mfh.litecashier.database.logic.PosProductService;
import com.mfh.litecashier.database.logic.PosProductSkuService;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.event.CashierAffairEvent;
import com.mfh.litecashier.event.SerialPortEvent;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.service.OrderSyncManager;
import com.mfh.litecashier.service.ValidateManager;
import com.mfh.litecashier.ui.dialog.AccountDialog;
import com.mfh.litecashier.ui.dialog.ResumeMachineDialog;
import com.mfh.litecashier.ui.dialog.SelectCompanyHumanDialog;
import com.mfh.litecashier.ui.fragment.ReportFragment;
import com.mfh.litecashier.ui.fragment.cashier.CashierFragment;
import com.mfh.litecashier.ui.fragment.cashier.CommonlyFragment;
import com.mfh.litecashier.ui.fragment.cashier.ExpressFragment;
import com.mfh.litecashier.ui.fragment.cashier.FrontCategoryFragment;
import com.mfh.litecashier.ui.fragment.cashier.LaundryFragment;
import com.mfh.litecashier.ui.fragment.components.DailySettleFragment;
import com.mfh.litecashier.ui.fragment.inventory.InventoryCostFragment;
import com.mfh.litecashier.ui.fragment.message.MessageFragment;
import com.mfh.litecashier.ui.fragment.orderflow.OrderFlowFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFragment;
import com.mfh.litecashier.ui.fragment.settings.SettingsFragment;
import com.mfh.litecashier.ui.widget.LeftTabStrip;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.AlarmManagerHelper;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.DataCacheHelper;
import com.mfh.litecashier.utils.FreshShopcartHelper;
import com.mfh.litecashier.utils.PurchaseShopcartHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;
import com.umeng.update.UmengUpdateAgent;

import net.tsz.afinal.FinalDb;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.bingshanguxue.cashier.android_serialport_api.SerialPortFinder;
import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class MainActivity extends BaseActivity {

    @Bind(R.id.iv_header)
    AvatarView ivHeader;
    @Bind(R.id.tv_username)
    TextView tvUsername;
    @Bind(R.id.tab_homemenu)
    LeftTabStrip leftSlidingTabStrip;

    DispQueueThread DispQueue;//刷新显示线程
    private SerialPortFinder mSerialPortFinder;//串口设备搜索
    private SerialControl comDisplay, comPrint, comScale;//串口

    private AccountDialog lockMachineDialog = null;
    private ResumeMachineDialog resumeMachineDialog = null;
    private SelectCompanyHumanDialog selectCompanyHumanDialog = null;

    private static final int TAB_INDEX_CASHIER = 0;
    private static final int TAB_INDEX_ORDERFLOW = 1;
    private static final int TAB_INDEX_COMMODITY = 2;
    private static final int TAB_INDEX_RESTOCK = 3;//库存
    private static final int TAB_INDEX_MESSAGE = 4;
    private static final int TAB_INDEX_SETTINGS = 5;
    private static final int TAB_INDEX_REPORT = 6;//报表
    private CommonlyFragment commonlyFragment;//常用
    private ExpressFragment expressFragment;//快递
    private LaundryFragment laundryFragment;// 洗衣
    private FrontCategoryFragment frontCategoryFragment;//前台类目

    private CashierFragment cashierFragment;//收银
    private InventoryCostFragment mInventoryCostFragment;//库存
    private PurchaseFragment purchaseFragment;//采购
    private OrderFlowFragment orderFlowFragment;//流水
    private MessageFragment messageFragment;//消息
    private ReportFragment reportFragment;//报表
    private SettingsFragment settingsFragment;//设置

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, MainActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
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
//        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

//        hideSystemUI();

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
//        MobclickAgent.onProfileSignIn(MfhLoginService.get().getCurrentGuId());

        AlarmManagerHelper.registerDailysettle(this);

        initCOM();

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //清空商品采购购物车
        PurchaseShopcartHelper.getInstance().clear();
        FreshShopcartHelper.getInstance().clear();

        ivHeader.setBorderWidth(2);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));

        initTabs();
        initFragments();

        //显示收银页面
        showCashierFragment();

        ValidateManager.get().batchValidate();

        reload(false);

//        startActivity(new Intent(this, NfcActivity.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ZLogger.d("MainActivity.onPause");

        if (lockMachineDialog != null) {
            lockMachineDialog.dismiss();
        }
        if (resumeMachineDialog != null) {
            resumeMachineDialog.dismiss();
        }
        if (selectCompanyHumanDialog != null) {
            selectCompanyHumanDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        hideSystemUI();
    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        //Any time the window receives focus, simply set the IMMERSIVE mode.
//        if (hasFocus) {
//            hideSystemUI();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lockMachineDialog != null) {
            lockMachineDialog.dismiss();
            lockMachineDialog = null;
        }
        if (resumeMachineDialog != null) {
            resumeMachineDialog.dismiss();
            resumeMachineDialog = null;
        }
        if (selectCompanyHumanDialog != null) {
            selectCompanyHumanDialog.dismiss();
            selectCompanyHumanDialog = null;
        }

        EventBus.getDefault().unregister(this);

        DataCacheHelper.getInstance().reset();

        AppHelper.clearTempData();

        //关闭串口
        CloseComPort(comDisplay);
        CloseComPort(comPrint);
        CloseComPort(comScale);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ZLogger.d("onConfigurationChanged" + newConfig.toString());
        CloseComPort(comDisplay);
        CloseComPort(comPrint);
        CloseComPort(comScale);

        setControls();
    }

    @Override
    public void onBackPressed() {
        String dbName;
        if (BizConfig.RELEASE) {
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON, UConfig.CONFIG_PARAM_DB_NAME, "mfh_cashier_release.db");
        } else {
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON, "dev." + UConfig.CONFIG_PARAM_DB_NAME, "mfh_cashier_dev.db");
        }
        ZLogger.d("关闭数据库:" + dbName);
        FinalDb db = FinalDb.getDb(dbName);
        if (db != null) {
            db.close();
        }
        System.exit(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_NATIVE_LOGIN: {
                if (resultCode == Activity.RESULT_OK) {
                    DialogUtil.showHint("登录成功");
                    if (leftSlidingTabStrip.getCurrentPosition() == TAB_INDEX_CASHIER) {
                        //初始化收银
                        EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_RESET_CASHIER));
                    }

                    reload(true);
                }
            }
            break;
            case Constants.ARC_HANDOVER: {
                if (resultCode == Activity.RESULT_OK) {
                    handoverSelectAccount();
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
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                && event.getAction() == KeyEvent.ACTION_DOWN) {
//
////            if ((System.currentTimeMillis() - exitTime) > 3000) {
////                DialogUtil.showHint("再按一次将退出程序");
////                exitTime = System.currentTimeMillis();
////            } else {
//////                List<PosOrderEntity> syncPosOrderList = PosOrderService.get().getSyncOrderList();
//////                if ( syncPosOrderList != null && syncPosOrderList.size() > 0){
//////                    showSyncDataAlert(syncPosOrderList.size());
//////                }else{
//                    finish();
//////                }
////            }
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }


//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            ZLogger.d("失去焦点");
//            return;
//        }
//        ZLogger.d("获得焦点");
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//            getWindow().getDecorView()
//                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN// hide status bar
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//        else{
//            int mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//
//            getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
//        }
//    }

    /**
     * 单击头像
     */
    @OnClick(R.id.iv_header)
    public void clickHeader() {
        if (MfhLoginService.get().haveLogined()) {
            if (lockMachineDialog == null) {
                lockMachineDialog = new AccountDialog(this);
                lockMachineDialog.setCancelable(false);
                lockMachineDialog.setCanceledOnTouchOutside(true);
            }
            lockMachineDialog.init(1, new AccountDialog.DialogClickListener() {
                @Override
                public void onLock() {
                    lockMachine();
                }

                @Override
                public void onHandOver() {
                    handoverAnalysis();
                }

                @Override
                public void onDailySettle() {
                    dailySettle(null, true);
                }

                @Override
                public void onLogout() {

                    logout();
                }
            });

            lockMachineDialog.show();
        } else {
            redirectToLogin();
        }
    }

    /**
     * 显示收银页面
     */
    public void showCashierFragment() {
        hideRightSideFragment();

        leftSlidingTabStrip.selectedTab(TAB_INDEX_CASHIER);
//        hideContentFragment();
        if (cashierFragment == null) {
            cashierFragment = new CashierFragment();
        }

        if (cashierFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(orderFlowFragment).hide(messageFragment).hide(reportFragment)
                    .hide(settingsFragment).hide(mInventoryCostFragment).hide(purchaseFragment)
                    .show(cashierFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_1, cashierFragment)
                    .hide(orderFlowFragment).hide(messageFragment).hide(reportFragment)
                    .hide(settingsFragment).hide(mInventoryCostFragment).hide(purchaseFragment)
                    .show(cashierFragment).commit();
        }

        if (MfhLoginService.get().haveLogined()
                && SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_PRODUCTS_ENABLED, true)) {
            DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
        }
        EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_START_CASHIER));

        //打开秤的串口
        if (!comScale.isOpen()) {
            OpenComPort(comScale);
        }

//        DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO);
    }

    /**
     * 显示流水页面
     */
    public void showOrderFlowFragment() {
        //设置需要更新门店收银流水
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STORE_ORDERFLOW_ENABLED, true);

        hideRightSideFragment();
        leftSlidingTabStrip.selectedTab(TAB_INDEX_ORDERFLOW);
        if (orderFlowFragment == null) {
            orderFlowFragment = new OrderFlowFragment();
        }

        if (orderFlowFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(mInventoryCostFragment).hide(messageFragment).hide(reportFragment)
                    .hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(orderFlowFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_1, orderFlowFragment)
                    .hide(mInventoryCostFragment).hide(messageFragment).hide(reportFragment)
                    .hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(orderFlowFragment).commit();
        }
        orderFlowFragment.refresh();

        //清空消息数
        resetUnreadOrder();

        //同步订单信息
        OrderSyncManager.get().sync();
    }

    /**
     * 显示库存页面
     */
    public void showStockFragment() {
        //设置数据同步状态
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STOCKCHECK_ORDER_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STOCKLOSS_ORDER_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVIOORDER_IN_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVIOORDER_OUT_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVTRANSORDER_IN_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVTRANSORDER_OUT_ENABLED, true);

        hideRightSideFragment();
        leftSlidingTabStrip.selectedTab(TAB_INDEX_COMMODITY);
        if (mInventoryCostFragment == null) {
            mInventoryCostFragment = new InventoryCostFragment();
        }

        if (mInventoryCostFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(orderFlowFragment).hide(messageFragment).hide(reportFragment).hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(mInventoryCostFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_1, mInventoryCostFragment)
                    .hide(orderFlowFragment).hide(messageFragment).hide(reportFragment).hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(mInventoryCostFragment).commit();
        }
//        mInventoryCostFragment.refresh();

        DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO);
    }

    /**
     * 采购
     */
    public void showPurchaseFragment() {
        if (!MfhLoginService.get().haveLogined()) {
            redirectToLogin();
            return;
        }

        leftSlidingTabStrip.selectedTab(TAB_INDEX_RESTOCK);

        //设置数据同步状态
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PURCHASESEND_ORDER_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PURCHASERECEIPT_ORDER_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PURCHASERETURN_ORDER_ENABLED, true);

        hideRightSideFragment();
        if (purchaseFragment == null) {
            purchaseFragment = new PurchaseFragment();
        }

        if (purchaseFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(orderFlowFragment).hide(messageFragment).hide(reportFragment)
                    .hide(settingsFragment).hide(cashierFragment).hide(mInventoryCostFragment)
                    .show(purchaseFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_1, purchaseFragment)
                    .hide(orderFlowFragment).hide(messageFragment).hide(reportFragment)
                    .hide(settingsFragment).hide(cashierFragment).hide(mInventoryCostFragment)
                    .show(purchaseFragment).commit();
        }
        purchaseFragment.refresh();

        DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO);
    }


    /**
     * 显示消息页面
     */
    public void showMessageFragment() {
        hideRightSideFragment();
        leftSlidingTabStrip.selectedTab(TAB_INDEX_MESSAGE);
        if (messageFragment == null) {
            messageFragment = new MessageFragment();
        }

        if (messageFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(mInventoryCostFragment).hide(orderFlowFragment).hide(reportFragment).hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(messageFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_1, messageFragment)
                    .hide(mInventoryCostFragment).hide(orderFlowFragment).hide(reportFragment).hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(messageFragment).commit();
        }
        messageFragment.refresh();
    }

    /**
     * 显示报表页面
     */
    public void showReportFragment() {
        hideRightSideFragment();
        leftSlidingTabStrip.selectedTab(TAB_INDEX_REPORT);
        if (reportFragment == null) {
            reportFragment = new ReportFragment();
        }

        if (reportFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(mInventoryCostFragment).hide(orderFlowFragment).hide(messageFragment).hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(reportFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_1, reportFragment)
                    .hide(mInventoryCostFragment).hide(orderFlowFragment).hide(messageFragment).hide(settingsFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(reportFragment).commit();
        }
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_1, orderFragment)
////                .hide(commodityFragment)
//                .show(orderFragment)
//                .commit();
        reportFragment.loadData();
    }

    /**
     * 显示设置页面
     */
    public void showSettingsFragment() {
        hideRightSideFragment();
        leftSlidingTabStrip.selectedTab(TAB_INDEX_SETTINGS);
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }

        if (settingsFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(mInventoryCostFragment).hide(orderFlowFragment).hide(messageFragment).hide(reportFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(settingsFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_1, settingsFragment)
                    .hide(mInventoryCostFragment).hide(orderFlowFragment).hide(messageFragment).hide(reportFragment).hide(cashierFragment).hide(purchaseFragment)
                    .show(settingsFragment).commit();
        }
        settingsFragment.refresh();
    }

    /**
     * 数据同步
     *
     * @param isSlient true:后台同步数据；false:显示进度对话框。
     */
    private void dataSync(boolean isSlient) {
        refreshHumanInfo();

        //账号发生改变
        if (MfhLoginService.get().isCompanyOrOfficeChanged()) {
            PurchaseShopcartHelper.getInstance().clear();
            FreshShopcartHelper.getInstance().clear();
            CommonlyGoodsService.get().clear();// 清空常用商品
            PosProductService.get().clear();
            PosProductSkuService.get().clear();
            SharedPreferencesHelper.setSyncProductsCursor("");
            SharedPreferencesHelper.setPosSkuLastUpdate("");
        }

//        AppHelper.clearCache();
//        hideSyncDataDialog();
        if (isSlient) {
            DataSyncManager.get().sync();
        } else {
            showSyncDataDialog();
        }
    }

    private void reload(boolean isSlient) {
        //设置需要更新前台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_FRONTCATEGORY_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_CUSTOM_FRONTCATEGORY_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_LAUNDRY_FRONTCATEGORY_ENABLED, true);
        //设置需要更新前台类目
        SharedPreferencesHelper.setSyncFrontCategorySubEnabled(true);
        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED, true);

        //清除缓存数据
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME).clear();

//        hideRightSideFragment();

        dataSync(true);
    }

    /**
     * 显示同步数据对话框
     */
    public void showSyncDataDialog() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在同步数据...", false);
        DataSyncManager.get().sync();
    }

    /**
     * 锁定机器
     */
    private void lockMachine() {
        CompanyHumanEntity human = new CompanyHumanEntity();
        human.setPassword(MfhLoginService.get().getPassword());
        human.setHeaderUrl(MfhLoginService.get().getHeadimage());
        human.setName(MfhLoginService.get().getHumanName());

        if (resumeMachineDialog == null) {
            resumeMachineDialog = new ResumeMachineDialog(this);
            resumeMachineDialog.setCancelable(false);
            resumeMachineDialog.setCanceledOnTouchOutside(false);
        }
        resumeMachineDialog.init(ResumeMachineDialog.DTYPE_LOCK, human, null);
        resumeMachineDialog.show();
    }

    /**
     * 交接班－－统计交接班数据
     */
    private void handoverAnalysis() {
        Intent intent = new Intent(this, SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_HANDOVER);
//        extras.putString(SelectPlatformGoodsFragment.EXTRA_KEY_BARCODE, barcode);

        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_HANDOVER);
    }

    /**
     * 交接班－－选择交接班账号
     */
    private void handoverSelectAccount() {
        if (selectCompanyHumanDialog == null) {
            selectCompanyHumanDialog = new SelectCompanyHumanDialog(this);
            selectCompanyHumanDialog.setCancelable(false);
            selectCompanyHumanDialog.setCanceledOnTouchOutside(false);
        }
        selectCompanyHumanDialog.setOnDialogClickListener(new SelectCompanyHumanDialog.DialogClickListener() {
            @Override
            public void onSelectHuman(CompanyHumanEntity entity) {
                handoverConfirmAccount(entity);
            }
        });
        selectCompanyHumanDialog.show();
    }

    /**
     * 交接班－－确认交接班账号
     */
    private void handoverConfirmAccount(CompanyHumanEntity entity) {
        if (resumeMachineDialog == null) {
            resumeMachineDialog = new ResumeMachineDialog(this);
            resumeMachineDialog.setCancelable(false);
            resumeMachineDialog.setCanceledOnTouchOutside(false);
        }

        resumeMachineDialog.init(ResumeMachineDialog.DTYPE_HANDOVER, entity, new ResumeMachineDialog.DialogClickListener() {
            @Override
            public void onChangeHuman() {

                //初始化收银,createdBy(humanId)已经改变
                EventBus.getDefault().post(new CashierAffairEvent(com.mfh.litecashier.event.CashierAffairEvent.EVENT_ID_RESET_CASHIER));
                retryLogin(true);
            }
        });
        resumeMachineDialog.show();
    }

    /**
     * 日结－
     */
    private void dailySettle(String datetime, boolean cancelable) {
//        ZLogger.df(String.format("准备日结：datetime = %s, cancelable = %b", datetime, cancelable));
        Intent intent = new Intent(this, SimpleDialogActivity.class);
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_DAILY_SETTLE);
        extras.putString(DailySettleFragment.EXTRA_KEY_DATETIME, datetime);
        extras.putBoolean(DailySettleFragment.EXTRA_KEY_CANCELABLE, cancelable);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void initTabs() {
//        DensityUtil.dip2px(this, getResources().getDimension(R.dimen.home_leftslide_width));
        //setupViewPager
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果
//        leftSlidingTabStrip.setDisableViewPager(true);
        leftSlidingTabStrip.setOnClickTabListener(new LeftTabStrip.OnClickTabListener() {
            @Override
            public void onClickTab(View tab, int index) {
                DataCacheHelper.getInstance().reset();

                if (index != TAB_INDEX_CASHIER && index == leftSlidingTabStrip.getCurrentPosition()) {
                    ZLogger.d(String.format("setOnClickTabListener:%d/%d",
                            index, leftSlidingTabStrip.getCurrentPosition()));
                    return;
                }

                switch (index) {
                    case TAB_INDEX_CASHIER: {
                        showCashierFragment();
                    }
                    break;
                    case TAB_INDEX_COMMODITY: {
                        if (MfhLoginService.get().haveLogined()) {
//                            if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//                                DialogUtil.showHint(R.string.toast_network_error);
//                                return;
//                            }

                            showStockFragment();
//                            sendBroadcast(new Intent(Constants.BROADCAST_ACTION_REFRESH_DATA_COMMODITY));
                        } else {
                            redirectToLogin();
                        }
                    }
                    break;
                    case TAB_INDEX_RESTOCK: {
                        showPurchaseFragment();
                    }
                    break;
                    case TAB_INDEX_ORDERFLOW: {
                        if (MfhLoginService.get().haveLogined()) {
                            showOrderFlowFragment();
                        } else {
                            redirectToLogin();
                        }
                    }
                    break;
                    case TAB_INDEX_MESSAGE:
                        showMessageFragment();
                        break;
                    case TAB_INDEX_REPORT:
                        showReportFragment();
                        break;
                    case TAB_INDEX_SETTINGS:
                        showSettingsFragment();
//                        redirectToSettings();
                        break;
                    default:
                        break;
                }
            }
        });

        addTab(new ViewPageInfo("收银", R.mipmap.ic_leftmenu_cashier_normal));
        addTab(new ViewPageInfo("流水", R.mipmap.ic_leftmenu_restock_normal));
        addTab(new ViewPageInfo("库存", R.mipmap.ic_leftmenu_stock_normal));
        addTab(new ViewPageInfo("采购", R.mipmap.ic_leftmenu_order_normal));
        addTab(new ViewPageInfo("消息", R.mipmap.ic_leftmenu_message_normal));
//        addTab(new ViewPageInfo("报表", R.mipmap.ic_leftmenu_report_normal));
        addTab(new ViewPageInfo("设置", R.mipmap.ic_leftmenu_settings_normal));
        leftSlidingTabStrip.selectedTab(TAB_INDEX_CASHIER);
    }

    private final class ViewPageInfo {
        public final String title;
        public final int resId;

        public ViewPageInfo(String title, int resId) {
            this.title = title;
            this.resId = resId;
        }
    }

    private void addTab(ViewPageInfo viewPageInfo) {
        View v = LayoutInflater.from(CashierApp.getAppContext()).inflate(R.layout.tabitem_home, null, false);
        v.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(this, getResources().getDimension(R.dimen.home_leftslide_width)),
                DensityUtil.dip2px(this, getResources().getDimension(R.dimen.home_leftslide_height))));

        TextView title = (TextView) v.findViewById(R.id.tv_buttonText);
        if (title != null) {
            title.setText(viewPageInfo.title);
        }
        v.setTag(R.id.tab_title, viewPageInfo.title);

        ImageView icon = (ImageView) v.findViewById(R.id.iv_buttonImage);
//        icon.setImageResource(viewPageInfo.resId);
        // 使用着色
        icon.setImageDrawable(null);
        icon.setBackgroundResource(viewPageInfo.resId);
        icon.setBackground(DrawableUtils.tintDrawable(icon.getBackground(),
                getResources().getColorStateList(R.color.maintab_tint_colors)));

        leftSlidingTabStrip.addTab(v);
    }

    private void initFragments() {
        commonlyFragment = new CommonlyFragment();
        mInventoryCostFragment = new InventoryCostFragment();
        orderFlowFragment = new OrderFlowFragment();
        purchaseFragment = new PurchaseFragment();
        messageFragment = new MessageFragment();
        reportFragment = new ReportFragment();
        settingsFragment = new SettingsFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_1, mInventoryCostFragment).hide(mInventoryCostFragment)
                .add(R.id.fragment_container_1, orderFlowFragment).hide(orderFlowFragment)
                .add(R.id.fragment_container_1, purchaseFragment).hide(purchaseFragment)
                .add(R.id.fragment_container_1, messageFragment).hide(messageFragment)
                .add(R.id.fragment_container_1, reportFragment).hide(reportFragment)
                .add(R.id.fragment_container_1, settingsFragment).hide(settingsFragment)
                .commit();
    }

    /**
     * 退出当前账号
     */
    private void logout() {

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在退出当前账号...", false);
//                    // 保存统计数据
//                    MobclickAgent.onKillProcess(CashierApp.getAppContext());
//
//                    //退出程序
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    System.exit(0);
        MfhUserManager.getInstance().logout(new Callback() {
            @Override
            public void onSuccess() {
//                showProgressDialog(ProgressDialog.STATUS_DONE, "正在退出当前账号...", false);
                hideProgressDialog();
                redirectToLogin();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
//                showProgressDialog(ProgressDialog.STATUS_ERROR, "正在退出当前账号...", true);
                hideProgressDialog();
                redirectToLogin();
            }
        });
    }

    /**
     * 跳转至登录页面,清空账号信息
     */
    private void redirectToLogin() {
        // 重置数据更新标志，避免登录其他账号导致数据重叠。
        //设置需要更新前台类目
//        SharedPreferencesHelper.setSyncFrontCategorySubEnabled(true);
        //设置需要更新商品中心,商品后台类目
//        SharedPreferencesHelper.setSyncEnabled(SharedPreferencesHelper.PREF_KEY_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);

//        MobclickAgent.onProfileSignOff();
//        AppHelper.resetMemberAccountData();

        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_OFFICELIST);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);
    }

    private class SerialControl extends SerialHelper {

        public SerialControl(String sPort, String sBaudRate) {
            super(sPort, sBaudRate);
        }

        public SerialControl(String sPort, int iBaudRate) {
            super(sPort, iBaudRate);
        }

        public SerialControl() {
        }

        protected void onDataReceived(final ComBean ComRecData) {
            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
            //最终效果差不多-_-，线程定时刷新稍好一些。
            DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
//            StringBuilder sMsg = new StringBuilder();
//            sMsg.append(ComRecData.sRecTime);
//            sMsg.append("[");
//            sMsg.append(ComRecData.sComPort);
//            sMsg.append("]");
//            sMsg.append("[Hex] ");
//            sMsg.append(DataConvertUtil.ByteArrToHex(ComRecData.bRec));
//            sMsg.append("\r\n");
//            ZLogger.d("onDataReceived: " + sMsg);
            /*
            runOnUiThread(new Runnable()//直接刷新显示
			{
				public void run()
				{
					DispRecData(ComRecData);
				}
			});*/
        }
    }

    /**
     * 刷新显示线程
     */
    private class DispQueueThread extends Thread {
        private Queue<ComBean> QueueList = new LinkedList<>();

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                final ComBean ComData;
                while ((ComData = QueueList.poll()) != null) {

                    DispRecData(ComData);
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            DispRecData(ComData);
//                        }
//                    });

                    try {
                        Thread.sleep(100);//显示性能高的话，可以把此数值调小。
                    } catch (Exception e) {
//                        e.printStackTrace();
                        ZLogger.e(e.toString());
                    }
                    break;
                }
            }
        }

        public synchronized void AddQueue(ComBean ComData) {
            QueueList.add(ComData);
        }
    }

    /**
     * 显示接收数据
     */
    private void DispRecData(ComBean ComRecData) {
        StringBuilder sMsg = new StringBuilder();
        sMsg.append(String.format("%s %s [%s] [%s]", ComRecData.sRecTime, ComRecData.sComPort,
                new String(ComRecData.bRec), DataConvertUtil.ByteArrToHex(ComRecData.bRec)));
        ZLogger.d("COM RECV:" + sMsg.toString());
        //[43 0D
        // 30 30 30 2E 30 30 30 0D
        // 34 30 30 2E 30 30 30 0D
        // 55 30 30 30 2E 30 30 0D
        // 54 30 30 30 30 2E 30 30 0D
        // 0A ]
        //＝＝》
        // [C CR
        // 0 0 0 . 0 0 0 CR     1+6+1
        // 4 0 0 . 0 0 0 CR     1+6+1
        // U 0 0 0 . 0 0 CR     1+6+1
        // T 0 0 0 0 . 0 0 CR   1+7+1
        // LF]

//        DialogUtil.showHint("接收串口数据!" + sMsg.toString());
        if (ComRecData.sComPort.equals(SMScaleDigiAgent.PORT_SCALE_DS781)) {
            //TODO,刷新重量,解析数据
//            Intent intent = new Intent(Constants.BROADCAST_ACTION_REFRESH_NET_WEIGHT);
////            intent.putExtra("netWeight", new String(ComRecData.bRec));
//            intent.putExtra("netWeight", ComRecData.bRec);
//            sendBroadcast(intent);

            DataCacheHelper.getInstance().setNetWeight(ComRecData.bRec);
        }
    }

    /**
     * 初始化串口
     */
    private void initCOM() {
        comDisplay = new SerialControl(SerialManager.getLedPort(), SerialManager.getLedBaudrate());
        comPrint = new SerialControl(SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate());
//        comScale = new SerialControl(SerialManager.getScalePort(), SerialManager.getScaleBaudrate());
        comScale = new SerialControl(SMScaleDigiAgent.PORT_SCALE_DS781, SMScaleDigiAgent.BAUDRATE_SCALE_DS781);

        DispQueue = new DispQueueThread();
        DispQueue.start();

        setControls();
    }

    /**
     * 打开串口
     */
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ZLogger.e("打开串口失败:没有串口读/写权限!" + ComPort.getPort());
//            DialogUtil.showHint("打开串口失败:没有串口读/写权限!" + ComPort.getPort());
        } catch (IOException e) {
            ZLogger.e("打开串口失败:未知错误!" + ComPort.getPort());
//            DialogUtil.showHint("打开串口失败:未知错误!" + ComPort.getPort());
        } catch (InvalidParameterException e) {
            ZLogger.e("打开串口失败:参数错误!");
//            DialogUtil.showHint("打开串口失败:参数错误!" + ComPort.getPort());
        }
    }

    /**
     * 关闭串口
     */
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, String sOut, boolean bHex) {
        if (ComPort != null && ComPort.isOpen()) {
//            DialogUtil.showHint("发送串口数据!" + sOut);
            if (bHex) {
                ComPort.sendHex(sOut);
            } else {
                ComPort.sendTxt(sOut);
            }
        }
    }

    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, byte[] bOutArray) {
        if (ComPort != null && ComPort.isOpen()) {
            ComPort.send(bOutArray);
        }
    }

    /**
     * poslab: devices [/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttymxc4, /dev/ttymxc3, /dev/ttymxc2, /dev/ttymxc1, /dev/ttymxc0]
     * JOOYTEC: devices:[/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttyS3, /dev/ttyS1, /dev/ttyS0, /dev/ttyFIQ0]
     */
    public void setControls() {
        mSerialPortFinder = new SerialPortFinder();

        String[] entryValues2 = mSerialPortFinder.getAllDevices();
        List<String> allDevices2 = new ArrayList<>();
        if (entryValues2 != null) {
            Collections.addAll(allDevices2, entryValues2);
        }
        ZLogger.df("devices:" + allDevices2.toString());


        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<>();
        if (entryValues != null) {
            Collections.addAll(allDevices, entryValues);
        }
        ZLogger.df("devices:" + allDevices.toString());
        DataCacheHelper.getInstance().setComDevicesPath(allDevices);//保存devices

        if (allDevices.contains(SerialManager.PORT_PRINTER)) {
            OpenComPort(comPrint);
        }
        if (allDevices.contains(SerialManager.PORT_SCREEN)) {
            OpenComPort(comDisplay);
        }
        if (allDevices.contains(SerialManager.PORT_SCALE)) {
            OpenComPort(comScale);
        }
    }

    /**
     * 验证
     */
    public void onEventMainThread(ValidateManager.ValidateManagerEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("MaiinActivity: ValidateManagerEvent(%d)", eventId));
        switch (eventId) {
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_START: {
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_SESSION_EXPIRED: {
                retryLogin(true);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN: {
                redirectToLogin();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_DAILYSETTLE: {
                dailySettle(args != null ? args.getString("dailysettleDatetime") : null, false);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED: {
                //验证结束开始加载数据
//                dataSync(false);
            }
            break;
        }
    }

    /**
     * 同步
     */
    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
        int eventId = event.getEventId();

        ZLogger.d(String.format("MaiinActivity: DataSyncEvent(%d)", eventId));
        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            hideProgressDialog();
            //同步数据结束后开始同步订单
            OrderSyncManager.get().sync();
        }
    }

    public void onEventMainThread(AffairEvent event) {
        //有新订单
        if (event.getAffairId() == AffairEvent.EVENT_ID_APPEND_UNREAD_ORDER) {
            if (leftSlidingTabStrip.getCurrentPosition() == TAB_INDEX_ORDERFLOW) {
                resetUnreadOrder();

                //刷新数据
                if (orderFlowFragment != null && orderFlowFragment.isAdded()) {
                    orderFlowFragment.refresh();
                }
            } else {
                //显示数字
                DataCacheHelper.getInstance().appendUnreadOrder();

                int number = DataCacheHelper.getInstance().getUnreadOrder();
                TextView tvBadge = ((TextView) leftSlidingTabStrip.getBadgeView(2));
                if (number > 0) {
                    tvBadge.setText(String.valueOf(number));
                    tvBadge.setVisibility(View.VISIBLE);
                } else {
                    tvBadge.setText("");
                    tvBadge.setVisibility(View.GONE);
                }
            }
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_SYNC_DATA_INITIALIZE) {
            if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            PosProductService.get().clear();
            PosProductSkuService.get().clear();
            SharedPreferencesHelper.setSyncProductsCursor("");
            SharedPreferencesHelper.setPosSkuLastUpdate("");
            showSyncDataDialog();

        } else if (event.getAffairId() == AffairEvent.EVENT_ID_SYNC_DATA_START) {
            showSyncDataDialog();
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_SHOW_COMMONLY) {
            showCommonlyFragment();
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_SHOW_EXPRESS) {
            showExpressFragment();
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_SHOW_LAUNDRY) {
            showLaundryFragment();
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_SHOW_FRONT_CATEGORY) {
            showFrontCategoryFragment(event.getArgs());
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_HIDE_LAUNDRY || event.getAffairId() == AffairEvent.EVENT_ID_HIDE_RIGHTSLIDE) {
            hideRightSideFragment();
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_POPBACKSTACK) {
            popBackStack();
        }
    }

    /**
     * 串口
     */
    public void onEventMainThread(SerialPortEvent event) {
        ZLogger.d(String.format("onEventMainThread: SerialPortEvent(%d)", event.getType()));
        //客显
        if (event.getType() == SerialPortEvent.SERIAL_TYPE_DISPLAY) {
            if (!comDisplay.isOpen()) {
                //打开
                OpenComPort(comDisplay);
            }
            sendPortData(comDisplay, event.getCmd(), true);
        } else if (event.getType() == 1) {
            if (!comPrint.isOpen()) {
                OpenComPort(comPrint);
            }
            sendPortData(comPrint, event.getCmd(), false);
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_PRINTER) {
            if (!comPrint.isOpen()) {
                OpenComPort(comPrint);
            }
            sendPortData(comPrint, event.getCmdBytes());
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_PRINTER_INIT) {
            if (comPrint.isOpen()) {
                CloseComPort(comPrint);
            }

            comPrint = new SerialControl(SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate());
            OpenComPort(comPrint);
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_PRINTER_OPEN) {
            if (!comPrint.isOpen()) {
                OpenComPort(comPrint);
            }
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_PRINTER_CLOSE) {
            CloseComPort(comPrint);
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_SCALE_INIT) {
            try {
                if (comScale.isOpen()) {
                    ZLogger.d("关闭电子秤串口");
                    CloseComPort(comScale);
                }

                ZLogger.d("初始化电子秤串口");
//                comScale = new SerialControl(SerialManager.getScalePort(), SerialManager.getScaleBaudrate());
                comScale = new SerialControl(SMScaleDigiAgent.PORT_SCALE_DS781, SMScaleDigiAgent.BAUDRATE_SCALE_DS781);

                ZLogger.d("打开电子秤串口");
                OpenComPort(comScale);
                ZLogger.d("打开电子秤串口完成");
                //清空数据
                DataCacheHelper.getInstance().setNetWeight(0D);
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_SCALE_OPEN) {
            if (!comScale.isOpen()) {
                OpenComPort(comScale);
            }

            //清空数据
            DataCacheHelper.getInstance().setNetWeight(0D);
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_SCALE_CLOSE) {
            CloseComPort(comScale);
            //清空数据
            DataCacheHelper.getInstance().setNetWeight(0D);
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_SCALE) {
            if (!comScale.isOpen()) {
                OpenComPort(comScale);
                DataCacheHelper.getInstance().setNetWeight(0D);
            }
            sendPortData(comScale, event.getCmd(), false);
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_INIT) {
            if (comDisplay.isOpen()) {
                CloseComPort(comDisplay);
            }

            comDisplay = new SerialControl(SerialManager.getLedPort(), SerialManager.getLedBaudrate());
            OpenComPort(comDisplay);
            sendPortData(comDisplay, SerialManager.VFD("12.306"));
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_OPEN) {
            if (comDisplay == null) {
                comDisplay = new SerialControl(SerialManager.getLedPort(), SerialManager.getLedBaudrate());
            }
            if (!comDisplay.isOpen()) {
                OpenComPort(comDisplay);
            }
//            sendPortData(comDisplay, SerialManager.VFD("5201314"));
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD) {
            if (!comDisplay.isOpen()) {
                OpenComPort(comDisplay);
            }
            sendPortData(comDisplay, SerialManager.VFD(event.getCmd()));
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_BYTE) {
            if (!comDisplay.isOpen()) {
                OpenComPort(comDisplay);
            }
            sendPortData(comDisplay, event.getCmdBytes());
        }
    }

    /**
     * 清空未读订单数
     */
    private void resetUnreadOrder() {
        DataCacheHelper.getInstance().clearUnreadOrder();

        int number = DataCacheHelper.getInstance().getUnreadOrder();
        TextView tvBadge = ((TextView) leftSlidingTabStrip.getBadgeView(TAB_INDEX_ORDERFLOW));
        if (number > 0) {
            tvBadge.setText(String.valueOf(number));
            tvBadge.setVisibility(View.VISIBLE);
        } else {
            tvBadge.setText("");
            tvBadge.setVisibility(View.GONE);
        }
    }

    /**
     * 尝试登录
     *
     * @param bSlient 是否静默重试登录
     */
    private void retryLogin(final boolean bSlient) {
        MfhLoginService.get().doLoginAsync(MfhLoginService.get().getLoginName(),
                MfhLoginService.get().getPassword(), new LoginCallback() {
                    @Override
                    public void loginSuccess(UserMixInfo user) {
                        //登录成功
                        ZLogger.df("重登录成功：");

                        //注册到消息桥
                        IMClient.getInstance().registerBridge();

                        dataSync(true);
                    }

                    @Override
                    public void loginFailed(String errMsg) {
                        //登录失败
                        ZLogger.df("重登录失败：" + errMsg);
                        if (!bSlient) {
                            redirectToLogin();
                        }
                    }
                });
    }


    /**
     * 用户个人信息
     */
    private void refreshHumanInfo() {
        ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
        tvUsername.setText(MfhLoginService.get().getHumanName());
    }

    private void showCommonlyFragment() {
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_container_service, commonlyFragment);
//        // ---add to the back stack---
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();

        if (commonlyFragment == null) {
            commonlyFragment = new CommonlyFragment();
        }

        if (commonlyFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .show(commonlyFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_rightside, commonlyFragment)
                    .show(commonlyFragment).commit();
        }

        commonlyFragment.reload();
    }

    /**
     * 显示快递公司
     */
    private void showExpressFragment() {
        if (expressFragment == null) {
            expressFragment = new ExpressFragment();
        }

        if (expressFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .show(expressFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_rightside, expressFragment)
                    .show(expressFragment).commit();
        }
    }

    /**
     * 洗衣
     */
    private void showLaundryFragment() {
        if (laundryFragment == null) {
            laundryFragment = new LaundryFragment();
        }

        if (laundryFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .show(laundryFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_rightside, laundryFragment)
                    .show(laundryFragment).commit();
        }

        laundryFragment.reload();
    }

    /**
     * 显示类目页面
     */
    private void showFrontCategoryFragment(Bundle args) {
        if (frontCategoryFragment == null) {
            frontCategoryFragment = FrontCategoryFragment.newInstance(args);
        } else {
            frontCategoryFragment.init(args);
        }

        if (frontCategoryFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(frontCategoryFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_rightside, frontCategoryFragment)
                    .show(frontCategoryFragment).commit();
        }

        frontCategoryFragment.reload();
    }

    /**
     * 隐藏右侧fragment
     */
    private void hideRightSideFragment() {
        if (expressFragment != null && expressFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(expressFragment).commit();
        }
        if (laundryFragment != null && laundryFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(laundryFragment).commit();
        }
        if (commonlyFragment != null && commonlyFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(commonlyFragment).commit();
        }
        if (frontCategoryFragment != null && frontCategoryFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(frontCategoryFragment).commit();
        }
    }

    private void popBackStack() {
        getSupportFragmentManager().popBackStack();
    }

}

package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.PosProductSkuEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.LastOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.bingshanguxue.vector_uikit.SyncButton;
import com.bingshanguxue.vector_user.bean.Human;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.config.UConfig;
import com.mfh.framework.BizConfig;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.alarm.AlarmManagerHelper;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfoWrapper;
import com.mfh.litecashier.bean.wrapper.HangupOrder;
import com.mfh.litecashier.bean.wrapper.LocalFrontCategoryGoods;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.com.SerialManager;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.CloudSyncManager;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.service.DialogManager;
import com.mfh.litecashier.service.EslSyncManager2;
import com.mfh.litecashier.service.TimeTaskManager;
import com.mfh.litecashier.service.UploadSyncManager;
import com.mfh.litecashier.service.ValidateManager;
import com.mfh.litecashier.ui.adapter.CashierServiceMenuAdapter;
import com.mfh.litecashier.ui.adapter.CashierSwipAdapter;
import com.mfh.litecashier.ui.dialog.ActionDialog;
import com.mfh.litecashier.ui.dialog.AdministratorSigninDialog;
import com.mfh.litecashier.ui.dialog.AlipayDialog;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;
import com.mfh.litecashier.ui.dialog.ExpressDialog;
import com.mfh.litecashier.ui.dialog.HangupOrderDialog;
import com.mfh.litecashier.ui.dialog.InitCardByStepDialog;
import com.mfh.litecashier.ui.dialog.LaundryDialog;
import com.mfh.litecashier.ui.dialog.QueryBalanceDialog;
import com.mfh.litecashier.ui.dialog.ReceiveGoodsDialog;
import com.mfh.litecashier.ui.dialog.RegisterUserDialog;
import com.mfh.litecashier.ui.dialog.ReturnGoodsDialog;
import com.mfh.litecashier.ui.dialog.ValidatePhonenumberDialog;
import com.mfh.litecashier.ui.fragment.components.HomeAdvFragment;
import com.mfh.litecashier.ui.fragment.goods.LocalFrontCategoryFragment;
import com.mfh.litecashier.ui.fragment.inventory.StockScSkuGoodsFragment;
import com.mfh.litecashier.ui.view.ICashierView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.DataCacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;
import com.tencent.bugly.beta.Beta;

import net.tsz.afinal.FinalDb;

import org.century.GreenTagsApi;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class MainActivity extends CashierActivity implements ICashierView {

    @Bind(R.id.slideMenu)
    RecyclerView menuRecyclerView;
    private CashierServiceMenuAdapter menuAdapter;
    @Bind(R.id.button_sync)
    SyncButton btnSync;

    @Bind(R.id.tv_quantity)
    TextView tvQuantity;
    @Bind(R.id.tv_amount)
    TextView tvAmount;
    @Bind(R.id.tv_last_amount)
    TextView tvLastAmount;
    @Bind(R.id.tv_last_quantity)
    TextView tvLastQuantity;
    @Bind(R.id.tv_last_discount)
    TextView tvLastDiscount;
    @Bind(R.id.tv_last_charge)
    TextView tvLastCharge;
    @Bind(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;
    @Bind(R.id.product_list)
    RecyclerView productRecyclerView;
    private ItemTouchHelper itemTouchHelper;
    private CashierSwipAdapter productAdapter;
    @Bind(R.id.fab_settle)
    FloatingActionButton btnSettle;
    @Bind(R.id.float_hangup)
    TextView fabHangup;

    private DoubleInputDialog changePriceDialog = null;
    private DoubleInputDialog changeDiscountDialog = null;
    private DoubleInputDialog changeQuantityDialog = null;
    private DoubleInputDialog quantityCheckDialog = null;
    private HangupOrderDialog hangupOrderDialog = null;
    private AlipayDialog alipayDialog = null;
    private ValidatePhonenumberDialog mValidatePhonenumberDialog = null;
    private QueryBalanceDialog mQueryBalanceDialog = null;
    private RegisterUserDialog mRegisterUserDialog = null;
    private InitCardByStepDialog initCardDialog = null;
    private ReturnGoodsDialog returnGoodsDialog = null;
    private ExpressDialog expressDialog = null;
    private LaundryDialog laundryDialog = null;
    private ReceiveGoodsDialog receiveGoodsDialog = null;
    private ActionDialog registerPlatDialog = null;


    /**
     * POS唯一订单号，由POS机本地生成的12位字符串
     */
    private String curPosTradeNo;

    private CashierPresenter cashierPresenter;

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
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cashierPresenter = new CashierPresenter(this);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (!BizConfig.RELEASE) {
            DialogUtil.showHint("您正在使用的是测试版本，如需切换到正式版本请联系服务商。");
        }

        initMenuRecyclerView();
        initBarCodeInput();
        initCashierRecyclerView();
//        showAdvFragment();
        showLocalFrontCategoryFragment();

        if (menuAdapter != null) {
            menuAdapter.setEntityList(cashierPresenter.getCashierFunctions());
        }

        //刷新挂单
        refreshFloatHangup();

        initCashierOrder();

        reload(true);

        //打开秤的串口
        OpenComPort(comScale);

        cloudSpeak("欢迎使用米西厨房智能收银系统");

        ZLogger.d("小版本标记：2016-07-28-001");

        AlarmManagerHelper.registerBuglyUpgrade(this);
        AlarmManagerHelper.triggleNextDailysettle(0);
//        AlarmManagerHelper.triggleSyncPosOrder(this);
        TimeTaskManager.getInstance().start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (inlvBarcode == null) {
            inlvBarcode = (InputNumberLabelView) findViewById(R.id.inlv_barcode);
        }

        if (inlvBarcode != null) {
            inlvBarcode.clear();
            inlvBarcode.requestFocus();
        }

        int count = SharedPreferencesHelper.getInt(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, 0);
        if (menuAdapter != null) {
            menuAdapter.setBadgeNumber(CashierFunctional.OPTION_ID_ONLINE_ORDER,
                    count);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataCacheHelper.getInstance().reset();

        AppHelper.clearTempData();
    }


    @Override
    public void onBackPressed() {
        String dbName;
        if (BizConfig.RELEASE) {
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                    UConfig.CONFIG_PARAM_DB_NAME, "mfh_cashier_release.db");
        } else {
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                    "dev." + UConfig.CONFIG_PARAM_DB_NAME, "mfh_cashier_dev.db");
        }
        ZLogger.d("关闭数据库:" + dbName);
        FinalDb db = FinalDb.getDb(dbName);
        if (db != null) {
            db.close();
        }
        System.exit(0);
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
     * 初始化前台类目
     */
    private void initMenuRecyclerView() {
//        GridLayoutManager mRLayoutManager = new GridLayoutManager(this, 8);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
//        mRLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        menuRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.5f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        menuAdapter = new CashierServiceMenuAdapter(CashierApp.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new CashierServiceMenuAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                CashierFunctional entity = menuAdapter.getEntity(position);
                if (entity != null && entity.getType() == 0) {
                    responseMenu(entity.getId());
                }
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
//        refreshFrontCategory();
    }

    /**
     * 固有功能
     */
    private void responseMenu(Long id) {
        if (id == null) {
            return;
        }

        if (id.compareTo(CashierFunctional.OPTION_ID_ONLINE_ORDER) == 0) {
            redirectToOnlineOrder();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_GOODS_LIST) == 0) {
            redirectToGoodsList();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_PACKAGE) == 0) {
            packageService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_REGISTER_VIP) == 0) {
            registerVIPStep1();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_BALANCE_QUERY) == 0) {
            queryBalance();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_MEMBER_CARD) == 0) {
            initVipCardStep1();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_EXPRESS) == 0) {
            expressService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_LAUNDRY) == 0) {
            laundryService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_RETURN_GOODS) == 0) {
            returnGoods();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_RECEIVE_GOODS) == 0) {
            receiveGoodsService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_FEEDPAPER) == 0) {
            //走纸
            SerialManager.feedPaper();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_MONEYBOX) == 0) {
            openMoneyBox();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_CLEAR_ORDER) == 0) {
            initCashierOrder();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_HANGUP_ORDER) == 0) {
            hangUpOrder();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_SETTINGS) == 0) {
            redirectToSettings();
        } else {
            DialogUtil.showHint("@开发君 失踪了...");
        }
    }

    /**
     * 取包裹
     */
    private void packageService() {
        //直接根据取货码查询
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE, FragmentActivity.FT_STOCK_DETAIL);
        FragmentActivity.actionStart(this, extras);
    }

    /**
     * 余额查询
     */
    private void queryBalance() {
        //开卡
        if (mQueryBalanceDialog == null) {
            mQueryBalanceDialog = new QueryBalanceDialog(this);
            mQueryBalanceDialog.setCancelable(false);
            mQueryBalanceDialog.setCanceledOnTouchOutside(false);
        }
        mQueryBalanceDialog.initialize(new QueryBalanceDialog.OnValidateListener() {
            @Override
            public void onSuccess(String phonenumber) {
                registerVIPStep2(phonenumber);
            }

            @Override
            public void onError() {

            }
        });
        if (!mQueryBalanceDialog.isShowing()) {
            mQueryBalanceDialog.show();
        }
    }

    /**
     * 跳转到线上订单
     */
    public void redirectToOnlineOrder() {
        if (menuAdapter != null) {
            menuAdapter.setBadgeNumber(CashierFunctional.OPTION_ID_ONLINE_ORDER,
                    0);
        }

        ZLogger.df(">>>打开线上订单页面");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_ONLINE_ORDER);
        SimpleActivity.actionStart(this, extras);
    }

    /**
     * 跳转到商品列表
     */
    public void redirectToGoodsList() {
        ZLogger.df(">>>打开商品列表");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_GOODS_LIST);
        SimpleActivity.actionStart(this, extras);
    }

    /**
     * 会员注册1-验证手机号
     */
    private void registerVIPStep1() {
        if (mValidatePhonenumberDialog == null) {
            mValidatePhonenumberDialog = new ValidatePhonenumberDialog(this);
            mValidatePhonenumberDialog.setCancelable(false);
            mValidatePhonenumberDialog.setCanceledOnTouchOutside(false);
        }
        mValidatePhonenumberDialog.initialize(new ValidatePhonenumberDialog.OnValidateListener() {
            @Override
            public void onSuccess(String phonenumber) {
                registerVIPStep2(phonenumber);
            }

            @Override
            public void onError() {

            }
        });
        if (!mValidatePhonenumberDialog.isShowing()) {
            mValidatePhonenumberDialog.show();
        }
    }

    /**
     * 会员注册2-输入登录密码和支付密码
     */
    private void registerVIPStep2(String phonenumber) {
        //开卡
        if (mRegisterUserDialog == null) {
            mRegisterUserDialog = new RegisterUserDialog(this);
            mRegisterUserDialog.setCancelable(false);
            mRegisterUserDialog.setCanceledOnTouchOutside(false);
        }
        mRegisterUserDialog.initialize(phonenumber, new RegisterUserDialog.OnRegisterListener() {
            @Override
            public void onSuccess(Human human) {
                registerVIPStep3(human);
            }

            @Override
            public void onCandel() {

            }

            @Override
            public void onFailed() {

            }
        });
        if (!mRegisterUserDialog.isShowing()) {
            mRegisterUserDialog.show();
        }
    }

    /**
     * 会员注册3-办卡
     */
    private void registerVIPStep3(Human human) {
        if (initCardDialog == null) {
            initCardDialog = new InitCardByStepDialog(this);
            initCardDialog.setCancelable(false);
            initCardDialog.setCanceledOnTouchOutside(false);
        }
        initCardDialog.initialize(human);
        if (!initCardDialog.isShowing()) {
            initCardDialog.show();
        }
    }

    /**
     * 会员卡开卡
     */
    private void initVipCardStep1() {
        if (mValidatePhonenumberDialog == null) {
            mValidatePhonenumberDialog = new ValidatePhonenumberDialog(this);
            mValidatePhonenumberDialog.setCancelable(false);
            mValidatePhonenumberDialog.setCanceledOnTouchOutside(false);
        }
        mValidatePhonenumberDialog.initialize(new ValidatePhonenumberDialog.OnValidateListener() {
            @Override
            public void onSuccess(String phonenumber) {
                initVipCardStep2(phonenumber);
            }

            @Override
            public void onError() {

            }
        });
        if (!mValidatePhonenumberDialog.isShowing()) {
            mValidatePhonenumberDialog.show();
        }
    }

    private void initVipCardStep2(String phonenumber) {
        if (initCardDialog == null) {
            initCardDialog = new InitCardByStepDialog(this);
            initCardDialog.setCancelable(false);
            initCardDialog.setCanceledOnTouchOutside(false);
        }
        initCardDialog.initialize(phonenumber, new InitCardByStepDialog.OnInitCardListener() {
            @Override
            public void onSuccess() {
                showProgressDialog(ProgressDialog.STATUS_DONE, "开卡成功", true);
            }

            @Override
            public void onFailed() {

            }
        });
        if (!initCardDialog.isShowing()) {
            initCardDialog.show();
        }
    }


    /**
     * 寄快递
     */
    private void expressService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(getString(R.string.toast_network_error));
//            return;
//        }

        if (expressDialog == null) {
            expressDialog = new ExpressDialog(this);
            expressDialog.setCancelable(false);
            expressDialog.setCanceledOnTouchOutside(false);
        }
        expressDialog.init(new ExpressDialog.DialogListener() {
            @Override
            public void query(String text) {

            }

            @Override
            public void onNextStep() {
                //TODO,显示快递页面
                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_EXPRESS));
            }
        });
        if (!expressDialog.isShowing()) {
            expressDialog.show();
        }
    }

    /**
     * 洗衣
     */
    private void laundryService() {
        if (laundryDialog == null) {
            laundryDialog = new LaundryDialog(this);
            laundryDialog.setCancelable(false);
            laundryDialog.setCanceledOnTouchOutside(false);
        }
        laundryDialog.init(new LaundryDialog.DialogListener() {

            @Override
            public void onNextStep(String text) {
                // TODO: 5/23/16 跳转到新的页面洗衣
//                //挂起POS收银
//                hangUpOrder();
//
//                //保存衣袋编号，同时标记开始进行洗衣服务，挂起当前订单
//                packageCode = text;
//                curBizType = BizType.LAUNDRY;

                //显示洗衣商品
                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_LAUNDRY));
            }
        });
        if (!laundryDialog.isShowing()) {
            laundryDialog.show();
        }
    }

    /**
     * 退货
     */
    private void returnGoods() {
        if (returnGoodsDialog == null) {
            returnGoodsDialog = new ReturnGoodsDialog(this);
            returnGoodsDialog.setCancelable(false);
            returnGoodsDialog.setCanceledOnTouchOutside(false);
        }
        if (!returnGoodsDialog.isShowing()) {
            returnGoodsDialog.show();
        }
    }

    /**
     * 领取商品
     */
    private void receiveGoodsService() {
        if (receiveGoodsDialog == null) {
            receiveGoodsDialog = new ReceiveGoodsDialog(this);
            receiveGoodsDialog.setCancelable(false);
            receiveGoodsDialog.setCanceledOnTouchOutside(false);
        }
        receiveGoodsDialog.init(new ReceiveGoodsDialog.DialogListener() {
            @Override
            public void onOrderConfirmed(String orderBarcode) {
                // TODO: 5/23/16
//                resumeOrder(orderBarcode);
            }
        });
        if (!receiveGoodsDialog.isShowing()) {
            receiveGoodsDialog.show();
        }
    }


    /**
     * 同步数据
     */
    @OnClick(R.id.button_sync)
    public void syncData() {
        btnSync.startSync();

        //设置需要更新前台类目
        SharedPreferencesHelper.setSyncFrontCategorySubEnabled(true);
        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED, true);

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SKU_UPDATE_UNREADNUMBER, 0);

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSync.stopSync();
            return;
        }

        //同步数据
        DataSyncManager.get().sync();
    }

    /**
     * 数据同步
     *
     * @param isSlient true:后台同步数据；false:显示进度对话框。
     */
    private void dataSync(boolean isSlient) {
        //账号发生改变
        if (MfhLoginService.get().isCompanyOrOfficeChanged()) {
            AppHelper.clearAppData();
        }

//        AppHelper.clearCache();
        if (!isSlient) {
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在同步数据...",
                    true, false);
        }
        btnSync.startSync();
        DataSyncManager.get().sync();

        /**
         * @param isManual  用户手动点击检查，非用户点击操作请传false
         * @param isSilence 是否显示弹窗等交互，[true:没有弹窗和toast] [false:有弹窗或toast]
         */
        Beta.checkUpgrade(false, false);
    }

    public void redirectToSettings() {
        if (!MfhLoginService.get().haveLogined()) {
            redirectToLogin();
        } else {
            enterAdministratorMode();
        }
    }


    private AdministratorSigninDialog mAdministratorSigninDialog = null;

    /**
     * 进入管理员模式
     */
    private void enterAdministratorMode() {
        if (mAdministratorSigninDialog == null) {
            mAdministratorSigninDialog = new AdministratorSigninDialog(this);
            mAdministratorSigninDialog.setCancelable(true);
            mAdministratorSigninDialog.setCanceledOnTouchOutside(true);
        }
        mAdministratorSigninDialog.init("管理员密码", new AdministratorSigninDialog.OnResponseCallback() {
            @Override
            public void onSignInSuccess() {
                UIHelper.startActivity(MainActivity.this, AdministratorActivity.class);
// TODO: 5/19/16
//                UIHelper.startActivity(this, SettingsActivity.class);
            }
        });
        mAdministratorSigninDialog.show();
    }

    /**
     * 重新加载数据
     */
    private void reload(boolean isSlient) {
        //设置需要更新前台类目
        SharedPreferencesHelper.setSyncFrontCategorySubEnabled(true);
        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED, true);

        //清除缓存数据
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME).clear();

//        hideRightSideFragment();

        dataSync(isSlient);

        ValidateManager.get().batchValidate();
    }

    public void onEventMainThread(AffairEvent event) {
        int eventId = event.getAffairId();
        Bundle bundle = event.getArgs();
        ZLogger.d(String.format("AffairEvent(%d)", eventId));
        if (eventId == AffairEvent.EVENT_ID_SYNC_DATA_INITIALIZE) {
            if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            PosProductService.get().clear();
            PosProductSkuService.get().clear();
            SharedPreferencesHelper.setSyncProductsCursor("");
            SharedPreferencesHelper.setPosSkuLastUpdate("");
            SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                    GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
            SharedPreferencesManager.set(SMScaleSyncManager2.PREF_SMSCALE,
                    SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR, "");

//            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在同步数据...", true, false);
            btnSync.startSync();
            DataSyncManager.get().sync();
        } else if (eventId == AffairEvent.EVENT_ID_APPEND_UNREAD_SKU) {
            int count = SharedPreferencesHelper.getInt(SharedPreferencesHelper.PK_SKU_UPDATE_UNREADNUMBER, 0);
            if (count > 1) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SKU_UPDATE_UNREADNUMBER, 0);
                btnSync.startSync();
                DataSyncManager.get().sync();
            } else {
                btnSync.setBadgeEnabled(true);
            }
        } else if (eventId == AffairEvent.EVENT_ID_APPEND_UNREAD_SCHEDULE_ORDER) {
            int count = SharedPreferencesHelper.getInt(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, 0);
            menuAdapter.setBadgeNumber(CashierFunctional.OPTION_ID_ONLINE_ORDER,
                    count);
            if (count > 0) {
                cloudSpeak("您有新订单,请注意查收");
            }
        } else if (eventId == AffairEvent.EVENT_ID_LOCK_POS_CLIENT) {
            Double amount = bundle.getDouble("amount");
            QuickPayInfo quickPayInfo = new QuickPayInfo();
            quickPayInfo.setBizType(BizType.DAILYSETTLE);
            quickPayInfo.setSubBizType(BizType.CASH_QUOTA);
            quickPayInfo.setPayType(WayType.ALI_F2F);
            quickPayInfo.setSubject("提交营业现金");
            quickPayInfo.setBody("营业现金已超出授权限额，请尽快提交现金，解锁POS设备！");
            quickPayInfo.setAmount(amount);
            quickPayInfo.setMinAmount(amount);
            incomeDistributionTopup(quickPayInfo);
        } else if (eventId == AffairEvent.EVENT_ID_PRE_LOCK_POS_CLIENT) {
            Double amount = bundle.getDouble("amount");

            DialogUtil.showHint(String.format("现金授权额度(%.2f)即将用完，" +
                    "为了不影响您使用POS设备，请及时充值", amount));

        } else if (eventId == AffairEvent.EVENT_ID_UNLOCK_POS_CLIENT) {
            if (alipayDialog != null) {
                alipayDialog.dismiss();
            }
        } else if (eventId == AffairEvent.EVENT_ID_RESET_CASHIER) {
            initCashierOrder();
        } else if (eventId == AffairEvent.EVENT_ID_CASHIER_FRONTCATA_GOODS) {
            LocalFrontCategoryGoods goods = (LocalFrontCategoryGoods) bundle.getSerializable("goods");
            cashierGoods(goods);
        }

    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
        ZLogger.d(String.format("DataSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            hideProgressDialog();
            btnSync.stopSync();
            //同步数据结束后开始同步订单
            UploadSyncManager.getInstance().sync();

            CloudSyncManager.get().importFromChainSku();
            EslSyncManager2.getInstance().sync();
            SMScaleSyncManager2.getInstance().sync();
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
            case ValidateManager.ValidateManagerEvent.EVENT_ID_INTERRUPT_NEED_LOGIN: {
                redirectToLogin();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_RETRY_SIGNIN_SUCCEED: {
                dataSync(true);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER: {
                DialogManager.getInstance().registerPos(MainActivity.this);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_INCOME_DESTRIBUTION_TOPUP: {
                Double amount = args.getDouble("amount");
                QuickPayInfo quickPayInfo = new QuickPayInfo();
                quickPayInfo.setBizType(BizType.DAILYSETTLE);
                quickPayInfo.setSubBizType(BizType.INCOME_DISTRIBUTION);
                quickPayInfo.setPayType(WayType.ALI_F2F);
                quickPayInfo.setSubject("账户充值");
                quickPayInfo.setBody("清分余额不足,请尽快充值,解锁POS设备！");
                quickPayInfo.setAmount(amount);
                quickPayInfo.setMinAmount(amount);

                incomeDistributionTopup(quickPayInfo);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_CASH_QUOTA_TOPUP: {
                Double amount = args.getDouble("amount");
                QuickPayInfo quickPayInfo = new QuickPayInfo();
                quickPayInfo.setBizType(BizType.DAILYSETTLE);
                quickPayInfo.setSubBizType(BizType.CASH_QUOTA);
                quickPayInfo.setPayType(WayType.ALI_F2F);
                quickPayInfo.setSubject("提交营业现金");
                quickPayInfo.setBody("营业现金已超出授权限额，请尽快提交现金，解锁POS设备！");
                quickPayInfo.setAmount(amount);
                quickPayInfo.setMinAmount(amount);
                incomeDistributionTopup(quickPayInfo);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED: {
                //验证结束开始加载数据
//                dataSync(false);
                Beta.checkUpgrade(false, false);
            }
            break;
        }
    }

    /**
     * 结算(需要登录)
     */
    @OnClick(R.id.fab_settle)
    public void settle() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        btnSettle.setEnabled(false);

        //判断是否登录
        if (!MfhLoginService.get().haveLogined()) {
            DialogUtil.showHint("请先登录");
            btnSettle.setEnabled(true);
            hideProgressDialog();
            return;
        }

        //判断是否登录
        if (productAdapter.getItemCount() <= 0) {
            DialogUtil.showHint("商品明细不能为空");
            btnSettle.setEnabled(true);
            hideProgressDialog();
            return;
        }

        if (productAdapter.haveEmptyPrice()) {
            showConfirmDialog("有商品未设置价格或价格为零，是否确认结算？",
                    "结算", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            doPosSettleStuff();
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            btnSettle.setEnabled(true);
                            hideProgressDialog();
                        }
                    });
        } else {
            doPosSettleStuff();
        }
    }

    /**
     * <ol>
     * 结算
     * <li>判断当前收银台购物车的商品是否为空，若不为空，则继续第2步，否则结束；</li>
     * <li>生成订单,［并拆单］；</li>
     * <li>更新订单明细（需要删除历史记录）；</li>
     * <li>结束</li>
     * </ol>
     */
    private void doPosSettleStuff() {
        Observable.create(new Observable.OnSubscribe<CashierOrderInfo>() {
            @Override
            public void call(Subscriber<? super CashierOrderInfo> subscriber) {
                CashierOrderInfo cashierOrderInfo = CashierAgent.settle(curPosTradeNo,
                        PosOrderEntity.ORDER_STATUS_STAY_PAY, productAdapter.getEntityList());
                if (cashierOrderInfo != null) {
                    //显示客显
                    CashierHelper.broadcastCashierOrderInfo(CashierOrderInfoWrapper.CMD_PAY_ORDER, cashierOrderInfo);
                }
                ZLogger.df(String.format("[点击结算]--生成结算信息：%s",
                        JSON.toJSONString(cashierOrderInfo)));

                subscriber.onNext(cashierOrderInfo);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CashierOrderInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CashierOrderInfo cashierOrderInfo) {
                        if (cashierOrderInfo != null) {
                            hideProgressDialog();
                            Intent intent = new Intent(MainActivity.this, CashierPayActivity.class);
                            Bundle extras = new Bundle();
                            extras.putSerializable(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
                            intent.putExtras(extras);
                            startActivityForResult(intent, Constants.ARC_MFPAY);
                        } else {
                            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "订单创建失败", true);
                            btnSettle.setEnabled(true);
                        }
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_MFPAY: {
                if (resultCode == Activity.RESULT_OK) {
                    ZLogger.df("订单支付成功");
                    if (data != null) {
                        CashierOrderInfo cashierOrderInfo = (CashierOrderInfo) data
                                .getSerializableExtra(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO);

                        new SettleAsyncTask(curPosTradeNo).execute(cashierOrderInfo);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    ZLogger.df("取消收银订单支付");
                    boolean isClearOrder = false;
                    if (data != null) {
                        isClearOrder = data.getBooleanExtra(CashierPayActivity.EXTRA_KEY_IS_CLEAR_ORDER, false);
                    }
                    if (isClearOrder) {

                        ZLogger.df("清空收银购物车，重新开始新订单");
//加载订单
                        if (StringUtils.isEmpty(curPosTradeNo)) {
                            CashierShopcartService.getInstance()
                                    .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
                        }
                        obtaincurPosTradeNo(null);
                        productAdapter.setEntityList(null);
                    }
                    btnSettle.setEnabled(true);

                } else {
                    ZLogger.df("取消收银订单支付2");
                    btnSettle.setEnabled(true);
                }


            }
            break;
            case Constants.ARC_CREATE_PURCHASE_GOODS: {
                if (resultCode == Activity.RESULT_OK) {
                    //TODO,新增商品成功，同步商品
                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            break;
            case Constants.ARC_NATIVE_LOGIN: {
                if (resultCode == Activity.RESULT_OK) {
                    DialogUtil.showHint("登录成功");
                    //初始化收银
                    initCashierOrder();
                    reload(true);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 异步处理订单支付结果
     * <p/>
     * 保存订单信息，打印小票，显示上一单信息，同步订单，统计订单金额，语音播报
     */
    private class SettleAsyncTask extends AsyncTask<CashierOrderInfo, Integer, LastOrderInfo> {
        private String posTradeNo;

        public SettleAsyncTask(String posTradeNo) {
            this.posTradeNo = posTradeNo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //清空当前收银列表，开始新的订单
            obtaincurPosTradeNo(null);
            productAdapter.setEntityList(null);
        }

        @Override
        protected LastOrderInfo doInBackground(CashierOrderInfo... params) {
            //重新生成订单，清空购物车
            if (!StringUtils.isEmpty(posTradeNo)) {
                CashierShopcartService.getInstance()
                        .deleteBy(String.format("posTradeNo = '%s'", posTradeNo));
            }

            CashierOrderInfo cashierOrderInfo = params[0];
            if (cashierOrderInfo == null) {
                return null;
            }
            ZLogger.df(String.format("%s支付，流水编号：%s\n%s",
                    WayType.name(cashierOrderInfo.getBizType()), cashierOrderInfo.getPosTradeNo(),
                    JSONObject.toJSONString(cashierOrderInfo)));


            // TODO: 7/5/16 下个版本放到支付页面去,更新客显，支付完成
            CashierHelper.broadcastCashierOrderInfo(CashierOrderInfoWrapper.CMD_FINISH_ORDER, cashierOrderInfo);

            List<PosOrderEntity> orderEntities = CashierFactory
                    .fetchActiveOrderEntities(BizType.POS, cashierOrderInfo.getPosTradeNo());
            //同步订单信息
//            UploadSyncManager.getInstance().stepUploadPosOrder(orderEntities);
            //打印订单
            PrintManager.printPosOrder(orderEntities, true);
            //保存上一单信息
            LastOrderInfo lastOrderInfo = CashierFactory.genLastOrderInfo(orderEntities);
            if (lastOrderInfo != null) {

                int payType = lastOrderInfo.getPayType();
                Double finalAmount = lastOrderInfo.getFinalAmount();
                Double changeAmount = lastOrderInfo.getChangeAmount();
                Double bCount = lastOrderInfo.getbCount();

                //显示找零
//        SerialManager.show(4, Math.abs(cashierOrderInfo.getHandleAmount()));
                SerialManager.vfdShow(String.format("Change:%.2f\r\nThank You!", changeAmount));

                if (changeAmount >= 0.01) {
                    cloudSpeak(String.format("%s 支付 %.2f 元, 找零 %.2f 元，商品数量 %.0f, 谢谢光临！",
                            WayType.name(payType), finalAmount, changeAmount, bCount));
                } else {
                    cloudSpeak(String.format("%s 支付 %.2f 元, 商品数量 %.0f, 谢谢光临！",
                            WayType.name(payType), finalAmount, bCount));
                }
            }

            //统计订单
//            ValidateManager.get().stepValidate(ValidateManager.STEP_VALIDATE_CASHQUOTA);
            return lastOrderInfo;
        }

        @Override
        protected void onPostExecute(LastOrderInfo lastOrderInfo) {
            super.onPostExecute(lastOrderInfo);
            refreshLastOrder(lastOrderInfo);

            btnSettle.setEnabled(true);
        }

    }

    @OnClick(R.id.float_hangup)
    public void showOrder() {
        if (hangupOrderDialog == null) {
            hangupOrderDialog = new HangupOrderDialog(this);
            hangupOrderDialog.setCancelable(true);
            hangupOrderDialog.setCanceledOnTouchOutside(true);
        }
        hangupOrderDialog.init(new HangupOrderDialog.OnResponseCallback() {

            @Override
            public void onResumeOrder(String orderBarCode) {
                resumeOrder(orderBarCode);
            }
        });
        if (!hangupOrderDialog.isShowing()) {
            hangupOrderDialog.show();
        }
    }

    /**
     * 初始化商品列表
     */
    private void initCashierRecyclerView() {
        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
        //设置Item增加、移除动画
//        productRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(
                this, LineItemDecoration.VERTICAL_LIST));

        productAdapter = new CashierSwipAdapter(this, null);
        productAdapter.setOnAdapterListener(new CashierSwipAdapter.OnAdapterListener() {

            @Override
            public void onPriceClicked(int position) {
                changeGoodsPrice(position);
            }

            @Override
            public void onDiscountClicked(int position) {
                changeGoodsDiscount(position);
            }

            @Override
            public void onQuantityClicked(int position) {
                changeGoodsQuantity(position);
            }

            @Override
            public void onDataSetChanged(boolean needScroll) {
                tvQuantity.setText(String.format("%.2f", productAdapter.getBcount()));
                tvAmount.setText(String.format("%.2f", productAdapter.getFinalAmount()));//成交价

                if (productAdapter.getItemCount() > 0) {
                    btnSettle.setEnabled(true);
                } else {
                    //清除屏幕上的字符
                    SerialManager.clear();
                    //TODO,清除客显屏幕

                    btnSettle.setEnabled(false);
                }
                if (needScroll) {
                    //后来者居上
                    productRecyclerView.scrollToPosition(0);
                }
            }
        });


        ItemTouchHelper.Callback callback = new MyItemTouchHelper(productAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        //关联到RecyclerView
        itemTouchHelper.attachToRecyclerView(productRecyclerView);

        // specify an adapter
        productRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 开钱箱
     */
    public void openMoneyBox() {
        //打开钱箱
        SerialManager.openMoneyBox();
    }


    /**
     * 初始化条码输入
     */
    private void initBarCodeInput() {
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.requestFocus();
        inlvBarcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String barcode = inlvBarcode.getInputString();
                        searchGoodsByBarcode(barcode);
//                        searchGoodsByBarcodeRx(barcode);
                    }

                    return true;
                }
                //Press “*”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //判断是否已经有数字，如果已经有则直接加数字，否则弹窗
                        String inputText = inlvBarcode.getInputString();
                        if (StringUtils.isEmpty(inputText)) {
                            changeGoodsQuantity(0);
//                            if (productAdapter != null) {
//                                productAdapter.changeQuantity();
//                            }
                        } else {
                            inlvBarcode.clear();
                            try {
                                if (productAdapter != null) {
                                    productAdapter.changeQuantity(Double.valueOf(inputText));
                                }
                            } catch (Exception e) {
                                ZLogger.e(e.toString());
                            }
                        }
                    }
                    return true;
                }
                //Press “＋”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_ADD) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (btnSettle.isEnabled()) {
                            settle();
                        }
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }


    /**
     * 修改商品价格
     */
    private void changeGoodsPrice(final int position) {
        final CashierShopcartEntity entity = productAdapter.getEntity(position);
        if (entity == null) {
            return;
        }

        if (changePriceDialog == null) {
            changePriceDialog = new DoubleInputDialog(this);
            changePriceDialog.setCancelable(true);
            changePriceDialog.setCanceledOnTouchOutside(true);
        }
        changePriceDialog.initialzie("成交价", 2, entity.getFinalPrice(), "元",
                new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        entity.setFinalPrice(quantity);
                        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
                        CashierShopcartService.getInstance().saveOrUpdate(entity);

                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged(position, false);
                        }
                    }
                });
        changePriceDialog.show();
    }


    /**
     * 修改商品价格折扣
     */
    private void changeGoodsDiscount(final int position) {
        final CashierShopcartEntity entity = productAdapter.getEntity(position);
        if (entity == null) {
            return;
        }

        if (changeDiscountDialog == null) {
            changeDiscountDialog = new DoubleInputDialog(this);
            changeDiscountDialog.setCancelable(true);
            changeDiscountDialog.setCanceledOnTouchOutside(true);
        }
        changeDiscountDialog.initialzie("折扣", 0,
                CashierAgent.calculatePriceDiscount(entity.getCostPrice(), entity.getFinalPrice()),
                "%", new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double value) {
                        entity.setFinalPrice(entity.getCostPrice() * value / 100);
                        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
                        CashierShopcartService.getInstance().saveOrUpdate(entity);

                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged(position, false);
                        }
                    }
                });
        changeDiscountDialog.show();
    }

    /**
     * 修改商品数量
     */
    private void changeGoodsQuantity(final int position) {
        final CashierShopcartEntity entity = productAdapter.getEntity(position);
        if (entity == null) {
            return;
        }

        if (changeQuantityDialog == null) {
            changeQuantityDialog = new DoubleInputDialog(this);
            changeQuantityDialog.setCancelable(true);
            changeQuantityDialog.setCanceledOnTouchOutside(true);
        }
        changeQuantityDialog.initialzie("数量", 3, entity.getBcount(), entity.getUnit(),
                new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        entity.setBcount(quantity);
                        entity.setAmount(entity.getBcount() * entity.getCostPrice());
                        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());

                        CashierShopcartService.getInstance().saveOrUpdate(entity);
                        // TODO: 7/7/16
                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged(position, false);
                        }
                    }
                });
        changeQuantityDialog.show();
    }

    /**
     * 刷新上一单信息,更新客显，语音播报
     */
    private void refreshLastOrder(LastOrderInfo lastOrderInfo) {
        if (lastOrderInfo != null) {
            tvLastAmount.setText(String.format("合计: ¥%.2f", lastOrderInfo.getFinalAmount()));
            tvLastQuantity.setText(String.format("数量: %.2f", lastOrderInfo.getbCount()));
            tvLastDiscount.setText(String.format("优惠: ¥%.2f", lastOrderInfo.getDiscountAmount()));
            tvLastCharge.setText(String.format("找零: ¥%.2f", lastOrderInfo.getChangeAmount()));
        } else {
            tvLastAmount.setText(String.format("合计: ¥%.2f", 0D));
            tvLastQuantity.setText(String.format("数量: %.2f", 0D));
            tvLastDiscount.setText(String.format("优惠: ¥%.2f", 0D));
            tvLastCharge.setText(String.format("找零: ¥%.2f", 0D));
        }
    }

    /**
     * 刷新挂起浮动按钮
     */
    private void refreshFloatHangup() {
        List<HangupOrder> hangupOrderList = CashierHelper.mergeHangupOrders(BizType.POS);
        if (hangupOrderList != null && hangupOrderList.size() > 0) {
            fabHangup.setText(String.valueOf(hangupOrderList.size()));
            fabHangup.setVisibility(View.VISIBLE);
        } else {
            fabHangup.setText("0");
            fabHangup.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化收银
     */
    private void initCashierOrder() {
        try {
            if (inlvBarcode != null) {
                inlvBarcode.clear();
                inlvBarcode.requestFocusEnd();
            }

            //刷新上一单数据
            refreshLastOrder(null);

            //加载订单
            if (StringUtils.isEmpty(curPosTradeNo)) {
                CashierShopcartService.getInstance()
                        .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
            }
            obtaincurPosTradeNo(null);
            productAdapter.setEntityList(null);

            CashierHelper.broadcastCashierOrderInfo(CashierOrderInfoWrapper.CMD_CLEAR_ORDER, null);
            //刷新挂单
            refreshFloatHangup();

            UploadSyncManager.getInstance().sync();
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 挂单
     * <ol>
     * 挂单
     * <li>判断当前收银台购物车的商品是否为空，若不为空，则继续第2步，否则结束；</li>
     * <li>生成订单,［并拆单］；</li>
     * <li>更新订单明细（需要删除历史记录）；</li>
     * <li>结束</li>
     * </ol>
     */
    public void hangUpOrder() {
        inlvBarcode.clear();
        //清除屏幕上的字符
        SerialManager.clear();
        //Step 1:
        if (productAdapter.getItemCount() > 0) {
            ZLogger.d(String.format("挂单：%s", curPosTradeNo));
            CashierAgent.settle(curPosTradeNo, PosOrderEntity.ORDER_STATUS_HANGUP,
                    productAdapter.getEntityList());

            //刷新挂单
            refreshFloatHangup();
        }

//        //重新生成订单
//        if (!StringUtils.isEmpty(curPosTradeNo)) {
//            CashierShopcartService.getInstance()
//                    .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
//        }
        obtaincurPosTradeNo(null);
        productAdapter.setEntityList(null);
    }

    /**
     * 调单
     */
    private void resumeOrder(String posTradeNo) {
        inlvBarcode.clear();
        //清除屏幕上的字符
        SerialManager.clear();

        if (productAdapter.getItemCount() > 0) {
            //挂起当前订单
            ZLogger.df(String.format("挂单：%s", curPosTradeNo));
            CashierAgent.settle(curPosTradeNo, PosOrderEntity.ORDER_STATUS_HANGUP,
                    productAdapter.getEntityList());
        }

        ZLogger.df(String.format("调单：%s", posTradeNo));
        //加载新订单
        obtaincurPosTradeNo(posTradeNo);
        //加载明细
        CashierShopcartService.getInstance().readOrderItems(posTradeNo,
                CashierAgent.resume(posTradeNo));
        List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                .queryAllBy(String.format("posTradeNo = '%s'", posTradeNo));
        productAdapter.setEntityList(shopcartEntities);

        //刷新挂单
        refreshFloatHangup();
    }


    /**
     * 根据条码查询商品
     */
    private void searchGoodsByBarcode(final String barCode) {
        if (StringUtils.isEmpty(barCode)) {
//            条码无效
            return;
        }

        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        cashierPresenter.findGoods(barCode);
    }

    private void searchGoodsByBarcodeRx(final String barCode) {

        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        cashierPresenter.findGoods(barCode);

        Observable.create(new Observable.OnSubscribe<PosProductEntity>() {
            @Override
            public void call(Subscriber<? super PosProductEntity> subscriber) {

                if (StringUtils.isEmpty(barCode)) {
                    subscriber.onError(new Throwable("商品条码无效"));
                    return;
                }

                PosProductEntity goods;
                //生鲜商品条码是以'2'开头并且是13位，F CCCCCC XXXXX CD
                if (barCode.startsWith("2") && barCode.length() == 13) {
                    String plu = barCode.substring(1, 7);
                    String weightStr = String.format("%s.%s", barCode.substring(7, 9), barCode.substring(9, 12));
                    Double weight = Double.valueOf(weightStr);
                    ZLogger.df(String.format("搜索生鲜商品 条码：%s, PLU码：%s, 重量：%f",
                            barCode, plu, weight));

                    int packFlag = 0;//是否是箱规：0不是；1是
                    //Step 1:查询商品
                    goods = PosProductService.get().findGoods(plu);
                    if (goods == null) {
                        // Step 2: 查询主条码
                        List<PosProductSkuEntity> entityList = PosProductSkuService.get()
                                .queryAllByDesc(String.format("otherBarcode = '%s'", plu));
                        if (entityList != null && entityList.size() > 0) {
                            PosProductSkuEntity posProductSkuEntity = entityList.get(0);
                            String mainBarcode = posProductSkuEntity.getMainBarcode();
                            packFlag = posProductSkuEntity.getPackFlag();
                            ZLogger.df(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));

                            //Step 3:根据主条码再次查询商品
                            goods = PosProductService.get().findGoods(mainBarcode);
                        }
                    }

                } else {
                    int packFlag = 0;//是否是箱规：0不是；1是
                    //Step 1:查询商品
                    goods = PosProductService.get().findGoods(barCode);
                    if (goods == null) {
                        // Step 2: 查询主条码
                        List<PosProductSkuEntity> entityList = PosProductSkuService.get()
                                .queryAllByDesc(String.format("otherBarcode = '%s'", barCode));
                        if (entityList != null && entityList.size() > 0) {
                            PosProductSkuEntity posProductSkuEntity = entityList.get(0);
                            String mainBarcode = posProductSkuEntity.getMainBarcode();
                            packFlag = posProductSkuEntity.getPackFlag();
                            ZLogger.df(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));

                            //Step 3:根据主条码再次查询商品
                            goods = PosProductService.get().findGoods(mainBarcode);
                        }
                    }
                }

                subscriber.onNext(goods);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PosProductEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(PosProductEntity posProductEntity) {

                    }
                });
    }


    /**
     * 获取当前订单交易编号
     */
    public void obtaincurPosTradeNo(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            curPosTradeNo = MUtils.getOrderBarCode();
        } else {
            curPosTradeNo = barcode;
        }

    }

    @Override
    public void onFindGoods(final PosProductEntity goods, int packFlag) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            final Double weightVal = DataCacheHelper.getInstance().getNetWeight();
            if (weightVal > 0) {
                addGoods2Cashier(curPosTradeNo, goods, weightVal);
            } else {
                if (quantityCheckDialog == null) {
                    quantityCheckDialog = new DoubleInputDialog(this);
                    quantityCheckDialog.setCancelable(true);
                    quantityCheckDialog.setCanceledOnTouchOutside(true);
                }
                quantityCheckDialog.initialzie("重量", 3, weightVal, goods.getUnit(),
                        new DoubleInputDialog.OnResponseCallback() {
                            @Override
                            public void onQuantityChanged(Double quantity) {
                                addGoods2Cashier(curPosTradeNo, goods, quantity);
                            }
                        });
                quantityCheckDialog.show();
            }

        } else {
            if (packFlag == 1) {
                addGoods2Cashier(curPosTradeNo, goods, goods.getPackageNum());
            } else {
                addGoods2Cashier(curPosTradeNo, goods, 1D);
            }
        }
    }

    @Override
    public void onFindFreshGoods(PosProductEntity goods, Double weight) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            //计重商品直接读取条码中的重量信息
            addGoods2Cashier(curPosTradeNo, goods, weight);
        } else {
            // TODO: 8/24/16 如果是扫描秤打印的条码，计件商品又可能不是1
            //计件商品默认商品数量加1
            addGoods2Cashier(curPosTradeNo, goods, 1D);
        }
    }

    @Override
    public void onFindGoodsEmpty(String barcode) {
        Intent intent = new Intent(this, SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleDialogActivity.FRAGMENT_TYPE_CREATE_PURCHASE_GOODS);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE,
                SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
        extras.putString(StockScSkuGoodsFragment.EXTRY_KEY_BARCODE, barcode);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_CREATE_PURCHASE_GOODS);
    }

    private void cashierGoods(final LocalFrontCategoryGoods goods) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            final Double weightVal = DataCacheHelper.getInstance().getNetWeight();
            if (weightVal > 0) {
                addGoods2Cashier(curPosTradeNo, goods, weightVal);
            } else {
                if (quantityCheckDialog == null) {
                    quantityCheckDialog = new DoubleInputDialog(this);
                    quantityCheckDialog.setCancelable(true);
                    quantityCheckDialog.setCanceledOnTouchOutside(true);
                }
                quantityCheckDialog.initialzie("重量", 3, weightVal, goods.getUnit(),
                        new DoubleInputDialog.OnResponseCallback() {
                            @Override
                            public void onQuantityChanged(Double quantity) {
                                addGoods2Cashier(curPosTradeNo, goods, quantity);
                            }
                        });
                quantityCheckDialog.show();
            }

        } else {
            addGoods2Cashier(curPosTradeNo, goods, 1D);
        }
    }

    /**
     * 添加商品到收银台
     */
    private void addGoods2Cashier(final String orderBarCode, final PosProductEntity goods, final Double bCount) {
        Observable.create(new Observable.OnSubscribe<List<CashierShopcartEntity>>() {
            @Override
            public void call(Subscriber<? super List<CashierShopcartEntity>> subscriber) {
                //添加商品
                CashierShopcartService.getInstance().append(orderBarCode, goods, bCount);

                //刷新订单列表
                List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                        .queryAllByDesc(String.format("posTradeNo = '%s'", orderBarCode));

                subscriber.onNext(shopcartEntities);
                subscriber.onCompleted();

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CashierShopcartEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<CashierShopcartEntity> cashierShopcartEntities) {
                        productAdapter.setEntityList(cashierShopcartEntities);
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
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);
    }

    /**
     * 针对当前用户所属网点提交营业现金，并触发一次日结操作
     */
    private void incomeDistributionTopup(final QuickPayInfo quickPayInfo) {
        ZLogger.df(String.format(">>>准备提交营业现金: %s", JSONObject.toJSONString(quickPayInfo)));

        if (alipayDialog == null) {
            alipayDialog = new AlipayDialog(this);
            alipayDialog.setCancelable(false);
            alipayDialog.setCanceledOnTouchOutside(false);
        }
        alipayDialog.initialize(quickPayInfo, false, new AlipayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed(QuickPayInfo mQuickPayInfo, String outTradeNo) {
                PrintManager.printTopupReceipt(quickPayInfo, outTradeNo);

                UploadSyncManager.getInstance().sync();
            }

            @Override
            public void onPayCanceled() {
            }

        });

        if (BizConfig.RELEASE && !alipayDialog.isShowing()) {
            alipayDialog.show();
        }
    }

    /**
     * 注册设备
     */
    private void registerPlat() {
        if (registerPlatDialog == null) {
            registerPlatDialog = new ActionDialog(this);
            registerPlatDialog.setCancelable(false);
            registerPlatDialog.setCanceledOnTouchOutside(false);
        }
        registerPlatDialog.initialize("注册设备", "", new ActionDialog.OnActionClickListener() {
            @Override
            public void onAction1() {
                // TODO: 8/8/16 注册设备
            }

            @Override
            public void onAction2() {
                // TODO: 8/8/16 暂不注册
                registerPlatDialog.dismiss();
            }

            @Override
            public void onAction3() {

            }
        });
        registerPlatDialog.setActions("立即注册", "暂不注册", null);
        if (!registerPlatDialog.isShowing()) {
            registerPlatDialog.show();
        }
    }


    /**
     * 显示广告
     */
    private void showAdvFragment() {
        HomeAdvFragment fragment;
        Intent intent = this.getIntent();
        if (intent != null) {
            fragment = HomeAdvFragment.newInstance(intent.getExtras());
        } else {
            fragment = HomeAdvFragment.newInstance(null);
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_plugin, fragment).show(fragment)
                .commit();
    }

    /**
     * 显示广告
     */
    private void showLocalFrontCategoryFragment() {
        LocalFrontCategoryFragment fragment;
        Intent intent = this.getIntent();
        if (intent != null) {
            fragment = LocalFrontCategoryFragment.newInstance(intent.getExtras());
        } else {
            fragment = LocalFrontCategoryFragment.newInstance(null);
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_plugin, fragment).show(fragment)
                .commit();
    }
}

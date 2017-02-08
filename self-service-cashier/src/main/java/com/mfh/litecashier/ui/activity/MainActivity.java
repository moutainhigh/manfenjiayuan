package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.PosProductSkuEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.bingshanguxue.cashier.hardware.led.LedAgent;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.bingshanguxue.cashier.model.wrapper.LastOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.bingshanguxue.cashier.model.wrapper.ResMenu;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.SyncButton;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.business.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.utils.BarcodeUtils;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IScOrderView;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.Priv;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.ResultCode;
import com.mfh.framework.uikit.compound.BadgeViewButton;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.alarm.AlarmManagerHelper;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfoWrapper;
import com.mfh.litecashier.bean.wrapper.HangupOrder;
import com.mfh.litecashier.bean.wrapper.LocalFrontCategoryGoods;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.DataDownloadManager;
import com.mfh.litecashier.service.DataUploadManager;
import com.mfh.litecashier.service.DemoPushService;
import com.mfh.litecashier.service.EslSyncManager2;
import com.mfh.litecashier.service.ValidateManager;
import com.mfh.litecashier.ui.ActivityRoute;
import com.mfh.litecashier.ui.cashier.CashierDesktopObservable;
import com.mfh.litecashier.ui.cashier.OrderPresentation;
import com.mfh.litecashier.ui.adapter.CashierMenuAdapter;
import com.mfh.litecashier.ui.adapter.CashierSwipAdapter;
import com.mfh.litecashier.ui.dialog.AlipayDialog;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;
import com.mfh.litecashier.ui.dialog.HangupOrderDialog;
import com.mfh.litecashier.ui.dialog.InitCardByStepDialog;
import com.mfh.litecashier.ui.dialog.QueryBalanceDialog;
import com.mfh.litecashier.ui.dialog.RegisterUserDialog;
import com.mfh.litecashier.ui.dialog.ReturnGoodsDialog;
import com.mfh.litecashier.ui.dialog.ValidatePhonenumberDialog;
import com.mfh.litecashier.ui.fragment.goods.LocalFrontCategoryFragment;
import com.mfh.litecashier.ui.fragment.goods.query.QueryGoodsFragment;
import com.mfh.litecashier.ui.fragment.pay.PayStep1Fragment;
import com.mfh.litecashier.ui.prepare.PrepareActivity;
import com.mfh.litecashier.ui.prepare.PrepareStep2Fragment;
import com.mfh.litecashier.ui.view.ICashierView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.GlobalInstance;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;



/**
 * 首页
 * Created by bingshanguxue on 15/8/30.
 */

public class MainActivity extends CashierActivity
        implements ICashierView, IScOrderView, QueryGoodsFragment.OnFragmentListener {

    @BindView(R.id.slideMenu)
    RecyclerView menuRecyclerView;
    private CashierMenuAdapter menuAdapter;
    @BindView(R.id.button_sync)
    SyncButton btnSync;
    @BindView(R.id.tv_last_amount)
    TextView tvLastAmount;
    @BindView(R.id.tv_last_quantity)
    TextView tvLastQuantity;
    @BindView(R.id.tv_last_discount)
    TextView tvLastDiscount;
    @BindView(R.id.tv_last_charge)
    TextView tvLastCharge;
    @BindView(R.id.label_quantity)
    MultiLayerLabel labelQuantity;
    @BindView(R.id.label_amount)
    MultiLayerLabel labelAmount;
    @BindView(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;
    @BindView(R.id.product_list)
    RecyclerView productRecyclerView;
    private ItemTouchHelper itemTouchHelper;
    private CashierSwipAdapter productAdapter;
    @BindView(R.id.fab_pick)
    ImageButton btnPick;
    @BindView(R.id.fab_settle)
    ImageButton btnSettle;
    @BindView(R.id.float_hangup)
    TextView fabHangup;
    @BindView(R.id.fab_orderDiscount)
    TextView fabOrderDiscount;
    @BindView(R.id.buttonPrepareOrder)
    BadgeViewButton btnPrepareOrder;

    private Fragment mQueryGoodsFragment;
    private Fragment mLocalFrontCategoryFragment;

    private NumberInputDialog barcodeInputDialog = null;
    private DoubleInputDialog commitPriceDialog = null;//价格为空，补填价格
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


    /**
     * POS唯一订单号，由POS机本地生成的12位字符串
     */
    private String curPosTradeNo;
    /**
     * 订单折扣，默认值为1，新扫描商品默认使用该折扣
     */
    private Double orderDiscount = 100D;

    private CashierPresenter cashierPresenter;
    private ScOrderPresenter mScOrderPresenter;


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
        mScOrderPresenter = new ScOrderPresenter(this);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (!BizConfig.RELEASE) {
            DialogUtil.showHint("您正在使用的是测试版本，如需切换到正式版本请联系服务商。");
        }

        setupGetui();

        initMenuRecyclerView();
        initBarCodeInput();
        initCashierRecyclerView();

        if (savedInstanceState != null) {  // “内存重启”时调用
            mQueryGoodsFragment = getSupportFragmentManager().findFragmentByTag("QueryGoodsFragment");
            mLocalFrontCategoryFragment = getSupportFragmentManager().findFragmentByTag("LocalFrontCategoryFragment");
            // 解决重叠问题
            getSupportFragmentManager().beginTransaction()
                    .show(mLocalFrontCategoryFragment)
                    .hide(mQueryGoodsFragment)
                    .commit();
        }else{  // 正常时
            mQueryGoodsFragment = QueryGoodsFragment.newInstance(null, this);
            mLocalFrontCategoryFragment = LocalFrontCategoryFragment.newInstance(null);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_plugin, mQueryGoodsFragment, "QueryGoodsFragment")
                    .add(R.id.fragment_plugin,mLocalFrontCategoryFragment, "LocalFrontCategoryFragment")
                    .hide(mQueryGoodsFragment)
                    .commit();
        }

        if (MfhUserManager.getInstance().containsModule(Priv.FUNC_SUPPORT_BUY)){
            btnPrepareOrder.setVisibility(View.VISIBLE);
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.ORDER_TRANS_NOTIFY);
            btnPrepareOrder.setBadgeNumber(count);
        }
        else{
            ZLogger.d("当前登录用户不具有买手能力");
//            btnPrepareOrder.setBadgeNumber(0);
            btnPrepareOrder.setVisibility(View.GONE);
//            btnPrepareOrder.setVisibility(View.VISIBLE);
        }

        if (menuAdapter != null) {
            menuAdapter.setEntityList(cashierPresenter.getCashierFunctions());
        }


        reload();

        initPresentation();


        CashierDesktopObservable.getInstance().addObserver(new java.util.Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                labelQuantity.setTopText(String.format("%.2f", CashierDesktopObservable.getInstance().getBcount()));
                labelAmount.setTopText(String.format("%.2f", CashierDesktopObservable.getInstance().getAmount()));//成交价
            }
        });

        ValidateManager.get().batchValidate();

        AlarmManagerHelper.registerBuglyUpgrade(this);
        AlarmManagerHelper.triggleNextDailysettle(0);

//        MfhUserManager.getInstance().updateModules();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupGetui();
        if (inlvBarcode == null) {
            inlvBarcode = (InputNumberLabelView) findViewById(R.id.inlv_barcode);
        }

        if (inlvBarcode != null) {
            inlvBarcode.clear();
            inlvBarcode.requestFocusEnd();
        }

        int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.NEW_PURCHASE_ORDER);
        if (menuAdapter != null) {
            menuAdapter.setBadgeNumber(ResMenu.CASHIER_MENU_ONLINE_ORDER, count);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        AppHelper.clearCacheData();

        hidePresentation();
    }


    @Override
    public void onBackPressed() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", true, false);
        isWaitForExit = true;
        DataUploadManager.getInstance().syncDefault();
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

        menuAdapter = new CashierMenuAdapter(CashierApp.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new CashierMenuAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                ResMenu entity = menuAdapter.getEntity(position);
                if (entity != null) {
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

        if (id.compareTo(ResMenu.CASHIER_MENU_ONLINE_ORDER) == 0) {
            redirectToOnlineOrder();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_PACKAGE) == 0) {
            packageService();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_REGISTER_VIP) == 0) {
            registerVIPStep1();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_BALANCE_QUERY) == 0) {
//            if (mPresentation != null) {
//                mPresentation.setText("查询余额");
//            }
            queryBalance();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_MEMBER_CARD) == 0) {
            initVipCardStep1();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_RETURN_GOODS) == 0) {
            returnGoods();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_MONEYBOX) == 0) {
            openMoneyBox();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_HANGUP_ORDER) == 0) {
//            if (mPresentation != null) {
//                mPresentation.dismiss();
//            } else {
//                DialogUtil.showHint("不支持双屏");
//            }
            hangUpOrder();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_SETTINGS) == 0) {
            redirectToSettings();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_DISCOUNT) == 0) {
//            if (mPresentation != null) {
//                mPresentation.setText("折扣");
//            } else {
//                DialogUtil.showHint("不支持双屏");
//            }
            changeOrderDiscount();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_PRINT_ORDER) == 0) {
//            printScOrder();
            printPrepareOrder();
        } else if (id.compareTo(ResMenu.CASHIER_MENU_SCORE) == 0) {
//            if (mPresentation != null) {
//                mPresentation.setText("积分兑换");
//            } else {
//                DialogUtil.showHint("不支持双屏");
//            }
            exchangeScore();
        } else {
            DialogUtil.showHint(R.string.coming_soon);
        }
    }

    /**
     * 取包裹
     */
    private void packageService() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE, FragmentActivity.FT_STOCK_DETAIL);
        FragmentActivity.actionStart(this, extras);
    }

    /**
     * 余额查询
     */
    private void queryBalance() {
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
            menuAdapter.setBadgeNumber(ResMenu.CASHIER_MENU_ONLINE_ORDER,
                    0);
        }

        ActivityRoute.redirect2OrderList(this);
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
        initCardDialog.initialize(human, new InitCardByStepDialog.OnInitCardListener() {
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
     * 同步数据
     */
    @OnClick(R.id.button_sync)
    public void syncData() {
        btnSync.startSync();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSync.stopSync();
            return;
        }

        //同步数据
        ZLogger.df("点击[同步]，准备下载数据...");
        DataDownloadManager.get().manualSync();
    }

    /**
     * 数据同步
     */
    private void dataSync() {
        if (MfhLoginService.get().isCompanyOrOfficeChanged()) {
            AppHelper.clearAppData();
        }

        btnSync.startSync();
        DataDownloadManager.get().launcherSync();
    }

    public void redirectToSettings() {
        if (!MfhLoginService.get().haveLogined()) {
            redirectToLogin();
        } else {
//            enterAdministratorMode();
            UIHelper.startActivity(MainActivity.this, AdministratorActivity.class);
        }
    }

    /**
     * 重新加载数据
     */
    private void reload() {
        try {
            if (inlvBarcode != null) {
                inlvBarcode.clear();
                inlvBarcode.requestFocusEnd();
            }

            MfhUserManager.getInstance().updateModules();

            //刷新上一单数据
            refreshLastOrder(null);
            CashierHelper.broadcastCashierOrderInfo(CashierOrderInfoWrapper.CMD_CLEAR_ORDER, null);


            //加载订单
            if (!StringUtils.isEmpty(curPosTradeNo)) {
                CashierShopcartService.getInstance()
                        .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
            }
            obtaincurPosTradeNo(null);
            if (productAdapter != null) {
                productAdapter.setEntityList(null);
            }
            changeOrderDiscount(false, 100D);

            //刷新挂单
            refreshFloatHangup();

            DataUploadManager.getInstance().syncDefault();

             //清除缓存数据
            ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME).clear();

            ZLogger.d("重新加载数据，准备同步数据...");
            dataSync();
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.ef(e.toString());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AffairEvent event) {
        int eventId = event.getAffairId();
        Bundle bundle = event.getArgs();
        ZLogger.d(String.format("AffairEvent(%d)", eventId));
        if (eventId == AffairEvent.EVENT_ID_APPEND_UNREAD_SKU) {
            btnSync.startSync();
            btnSync.setBadgeEnabled(false);
            DataDownloadManager.get().syncProducts();
        } else if (eventId == AffairEvent.EVENT_ID_ORDER_TRANS_NOTIFY) {
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.ORDER_TRANS_NOTIFY);
            ZLogger.d("待拣货订单未读消息个数为：" + count);
            if (count > 0) {
                if (mTtsBinder != null){
                    mTtsBinder.cloudSpeak("您有新订单，请尽快处理");
                }
                btnPrepareOrder.setVisibility(View.VISIBLE);
                btnPrepareOrder.setBadgeNumber(count);
            }
            else{
                btnPrepareOrder.setVisibility(View.GONE);
            }

        } else if (eventId == AffairEvent.EVENT_ID_APPEND_UNREAD_SCHEDULE_ORDER) {
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.NEW_PURCHASE_ORDER);
            menuAdapter.setBadgeNumber(ResMenu.CASHIER_MENU_ONLINE_ORDER, count);
            ZLogger.d("生鲜预定订单未读消息个数为：" + count);
            if (count > 0) {
                if (mTtsBinder != null){
                    mTtsBinder.cloudSpeak("您有新订单，请尽快处理");
                }
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
            reload();
            ValidateManager.get().stepValidate(ValidateManager.STEP_REGISTER_PLAT);
            EventBus.getDefault().post(new DataDownloadManager.DataDownloadEvent(DataDownloadManager.DataDownloadEvent.EVENT_FRONTEND_CATEGORY_UPDATED));

        } else if (eventId == AffairEvent.EVENT_ID_CASHIER_FRONTCATA_GOODS) {
            LocalFrontCategoryGoods goods = (LocalFrontCategoryGoods) bundle.getSerializable("goods");
            cashierGoods(goods);
        }

    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataDownloadManager.DataDownloadEvent event) {
        ZLogger.d(String.format("DataDownloadEvent(%d)", event.getEventId()));
        switch (event.getEventId()) {
            case DataDownloadManager.DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS: {
                btnSync.startSync();
                btnSync.setBadgeEnabled(false);
            }
            break;
            case DataDownloadManager.DataDownloadEvent.EVENT_POSPRODUCTS_UPDATED: {
                EslSyncManager2.getInstance().sync();
                SMScaleSyncManager2.getInstance().sync();
            }
            break;
            case DataDownloadManager.DataDownloadEvent.EVENT_ID_SYNC_DATA_FINISHED: {
                hideProgressDialog();
                btnSync.stopSync();
            }
            break;
        }
    }

    boolean isWaitForExit = false;

    /**
     * 上传数据到云端
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataUploadManager.UploadSyncManagerEvent event) {
        ZLogger.d(String.format("UploadSyncManagerEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataUploadManager.UploadSyncManagerEvent.EVENT_ID_SYNC_DATA_ERROR) {
            if (isWaitForExit) {
                isWaitForExit = false;
                AppHelper.closeApp();
            }
        } else if (event.getEventId() == DataUploadManager.UploadSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            if (isWaitForExit) {
                isWaitForExit = false;
                AppHelper.closeApp();
            }
        }
    }

    /**
     * 验证
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
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
                ZLogger.d("重登录成功，准备同步数据...");
                dataSync();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER: {
                GlobalInstance.getInstance().registerPos(MainActivity.this);
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
//                Beta.checkUpgrade(false, false);
            }
            break;
        }
    }

    /**
     * 跳转至拣货页面
     */
    @OnClick(R.id.fab_pick)
    public void redirect2Pick() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        btnPick.setEnabled(false);

        //判断是否登录
        if (!MfhLoginService.get().haveLogined()) {
            DialogUtil.showHint("请先登录");
            btnSettle.setEnabled(true);
            hideProgressDialog();
            return;
        }

        Intent intent = new Intent(MainActivity.this, PrepareActivity.class);
        Bundle extras = new Bundle();
        extras.putString(PrepareActivity.EXTRA_KEY_TRADENO, curPosTradeNo);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_CASHIER_PREPAREGOODS);
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

        //判断商品明细是否为空
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
                CashierOrderInfo cashierOrderInfo = CashierAgent.settle(BizType.POS,
                        PosType.POS_STANDARD, curPosTradeNo, null,
                        PosOrderEntity.ORDER_STATUS_STAY_PAY,
                        productAdapter.getEntityList());
                if (cashierOrderInfo != null) {
                    ZLogger.df(String.format("[点击结算]--生成结算信息：%s",
                            JSON.toJSONString(cashierOrderInfo)));

                }

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
                        //显示客显
                        CashierHelper.broadcastCashierOrderInfo(CashierOrderInfoWrapper.CMD_PAY_ORDER, cashierOrderInfo);
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

                        saveSettleResult(curPosTradeNo, cashierOrderInfo);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    ZLogger.df("取消收银订单支付");
                    boolean isClearOrder = false;
                    if (data != null) {
                        isClearOrder = data.getBooleanExtra(PayStep1Fragment.EXTRA_KEY_IS_CLEAR_ORDER, false);
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
                        changeOrderDiscount(false, 100D);
                    }
                    btnSettle.setEnabled(true);

                } else {
                    ZLogger.df("取消收银订单支付2");
                    btnSettle.setEnabled(true);
                }
            }
            break;
            case Constants.ARC_CASHIER_PREPAREGOODS: {
                if (resultCode == Activity.RESULT_OK) {
                    ZLogger.df("订单组货成功");
                    if (!StringUtils.isEmpty(curPosTradeNo)) {
                        CashierShopcartService.getInstance()
                                .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
                    }
                    obtaincurPosTradeNo(null);
                    productAdapter.setEntityList(null);
                    changeOrderDiscount(false, 100D);

                    if (data != null) {
                        ZLogger.df("配送成功: " + StringUtils.decodeBundle(data.getExtras()));
                        boolean isTakeoutOrder = data.getBooleanExtra("isTakeOutOrder", false);
                        if (isTakeoutOrder) {
                            String posTradeNo = data.getStringExtra("posTradeNo");
                            if (!StringUtils.isEmpty(posTradeNo)) {
                                PosOrderEntity orderEntity = CashierAgent.fetchOrderEntity(BizType.POS,
                                        posTradeNo);
                                if (orderEntity != null) {
                                    PrinterFactory.getPrinterManager().printPosOrder(orderEntity);
                                }

                                //同步订单信息
                                if (SharedPreferencesUltimate.isUploadPosOrderRealtime()){
                                    DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
                                }
                            }
                        } else {
                            ScOrder scOrder = (ScOrder) data.getSerializableExtra(PrepareStep2Fragment.EXTRA_KEY_SCORDER);
                            if (scOrder != null) {
                                PrinterFactory.getPrinterManager().printSendOrder(scOrder);
                            }
                        }
                    }
                }

                btnPick.setEnabled(true);
            }
            break;
            case ResultCode.ARC_NATIVE_SIGNIN: {
                if (resultCode == Activity.RESULT_OK) {
                    DialogUtil.showHint("登录成功");
                    reload();
                    ValidateManager.get().stepValidate(ValidateManager.STEP_REGISTER_PLAT);
                    EventBus.getDefault().post(new DataDownloadManager.DataDownloadEvent(DataDownloadManager.DataDownloadEvent.EVENT_FRONTEND_CATEGORY_UPDATED));
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
    private void saveSettleResult(final String posTradeNo, final CashierOrderInfo cashierOrderInfo) {
        //清空当前收银列表，开始新的订单
        obtaincurPosTradeNo(null);
        productAdapter.setEntityList(null);
        changeOrderDiscount(false, 100D);
        // TODO: 7/5/16 下个版本放到支付页面去,更新客显，支付完成
        CashierHelper.broadcastCashierOrderInfo(CashierOrderInfoWrapper.CMD_FINISH_ORDER,
                null);

        Observable.create(new Observable.OnSubscribe<LastOrderInfo>() {
            @Override
            public void call(Subscriber<? super LastOrderInfo> subscriber) {

                //重新生成订单，清空购物车
                if (!StringUtils.isEmpty(posTradeNo)) {
                    CashierShopcartService.getInstance()
                            .deleteBy(String.format("posTradeNo = '%s'", posTradeNo));
                }

                ZLogger.df(String.format("%s支付，流水编号：%s\n%s",
                        WayType.name(cashierOrderInfo.getBizType()), cashierOrderInfo.getPosTradeNo(),
                        JSONObject.toJSONString(cashierOrderInfo)));

                PosOrderEntity orderEntity = CashierAgent.fetchOrderEntity(BizType.POS,
                        cashierOrderInfo.getPosTradeNo());

                //实时同步订单信息
                if (SharedPreferencesUltimate.isUploadPosOrderRealtime()){
                    DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
                }
                //打印订单
                PrinterFactory.getPrinterManager().printPosOrder(orderEntity);

                //保存上一单信息
                LastOrderInfo lastOrderInfo = CashierAgent.genLastOrderInfo(orderEntity);
                if (lastOrderInfo != null) {
                    int payType = lastOrderInfo.getPayType();
                    Double finalAmount = lastOrderInfo.getFinalAmount();
                    Double changeAmount = lastOrderInfo.getChangeAmount();
                    Double bCount = lastOrderInfo.getbCount();

                    //显示找零
//        SerialManager.show(4, Math.abs(cashierOrderInfo.getHandleAmount()));
                    LedAgent.vfdShow(String.format("Change:%.2f\r\nThank You!", changeAmount));

                    if (changeAmount >= 0.01) {
                        if (mTtsBinder != null){
                            mTtsBinder.cloudSpeak(String.format("%s 支付 %.2f 元, 找零 %.2f 元，商品数量 %.0f, 谢谢光临！",
                                    WayType.name(payType), finalAmount, changeAmount, bCount));
                        }
                    } else {
                        if (mTtsBinder != null){
                            mTtsBinder.cloudSpeak(String.format("%s 支付 %.2f 元, 商品数量 %.0f, 谢谢光临！",
                                    WayType.name(payType), finalAmount, bCount));
                        }
                    }
                }
                subscriber.onNext(lastOrderInfo);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LastOrderInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(LastOrderInfo lastOrderInfo) {
                        refreshLastOrder(lastOrderInfo);

                        btnSettle.setEnabled(true);
                    }

                });
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
                // TODO: 07/02/2017
                if (productAdapter.getItemCount() > 0) {
                    btnSettle.setEnabled(true);
                } else {
                    LedAgent.clear();
                    btnSettle.setEnabled(false);
                }

                CashierDesktopObservable.getInstance().setShopcartEntities(productAdapter.getEntityList());
//                labelQuantity.setTopText(String.format("%.2f", productAdapter.getBcount()));
//                labelAmount.setTopText(String.format("%.2f", productAdapter.getFinalAmount()));//成交价

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
        PrinterFactory.getPrinterManager().openMoneyBox();
    }

    /**
     * 初始化条码输入
     */
    private void initBarCodeInput() {
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            //条码枪扫描结束后会自动触发回车键
                            searchGoodsByBarcode(text);
//                        searchGoodsByBarcodeRx(text);
                        }
                        //Press “*”
                        if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                            //判断是否已经有数字，如果已经有则直接加数字，否则弹窗
                            if (StringUtils.isEmpty(text)) {
                                changeGoodsQuantity(0);
//                            if (productAdapter != null) {
//                                productAdapter.changeQuantity();
//                            }
                            } else {
                                inlvBarcode.clear();
                                try {
                                    if (productAdapter != null) {
                                        productAdapter.changeQuantity(Double.valueOf(text));
                                    }
                                } catch (Exception e) {
                                    ZLogger.e(e.toString());
                                }
                            }
                        }
                        //Press “＋”
                        if (keyCode == KeyEvent.KEYCODE_NUMPAD_ADD) {
                            if (btnSettle.isEnabled()) {
                                settle();
                            }
                        }
                    }
                });
        inlvBarcode.registerOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onClickAction1(String text) {
                if (inlvBarcode.isAction1Selected()){
                    redirect2LocalCategory();
                }
                else{
                    redirect2QueryGoods();
                }
            }

            @Override
            public void onLongClickAction1(String text) {

            }
        });
        inlvBarcode.requestFocusEnd();
        inlvBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvBarcode.isSoftKeyboardEnabled()) {
                        showBarcodeKeyboard();
                    }
                }

                inlvBarcode.requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
            }
        });
    }


    /**
     * 显示条码输入界面
     * 相当于扫描条码
     */
    private void showBarcodeKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(this);
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.BARCODE, "收银", "商品条码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
//                        inlvBarcode.setInputString(value);
                        searchGoodsByBarcode(value);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
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
        if (!changePriceDialog.isShowing()) {
            changePriceDialog.show();
        }
    }


    /**
     * 修改商品折扣
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
        changeDiscountDialog.initialzie("商品折扣", 0,
                100 * CashierAgent.calculatePriceDiscount(entity.getCostPrice(), entity.getFinalPrice()),
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
        if (!changeDiscountDialog.isShowing()) {
            changeDiscountDialog.show();
        }
    }

    /**
     * 积分兑换
     */
    private void exchangeScore() {
        ActivityRoute.redirect2ExchangeScore(this);
    }

    /**
     * 打印拣货单
     */
    private void printPrepareOrder() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(this);
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.BARCODE, "拣货单", "订单条码", "打印",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        mScOrderPresenter.getByBarcode(value, ScOrder.MFHORDER_STATUS_BUYING, true);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    /**
     * 修改订单折扣
     */
    private void changeOrderDiscount(boolean enabled, Double discount) {
        orderDiscount = discount;
        if (enabled) {
            fabOrderDiscount.setText(String.format("%.0f%%", discount));
            fabOrderDiscount.setVisibility(View.VISIBLE);
            batchMakeDiscount(curPosTradeNo, discount);
        } else {
            fabOrderDiscount.setVisibility(View.GONE);
        }
    }

    /**
     * 修改订单折扣
     */
    @OnClick(R.id.fab_orderDiscount)
    public void changeOrderDiscount() {
        if (changeDiscountDialog == null) {
            changeDiscountDialog = new DoubleInputDialog(this);
            changeDiscountDialog.setCancelable(true);
            changeDiscountDialog.setCanceledOnTouchOutside(true);
        }
        changeDiscountDialog.initialzie("订单折扣", 0, orderDiscount, "%",
                new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double value) {
                        changeOrderDiscount(true, value);
                    }
                });
        if (!changeDiscountDialog.isShowing()) {
            changeDiscountDialog.show();
        }
    }

    /**
     * 批量修改订单折扣
     */
    private void batchMakeDiscount(final String posTradeNo, final Double discount) {
        Observable.create(new Observable.OnSubscribe<List<CashierShopcartEntity>>() {
            @Override
            public void call(Subscriber<? super List<CashierShopcartEntity>> subscriber) {

                CashierShopcartService.getInstance().batchDiscount(posTradeNo, discount);

                List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                        .queryAllBy(String.format("posTradeNo = '%s'", curPosTradeNo));

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
        LedAgent.clear();
        //Step 1:
        if (productAdapter.getItemCount() > 0) {
            ZLogger.d(String.format("挂单：%s", curPosTradeNo));
            CashierAgent.settle(BizType.POS, PosType.POS_STANDARD, curPosTradeNo, null,
                    PosOrderEntity.ORDER_STATUS_HANGUP, productAdapter.getEntityList());

            //刷新挂单
            refreshFloatHangup();
        }

//        //重新生成订单
        if (!StringUtils.isEmpty(curPosTradeNo)) {
            CashierShopcartService.getInstance()
                    .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
        }
        obtaincurPosTradeNo(null);
        productAdapter.setEntityList(null);
        changeOrderDiscount(false, 100D);
    }

    /**
     * 调单
     */
    private void resumeOrder(String posTradeNo) {
        inlvBarcode.clear();
        LedAgent.clear();

        if (productAdapter.getItemCount() > 0) {
            //挂起当前订单
            ZLogger.df(String.format("挂单：%s", curPosTradeNo));
            CashierAgent.settle(BizType.POS, PosType.POS_STANDARD, curPosTradeNo, null,
                    PosOrderEntity.ORDER_STATUS_HANGUP, productAdapter.getEntityList());
        }

        ZLogger.df(String.format("调单：%s", posTradeNo));

        //加载新订单
        obtaincurPosTradeNo(posTradeNo);
        //加载明细
        CashierShopcartService.getInstance().readOrderItems(posTradeNo,
                CashierAgent.resume(BizType.POS, posTradeNo));
        List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                .queryAllBy(String.format("posTradeNo = '%s'", posTradeNo));
        productAdapter.setEntityList(shopcartEntities);
        changeOrderDiscount(false, 100D);

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

//        cashierPresenter.findGoods(barCode);

        Observable.create(new Observable.OnSubscribe<PosProductEntity>() {
            @Override
            public void call(Subscriber<? super PosProductEntity> subscriber) {

                if (StringUtils.isEmpty(barCode)) {
                    subscriber.onError(new Throwable("商品条码无效"));
                    return;
                }

                PosProductEntity goods;

                if (BarcodeUtils.getType(barCode) == BarcodeUtils.BARCODE_DIGI){
                    String plu = BarcodeUtils.getDigiPlu(barCode);
//                    String plu = barCode.substring(1, 7);
//                    String weightStr = String.format("%s.%s", barCode.substring(7, 9), barCode.substring(9, 12));
//                    Double weight = Double.valueOf(weightStr);
//                    ZLogger.df(String.format("搜索生鲜商品 条码：%s, PLU码：%s, 重量：%f",
//                            barCode, plu, weight));

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

//        if (goods.getStatus() != 1) {
//            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
//            return;
//        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            final Double weightVal = GlobalInstance.getInstance().getNetWeight();
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

//        if (goods.getStatus() != 1) {
//            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
//            return;
//        }

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
        ActivityRoute.redirect2StoreIn(this, barcode);
    }

    private void cashierGoods(final LocalFrontCategoryGoods goods) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

//        if (goods.getStatus() != 1) {
//            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
//            return;
//        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            final Double weightVal = GlobalInstance.getInstance().getNetWeight();
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
    private void addGoods2Cashier(final String orderBarCode, final PosProductEntity goods,
                                  final Double bCount) {
        if (goods == null) {
            return;
        }

        Double costPrice = goods.getCostPrice();
        if (costPrice == null) {
            ZLogger.df("商品零售价为空，补填后才可以收银");
            commitGoodsCostprice1(orderBarCode, goods, bCount);
        } else {
            saveGoods2Cashier(orderBarCode, goods, bCount);
        }
    }

    /**
     * 补填商品零售价信息:输入商品价格
     */
    private void commitGoodsCostprice1(final String orderBarCode, final PosProductEntity goods,
                                       final Double bCount) {
        if (commitPriceDialog == null) {
            commitPriceDialog = new DoubleInputDialog(this);
            commitPriceDialog.setCancelable(true);
            commitPriceDialog.setCanceledOnTouchOutside(true);
        }
        commitPriceDialog.initialzie("零售价", 2, 0D, "元",
                new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        goods.setCostPrice(quantity);
                        commitGoodsCostprice2(orderBarCode, goods, bCount);
                    }
                });
        commitPriceDialog.setMinimumDoubleCheck(0.01D, true);
        if (!commitPriceDialog.isShowing()) {
            commitPriceDialog.show();
        }

    }

    /**
     * 补填商品零售价信息：提交修改信息
     */
    private void commitGoodsCostprice2(final String orderBarCode, final PosProductEntity goods,
                                       final Double bCount) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                        //{"code":"0","msg":"更新成功!","version":"1","data":""}
//                                RspValue<String> retValue = (RspValue<String>) rspData;
//                                String retStr = retValue.getValue();
//                                ZLogger.d("修改售价成功:" + retStr);

//                        DialogUtil.showHint("修改零售价成功");
                        PosProductService.get().saveOrUpdate(goods);

                        saveGoods2Cashier(orderBarCode, goods, bCount);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("修改失败：" + errMsg);
                        DialogUtil.showHint(errMsg);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", goods.getId());
        jsonObject.put("costPrice", goods.getCostPrice());
        jsonObject.put("tenantId", MfhLoginService.get().getSpid());
        InvSkuStoreApiImpl.update(jsonObject.toJSONString(), responseCallback);
    }

    /**
     * 保存商品到收银台
     */
    private void saveGoods2Cashier(final String orderBarCode, final PosProductEntity goods,
                                   final Double bCount) {
        Observable.create(new Observable.OnSubscribe<List<CashierShopcartEntity>>() {
            @Override
            public void call(Subscriber<? super List<CashierShopcartEntity>> subscriber) {
                //添加商品
                CashierShopcartService.getInstance().append(orderBarCode, orderDiscount, goods, bCount);

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
//        SharedPreferencesUltimate.setSyncFrontCategorySubEnabled(true);
        //设置需要更新商品中心,商品后台类目
//        SharedPreferencesUltimate.setSyncEnabled(SharedPreferencesUltimate.PREF_KEY_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);

//        MobclickAgent.onProfileSignOff();
//        AppHelper.resetMemberAccountData();

        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_OFFICELIST);
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ResultCode.ARC_NATIVE_SIGNIN);
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
                PrinterFactory.getPrinterManager().printTopupReceipt(quickPayInfo, outTradeNo);
                DataUploadManager.getInstance().syncDefault();
            }

            @Override
            public void onPayCanceled() {
            }

        });

        if (!alipayDialog.isShowing()) {
            alipayDialog.show();
        }
    }


    @Override
    public void onIScOrderViewProcess() {
//        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", true, false);
    }

    @Override
    public void onIScOrderViewError(String errorMsg) {
        DialogUtil.showHint(errorMsg);
    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {

    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {
        if (data != null) {
            PrinterFactory.getPrinterManager().printPrepareOrder(data);
        }
    }

    private MediaRouter mMediaRouter;
    private OrderPresentation mPresentation;

    /**
     * 初始化双屏异显
     * */
    private void initPresentation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            boolean isMultiWindowSupported = isInMultiWindowMode();
        }

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        //presentationDisplays[0]主屏；presentationDisplays[1]分屏
        if (presentationDisplays.length > 0) {
            // If there is more than one suitable presentation display, then we could consider
            // giving the user a choice.  For this example, we simply choose the first display
            // which is the one the system recommends as the preferred presentation display.
            Display display = presentationDisplays[0];
            mPresentation = new OrderPresentation(this, display);

            mPresentation.show();
        }

        // Get the media router service.
        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        updatePresentation();
    }

    private void updatePresentation() {
        // Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            ZLogger.d("Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            ZLogger.d("Showing presentation on display: " + presentationDisplay);
            mPresentation = new OrderPresentation(this, presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                ZLogger.d("Couldn't show presentation!  Display was removed in "
                        + "the meantime.");
                mPresentation = null;
            }
        }
    }


    /**
     * 关闭双屏异显
     * */
    private void hidePresentation(){
        if (mPresentation != null) {
            mPresentation.dismiss();
        }
    }

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        ZLogger.d("Presentation was dismissed.");
                        mPresentation = null;
                    }
                }
            };

    @Override
    public void onAddGoods(PosProductEntity productEntity) {
        onFindGoods(productEntity, 0);
    }

    /**
     * 个推SDK初始化
     * 我们建议应用开发者在Activity或Service类中调用个推SDK的初始化方法，确保SDK在各种情况下都能正常运行。
     * 一般情况下可以在主Activity的onCreate()或者onResume()方法中调用，也可以在多个主要界面Activity的
     * onCreate()或onResume()方法中调用。反复调用SDK初始化并不会有什么副作用。
     */
    private void setupGetui() {
        ZLogger.df("准备初始化个推服务...");
        PushManager.getInstance().initialize(this.getApplicationContext(), DemoPushService.class);

        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
        // IntentService, 必须在 AndroidManifest 中声明)
//        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), DemoIntentService.class);

        // 检查 so 是否存在
        File file = new File(this.getApplicationInfo().nativeLibraryDir + File.separator + "libgetuiext2.so");
        ZLogger.df("libgetuiext2.so exist = " + file.exists());

        String cid = PushManager.getInstance().getClientid(CashierApp.getAppContext());
        ZLogger.df("当前应用的cid = " + cid);
    }

    /**
     * 接单
     * */
    @OnClick(R.id.buttonPrepareOrder)
    public void redirect2PrepareOrder(){
        EmbMsgService.getInstance().setAllRead(IMBizType.ORDER_TRANS_NOTIFY);
        btnPrepareOrder.setBadgeNumber(0);

        ActivityRoute.redirect2PrepareAbleOrders(this);
    }

    /**
     * 查询商品
     */
    private void redirect2QueryGoods(){
        inlvBarcode.setAction1Selected(true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        mQueryGoodsFragment = getSupportFragmentManager().findFragmentByTag("QueryGoodsFragment");
        if (mQueryGoodsFragment == null){
            mQueryGoodsFragment = QueryGoodsFragment.newInstance(null, this);
            fragmentTransaction.add(R.id.fragment_plugin, mQueryGoodsFragment, "QueryGoodsFragment");
        }
        ((QueryGoodsFragment)mQueryGoodsFragment).setOnFragmentListener(this);
        mLocalFrontCategoryFragment = getSupportFragmentManager().findFragmentByTag("LocalFrontCategoryFragment");
        if (mLocalFrontCategoryFragment == null){
            mLocalFrontCategoryFragment = LocalFrontCategoryFragment.newInstance(null);
            fragmentTransaction.add(R.id.fragment_plugin, mLocalFrontCategoryFragment, "LocalFrontCategoryFragment");
        }

        fragmentTransaction.show(mQueryGoodsFragment)
                .hide(mLocalFrontCategoryFragment).commit();
    }

    /**
     * 前台类目
     */
    private void redirect2LocalCategory(){
        inlvBarcode.setAction1Selected(false);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        mQueryGoodsFragment = getSupportFragmentManager().findFragmentByTag("QueryGoodsFragment");
        if (mQueryGoodsFragment == null){
            mQueryGoodsFragment = QueryGoodsFragment.newInstance(null, this);
            fragmentTransaction.add(R.id.fragment_plugin, mQueryGoodsFragment, "QueryGoodsFragment");
        }

        mLocalFrontCategoryFragment = getSupportFragmentManager().findFragmentByTag("LocalFrontCategoryFragment");
        if (mLocalFrontCategoryFragment == null){
            mLocalFrontCategoryFragment = LocalFrontCategoryFragment.newInstance(null);
            fragmentTransaction.add(R.id.fragment_plugin, mLocalFrontCategoryFragment, "LocalFrontCategoryFragment");
        }

        fragmentTransaction.show(mLocalFrontCategoryFragment)
                .hide(mQueryGoodsFragment).commit();
    }

}

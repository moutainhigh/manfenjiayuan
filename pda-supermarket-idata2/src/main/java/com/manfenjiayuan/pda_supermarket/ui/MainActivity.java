package com.manfenjiayuan.pda_supermarket.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bingshanguxue.pda.IData95Activity;
import com.bingshanguxue.pda.ValidateManager;
import com.bingshanguxue.pda.alarm.AlarmManagerHelper;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.bizz.company.CompanyListFragment;
import com.bingshanguxue.pda.bizz.home.HomeAdapter;
import com.bingshanguxue.pda.bizz.home.HomeMenu;
import com.bingshanguxue.pda.utils.DialogManager;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.bingshanguxue.vector_uikit.widget.NaviAddressView;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.business.view.IPosRegisterView;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.event.AffairEvent;
import com.manfenjiayuan.pda_supermarket.service.DataDownloadManager;
import com.manfenjiayuan.pda_supermarket.service.DataUploadManager;
import com.manfenjiayuan.pda_supermarket.service.DemoPushService;
import com.manfenjiayuan.pda_supermarket.utils.AlertBeepManager;
import com.manfenjiayuan.pda_supermarket.utils.DataCacheHelper;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Office;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.api.constant.Priv;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.ResultCode;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;



/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class MainActivity extends IData95Activity implements IPosRegisterView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.address_view)
    NaviAddressView addressView;
    @BindView(R.id.menu_option)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HomeAdapter menuAdapter;
    private AlertBeepManager mBeepManager;

    boolean isWaitForExit = false;

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

    MenuItem menuLogin = null;

    @Override
    protected void initToolBar() {
        super.initToolBar();

//        setSupportActionBar(toolbar);

        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
//                if (id == R.id.action_sign_out) {
//                    showLogoutAlert();
//                } else
                if (id == R.id.action_sign_in) {
                    if (MfhLoginService.get().haveLogined()) {

                        Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_MY);
                        Intent intent = new Intent(MainActivity.this, PrimaryActivity.class);
                        intent.putExtras(extras);
                        startActivityForResult(intent, ARCode.ARC_MY);
                    } else {
                        redirectToLogin();
                    }
                }
                return true;
            }
        });
//        addressView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (MfhLoginService.get().haveLogined()) {
//                    selectOffice();
//                } else {
//                    DialogUtil.showHint("请先登录");
////                    redirectToLogin();
//                }
//            }
//        });
//        toolbar.addView(addressView);

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide soft input
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        EventBus.getDefault().register(this);

        setupGetui();

        mBeepManager = new AlertBeepManager(this);


        if (!BizConfig.RELEASE) {
            DialogUtil.showHint("您正在使用的是测试版本，如需切换到正式版本请联系服务商。");
        }

        initMenus();

        loadOffices();

        ValidateManager.get().batchValidate();

        AlarmManagerHelper.registerBuglyUpgrade(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        hideSystemUI();

//        refreshToolbar();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBeepManager.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menuLogin = menu.findItem(R.id.action_sign_in);
//        menuLogout = menu.findItem(R.id.action_sign_out);
//        MenuItemCompat.setActionView(settings, R.layout.view_corner_button);
//        final Button btnSettings = (Button) settings.getActionView().findViewById(R.id.corner_button);
//        btnSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UIHelper.redirectToActivity(UserActivity.this, SettingsActivity.class);
//            }
//        });

//        refreshToolbar();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ResultCode.ARC_NATIVE_SIGNIN: {
                if (resultCode == Activity.RESULT_OK) {
                    DialogUtil.showHint("登录成功");
                    loadOffices();

                    configMenuOptions();
                    //注册到消息桥
                    IMClient.getInstance().registerBridge();

//                    Beta.checkUpgrade(false, false);

                    ValidateManager.get().stepValidate(ValidateManager.STEP_REGISTER_PLAT);
                }
            }
            break;
            case ARCode.ARC_OFFICE_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    Office office = (Office) data.getSerializableExtra("office");
                    DataCacheHelper.getInstance().setCurrentOffice(office);
                    if (office != null) {
                        addressView.setText(office.getValue());
                    } else {
                        addressView.setText("请选择网点");
                    }
                }
            }
            break;
            case ARCode.ARC_MY: {
                if (!MfhLoginService.get().haveLogined()) {
                    redirectToLogin();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", true, false);
        isWaitForExit = true;
        DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
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

        menuRecyclerView.addItemDecoration(new DividerGridItemDecoration(this));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

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

    /**
     * 配置菜单
     */
    private void configMenuOptions() {
        MfhUserManager.getInstance().updateModules();

        List<HomeMenu> menus = new ArrayList<>();

        //门店：门店管理
        if (MfhUserManager.getInstance().containsModule(Priv.FUNC_SUPM_MANAGER)) {
            ZLogger.d("当前登录用户具有门店管理能力");
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_CASHIER,
                    "收银", R.mipmap.ic_cashier));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_GOODS,
                    "商品", R.mipmap.ic_goods));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_STORE_IN,
                    "商品建档", R.mipmap.ic_goods_storein));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_SENDORDER_NEW,
                    "订货", R.mipmap.ic_order_goods));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_RECVORDER_NEW,
                    "收货", R.mipmap.ic_receive_goods));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_RECVORDER_CONVERT,
                    "转换收货", R.mipmap.ic_invrecvorder_convert));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER,
                    "退货", R.mipmap.ic_return_goods));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_OUT,
                    "出库", R.mipmap.ic_stock_out));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_IN,
                    "入库", R.mipmap.ic_stock_in));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_INV_CONVERT,
                    "库存转换", R.mipmap.ic_inv_convert));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_QUERY_BILL,
                    "单据查询", R.mipmap.ic_query_bill));

            menus.add(new HomeMenu(HomeMenu.OPTION_ID_PRINT_TAGS,
                    "价签打印", R.mipmap.ic_print_tags));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_BIND_GOODS_2_TAGS,
                    "电子价签", R.mipmap.ic_bind_tags));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_WHOLESALER_GOODS_SHELVES,
                    "货架号", R.mipmap.ic_shelves));

            menus.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_LOSSORDER,
                    "报损", R.mipmap.ic_report_loss));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_INVLOSSORDER_STOCKTAKE,
                    "报损盘点", R.mipmap.ic_invlossorder_stock));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_INVCHECKORDER_STOCKTAKE,
                    "盘点", R.mipmap.ic_stocktake));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_PICKUP_ORDER,
                    "自提定安", R.mipmap.ic_package));
        }
        //买手：拣货
        if (MfhUserManager.getInstance().containsModule(Priv.FUNC_SUPPORT_BUY)) {
            ZLogger.d("当前登录用户具有买手能力");
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_BUY_SCORDER,
                    "买手订单", R.mipmap.ic_buy_scorder));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_BUY_PREPARE,
                    "组货", R.mipmap.ic_buy_prepare));
        }
        //骑手：妥投&揽件
        if (MfhUserManager.getInstance().containsModule(Priv.FUNC_SUPPORT_STOCK)) {
            ZLogger.d("当前登录用户具有骑手能力");
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_INSTOCK_SCORDER,
                    "骑手订单", R.mipmap.ic_instock_scorder));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_EMBRACE,
                    "揽件", R.mipmap.ic_lanjian));
            menus.add(new HomeMenu(HomeMenu.OPTION_ID_TUOTOU,
                    "妥投", R.mipmap.ic_tuotou));
        }
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_ONLINE_ORDER,
//                "线上订单", R.mipmap.ic_online_order));

        menuAdapter.setEntityList(menus);

        int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.ORDER_TRANS_NOTIFY);
        ZLogger.df("买手抢单未读消息个数为：" + count);
        menuAdapter.setBadgeNumber(HomeMenu.OPTION_ID_BUY_PREPARE, count);
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

        if (id.compareTo(HomeMenu.OPTION_ID_GOODS) == 0) {
            ActivityRoute.redirect2Goods(MainActivity.this);
        } else if (id.compareTo(HomeMenu.OPTION_ID_STORE_IN) == 0) {
            ActivityRoute.redirect2StoreIn(MainActivity.this, StoreType.SUPERMARKET);
        } else if (id.compareTo(HomeMenu.OPTION_ID_INVCHECKORDER_STOCKTAKE) == 0) {
            Office office = DataCacheHelper.getInstance().getCurrentOffice();
            if (office == null) {
                DialogUtil.showHint("请先选择网点");
                return;
            }

            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_CHECKORDER_STOCKTAKE);
        } else if (id.compareTo(HomeMenu.OPTION_ID_RECVORDER_NEW) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_RECVORDER_NEW);
        } else if (id.compareTo(HomeMenu.OPTION_ID_RECVORDER_CONVERT) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_RECVORDER_CONVERT);
//            ConvertRecvActivity.actionStart(MainActivity.this, null);
        }else if (id.compareTo(HomeMenu.OPTION_ID_BIND_GOODS_2_TAGS) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_BIND_GOODS_2_TAGS);
        } else if (id.compareTo(HomeMenu.OPTION_ID_CREATE_INV_LOSSORDER) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_LOSSORDER_NEW);
        } else if (id.compareTo(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_CREATE_INV_RETURNORDER);
        } else if (id.compareTo(HomeMenu.OPTION_ID_INV_CONVERT) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_CONVERT);
        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_IN) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INVIO_IN);
        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_OUT) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INVIO_OUT);
        } else if (id.compareTo(HomeMenu.OPTION_ID_PRINT_TAGS) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_PRINT_PRICETAGS);
        } else if (id.compareTo(HomeMenu.OPTION_ID_CASHIER) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_CASHIER);
        } else if (id.compareTo(HomeMenu.OPTION_ID_BUY_PREPARE) == 0) {
            EmbMsgService.getInstance().setAllRead(IMBizType.ORDER_TRANS_NOTIFY);
            menuAdapter.setBadgeNumber(HomeMenu.OPTION_ID_BUY_PREPARE, 0);

            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_BUY_PREAPARE);
        } else if (id.compareTo(HomeMenu.OPTION_ID_BUY_SCORDER) == 0) {
            EmbMsgService.getInstance().setAllRead(IMBizType.ORDER_TRANS_NOTIFY);
            menuAdapter.setBadgeNumber(HomeMenu.OPTION_ID_BUY_PREPARE, 0);

            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_BUY_SCORDER);
        } else if (id.compareTo(HomeMenu.OPTION_ID_INSTOCK_SCORDER) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INSTOCK_SCORDER);
        } else if (id.compareTo(HomeMenu.OPTION_ID_TUOTOU) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_RIDER_INSTOCK);
        } else if (id.compareTo(HomeMenu.OPTION_ID_EMBRACE) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_RIDER_SEND);
        }  else if (id.compareTo(HomeMenu.OPTION_ID_INVLOSSORDER_STOCKTAKE) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_LOSSORDER_LIST);
        }  else if (id.compareTo(HomeMenu.OPTION_ID_SENDORDER_NEW) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_SENDORDER_NEW);
        }  else if (id.compareTo(HomeMenu.OPTION_ID_PICKUP_ORDER) == 0) {
            ActivityRoute.redirect2Primary(MainActivity.this, PrimaryActivity.FT_INV_PICKUP_ORDER);
        } else {
            DialogUtil.showHint(R.string.coming_soon);
        }
    }

    /**
     * 加载网点
     */
    private void loadOffices() {
        Office office = null;
        List<Office> offices = MfhLoginService.get().getOffices();
        if (offices != null && offices.size() > 0) {
            for (Office office1 : offices) {
                ZLogger.d(String.format("%d,%d", office1.getCode(), MfhLoginService.get().getCurOfficeId()));
                if (ObjectsCompact.equals(office1.getCode(), MfhLoginService.get().getCurOfficeId())) {
                    office = office1;
                    break;
                }
            }

            if (office == null) {
                office = offices.get(0);
            }
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
    @OnClick(R.id.address_view)
    public void selectOffice() {
        if (!MfhLoginService.get().haveLogined()) {
            DialogUtil.showHint("请先登录");
        }
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_OFFICE_LIST);
        extras.putInt(CompanyListFragment.EXTRA_KEY_ABILITY_ITEM, AbilityItem.TENANT);
        Intent intent = new Intent(this, PrimaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_OFFICE_LIST);
    }


    /**
     * 跳转至登录页面
     */
    private void redirectToLogin() {
//        refreshToolbar();
        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ResultCode.ARC_NATIVE_SIGNIN);

//        LoginActivity.actionStart(MainActivity.this, null);
//        finish();
    }

    /**
     * 验证
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ValidateManager.ValidateManagerEvent event) {
        int eventId = event.getEventId();
//        Bundle args = event.getArgs();

        ZLogger.d(String.format("ValidateManagerEvent(%d)", eventId));
        switch (eventId) {
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_START: {
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_RETRYLOGIN_SUCCEED: {
                loadOffices();
//                Beta.checkUpgrade(false, false);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_LOGIN: {
                redirectToLogin();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_PLAT_NOT_REGISTER: {
                DialogManager.getInstance().registerPos(MainActivity.this);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED: {
//                Beta.checkUpgrade(false, false);

                configMenuOptions();
                DataDownloadManager.get().launcherSync();
            }
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AffairEvent event) {
        int eventId = event.getAffairId();
        Bundle bundle = event.getArgs();
        ZLogger.d(String.format("AffairEvent(%d)", eventId));
        if (eventId == AffairEvent.EVENT_ID_APPEND_UNREAD_SKU) {
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.TENANT_SKU_UPDATE);
            ZLogger.df("SKU更新未读消息个数为：" + count);
            if (count > 1) {
                DataDownloadManager.get().syncProducts();
            }
        } else if (eventId == AffairEvent.EVENT_ID_BUYER_PREPAREABLE) {
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.ORDER_TRANS_NOTIFY);
            ZLogger.df("买手抢单未读消息个数为：" + count);
            menuAdapter.setBadgeNumber(HomeMenu.OPTION_ID_BUY_PREPARE, count);
            mBeepManager.playBeepSoundAndVibrate();
        }
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataDownloadManager.DataDownloadEvent event) {
        ZLogger.d(String.format("DataDownloadManager(%d)", event.getEventId()));
        if (event.getEventId() == DataDownloadManager.DataDownloadEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            hideProgressDialog();
            configMenuOptions();
//            btnSync.stopSync();
            //同步数据结束后开始同步订单
            DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
        }
    }

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

    /**
     * 设置个推
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

        String cid = PushManager.getInstance().getClientid(AppContext.getAppContext());
        ZLogger.df("当前应用的cid = " + cid);
    }
}

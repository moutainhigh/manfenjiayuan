package com.mfh.enjoycity.ui.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.AppHelper;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.SubdisBean;
import com.mfh.enjoycity.database.AnonymousAddressEntity;
import com.mfh.enjoycity.database.AnonymousAddressService;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.events.HomeSubdisEvent;
import com.mfh.enjoycity.service.BackService;
import com.mfh.enjoycity.service.MfhUserService;
import com.mfh.enjoycity.ui.AddAddressActivity;
import com.mfh.enjoycity.ui.dialog.SelectAddressDialog;
import com.mfh.enjoycity.ui.fragments.HomeFragment;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.enjoycity.utils.UIHelper;
import com.mfh.enjoycity.view.FloatParterView;
import com.mfh.enjoycity.view.FloatShopcartView;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.core.location.MfLocationManagerProxy;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.network.URLHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_KEY_SELECT_SHOP_ID = "EXTRA_KEY_SELECT_SHOP_ID";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab_shopcart)
    FloatShopcartView fabShopcartView;
    @Bind(R.id.fab_mfparter)
    FloatParterView fabParterView;
    private NaviAddressView addressView;

    private HomeFragment homeFragment;

    private BroadcastReceiver receiver;

    private boolean bInitialized;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, MainActivity.class);
        if (extras != null){
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

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
                if (id == R.id.action_me) {
                    if (MfhLoginService.get().haveLogined()) {
                        UserActivity.actionStart(MainActivity.this, -1);
                    } else {
                        redirectToLogin();
                    }
                } else if (id == R.id.action_order) {
                    HybridActivity.actionStart(MainActivity.this, H5Api.URL_ME_PACKAGES, false, 0);
                }
                return true;
            }
        });
        addressView = new NaviAddressView(this);
        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSubdis();
            }
        });
        toolbar.addView(addressView);

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_main);
    }

    @Override
    protected boolean isMfLocationEnable() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent(getIntent());

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
//        MobclickAgent.onProfileSignIn(MfhLoginService.get().getCurrentGuId());

        bInitialized = false;

        registerReceiver();

        //Android 5.1.1 报错：
//        Caused by: java.lang.IllegalArgumentException: Service Intent must be explicit: Intent { act=com.mfh.owner.service.BackService }
//        startService(new Intent("com.mfh.owner.service.BackService"));
        Intent intent = new Intent(this, BackService.class);
        startService(intent);

        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment)
                .show(homeFragment)
                .commit();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id =su item.getItemId();
//        if (id == R.id.action_me) {
//            startActivity(new Intent(this, SettingsActivity.class));
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        //android.app.IntentReceiverLeaked: Activity com.mfh.enjoycity.ui.activity.UserActivity has leaked IntentReceiver com.mfh.enjoycity.ui.activity.UserActivity$3@443b09b8 that was originally registered here. Are you missing a call to unregisterReceiver()?
        if (receiver != null) {
            unregisterReceiver(receiver);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.ACTIVITY_REQUEST_LOGIN_H5) {
            if (resultCode == Activity.RESULT_OK) {
                UserActivity.actionStart(MainActivity.this, -1);
            } else {

            }
        } else if (requestCode == Constants.ACTIVITY_REQUEST_CODE_ADD_COMMUNITY) {
            if (resultCode == Activity.RESULT_OK) {
                refreshSubdis();
            } else {
//                if(intent != null && intent.getBooleanExtra(Constants.EXTRA_KEY_NEED_LOGIN, false)){
//                    redirectToLogin();
//                }
            }
        } else if (requestCode == Constants.ACTIVITY_REQUEST_CODE_SEARCH_COMMUNITY) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    SubdisBean bean = (SubdisBean) intent.getSerializableExtra(Constants.INTENT_KEY_ADDRESS_DATA);
                    ZLogger.d(bean.toString());

//                    if (MfhLoginService.get().haveLogined()){
////                        ReceiveAddressService dbService = ReceiveAddressService.get();
////                        dbService.saveOrUpdate(bean);
////
//                        ShopcartHelper.getInstance().refreshMemberOrderAddr(String.valueOf(bean.getId()));
//                    }else
                    {
                        AnonymousAddressService dbService = AnonymousAddressService.get();
                        dbService.saveOrUpdate(bean);

                        ShopcartHelper.getInstance().refreshAnonymousOrderAddr(String.valueOf(bean.getId()));
                    }

                    refreshSubdis();
                }
            }
        } else if (requestCode == Constants.ACTIVITY_REQUEST_CODE_SHOPCART) {
            refreshFloatShopcartView();
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();//接收者只有在activity才起作用。
        filter.addAction(Constants.ACTION_TOGGLE_FLOAT);
        filter.addAction(Constants.BROADCAST_ACTION_SHOPCART_REFRESH);
        filter.addAction(Constants.BROADCAST_ACTION_PARTER_REFRESH);
        filter.addAction(Constants.BROADCAST_ACTION_NOTIFY_EVALUATE_ORDER);
        filter.addAction(Constants.BROADCAST_ACTION_NOTIFY_TAKE_ORDER);
        filter.addAction(Constants.BROADCAST_ACTION_USER_RECV_ADDR_REFRESH);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_TOGGLE_FLOAT: {
                        boolean enabled = intent.getBooleanExtra(Constants.EXTRA_NAME_FLOAT_ENABLED, true);
                        toggleFloatView(enabled);
                    }
                    break;
                    case Constants.BROADCAST_ACTION_SHOPCART_REFRESH: {
                        refreshFloatShopcartView();
                    }
                    break;
                    case Constants.BROADCAST_ACTION_PARTER_REFRESH: {
                        refreshFloatParterView();
                    }
                    break;
                    case Constants.BROADCAST_ACTION_NOTIFY_TAKE_ORDER: {
                        String content = intent.getStringExtra("content");
                        String orderIds = intent.getStringExtra("orderIds");

                        ZLogger.d(String.format("content=%s,orderIds=%s", content, orderIds));
                        UIHelper.showConfirmOrderDialog(MainActivity.this, content, orderIds);
                    }
                    break;
                    case Constants.BROADCAST_ACTION_NOTIFY_EVALUATE_ORDER: {
                        String content = intent.getStringExtra("content");
                        String orderIds = intent.getStringExtra("orderIds");

                        ZLogger.d(String.format("content=%s,orderIds=%s", content, orderIds));
                        UIHelper.showEvaluateDialog(MainActivity.this, content, orderIds);
                    }
                    break;
//                    case Constants.BROADCAST_ACTION_USER_RECV_ADDR_REFRESH: {
//                        if (MfhLoginService.get().haveLogined()) {
//                            ReceiveAddressService dbService = ReceiveAddressService.get();
//                            List<ReceiveAddressEntity> entityList = dbService.queryAll(new PageInfo(1, 100));
//                            if (entityList != null && entityList.size() > 0) {
//                                if (!bInitialized) {
//                                    ReceiveAddressEntity entity = entityList.get(0);
//
//                                    ShopcartHelper.getInstance().refreshOrderAddr(entity.getId());
//                                    refreshSubdis();
//                                } else {
//                                    reloadSubdisData();
//                                }
//                            } else {
//                                selectSubdis();
//                            }
//                        }
//                    }
//                    break;
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private void toggleFloatView(boolean enabled){
        if (enabled) {
            fabShopcartView.setVisibility(View.VISIBLE);
            fabShopcartView.animate().translationY(0)
                    .setInterpolator(new AccelerateInterpolator(2)).start();

            refreshFloatParterView();
        } else {
//                            fabShopcartView.setVisibility(View.GONE);
            fabShopcartView.animate().translationY(fabShopcartView.getHeight() + DensityUtil.dip2px(MainActivity.this, 16))
                    .setInterpolator(new AccelerateInterpolator(2)).start();

            fabParterView.setVisibility(View.GONE);
            fabParterView.animate().translationY(fabShopcartView.getHeight() + DensityUtil.dip2px(MainActivity.this, 16))
                    .setInterpolator(new AccelerateInterpolator(2)).start();
        }
    }

    /**
     * reload data
     */
    private void reload() {
        addressView.setSelected(true);

        EnjoycityApiProxy.queryMfhParterInService();

        refreshFloatParterView();
        refreshFloatShopcartView();

        if (MfhLoginService.get().haveLogined()) {
            ReceiveAddressService dbService = ReceiveAddressService.get();
            List<ReceiveAddressEntity> entityList = dbService.queryAll(new PageInfo(1, 100));
            if (entityList != null && entityList.size() > 0) {
                if (!bInitialized) {
                    ReceiveAddressEntity entity = entityList.get(0);

                    ShopcartHelper.getInstance().refreshMemberOrderAddr(entity.getId());
                    refreshSubdis();
                } else {
                    reloadSubdisData();
                }
            } else {
                selectSubdis();
            }
            MfhUserService.getInstance().loadReceiveAddr();
        } else {
            AnonymousAddressService dbService = ServiceFactory.getService(AnonymousAddressService.class.getName());
            List<AnonymousAddressEntity> entityList = dbService.queryAll(new PageInfo(1, 100));
            if (entityList != null && entityList.size() > 0) {
                if (!bInitialized) {
                    AnonymousAddressEntity entity = entityList.get(0);

                    ShopcartHelper.getInstance().refreshAnonymousOrderAddr(entity.getId());
                    refreshSubdis();
                } else {
                    reloadSubdisData();
                }
            } else {
                selectSubdis();
            }
        }

        bInitialized = true;
    }

    /**
     * 刷新小区数据
     */
    private void reloadSubdisData() {
        if (homeFragment != null) {
            Long saveSubdisId = ShopcartHelper.getInstance().getSubdisId();//
            if (saveSubdisId == null){
                selectSubdis();
            }
            else{
                Long curSubdisId = homeFragment.getCurrentSubdisId();
                //当前收货地址和显示的小区不一致，自动加载购物车中
                if (curSubdisId == null || curSubdisId.compareTo(saveSubdisId) != 0) {
                    refreshSubdis();
                }
            }
        }
    }

    /**
     * 刷新当前选中小区
     */
    private void refreshSubdis() {
        addressView.setText(String.format("%s %s", ShopcartHelper.getInstance().getSubName(),
                ShopcartHelper.getInstance().getAddrName()));
        addressView.setSelected(true);

        //获取小区周边店铺信息
        if (homeFragment != null) {
            homeFragment.reload(ShopcartHelper.getInstance().getSubdisId());
        }
    }

    /**
     * 跳转到登录页面
     */
    private void redirectToLogin() {
        AppHelper.resetMemberAccountData();

        //TODO,判断当前页是否需要切换登录页面
        String authUrl = URLHelper.append(H5Api.URL_AUTH_INDEX, "redirect=" + H5Api.URL_NATIVIE_REDIRECT_AUTH);
        startActivityForResult(HybridActivity.loginIntent(MainActivity.this, authUrl), Constants.ACTIVITY_REQUEST_LOGIN_H5);
//        canRedirectToLogin = true;
    }

    private SelectAddressDialog dialog = null;

    /**
     * 显示选择地址对话框
     */
    private void selectSubdis() {
        if (dialog == null){
            dialog = new SelectAddressDialog(MainActivity.this);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        }
//        dialog.setTitle(R.string.dialog_title_share_to);

        SelectAddressDialog.DialogType dialogType = SelectAddressDialog.DialogType.LOCATION_ANONYMOUS;
        if (MfhLoginService.get().haveLogined()) {
            dialogType = SelectAddressDialog.DialogType.LOCATION_LOGIN;
        }
        dialog.init(dialogType, new SelectAddressDialog.OnResponseCallback() {
            @Override
            public void onRetryLocation() {
                initMfLocation();
//                H5ProductDetailActivity.actionStart(context, 0);
                findArroundSubdist();
            }

            @Override
            public void onSearch() {
                Intent intent = new Intent(MainActivity.this, SearchCommunityActivity.class);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SEARCH_COMMUNITY);
            }

            @Override
            public void onAdd() {
                startActivityForResult(new Intent(MainActivity.this, AddAddressActivity.class),
                        Constants.ACTIVITY_REQUEST_CODE_ADD_COMMUNITY);
            }

            @Override
            public void onSelectAddress() {
                refreshSubdis();
            }
        });

        if (!dialog.isShowing()){
            dialog.show();
        }
    }


    /**
     * 处理传进来的intent
     */
    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Long shopId = intent.getLongExtra(EXTRA_KEY_SELECT_SHOP_ID, 0);
        if (homeFragment != null) {
            homeFragment.refresh(shopId);
        }
    }

    @OnClick(R.id.fab_shopcart)
    public void redirectToCart() {
        toggleFloatView(false);

        Intent intent = new Intent(this, ShoppingCartActivity.class);
        intent.putExtra(ShoppingCartActivity.EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_FLOW);
        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SHOPCART);
//        ShoppingCartActivity.actionStart(MainActivity.this, ANIM_TYPE_NEW_FLOW);
    }

    @OnClick(R.id.fab_mfparter)
    public void redirectToOrder() {
        toggleFloatView(false);
        String url = URLHelper.append(H5Api.URL_ME_ORDER_MALL, null);
        HybridActivity.actionStart(MainActivity.this, url, true, false, 0);
    }


    private void findArroundSubdist() {
        DialogUtil.showHint("正在为您重新定位...");

        //回调
        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<SubdisBean>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<SubdisBean> rs) {//此处在主线程中执行。
                        saveQuerySubids(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                        super.processFailure(t, errMsg);
                        Message message = new Message();
                        message.what = MSG_ERROR;
                        uiHandler.sendMessage(message);
                    }
                }
                , SubdisBean.class
                , MfhApplication.getAppContext());

        //TODO
        EnjoycityApiProxy.findArroundSubdist(MfLocationManagerProxy.getLastLongitude(MainActivity.this),
                MfLocationManagerProxy.getLastLatitude(MainActivity.this), responseCallback);
    }

    private void saveQuerySubids(RspQueryResult<SubdisBean> rs) {
        try {
//            //保存下来
            int retSize = rs.getReturnNum();
            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));
//
            if (retSize < 1) {
//                //TODO,未搜索到结果
                Message message = new Message();
                message.what = MSG_NONE;
                uiHandler.sendMessage(message);
                return;
            }//

            List<SubdisBean> result = new ArrayList<>();
            for (int i = 0; i < retSize; i++) {
                result.add(rs.getRowEntity(i));
            }
            Message message = new Message();
            message.what = MSG_SUCCESS;
            message.obj = result;
            uiHandler.sendMessage(message);
        } catch (Throwable ex) {
            ZLogger.e(ex.toString());
            Message message = new Message();
            message.what = MSG_ERROR;
            uiHandler.sendMessage(message);
        }
    }

    private final static int MSG_NONE = 0;
    private final static int MSG_ERROR = 1;
    private final static int MSG_SUCCESS = 2;
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NONE:
                    DialogUtil.showHint("无结果，请重新再试一次");
//                    loadingTextView.hide();
                    break;
                case MSG_ERROR:
//                    loadingTextView.hide();
                    break;
                case MSG_SUCCESS:
//                    loadingTextView.hide();
                    List<SubdisBean> result = (List<SubdisBean>) msg.obj;
                    AnonymousAddressService dbService = ServiceFactory.getService(AnonymousAddressService.class.getName());
                    dbService.clear();
                    for (SubdisBean bean : result) {
                        dbService.saveOrUpdate(bean);
                    }
                    //TODO
                    selectSubdis();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 更新购物车信息
     */
    private void refreshFloatShopcartView() {
        List<ShoppingCartEntity> entityList = ShoppingCartService.get().queryAll();
        if (entityList != null && entityList.size() > 0) {
            fabShopcartView.setNumber(entityList.size());
//            fabShopcartView.setVisibility(View.VISIBLE);
        } else {
            fabShopcartView.setNumber(0);
//            fabShopcartView.setVisibility(View.GONE);
        }
        fabShopcartView.animate().translationY(0)
                .setInterpolator(new AccelerateInterpolator(2)).start();
    }

    /**
     * 更新小伙伴信息
     */
    private void refreshFloatParterView() {
        int count = SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ).getInt(Constants.PREF_KEY_PARTER_COUNT, 0);
        ZLogger.d("refreshFloatParterView count: " + count);

        if (count > 0) {
            fabParterView.setTip(String.format("%d位小伙伴\n正在为您服务", count));
            fabParterView.setVisibility(View.VISIBLE);
            fabParterView.animate().translationY(0)
                    .setInterpolator(new AccelerateInterpolator(2)).start();
        } else {
            fabParterView.setVisibility(View.GONE);
        }
    }

    /**
     * */
    public void onEventMainThread(HomeSubdisEvent event) {
        Intent intent = new Intent(this, SearchCommunityActivity.class);
        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SEARCH_COMMUNITY);
    }

//    private void validSession() {
//        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
//                NetProcessor.Processor<String>>(
//                new NetProcessor.Processor<String>() {
//                    @Override
//                    public void processResult(IResponseData rspData) {
////
//                    }
//
//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        MLog.d("validSession.processFailure: " + errMsg);
//
//                        //已过期，跳转到登录页面
////                        animProgress.setVisibility(View.GONE);
//                        com.mfh.comna.api.helper.UIHelper.redirectToActivity(MainActivity.this, LoginActivity.class);
//                        finish();
//                    }
//                }
//                , String.class
//                , BizApplication.getAppContext()) {
//        };
//
//        EnjoycityApiProxy.validSession(responseCallback);
//    }

}

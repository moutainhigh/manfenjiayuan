package com.mfh.litecashier.ui.fragment.cashier;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.vector_user.bean.Human;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.entity.UserMixInfo;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.adv.AdvLocalPic;
import com.mfh.framework.uikit.adv.AdvLocalPicAdapter;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.BadgeViewButton;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.AdvertiseBean;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.logic.CommonlyGoodsService;
import com.mfh.litecashier.database.logic.PosProductSkuService;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.event.CashierAffairEvent;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.CloudSyncManager;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.service.EslSyncManager2;
import com.mfh.litecashier.service.OrderSyncManager2;
import com.mfh.litecashier.service.ValidateManager;
import com.mfh.litecashier.ui.activity.AdministratorActivity;
import com.mfh.litecashier.ui.activity.ServiceActivity;
import com.mfh.litecashier.ui.activity.SignInActivity;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.adapter.AdvertisementPagerAdapter;
import com.mfh.litecashier.ui.adapter.CashierServiceMenuAdapter;
import com.mfh.litecashier.ui.adapter.GrouponGridAdapter;
import com.mfh.litecashier.ui.dialog.AdministratorSigninDialog;
import com.mfh.litecashier.ui.dialog.ExpressDialog;
import com.mfh.litecashier.ui.dialog.InitCardByStepDialog;
import com.mfh.litecashier.ui.dialog.LaundryDialog;
import com.mfh.litecashier.ui.dialog.QueryBalanceDialog;
import com.mfh.litecashier.ui.dialog.ReceiveGoodsDialog;
import com.mfh.litecashier.ui.dialog.RegisterUserDialog;
import com.mfh.litecashier.ui.dialog.ReturnGoodsDialog;
import com.mfh.litecashier.ui.dialog.ValidatePhonenumberDialog;
import com.mfh.litecashier.ui.fragment.inventory.CreateInventoryTransOrderFragment;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.FreshShopcartHelper;
import com.mfh.litecashier.utils.PurchaseShopcartHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;
import com.tencent.bugly.beta.Beta;

import org.century.GreenTagsApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 服务台
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CashierServiceFragment extends BaseFragment {

    @Bind(R.id.tv_service_title)
    TextView tvServiceTitle;
    @Bind(R.id.syncProgressBar)
    ProgressBar syncProgressBar;
    @Bind(R.id.ib_shopcart)
    BadgeViewButton shopcartBadgeView;
    @Bind(R.id.viewpager_adv)
    AdvertisementViewPager advertiseViewPager;
    private AdvertisementPagerAdapter advertisePagerAdapter;
    private AdvLocalPicAdapter mPictureAdvPagerAdapter;
    @Bind(R.id.category_list)
    RecyclerView menuRecyclerView;
    private CashierServiceMenuAdapter menuAdapter;
    @Bind(R.id.groupon_list)
    RecyclerView grouponRecyclerView;
    private GrouponGridAdapter grouponAdapter;

    private ValidatePhonenumberDialog mValidatePhonenumberDialog = null;
    private QueryBalanceDialog mQueryBalanceDialog = null;
    private RegisterUserDialog mRegisterUserDialog = null;
    private InitCardByStepDialog initCardDialog = null;
    private ReturnGoodsDialog returnGoodsDialog = null;
    private ExpressDialog expressDialog = null;
    private LaundryDialog laundryDialog = null;
    private ReceiveGoodsDialog receiveGoodsDialog = null;

    private CashierPresenter cashierPresenter;

    public static CashierServiceFragment newInstance(Bundle args) {
        CashierServiceFragment fragment = new CashierServiceFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier_service_behavior;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        cashierPresenter = new CashierPresenter(null);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            initShortcutRecyclerView();
            initGrouponRecyclerView();

            //TODO,加载广告数据，然后再填充广告
            List<AdvertiseBean> advList = new ArrayList<>();
            //multi
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_MULTI, "http://resource.manfenjiayuan.cn/product/thumbnail_1294.jpg", "衣服", "秋冬热卖"));
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_MULTI, "http://chunchunimage.b0.upaiyun.com/product/3655.JPG!small", "可口可乐苏州分公司", "年终特卖会，330ml买3箱送一箱，还送50元代金券，还不马上购买!"));
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_MULTI, "http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small", "面包", "新鲜刚出炉的面包，赶快来买!"));
            //simple
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_SIMPLE, "http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small", "面包", "新鲜刚出炉的面包，赶快来买!"));
            advertisePagerAdapter = new AdvertisementPagerAdapter(getActivity(),
                    advList, null);

            List<AdvLocalPic> localAdvList = new ArrayList<>();
            localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_beef));
            localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_duanwu));
            localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_apple));
            localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_fresh));
            mPictureAdvPagerAdapter = new AdvLocalPicAdapter(getActivity(), localAdvList, null);
            advertiseViewPager.setAdapter(mPictureAdvPagerAdapter);
            //TODO,定时切换(每隔5秒切换一次)
            advertiseViewPager.startSlide(5 * 1000);

            refreshFrontCategory();
            reload(true);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int count = SharedPreferencesHelper.getInt(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, 0);
        shopcartBadgeView.setBadgeNumber(count);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private long clickDeveloperModeTick = 0;
    private int clickDeveloperModeTimes = 0;
    @OnClick(R.id.tv_service_title)
    public void enterDeveloperMode(){
        if ((System.currentTimeMillis() - clickDeveloperModeTick) > 3000) {
            clickDeveloperModeTimes = 0;
        } else {
            clickDeveloperModeTimes++;
        }
        clickDeveloperModeTick = System.currentTimeMillis();

        if (clickDeveloperModeTimes > 2 && clickDeveloperModeTimes < 7){
            DialogUtil.showHint(String.format("连续按8次可以获得开发者送出的神秘礼物，还差%d次！", 8 - clickDeveloperModeTimes));
        }
        else if (clickDeveloperModeTimes == 8){

            clickDeveloperModeTimes = 0;
            // TODO: 6/6/16

            DialogUtil.showHint("恭喜你，你已经打获得了开发者送给你的神秘礼物！");
            UIHelper.startActivity(getContext(), AdministratorActivity.class);
        }

    }

    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
        ZLogger.d(String.format("DataSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_REFRESH_FRONT_CATEGORYINFO) {
            refreshFrontCategory();
        } else if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            hideProgressDialog();
            syncProgressBar.setVisibility(View.INVISIBLE);
            //同步数据结束后开始同步订单
            OrderSyncManager2.get().sync();

            CloudSyncManager.get().importFromChainSku();
            EslSyncManager2.getInstance().sync();
            SMScaleSyncManager2.getInstance().sync();
        }
    }

    public void onEventMainThread(AffairEvent event) {
        ZLogger.d(String.format("AffairEvent(%d)", event.getAffairId()));
        //有新订单
        if (event.getAffairId() == AffairEvent.EVENT_ID_SYNC_DATA_INITIALIZE) {
            if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            PosProductService.get().clear();
            PosProductSkuService.get().clear();
            SharedPreferencesHelper.setSyncProductsCursor("");
            SharedPreferencesHelper.setPosSkuLastUpdate("");
            SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
            SharedPreferencesManager.set(SMScaleSyncManager2.PREF_SMSCALE,
                    SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR, "");

//            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在同步数据...", true, false);
            syncProgressBar.setVisibility(View.VISIBLE);
            DataSyncManager.get().sync();
        } else if (event.getAffairId() == AffairEvent.EVENT_ID_SYNC_DATA_START) {
//            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在同步数据...", true, false);
            syncProgressBar.setVisibility(View.VISIBLE);
            DataSyncManager.get().sync();

//            NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>() {
//                @Override
//                protected void processOperResult(EmbMsg result) {
////                doAfterSendSuccess(result);
//                    ZLogger.d("测试更新SKU商品");
//                }
//            };
//            EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class, getContext());
//            msgService.sendText(MfhLoginService.get().getCurrentGuId(),
//                    MfhLoginService.get().getCurrentGuId(),
//                    IMBizType.TENANT_SKU_UPDATE, "test update sku", processor);
        }
        else if (event.getAffairId() == AffairEvent.EVENT_ID_APPEND_UNREAD_SCHEDULE_ORDER){
            int count = SharedPreferencesHelper.getInt(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, 0);
            shopcartBadgeView.setBadgeNumber(count);
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
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_SESSION_EXPIRED: {
                retryLogin(false);
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_LOGIN: {
                redirectToLogin();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_PLAT_NOT_REGISTER: {
                DialogUtil.showHint("需要注册");
//                redirectToLogin();
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
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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
                    EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_RESET_CASHIER));
                    reload(true);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化前台类目
     */
    private void initShortcutRecyclerView() {
//        GridLayoutManager mRLayoutManager = new GridLayoutManager(getActivity(), 8);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
//        mRLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        menuRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
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
                if (entity == null) {
                    return;
                }
                if (entity.getType() == 0) {
                    responsePrivateFunction(entity.getId());
                } else {
                    Bundle args = new Bundle();
                    args.putString("title", entity.getNameCn());
                    args.putLong("categoryId", entity.getId());
                    EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_FRONT_CATEGORY, args));
                }

            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
//        refreshFrontCategory();
    }

    private void initGrouponRecyclerView() {
        StaggeredGridLayoutManager layoutManager
                = new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL);

//        grouponRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        grouponRecyclerView.setLayoutManager(layoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        grouponRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        grouponRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.5f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        grouponAdapter = new GrouponGridAdapter(CashierApp.getAppContext(), null);
        grouponAdapter.setOnAdapterLitener(new GrouponGridAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                CashierFunctional entity = grouponAdapter.getEntity(position);
                if (entity == null) {
                    return;
                }

                Bundle extras = new Bundle();
                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FT_GROUPON_DETAIL);

                ServiceActivity.actionStart(getActivity(), extras);
            }
        });

        grouponRecyclerView.setAdapter(grouponAdapter);
    }

    /**
     * 固有功能
     */
    private void responsePrivateFunction(Long id) {
        if (id == null) {
            return;
        }

        if (id.compareTo(CashierFunctional.OPTION_ID_PACKAGE) == 0) {
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
        } else if (id.compareTo(CashierFunctional.OPTION_ID_PRIVATE) == 0) {
            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_COMMONLY));
        } else if (id.compareTo(CashierFunctional.OPTION_ID_MALL) == 0) {
            commodityCenterService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_INVENTORY_TRANS_IN) == 0) {
            Bundle extras = new Bundle();
            extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_INVENTORY_ALLOCATION_ORDER);
            extras.putInt(CreateInventoryTransOrderFragment.EK_ENTERMODE, 2);

            ServiceActivity.actionStart(getActivity(), extras);

//                    Intent intent = new Intent(getActivity(), ServiceActivity.class);
//                    intent.putExtras(extras);
//                    startActivity(intent);
        } else if (id.compareTo(CashierFunctional.OPTION_ID_INVENTORY_TRANS_OUT) == 0) {
            Bundle extras = new Bundle();
            extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_INVENTORY_ALLOCATION_ORDER);
            extras.putInt(CreateInventoryTransOrderFragment.EK_ENTERMODE, 2);

            ServiceActivity.actionStart(getActivity(), extras);

//                    Intent intent = new Intent(getActivity(), ServiceActivity.class);
//                    intent.putExtras(extras);
//                    startActivity(intent);
        } else if (id.compareTo(CashierFunctional.OPTION_ID_SYNC) == 0) {
            syncData();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_MONEYBOX) == 0) {
            EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_OPEN_MONEYBOX));
        } else if (id.compareTo(CashierFunctional.OPTION_ID_CLEAR_ORDER) == 0) {
            EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_RESET_CASHIER));
        } else {
            DialogUtil.showHint("@开发君 失踪了...");
        }
    }

    /**
     * 线上订单
     */
    @OnClick(R.id.ib_shopcart)
    public void redirectToOnlineOrder() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_ONLINE_ORDER);
        SimpleActivity.actionStart(getActivity(), extras);

//        int count = SharedPreferencesHelper.getInt(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, 0);
//        shopcartBadgeView.setBadgeNumber(count);
    }

    /**
     * 同步数据
     */
//    @OnClick(R.id.ib_sync_data)
    public void syncData() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        //设置需要更新前台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_FRONTCATEGORY_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_CUSTOM_FRONTCATEGORY_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_LAUNDRY_FRONTCATEGORY_ENABLED, true);
        //设置需要更新前台类目
        SharedPreferencesHelper.setSyncFrontCategorySubEnabled(true);
        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED, true);

        //同步数据
        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SYNC_DATA_START));
    }

    @OnClick(R.id.ib_settings)
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
            mAdministratorSigninDialog = new AdministratorSigninDialog(getActivity());
            mAdministratorSigninDialog.setCancelable(true);
            mAdministratorSigninDialog.setCanceledOnTouchOutside(true);
        }
        mAdministratorSigninDialog.init("管理员密码", new AdministratorSigninDialog.OnResponseCallback() {
            @Override
            public void onSignInSuccess() {
                UIHelper.startActivity(getContext(), AdministratorActivity.class);
// TODO: 5/19/16
//                UIHelper.startActivity(getActivity(), SettingsActivity.class);
            }

            @Override
            public void onGuestSignIn() {

            }
        });
        mAdministratorSigninDialog.show();
    }

    /**
     * 刷新前台类目
     */
    private synchronized void refreshFrontCategory() {
//        List<PosCategory> localList = new ArrayList<>();
//
//        //公共前台类目
//        String publicCateCache = ACache.get(CashierApp.getAppContext(), Constants.CACHE_NAME)
//                .getAsString(Constants.CK_PUBLIC_FRONT_CATEGORY);
//        String customCateCache = ACache.get(CashierApp.getAppContext(), Constants.CACHE_NAME)
//                .getAsString(Constants.CK_CUSTOM_FRONT_CATEGORY);
//        //私有前台类目
//        List<PosCategory> publicData = JSONArray.parseArray(publicCateCache, PosCategory.class);
//        List<PosCategory> customData = JSONArray.parseArray(customCateCache, PosCategory.class);
//        if (menuAdapter != null) {
//            menuAdapter.setEntityList(localList, publicData, customData);
//        }

        if (menuAdapter != null) {
            menuAdapter.setEntityList(cashierPresenter.getCashierFunctions());
        }
        if (grouponAdapter != null) {
            grouponAdapter.setEntityList(cashierPresenter.getGrouponList());
        }
    }

    /**
     * 取包裹
     */
    private void packageService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(R.string.toast_network_error);
//            return;
//        }

        //直接根据取货码查询
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_STOCK_DETAIL);
        ServiceActivity.actionStart(getActivity(), extras);
    }

    /**
     * 商城
     */
    private void commodityCenterService() {
        //直接根据取货码查询
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_COMMODITY_CENTER);
        ServiceActivity.actionStart(getActivity(), extras);
    }

    /**
     * 余额查询
     */
    private void queryBalance() {
        //开卡
        if (mQueryBalanceDialog == null) {
            mQueryBalanceDialog = new QueryBalanceDialog(getActivity());
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
     * 会员注册1-验证手机号
     */
    private void registerVIPStep1() {
        //开卡
        if (mValidatePhonenumberDialog == null) {
            mValidatePhonenumberDialog = new ValidatePhonenumberDialog(getActivity());
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
            mRegisterUserDialog = new RegisterUserDialog(getActivity());
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
    private void registerVIPStep3(Human human){
        if (initCardDialog == null) {
            initCardDialog = new InitCardByStepDialog(getActivity());
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
            mValidatePhonenumberDialog = new ValidatePhonenumberDialog(getActivity());
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

    private void initVipCardStep2(String phonenumber){
        if (initCardDialog == null) {
            initCardDialog = new InitCardByStepDialog(getActivity());
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
            expressDialog = new ExpressDialog(getActivity());
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
            laundryDialog = new LaundryDialog(getActivity());
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
            returnGoodsDialog = new ReturnGoodsDialog(getActivity());
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
            receiveGoodsDialog = new ReceiveGoodsDialog(getActivity());
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
     * 数据同步
     *
     * @param isSlient true:后台同步数据；false:显示进度对话框。
     */
    private void dataSync(boolean isSlient) {
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
        if (!isSlient) {
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在同步数据...", true, false);
        }
        syncProgressBar.setVisibility(View.VISIBLE);
        DataSyncManager.get().sync();
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

        dataSync(isSlient);
    }

}
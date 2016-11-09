package com.manfenjiayuan.mixicook_vip.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.widget.NaviAddressView;
import com.manfenjiayuan.business.presenter.PosRegisterPresenter;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.business.view.IPosRegisterView;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ValidateManager;
import com.manfenjiayuan.mixicook_vip.database.HomeGoodsTempService;
import com.manfenjiayuan.mixicook_vip.model.CartBrief;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.SmsSignActivity;
import com.manfenjiayuan.mixicook_vip.ui.address.AddAddressFragment;
import com.manfenjiayuan.mixicook_vip.ui.address.IReciaddrView;
import com.manfenjiayuan.mixicook_vip.ui.address.ReciaddrPresenter;
import com.manfenjiayuan.mixicook_vip.ui.hybrid.HybridFragment;
import com.manfenjiayuan.mixicook_vip.ui.mutitype.Card1;
import com.manfenjiayuan.mixicook_vip.ui.mutitype.Card10;
import com.manfenjiayuan.mixicook_vip.ui.mutitype.Card1Item;
import com.manfenjiayuan.mixicook_vip.ui.mutitype.Card2Item;
import com.manfenjiayuan.mixicook_vip.ui.mutitype.Card6;
import com.manfenjiayuan.mixicook_vip.ui.mutitype.Card9;
import com.manfenjiayuan.mixicook_vip.ui.shopcart.ShopcartFragment;
import com.manfenjiayuan.mixicook_vip.utils.AddCartAnimation;
import com.manfenjiayuan.mixicook_vip.utils.AddCartOptions;
import com.manfenjiayuan.mixicook_vip.widget.FloatView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.Constants;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.anon.sc.storeRack.CardProduct;
import com.mfh.framework.api.anon.sc.storeRack.ScStoreRackApi;
import com.mfh.framework.api.anon.sc.storeRack.StoreRack;
import com.mfh.framework.api.anon.sc.storeRack.StoreRackCard;
import com.mfh.framework.api.anon.sc.storeRack.StoreRackCardItem;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.companyInfo.CompanyInfoPresenter;
import com.mfh.framework.api.companyInfo.ICompanyInfoView;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.shoppingCart.ShoppingCartApiImpl;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.system.PermissionUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.ProgressView;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.tencent.bugly.beta.Beta;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import me.drakeet.multitype.Item;
import me.drakeet.multitype.MultiTypeAdapter;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class HomeFragment extends BaseFragment
        implements IReciaddrView, ICompanyInfoView, IScGoodsSkuView, IPosRegisterView {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.address_view)
    NaviAddressView mNaviAddressView;
    @Bind(R.id.rootview)
    CoordinatorLayout rootView;
    @Bind(R.id.bannerRackList)
    RecyclerView bannerRackRecyclerView;
    @Bind(R.id.homeRackList)
    RecyclerView homeRackRecyclerView;
    @Bind(R.id.noAddressView)
    View mNoAddressView;
    @Bind(R.id.noCompanyView)
    View mNoCompanyView;
    @Bind(R.id.tv_company_outofrange)
    TextView tvCompanyOutofRange;
//    @Bind(R.id.fab_cart)
//    FloatingActionButton fabCart;
    @Bind(R.id.float_cart)
    FloatView floatCartView;

    //当前收货地址，用来定位店铺
    private Reciaddr curAddress = null;
    private CompanyInfo curCompanyInfo = null;//当前店铺
    private ReciaddrPresenter mReciaddrPresenter;
    private CompanyInfoPresenter mCompanyInfoPresenter;
    private ScGoodsSkuPresenter mScGoodsSkuPresenter;
    private PosRegisterPresenter mPosRegisterPresenter;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        mReciaddrPresenter = new ReciaddrPresenter(this);
        mCompanyInfoPresenter = new CompanyInfoPresenter(this);
        mScGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
        mPosRegisterPresenter = new PosRegisterPresenter(this);

    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        mToolbar.setTitle("价签绑定");
//        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
//        mToolbar.setNavigationOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        getActivity().onBackPressed();
//                    }
//                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_my) {
                    redirect2My();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_home);

        initBannerRack();
        initHomeRack();

        ValidateManager.get().batchValidate();
        loadInitStep1();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE: {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bundle bundle = data.getExtras();
                        String resultText = bundle.getString("result", "");
//                Bitmap barcode =  (Bitmap)bundle.getParcelable("bitmap");//扫描截图

                        if (StringUtils.isUrl(resultText) && resultText.contains(MfhApi.DOMAIN)) {
                            DialogUtil.showHint(resultText);
                        } else {
                            DialogUtil.showHint(String.format("非法的URL： %s", resultText));
                        }
                    } catch (Exception ex) {
                        //TransactionTooLargeException
                        ZLogger.e(ex.toString());
                    }
                }
            }
            break;
            case ARCode.ARC_SIGNIN: {
                loadInitStep1();
            }
            break;
            case ARCode.ARC_ADDRESS_LIST: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    curAddress = (Reciaddr) data.getSerializableExtra("reciaddr");
                    loadInitStep2();
                }
            }
            break;
            case ARCode.ARC_ADD_ADDRESS: {
                if (resultCode == Activity.RESULT_OK) {
                    loadInitStep1();
                }
            }
            break;
            case ARCode.ARC_MY: {
                if (resultCode == Activity.RESULT_OK) {
                    if (!MfhLoginService.get().haveLogined()) {
                        ZLogger.d("退出当前登录账号，准备跳转到登录页面");
                        redirect2Login();
                    }
                }
            }
            break;
            case ARCode.ARC_SHOPCART: {
                if (data != null){
                    boolean isNeedReload = data.getBooleanExtra(ARCode.INTENT_KEY_ISRELOAD, false);
                    if (isNeedReload){
                        refreshShopcart();
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
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
            case ValidateManager.ValidateManagerEvent.EVENT_ID_INTERRUPT_NEED_LOGIN: {
                ZLogger.d("账号失效，准备跳转到登录页面");
                redirect2Login();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER: {
                showRegisterPlatDialog();
            }
            break;
            case ValidateManager.ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED: {
                Beta.checkUpgrade(false, false);
            }
            break;
        }
    }


    public void onEventMainThread(ShopcartEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ShopcartEvent(%d)", eventId));
        switch (eventId) {
            case ShopcartEvent.EVENT_ID_ADD2CART: {
                AddCartOptions options = event.getCartOptions();
                if (options != null){
                    AddCartAnimation.AddToCart2((ImageView) options.getSharedView(),
                            floatCartView, getActivity(), rootView, 0.5F);
                }
            }
            break;
            case ShopcartEvent.EVENT_ID_DATASETCHANGED: {
                refreshShopcart();
            }
            break;
        }
    }

    /**
     * 刷新购物车商品数量
     * */
    private void refreshShopcart(){
        NetCallBack.NetTaskCallBack responseC = new NetCallBack.NetTaskCallBack<CartBrief,
                NetProcessor.Processor<CartBrief>>(
                new NetProcessor.Processor<CartBrief>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        ZLogger.df("调整购物车商品数量: 操作成功");
//
                        CartBrief cartBrief = null;
                        try {
                            if (rspData != null) {
                                RspBean<CartBrief> retValue = (RspBean<CartBrief>) rspData;
                                cartBrief = retValue.getValue();
                            }
                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
//
                        if (cartBrief != null){
                            floatCartView.setBadgeNumber(cartBrief.getSkuNum());
//                            DialogUtil.showHint(String.format("商品SKu数目 %.2f", cartBrief.getSkuNum()));
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("调整购物车商品数量: " + errMsg);
                    }
                }
                , CartBrief.class
                , MfhApplication.getAppContext()) {
        };

        ShoppingCartApiImpl.staticShopCart(String.valueOf(curCompanyInfo.getId()), responseC);
    }

    /**
     * 显示注册设备提示框
     */
    public void showRegisterPlatDialog() {
        CommonDialog dialog = new CommonDialog(getActivity());
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
     * 快捷支付
     */
    @OnClick(R.id.btn_pay)
    public void quickPay() {
        if (!MfhLoginService.get().haveLogined()) {
            ZLogger.d("快捷支付，准备跳转到登录页面");
            redirect2Login();
        }

        ActivityRoute.redirect2QuickPay(getActivity());
    }

    @OnClick(R.id.btn_scan)
    public void scannerQR() {
        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            DialogUtil.showHint("拍照权限未打开！");
            // Camera permission has not been granted.
            requestCameraPermission();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            // Camera permissions is already available, show the ScanActivity.
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            startActivityForResult(intent, UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE);
        }
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestCameraPermission() {
        ZLogger.i("CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            ZLogger.i("Displaying camera permission rationale to provide additional context.");
            Snackbar.make(homeRackRecyclerView, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.CAMERA},
                                    Constants.REQUEST_CODE_CAMERA);
                        }
                    })
                    .show();
        } else {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CODE_CAMERA);
        }
        // END_INCLUDE(camera_permission_request)
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            ZLogger.i("Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                ZLogger.i("CAMERA permission has now been granted. Showing preview.");
                Snackbar.make(homeRackRecyclerView, R.string.permision_available_camera,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                ZLogger.i("CAMERA permission was NOT granted.");
                Snackbar.make(homeRackRecyclerView, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        } else if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            ZLogger.i("Received response for contact permissions request.");

            // We have requested multiple permissions for contacts, so all of them need to be
            // checked.
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display contacts fragment.
                Snackbar.make(homeRackRecyclerView, R.string.permision_available_contacts,
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                ZLogger.i("Contacts permissions were NOT granted.");
                Snackbar.make(homeRackRecyclerView, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 跳转到购物车
     */
    @OnClick(R.id.float_cart)
    public void redirect2Cart() {
        if (!MfhLoginService.get().haveLogined()) {
            ZLogger.d("购物车，准备跳转到登录页面");
            redirect2Login();
        }

//        ActivityRoute.redirect2Cart(getActivity(), curAddress, curCompanyInfo);

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_SHOPCART);
        if (curAddress != null){
            extras.putSerializable(ShopcartFragment.EXTRA_KEY_ADDRESSINFO, curAddress);
        }
        if (curCompanyInfo != null){
            extras.putSerializable(ShopcartFragment.EXTRA_KEY_COMPANYINFO, curCompanyInfo);
        }
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_SHOPCART);
    }

    /**
     * 跳转到我的
     */
    private void redirect2My() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_MY);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_MY);
    }

    /**
     * 跳转到预定
     */
    private void redirect2Reserve() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_RESERVE);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 跳转到url
     */
    private void redirect2Url(String url) {
        ZLogger.d("准备跳转页面: " + url);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_HYBRID);
        extras.putString(HybridFragment.EXTRA_KEY_ORIGINALURL, url);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);

//        Intent intent = new Intent(getActivity(), HybridActivity.class);
//        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL,
//                URLHelper.append(url, String.format("ownerId=%d", MfhLoginService.get().getCurrentGuId())));
//        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, true);
//        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
//        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, -1);
//        intent.putExtra(HybridActivity.EXTRA_KEY_COOKIE_URL, Mixicook.COOKIE_URL);
//        intent.putExtra(HybridActivity.EXTRA_KEY_COOKIE_DOMAIN, Mixicook.DOMAIN);
//        startActivity(intent);
    }

    /**
     * 跳转至登录页面
     */
    private void redirect2Login() {
        ZLogger.d("初始化应用，未登录准备跳转到登录页面");

        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);

        if (AppContext.loginType == 0) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.putExtras(extras);
            startActivityForResult(intent, ARCode.ARC_SIGNIN);
        } else {
            Intent intent = new Intent(getActivity(), SmsSignActivity.class);
            intent.putExtras(extras);
            startActivityForResult(intent, ARCode.ARC_SIGNIN);
        }
    }

    /**
     * 跳转至收货地址
     */
    @OnClick({R.id.address_view, R.id.button_change_address})
    public void redirect2MyAddress() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_ADDRESS_LIST);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ADDRESS_LIST);
    }

    /**
     * 跳转至新增地址
     */
    @OnClick(R.id.button_add_address)
    public void redirect2AddAddress() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_ADD_ADDRESS);
        extras.putInt(AddAddressFragment.EXTRA_KEY_MODE, 0);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ADD_ADDRESS);
    }


    /**
     * 初始化广告货架
     */
    private void initBannerRack() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bannerRackRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        bannerRackRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
//        homeRackRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

//        menuRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

//        homeRackRecyclerView.setAdapter(menuAdapter);
    }

    /**
     * 初始化首页货架
     */
    private void initHomeRack() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        homeRackRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        homeRackRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
//        homeRackRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

//        menuRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

//        homeRackRecyclerView.setAdapter(menuAdapter);
    }


    /**
     * 初始化：加载默认收货地址
     */
    private void loadInitStep1() {
        if (MfhLoginService.get().haveLogined()) {
            showProgressDialog(ProgressView.STATUS_PROCESSING, "加载地址...", false);
            mReciaddrPresenter.getDefaultAddrsByHuman(MfhLoginService.get().getCurrentGuId());
        }
    }

    @Override
    public void onIReciaddrViewProcess() {

    }

    @Override
    public void onIReciaddrViewError(String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            ZLogger.e(errorMsg);
            showProgressDialog(ProgressView.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public void onIReciaddrViewSuccess(PageInfo pageInfo, List<Reciaddr> dataList) {

    }

    @Override
    public void onIReciaddrViewSuccess(Reciaddr data) {
        hideProgressDialog();
        curAddress = data;
        loadInitStep2();
    }


    /**
     * 初始化：查询店铺
     */
    private void loadInitStep2() {
        if (curAddress != null) {
            mNaviAddressView.setText(curAddress.getSubName());
            mNoAddressView.setVisibility(View.GONE);

            showProgressDialog(ProgressView.STATUS_PROCESSING, "加载店铺...", false);
            mCompanyInfoPresenter.findServicedNetsForUserPos(curAddress.getCityID(),
                    String.valueOf(curAddress.getLongitude()),
                    String.valueOf(curAddress.getLatitude()), null);
        } else {
            mNaviAddressView.setText("");
//            mNoAddressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onICompanyInfoViewProcess() {

    }

    @Override
    public void onICompanyInfoViewError(String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            ZLogger.e(errorMsg);
            showProgressDialog(ProgressView.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public void onICompanyInfoViewSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
        if (dataList != null && dataList.size() > 0) {
            curCompanyInfo = dataList.get(0);
        } else {
            curCompanyInfo = null;
        }
        hideProgressDialog();
        loadInitStep3();
    }


    /**
     * 初始化：加载店铺货架
     */
    private void loadInitStep3() {
        HomeGoodsTempService.getInstance().clear();
        if (curCompanyInfo == null) {
            mNoCompanyView.setVisibility(View.VISIBLE);
            tvCompanyOutofRange.setText(getString(R.string.address_outofrange, curAddress.getSubName()));
            return;
        }
        mNoCompanyView.setVisibility(View.GONE);

        //刷新购物车
        refreshShopcart();

        showProgressDialog(ProgressView.STATUS_PROCESSING, "加载货架...", false);
        ScStoreRackApi.findByShopMust(curCompanyInfo.getId(),
                String.format("%d,%d",
                        ScStoreRackApi.RACK_TYPE_SC_HOME, ScStoreRackApi.RACK_TYPE_SHOP_HOME),
                rackRC);
//        ScStoreRackApi.getByShopMust(curCompanyInfo.getId(), getByShopMustRC);
    }

    private NetCallBack.QueryRsCallBack rackRC = new NetCallBack.QueryRsCallBack<>(
            new NetProcessor.QueryRsProcessor<StoreRack>(new PageInfo(1, 100)) {
                //                处理查询结果集，子类必须继承
                @Override
                public void processQueryResult(RspQueryResult<StoreRack> rs) {//此处在主线程中执行。
                    List<StoreRack> racks = new ArrayList<>();

                    if (rs != null && rs.getReturnNum() > 0) {
                        for (int i = 0; i < rs.getReturnNum(); i++) {
                            racks.add(rs.getRowEntity(i));
                        }
                    }

                    refreshRackFloor(racks);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    ZLogger.d("processFailure: " + errMsg);
                    refreshRackFloor(null);
                }
            }
            , StoreRack.class
            , MfhApplication.getAppContext());

    /**
     * 刷新首页楼层
     */
    private void refreshRackFloor(List<StoreRack> racks) {
        hideProgressDialog();
        bannerRackRecyclerView.setAdapter(null);
        homeRackRecyclerView.setAdapter(null);
        if (racks == null || racks.size() < 1) {
            DialogUtil.showHint("这个人很懒，什么都没有发布");
            return;
        }
        List<CardProduct> products = new ArrayList<>();

        try {
            for (StoreRack rack : racks) {
                String dataInfo = rack.getDataInfo();
                ZLogger.d("dataInfo:\n" + dataInfo);
                String unescapeDataInfo = StringEscapeUtils.unescapeJava(dataInfo);
                ZLogger.d("unescapeDataInfo:\n" + unescapeDataInfo);

                if (StringUtils.isEmpty(unescapeDataInfo)){
                    ZLogger.d(String.format("<%s>: 什么都没有", rack.getRackName()));
                    continue;
                }

                List<StoreRackCard> cards = JSONArray.parseArray(unescapeDataInfo,
                        StoreRackCard.class);

                if (cards == null) {
                    ZLogger.d(String.format("<%s>: 什么都没有", rack.getRackName()));
                    continue;
                } else {
                    ZLogger.d(String.format("<%s>:\n%s", rack.getRackName(), JSONObject.toJSONString(cards)));
                }
                List<CardProduct> temp = null;
                if (rack.getRackType() == ScStoreRackApi.RACK_TYPE_SC_HOME) {
                    temp = decodeBannerCard(cards);
                } else if (rack.getRackType() == ScStoreRackApi.RACK_TYPE_SHOP_HOME) {
                    temp = decodeHomeCard(cards);
                } else {

                }
                if (temp != null){
                    products.addAll(temp);
                }
            }
            loadInitStep5(products);

        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    /**
     * 解析商城首页数据
     */
    private List<CardProduct> decodeBannerCard(List<StoreRackCard> cards) {
        List<CardProduct> products = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        if (cards != null && cards.size() > 0) {
            for (StoreRackCard card : cards) {
                List<StoreRackCardItem> cardItems = card.getItems();
                if (card.getType().equals(1)) {
                    List<Card1Item> card1Items = new ArrayList<>();
                    if (cardItems != null && cardItems.size() > 0) {
                        for (StoreRackCardItem cardItem : cardItems) {
                            Card1Item card1Item = new Card1Item();
                            card1Item.setImageUrl(cardItem.getImageUrl());
                            card1Item.setLink(cardItem.getLink());
                            card1Item.setLnktype(cardItem.getLnktype());
                            card1Items.add(card1Item);
                        }
                    }
                    Card1 card1 = new Card1();
                    card1.setItems(card1Items);
                    items.add(card1);
                    ZLogger.d(String.format("添加card1: %d", card1Items.size()));
                } else if (card.getType().equals(2)) {
                    List<Card2Item> card2Items = new ArrayList<>();
//                                if (cardItems != null && cardItems.size() > 0) {
//                                    for (StoreRackCardItem cardItem : cardItems) {
//                                        Card2Item card2Item = new Card2Item();
//                                        card2Item.setImageUrl(cardItem.getImageUrl());
//                                        card2Item.setLink(cardItem.getLink());
//                                        card2Item.setLnktype(cardItem.getLnktype());
//                                        card2Items.add(card2Item);
//                                    }
//                                }
//                                Card2 card2 = new Card2();
//                                card2.setType(card.getType());
//                                card2.setNetId(card.getNetId());
//                                card2.setItems(card.getItems());
//                                card2.setNetName(card.getNetName());
//                                card2.setCategoryName(card.getCategoryName());
//                                card2.setFrontCategoryId(card.getFrontCategoryId());
//                                card2.setProducts(card.getProducts());
//                                card2.setItems(card2Items);
//                                items.add(card2);
//                                ZLogger.d(String.format("添加card2: %d", card2Items.size()));
                    items.add(card);

                    ZLogger.d("添加card2：" + card.getCategoryName());
                }
                else if (card.getType().equals(6)) {
                    Card6 card6 = new Card6();
                    card6.setType(card.getType());
                    card6.setNetName(card.getNetName());
                    card6.setItems(card.getItems());
                    items.add(card6);
                    ZLogger.d("添加card6：" + card6.getNetName());
                }else if (card.getType().equals(9)) {
                    Card9 card9 = new Card9();
                    card9.setType(card.getType());
                    card9.setCategoryName(card.getCategoryName());
                    card9.setFrontCategoryId(card.getFrontCategoryId());
                    card9.setProducts(card.getProducts());
                    card9.setShopId(curCompanyInfo.getId());
                    items.add(card9);
                    ZLogger.d("添加card9：" + card9.getCategoryName());

                    products.addAll(card.getProducts());
                }
            }
        }
//            ZLogger.d("items.size=" + items.size());
        if (items.size() > 0) {
            bannerRackRecyclerView.setAdapter(new MultiTypeAdapter(items));
        } else {
            bannerRackRecyclerView.setAdapter(null);
        }

        return products;
    }


    /**
     * 解析首页卡片数据
     */
    private List<CardProduct> decodeHomeCard(List<StoreRackCard> cards) {
        List<CardProduct> products = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        if (cards != null && cards.size() > 0) {
            for (StoreRackCard card : cards) {
                List<StoreRackCardItem> cardItems = card.getItems();
                if (card.getType().equals(1)) {
                    List<Card1Item> card1Items = new ArrayList<>();
                    if (cardItems != null && cardItems.size() > 0) {
                        for (StoreRackCardItem cardItem : cardItems) {
                            Card1Item card1Item = new Card1Item();
                            card1Item.setImageUrl(cardItem.getImageUrl());
                            card1Item.setLink(cardItem.getLink());
                            card1Item.setLnktype(cardItem.getLnktype());
                            card1Items.add(card1Item);
                        }
                    }
                    Card1 card1 = new Card1();
                    card1.setItems(card1Items);
                    items.add(card1);
                    ZLogger.d(String.format("添加card1: %d", card1Items.size()));
                } else if (card.getType().equals(2)) {
                    List<Card2Item> card2Items = new ArrayList<>();
//                                if (cardItems != null && cardItems.size() > 0) {
//                                    for (StoreRackCardItem cardItem : cardItems) {
//                                        Card2Item card2Item = new Card2Item();
//                                        card2Item.setImageUrl(cardItem.getImageUrl());
//                                        card2Item.setLink(cardItem.getLink());
//                                        card2Item.setLnktype(cardItem.getLnktype());
//                                        card2Items.add(card2Item);
//                                    }
//                                }
//                                Card2 card2 = new Card2();
//                                card2.setType(card.getType());
//                                card2.setNetId(card.getNetId());
//                                card2.setItems(card.getItems());
//                                card2.setNetName(card.getNetName());
//                                card2.setCategoryName(card.getCategoryName());
//                                card2.setFrontCategoryId(card.getFrontCategoryId());
//                                card2.setProducts(card.getProducts());
//                                card2.setItems(card2Items);
//                                items.add(card2);
//                                ZLogger.d(String.format("添加card2: %d", card2Items.size()));
                    items.add(card);
                    ZLogger.d("添加card2：" + card.getCategoryName());

                } else if (card.getType().equals(9)) {
                    Card9 card9 = new Card9();
                    card9.setType(card.getType());
                    card9.setCategoryName(card.getCategoryName());
                    card9.setFrontCategoryId(card.getFrontCategoryId());
                    card9.setProducts(card.getProducts());
                    card9.setShopId(curCompanyInfo.getId());
                    items.add(card9);
                    ZLogger.d("添加card9：" + card9.getCategoryName());

                    products.addAll(card.getProducts());
                } else if (card.getType().equals(10)) {
                    Card10 card10 = new Card10();
                    card10.setType(card.getType());
                    card10.setNetName(card.getNetName());
                    card10.setItems(card.getItems());
                    items.add(card10);
                    ZLogger.d("添加card10：" + card10.getNetName());
                }
            }
        }
//            ZLogger.d("items.size=" + items.size());
        if (items.size() > 0) {
            homeRackRecyclerView.setAdapter(new MultiTypeAdapter(items));
        } else {
            homeRackRecyclerView.setAdapter(null);
        }

        return products;
    }


    /**
     * 初始化：批量更新商品信息
     */
    private void loadInitStep5(List<CardProduct> products) {
        if (products == null || products.size() < 1) {
            ZLogger.d("商品列表为空，不需要批量更新商品信息");
            return;
        }

        showProgressDialog(ProgressView.STATUS_PROCESSING, "加载商品信息...", false);

        StringBuilder sb = new StringBuilder();
        for (CardProduct product : products) {
            Long skuId = product.getSkuId();
            if (skuId == null){
                continue;
            }

            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(skuId);
        }
        mScGoodsSkuPresenter.findOnlineGoodsList(curCompanyInfo.getId(), sb.toString(), null);
    }

    @Override
    public void onIScGoodsSkuViewProcess() {

    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            ZLogger.e(errorMsg);
            showProgressDialog(ProgressView.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        hideProgressDialog();
        HomeGoodsTempService.getInstance().batch(dataList);
//        保存数据
        if (bannerRackRecyclerView.getAdapter() != null){
            bannerRackRecyclerView.getAdapter().notifyDataSetChanged();
        }
        if (homeRackRecyclerView.getAdapter() != null){
            homeRackRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku data) {

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

package com.manfenjiayuan.mixicook_vip.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.Constants;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.manfenjiayuan.mixicook_vip.ui.address.IReciaddrView;
import com.manfenjiayuan.mixicook_vip.ui.address.ReciaddrPresenter;
import com.manfenjiayuan.mixicook_vip.ui.hybrid.HybridFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.anon.storeRack.ScStoreRackApi;
import com.mfh.framework.api.anon.storeRack.StoreRack;
import com.mfh.framework.api.anon.storeRack.StoreRackCard;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.companyInfo.CompanyInfoPresenter;
import com.mfh.framework.api.companyInfo.ICompanyInfoView;
import com.mfh.framework.api.mobile.Mixicook;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.JBridgeConf;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.network.URLHelper;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.compound.ProgressView;
import com.mfh.framework.uikit.widget.EmptyLayout;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import me.drakeet.multitype.Item;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class HomeFragment extends BaseFragment
        implements IReciaddrView, ICompanyInfoView, IStoreRackView {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.address_view)
    NaviAddressView mNaviAddressView;

    @Bind(R.id.viewpager_adv)
    AdvertisementViewPager advertiseViewPager;
    private BannerAdapter mBannerAdapter;
    @Bind(R.id.nav)
    RecyclerView menuRecyclerView;
    private CloudMenuAdapter menuAdapter;
    @Bind(R.id.storeRackList)
    RecyclerView storeRackRecyclerView;

    @Bind(R.id.webview)
    HybridWebView mWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;
    @Bind(R.id.noAddressView)
    View mNoAddressView;
    @Bind(R.id.noCompanyView)
    View mNoCompanyView;

    private WebViewJavascriptBridge bridge;

    //当前收货地址，用来定位店铺
    private Reciaddr curAddress = null;
    private CompanyInfo curCompanyInfo = null;//当前店铺
    private ReciaddrPresenter mReciaddrPresenter;
    private CompanyInfoPresenter mCompanyInfoPresenter;
    private StoreRackPresenter mStoreRackPresenter;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        mReciaddrPresenter = new ReciaddrPresenter(this);
        mCompanyInfoPresenter = new CompanyInfoPresenter(this);
        mStoreRackPresenter = new StoreRackPresenter(this);

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
                    Bundle extras = new Bundle();
                    extras.putString(SimpleActivity.EXTRA_TITLE, "我的");
                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                    extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_MY);
                    Intent intent = new Intent(getActivity(), FragmentActivity.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_home);
        mBannerAdapter = new BannerAdapter(getActivity(), null, advertiseViewPager,
                new BannerAdapter.OnBannerAdapterCallback() {
                    @Override
                    public void onRedirectTo(String url) {
                        redirect2Url(url);
                    }
                });
        advertiseViewPager.setAdapter(mBannerAdapter);

        //TODO,定时切换(每隔5秒切换一次)
        advertiseViewPager.startSlide(3 * 1000);

        initCloudMenus();
        initStoreRack();
        initWebView();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadInitStep3();
            }
        });

        loadInitStep1();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
        }
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
            case ARCode.ARC_MYADDRESS: {
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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btn_pay)
    public void quickPay() {
        if (!MfhLoginService.get().haveLogined()) {
            redirect2Login();
        }

        ActivityRoute.redirect2QuickPay(getActivity());
    }

    @OnClick(R.id.btn_scan)
    public void scannerQR() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            DialogUtil.showHint("拍照权限未打开！");
            return;
        }

        //需要处理扫描结果
        Intent intent = new Intent(getActivity(), ScanActivity.class);
        startActivityForResult(intent, UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE);
    }

    /**
     * 跳转到购物车
     */
    @OnClick(R.id.fab_cart)
    public void redirect2Cart() {
        if (!MfhLoginService.get().haveLogined()) {
            redirect2Login();
        }

        ActivityRoute.redirect2Cart(getActivity(), 136076L);
//        Bundle extras = new Bundle();
//        extras.putString(SimpleActivity.EXTRA_TITLE, "购物车");
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_SHOPCART);
//        extras.putLong(ShopcartFragment.EXTRA_KEY_SHOP_ID, 136076L);
//        Intent intent = new Intent(getActivity(), FragmentActivity.class);
//        intent.putExtras(extras);
//        startActivity(intent);
    }

    /**
     * 跳转到预定
     */
    private void redirect2Reserve() {
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "预定");
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
        extras.putString(SimpleActivity.EXTRA_TITLE, "");
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
        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);

        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);

//        LoginActivity.actionStart(MainActivity.this, null);
//        finish();
    }

    /**
     * 跳转至定位
     */
    @OnClick({R.id.address_view, R.id.button_change_address})
    public void redirect2MyAddress() {
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "收货地址");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_MYADDRESS);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_MYADDRESS);
    }

    /**
     * 跳转至定位
     */
    @OnClick(R.id.button_add_address)
    public void redirect2AddAddress() {
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "新增收货地址");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_ADD_ADDRESS);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ADD_ADDRESS);
    }

    public void redirect2Location() {
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "定位");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_LOCATION);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 初始化菜单
     */
    private void initCloudMenus() {
        GridLayoutManager mRLayoutManager = new GridLayoutManager(getActivity(), 4);
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

//        menuRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

        menuAdapter = new CloudMenuAdapter(getActivity(), null);
        menuAdapter.setOnAdapterLitener(new CloudMenuAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO: 9/27/16 跳转页面
                CloudMenu menu = menuAdapter.getEntity(position);
                if (menu != null) {
                    if (position == 0) {
                        redirect2Reserve();
                    } else {
                        redirect2Url(menu.getLink());
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
    }

    /**
     * 初始化菜单
     */
    private void initStoreRack() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        storeRackRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        storeRackRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
//        storeRackRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

//        menuRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

//        storeRackRecyclerView.setAdapter(menuAdapter);
    }

    /**
     * 初始化webview
     */
    private void initWebView() {
        syncCookies(getActivity(), Mixicook.COOKIE_URL);

//        mWebView.setScrollViewCallbacks(mWebViewScrollCallbacks);

        registerHandle();
//        this.mWebView.setWebViewClient(new MyWebViewClient());
//        this.mWebView.setWebChromeClient(new MyWebChromeClient());
    }




    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d(url);

//            if (Uri.parse(url).getHost().equals(Mixicook.DOMAIN)) {
//                // This is my web site, so do not override; let my WebView load the page
//                return false;
//            }
//            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            HybridActivity.actionStart(getActivity(), url, true, false, 0);
//            return true;

            if (webviewDelegate != null) {
                webviewDelegate.shouldOverrideUrlLoading(view, url);
            }
//
            boolean flag = super.shouldOverrideUrlLoading(view, url);
////            mCurrentUrl = url;
////            isLocalResource = false;
            return flag;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (webviewDelegate != null) {
                webviewDelegate.onPageFinished(view, url);
            }
        }


        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            if (webviewDelegate != null) {
                webviewDelegate.onReceivedError(view, errorCode, description, failingUrl);
            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            //Uncaught SyntaxError: Unexpected token var line:1
            if (cm != null) {
                DialogUtil.showHint(cm.message());
                ZLogger.d(cm.message() + "-- From line:" + cm.lineNumber()
                        + " of " + cm.sourceId());
            } else {
                ZLogger.d("null");
            }
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            // if don't cancel the alert, webview after onJsAlert not responding taps
            // you can check this :
            // http://stackoverflow.com/questions/15892644/android-webview-after-onjsalert-not-responding-taps
            result.cancel();
            ZLogger.d(String.format("url:%s , message:%s", url, message));
            DialogUtil.showHint(message);
            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            ZLogger.d(title);
            if (webviewDelegate != null) {
                webviewDelegate.onReceivedTitle(view, title);
            }
        }


        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            if (webviewDelegate != null) {
                webviewDelegate.onReceivedIcon(view, icon);
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) { // 进度
            super.onProgressChanged(view, newProgress);
            ZLogger.d(String.format("网页加载进度 %d%%", newProgress));
            if (webviewDelegate != null) {
                webviewDelegate.onProgressChanged(view, newProgress);
            }
        }

    }

    /**
     * register native method
     */
    private void registerHandle() {
        bridge = new WebViewJavascriptBridge(getActivity(), mWebView, new UserServerHandler(), webviewDelegate);
//新增商品到购物车
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_ADD2CART,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        {
//                                "productId": 456,
//                                "productName": "商品名",
//                                "productPrice": 88.88,
//                                "productImageUrl": "商品图片链接",
//                                "shopId": 789
//                        }
                        // TODO: 9/28/16 保存商品到数据库
                    }
                });
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler {
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
            if (null != responseCallback) {
                responseCallback.callback("i have already received you data.");
            }
        }
    }

    WebViewDelegate webviewDelegate = new WebViewDelegate() {

        @Override
        public void onPageFinished(WebView view, String url) {
            onUrlFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            emptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals(Mixicook.URL_MARKET_SHOP)) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            HybridActivity.actionStart(getActivity(), url, true, false, 0);
            redirect2Url(url);
            return true;

//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

//            url = Uri.decode(url);
//            view.loadUrl(url);//在当前webview中加载页面。

//            HybridActivity.actionStart(getActivity(), url, true, false, -1);
//            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            ZLogger.d("网页标题: " + title);
//            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            ZLogger.d(String.format("网页加载进度 %d%%", newProgress));

//            if (newProgress > 90) {
////                loadingImageView.toggle(false);
////                emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            }
        }
    };

    /**
     * 链接载入成功后会被调用
     *
     * @param view WebView
     * @param url  链接地址
     */
    protected void onUrlFinished(WebView view, String url) {
        ZLogger.d("网页加载完成 " + url);
//        mCurrentUrl = url;
//        ZLogger.d("onPageFinished, mCurrentUrl = " + mCurrentUrl);
        if (emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING) {
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }
    }

    /**
     * 初始化：加载默认收货地址
     */
    private void loadInitStep1() {
        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner("http://chunchunimage.b0.upaiyun.com/material/2178_ResMedia1_0.png",
                "http://mobile.mixicook.com/mobile/market/shop/prods?vt=fresh"));
        banners.add(new Banner("http://chunchunimage.b0.upaiyun.com/material/2179_ResMedia1_0.png",
                "http://c-nfa.jd.com/adclick?keyStr=z5AXFoIimt1jiDK32+w4mWlR1HP401IHmvwFQGlQg62DECFaj1VPyEZYMw3cXfc2CxvOOVD2rnv/tQnNILoRbJZok8XgW9aC0UnfI2Y2DbLW8RAi674cFuEN84YZEeQonstpyx8chdzGH/Tt/DuZKOTYQSW32nwX2XXLNm2EQSo7yQH6xLNJLR1ZWmrHbzuMsydBGMtfwt6u4LkNC7bDAMpcGf/hfJ9kGSpkO+vR1GqxgCGbFGYGghKlU0XJlFcvhr7z3ac6iz/RpJGd/q9syZ/0ct4OKlEcAqbi9x835TUooVTqpViRgRutHXyISSAg&cv=2.0&url=//pinpaijie.jd.com/"));
        mBannerAdapter.setData(banners);

        List<CloudMenu> menus = new ArrayList<>();
        menus.add(new CloudMenu("http://chunchunimage.b0.upaiyun.com/material/2131_ResMedia1_0.png",
                "http://mobile.mixicook.com/mobile/market/shop/prods?vt=fresh"));
        menus.add(new CloudMenu("http://chunchunimage.b0.upaiyun.com/material/2180_ResMedia1_0.png",
                "http://mobile.mixicook.com/mobile/me/address"));
        menus.add(new CloudMenu("http://chunchunimage.b0.upaiyun.com/material/2177_ResMedia1_0.png",
                "http://mobile.mixicook.com/mobile/market/shop?netId=136076"));
        menus.add(new CloudMenu("http://chunchunimage.b0.upaiyun.com/material/2181_ResMedia1_0.png",
                "http://mobile.mixicook.com/mobile/me/coupons"));
        menuAdapter.setEntityList(menus);

        loadInitStep4(83L);
        showProgressDialog(ProgressView.STATUS_PROCESSING, "加载地址...", false);
        //加载默认收货地址
        mReciaddrPresenter.getDefaultAddrsByHuman(MfhLoginService.get().getCurrentGuId());
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
        if (curCompanyInfo != null) {
            mNoCompanyView.setVisibility(View.GONE);

            showProgressDialog(ProgressView.STATUS_PROCESSING, "加载货架...", false);
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Long,
                    NetProcessor.Processor<Long>>(
                    new NetProcessor.Processor<Long>() {
                        @Override
                        public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","dat"}}
                            Long rackId = null;
                            try {
                                if (rspData != null){
                                    RspBean<Long> retValue = (RspBean<Long>) rspData;
                                    if (retValue != null){
                                        rackId = retValue.getValue();
                                    }
                                }
                            } catch (Exception e) {
                                ZLogger.ef(e.toString());
                            }
                            loadInitStep4(rackId);
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            ZLogger.d("加载店铺信息失败:" + errMsg);
                            loadInitStep4(null);
                        }
                    }
                    , Long.class
                    , MfhApplication.getAppContext()) {
            };

            ScStoreRackApi.getByShopIdMust(curCompanyInfo.getId(), responseCallback);


            String newUrl = URLHelper.append(Mixicook.URL_MARKET_SHOP,
                    String.format("ownerId=%d&netId=%d",
                            MfhLoginService.get().getCurrentGuId(), curCompanyInfo.getId()));

            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

            WebViewUtils.loadUrl(mWebView, newUrl);
//        mWebView.loadUrl("http://www.jd.com/");
        } else {
            mNoCompanyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化：加载货架商品信息
     */
    private void loadInitStep4(Long rackId) {
        hideProgressDialog();
        if (rackId == null) {
            DialogUtil.showHint("这个人很懒，什么都没有发布");
            return;
        }

        showProgressDialog(ProgressView.STATUS_PROCESSING, "加载货架商品...", false);
        mStoreRackPresenter.getById(rackId);
    }

    @Override
    public void onIStoreRackViewProcess() {

    }

    @Override
    public void onIStoreRackViewError(String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            ZLogger.e(errorMsg);
            showProgressDialog(ProgressView.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public void onIStoreRackViewSuccess(StoreRack data) {
        hideProgressDialog();
        if (data != null){
//            List<StoreRackCard> dataInfo = data.getDataInfo();
//            if (dataInfo != null && dataInfo.size() > 0){
//                for (StoreRackCard storeRackCard : dataInfo){
//                    ZLogger.d("storeRackCard:\n" + JSONObject.toJSONString(storeRackCard));
//                }
//            }

            String dataInfo = data.getDataInfo();
            ZLogger.d("dataInfo:\n" + dataInfo);
            String unescapeDataInfo = StringEscapeUtils.unescapeJava(dataInfo);
            ZLogger.d("unescapeDataInfo:\n" + unescapeDataInfo);
            //后台返回的unescapeDataInfo前后各有一个双引号，需要删除才能正确解析
            if (!StringUtils.isEmpty(unescapeDataInfo)){
                unescapeDataInfo = unescapeDataInfo.substring(1);
                ZLogger.d("unescapeDataInfo:\n" + unescapeDataInfo);
                unescapeDataInfo = unescapeDataInfo.substring(0, unescapeDataInfo.length() -1);
                ZLogger.d("unescapeDataInfo:\n" + unescapeDataInfo);
            }

            List<StoreRackCard> storeRackCards2 = null;
            try{
                storeRackCards2 = JSONArray.parseArray(unescapeDataInfo,
                        StoreRackCard.class);
                if (storeRackCards2 == null){
                    ZLogger.d("storeRackCards2 is null");
                }
                else{
                    ZLogger.d("storeRackCards2:\n" + JSONObject.toJSONString(storeRackCards2));
                }
            }
            catch (Exception e){
                ZLogger.e(e.toString());
            }

            List<Item> items = new ArrayList<>();
            if (storeRackCards2 != null && storeRackCards2.size() > 0){
                for (StoreRackCard card : storeRackCards2){
                    if (card.getType().equals(2)){
                        items.add(card);
                    }
                }
            }
            ZLogger.d("items.size=" + items.size());
            if (items.size() > 0){
                storeRackRecyclerView.setAdapter(new MultiTypeAdapter(items));
            }
            else{
                storeRackRecyclerView.setAdapter(null);
            }
        }
    }

    private void loadInitStep5(){
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if (mWebView != null) {
            String url = mWebView.getUrl();
            if (StringUtils.isEmpty(url)) {
                return;
            }
//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            mWebView.clearHistory();
            mWebView.loadUrl(url);
        }
    }

    /**
     * 同步Cookie
     */
    public static void syncCookies(Context context, String url) {
        String sessionId = MfhLoginService.get().getCurrentSessionId();
        if (sessionId != null) {
            StringBuilder sbCookie = new StringBuilder();
            sbCookie.append(String.format("JSESSIONID=%s", sessionId));
            sbCookie.append(String.format(";domain=%s", Mixicook.DOMAIN));
            sbCookie.append(String.format(";path=%s", "/"));
            String cookieValue = sbCookie.toString();

            WebViewUtils.syncCookies(context, url, cookieValue);
        }
    }

}

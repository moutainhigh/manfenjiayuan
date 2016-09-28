package com.manfenjiayuan.mixicook_vip.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.manfenjiayuan.business.Constants;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.widget.EmptyLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class HomeFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.address_view)
    NaviAddressView mNaviAddressView;

    @Bind(R.id.viewpager_adv)
    AdvertisementViewPager advertiseViewPager;
    private BannerAdapter mBannerAdapter;
    @Bind(R.id.nav)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private CloudMenuAdapter menuAdapter;

    @Bind(R.id.webview)
    HybridWebView mWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private WebViewJavascriptBridge bridge;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
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
                Intent intent = new Intent(getActivity(), HybridActivity.class);
                intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL, url);
                intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, true);
                intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
                intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, -1);
                startActivity(intent);
            }
        });
        advertiseViewPager.setAdapter(mBannerAdapter);

        //TODO,定时切换(每隔5秒切换一次)
        advertiseViewPager.startSlide(3 * 1000);

        initCloudMenus();
        initWebView();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        DialogUtil.showHint("首页");
        loadInit();
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
        if (requestCode == UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE) {
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

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btn_pay)
    public void quickPay() {
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "支付");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_FRAGMENT_TYPE, SimpleActivity.FT_QUICK_PAY);
        Intent intent = new Intent(getActivity(), SimpleActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
//        startActivityForResult(intent, 0);
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
     * */
    @OnClick(R.id.fab_cart)
    public void redirect2Cart(){
        if (!MfhLoginService.get().haveLogined()){
            redirect2Login();
        }

        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "购物车");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_SHOPCART);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 跳转到预定
     * */
    private void redirect2Reserve(){
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
     * */
    private void redirect2Url(String url){
        Intent intent = new Intent(getActivity(), HybridActivity.class);
        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, true);
        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, -1);
        startActivity(intent);
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
     * 初始化菜单
     */
    private void initCloudMenus() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 4);
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
                if (menu != null){
                    if (position == 0){
                        redirect2Reserve();
                    }
                    else {
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
     * 初始化webview
     */
    private void initWebView() {
//        mWebView.setScrollViewCallbacks(mWebViewScrollCallbacks);

        bridge = new WebViewJavascriptBridge(getActivity(), mWebView, new UserServerHandler(), webviewDelegate);
        registerHandle();
    }

    /**
     * register native method
     */
    private void registerHandle() {

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
            ZLogger.d(String.format("shouldOverrideUrlLoading url=%s\ncookie=%s",
                    url, CookieManager.getInstance().getCookie(url)));

//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

//            url = Uri.decode(url);
//            view.loadUrl(url);//在当前webview中加载页面。

//            HybridActivity.actionStart(getActivity(), url, true, false, -1);
            return false;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
//            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
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
//        mCurrentUrl = url;
//        ZLogger.d("onPageFinished, mCurrentUrl = " + mCurrentUrl);
        if (emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING) {
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }
    }

    /**
     * 初始化
     */
    private void loadInit() {
        // TODO: 9/27/16 加载轮播图片
        // TODO: 9/27/16 加载菜单
        // TODO: 9/27/16 加载商品卡片
        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner("https://aecpm.alicdn.com/simba/img/TB1FmSgNpXXXXb_XFXXSutbFXXX.jpg",
                "https://click.mz.simba.taobao.com/ecpm?spm=a21bo.50862.201862-2.1.YgfLLw&e=kTCipUy" +
                        "zWXum7JZHpWwQvxsQmLP5zomMZiJVzamea9wtBR5W0ylJQEWMIF9CwHAmYJPuQun1zwv6oPtw24" +
                        "gOLPkh5UuRjYwuUK7RDtIZydUyXElRAMLwo5FiZpwDPce9SalYl30lkz6EOCeXha9ClVp5FikGw" +
                        "iZ293AHALUCy6sEozLirFoc19qg9rrJTYaGxAZE3vfNe%2BaUNQizIwF2NX5kRLZc8N8e7gx%2B" +
                        "QcPBhJKwjDMCz7Gm3eDXFqBoDmUIthsRmBbRVqur5DdJh33rZPXFmVKPNARQeGovvB2SXPWW0YB" +
                        "cNFpS7W7O5HK4VQsSjbPtEmzvMB7RsJRkwcYjgA%3D%3D&u=https%3A%2F%2Fdetail.tmall." +
                        "com%2Fitem.htm%3F_u%3Dk2hj1fm6f08e%26id%3D529654018306&k=345"));
        banners.add(new Banner("https://img.alicdn.com/tps/TB1ruKDNpXXXXXmXFXXXXXXXXXX-520-280.jpg",
                "https://www.taobao.com/markets/promotion/pingjia?spm=a21bo.50862.201862-1.d1.YgfLL" +
                        "w&acm=20140506001.1003.2.1129488&aldid=xpXHqNce&scm=1003.2.20140506001.OTHER" +
                        "_1472993910877_1129488&pos=1"));
        mBannerAdapter.setData(banners);

        List<CloudMenu> menus = new ArrayList<>();
        menus.add(new CloudMenu("https://img.alicdn.com/i2/2/TB1eWlgKFXXXXXaXXXXSutbFXXX.jpg_170x170.jpg",
                "https://store.taobao.com/shop/view_shop.htm?spm=a21bo.50862.201863-4.d1.YgfLLw&pvid=b3cc2262-f10a-4534-8635-eef3730de34b&abbucket=_AB-M65_B5&acm=03014.1003.1.765824&aldid=xpXHqNce&user_number_id=628189716&abtest=_AB-LR65-PR65&brandId=11714362&scm=1007.13143.30625.100200300000000&pos=1"));
        menus.add(new CloudMenu("https://img.alicdn.com/i2/2/TB1vNfVHpXXXXbDXXXXSutbFXXX.jpg_170x170.jpg",
                "https://store.taobao.com/shop/view_shop.htm?spm=a21bo.50862.201863-4.d9.YgfLLw&pvid=b3cc2262-f10a-4534-8635-eef3730de34b&abbucket=_AB-M65_B5&acm=03014.1003.1.765824&aldid=xpXHqNce&user_number_id=669816508&abtest=_AB-LR65-PR65&brandId=95804681&scm=1007.13143.30625.100200300000000&pos=9"));
        menus.add(new CloudMenu("https://img.alicdn.com/i2/2/TB1eWlgKFXXXXXaXXXXSutbFXXX.jpg_170x170.jpg",
                "https://store.taobao.com/shop/view_shop.htm?spm=a21bo.50862.201863-4.d1.YgfLLw&pvid=b3cc2262-f10a-4534-8635-eef3730de34b&abbucket=_AB-M65_B5&acm=03014.1003.1.765824&aldid=xpXHqNce&user_number_id=628189716&abtest=_AB-LR65-PR65&brandId=11714362&scm=1007.13143.30625.100200300000000&pos=1"));
        menus.add(new CloudMenu("https://img.alicdn.com/i2/2/TB1vNfVHpXXXXbDXXXXSutbFXXX.jpg_170x170.jpg",
                "https://store.taobao.com/shop/view_shop.htm?spm=a21bo.50862.201863-4.d9.YgfLLw&pvid=b3cc2262-f10a-4534-8635-eef3730de34b&abbucket=_AB-M65_B5&acm=03014.1003.1.765824&aldid=xpXHqNce&user_number_id=669816508&abtest=_AB-LR65-PR65&brandId=95804681&scm=1007.13143.30625.100200300000000&pos=9"));
        menuAdapter.setEntityList(menus);

        mWebView.loadUrl("http://www.jd.com/");
    }

    /**
     * 重新加载
     */
    private void reload() {
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if (mWebView != null) {
            String url = mWebView.getUrl();
            if (TextUtils.isEmpty(url)) {
                return;
            }
//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            mWebView.clearHistory();
            mWebView.loadUrl(url);
        }
    }

}

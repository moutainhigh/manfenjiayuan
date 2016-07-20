package com.manfenjiayuan.loveshopping.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.manfenjiayuan.loveshopping.R;
import com.manfenjiayuan.loveshopping.activity.SelectAddressActivity;
import com.manfenjiayuan.loveshopping.eventbus.CommunityEvent;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.net.URLHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.uikit.widget.OnTabReselectListener;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.entity.Subdis;
import com.mfh.framework.api.MfhApi;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

//import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link H5HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link H5HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class H5HomeFragment extends BaseFragment implements
        OnTabReselectListener {

    @Bind(R.id.address_view)
    NaviAddressView mAddressView;
    @Bind(R.id.webview)
    HybridWebView mWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;
    public static final int STATE_NONE = 0;
    public static final int STATE_REFRESH = 1;
    public static final int STATE_LOADMORE = 2;
    public static final int STATE_NOMORE = 3;
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    public static int mState = STATE_NONE;
    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private WebViewJavascriptBridge bridge;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_URL = "url";
    public static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String pageUrl;
    private String mParam2;

    private Subdis mSubdis;


    public H5HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static H5HomeFragment newInstance(String url, String param2) {
        H5HomeFragment fragment = new H5HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home_h5;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            pageUrl = getArguments().getString(ARG_URL, URLHelper.append(H5Api.URL_HOME_SHOP, "shopId=133123"));
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        ZLogger.d(String.format("pageUrl: %s", pageUrl));
        initWebView();

        setupSwipeRefresh();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        loadData();
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

        EventBus.getDefault().unregister(this);

        if (mWebView != null) {
            mWebView.destroy();
        }
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
        //跳转至热卖商品
//        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATICE_HOTSALE,
//                new WebViewJavascriptBridge.WVJBHandler() {
//                    @Override
//                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
////                        {
////                            "shopId": 131228
////                        }
//                        JSONObject jsonObject = JSON.parseObject(data);
//                        Long shopId = jsonObject.getLong("shopId");
//                        Bundle extras = new Bundle();
//                        extras.putLong(HotSalesActivity.EXTRA_KEY_SHOP_ID, shopId);
//                        HotSalesActivity.actionStart(getContext(), extras);
//                    }
//                });
//
//        //跳转至我常买商品
//        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATICE_OFENBUY,
//                new WebViewJavascriptBridge.WVJBHandler() {
//                    @Override
//                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        //                        {
////                            "shopId": 131228
////                        }
//                        JSONObject jsonObject = JSON.parseObject(data);
//                        Long shopId = jsonObject.getLong("shopId");
//                        Bundle extras = new Bundle();
//                        extras.putLong(OfenBuyActivity.EXTRA_KEY_SHOP_ID, shopId);
//                        OfenBuyActivity.actionStart(getContext(), extras);
//
//
//
//                    }
//                });
//
//        //跳转至全部商品
//        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATICE_ALL,
//                new WebViewJavascriptBridge.WVJBHandler() {
//                    @Override
//                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
////                        {
////                            "shopId": 131228
////                              "categoryId": ""
////                        }
//                        JSONObject jsonObject = JSON.parseObject(data);
//                        Long shopId = jsonObject.getLong("shopId");
//                        String categoryId = jsonObject.getString("categoryId");
//                        Bundle extras = new Bundle();
//                        extras.putLong(CategoryTabActivity.EXTRA_KEY_SHOP_ID, shopId);
//                        extras.putString(CategoryTabActivity.EXTRA_KEY_CATEGORY_ID, categoryId);
////                        AllProductActivity.actionStart(getContext(), extras);
//                        CategoryTabActivity.actionStart(getContext(), extras);
//                    }
//                });
//
//        //跳转至购物车
//        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATIVE_SHOPCART,
//                new WebViewJavascriptBridge.WVJBHandler() {
//                    @Override
//                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        ShoppingCartActivity.actionStart(getContext(), 0);
//                    }
//                });
//
//
//        //跳转至商品详情
//        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATIVE_PRODUCT_DETAIL,
//                new WebViewJavascriptBridge.WVJBHandler() {
//                    @Override
//                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
////                        {
////                                "productId": 456,
////                                "shopId": 789
////                        }
//                        JSONObject jsonObject = JSON.parseObject(data);
//                        Long productId = jsonObject.getLong("productId");
//                        Long shopId = jsonObject.getLong("shopId");
//
//                        Bundle extras = new Bundle();
//                        extras.putInt(ProductDetailActivity.EXTRA_KEY_ANIM_TYPE, 0);
//                        extras.putLong(ProductDetailActivity.EXTRA_KEY_PRODUCT_ID, productId);
//                        extras.putLong(ProductDetailActivity.EXTRA_KEY_SHOP_ID, shopId);
//                        ProductDetailActivity.actionStart(getContext(), extras);
//                    }
//                });
//
//        //新增商品到购物车
//        bridge.registerHandler(JBridgeConf.HANDLE_NAME_SHOPCART_ADD,
//                new WebViewJavascriptBridge.WVJBHandler() {
//                    @Override
//                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
////                        {
////                                "productId": 456,
////                                "productName": "商品名",
////                                "productPrice": 88.88,
////                                "productImageUrl": "商品图片链接",
////                                "shopId": 789
////                        }
//                        ShopcartHelper.getInstance().generateHybridShopcartData(data);
//                    }
//                });
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler {
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
            ZLogger.d("Android [RECV] " + data);
            if (null != responseCallback) {
                responseCallback.callback("i have already received you data.");
            } else {
//                DialogUtil.showHint("Android [RECV] " + data);
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
            ZLogger.d(String.format("shouldOverrideUrlLoading url=%s\ncookie=",
                    url, CookieManager.getInstance().getCookie(url)));

//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

//            url = Uri.decode(url);
//            view.loadUrl(url);//在当前webview中加载页面。

//            HybridActivity.actionStart(getActivity(), url, true, false, -1);
            return true;
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
//        MLog.d("onPageFinished, mCurrentUrl = " + mCurrentUrl);
        if (emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING) {
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }

        setRefreshing(false);
    }


    /**
     * 加载数据
     */
    private void loadData() {
        if (StringUtils.isEmpty(pageUrl)) {
            DialogUtil.showHint("加载错误");
            return;
        }
        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
//        mWebView.loadUrl(pageUrl);
        WebViewUtils.loadUrl(mWebView, pageUrl);
    }

    private void reload() {
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if (mWebView != null) {
            String url = mWebView.getUrl();
            if (TextUtils.isEmpty(url)) {
                //TODO
//                url = MobileURLConf.URL_AUTH_GUIDE;
                return;
            }
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            mWebView.clearHistory();
//            mWebView.loadUrl(url);
            WebViewUtils.loadUrl(mWebView, pageUrl);
        }
    }

    /**
     * 刷新加载更多
     */
    public void refreshToLoadMore() {
        setRefreshing(false);
    }

    /**
     * 设置刷新
     */
    private void setupSwipeRefresh() {
//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.swiperefresh_color1, R.color.swiperefresh_color2,
                    R.color.swiperefresh_color3, R.color.swiperefresh_color4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mState == STATE_REFRESH) {
                        ZLogger.d("正在刷新");
                        return;
                    }

                    refreshToLoadMore();
                }
            });
        }
        mState = STATE_NONE;
    }

    /**
     * 设置刷新状态
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            setSwipeRefreshLoadingState();
        } else {
            setSwipeRefreshLoadedState();
        }
    }


    /**
     * 设置顶部正在加载的状态
     */
    private void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);


            mState = STATE_REFRESH;
        }
    }

    /**
     * 设置顶部加载完毕的状态
     */
    private void setSwipeRefreshLoadedState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);


            mState = STATE_NONE;
        }
    }

    @Override
    public void onTabReselect() {

        ZLogger.d("HomeFragment.onTabReselect");
    }

    @OnClick(R.id.address_view)
    public void changeAddress() {

        //需要处理扫描结果
        UIHelper.startActivity(getActivity(), SelectAddressActivity.class);
    }

    @OnClick(R.id.iv_scanner)
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

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(CommunityEvent event) {
        ZLogger.d(String.format("H5HomeFragment: CommunityEvent(%d)", event.getEventId()));
        //客显
        if (event.getEventId() == CommunityEvent.EVENT_ID_UPDATED) {
            Subdis subdis = event.getSubdis();
            if (subdis != null) {
                mSubdis = subdis;
                mAddressView.setText(mSubdis.getSubdisName());
                reload();
            }
        }
    }

}

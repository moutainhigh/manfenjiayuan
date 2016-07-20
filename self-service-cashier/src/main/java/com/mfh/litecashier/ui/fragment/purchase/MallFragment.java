package com.mfh.litecashier.ui.fragment.purchase;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.TextView;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.observablescrollview.ObservableScrollViewCallbacks;
import com.mfh.framework.uikit.observablescrollview.ScrollState;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.adapter.CommondityCenterCategoryAdapter;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.utils.MfhURLConf;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品中心
 * Created by Nat.ZZN(bingshanguxue) on 15/12/14.
 * */
public class MallFragment extends BaseFragment {
    public static final String EXTRA_KEY_ROOT_URL = "extra_key_root_url";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @Bind(R.id.category_list)
    RecyclerViewEmptySupport categoryRecyclerView;
    @Bind(R.id.empty_category)
    TextView emptyCategoryView;
    private LinearLayoutManager linearLayoutManager;
    private CommondityCenterCategoryAdapter categoryAdapter;

    private boolean mFirstScroll;
    private boolean mDragging;
    private int mBaseTranslationY;
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

    private String mRootUrl;

    public static MallFragment newInstance(Bundle args){
        MallFragment fragment = new MallFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_commodity_center;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Intent intent = getActivity().getIntent();
//        if(intent != null){
//            mRootUrl = intent.getStringExtra(EXTRA_KEY_ROOT_URL);
//        }
        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            mRootUrl = args.getString(EXTRA_KEY_ROOT_URL, "");
        }

        tvHeaderTitle.setText("商品中心");
        initCategoryRecyclerView();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWebView != null){
            mWebView.destroy();
        }
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().finish();
    }

    private void initCategoryRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        categoryRecyclerView.setHasFixedSize(true);
        //添加分割线
        categoryRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        categoryRecyclerView.setEmptyView(emptyCategoryView);
//        orderRecyclerView.init(10);

        categoryAdapter = new CommondityCenterCategoryAdapter(CashierApp.getAppContext(), null);
        categoryAdapter.setOnAdapterLitener(new CommondityCenterCategoryAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TODO,跳转至详情页面
//                refreshGoodsList(orderListAdapter.getCurPosOrder());

                Bundle extras = new Bundle();
                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_PURSHACE_STANDARD_GOODS);
                SimpleActivity.actionStart(getActivity(), extras);
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
//                onLoadFinished();
//                refreshGoodsList(orderListAdapter.getCurPosOrder());
            }
        });
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    /**
     * 初始化webview
     * */
    private void initWebView(){
//        mWebView.setScrollViewCallbacks(mWebViewScrollCallbacks);

        bridge= new WebViewJavascriptBridge(getActivity(), mWebView, new UserServerHandler(), webviewDelegate) ;
        registerHandle();
    }

    /**
     * register native method
     * */
    private void registerHandle(){

    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
            if (null != responseCallback) {
                responseCallback.callback("i have already received you data.");
            }
        }
    }

    WebViewDelegate webviewDelegate = new WebViewDelegate(){

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
     * @param view
     *            WebView
     * @param url
     *            链接地址
     */
    protected void onUrlFinished(WebView view, String url) {
//        mCurrentUrl = url;
//        ZLogger.d("onPageFinished, mCurrentUrl = " + mCurrentUrl);
        if(emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING){
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }

        setRefreshing(false);
    }

    private ObservableScrollViewCallbacks mWebViewScrollCallbacks = new ObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        }

        @Override
        public void onDownMotionEvent() {
            // Workaround: WebView inside a ScrollView absorbs down motion events, so observing
            // down motion event from the WebView is required.
            mFirstScroll = mDragging = true;
        }

        @Override
        public void onUpOrCancelMotionEvent(ScrollState scrollState) {
//            int scrollY = mWebView.getCurrentScrollY();
            if (scrollState == ScrollState.DOWN) {
//                if(scrollY <= 0){
//                    showBannersView();
//                }
//                Intent intent = new Intent(Constants.ACTION_TOGGLE_FLOAT);
//                intent.putExtra(Constants.EXTRA_NAME_FLOAT_ENABLED, true);
//                getActivity().sendBroadcast(intent);
            } else if (scrollState == ScrollState.UP) {
//                Intent intent = new Intent(Constants.ACTION_TOGGLE_FLOAT);
//                intent.putExtra(Constants.EXTRA_NAME_FLOAT_ENABLED, false);
//                getActivity().sendBroadcast(intent);
            } else {
                // Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
            }
        }
    };

    /**
     * 加载数据
     * */
    private void loadData(){
        //加载类目
//        loadCategory();
        readTestData();

        //加载首页
        setRefreshing(true);
        //JSESSIONID
        String pageUrl = MfhURLConf.URL_COMMODITY_CENTER;//MfhURLConf.generateH5Url(mRootUrl, null);

        ZLogger.d("pageUrl:" + pageUrl);
        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
        mWebView.loadUrl(pageUrl);
    }

    /**
     * 重新加载*/
    private void reload(){
        setRefreshing(true);

        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if(mWebView != null){
            String url = mWebView.getUrl();
            if(TextUtils.isEmpty(url)){
                setRefreshing(false);
                return;
            }
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            mWebView.clearHistory();
            mWebView.loadUrl(url);
        }
    }

    /**
     * 设置刷新
     * */
    private void setupSwipeRefresh(){
//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
        if(mSwipeRefreshLayout != null){
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

                    loadData();
                }
            });
        }
        mState = STATE_NONE;
    }

    /**
     * 设置刷新状态
     * */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            setSwipeRefreshLoadingState();
        } else {
            setSwipeRefreshLoadedState();
        }
    }



    /** 设置顶部正在加载的状态 */
    private void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);


            mState = STATE_REFRESH;
        }
    }

    /** 设置顶部加载完毕的状态 */
    private void setSwipeRefreshLoadedState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);

            mState = STATE_NONE;
        }
    }

    /**
     * */
    private void readTestData(){
        List<CashierFunctional> localList = new ArrayList<>();
        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_FEEDPAPER, "走纸", R.mipmap.ic_commoditycenter_01));
        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RETURN_GOODS, "退货", R.mipmap.ic_commoditycenter_02));
        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PAYBACK, "返货", R.mipmap.ic_commoditycenter_04));
        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PACKAGE, "包裹", R.mipmap.ic_commoditycenter_05));
        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_EXPRESS, "寄快递", R.mipmap.ic_commoditycenter_07));
        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_LAUNDRY, "洗衣", R.mipmap.ic_commoditycenter_08));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_MEMBER_CARD, "会员卡", R.mipmap.ic_commoditycenter_09));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RECHARGE, "充值", R.mipmap.ic_service_recharge));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PRIVATE, "我的", R.mipmap.ic_service_private));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_COMMODITY_CENTER, "商品中心", R.mipmap.ic_service_mall));

        categoryAdapter.setEntityList(localList);
    }
}

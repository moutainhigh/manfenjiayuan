package com.mfh.litecashier.ui.fragment;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.GrouponDetail;
import com.mfh.litecashier.ui.dialog.ScanCardDialog;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 团购
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class GrouponFragment extends BaseFragment {
    public static final String EXTRA_KEY_ROOT_URL = "extra_key_root_url";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.webview)
    HybridWebView mWebView;
    @BindView(R.id.error_view)
    EmptyLayout emptyView;

    public static final int STATE_NONE = 0;
    public static final int STATE_REFRESH = 1;
    public static final int STATE_LOADMORE = 2;
    public static final int STATE_NOMORE = 3;
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    public static int mState = STATE_NONE;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.button_groupon)
    Button btnSubmit;

    private ScanCardDialog mScanCardDialog = null;

    private WebViewJavascriptBridge bridge;

    private GrouponDetail grouponDetail;
    private String mRootUrl;

    public static GrouponFragment newInstance(Bundle args){
        GrouponFragment fragment = new GrouponFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_groupon;
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

        mToolbar.setTitle("团购");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });


        initWebView();

        setupSwipeRefresh();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        grouponDetail = new GrouponDetail();
        grouponDetail.setPageUrl("http://www.jd.com/");
        grouponDetail.setName("正宗美国红提三斤装");
        grouponDetail.setPrice(12.8);
        loadData();
        refresh();
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


    private void refresh(){
        if (grouponDetail != null){
            tvTitle.setText(grouponDetail.getName());
        }
    }

    /**
     * 加载数据
     * */
    public void loadData(){
        //加载首页
        setRefreshing(true);
        //JSESSIONID
//        String pageUrl = MfhURLConf.generateUrl(MfhURLConf.URL_REPORT, null);
        String pageUrl = grouponDetail.getPageUrl();

        ZLogger.d("pageUrl:" + pageUrl);
//        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
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
//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
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

    @OnClick(R.id.button_groupon)
    public void submit(){
        btnSubmit.setEnabled(false);
        if (mScanCardDialog == null) {
            mScanCardDialog = new ScanCardDialog(getActivity());
            mScanCardDialog.setCancelable(false);
            mScanCardDialog.setCanceledOnTouchOutside(false);
        }
        mScanCardDialog.init(new ScanCardDialog.DialogViewListener() {
            @Override
            public void onCardDetected(String cardNo) {
                // TODO: 5/25/16 刷卡支付，并参团
                DialogUtil.showHint(cardNo);
                btnSubmit.setEnabled(true);
            }

            @Override
            public void onCancel() {
                btnSubmit.setEnabled(true);
                DialogUtil.showHint("取消参团");
            }
        });
        mScanCardDialog.show();
    }

}

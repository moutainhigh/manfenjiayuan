package com.mfh.buyers.ui.web;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.mfh.buyers.R;
import com.mfh.buyers.fragments.BaseFragment;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;


/**
 * 浏览器
 * */
public class BrowserFragment extends BaseFragment {
    public static final String EXTRA_KEY_REDIRECT_URL = "redirectUrl";
    public static final String EXTRA_KEY_SYNC_COOKIE = "syncCookie";
    //发生链接跳转时，立刻跳转到新的webview
    public static final String EXTRA_KEY_OVERRIDE_URL_ATONCE = "overrideUrlAtOnce";

    @Bind(R.id.webview)
    HybridWebView myWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private Activity activity;
    private String mRootUrl, mCurrentUrl;
    private Map<String, String> titleMap = new HashMap<>();
    private boolean isLocalResource;//是否是本地资源，决定刷新时是加载根目录还是当前目录

    public static final int STATE_NONE = 0;
    public static final int STATE_REFRESH = 1;
    public static final int STATE_LOADMORE = 2;
    public static final int STATE_NOMORE = 3;
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    public static int mState = STATE_NONE;
    @Bind(R.id.swiperefreshlayout) SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean bNeedSyncCookie;//是否需要同步cookie
    private boolean bOverrideAtOnce;//发生链接跳转时，立刻跳转到新的webview


    public interface BrowserListener{
        void onTitleChanged(String title);
    }
    private BrowserListener listener;
    public void setBrowserListener(BrowserListener listener){
        this.listener = listener;
    }


    @Override
    public int getLayoutId() {
        return R.layout.fragment_browser;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();

        Intent intent = getActivity().getIntent();
        if(intent != null){
            mRootUrl = intent.getStringExtra(EXTRA_KEY_REDIRECT_URL);//intent.getExtra().getString("redirectUrl");
            bNeedSyncCookie = intent.getBooleanExtra(EXTRA_KEY_SYNC_COOKIE, false);
            bOverrideAtOnce = intent.getBooleanExtra(EXTRA_KEY_OVERRIDE_URL_ATONCE, false);
        }

        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            mRootUrl = args.getString(EXTRA_KEY_REDIRECT_URL, "");
            bNeedSyncCookie = args.getBoolean(EXTRA_KEY_SYNC_COOKIE, false);
            bOverrideAtOnce = args.getBoolean(EXTRA_KEY_OVERRIDE_URL_ATONCE, false);
        }
        mCurrentUrl = mRootUrl;

        initWebView();

//        emptyView = (EmptyLayout) rootView.findViewById(R.id.error_view);
        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        setupSwipeRefresh();
        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

        titleMap.clear();

        isLocalResource = WebViewUtils.loadUrl(myWebView, mRootUrl);
    }

    @Override
    public void onResume(){
        super.onResume();
        myWebView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        myWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myWebView.destroy();
    }

    //    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {  //表示按返回键 时的操作
//            myWebView.goBack();   //后退
//            return true;    //已处理
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    /**
     * 初始化WebView
     * */
    private void initWebView(){
        try{
//            实例化WebView组件
//            myWebView = (HybridWebView) rootView.findViewById(R.id.webview);

            myWebView.setWebViewClient(new MyWebViewClient());
            myWebView.setWebChromeClient(new MyWebChromeClient());
            //清空WebView缓存
//        myWebView.clearCache(false);

            //佚名内部类处理,点击系统“Back”键，浏览的网页回退而不是退出浏览器。
            myWebView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
//                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    // Check if the key event was the Back button and if there's history
                    if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
                        myWebView.goBack();
                        updateWebTitle();
                        return true;    //已处理
                    }
//                }
                    return false;
                }
            });

            if(bNeedSyncCookie){
                WebViewUtils.syncCookies(getContext(), mCurrentUrl, getCookie());
            }

            //支持javascripte自定义对象
            //webview中js调用本地java方法
            //This is the name of the JavaScript Interface we will pass to the WebView element in the next step
//            myWebView.addJavascriptInterface(new Object(){
//                //TODO, function
////            public void function() {
////                mHandler.post(new Runnable() {
////                    public void run() {
////                        myWebView.loadUrl("javascript:wave()");
////                    }
////                });
////            }
//            }, "对象在js中的别名");
            //
        }
        catch(Exception e){
            Log.e("Nat: initWebView failed", e.getMessage());
        }
    }

    @Override
    public void onPostExecute(int taskKind, Object result, Object... params) {

    }

    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d(String.format("BrowserFragment.shouldOverrideUrlLoading %s", url));

            if(bOverrideAtOnce){
                //TODO
                ComnJBH5Activity.actionStart(getActivity(), url, true, false, 0);
                return true;
            }
            onUrlLoading(view, url);
            //TODO

            boolean flag =  super.shouldOverrideUrlLoading(view, url);
            mCurrentUrl = url;
            isLocalResource = false;
            return flag;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            onUrlFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            emptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            onWebIcon(view, icon);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) { // 进度
            super.onProgressChanged(view, newProgress);
            if (newProgress > 90) {
//                loadingImageView.toggle(false);
            }
        }
    }

    /**
     * 载入链接之前会被调用
     *
     * @param view
     *            WebView
     * @param url
     *            链接地址
     */
    protected void onUrlLoading(WebView view, String url) {
        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

        WebViewUtils.setCookie(url, getCookie());
    }

    private String getCookie(){
        String sessionId = MfhLoginService.get().getCurrentSessionId();
        if(sessionId == null){
            return null;
        }

        return String.format("JSESSIONID=%s; domain=%s; path=/", sessionId, "devmobile.manfenjiayuan.com");
    }

    /**
     * 链接载入成功后会被调用
     *
     * @param view
     *            WebView
     * @param url
     *            链接地址
     */
    protected void onUrlFinished(WebView view, String url) {
        mCurrentUrl = url;
        MLog.d("onPageFinished, mCurrentUrl = " + mCurrentUrl);
        if(emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING){
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }

        setRefreshing(false);
    }

    /**
     * 当前WebView显示页面的标题
     *
     * @param view
     *            WebView
     * @param title
     *            web页面标题
     */
    protected void onWebTitle(WebView view, String title) {
        try{
            // 必须做判断，由于webview加载属于耗时操作，可能会本Activity已经关闭了才被调用
            if (activity != null && myWebView != null) {
//                Log.d("Nat: webView.onReceivedTitle.Url", view.getUrl());
//                Log.d("Nat: webView.onReceivedTitle.OriginalUrl", view.getOriginalUrl());
                //ISSUES: 保存标题，解决当goback后标题不改变问题。
                titleMap.put(myWebView.getUrl(), myWebView.getTitle());

                if (listener != null){
                    listener.onTitleChanged(myWebView.getTitle());
                }
            }
        }catch(Exception e){
            Log.e("Nat: onWebTitle", e.toString());
        }

    }

    /**
     * 当前WebView显示页面的图标
     *
     * @param view
     *            WebView
     * @param icon
     *            web页面图标
     */
    protected void onWebIcon(WebView view, Bitmap icon) {
    }

    /**
     * 更新网页标题
     * */
    private void updateWebTitle(){
        try{
            if(activity != null && myWebView != null){
//            Log.d("Nat: webView.updateWebTitle.Url", myWebView.getUrl());
//            Log.d("Nat: webView.updateWebTitle.OriginalUrl", myWebView.getOriginalUrl());
                String url = myWebView.getOriginalUrl();
                if(url != null && titleMap != null && titleMap.containsKey(url)){
                    if (listener != null){
                        listener.onTitleChanged(titleMap.get(url));
                    }
                }else{
                    if (listener != null){
                        listener.onTitleChanged(myWebView.getTitle());
                    }
                }

//            Log.d("Nat: webView.Title", tvTopBarTitle.getText().toString());
            }
        }
        catch(Exception e){
            Log.e("Nat: updateWebTitle", e.toString());
        }
    }

    private void reload(){
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if(myWebView != null){
            String url = myWebView.getUrl();
            if(TextUtils.isEmpty(url)){
                //TODO
//                url = MobileURLConf.URL_AUTH_GUIDE;
                return;
            }
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            myWebView.clearHistory();
            myWebView.loadUrl(url);
        }
    }

    /**
     * 刷新加载更多
     * */
    public void refreshToLoadMore(){
        if(myWebView != null){
            //下拉刷新加载
            if(isLocalResource){
                setRefreshing(true);
                isLocalResource = WebViewUtils.loadUrl(myWebView, mRootUrl);
            }
            else{
                String url = myWebView.getUrl();
                if(!TextUtils.isEmpty(url) && URLUtil.isNetworkUrl(url)){
                    setRefreshing(true);
                    WebViewUtils.loadUrl(myWebView, url);
//                    myWebView.loadUrl(url);
                }
            }
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

                    refreshToLoadMore();
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

}

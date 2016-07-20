package com.mfh.owner.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.net.URLHelper;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.owner.R;
import com.mfh.owner.ui.activity.NativeWebViewActivity;
import com.mfh.owner.ui.web.HybridHelper;
import com.mfh.owner.utils.MobileURLConf;
import com.mfh.owner.utils.UIHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;


/**
 * 预订
 *
 * @author zhangzn created on 2015-04-13
 * @since Framework 1.0
 */
public class OrderFragment extends BaseFragment{
    private static final String URL_ROOT = MobileURLConf.URL_ME_PACKAGES;

    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.webview)
    HybridWebView myWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private Activity context;
    private String mCurrentUrl = URL_ROOT;
    private Map<String, String> titleMap = new HashMap<>();

    public OrderFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_order;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });

        titleMap.clear();

        mCurrentUrl = MobileURLConf.generateUrl(URL_ROOT, null);

        initTopBar();
        initWebView();

        if (!MfhLoginService.get().haveLogined()){
            UIHelper.sendLoginBroadcast(getActivity());
        }
        else{
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            WebViewUtils.loadUrl(myWebView, mCurrentUrl);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        myWebView.onResume();

        //TODO
//        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        myWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myWebView.destroy();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if(requestCode == IMConstants.ACTIVITY_REQUEST_CHANGE_ORDER)
//        {
////            if(resultCode == Activity.RESULT_OK){
////                refresh();
////            }
//            refresh();
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    /**
     * 初始化TopBar
     * */
    private void initTopBar(){
        tvTopBarTitle.setText(R.string.topbar_title_order);
//        tvTopBarTitle.setText("");
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToHistory();
            }
        });
        setupBackIndicator();
    }

    /**
     * 设置返回按键
     * */
    private void setupBackIndicator(){
//        if(mCurrentUrl.contains(MobileURLConf.URL_ME_PACKAGES)){
        if(!mCurrentUrl.contains(MobileURLConf.URL_ME_PACKAGES_CHANGE)){
            ibBack.setVisibility(View.INVISIBLE);

            com.mfh.owner.utils.UIHelper.sendToggleTabbarBroadcast(getContext(), true);
        }else{
            ibBack.setVisibility(View.VISIBLE);

            com.mfh.owner.utils.UIHelper.sendToggleTabbarBroadcast(getContext(), false);
        }
    }

    private void updateWebTitle(){
        try{
            if(myWebView != null){
//            Log.d("Nat: webView.updateWebTitle.Url", myWebView.getUrl());
//            Log.d("Nat: webView.updateWebTitle.OriginalUrl", myWebView.getOriginalUrl());
                String url = myWebView.getOriginalUrl();
                if(url != null && titleMap != null && titleMap.containsKey(url)){
                    tvTopBarTitle.setText(titleMap.get(url));
                }else{
                    tvTopBarTitle.setText(myWebView.getTitle());
                }

//            Log.d("Nat: webView.Title", tvTopBarTitle.getText().toString());
            }
        }
        catch(Exception e){
            ZLogger.e("updateWebTitle " + e.toString());
        }
    }

     /**
     * 初始化WebView
     * */
    private void initWebView(){
        myWebView.setWebViewClient(new MyWebViewClient());
        myWebView.setWebChromeClient(new MyWebChromeClient());

//        myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //清空WebView缓存
//        myWebView.clearCache(false);
        //佚名内部类处理,点击系统“Back”键，浏览的网页回退而不是退出浏览器。
        //ZZN-20150527 首页只显示静态网页，点击链接跳转到新网页中，所以不需要后退功能。
        myWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
////                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {  //表示按返回键时的操作
                        myWebView.goBack();   //后退
                        updateWebTitle();
                        return true;    //已处理
                    }
////                }
                return false;
            }
        });
//        //屏蔽掉长按事件
//        myWebView.setOnLongClickListener(new View.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View v) {
//                return true;
//            }
//        });
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d(String.format("shouldOverrideUrlLoading.url= %s; cookie=%s", url, CookieManager.getInstance().getCookie(url)));
            //跳转至注册·登录引导页
            if (url.contains(MobileURLConf.URL_AUTH_INDEX)
                    || url.contains(MobileURLConf.URL_AUTH_LOGIN)) {
                com.mfh.owner.utils.UIHelper.sendLoginBroadcast(getActivity());
                return true;
            }

            url = Uri.decode(url);
//            if (url.contains(MobileURLConf.URL_ME_PACKAGES)) {
            onUrlLoading(view, url);
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
//            view.loadUrl(url);//reload,拦截UR，继续在当前页面显示
//            return true;
//            }
//            else {
//                //跳转至新页面打开
////                NativeWebViewActivity.actionStart(getActivity(), url, true, true, false);
//                redirectToNativeWebForResult(url, true, IMConstants.ACTIVITY_REQUEST_CHANGE_ORDER);
//                return true;
//            }

            boolean flag =  super.shouldOverrideUrlLoading(view, url);
            //TODO
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

            ZLogger.d(String.format("onReceivedError errorCode=%d, description=%s, failingUrl=%s",
                    errorCode, description, failingUrl));
            emptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            //保存标题
            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
//            onWebIcon(view, icon);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) { // 进度
            super.onProgressChanged(view, newProgress);
            // if (newProgress == 100) {
            // mProgress.setVisibility(View.GONE);
            // } else {
            // mProgress.setVisibility(View.VISIBLE);
            // mProgress.setProgress(newProgress);
            // }
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
        if (!url.contains(URL_ROOT)){
            ibBack.setVisibility(View.VISIBLE);
        }

        HybridHelper.syncCookies(getContext(), url);
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
        //data:text/html,chromewebdata
        mCurrentUrl = url;
        ZLogger.d("onPageFinished.mCurrentUrl = " + mCurrentUrl);
        if(emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING){
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }

        //修复 网页加载失败后设置失败问题。
        setupBackIndicator();
//            Log.d("Nat: webView.onPageFinished.Url", view.getUrl());
//            Log.d("Nat: webView.onPageFinished.OriginalUrl", view.getOriginalUrl());
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
            if (getActivity() != null && myWebView != null) { // 必须做判断，由于webview加载属于耗时操作，可能会本Activity已经关闭了才被调用
//            Log.d("Nat: webView.onReceivedTitle.Url", view.getUrl());
//            Log.d("Nat: webView.onReceivedTitle.OriginalUrl", view.getOriginalUrl());
            tvTopBarTitle.setText(myWebView.getTitle());

            titleMap.put(myWebView.getUrl(), myWebView.getTitle());//保存标题，解决当goback后标题不改变问题。
            }
        }
        catch(Exception e){
            ZLogger.e("onWebTitle " + e.toString());
        }
    }


    /**
     * 初始化数据
     * */
    public void loadData(){
        mCurrentUrl = URLHelper.append(URL_ROOT, "t", String.valueOf(new Date().getTime()));
        ZLogger.d("mCurrentUrl=" + mCurrentUrl);
        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
//        WebViewUtils.loadUrl(myWebView, mCurrentUrl, URL_ROOT);
        myWebView.loadUrl(mCurrentUrl);
    }

    /**
     * 重新加载数据
     * */
    public void reloadData(){
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if(myWebView != null){
            String url = myWebView.getUrl();
            if (url != null){
                emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
                myWebView.loadUrl(myWebView.getUrl());
            }
        }
        else{
            //TODO
        }
    }


    /**
     * 注意：不能使用getActivity()启动startActivityForResult，直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     * */
    private void redirectToNativeWebForResult(String url, boolean bNeedSyncCookie, int requestCode){
        Intent intent = new Intent(getActivity(), NativeWebViewActivity.class);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_BACKASHOMEUP, false);
        startActivityForResult(intent, requestCode);
    }

    public boolean isRootWeb() {
        return ibBack.getVisibility() != View.VISIBLE;
    }

    public void backToHistory(){
        if (myWebView != null && myWebView.canGoBack()) {
            myWebView.goBack();
            updateWebTitle();
//            mCurrentUrl = myWebView.getUrl();
        }else{
            mCurrentUrl = URL_ROOT;
//            myWebView.loadUrl(mCurrentUrl);
        }

        setupBackIndicator();
    }

}

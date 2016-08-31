package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mfh.enjoycity.R;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewUtils;

import butterknife.Bind;


/**
 * 加载本地HTML文件
 * 软件介绍（图片）/用户协议/帮助
 * */
public class StaticWebActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.webview)
    HybridWebView myWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private static final String EXTRA_URL = "EXTRA_URL";

    public static void actionStart(Activity context, String redirectUrl){
        Intent intent = new Intent(context, StaticWebActivity.class);
        intent.putExtra(EXTRA_URL, redirectUrl);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_native_web;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_about);//必须在setSupportActionBar(toolbar);之前设置才有效
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        //import that this is set first
        setSupportActionBar(toolbar);
        //call setNavigationIcon() to display our back arrow image which we’ll use to navigate back
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StaticWebActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWebView();

        Intent intent = this.getIntent();
        if(intent != null){
            String url = intent.getStringExtra(EXTRA_URL);
            if(!StringUtils.isEmpty(url)){
                myWebView.loadUrl(url);
            }
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

            url = Uri.decode(url);
//            if (url.contains(MobileURLConf.URL_ME_PACKAGES)) {
            onUrlLoading(view, url);
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            view.loadUrl(url);//reload
            return true;
//            }
//            else {
//                //跳转至新页面打开
////                NativeWebViewActivity.actionStart(getActivity(), url, true, true, false);
//                redirectToNativeWebForResult(url, true, OwnerConstants.ACTIVITY_REQUEST_CHANGE_ORDER);
//                return true;
//            }

//            boolean flag =  super.shouldOverrideUrlLoading(view, url);
//            //TODO
//            return flag;
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
       WebViewUtils.syncCookie(StaticWebActivity.this, url);
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
//        mCurrentUrl = url;
        ZLogger.d("onPageFinished.mCurrentUrl = " + url);
        if(emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING){
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }
//            Log.d("Nat: webView.onPageFinished.Url", view.getUrl());
//            Log.d("Nat: webView.onPageFinished.OriginalUrl", view.getOriginalUrl());
    }


}

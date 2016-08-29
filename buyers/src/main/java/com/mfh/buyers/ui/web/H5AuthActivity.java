package com.mfh.buyers.ui.web;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.buyers.R;
import com.mfh.buyers.utils.MobileURLConf;
import com.mfh.buyers.utils.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.hybrid.HtmlManager;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.hybrid.WebViewUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;


/**
 * H5 登录·找回密码·注册
 * @author NAT.ZZN(bingshanguxue)
 * */
public class H5AuthActivity extends BaseActivity {
    public static final String EXTRA_KEY_REDIRECT_URL = "redirectUrl";
    public static final String EXTRA_KEY_SYNC_COOKIE = "syncCookie";

    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private Context context;
    @Bind(R.id.webview_custom)
    HybridWebView myWebView;
    private WebViewJavascriptBridge bridge;

    private String mCurrentUrl = MobileURLConf.URL_AUTH_INDEX;//default
    private Map<String, String> titleMap = new HashMap<>();

    private boolean bNeedSyncCookie;//是否需要同步cookie


    public static void actionStart(Activity context, String redirectUrl){
        actionStart(context, redirectUrl, false, true, false);
    }
    public static void actionStart(Activity context, String redirectUrl, boolean bSyncCookie, boolean bGoBack, boolean bAnimation){
        Intent intent = new Intent(context, H5AuthActivity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
        context.startActivity(intent);

        if(bAnimation){
            //Activity切换动画,缩放+透明
            context.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
    }
    public static void actionStartForResult(Activity context, String redirectUrl){
        actionStartForResult(context, redirectUrl, false, true, false);
    }
    public static void actionStartForResult(Activity context, String redirectUrl, boolean bSyncCookie, boolean bGoBack, boolean bAnimation){
        Intent intent = new Intent(context, H5AuthActivity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
        context.startActivity(intent);

        if(bAnimation){
            //Activity切换动画,缩放+透明
            context.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_base_hybrid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Intent intent = this.getIntent();
        if(intent != null){
            mCurrentUrl = intent.getStringExtra(EXTRA_KEY_REDIRECT_URL);//intent.getExtra().getString("redirectUrl");
            bNeedSyncCookie = intent.getBooleanExtra(EXTRA_KEY_SYNC_COOKIE, false);
        }

        initTopBar();
        initWebView();

//        emptyView = (EmptyLayout) findViewById(R.id.error_view);
        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        titleMap.clear();

        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

        ZLogger.d("mCurrentUrl=" + mCurrentUrl);
//        myWebView.loadUrl(mCurrentUrl);
        WebViewUtils.loadUrl(myWebView, mCurrentUrl);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    public void onResume(){
        super.onResume();
        myWebView.onResume();
    }
    public void onPause(){
        super.onPause();
        myWebView.onPause();
    }

    @Override
    protected void onDestroy() {
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

    private void loadData(){
        String localHtmlData = HtmlManager.getInstance(this).getLocalData("");
        if(localHtmlData != null){
//            Log.d("Nat", "load local html, useLocaleResource = " + useLocaleResource);
            myWebView.loadDataWithBaseURL(HtmlManager.BASE_URL_HOME, localHtmlData,
                    HtmlManager.MIME_TYPE, HtmlManager.ENCODING_UTF8, null);
        }
        else{
//            Log.d("Nat", "load web html, useLocaleResource = " + useLocaleResource);
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            myWebView.loadUrl(mCurrentUrl);
        }
    }
    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
//        tvTopBarTitle = (TextView) findViewById(R.id.topbar_title);
//        ibBack = (ImageButton) findViewById(R.id.ib_back);

        tvTopBarTitle.setText("");
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myWebView != null && myWebView.canGoBack()) {
                    myWebView.goBack();
                    updateWebTitle();
                }else{
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }
        });
        setupBackIndicator();
    }

    /**
     * 设置返回按键
     * */
    private void setupBackIndicator(){
        if(mCurrentUrl.contains(MobileURLConf.URL_AUTH_INDEX)){
            ibBack.setVisibility(View.INVISIBLE);
        }else{
            ibBack.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化WebView
     * */
    private void initWebView(){
        try{
//            myWebView = (HybridWebView) findViewById(R.id.webview_custom);
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
                UIHelper.syncCookies(this, URLConf.URL_DEFAULT);
            }

            bridge= new WebViewJavascriptBridge(this, myWebView, new UserServerHandler(), webviewDelegate) ;
            //保存用户登录信息
            bridge.registerHandler(JBridgeConf.HANDLE_NAME_SAVE_USER_LOGIN_INFO, new WebViewJavascriptBridge.WVJBHandler() {
                @Override
                public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                    AppHelper.saveUserLoginInfo(data);
//                    loginSuccess();
                }
            });
        }
        catch(Exception e){
            ZLogger.e("initWebView failed" +  e.getMessage());
        }
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
            if (context != null && myWebView != null) { // 必须做判断，由于webview加载属于耗时操作，可能会本Activity已经关闭了才被调用

//                Log.d("Nat: webView.onReceivedTitle.Url", view.getUrl());
//                Log.d("Nat: webView.onReceivedTitle.OriginalUrl", view.getOriginalUrl());
                tvTopBarTitle.setText(myWebView.getTitle());

                titleMap.put(myWebView.getUrl(), myWebView.getTitle());//保存标题，解决当goback后标题不改变问题。
            }
        }catch(Exception e){
            ZLogger.e("onWebTitle", e.toString());
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
            ZLogger.e("updateWebTitle", e.toString());
        }
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
            ZLogger.d("Android [RECV] " + data);
            if (null != responseCallback) {
                responseCallback.callback("i have already received you data.");
            }else{
                DialogUtil.showHint("Android [RECV] " + data);
            }
        }
    }

    WebViewDelegate webviewDelegate = new WebViewDelegate(){

        @Override
        public void onPageFinished(WebView view, String url) {
            mCurrentUrl = url;
            ZLogger.d("onPageFinished.mCurrentUrl = " + mCurrentUrl);
            setupBackIndicator();
            if(emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING){
                emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            emptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d(String.format("shouldOverrideUrlLoading url=%s\ncookie=", url, CookieManager.getInstance().getCookie(url)));

            //支持返回键
            if(!url.contains(MobileURLConf.URL_AUTH_INDEX)){
                ibBack.setVisibility(View.VISIBLE);
            }

            //登录成功/重置密码成功
//            if(url.equalsIgnoreCase(MobileURLConf.URL_NATIVIE_REDIRECT_AUTH) || url.startsWith(MobileURLConf.URL_ME)){
            if(url.equalsIgnoreCase(MobileURLConf.URL_NATIVIE_REDIRECT_AUTH)){
                loginSuccess();
                return false;
            }

            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

//            url = Uri.decode(url);
//            view.loadUrl(url);//在当前webview中加载页面。
            return false;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            onWebIcon(view, icon);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress > 90) {
//                loadingImageView.toggle(false);
//                emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }
        }
    };



    /**
     * 切换界面
     * */
    private void loginSuccess(){
        setResult(RESULT_OK);

        this.finish();
    }

    private void loginFailed(){
        setResult(Activity.RESULT_CANCELED);
        DialogUtil.showHint("登录失败");

        this.finish();
    }


    /**
     * 重新加载网页
     * */
    private void reload(){
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if(myWebView != null){
            String url = myWebView.getUrl();
            if(TextUtils.isEmpty(url)){
                url = MobileURLConf.URL_AUTH_INDEX;
            }
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            myWebView.clearHistory();
            myWebView.loadUrl(url);
//            myWebView.reload();
        }
    }

}

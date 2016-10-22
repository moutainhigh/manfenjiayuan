package com.manfenjiayuan.mixicook_vip.ui.hybrid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.manfenjiayuan.business.ui.HybridActivity;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.mobile.Mixicook;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.JBridgeConf;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.URLHelper;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.EmptyLayout;

import butterknife.Bind;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class HybridFragment extends BaseFragment {
    public static final String EXTRA_KEY_ORIGINALURL = "originalUrl";


    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.webview)
    HybridWebView mWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private WebViewJavascriptBridge bridge;

    private String originalUrl;


    public static HybridFragment newInstance(Bundle args){
        HybridFragment fragment = new HybridFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_hybrid;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            originalUrl = args.getString(EXTRA_KEY_ORIGINALURL);
        }

//        mToolbar.setTitle("价签绑定");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_my) {
//                    Bundle extras = new Bundle();
//                    extras.putString(SimpleActivity.EXTRA_TITLE, "我的");
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//                    extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_MY);
//                    Intent intent = new Intent(getActivity(), FragmentActivity.class);
//                    intent.putExtras(extras);
//                    startActivity(intent);
//                }
//                return true;
//            }
//        });
        // Inflate a menu to be displayed in the toolbar
//        mToolbar.inflateMenu(R.menu.menu_home);

        if (StringUtils.isEmpty(originalUrl)){
            originalUrl = "";
        }

        initWebView();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

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


    /**
     * 跳转到url
     */
    private void redirect2Url(String url) {
        ZLogger.d("准备跳转页面: " + url);
        Intent intent = new Intent(getActivity(), HybridActivity.class);
        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL,
                URLHelper.append(url, String.format("ownerId=%d", MfhLoginService.get().getCurrentGuId())));
        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, true);
        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, -1);
        intent.putExtra(HybridActivity.EXTRA_KEY_COOKIE_URL, Mixicook.COOKIE_URL);
        intent.putExtra(HybridActivity.EXTRA_KEY_COOKIE_DOMAIN, Mixicook.DOMAIN);
        startActivity(intent);
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

            if (webviewDelegate != null){
                webviewDelegate.shouldOverrideUrlLoading(view, url);
            }
//
            boolean flag =  super.shouldOverrideUrlLoading(view, url);
////            mCurrentUrl = url;
////            isLocalResource = false;
            return flag;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (webviewDelegate != null){
                webviewDelegate.onPageFinished(view, url);
            }
        }


        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            if (webviewDelegate != null){
                webviewDelegate.onReceivedError(view, errorCode, description, failingUrl);
            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            //Uncaught SyntaxError: Unexpected token var line:1
            if(cm != null){
                DialogUtil.showHint(cm.message());
                ZLogger.d(cm.message() + "-- From line:"+ cm.lineNumber()
                        + " of " + cm.sourceId());
            }else{
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
            ZLogger.d("网页标题: " + title);

            if(webviewDelegate != null){
                webviewDelegate.onReceivedTitle(view, title);
            }
        }


        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            if(webviewDelegate != null){
                webviewDelegate.onReceivedIcon(view, icon);
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) { // 进度
            super.onProgressChanged(view, newProgress);
            ZLogger.d(String.format("网页加载进度 %d%%", newProgress));
            if(webviewDelegate != null){
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
            if (Uri.parse(url).getHost().equals(Mixicook.API_BASE_URL)) {
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
            mToolbar.setTitle(title);
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
     * 初始化
     */
    private void loadInit() {
        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
        WebViewUtils.loadUrl(mWebView, originalUrl);
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


    /**
     * 重新加载
     */
    public void reload() {
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

package com.mfh.framework.hybrid;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.widget.EmptyLayout;

import java.util.HashMap;
import java.util.Map;


/**
 * H5 JavascriptBridge · WebView · 与具体业务相关
 *
 * @author bingshanguxue
 */
public class BaseHybridActivity extends BaseActivity {
    public static final String EXTRA_KEY_REDIRECT_URL = "redirectUrl";
    public static final String EXTRA_KEY_JSBRIDGE_ENABLED = "jsBridgeEnabled";
    public static final String EXTRA_KEY_SYNC_COOKIE = "syncCookie";
    public static final String EXTRA_KEY_BACKASHOMEUP = "backAsHomeUp";

    protected Toolbar toolbar;
    private ProgressBar animProgress;
    private EmptyLayout emptyView;
    protected HybridWebView myWebView;

    private boolean jsBridgeEnabled;//是否使用JSBridge
    private boolean bNeedSyncCookie;//是否需要同步cookie
    private boolean backAsHomeUp = true;//true,关闭网页(默认);false,返回上一页

    private Activity activity;
    protected WebViewJavascriptBridge bridge;

    protected H5ShareEntity mH5ShareEntity;//页面分享数据
    protected String mCurrentUrl;
    private Map<String, String> titleMap = new HashMap<>();


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_base_hybrid;
    }

    @Override
    protected void initViews() {
        super.initViews();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        animProgress = (ProgressBar) findViewById(R.id.animProgress);
        emptyView = (EmptyLayout) findViewById(R.id.error_view);
        myWebView = (HybridWebView) findViewById(R.id.webview_custom);
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle("");
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        setSupportActionBar(toolbar);
//        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!backAsHomeUp && myWebView != null && myWebView.canGoBack()) {
                            myWebView.goBack();
                            updateWebTitle();
                        } else {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }

//                        setResult(RESULT_CANCELED);
//                        finish();
//                        HybridActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();
        super.onCreate(savedInstanceState);

        activity = this;

        initWebView();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        titleMap.clear();

        if (!StringUtils.isEmpty(mCurrentUrl)) {
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            WebViewUtils.loadUrl(myWebView, mCurrentUrl);
        } else {
            DialogUtil.showHint("地址无效");
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        myWebView.onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myWebView != null) {
            myWebView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (myWebView != null) {
                myWebView.destroy();
            }
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
        }
    }

    @Override
    public void onBackPressed() {
        if (!backAsHomeUp && myWebView != null && myWebView.canGoBack()) {
            myWebView.goBack();
            updateWebTitle();
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        switch (requestCode){
//            case com.mfh.framework.Constants.REQUEST_CODE_XIANGCE://相册
//                if(intent != null){
//                    Uri uri = intent.getData();
//                    if(uri != null){
//                        //uri= content://media/external/images/media/61232
//                        //path= /data/data/com.mfh.owner/files/temp/20150706140434.JPEG
//                        ZLogger.d("uri= " + uri);
//                        File imageFile = ImageUtil.uriToCompressFile(HybridActivity.this, uri);
//                        saveImageFile(imageFile);
//                    }
//                }
//                break;
//            case com.mfh.framework.Constants.REQUEST_CODE_CAMERA://相机
//                CameraSessionUtil cameraUtil = ServiceFactory.getService(CameraSessionUtil.class.getName());
////                CameraSessionUtil camera = this.materialController.getCameraUtil();
//                //path= /data/data/com.mfh.owner/files/temp/20150706140510.JPEG
//                File imageFile = cameraUtil.getCameraResultFile(resultCode, intent, this);
//                saveImageFile(imageFile);
//
//                break;
//        }
//
//        super.onActivityResult(requestCode, resultCode, intent);
//    }

    //    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {  //表示按返回键 时的操作
//            myWebView.goBack();   //后退
//            return true;    //已处理
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    /**
     * */
    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            mCurrentUrl = intent.getStringExtra(EXTRA_KEY_REDIRECT_URL);
            jsBridgeEnabled = intent.getBooleanExtra(EXTRA_KEY_JSBRIDGE_ENABLED, true);
            bNeedSyncCookie = intent.getBooleanExtra(EXTRA_KEY_SYNC_COOKIE, false);
            backAsHomeUp = intent.getBooleanExtra(EXTRA_KEY_BACKASHOMEUP, true);
            animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }

        //TODO,
        //setTheme必须放在onCreate之前执行，后面执行是无效的
        setTheme();
    }

    /**
     * 初始化WebView
     */
    private void initWebView() {
        try {
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

//            myWebView.loadUrl("file:///android_asset/XX.html");//加载asset文件夹网页文件
//            myWebView.setEnabled(true);
//            myWebView.setActivated(true);

            if (bNeedSyncCookie) {
                WebViewUtils.syncCookies(this, MfhApi.URL_DEFAULT);
            }

            if (jsBridgeEnabled) {
                registerHandle();
            } else {
                this.myWebView.setWebViewClient(new MyWebViewClient());
                this.myWebView.setWebChromeClient(new MyWebChromeClient());     //optional, for show console and alert
            }
        } catch (Exception e) {
            ZLogger.e(e.getMessage());
        }
    }

    WebViewDelegate webviewDelegate = new WebViewDelegate() {

        @Override
        public void onPageFinished(WebView view, String url) {
            mCurrentUrl = url;
            ZLogger.d("onPageFinished.mCurrentUrl = " + mCurrentUrl);
            if (emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING) {
                emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            ZLogger.d(String.format("onReceivedError errorCode=%d, description=%s, failingUrl=%s",
                    errorCode, description, failingUrl));
            emptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d(String.format("shouldOverrideUrlLoading url=%s\ncookie=",
                    url, CookieManager.getInstance().getCookie(url)));

            if (url.equalsIgnoreCase(H5Api.URL_NATIVIE_REDIRECT_AUTH)) {
                setResult(RESULT_OK);
                finish();
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
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
//            if (newProgress > 90) {
////                loadingImageView.toggle(false);
////                emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            }
        }
    };


    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            Log.d(TAG, "onPageFinished");
            if (webviewDelegate != null) {
                webviewDelegate.onPageFinished(view, url);
            }
        }

        /*慎重在shouldoverrideurlloading中返回true
        当设置了WebviewClient时，在shouldoverrideurlloading中如果不需要对url进行拦截做处理，而是简单的继续加载此网址。
        则建议采用返回false的方式而不是loadUrl的方式进行加载网址。
        因为如果采用loadUrl的方式进行加载，那么对于加载有跳转的网址时，进行webview.goBack就会特别麻烦。
        例如加载链接如下：
        A->(B->C->D)->E 括号内为跳转
        如果采用return false的方式，那么在goBack的时候，可以从第二步直接回到A网页。从E回到A只需要执行两次goBack
        而如果采用的是loadUrl，则没办法直接从第二步回到A网页。因为loadUrl把第二步的每个跳转都认为是一个新的网页加载，因此从E回到A需要执行四次goBack

        只有当不需要加载网址而是拦截做其他处理，如拦截tel:xxx等特殊url做拨号处理的时候，才应该返回true。*/
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (webviewDelegate != null) {
                boolean innerFlag = webviewDelegate.shouldOverrideUrlLoading(view, url);
                if (innerFlag) {
                    return true;
                }
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                boolean flag = super.shouldOverrideUrlLoading(view, url);
                return flag;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (webviewDelegate != null) {
                webviewDelegate.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        /*访问证书有问题的SSL网页

        对于有证书问题的网页，比如过期、信息不正确、发行机关不被信任等，Webview默认情况下会拒绝访问。而PC端浏览器的处理则是提供用户进行选择是否要继续，在android也是可以实现的。
        首先第一种是直接继续，不需要让用户进行选择
        这里要注意的是，千万不要调用super的onReceivedSslError方法，因为此方法中已经调用了handler.cancel()。
        如果调用了，则会出现第一次无法加载，第二次却能正常访问的现象。*/
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // 默认为调用handler.cancel()方法，因此不要调用super的onReceivedSslError方法
            // super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }

    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            //Uncaught SyntaxError: Unexpected token var line:1
            if (cm != null) {
                ZLogger.d("Console " + cm.message() + " line:" + cm.lineNumber() + " sourceId:" + cm.sourceId());
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
            DialogUtil.showHint(message);
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (webviewDelegate != null) {
                webviewDelegate.onProgressChanged(view, newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
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
    }


    /**
     * 当前WebView显示页面的标题
     *
     * @param view  WebView
     * @param title web页面标题
     */
    protected void onWebTitle(WebView view, String title) {
        try {
            // 必须做判断，由于webview加载属于耗时操作，可能会本Activity已经关闭了才被调用
            if (activity != null && myWebView != null) {

//                Log.d("Nat: webView.onReceivedTitle.Url", view.getUrl());
//                Log.d("Nat: webView.onReceivedTitle.OriginalUrl", view.getOriginalUrl());
                toolbar.setTitle(myWebView.getTitle());

                //保存标题，解决当goback后标题不改变问题。
                titleMap.put(myWebView.getUrl(), myWebView.getTitle());
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

    }

    private void updateWebTitle() {
        try {
            if (activity != null && myWebView != null) {
//            Log.d("Nat: webView.updateWebTitle.Url", myWebView.getUrl());
//            Log.d("Nat: webView.updateWebTitle.OriginalUrl", myWebView.getOriginalUrl());
                String url = myWebView.getOriginalUrl();
                if (url != null && titleMap != null && titleMap.containsKey(url)) {
                    toolbar.setTitle(titleMap.get(url));
                } else {
                    toolbar.setTitle(myWebView.getTitle());
                }

//            Log.d("Nat: webView.Title", tvTopBarTitle.getText().toString());
            }
        } catch (Exception e) {
            ZLogger.e("updateWebTitle" + e.toString());
        }
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler {
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
            ZLogger.d("Android [RECV] " + data);
            if (null != responseCallback) {
                responseCallback.callback("i have already received you data.");
            } else {
                DialogUtil.showHint("Android [RECV] " + data);
            }
        }
    }

    /**
     * register native method
     */
    protected void registerHandle() {
        bridge = new WebViewJavascriptBridge(this, myWebView, new UserServerHandler(), webviewDelegate);
        //选择图片
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_SELECT_PICTURE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        ZLogger.d("H5 call " + JBridgeConf.HANDLE_NAME_SELECT_PICTURE);
                        UIHelper.selectPicture(BaseHybridActivity.this, "选择图片");
                    }
                });

        //打开浏览器
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_OPEN_BROWSER,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        ZLogger.d("H5(openBrowser):" + data);
                        UIHelper.openBrowser(BaseHybridActivity.this, data);
                    }
                });

        //拨打电话
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_CALL_PHONE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        ZLogger.d("H5(callPhone):" + data);
                        UIHelper.callPhone(BaseHybridActivity.this, data);
                    }
                });
        //分享
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_NATIVE_SHARE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        if (StringUtils.isEmpty(data)) {
                            return;
                        }

                        try {
                            ZLogger.d("H5(nativeShare):" + data);
//                                H5ShareEntity temp = new H5ShareEntity();
//                                JSONObject jsonObject = JSON.parseObject(data);
//                                temp.setCurrentUrl(jsonObject.getString("currentUrl"));//当前页Url，用于校验
//                                temp.setShareUrl(jsonObject.getString("shareUrl"));//分享页Url
//                                temp.setTitle(jsonObject.getString("title"));//标题
//                                temp.setDescription(jsonObject.getString("description"));//描述
//                                temp.setImageUrl(jsonObject.getString("imageUrl"));//图片Url

                            mH5ShareEntity = JSON.toJavaObject(JSONObject.parseObject(data), H5ShareEntity.class);
                        } catch (Exception e) {
                            ZLogger.e(e.toString());
                        }
                    }
                });

    }

    /**
     * 重新加载网页
     */
    private void reload() {
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if (myWebView != null) {
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            myWebView.clearHistory();
            myWebView.loadUrl(myWebView.getUrl());
//            myWebView.reload();
        }
    }

}

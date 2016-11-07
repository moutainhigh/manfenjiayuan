package com.mfh.enjoycity.ui.web;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.manfenjiayuan.business.bean.AccountPayResponse;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.enjoycity.AppHelper;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.SharePopupAdapter;
import com.mfh.enjoycity.bean.SharePopupData;
import com.mfh.enjoycity.ui.activity.MainActivity;
import com.mfh.enjoycity.utils.AlipayConstants;
import com.mfh.enjoycity.utils.EnjoycityApi;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.wxapi.WXHelper;
import com.mfh.enjoycity.wxapi.WXUtil;
import com.mfh.framework.Constants;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApiImpl;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.pay.AppPrePayRsp;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.api.pay.PayApiImpl;
import com.mfh.framework.api.pay.PreOrderRsp;
import com.mfh.framework.api.payOrder.PayOrderApiImpl;
import com.mfh.framework.api.pmcstock.PmcStockApiImpl;
import com.mfh.framework.core.camera.CameraSessionUtil;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.BitmapUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.ImageUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.hybrid.H5ShareEntity;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.JBridgeConf;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.pay.alipay.AlipayUtil;
import com.mfh.framework.pay.alipay.Base64;
import com.mfh.framework.pay.alipay.PayResult;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ShareDialog;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;


/**
 * 浏览器
 * */
public class BrowserFragment extends BaseFragment {
    public static final String EXTRA_KEY_REDIRECT_URL       = "redirectUrl";
    public static final String EXTRA_KEY_JSBRIDGE_ENABLED   = "jsBridgeEnabled";
    public static final String EXTRA_KEY_SYNC_COOKIE        = "syncCookie";
    public static final String EXTRA_KEY_GOBACK_ENABLED     = "isGoBackEnabled";
    //发生链接跳转时，立刻跳转到新的webview
    public static final String EXTRA_KEY_OVERRIDE_URL_ATONCE = "overrideUrlAtOnce";

    @Bind(R.id.webview)
    HybridWebView myWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    public static final int STATE_NONE = 0;
    public static final int STATE_REFRESH = 1;
    public static final int STATE_LOADMORE = 2;
    public static final int STATE_NOMORE = 3;
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    public static int mState = STATE_NONE;
    @Bind(R.id.swiperefreshlayout) SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean jsBridgeEnabled;//是否使用JSBridge
    private boolean bNeedSyncCookie;//是否需要同步cookie
    private boolean bOverrideAtOnce;//发生链接跳转时，立刻跳转到新的webview
    private boolean isGoBackEnabled;//按下返回按键是否回退

    private Activity activity;
    protected String mRootUrl, mCurrentUrl;
    private Map<String, String> titleMap = new HashMap<>();
    private boolean isLocalResource;//是否是本地资源，决定刷新时是加载根目录还是当前目录

    protected WebViewJavascriptBridge bridge;
    private H5ShareEntity mH5ShareEntity;//保存当前页面

    public static BrowserFragment newInstance(String url, boolean bOverrideAtOnce) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(BrowserFragment.EXTRA_KEY_REDIRECT_URL, url);
        args.putBoolean(BrowserFragment.EXTRA_KEY_OVERRIDE_URL_ATONCE, bOverrideAtOnce);
        fragment.setArguments(args);
        return fragment;
    }

    public interface BrowserListener{
        void onTitleChanged(String title);
    }
    private BrowserListener listener;
    public void setBrowserListener(BrowserListener listener){
        this.listener = listener;
    }


    @Override
    public int getLayoutResId() {
        return R.layout.fragment_browser;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();

        Intent intent = getActivity().getIntent();
        if(intent != null){
            mRootUrl = intent.getStringExtra(EXTRA_KEY_REDIRECT_URL);//intent.getExtra().getString("redirectUrl");
            jsBridgeEnabled = intent.getBooleanExtra(EXTRA_KEY_JSBRIDGE_ENABLED, false);
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

        titleMap.clear();

        load();
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
        if(myWebView != null){
            myWebView.destroy();
        }
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
                HybridHelper.syncCookies(getContext(), mCurrentUrl);
            }

            if(jsBridgeEnabled){
                registerHandle();
            }else{
                this.myWebView.setWebViewClient(new MyWebViewClient());
                this.myWebView.setWebChromeClient(new MyWebChromeClient());     //optional, for show console and alert
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {switch (requestCode){
        case Constants.REQUEST_CODE_XIANGCE://相册
            if(data != null){
                Uri uri = data.getData();
                if(uri != null){
                    //uri= content://media/external/images/media/61232
                    //path= /data/data/com.mfh.owner/files/temp/20150706140434.JPEG
                    ZLogger.d("uri= " + uri);
                    File imageFile = ImageUtil.uriToCompressFile(getActivity(), uri);
                    saveImageFile(imageFile);
                }
            }
            break;
        case Constants.REQUEST_CODE_CAMERA://相机
            CameraSessionUtil cameraUtil = ServiceFactory.getService(CameraSessionUtil.class.getName());
//                CameraSessionUtil camera = this.materialController.getCameraUtil();
            //path= /data/data/com.mfh.owner/files/temp/20150706140510.JPEG
            File imageFile = cameraUtil.getCameraResultFile(data, getActivity());
            saveImageFile(imageFile);

            break;
    }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
            ZLogger.d("Android [RECV] " + data);
            if (null != responseCallback) {
                responseCallback.callback("i have already received you data.");
            }else{
//                DialogUtil.showHint("Android [RECV] " + data);
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
            ZLogger.d(String.format("shouldOverrideUrlLoading url=%s\ncookie=",
                    url, CookieManager.getInstance().getCookie(url)));

            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);


//            HybridActivity.actionStart(getActivity(), url, true, false, -1);
//            url = Uri.decode(url);
//            view.loadUrl(url);//在当前webview中加载页面。
            return true;
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

    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d( String.format("BrowserFragment.shouldOverrideUrlLoading %s", url));

            if(bOverrideAtOnce){
                //TODO
                HybridActivity.actionStart(getActivity(), url, true, false, 0);
                return true;
            }

            if (webviewDelegate != null){
                webviewDelegate.shouldOverrideUrlLoading(view, url);
            }

            boolean flag =  super.shouldOverrideUrlLoading(view, url);
            mCurrentUrl = url;
            isLocalResource = false;
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
                ZLogger.d(cm.message()
                        + "-- From line:"+ cm.lineNumber()
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
            ZLogger.d(String.format("url=%s, message=%s"));
            DialogUtil.showHint(message);
            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
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
            if(webviewDelegate != null){
                webviewDelegate.onProgressChanged(view, newProgress);
            }
        }
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
        ZLogger.d("mCurrentUrl = " + mCurrentUrl);
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

    /**
     * 加载数据
     */
    private void load() {
        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
        isLocalResource = WebViewUtils.loadUrl(myWebView, mRootUrl);
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

    /**
     * register native method
     * */
    protected void registerHandle(){
        bridge= new WebViewJavascriptBridge(getActivity(), myWebView, new UserServerHandler(), webviewDelegate) ;
        //选择图片
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_SELECT_PICTURE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        ZLogger.d("H5 call " + JBridgeConf.HANDLE_NAME_SELECT_PICTURE);
                        UIHelper.selectPicture(getActivity(), "选择图片");
                    }
                });

        //打开浏览器
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_OPEN_BROWSER,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        ZLogger.d("H5(openBrowser):" + data);
                        UIHelper.openBrowser(getActivity(), data);
                    }
                });

        //保存用户登录信息
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_SAVE_USER_LOGIN_INFO,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        AppHelper.saveUserLoginInfo(data);
                    }
                });

        //充值--支付宝/微信
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_NATIVE_PAY,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        JSONObject jsonObject = JSON.parseObject(data);
                        int wayType = jsonObject.getInteger("wayType");//充值方式
                        String amount = jsonObject.getString("amount");//金额
                        prepay(wayType, amount);
                    }
                });
        //订单支付--支付宝/微信
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_NATIVE_PAY_ORDER,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        JSONObject jsonObject = JSON.parseObject(data);
                        int wayType = jsonObject.getInteger("wayType");//充值方式
                        int btype = jsonObject.getInteger("btype");//业务类型
                        String orderIds = jsonObject.getString("orderIds");//订单id,多个以英文,隔开
                        prePayOrder(wayType, btype, orderIds);
                    }
                });
        //分享
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_NATIVE_SHARE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        try{
                            ZLogger.d("H5(nativeShare):" + data);
                            if(!TextUtils.isEmpty(data)){
                                H5ShareEntity temp = new H5ShareEntity();

                                JSONObject jsonObject = JSON.parseObject(data);
                                temp.setCurrentUrl(jsonObject.getString("currentUrl"));//当前页Url，用于校验
                                temp.setShareUrl(jsonObject.getString("shareUrl"));//分享页Url
                                temp.setTitle(jsonObject.getString("title"));//标题
                                temp.setDescription(jsonObject.getString("description"));//描述
                                temp.setImageUrl(jsonObject.getString("imageUrl"));//图片Url

                                mH5ShareEntity = temp;
                            }
                        }
                        catch(Exception e){
                            ZLogger.e(e.toString());
                        }

                    }
                });
    }

    private void saveImageFile(final File file){
        new Thread(){
            @Override
            public void run() {
//                super.run();
                try{
                    if(file != null){
                        ZLogger.d("imageFile.path= " + file.getPath());
                        Bitmap bmp = ImageUtil.loadImgThumbnail(file.getPath(), 240, 160);
                        if(bmp != null){
//                            ivSelect.setImageBitmap(bmp);
                            ZLogger.e("encod bmp to base64");
                            final String base64Str = Base64.encode(BitmapUtils.bmpToByteArray(bmp,
                                    Bitmap.CompressFormat.JPEG,
                                    true));
                            ZLogger.e(String.format("base64Str =(%d) %s ", base64Str.length(), base64Str));
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.add(base64Str);
//                            jsonArray.add("data 2");
                            ZLogger.e(String.format("jsonArray =(%d) %s ", jsonArray.size(), jsonArray.toString()));

                            bridge.callHandler(JBridgeConf.HANDLE_NAME_SUBMIT_IMAGE, jsonArray.toString(),
                                    new WebViewJavascriptBridge.WVJBResponseCallback() {
                                        @Override
                                        public void callback(String responseData) {
                                            ZLogger.d("H5 response! " + responseData);
//                                                DialogUtil.showHint("H5 response! " + responseData);
                                            if (responseData.equalsIgnoreCase(base64Str)) {
                                                DialogUtil.showHint("数据完整");
                                            } else {
                                                DialogUtil.showHint("数据不完整");
                                            }
                                        }
                                    });
                        }
                    }
                }
                catch(Exception e){
                    ZLogger.e("saveImageFile failed, " + e.toString());
                }
            }
        }.start();
    }

    /**
     * 显示更多菜单
     * */
    private void showSharePopup(View parentView){
        try{
            int parentViewMeasuredWidth = parentView.getMeasuredWidth() / 3;
            int parentViewMeasuredHeight = parentView.getMeasuredHeight();
            int offsetX = parentViewMeasuredWidth * 2 - 16;
            int offsetY = 0;

            View contentView = LayoutInflater.from(getContext()).inflate(R.layout.popup_listview_share, null);

            final PopupWindow popupWindow = new PopupWindow(contentView, parentViewMeasuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            popupWindow.update();

            popupWindow.setFocusable(true);// 使其获取焦点
            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.setBackgroundDrawable(new BitmapDrawable());

            ListView menuList = (ListView) contentView.findViewById(R.id.listview_popup_share);
            // 加载数据
            List<SharePopupData> menus = new ArrayList<>();
            menus.add(new SharePopupData("首页", R.drawable.actionbar_home_white, SharePopupData.TAG_HOME));
            if(mH5ShareEntity != null){
                String sharePageUrl = mH5ShareEntity.getCurrentUrl();
                String webviewUrl = myWebView.getUrl();
                if(sharePageUrl != null && webviewUrl != null && sharePageUrl.equalsIgnoreCase(webviewUrl)){
                    menus.add(new SharePopupData("分享", R.drawable.actionbar_share_white, SharePopupData.TAG_SHARE));
//                menus.add(new SharePopupData("分享", R.drawable.actionbar_share_white, SharePopupData.TAG_SHARE));
                }
            }
            final SharePopupAdapter menuAdapter = new SharePopupAdapter(getContext(), menus);
            menuList.setAdapter(menuAdapter);
            menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        int position, long id) {
                    SharePopupData popupData = (SharePopupData) adapterView.getAdapter().getItem(position);
                    if (popupData != null) {
                        switch (popupData.getTag()) {
                            case SharePopupData.TAG_HOME: {
                                //跳转到首页
                                MainActivity.actionStart(getContext(), null);
                                getActivity().finish();
                            }
                            break;
                            case SharePopupData.TAG_SHARE: {
                                handleShare();
                            }
                            break;
                        }
                    }

                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                }
            });

            int[] location = new int[2];
            parentView.getLocationOnScreen(location);
            popupWindow.getContentView().measure(parentViewMeasuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.getContentView().setBackgroundResource(R.drawable.share_popup_background);

            //Display the content view in a popup window at the specified location.
            popupWindow.showAsDropDown(parentView, offsetX, offsetY);
        }
        catch(Exception e1){
            ZLogger.e(e1.toString());
        }
    }

    /**
     * 分享
     * */
    private void handleShare(){
        final ShareDialog dialog = new ShareDialog(getContext());
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(R.string.dialog_title_share_to);
        dialog.setOnPlatformClickListener(new ShareDialog.OnSharePlatformClick() {
            @Override
            public void onPlatformClick(int id) {
                if (id == R.id.ly_share_weichat) {
                    if(mH5ShareEntity != null){
                        WXHelper.getInstance(getContext())
                                .sendWebpageToWX(mH5ShareEntity.getShareUrl(), mH5ShareEntity.getTitle(),
                                        mH5ShareEntity.getDescription(), mH5ShareEntity.getImageUrl(),
                                        SendMessageToWX.Req.WXSceneTimeline);
                    }
                    else{
                        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                        WXHelper.getInstance(getContext())
                                .sendWebpageToWX(mCurrentUrl, "满分家园", "品质新生活", thumb,
                                        SendMessageToWX.Req.WXSceneTimeline);
                    }
                } else if (id == R.id.ly_share_weichat_circle) {
                    if(mH5ShareEntity != null){
                        WXHelper.getInstance(getContext())
                                .sendWebpageToWX(mH5ShareEntity.getShareUrl(), mH5ShareEntity.getTitle(),
                                        mH5ShareEntity.getDescription(), mH5ShareEntity.getImageUrl(),
                                        SendMessageToWX.Req.WXSceneSession);
                    }
                    else{
                        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                        WXHelper.getInstance(getContext())
                                .sendWebpageToWX(mCurrentUrl, "满分家园", "品质新生活", thumb,
                                        SendMessageToWX.Req.WXSceneSession);
                    }
                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 预支付
     * @param amount 支付金额：单位为元，最小金额为0.01元。
     * */
    private void prepay(int wayType, final String amount){
//        emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
//        animProgress.setVisibility(View.VISIBLE);

        if(!NetworkUtils.isConnect(getContext())){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        if(TextUtils.isEmpty(amount)){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }

        if(wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY){
            //回调
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);

                            orderPayFailed(-1);
                        }

                        @Override
                        public void processResult(IResponseData rspData) {
//                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            //商户网站唯一订单号
                            String outTradeNo = retValue.getValue();
                            ZLogger.d("prePayResponse: " + outTradeNo);
                            if(!TextUtils.isEmpty(outTradeNo)){
                                //支付宝充值
                                alipay("满分家园账单充值", "支付宝充值", amount, outTradeNo,
                                        EnjoycityApi.ALIPAY_NOTIFY_URL, null);
                            }
                            else{
                                orderPayFailed(-1);
                                DialogUtil.showHint("outTradeNo 不能为空");
                            }
                        }
                    }
                    , String.class
                    , MfhApplication.getAppContext())
            {
            };

            PayOrderApiImpl.prePay(MfhLoginService.get().getCurrentGuId(), amount, wayType,
                    WXUtil.genNonceStr(), responseCallback);
        }
        else if(wayType == EnjoycityApiProxy.WAYTYPE_WXPAY){
            //回调
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<AppPrePayRsp,
                    NetProcessor.Processor<AppPrePayRsp>>(
                    new NetProcessor.Processor<AppPrePayRsp>() {
                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);

                            orderPayFailed(-1);
                        }

                        @Override
                        public void processResult(IResponseData rspData) {
//                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<AppPrePayRsp> retValue = (RspBean<AppPrePayRsp>) rspData;
                            AppPrePayRsp prePayResponse = retValue.getValue();
                            ZLogger.d("prePayResponse: " + prePayResponse.toString());
                            String prepayId = prePayResponse.getPrepayId();

                            if(prepayId != null){
                                WXHelper.getInstance(getContext()).sendPayReq(prepayId);
                            }else {
                                orderPayFailed(-1);
                                DialogUtil.showHint("prepayId 不能为空");
                            }
                        }
                    }
                    , AppPrePayRsp.class
                    , MfhApplication.getAppContext())
            {
            };

            PayOrderApiImpl.prePayForApp(PayApi.WEPAY_CONFIGID_ENJOYCITY,
                    MfhLoginService.get().getCurrentGuId(), amount, wayType,
                    WXUtil.genNonceStr(), BizType.RECHARGE, responseCallback);
        }else{
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }
    }

    /**
     * 预支付订单
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * */
    private void prePayOrder(final int wayType, final int btype, final String orderIds){
//        emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
//        animProgress.setVisibility(View.VISIBLE);

        if(!NetworkUtils.isConnect(getContext())){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        if(TextUtils.isEmpty(orderIds)){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<PreOrderRsp,
                NetProcessor.Processor<PreOrderRsp>>(
                new NetProcessor.Processor<PreOrderRsp>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        orderPayFailed(-1);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        RspBean<PreOrderRsp> retValue = (RspBean<PreOrderRsp>) rspData;
                        PreOrderRsp prePayResponse = retValue.getValue();
                        ZLogger.d("prePayResponse: " + prePayResponse.toString());
                        //商户网站唯一订单号
                        String outTradeNo = prePayResponse.getId();
                        String token = prePayResponse.getToken();
                        if(!TextUtils.isEmpty(outTradeNo)){
//                                amount=1.0id=138750token=501903prepayId=nullsign=null
                            if(wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY){
                                orderPayData.clear();
                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS, orderIds);
                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_TOKEN, token);
                                ZLogger.d("orderPayData: " + orderPayData.toString());
//                                {btype=3, token=257052, orderIds=138756, preOrderId=138757}

                                //支付宝
                                alipay("商品名称", "商品详情", prePayResponse.getAmount(),
                                        outTradeNo, EnjoycityApi.ALIPAY_ORDER_NOTIFY_URL, token);
                            }
                            else if(wayType == EnjoycityApiProxy.WAYTYPE_WXPAY){
                                String prepayId = prePayResponse.getPrepayId();
                                if(prepayId != null){
                                    orderPayData.clear();
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS, orderIds);
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_TOKEN, token);
                                    ZLogger.d("orderPayData: " + orderPayData.toString());

                                    WXHelper.getInstance(getContext()).sendPayReq(prepayId);
                                }else {
                                    orderPayFailed(-1);
                                    DialogUtil.showHint("prepayId 不能为空");
                                }
                            }
                        }
                        else{
                            orderPayFailed(-1);
                            DialogUtil.showHint("outTradeNo 不能为空");
                        }
                    }
                }
                , PreOrderRsp.class
                , MfhApplication.getAppContext())
        {
        };

        PmcStockApiImpl.prePayOrder(MfhLoginService.get().getCurrentGuId(),
                orderIds, btype, wayType, WXUtil.genNonceStr(), responseCallback);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     *
     * 系统繁忙，请稍后再试（ALI64）
     */
    public void alipay(final String subject, final String body, final String amount,
                       final String outTradeNo, final String notifyUrl, final String token) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(getActivity());
                // 调用支付接口，获取支付结果
                String payInfo = AlipayUtil.genPayInfo(AlipayConstants.PARTNER, AlipayConstants.SELLER,
                        AlipayConstants.RSA_PRIVATE,subject, body, amount, outTradeNo, notifyUrl, token);
                String result = alipay.pay(payInfo, true);
                // 解析结果
//                parseAlipayResp(result);
                //resultStatus={6001};memo={操作已经取消。};result={}
                Message msg = new Message();
                msg.what = ALI_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        });
        thread.start();
    }

    private static final int ALI_PAY_FLAG = 1;
    private static final int ALI_CHECK_FLAG = 2;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALI_PAY_FLAG: {
//                    animProgress.setVisibility(View.GONE);
//                    emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

                    parseAlipayResp((String) msg.obj);
                    break;
                }
                case ALI_CHECK_FLAG: {
                    DialogUtil.showHint("检查结果为：" + msg.obj);
                    break;
                }
                default:
                    break;
            }
        }
    };


    /**
     * 解析支付宝处理结果
     * */
    private void parseAlipayResp(String resp){
        PayResult payResult = new PayResult(resp);
//        resultStatus={9000};memo={};result={partner="2088011585033309"&seller_id="finance@manfenjiayuan.com"&out_trade_no="138761"&subject="商品名称"&body="商品详情"&total_fee="0.01"&notify_url="http://devnew.manfenjiayuan.com/pmc/pmcstock/notifyOrder"&service="mobile.securitypay.pay"&payment_type="1"&_input_charset="utf-8"&it_b_pay="30m"&return_url="m.alipay.com"&success="true"&sign_type="RSA"&sign="OoNoZHMgXQ81Irh/DnCjEhfaEuL5lIqjxCgs05+gV/oIUUqjMffmeRf4fPuXwVsC4XpjQjdNLnCLgXqfIvpAYdt3bqDXEGV1BojgEJl1bz8HCrvT8YIAgPMY/0S9qzCDwuMNcDhcTo2dilK2isUE5AD1MjYtgmtEIWG3WDJNqIA="}
        ZLogger.d("parseAlipayResp: " + payResult.toString());

        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
        String resultInfo = payResult.getResult();
        String resultStatus = payResult.getResultStatus();

        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
        if (TextUtils.equals(resultStatus, "9000")) {
            processOrder(EnjoycityApiProxy.WAYTYPE_ALIPAY);
        } else {
            // 判断resultStatus 为非“9000”则代表可能支付失败
            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                processOrder(EnjoycityApiProxy.WAYTYPE_ALIPAY);
//                if(BizConfig.DEBUG){
//                    DialogUtil.showHint("支付结果确认中");
//                }
            }
            else if (TextUtils.equals(resultStatus, "6001")) {
                notifyPayResult(-2);
                DialogUtil.showHint("支付取消");
            }else {
                notifyPayResult(-1);
                //6001,支付取消
                //6002,网络连接出错
                //4000,支付失败
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                DialogUtil.showHint("支付失败");
            }
        }
    }

    public void parseWxpayResp(int errorCode, String errStr){
        try{
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

            switch(errorCode){
                //成功，展示成功页面
                case 0:{
                    //如果支付成功则去后台查询支付结果再展示用户实际支付结果。注意一定不能以客户端
                    // 返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
                    processOrder(EnjoycityApiProxy.WAYTYPE_WXPAY);
                }
                break;
                //错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                case -1:{
                    notifyPayResult(-1);
                    DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", errorCode, (errStr != null ? errStr : "")));

                }
                break;
                //用户取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
                case -2:{
                    notifyPayResult(-2);

                    DialogUtil.showHint("取消微信充值");
                }
            }
        }
        catch (Exception e){
            ZLogger.e("parseWxpayResp failed, " + e.toString());
        }
    }

    private Map<String, String> orderPayData = new HashMap<>();
    /**
     * 处理订单
     * 微信/支付宝支付成功后，调用满分后台支付接口，处理订单。
     * */
    private void processOrder(final int wayType){
        if (orderPayData.isEmpty()){
            notifyPayResult(0);

            DialogUtil.showHint("支付成功");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<AccountPayResponse,
                NetProcessor.Processor<AccountPayResponse>>(
                new NetProcessor.Processor<AccountPayResponse>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("processFailure:" + errMsg);
                        orderPayFailed(-1);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //返回多个订单信息列表
//                        {"code":"0",
// "msg":"支付成功!",
// "version":"1",
// "data":[{"dueDate":null,"sellerId":245514,"orderType":0,"bcount":1,"amount":0.01,"guideHumanid":null,"sellOffice":245552,"score":0.0,"discount":1.0,"payType":1,"session_id":null,"adjPrice":"0.0","couponsIds":null,"receiveStock":1192,"finishTime":null,"moneyRegion":null,"paystatus":1,"barcode":"9903000000182199","btype":3,"humanId":245514,"subdisId":null,"addrvalId":null,"addressId":null,"sendhome":0,"urgent":0,"status":0,"remark":"","companyId":245468,"id":138760,"createdBy":"245514","createdDate":"2015-07-21 17:05:11","updatedBy":"","updatedDate":"2015-07-21 17:07:19"}]}
//                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
//                        RspBean<AppPrePayRsp> retValue = (RspBean<AppPrePayRsp>) rspData;
//                        AppPrePayRsp prePayResponse = retValue.getValue();
//                        ZLogger.d("prePayResponse: " + prePayResponse.toString());
                        notifyPayResult(0);
                        DialogUtil.showHint("支付成功");
//                        if(wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY){
//                            //返回账单列表页面
//                            setResult(RESULT_OK);
//                            finish();
//                        }
                    }
                }
                , AccountPayResponse.class
                , MfhApplication.getAppContext())
        {
        };

        DialogUtil.showHint("系统正在处理订单，请稍候...");
        String tradeNo = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID);
        String orderIds = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS);
        String btype = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE);
        String token = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_TOKEN);
        orderPayData.clear();
        CommonUserAccountApiImpl.mfhAccountPay(tradeNo, orderIds, Integer.valueOf(btype), token, responseCallback);
    }

    /**
     * 订单支付失败
     * */
    private void orderPayFailed(int errorCode){
        notifyPayResult(errorCode);
        //                        animProgress.setVisibility(View.GONE);
//                        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
    }

    /**
     * 反馈支付结果给H5
     * @param errorCode 0 成功/-1 失败/-2 取消
     * */
    private void notifyPayResult(int errorCode){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", errorCode);
        bridge.callHandler(JBridgeConf.HANDLE_NAME_PAYRESULT, jsonObject.toString(),
                new WebViewJavascriptBridge.WVJBResponseCallback() {
                    @Override
                    public void callback(String responseData) {
                        ZLogger.d("H5 response! " + responseData);
//                      DialogUtil.showHint("H5 response! " + responseData);
                    }
                });
    }

}

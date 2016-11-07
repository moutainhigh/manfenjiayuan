package com.mfh.owner.ui.web;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApiImpl;
import com.mfh.framework.api.pay.PayApiImpl;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.camera.CameraSessionUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.BitmapUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.ImageUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ShareDialog;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.owner.AppHelper;
import com.mfh.owner.R;
import com.mfh.owner.adapter.SharePopupAdapter;
import com.mfh.owner.alipay.Base64;
import com.mfh.owner.alipay.PayResult;
import com.mfh.owner.bean.NativeShareData;
import com.mfh.owner.bean.PreOrderResponse;
import com.mfh.owner.bean.SharePopupData;
import com.mfh.owner.bean.WXPrePayResponse;
import com.mfh.owner.ui.MainTabActivity;
import com.mfh.owner.utils.AliPayUtil;
import com.mfh.owner.utils.NetProxy;
import com.mfh.owner.utils.UIHelper;
import com.mfh.owner.wxapi.WXHelper;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;


/**
 * H5 JavascriptBridge · WebView · 与具体业务相关
 * @author NAT.ZZN (bingshanguxue)
 * */
public class ComnJBH5Activity extends BaseActivity {
    public static final String EXTRA_KEY_REDIRECT_URL = "redirectUrl";
    public static final String EXTRA_KEY_SYNC_COOKIE = "syncCookie";
    public static final String EXTRA_KEY_BACKASHOMEUP = "backAsHomeUp";

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.animProgress) ProgressBar animProgress;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private Context context;
    @Bind(R.id.webview_custom)
    HybridWebView myWebView;
    private WebViewJavascriptBridge bridge;

    private String mCurrentUrl;
    private Map<String, String> titleMap = new HashMap<>();
    private NativeShareData nativeShareData;//保存当前页面

    private boolean bNeedSyncCookie;//是否需要同步cookie
    private boolean backAsHomeUp = true;//true,关闭网页(默认);false,返回上一页
    private int animType = ANIM_TYPE_NEW_NONE;

    private static ComnJBH5Activity mInstance;
    public static ComnJBH5Activity getInstance(){
        return mInstance;
    }


    public static void actionStart(Activity context, String redirectUrl){
        actionStart(context, redirectUrl, false, true, -1);
    }
    public static void actionStart(Activity context, String redirectUrl, boolean bSyncCookie,
                                   boolean backAsHomeUp, int animationType){
        Intent intent = new Intent(context, ComnJBH5Activity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
        intent.putExtra(EXTRA_KEY_BACKASHOMEUP, backAsHomeUp);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, animationType);
        context.startActivity(intent);

        //Activity切换动画,默认无动画（-1）
//        if(animationType == 0){
//            //底部弹出底部隐藏
//            context.overridePendingTransition(R.anim.bottom_in, R.anim.bottom_out);
//        }
//        else if(animationType == 1){
//            //Activity切换动画,缩放+透明
//            context.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
//        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_base_hybrid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        context = this;
        mInstance = this;
        initWebView();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        titleMap.clear();

        if(!TextUtils.isEmpty(mCurrentUrl)){
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            WebViewUtils.loadUrl(myWebView, mCurrentUrl);
        }
        else{
            DialogUtil.showHint("地址无效");
            finish();
        }
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
        if(myWebView != null){
            myWebView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(myWebView != null){
                myWebView.destroy();
            }

            mInstance = null;
        }
        catch(Exception ex){
           ZLogger.e(ex.toString());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode){
            case CameraSessionUtil.REQUEST_CODE_XIANGCE://相册
                if(intent != null){
                    Uri uri = intent.getData();
                    if(uri != null){
                        //uri= content://media/external/images/media/61232
                        //path= /data/data/com.mfh.owner/files/temp/20150706140434.JPEG
                       ZLogger.d("uri= " + uri);
                        File imageFile = ImageUtil.uriToCompressFile(ComnJBH5Activity.this, uri);
                        saveImageFile(imageFile);
                    }
                }
                break;
            case CameraSessionUtil.REQUEST_CODE_CAMERA://相机
                CameraSessionUtil cameraUtil = ServiceFactory.getService(CameraSessionUtil.class.getName());
//                CameraSessionUtil camera = this.materialController.getCameraUtil();
                //path= /data/data/com.mfh.owner/files/temp/20150706140510.JPEG
                File imageFile = cameraUtil.getCameraResultFile(resultCode, intent, this);
                saveImageFile(imageFile);

                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
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
     * */
    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            mCurrentUrl = intent.getStringExtra(EXTRA_KEY_REDIRECT_URL);//intent.getExtra().getString("redirectUrl");
            bNeedSyncCookie = intent.getBooleanExtra(EXTRA_KEY_SYNC_COOKIE, false);
            backAsHomeUp = intent.getBooleanExtra(EXTRA_KEY_BACKASHOMEUP, true);
            animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, -1);
        }

        //setTheme必须放在onCreate之前执行，后面执行是无效的
        if(animType == 0){
            this.setTheme(R.style.NewFlow);
//            this.setTheme(R.style.activity_new_task2);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        setSupportActionBar(toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_more) {
                    showSharePopup(toolbar);
                }
                return true;
            }
        });

        setupBackIndicator();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!backAsHomeUp && myWebView != null && myWebView.canGoBack()) {
            myWebView.goBack();
            updateWebTitle();
        } else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    /**
     * 设置返回按键
     * */
    private void setupBackIndicator(){
//        if(mCurrentUrl.contains(MobileURLConf.URL_AUTH_INDEX)){
//            ibBack.setVisibility(View.GONE);
//        }else{
//            ibBack.setVisibility(View.VISIBLE);
//        }
    }

    /**
     * 初始化WebView
     * */
    private void initWebView(){
        try{
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

            if(bNeedSyncCookie){
                HybridHelper.syncCookies(this, MfhApi.URL_DEFAULT);
            }

            bridge= new WebViewJavascriptBridge(this, myWebView, new UserServerHandler(),
                    webviewDelegate) ;

            registerHandle();
        }
        catch(Exception e){
           ZLogger.e(e.getMessage());
        }
    }

    /**
     * register native method
     * */
    private void registerHandle(){
        //选择图片
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_SELECT_PICTURE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                       ZLogger.d("H5 call " + JBridgeConf.HANDLE_NAME_SELECT_PICTURE);
                        UIHelper.showSelectPictureDialog(ComnJBH5Activity.this);
                    }
                });

        //打开浏览器
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_OPEN_BROWSER,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                       ZLogger.d("H5(openBrowser):" + data);
                        UIHelper.openBrowser(ComnJBH5Activity.this, data);
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

        //充值--支付宝(1)/微信(21)
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
                                NativeShareData temp = new NativeShareData();

                                JSONObject jsonObject = JSON.parseObject(data);
                                temp.setCurrentUrl(jsonObject.getString("currentUrl"));//当前页Url，用于校验
                                temp.setShareUrl(jsonObject.getString("shareUrl"));//分享页Url
                                temp.setTitle(jsonObject.getString("title"));//标题
                                temp.setDescription(jsonObject.getString("description"));//描述
                                temp.setImageUrl(jsonObject.getString("imageUrl"));//图片Url

                                nativeShareData = temp;
                            }
                        }
                        catch(Exception e){
                           ZLogger.e(e.toString());
                        }

                    }
                });
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
            if (context != null && myWebView != null) {

//                Log.d("Nat: webView.onReceivedTitle.Url", view.getUrl());
//                Log.d("Nat: webView.onReceivedTitle.OriginalUrl", view.getOriginalUrl());
                toolbar.setTitle(myWebView.getTitle());

                //保存标题，解决当goback后标题不改变问题。
                titleMap.put(myWebView.getUrl(), myWebView.getTitle());
            }
        }catch(Exception e){
           ZLogger.e(e.toString());
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
                    toolbar.setTitle(titleMap.get(url));
                }else{
                    toolbar.setTitle(myWebView.getTitle());
                }

//            Log.d("Nat: webView.Title", tvTopBarTitle.getText().toString());
            }
        }
        catch(Exception e){
           ZLogger.e("updateWebTitle" + e.toString());
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
           ZLogger.d(String.format("shouldOverrideUrlLoading url=%s\ncookie=",
                    url, CookieManager.getInstance().getCookie(url)));

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
//            if (newProgress > 90) {
////                loadingImageView.toggle(false);
////                emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            }
        }
    };

    /**
     * 重新加载网页
     * */
    private void reload(){
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if(myWebView != null){
            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            myWebView.clearHistory();
            myWebView.loadUrl(myWebView.getUrl());
//            myWebView.reload();
        }
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

            View contentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.popup_listview_share, null);

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
            if(nativeShareData != null){
                String sharePageUrl = nativeShareData.getCurrentUrl();
                String webviewUrl = myWebView.getUrl();
                if(sharePageUrl != null && webviewUrl != null && sharePageUrl.equalsIgnoreCase(webviewUrl)){
                    menus.add(new SharePopupData("分享", R.drawable.actionbar_share_white, SharePopupData.TAG_SHARE));
//                menus.add(new SharePopupData("分享", R.drawable.actionbar_share_white, SharePopupData.TAG_SHARE));
                }
            }
            final SharePopupAdapter menuAdapter = new SharePopupAdapter(this, menus);
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
                                MainTabActivity.actionStart(ComnJBH5Activity.this, 0);
                                finish();
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
        final ShareDialog dialog = new ShareDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(R.string.dialog_title_share_to);
        dialog.setOnPlatformClickListener(new ShareDialog.OnSharePlatformClick() {
            @Override
            public void onPlatformClick(int id) {
                if (id == R.id.ly_share_weichat) {
                    if(nativeShareData != null){
                        WXHelper.getInstance(ComnJBH5Activity.this)
                                .sendWebpageToWX(nativeShareData.getShareUrl(), nativeShareData.getTitle(),
                                        nativeShareData.getDescription(), nativeShareData.getImageUrl(),
                                        SendMessageToWX.Req.WXSceneTimeline);
                    }
                    else{
                        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                        WXHelper.getInstance(ComnJBH5Activity.this)
                                .sendWebpageToWX(mCurrentUrl, "满分家园", "品质新生活", thumb,
                                        SendMessageToWX.Req.WXSceneTimeline);
                    }
                } else if (id == R.id.ly_share_weichat_circle) {
                    if(nativeShareData != null){
                        WXHelper.getInstance(ComnJBH5Activity.this)
                                .sendWebpageToWX(nativeShareData.getShareUrl(), nativeShareData.getTitle(),
                                        nativeShareData.getDescription(), nativeShareData.getImageUrl(),
                                        SendMessageToWX.Req.WXSceneSession);
                    }
                    else{
                        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                        WXHelper.getInstance(ComnJBH5Activity.this)
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

        if(!NetWorkUtil.isConnect(this)){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        if(TextUtils.isEmpty(amount)){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }

        if(wayType == NetProxy.WAYTYPE_ALIPAY){
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
                                        NetProxy.ALIPAY_NOTIFY_URL, null);
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

            NetProxy.prePay(MfhLoginService.get().getCurrentGuId(), amount, wayType, responseCallback);
        }
        else if(wayType == NetProxy.WAYTYPE_WXPAY){
            //回调
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<WXPrePayResponse,
                    NetProcessor.Processor<WXPrePayResponse>>(
                    new NetProcessor.Processor<WXPrePayResponse>() {
                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);

                            orderPayFailed(-1);
                        }

                        @Override
                        public void processResult(IResponseData rspData) {
//                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<WXPrePayResponse> retValue = (RspBean<WXPrePayResponse>) rspData;
                            WXPrePayResponse prePayResponse = retValue.getValue();
                           ZLogger.d("prePayResponse: " + prePayResponse.toString());
                            String prepayId = prePayResponse.getPrepayId();

                            if(prepayId != null){
                                WXHelper.getInstance(ComnJBH5Activity.this).sendPayReq(prepayId);
                            }else {
                                orderPayFailed(-1);
                                DialogUtil.showHint("prepayId 不能为空");
                            }
                        }
                    }
                    , WXPrePayResponse.class
                    , MfhApplication.getAppContext())
            {
            };

            NetProxy.prePayForApp(MfhLoginService.get().getCurrentGuId(), amount, wayType, responseCallback);
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

        if(!NetWorkUtil.isConnect(this)){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(com.mfh.owner.R.string.toast_network_error));
            return;
        }

        if(TextUtils.isEmpty(orderIds)){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<PreOrderResponse,
                NetProcessor.Processor<PreOrderResponse>>(
                new NetProcessor.Processor<PreOrderResponse>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        orderPayFailed(-1);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        RspBean<PreOrderResponse> retValue = (RspBean<PreOrderResponse>) rspData;
                        PreOrderResponse prePayResponse = retValue.getValue();
                       ZLogger.d("prePayResponse: " + prePayResponse.toString());
                        //商户网站唯一订单号
                        String outTradeNo = prePayResponse.getId();
                        String token = prePayResponse.getToken();
                        if(!TextUtils.isEmpty(outTradeNo)){
//                                amount=1.0id=138750token=501903prepayId=nullsign=null
                            if(wayType == NetProxy.WAYTYPE_ALIPAY){
                                orderPayData.clear();
                                orderPayData.put(NetProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
                                orderPayData.put(NetProxy.PARAM_KEY_ORDER_IDS, orderIds);
                                orderPayData.put(NetProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
                                orderPayData.put(NetProxy.PARAM_KEY_TOKEN, token);
                               ZLogger.d("orderPayData: " + orderPayData.toString());
//                                {btype=3, token=257052, orderIds=138756, preOrderId=138757}

                                //支付宝
                                alipay("商品名称", "商品详情", prePayResponse.getAmount(),
                                        outTradeNo, NetProxy.ALIPAY_ORDER_NOTIFY_URL, token);
                            }
                            else if(wayType == NetProxy.WAYTYPE_WXPAY){
                                String prepayId = prePayResponse.getPrepayId();
                                if(prepayId != null){
                                    orderPayData.clear();
                                    orderPayData.put(NetProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
                                    orderPayData.put(NetProxy.PARAM_KEY_ORDER_IDS, orderIds);
                                    orderPayData.put(NetProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
                                    orderPayData.put(NetProxy.PARAM_KEY_TOKEN, token);
                                   ZLogger.d("orderPayData: " + orderPayData.toString());

                                    WXHelper.getInstance(ComnJBH5Activity.this).sendPayReq(prepayId);
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
                , PreOrderResponse.class
                , MfhApplication.getAppContext())
        {
        };

        NetProxy proxy = new NetProxy();
        proxy.prePayOrder(MfhLoginService.get().getCurrentGuId(), orderIds, btype, wayType, responseCallback);
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
                PayTask alipay = new PayTask(ComnJBH5Activity.this);
                // 调用支付接口，获取支付结果
                String payInfo = AliPayUtil.genPayInfo(subject, body, amount, outTradeNo, notifyUrl, token);
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
            processOrder(NetProxy.WAYTYPE_ALIPAY);
        } else {
            // 判断resultStatus 为非“9000”则代表可能支付失败
            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                processOrder(NetProxy.WAYTYPE_ALIPAY);
//                if(BizConfig.DEBUG){
//                    DialogUtil.showHint("支付结果确认中");
//                }
            }
            else if (TextUtils.equals(resultStatus, "6001")) {
                notifyPayResult(-2);
                DialogUtil.showHint("支付取消");
            }
            else if (TextUtils.equals(resultStatus, "6002")) {
                notifyPayResult(-1);
                DialogUtil.showHint("网络连接出错");
            }else {
                notifyPayResult(-1);
                //4000
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
                    processOrder(NetProxy.WAYTYPE_WXPAY);
                }
                break;
                //错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                case -1:{
                    notifyPayResult(-1);DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", errorCode, (errStr != null ? errStr : "")));

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

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<WXPrePayResponse,
                NetProcessor.Processor<WXPrePayResponse>>(
                new NetProcessor.Processor<WXPrePayResponse>() {
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
//                       ZLogger.d("prePayResponse: " + prePayResponse.toString());
                        notifyPayResult(0);
                        DialogUtil.showHint("支付成功");

//                        if(wayType == NetProxy.WAYTYPE_ALIPAY){
//                            //返回账单列表页面
//                            setResult(RESULT_OK);
//                            finish();
//                        }
                    }
                }
                , WXPrePayResponse.class
                , MfhApplication.getAppContext())
        {
        };

        DialogUtil.showHint("系统正在处理订单，请稍候...");

        String tradeNo = orderPayData.get(NetProxy.PARAM_KEY_PR_EORDER_ID);
        String orderIds = orderPayData.get(NetProxy.PARAM_KEY_ORDER_IDS);
        String btype = orderPayData.get(NetProxy.PARAM_KEY_BIZ_TYPE);
        String token = orderPayData.get(NetProxy.PARAM_KEY_TOKEN);
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

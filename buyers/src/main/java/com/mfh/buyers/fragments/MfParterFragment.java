package com.mfh.buyers.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.mfh.buyers.R;
import com.mfh.buyers.bean.PreOrderResponse;
import com.mfh.buyers.bean.WXPrePayResponse;
import com.mfh.buyers.ui.activity.NativeWebViewActivity;
import com.mfh.buyers.ui.web.HybridHelper;
import com.mfh.buyers.ui.web.JBridgeConf;
import com.mfh.buyers.utils.AlipayConstants;
import com.mfh.buyers.utils.MobileURLConf;
import com.mfh.buyers.utils.NetProxy;
import com.mfh.buyers.utils.UIHelper;
import com.mfh.buyers.wxapi.WXHelper;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.net.URLHelper;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.pay.alipay.PayResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;


/**
 * 小伙伴
 *
 * @author zhangzn created on 2015-04-13
 * @since Framework 1.0
 */
public class MfParterFragment extends BaseFragment{
    private static final String URL_ROOT = MobileURLConf.URL_ME_MFHPARTER;


    public static final String EXTRA_KEY_REDIRECT_URL = "redirectUrl";

    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.webview)
    HybridWebView myWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private WebViewJavascriptBridge bridge;

    private Activity context;
    private String mRootUrl, mCurrentUrl = URL_ROOT;
    private Map<String, String> titleMap = new HashMap<>();

    public MfParterFragment() {
        super();
    }



    @Override
    public int getLayoutResId() {
        return R.layout.fragment_order;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        if(intent != null){
            mRootUrl = intent.getStringExtra(EXTRA_KEY_REDIRECT_URL);
        }

        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            mRootUrl = args.getString(EXTRA_KEY_REDIRECT_URL, "");
        }

        context = getActivity();
        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });

        titleMap.clear();

        mRootUrl = URL_ROOT;
        mCurrentUrl = MobileURLConf.generateUrl(mRootUrl, null);

        initTopBar();
        initWebView();

        if (!MfhLoginService.get().haveLogined()){
            com.mfh.buyers.utils.UIHelper.sendLoginBroadcast(getActivity());
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
//        if(requestCode == OwnerConstants.ACTIVITY_REQUEST_CHANGE_ORDER)
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
        tvTopBarTitle.setText(R.string.topbar_title_buyers);
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
        if(mCurrentUrl.contains(URL_ROOT)){
            ibBack.setVisibility(View.INVISIBLE);

            com.mfh.buyers.utils.UIHelper.sendToggleTabbarBroadcast(getContext(), true);
        }else{
            ibBack.setVisibility(View.VISIBLE);

            com.mfh.buyers.utils.UIHelper.sendToggleTabbarBroadcast(getContext(), false);
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
        bridge= new WebViewJavascriptBridge(getActivity(), myWebView, new UserServerHandler(), webviewDelegate) ;
        registerHandle();

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
                        UIHelper.showSelectPictureDialog(getActivity());
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

        //拨打电话
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_CALL_PHONE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        ZLogger.d("H5(callPhone):" + data);
                        UIHelper.callPhone(getActivity(), data);
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
//                        if(wayType == NetProxy.WAYTYPE_WXPAY){
//                            DialogUtil.showHint("开发正在发呆中，马上回来..");
//                            notifyPayResult(-1);
//                            return;
//                        }
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
                        String orderIds = jsonObject.getString("orderIds");//订单id,多个以英文逗号(,)隔开
                        prePayOrder(wayType, btype, orderIds);
                    }
                });
//        //分享
//        bridge.registerHandler(JBridgeConf.HANDLE_NAME_NATIVE_SHARE,
//                new WebViewJavascriptBridge.WVJBHandler() {
//                    @Override
//                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        try{
//                            ZLogger.d("H5(nativeShare):" + data);
//                            if(!TextUtils.isEmpty(data)){
//                                NativeShareData temp = new NativeShareData();
//
//                                JSONObject jsonObject = JSON.parseObject(data);
//                                temp.setCurrentUrl(jsonObject.getString("currentUrl"));//当前页Url，用于校验
//                                temp.setShareUrl(jsonObject.getString("shareUrl"));//分享页Url
//                                temp.setTitle(jsonObject.getString("title"));//标题
//                                temp.setDescription(jsonObject.getString("description"));//描述
//                                temp.setImageUrl(jsonObject.getString("imageUrl"));//图片Url
//
//                                nativeShareData = temp;
//                            }
//                        }
//                        catch(Exception e){
//                            ZLogger.e(e.toString());
//                        }
//
//                    }
//                });
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
        setupBackIndicator();

        HybridHelper.syncCookies(getContext(), url);
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

//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

//            url = Uri.decode(url);
//            view.loadUrl(url);//在当前webview中加载页面。

//            ComnJBH5Activity.actionStart(getActivity(), url, true, false, -1);
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

    /**
     * 预支付
     * @param amount 支付金额：单位为元，最小金额为0.01元。
     * */
    private void prepay(int wayType, final String amount){
//        emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
//        animProgress.setVisibility(View.VISIBLE);

        if(!NetWorkUtil.isConnect(getContext())){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            notifyPayResult(-1);
            return;
        }

        if(TextUtils.isEmpty(amount)){
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            notifyPayResult(-1);
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

            NetProxy proxy = new NetProxy();
            proxy.prePay(MfhLoginService.get().getCurrentGuId(), amount, wayType, responseCallback);
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
                                WXHelper.getInstance(getContext()).sendPayReq(prepayId);
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

            NetProxy proxy = new NetProxy();
            proxy.prePayForApp(MfhLoginService.get().getCurrentGuId(), amount, wayType, responseCallback);
        }else{
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }
    }

    /**
     * 预支付订单
     * @param wayType 支付方式
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * */
    private void prePayOrder(final int wayType, final int btype, final String orderIds){
//        emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
//        animProgress.setVisibility(View.VISIBLE);

        if(!NetWorkUtil.isConnect(getContext())){
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
                PayTask alipay = new PayTask(getActivity());
                // 调用支付接口，获取支付结果
                String payInfo = AlipayConstants.genPayInfo(subject, body, amount, outTradeNo, notifyUrl, token);
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
     */
    private void parseAlipayResp(String resp) {
        PayResult payResult = new PayResult(resp);
//        resultStatus={9000};memo={};result={partner="2088011585033309"&seller_id="finance@manfenjiayuan.com"&out_trade_no="138761"&subject="商品名称"&body="商品详情"&total_fee="0.01"&notify_url="http://devnew.manfenjiayuan.com/pmc/pmcstock/notifyOrder"&service="mobile.securitypay.pay"&payment_type="1"&_input_charset="utf-8"&it_b_pay="30m"&return_url="m.alipay.com"&success="true"&sign_type="RSA"&sign="OoNoZHMgXQ81Irh/DnCjEhfaEuL5lIqjxCgs05+gV/oIUUqjMffmeRf4fPuXwVsC4XpjQjdNLnCLgXqfIvpAYdt3bqDXEGV1BojgEJl1bz8HCrvT8YIAgPMY/0S9qzCDwuMNcDhcTo2dilK2isUE5AD1MjYtgmtEIWG3WDJNqIA="}
        ZLogger.d("parseAlipayResp: " + payResult.toString());

        /**
         * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
         * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
         * docType=1) 建议商户依赖异步通知
         */
        String resultInfo = payResult.getResult();
        String resultStatus = payResult.getResultStatus();

        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
        if (TextUtils.equals(resultStatus, "9000")) {
            processOrder(EnjoycityApiProxy.WAYTYPE_ALIPAY);
        } else {
            // 判断resultStatus 为非“9000”则代表可能支付失败
            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，
            // 最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                processOrder(EnjoycityApiProxy.WAYTYPE_ALIPAY);
//                if(BizConfig.DEBUG){
//                    DialogUtil.showHint("支付结果确认中");
//                }
            } else {
                //6001,支付取消
                //6002,网络连接出错
                //4000,支付失败
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                DialogUtil.showHint("支付失败");
            }
        }
    }

    public void parseWxpayResp(int errorCode, String errStr) {
        try {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

            switch (errorCode) {
                //成功，展示成功页面
                case 0: {
                    //如果支付成功则去后台查询支付结果再展示用户实际支付结果。注意一定不能以客户端
                    // 返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
                    processOrder(NetProxy.WAYTYPE_WXPAY);
                }
                break;
                //错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                case -1: {
                    notifyPayResult(-1);
                    if (!BizConfig.RELEASE) {
                        DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", errorCode, (errStr != null ? errStr : "")));
                    }
                }
                break;
                //用户取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
                case -2: {
                    notifyPayResult(-2);
                    if (!BizConfig.RELEASE) {
                        DialogUtil.showHint("取消微信充值");
                    }
                }
            }
        } catch (Exception e) {
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
            if (!BizConfig.RELEASE) {
                DialogUtil.showHint("支付成功");
            }
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
//                        RspBean<WXPrePayResponse> retValue = (RspBean<WXPrePayResponse>) rspData;
//                        WXPrePayResponse prePayResponse = retValue.getValue();
//                        ZLogger.d("prePayResponse: " + prePayResponse.toString());
                        notifyPayResult(0);
                        if (!BizConfig.RELEASE) {
                            DialogUtil.showHint("支付成功");
                        }

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

        if (!BizConfig.RELEASE) {
            DialogUtil.showHint("系统正在处理订单，请稍候...");
        }

        String tradeNo = orderPayData.get(NetProxy.PARAM_KEY_PR_EORDER_ID);
        String orderIds = orderPayData.get(NetProxy.PARAM_KEY_ORDER_IDS);
        String btype = orderPayData.get(NetProxy.PARAM_KEY_BIZ_TYPE);
        String token = orderPayData.get(NetProxy.PARAM_KEY_TOKEN);
        orderPayData.clear();
        NetProxy.mfhAccountPay(tradeNo, orderIds, Integer.valueOf(btype), token, responseCallback);
    }

    /**
     * 订单支付失败
     * */
    private void orderPayFailed(int errorCode){
        notifyPayResult(errorCode);
        //                        animProgress.setVisibility(View.GONE);
//                        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
    }

}

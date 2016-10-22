package com.manfenjiayuan.business.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.hybrid.BaseHybridActivity;
import com.mfh.framework.hybrid.JBridgeConf;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;

import java.util.HashMap;
import java.util.Map;


/**
 * H5 JavascriptBridge · WebView · 与具体业务相关
 *
 * @author bingshanguxue
 */
public class HybridActivity extends BaseHybridActivity {

    public static Intent loginIntent(Activity context, String redirectUrl) {
        Intent intent = new Intent(context, HybridActivity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_JSBRIDGE_ENABLED, true);
        intent.putExtra(EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_FLOW);
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static void actionStart(Activity context, String redirectUrl){
        actionStart(context, redirectUrl, -1);
    }
    public static void actionStart(Activity context, String redirectUrl, int animationType){
        actionStart(context, redirectUrl, true, animationType);
    }
    public static void actionStart(Activity context, String redirectUrl, boolean backAsHomeUp, int animationType){
        actionStart(context, redirectUrl, false, backAsHomeUp, animationType);
    }
    public static void actionStart(Activity context, String redirectUrl,
                                   boolean bSyncCookie,
                                   boolean backAsHomeUp, int animationType){
        actionStart(context, redirectUrl, true, bSyncCookie, backAsHomeUp, animationType);
    }
    public static void actionStart(Activity context, String redirectUrl,
                                   boolean jsBridgeEnabled,boolean bSyncCookie,
                                   boolean backAsHomeUp, int animationType){
        Intent intent = new Intent(context, HybridActivity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_JSBRIDGE_ENABLED, jsBridgeEnabled);
        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
        intent.putExtra(EXTRA_KEY_BACKASHOMEUP, backAsHomeUp);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, animationType);
        context.startActivity(intent);
    }


//    //actionStartForResult
//    public static void actionStart(Context context, String redirectUrl, boolean bSyncCookie,
//                                   boolean backAsHomeUp, int animationType){
//        Intent intent = new Intent(context, HybridActivity.class);
//        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
//        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
//        intent.putExtra(EXTRA_KEY_BACKASHOMEUP, backAsHomeUp);
//        context.startActivity(intent);
//
//        //默认无动画（-1）
//        if(animationType == 0){
//            //Activity切换动画,缩放+透明
//            context.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
//        }
//
//        start
//    }


    @Override
    protected void initToolBar() {
        super.initToolBar();

        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_more) {
                    showSharePopup(toolbar, myWebView.getUrl());
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_web);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void setTheme() {
        super.setTheme();
        //TODO,
        //setTheme必须放在onCreate之前执行，后面执行是无效的
        if (animType == ANIM_TYPE_NEW_FLOW) {
            this.setTheme(R.style.NewFlow);
        }
    }


    /**
     * register native method
     */
    @Override
    protected void registerHandle() {
        super.registerHandle();
        if (bridge == null){
            return;
        }
        //保存用户登录信息
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_SAVE_USER_LOGIN_INFO,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data,
                                       WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        // TODO: 7/14/16
//                        AppHelper.saveUserLoginInfo(data);
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
//                        if(wayType == EnjoycityApiProxy.WAYTYPE_WXPAY){
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

    }

    /**
     * 显示更多菜单
     */
    private void showSharePopup(View parentView, String url) {
//        try {
//            int parentViewMeasuredWidth = parentView.getMeasuredWidth() / 3;
//            int parentViewMeasuredHeight = parentView.getMeasuredHeight();
//            int offsetX = parentViewMeasuredWidth * 2 - 16;
//            int offsetY = 0;
//
//            View contentView = LayoutInflater.from(HybridActivity.this).inflate(R.layout.popup_listview_share, null);
//
//            final PopupWindow popupWindow = new PopupWindow(contentView, parentViewMeasuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
////        popupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
//            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
//            popupWindow.update();
//
//            popupWindow.setFocusable(true);// 使其获取焦点
//            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
//            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
//            popupWindow.setBackgroundDrawable(new BitmapDrawable());
//
//            ListView menuList = (ListView) contentView.findViewById(R.id.listview_popup_share);
//            // 加载数据
//            List<SharePopupData> menus = new ArrayList<>();
//            menus.add(new SharePopupData("首页", R.drawable.actionbar_home_white, SharePopupData.TAG_HOME));
//            if (mH5ShareEntity != null) {
//                String sharePageUrl = mH5ShareEntity.getCurrentUrl();
//                if (sharePageUrl != null && url != null && sharePageUrl.equalsIgnoreCase(url)) {
//                    menus.add(new SharePopupData("分享", R.drawable.actionbar_share_white, SharePopupData.TAG_SHARE));
////                menus.add(new SharePopupData("分享", R.drawable.actionbar_share_white, SharePopupData.TAG_SHARE));
//                }
//            }
//            final SharePopupAdapter menuAdapter = new SharePopupAdapter(this, menus);
//            menuList.setAdapter(menuAdapter);
//            menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view,
//                                        int position, long id) {
//                    SharePopupData popupData = (SharePopupData) adapterView.getAdapter().getItem(position);
//                    if (popupData != null) {
//                        switch (popupData.getTag()) {
//                            case SharePopupData.TAG_HOME: {
//                                //跳转到首页
//                                MainActivity.actionStart(HybridActivity.this, null);
//                                finish();
//                            }
//                            break;
//                            case SharePopupData.TAG_SHARE: {
//                                handleShare();
//                            }
//                            break;
//                        }
//                    }
//
//                    if (popupWindow != null) {
//                        popupWindow.dismiss();
//                    }
//                }
//            });
//
//            int[] location = new int[2];
//            parentView.getLocationOnScreen(location);
//            popupWindow.getContentView().measure(parentViewMeasuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
////        popupWindow.getContentView().setBackgroundResource(R.drawable.share_popup_background);
//
//            //Display the content view in a popup window at the specified location.
//            popupWindow.showAsDropDown(parentView, offsetX, offsetY);
//        } catch (Exception e1) {
//            ZLogger.e(e1.toString());
//        }
    }

    /**
     * 分享
     */
    private void handleShare() {
//        final ShareDialog dialog = new ShareDialog(this);
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.setTitle(R.string.dialog_title_share_to);
//        dialog.setOnPlatformClickListener(new ShareDialog.OnSharePlatformClick() {
//            @Override
//            public void onPlatformClick(int id) {
//                if (id == R.id.ly_share_weichat) {
//                    if (mH5ShareEntity != null) {
//                        WXHelper.getInstance(HybridActivity.this)
//                                .sendWebpageToWX(mH5ShareEntity.getShareUrl(), mH5ShareEntity.getTitle(),
//                                        mH5ShareEntity.getDescription(), mH5ShareEntity.getImageUrl(),
//                                        SendMessageToWX.Req.WXSceneTimeline);
//                    } else {
//                        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//                        WXHelper.getInstance(HybridActivity.this)
//                                .sendWebpageToWX(mCurrentUrl, "满分家园", "品质新生活", thumb,
//                                        SendMessageToWX.Req.WXSceneTimeline);
//                    }
//                } else if (id == R.id.ly_share_weichat_circle) {
//                    if (mH5ShareEntity != null) {
//                        WXHelper.getInstance(HybridActivity.this)
//                                .sendWebpageToWX(mH5ShareEntity.getShareUrl(), mH5ShareEntity.getTitle(),
//                                        mH5ShareEntity.getDescription(), mH5ShareEntity.getImageUrl(),
//                                        SendMessageToWX.Req.WXSceneSession);
//                    } else {
//                        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//                        WXHelper.getInstance(HybridActivity.this)
//                                .sendWebpageToWX(mCurrentUrl, "满分家园", "品质新生活", thumb,
//                                        SendMessageToWX.Req.WXSceneSession);
//                    }
//                }
//
//                dialog.dismiss();
//            }
//        });
//        dialog.show();
    }

    /**
     * 预支付
     *
     * @param amount 支付金额：单位为元，最小金额为0.01元。
     */
    private void prepay(int wayType, final String amount) {
//        emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
//        animProgress.setVisibility(View.VISIBLE);

//        if (!NetworkUtils.isConnect(this)) {
////            animProgress.setVisibility(View.GONE);
////            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            DialogUtil.showHint(getString(R.string.toast_network_error));
//            notifyPayResult(-1);
//            return;
//        }
//
//        if (TextUtils.isEmpty(amount)) {
////            animProgress.setVisibility(View.GONE);
////            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            DialogUtil.showHint("参数传递错误");
//            notifyPayResult(-1);
//            return;
//        }
//
//        if (wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY) {
//            //回调
//            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
//                    NetProcessor.Processor<String>>(
//                    new NetProcessor.Processor<String>() {
//                        @Override
//                        protected void processFailure(Throwable t, String errMsg) {
//                            super.processFailure(t, errMsg);
//
//                            orderPayFailed(-1);
//                        }
//
//                        @Override
//                        public void processResult(IResponseData rspData) {
////                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
//                            RspValue<String> retValue = (RspValue<String>) rspData;
//                            //商户网站唯一订单号
//                            String outTradeNo = retValue.getValue();
//                            ZLogger.d("prePayResponse: " + outTradeNo);
//                            if (!TextUtils.isEmpty(outTradeNo)) {
//                                //支付宝充值
//                                alipay("满分家园账单充值", "支付宝充值", amount, outTradeNo,
//                                        EnjoycityApi.ALIPAY_NOTIFY_URL, null);
//                            } else {
//                                orderPayFailed(-1);
//                                DialogUtil.showHint("outTradeNo 不能为空");
//                            }
//                        }
//                    }
//                    , String.class
//                    , MfhApplication.getAppContext()) {
//            };
//
//            EnjoycityApiProxy.prePay(MfhLoginService.get().getCurrentGuId(), amount, wayType, responseCallback);
//        } else if (wayType == EnjoycityApiProxy.WAYTYPE_WXPAY) {
//            //回调
//            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<AppPrePayRsp,
//                    NetProcessor.Processor<AppPrePayRsp>>(
//                    new NetProcessor.Processor<AppPrePayRsp>() {
//                        @Override
//                        protected void processFailure(Throwable t, String errMsg) {
//                            super.processFailure(t, errMsg);
//
//                            orderPayFailed(-1);
//                        }
//
//                        @Override
//                        public void processResult(IResponseData rspData) {
////                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
//                            RspBean<AppPrePayRsp> retValue = (RspBean<AppPrePayRsp>) rspData;
//                            AppPrePayRsp prePayResponse = retValue.getValue();
//                            ZLogger.d("prePayResponse: " + prePayResponse.toString());
//                            String prepayId = prePayResponse.getPrepayId();
//
//                            if (prepayId != null) {
//                                WXHelper.getInstance(HybridActivity.this).sendPayReq(prepayId);
//                            } else {
//                                orderPayFailed(-1);
//                                DialogUtil.showHint("prepayId 不能为空");
//                            }
//                        }
//                    }
//                    , AppPrePayRsp.class
//                    , MfhApplication.getAppContext()) {
//            };
//
//            EnjoycityApiProxy.prePayForApp(MfhLoginService.get().getCurrentGuId(), amount, wayType, responseCallback);
//        } else {
////            animProgress.setVisibility(View.GONE);
////            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            DialogUtil.showHint("参数传递错误");
//            return;
//        }
    }

    /**
     * 预支付订单
     *
     * @param wayType  支付方式
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype    业务类型, 3-商城(必填)
     */
    private void prePayOrder(final int wayType, final int btype, final String orderIds) {
//        emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
//        animProgress.setVisibility(View.VISIBLE);

//        if (!NetworkUtils.isConnect(this)) {
////            animProgress.setVisibility(View.GONE);
////            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            DialogUtil.showHint(getString(R.string.toast_network_error));
//            return;
//        }
//
//        if (TextUtils.isEmpty(orderIds)) {
////            animProgress.setVisibility(View.GONE);
////            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            DialogUtil.showHint("参数传递错误");
//            return;
//        }
//
//        //回调
//        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<PreOrderRsp,
//                NetProcessor.Processor<PreOrderRsp>>(
//                new NetProcessor.Processor<PreOrderRsp>() {
//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        orderPayFailed(-1);
//                    }
//
//                    @Override
//                    public void processResult(IResponseData rspData) {
//                        RspBean<PreOrderRsp> retValue = (RspBean<PreOrderRsp>) rspData;
//                        PreOrderRsp prePayResponse = retValue.getValue();
//                        ZLogger.d("prePayResponse: " + prePayResponse.toString());
//                        //商户网站唯一订单号
//                        String outTradeNo = prePayResponse.getId();
//                        String token = prePayResponse.getToken();
//                        if (!TextUtils.isEmpty(outTradeNo)) {
////                                amount=1.0id=138750token=501903prepayId=nullsign=null
//                            if (wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY) {
//                                orderPayData.clear();
//                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
//                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS, orderIds);
//                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
//                                orderPayData.put(EnjoycityApiProxy.PARAM_KEY_TOKEN, token);
//                                ZLogger.d("orderPayData: " + orderPayData.toString());
////                                {btype=3, token=257052, orderIds=138756, preOrderId=138757}
//
//                                //支付宝
//                                alipay("商品名称", "商品详情", prePayResponse.getAmount(),
//                                        outTradeNo, EnjoycityApi.ALIPAY_ORDER_NOTIFY_URL, token);
//                            } else if (wayType == EnjoycityApiProxy.WAYTYPE_WXPAY) {
//                                String prepayId = prePayResponse.getPrepayId();
//                                if (prepayId != null) {
//                                    orderPayData.clear();
//                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
//                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS, orderIds);
//                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
//                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_TOKEN, token);
//                                    ZLogger.d("orderPayData: " + orderPayData.toString());
//
//                                    WXHelper.getInstance(HybridActivity.this).sendPayReq(prepayId);
//                                } else {
//                                    orderPayFailed(-1);
//                                    DialogUtil.showHint("prepayId 不能为空");
//                                }
//                            }
//                        } else {
//                            orderPayFailed(-1);
//                            DialogUtil.showHint("outTradeNo 不能为空");
//                        }
//                    }
//                }
//                , PreOrderRsp.class
//                , MfhApplication.getAppContext()) {
//        };
//
//        EnjoycityApiProxy proxy = new EnjoycityApiProxy();
//        proxy.prePayOrder(MfhLoginService.get().getCurrentGuId(), orderIds, btype, wayType, responseCallback);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     * <p/>
     * 系统繁忙，请稍后再试（ALI64）
     */
    public void alipay(final String subject, final String body, final String amount,
                       final String outTradeNo, final String notifyUrl, final String token) {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // 构造PayTask 对象
//                PayTask alipay = new PayTask(HybridActivity.this);
//                // 调用支付接口，获取支付结果
//                String payInfo = AlipayUtil.genPayInfo(AlipayConstants.PARTNER, AlipayConstants.SELLER,
//                        AlipayConstants.RSA_PRIVATE, subject, body, amount, outTradeNo, notifyUrl, token);
//                String result = alipay.pay(payInfo, true);
//                // 解析结果
////                parseAlipayResp(result);
//                //resultStatus={6001};memo={操作已经取消。};result={}
//                Message msg = new Message();
//                msg.what = ALI_PAY_FLAG;
//                msg.obj = result;
//                mHandler.sendMessage(msg);
//            }
//        });
//        thread.start();
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
//        PayResult payResult = new PayResult(resp);
////        resultStatus={9000};memo={};result={partner="2088011585033309"&seller_id="finance@manfenjiayuan.com"&out_trade_no="138761"&subject="商品名称"&body="商品详情"&total_fee="0.01"&notify_url="http://devnew.manfenjiayuan.com/pmc/pmcstock/notifyOrder"&service="mobile.securitypay.pay"&payment_type="1"&_input_charset="utf-8"&it_b_pay="30m"&return_url="m.alipay.com"&success="true"&sign_type="RSA"&sign="OoNoZHMgXQ81Irh/DnCjEhfaEuL5lIqjxCgs05+gV/oIUUqjMffmeRf4fPuXwVsC4XpjQjdNLnCLgXqfIvpAYdt3bqDXEGV1BojgEJl1bz8HCrvT8YIAgPMY/0S9qzCDwuMNcDhcTo2dilK2isUE5AD1MjYtgmtEIWG3WDJNqIA="}
//        ZLogger.d("parseAlipayResp: " + payResult.toString());
//
//        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
//        String resultInfo = payResult.getResult();
//        String resultStatus = payResult.getResultStatus();
//
//        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
//        if (TextUtils.equals(resultStatus, "9000")) {
//            processOrder(EnjoycityApiProxy.WAYTYPE_ALIPAY);
//        } else {
//            // 判断resultStatus 为非“9000”则代表可能支付失败
//            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
//            if (TextUtils.equals(resultStatus, "8000")) {
//                processOrder(EnjoycityApiProxy.WAYTYPE_ALIPAY);
////                if(BizConfig.DEBUG){
////                    DialogUtil.showHint("支付结果确认中");
////                }
//            } else if (TextUtils.equals(resultStatus, "6001")) {
//                notifyPayResult(-2);
//                DialogUtil.showHint("支付取消");
//            } else {
//                notifyPayResult(-1);
//                //6001,支付取消
//                //6002,网络连接出错
//                //4000,支付失败
//                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                DialogUtil.showHint("支付失败");
//            }
//        }
    }

    public void parseWxpayResp(Bundle extras) {
//        try {
////            animProgress.setVisibility(View.GONE);
////            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//
//            int errCode = extras.getInt(Constants.BROADCAST_KEY_WXPAY_RESP_ERRCODE, 0);
//            String errStr = extras.getString(Constants.BROADCAST_KEY_WXPAY_RESP_ERRSTR);
//
//            switch (errCode) {
//                //成功，展示成功页面
//                case 0: {
//                    //如果支付成功则去后台查询支付结果再展示用户实际支付结果。注意一定不能以客户端
//                    // 返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
//                    processOrder(EnjoycityApiProxy.WAYTYPE_WXPAY);
//                }
//                break;
//                //错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
//                case -1: {
//                    notifyPayResult(-1);
//                    DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", errCode, (errStr != null ? errStr : "")));
//
//                }
//                break;
//                //用户取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
//                case -2: {
//                    notifyPayResult(-2);
//                    DialogUtil.showHint("取消微信充值");
//                }
//            }
//        } catch (Exception e) {
//            ZLogger.e("parseWxpayResp failed, " + e.toString());
//        }
    }

    private Map<String, String> orderPayData = new HashMap<>();

    /**
     * 处理订单
     * 微信/支付宝支付成功后，调用满分后台支付接口，处理订单。
     */
    private void processOrder(final int wayType) {
//        if (orderPayData.isEmpty()) {
//            notifyPayResult(0);
//            DialogUtil.showHint("支付成功");
//            return;
//        }
//
//        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<AccountPayResponse,
//                NetProcessor.Processor<AccountPayResponse>>(
//                new NetProcessor.Processor<AccountPayResponse>() {
//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        ZLogger.d("processFailure:" + errMsg);
//                        orderPayFailed(-1);
//                    }
//
//                    @Override
//                    public void processResult(IResponseData rspData) {
//                        //返回多个订单信息列表
////                        {"code":"0",
//// "msg":"支付成功!",
//// "version":"1",
//// "data":[{"dueDate":null,"sellerId":245514,"orderType":0,"bcount":1,"amount":0.01,"guideHumanid":null,"sellOffice":245552,"score":0.0,"discount":1.0,"payType":1,"session_id":null,"adjPrice":"0.0","couponsIds":null,"receiveStock":1192,"finishTime":null,"moneyRegion":null,"paystatus":1,"barcode":"9903000000182199","btype":3,"humanId":245514,"subdisId":null,"addrvalId":null,"addressId":null,"sendhome":0,"urgent":0,"status":0,"remark":"","companyId":245468,"id":138760,"createdBy":"245514","createdDate":"2015-07-21 17:05:11","updatedBy":"","updatedDate":"2015-07-21 17:07:19"}]}
////                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
////                        RspBean<AppPrePayRsp> retValue = (RspBean<AppPrePayRsp>) rspData;
////                        AppPrePayRsp prePayResponse = retValue.getValue();
////                        ZLogger.d("prePayResponse: " + prePayResponse.toString());
//                        notifyPayResult(0);
//                        DialogUtil.showHint("支付成功");
//
////                        if(wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY){
////                            //返回账单列表页面
////                            setResult(RESULT_OK);
////                            finish();
////                        }
//                    }
//                }
//                , AccountPayResponse.class
//                , MfhApplication.getAppContext()) {
//        };
//
//        DialogUtil.showHint("系统正在处理订单，请稍候...");
//        String tradeNo = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID);
//        String orderIds = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS);
//        String btype = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE);
//        String token = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_TOKEN);
//        orderPayData.clear();
//        PayApiImpl.mfhAccountPay(tradeNo, orderIds, Integer.valueOf(btype), token, responseCallback);
    }

    /**
     * 订单支付失败
     */
    private void orderPayFailed(int errorCode) {
        notifyPayResult(errorCode);
        //                        animProgress.setVisibility(View.GONE);
//                        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
    }

    /**
     * 反馈支付结果给H5
     *
     * @param errorCode 0 成功/-1 失败/-2 取消
     */
    private void notifyPayResult(int errorCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", errorCode);

        bridge.callHandler(JBridgeConf.HANDLE_NAME_PAYRESULT, jsonObject.toJSONString(),
                new WebViewJavascriptBridge.WVJBResponseCallback() {
                    @Override
                    public void callback(String responseData) {
                        ZLogger.d("H5 response! " + responseData);
//                      DialogUtil.showHint("H5 response! " + responseData);
                    }
                });
    }

}

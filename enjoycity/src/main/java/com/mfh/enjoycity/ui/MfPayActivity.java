package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.manfenjiayuan.business.bean.AccountPayResponse;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.PreOrderResponse;
import com.mfh.enjoycity.events.WxPayEvent;
import com.mfh.enjoycity.utils.AlipayConstants;
import com.mfh.enjoycity.utils.EnjoycityApi;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.wxapi.WXHelper;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.PayApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.pay.alipay.AlipayUtil;
import com.mfh.framework.pay.alipay.PayResult;
import com.mfh.framework.uikit.base.BaseActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 支付页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class MfPayActivity extends BaseActivity {
    private static final String TAG = MfPayActivity.class.getSimpleName();

    public static final String EXTRA_KEY_ORDER_IDS = "orderIds";
    public static final String EXTRA_KEY_ORDER_AMOUNT = "amount";

    @Bind(R.id.button_close)
    ImageButton btnClose;
    @Bind(R.id.tv_amount)
    TextView tvAmount;
    @Bind(R.id.ll_wxpay)
    View llWxPay;
    @Bind(R.id.ll_alipay)
    View llAliPay;
    @Bind({R.id.et_number_0, R.id.et_number_1, R.id.et_number_2, R.id.et_number_3, R.id.et_number_4, R.id.et_number_5})
    List<EditText> etPwds;
    @Bind(R.id.ib_toggle)
    ImageButton ibToggle;
    @Bind(R.id.ll_keyboard)
    View keyboard;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;

    private int MAX_LENGTH = 6;
    private String orderId;
    private String amount;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, MfPayActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_mf_pay;
    }

    @Override
    protected void initToolBar() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            // Translucent status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //注释该行，解决底部导航Tab在5.1.1 Nexus手机上和底部状态栏重叠问题。
            // Translucent navigation bar
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }


        EventBus.getDefault().register(this);

        MAX_LENGTH = etPwds.size();
//        tvAmount.setText(String.format("¥%s"));

        if (StringUtils.isEmpty(orderId)){
            setResult(RESULT_CANCELED);
            DialogUtil.showHint("订单号不能为空");
            finish();
        }

//        tvAmount.setText(String.format("%.2f", amount));
        tvAmount.setText(amount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
//        final MenuItem settings = menu.findItem(R.id.action_settings);
//        MenuItemCompat.setActionView(settings, R.layout.view_corner_button);
//        final Button btnSettings = (Button) settings.getActionView().findViewById(R.id.corner_button);
//        btnSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UIHelper.redirectToActivity(UserActivity.this, SettingsActivity.class);
//            }
//        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * */
    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, -1);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if(animType == ANIM_TYPE_NEW_FLOW){
                this.setTheme(R.style.AppTheme_NewFlow);
            }

            orderId = intent.getStringExtra(EXTRA_KEY_ORDER_IDS);
            amount = intent.getStringExtra(EXTRA_KEY_ORDER_AMOUNT);
        }
    }

    @OnClick(R.id.button_close)
    public void close(){
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.ll_wxpay)
    public void wxPay(){
        prePayOrder(EnjoycityApi.PAY_WAYTYPE_WX, EnjoycityApi.BTYPE_STORE, orderId);
    }

    @OnClick(R.id.ll_alipay)
    public void alipay(){
        prePayOrder(EnjoycityApi.PAY_WAYTYPE_ALIPAY, EnjoycityApi.BTYPE_STORE, orderId);
    }

    @OnClick(R.id.ib_toggle)
    public void toggleKeyboard(){
        if (keyboard.getVisibility() == View.VISIBLE){

//            Animation anim = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
//            anim.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    keyboard.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//            keyboard.startAnimation(anim);
            keyboard.setVisibility(View.GONE);
            ibToggle.setImageResource(R.drawable.icon_arrow_up);
        }
        else{

//            Animation anim = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
//            anim.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    keyboard.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//
////            keyboard.clearAnimation();
//            keyboard.startAnimation(anim);
            keyboard.setVisibility(View.VISIBLE);
            ibToggle.setImageResource(R.drawable.icon_arrow_down);
        }
    }

    @OnClick(R.id.key_0)
    public void onClick0(){
        append("0");
    }
    @OnClick(R.id.key_1)
    public void onClick1(){
        append("1");
    }
    @OnClick(R.id.key_2)
    public void onClick2(){
        append("2");
    }
    @OnClick(R.id.key_3)
    public void onClick3(){
        append("3");
    }
    @OnClick(R.id.key_4)
    public void onClick4(){
        append("4");
    }
    @OnClick(R.id.key_5)
    public void onClick5(){
        append("5");
    }
    @OnClick(R.id.key_6)
    public void onClick6(){
        append("6");
    }
    @OnClick(R.id.key_7)
    public void onClick7(){
        append("7");
    }
    @OnClick(R.id.key_8)
    public void onClick8(){
        append("8");
    }
    @OnClick(R.id.key_9)
    public void onClick9(){
        append("9");
    }
    @OnClick(R.id.key_del)
    public void onClick_del(){
        delete();
    }


    private String passPhase = "";
    private boolean bAcceptInput = true;

    private void append(String text){
        if (!bAcceptInput || StringUtils.isEmpty(text)){
            return;
        }

        passPhase += text;
        show();
    }

    private void delete(){
        if (!StringUtils.isEmpty(passPhase) && passPhase.length() > 0){
            passPhase = passPhase.substring(0, passPhase.length() - 1);
        }
        else{
            passPhase = "";
        }

        show();
    }

    private void show(){
        if (!StringUtils.isEmpty(passPhase)) {
            int len = passPhase.length();
            if (len < MAX_LENGTH){
                bAcceptInput = true;
                for (int i = 0; i < len; i++){
                    etPwds.get(i).setText(String.valueOf(passPhase.charAt(i)));
                }
                for (int j = len; j < MAX_LENGTH; j++){
                    etPwds.get(j).setText("");
                }
            }
            else{
                bAcceptInput = false;
                for (int i = 0; i < MAX_LENGTH; i++){
                    etPwds.get(i).setText(String.valueOf(passPhase.charAt(i)));
                }
                passPhase = passPhase.substring(0, MAX_LENGTH);
            }
        }
        else{
            bAcceptInput = true;
            for (int i = 0; i < MAX_LENGTH; i++){
                etPwds.get(i).setText("");
            }
            passPhase = "";
        }

        ZLogger.d(String.format("passPhase:%s", (passPhase == null ? "" : passPhase)));

        if (passPhase.length() == MAX_LENGTH){
            accountPay();
        }
    }

    /**
     * 满分账户支付
     * */
    private void accountPay(){
        animProgress.setVisibility(View.VISIBLE);
        if(!MfhLoginService.get().haveLogined()){
            animProgress.setVisibility(View.GONE);
            setResult(RESULT_CANCELED);
            DialogUtil.showHint("请先登录");
            finish();
        }
        else{
            if (!NetWorkUtil.isConnect(this)){
                animProgress.setVisibility(View.GONE);
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<AccountPayResponse,
                    NetProcessor.Processor<AccountPayResponse>>(
                    new NetProcessor.Processor<AccountPayResponse>() {
                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            ZLogger.d("processFailure: " + errMsg);
                            notifyPayResult(-1);
                        }

                        @Override
                        public void processResult(IResponseData rspData) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
//                            RspListBean<AccountPayResponse> retValue = (RspListBean<AccountPayResponse>) rspData;

//                            //测试分享
//                            List<AccountPayResponse> result = retValue.getValue();
//                            if (result.size() > 0){
//                                Intent intent = new Intent(MfPayActivity.this, MfOrderShareActivity.class);
//                                Bundle extras = new Bundle();
//                                extras.putString(MfPayActivity.EXTRA_KEY_ORDER_IDS, result.get(0).getId());
//                                intent.putExtras(extras);
//                                MfOrderShareActivity.actionStart(MfPayActivity.this, extras);
//                            }
                            notifyPayResult(0);
                        }
                    }
                    , AccountPayResponse.class
                    , MfhApplication.getAppContext())
            {
            };

            EnjoycityApiProxy.accountPay(MfhLoginService.get().getCurrentGuId(), orderId, passPhase, null, responseCallback);

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
        animProgress.setVisibility(View.VISIBLE);

        if(!NetWorkUtil.isConnect(this)){
            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<PreOrderResponse,
                NetProcessor.Processor<PreOrderResponse>>(
                new NetProcessor.Processor<PreOrderResponse>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        notifyPayResult(-1);
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

//                                finish();
                            }
                            else if(wayType == EnjoycityApiProxy.WAYTYPE_WXPAY){
                                //测试支付接口
//                                WXHelper.getInstance(MfPayActivity.this).getPrepayId();
                                String prepayId = prePayResponse.getPrepayId();
                                if(prepayId != null){
                                    orderPayData.clear();
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS, orderIds);
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_TOKEN, token);
                                    ZLogger.d("orderPayData: " + orderPayData.toString());

                                    WXHelper.getInstance(MfPayActivity.this).sendPayReq(prepayId);

//                                    finish();
                                }else {
                                    notifyPayResult(-1);
                                    DialogUtil.showHint("prepayId 不能为空");
                                }
                            }
                        }
                        else{
                            notifyPayResult(-1);
                            DialogUtil.showHint("outTradeNo 不能为空");
                        }
                    }
                }
                , PreOrderResponse.class
                , MfhApplication.getAppContext())
        {
        };

        EnjoycityApiProxy.prePayOrder(MfhLoginService.get().getCurrentGuId(), orderIds, btype,
                wayType, responseCallback);
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
                PayTask alipay = new PayTask(MfPayActivity.this);
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
                        notifyPayResult(-1);
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
        PayApiImpl.mfhAccountPay(tradeNo, orderIds, Integer.valueOf(btype), token, responseCallback);
    }

    /**
     * 反馈支付结果给H5
     * @param errorCode 0 成功/-1 失败/-2 取消
     * */
    private void notifyPayResult(int errorCode){
        animProgress.setVisibility(View.GONE);
//                        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", errorCode);
        if (errorCode == 0){
            setResult(RESULT_OK);
        }else{
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private static final int ALI_PAY_FLAG = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALI_PAY_FLAG: {
//                    animProgress.setVisibility(View.GONE);
//                    emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

                    parseAlipayResp((String) msg.obj);
                    break;
                }
                default:
                    break;
            }
        }
    };


    public void onEventMainThread(WxPayEvent event) {
        ZLogger.d(String.format("onEventMainThread: %d-%s", event.getErrCode(), event.getErrStr()));
        try{
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

            switch(event.getErrCode()){
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
                    DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", event.getErrCode(), event.getErrStr()));

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

//    //SecondEvent接收函数二
//    public void onEventBackgroundThread(WxPayEvent event){
//        ZLogger.d(String.format("onEventBackgroundThread: %d-%s", event.getErrCode(), event.getErrStr()));
//    }
//    //SecondEvent接收函数三
//    public void onEventAsync(WxPayEvent event){
//        ZLogger.d(String.format("onEventAsync: %d-%s", event.getErrCode(), event.getErrStr()));
//    }
//
//    public void onEvent(WxPayEvent event) {
//        ZLogger.d(String.format("onEvent: %d-%s", event.getErrCode(), event.getErrStr()));
//    }

}

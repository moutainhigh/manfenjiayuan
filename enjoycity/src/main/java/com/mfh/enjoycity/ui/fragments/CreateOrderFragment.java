package com.mfh.enjoycity.ui.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.manfenjiayuan.business.bean.AccountPayResponse;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.enjoycity.AppHelper;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.CreateOrderAdapter;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.api.pay.PreOrderRsp;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.ui.MfPayActivity;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.mfh.enjoycity.utils.AlipayConstants;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApi;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.utils.OrderHelper;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.enjoycity.wxapi.WXHelper;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrderApi;
import com.mfh.framework.api.shoppingCart.CartPack;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.network.URLHelper;
import com.mfh.framework.pay.alipay.AlipayUtil;
import com.mfh.framework.pay.alipay.PayResult;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 下单
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-08-13
 */
public class CreateOrderFragment extends BaseFragment {
    @Bind(R.id.tv_total_price)
    TextView tvTotalPrice;
    @Bind(R.id.btn_confirm_order)
    Button btnSubmit;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.listview_procucts)
    RecyclerView mRecyclerView;
    private CreateOrderAdapter mAdapter;

    private String orderPayInfo;


    public CreateOrderFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_order;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        initRecyclerView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.d(String.format("CreateOrderFragment.onActivityResult.requestCode=%d, resultCode=%d", requestCode, resultCode));

        switch (requestCode) {
            case Constants.ACTIVITY_REQUEST_CODE_CREATE_ORDER_PAY: {
                if (resultCode == Activity.RESULT_OK) {

//                    if (data != null){
//                        int wayType = data.getIntExtra("wayType", 0);
//
//                    }
                    //删除购物车中商品信息
                    OrderHelper.getInstance().clearOrderProducts();
                    //跳转至订单页面
                    String url = URLHelper.append(MobileApi.URL_ME_ORDER_MALL, null);
                    HybridActivity.actionStart(getActivity(), url, true, false, -1);

                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } else {

                }
            }
            break;
            case Constants.ACTIVITY_REQUEST_LOGIN_H5: {
                if (resultCode == Activity.RESULT_OK) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } else {
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 确认下单
     */
    @OnClick(R.id.btn_confirm_order)
    public void submit() {
        if (btnSubmit.getText().equals("立即支付")){
            payOrder();
        }
        else{
            saveOrder();
        }
    }

    private void saveOrder(){
        if (!OrderHelper.getInstance().isDeliverTimeEnabled()) {
            DialogUtil.showHint("未选择送货时间，请先进行选择");
            return;
        }

        if (!NetworkUtils.isConnect(getContext())) {
            animProgress.setVisibility(View.GONE);
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }


        if (!MfhLoginService.get().haveLogined()) {
            animProgress.setVisibility(View.GONE);

            AppHelper.resetMemberAccountData();

            //TODO,判断当前页是否需要切换登录页面
            String authUrl = URLHelper.append(MobileApi.URL_AUTH_INDEX,
                    "redirect=" + MobileApi.URL_NATIVIE_REDIRECT_AUTH);
            startActivityForResult(HybridActivity.loginIntent(getActivity(), authUrl),
                    Constants.ACTIVITY_REQUEST_LOGIN_H5);
            return;
        }


        Long humanId = MfhLoginService.get().getHumanId();

        animProgress.setVisibility(View.VISIBLE);

        JSONObject order = new JSONObject();
        order.put("addressId", ShopcartHelper.getInstance().getAddressId());//收件地址编号,若为空采用业主默认收件地址
        order.put("addrvalId", ShopcartHelper.getInstance().getAddrvalid());//收件公寓地址编号,若为空采用业主默认绑定楼幢
        order.put("guideHumanid", humanId);//导购员编号
        order.put("humanId", humanId);//购物业主编号
        order.put("subdisId", ShopcartHelper.getInstance().getSubdisId());//小区编号
        order.put("payType", 1);///支付类型:0货到付款，1 预先支付
        order.put("bcount", OrderHelper.getInstance().getProductTotalCount());//总件数
        order.put("amount", OrderHelper.getInstance().getOrderTotalAmount());//总价
        order.put("dueDate", OrderHelper.getInstance().getDueDate());//期望送达时间：开始
        order.put("dueDateEnd", OrderHelper.getInstance().getDueDateEnd());//期望送达时间：结束
        order.put("subType", 1);//商超
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);


        JSONArray items = new JSONArray();
        List<CartPack> productList = order.getJSONArray("products");
        if (productList != null && productList.size() > 0) {
            for (ShoppingCartEntity entity : productList) {
                JSONObject item = new JSONObject();
                item.put("shopId", entity.getShopId());//订单项店铺编号（NumberFormat）
                item.put("productId", entity.getProductId());//订单项商品编号（NumberFormat）
                item.put("bcount", entity.getProductCount());//订单项件数
                item.put("price", entity.getProductPrice());//商品单价
                item.put("amount", entity.getTotalAmount());//订单项总价
                item.put("remark", entity.getProductName());//备注
                items.add(item);
            }
        }

        ScOrderApi.saveOrder(order.toJSONString(), items.toJSONString(),
                null, null, saveOrderResponseCallback);
    }


    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
//        mRecyclerView.setOnTouchListener(
//                new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        return mIsRefreshing;
//                    }
//                }
//        );
//        mRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                int position = mRecyclerView.getChildPosition(v);
////                DialogUtil.showHint("click " + position);
//                return false;
//            }
//        });

        mAdapter = new CreateOrderAdapter(getContext(), OrderHelper.getInstance().getShopBeanList());
        mRecyclerView.setAdapter(mAdapter);
//        mShoppingCartAdapter.setOnItemClickListener(
//                new OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        if (!mIsRefreshing) {
//                            if (mMessages.get(position).getIsMessage()) {
//                                showDetailSMS(mMessages.get(position));
//                            }
//                        }
//                    }
//                }
//        );

        tvTotalPrice.setText(String.format("￥ %.2f", OrderHelper.getInstance().getOrderTotalAmount()));
    }

    NetCallBack.NetTaskCallBack saveOrderResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"新增成功!","version":"1","data":{"val":"100980"}}
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    if (retValue != null) {
                        //返回订单编号，多个订单编号以逗号隔开，556935,556936,556937
                        //返回订单编号和价格，以分号隔开。
                        String respStr = retValue.getValue();

                        Message msg = new Message();
                        msg.what = CREATE_ORDER_SUCCEESS;
                        msg.obj = respStr;
                        mHandler.sendMessage(msg);
                        ZLogger.d(String.format("saveOrderResponseCallback %s", retValue.getValue()));
                    }
                    else{
                        animProgress.setVisibility(View.GONE);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("saveOrderResponseCallback.processFailure: " + errMsg);

                    animProgress.setVisibility(View.GONE);

                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    private Map<String, String> orderPayData = new HashMap<>();

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

        if (!NetworkUtils.isConnect(getContext())) {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        if (TextUtils.isEmpty(orderIds)) {
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
                        DialogUtil.showHint("支付失败");
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        RspBean<PreOrderRsp> retValue = (RspBean<PreOrderRsp>) rspData;
                        PreOrderRsp prePayResponse = retValue.getValue();
                        ZLogger.d("prePayResponse: " + prePayResponse.toString());
                        //商户网站唯一订单号
                        String outTradeNo = prePayResponse.getId();
                        String token = prePayResponse.getToken();
                        if (!TextUtils.isEmpty(outTradeNo)) {
//                                amount=1.0id=138750token=501903prepayId=nullsign=null
                            if (wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY) {
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
                            } else if (wayType == EnjoycityApiProxy.WAYTYPE_WXPAY) {
                                String prepayId = prePayResponse.getPrepayId();
                                if (prepayId != null) {
                                    orderPayData.clear();
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID, outTradeNo);
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS, orderIds);
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE, String.valueOf(btype));
                                    orderPayData.put(EnjoycityApiProxy.PARAM_KEY_TOKEN, token);
                                    ZLogger.d("orderPayData: " + orderPayData.toString());

                                    WXHelper.getInstance(getContext()).sendPayReq(prepayId);
                                } else {
                                    DialogUtil.showHint("prepayId 不能为空");
                                }
                            }
                        } else {
                            DialogUtil.showHint("outTradeNo 不能为空");
                        }
                    }
                }
                , PreOrderRsp.class
                , MfhApplication.getAppContext()) {
        };

        PayApiImpl.prePayOrder(MfhLoginService.get().getCurrentGuId(), orderIds, btype, wayType, responseCallback);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     * <p/>
     * 系统繁忙，请稍后再试（ALI64）
     */
    public void alipay(final String subject, final String body, final String amount,
                       final String outTradeNo, final String notifyUrl, final String token) {
        Runnable payRunnable = new Runnable(){

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(getActivity());
                // 调用支付接口，获取支付结果
                String payInfo = AlipayUtil.genPayInfo(AlipayConstants.PARTNER, AlipayConstants.SELLER,
                        AlipayConstants.RSA_PRIVATE,
                        subject, body, amount, outTradeNo, notifyUrl, token);
                String result = alipay.pay(payInfo, true);
                // 解析结果
//                parseAlipayResp(result);
                //resultStatus={6001};memo={操作已经取消。};result={}
                Message msg = new Message();
                msg.what = ALI_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();

    }

    private static final int CREATE_ORDER_SUCCEESS = 1;
    private static final int ALI_PAY_FLAG = 2;
    private static final int ALI_CHECK_FLAG = 3;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREATE_ORDER_SUCCEESS: {
                    animProgress.setVisibility(View.GONE);
//                    btnSubmit.setEnabled(false);
                    btnSubmit.setText("立即支付");

                    orderPayInfo = (String) msg.obj;
                    payOrder();
                }
                break;
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

    private void payOrder(){
        if (StringUtils.isEmpty(orderPayInfo)){
            DialogUtil.showHint("支付失败");
            return;
        }
        String[] respArr = orderPayInfo.split(";");
        String orderIds = "";
        String totalAmount = "";
        if (respArr != null) {
            if (respArr.length > 0) {
                orderIds = respArr[0];
            }
            if (respArr.length > 1) {
                totalAmount = respArr[1];
            }
        }


        Intent intent = new Intent(getContext(), MfPayActivity.class);
        Bundle extras = new Bundle();
        extras.putString(MfPayActivity.EXTRA_KEY_ORDER_IDS, orderIds);
        extras.putString(MfPayActivity.EXTRA_KEY_ORDER_AMOUNT, totalAmount);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_CREATE_ORDER_PAY);

//                    UIHelper.redirectToActivity(getContext(), MfPayActivity.class);
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

    public void parseWxpayResp(int errorCode, String errStr) {
        try {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

            switch (errorCode) {
                //成功，展示成功页面
                case 0: {
                    //如果支付成功则去后台查询支付结果再展示用户实际支付结果。注意一定不能以客户端
                    // 返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
                    processOrder(EnjoycityApiProxy.WAYTYPE_WXPAY);
                }
                break;
                //错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                case -1: {DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", errorCode, (errStr != null ? errStr : "")));

                }
                break;
                //用户取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
                case -2: {
                    DialogUtil.showHint("取消微信充值");
                }
            }
        } catch (Exception e) {
            ZLogger.e("parseWxpayResp failed, " + e.toString());
        }
    }

    /**
     * 处理订单
     * 微信/支付宝支付成功后，调用满分后台支付接口，处理订单。
     */
    private void processOrder(final int wayType) {
        if (orderPayData.isEmpty()) {
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

                        DialogUtil.showHint("支付失败");
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
//                        MLog.d("prePayResponse: " + prePayResponse.toString());

                        DialogUtil.showHint("支付成功");

//                        if(wayType == EnjoycityApiProxy.WAYTYPE_ALIPAY){
//                            //返回账单列表页面
//                            setResult(RESULT_OK);
//                            finish();
//                        }
                    }
                }
                , AccountPayResponse.class
                , MfhApplication.getAppContext()) {
        };

        DialogUtil.showHint("系统正在处理订单，请稍候...");

        String tradeNo = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_PR_EORDER_ID);
        String orderIds = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_ORDER_IDS);
        String btype = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_BIZ_TYPE);
        String token = orderPayData.get(EnjoycityApiProxy.PARAM_KEY_TOKEN);
        orderPayData.clear();
        PayApiImpl.mfhAccountPay(tradeNo, orderIds, Integer.valueOf(btype), token, responseCallback);
    }

}

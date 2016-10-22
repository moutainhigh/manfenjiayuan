package com.manfenjiayuan.mixicook_vip.ui.order;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.manfenjiayuan.business.bean.AccountPayResponse;
import com.manfenjiayuan.mixicook_vip.AlipayConstants;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.widget.LabelView1;
import net.sourceforge.simcpux.WXHelper;
import com.manfenjiayuan.mixicook_vip.wxapi.WXUtil;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.api.pay.PayApiImpl;
import com.mfh.framework.api.pay.PreOrderRsp;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.pay.alipay.OrderInfoUtil2_0;
import com.mfh.framework.pay.alipay.PayResult;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;


/**
 * 订单支付页面
 * Created by bingshanguxue on 6/28/16.
 */
public class OrderPayFragment extends BaseFragment {
    public static final String EXTRA_KEY_ORDERBRIEF = "orderBrief";
    private static final int PAY_ACTION_ACCOUNT = 1;
    private static final int PAY_ACTION_ALIPAY = 2;
    private static final int PAY_ACTION_WEPAY = 4;


    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tv_amount)
    TextView tvAmount;
    @Bind(R.id.action_alipay)
    LabelView1 labelAlipay;
    @Bind(R.id.action_wepay)
    LabelView1 labelWepay;
    @Bind(R.id.action_mfpay)
    LabelView1 labelMfpay;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    private ScOrderPaymentDialog paymentDialog = null;

    private PayOrderBrief mPayOrderBrief;
    private int curPayAction = PAY_ACTION_ACCOUNT;
    private Map<String, String> orderPayData = new HashMap<>();


    public static OrderPayFragment newInstance(Bundle args) {
        OrderPayFragment fragment = new OrderPayFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected boolean isResponseBackPressed() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_order_pay;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mPayOrderBrief = (PayOrderBrief) args.getSerializable(EXTRA_KEY_ORDERBRIEF);
        }

        toolbar.setTitle("立即支付");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        labelAlipay.setOnViewListener(new LabelView1.OnViewListener() {

            @Override
            public void onClick(View v, boolean isChecked) {
                togglePayWay(PAY_ACTION_ALIPAY, !isChecked);
            }

            @Override
            public void onClickCheck(boolean isChecked) {
                togglePayWay(PAY_ACTION_ALIPAY, isChecked);

            }

            @Override
            public void onCheckedChanged(boolean isChecked) {
//                togglePayWay(1, isChecked);
            }
        });
        labelWepay.setOnViewListener(new LabelView1.OnViewListener() {
            @Override
            public void onClick(View v, boolean isChecked) {
                togglePayWay(PAY_ACTION_WEPAY, !isChecked);
            }

            @Override
            public void onClickCheck(boolean isChecked) {
                togglePayWay(PAY_ACTION_WEPAY, isChecked);
            }

            @Override
            public void onCheckedChanged(boolean isChecked) {
//                togglePayWay(2, isChecked);
            }
        });
        labelMfpay.setOnViewListener(new LabelView1.OnViewListener() {

            @Override
            public void onClick(View v, boolean isChecked) {
                togglePayWay(PAY_ACTION_ACCOUNT, !isChecked);
            }

            @Override
            public void onClickCheck(boolean isChecked) {
                togglePayWay(PAY_ACTION_ACCOUNT, isChecked);
            }

            @Override
            public void onCheckedChanged(boolean isChecked) {
//                togglePayWay(4, isChecked);
            }
        });

        loadInit();
    }

    @Override
    public boolean onBackPressed() {
        showConfirmDialog("你要放弃这次支付吗？",
                "确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finish();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return isResponseBackPressed();
    }

    /**
     * 初始化
     */
    private void loadInit() {
        if (mPayOrderBrief == null) {
            DialogUtil.showHint("订单支付数据无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
            return;
        }
        ZLogger.d("orderBrief:\n" + JSON.toJSONString(mPayOrderBrief));
        tvAmount.setText(String.format("￥ %.2f", mPayOrderBrief.getAmount()));

        togglePayWay(curPayAction, true);
    }

    /**
     * 切换支付方式
     */
    private void togglePayWay(int payAction, boolean isChecked) {
        ZLogger.d(String.format("%d ^ %d = ", curPayAction, payAction));

        if (isChecked) {
            curPayAction = payAction;
        } else {
            curPayAction ^= payAction;
        }
        ZLogger.d("curPayAction = " + curPayAction);

        if ((curPayAction & PAY_ACTION_ALIPAY) == PAY_ACTION_ALIPAY) {
            labelAlipay.setChecked(true);
            labelWepay.setChecked(false);
            labelMfpay.setChecked(false);
        } else if ((curPayAction & PAY_ACTION_WEPAY) == PAY_ACTION_WEPAY) {
            labelAlipay.setChecked(false);
            labelWepay.setChecked(true);
            labelMfpay.setChecked(false);
        } else if ((curPayAction & PAY_ACTION_ACCOUNT) == PAY_ACTION_ACCOUNT) {
            labelAlipay.setChecked(false);
            labelWepay.setChecked(false);
            labelMfpay.setChecked(true);
        } else {
            labelAlipay.setChecked(false);
            labelWepay.setChecked(false);
            labelMfpay.setChecked(false);
        }
    }

    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);

        if ((curPayAction & PAY_ACTION_ALIPAY) == PAY_ACTION_ALIPAY) {
            prePayOrder(PayApi.WAYTYPE_ALIPAY, mPayOrderBrief.getBizType(), mPayOrderBrief.getOrderIds());

        } else if ((curPayAction & PAY_ACTION_WEPAY) == PAY_ACTION_WEPAY) {
            prePayOrder(PayApi.WAYTYPE_WXPAY, mPayOrderBrief.getBizType(), mPayOrderBrief.getOrderIds());

        } else if ((curPayAction & PAY_ACTION_ACCOUNT) == PAY_ACTION_ACCOUNT) {
            scAccountPay();
        } else {
            DialogUtil.showHint("请选择支付方式");
            btnSubmit.setEnabled(true);
        }
    }

    /**
     * 会员支付
     */
    private void scAccountPay() {
        if (paymentDialog == null) {
            paymentDialog = new ScOrderPaymentDialog(getActivity());
            paymentDialog.setCancelable(false);
            paymentDialog.setCanceledOnTouchOutside(false);
        }
        paymentDialog.init("支付密码", mPayOrderBrief.getOrderIds(),
                new ScOrderPaymentDialog.OnResponseCallback() {
                    @Override
                    public void onSuccess() {
                        getActivity().setResult(RESULT_OK);
                        getActivity().finish();
                    }

                    @Override
                    public void onCancel() {
                        btnSubmit.setEnabled(true);
                    }
                });
        if (!paymentDialog.isShowing()) {
            paymentDialog.show();
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }
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
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候..", false);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            hideProgressDialog();
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            btnSubmit.setEnabled(true);

            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<PreOrderRsp,
                NetProcessor.Processor<PreOrderRsp>>(
                new NetProcessor.Processor<PreOrderRsp>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        notifyPayResult(-1);
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
                            if (wayType == PayApi.WAYTYPE_ALIPAY) {
                                orderPayData.clear();
                                orderPayData.put("preOrderId", outTradeNo);
                                orderPayData.put("orderIds", orderIds);
                                orderPayData.put("btype", String.valueOf(btype));
                                orderPayData.put("token", token);
                                ZLogger.d("orderPayData: " + orderPayData.toString());
//                                {btype=3, token=257052, orderIds=138756, preOrderId=138757}

                                //支付宝
                                alipay("商品名称", "商品详情", prePayResponse.getAmount(), outTradeNo);

//                                finish();
                            } else if (wayType == PayApi.WAYTYPE_WXPAY) {
                                //测试支付接口
//                                WXHelper.getInstance(MfPayActivity.this).getPrepayId();
                                //后台调用统一下单API生成预付单,获取到prepay_id后将参数再次签名传输给APP发起支付
                                String prepayId = prePayResponse.getPrepayId();
                                if (prepayId != null) {
                                    orderPayData.clear();
                                    orderPayData.put("preOrderId", outTradeNo);
                                    orderPayData.put("orderIds", orderIds);
                                    orderPayData.put("btype", String.valueOf(btype));
                                    orderPayData.put("token", token);
                                    ZLogger.d("orderPayData: " + orderPayData.toString());

                                    hideProgressDialog();
                                    btnSubmit.setEnabled(true);
                                    WXHelper.getInstance(getContext()).sendPayReq(prepayId);

//                                    finish();
                                } else {
                                    notifyPayResult(-1);
                                    DialogUtil.showHint("prepayId 不能为空");
                                }
                            }
                        } else {
                            notifyPayResult(-1);
                            DialogUtil.showHint("outTradeNo 不能为空");
                        }
                    }
                }
                , PreOrderRsp.class
                , MfhApplication.getAppContext()) {
        };

        PayApiImpl.prePayOrder(MfhLoginService.get().getCurrentGuId(), orderIds, btype,
                wayType, WXUtil.genNonceStr(), responseCallback);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     * 支付行为需要在独立的非ui线程中执行
     * <p>
     * 系统繁忙，请稍后再试（ALI64）
     */
    public void alipay(final String subject, final String body, final String amount, final String outTradeNo) {
        String bizContent = OrderInfoUtil2_0.buildBizContent( body, subject, outTradeNo, "30m",
                amount, AlipayConstants.SELLER);
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(AlipayConstants.APPID,
                OrderInfoUtil2_0.ALIPAY_TRADE_APPPAY, AlipayConstants.CHARSET,
                TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS),
                AlipayConstants.ALIPAY_NOTIFY_URL, bizContent);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
        String sign = OrderInfoUtil2_0.getSign(params, AlipayConstants.RSA_PRIVATE);
        final String orderInfo = orderParam + "&" + sign;

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(getActivity());

                // 调用支付接口，获取支付结果
                Map<String, String> result = alipay.payV2(orderInfo, true);
                ZLogger.d("支付宝支付完成:" + result.toString());

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

    private static final int ALI_PAY_FLAG = 1;
    private static final int ALI_CHECK_FLAG = 2;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALI_PAY_FLAG: {
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
     *
     * @param resp
     */
    private void parseAlipayResp(String resp) {
        PayResult payResult = new PayResult(resp);
//        resultStatus={9000};memo={};result={partner="2088011585033309"&seller_id="finance@manfenjiayuan.com"&out_trade_no="138761"&subject="商品名称"&body="商品详情"&total_fee="0.01"&notify_url="http://devnew.manfenjiayuan.com/pmc/pmcstock/notifyOrder"&service="mobile.securitypay.pay"&payment_type="1"&_input_charset="utf-8"&it_b_pay="30m"&return_url="m.alipay.com"&success="true"&sign_type="RSA"&sign="OoNoZHMgXQ81Irh/DnCjEhfaEuL5lIqjxCgs05+gV/oIUUqjMffmeRf4fPuXwVsC4XpjQjdNLnCLgXqfIvpAYdt3bqDXEGV1BojgEJl1bz8HCrvT8YIAgPMY/0S9qzCDwuMNcDhcTo2dilK2isUE5AD1MjYtgmtEIWG3WDJNqIA="}
        ZLogger.d("parseAlipayResp: " + payResult.toString());

        /**
         对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
         */
        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
        String resultStatus = payResult.getResultStatus();

        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
        if (TextUtils.equals(resultStatus, "9000")) {
            // 注意：该笔订单是否真实支付成功，需要依赖服务端的异步通知。
            DialogUtil.showHint("支付成功");
            processOrder(PayApi.WAYTYPE_ALIPAY);
        } else {
            // 注意：该笔订单是否真实支付成功，需要依赖服务端的异步通知。
            // 判断resultStatus 为非“9000”则代表可能支付失败
            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                processOrder(PayApi.WAYTYPE_ALIPAY);
//                if(BizConfig.DEBUG){
//                    DialogUtil.showHint("支付结果确认中");
//                }
            } else if (TextUtils.equals(resultStatus, "6001")) {
                notifyPayResult(-2);
                DialogUtil.showHint("支付取消");
            } else {
                notifyPayResult(-1);
                //6001,支付取消
                //6002,网络连接出错
                //4000,支付失败
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                DialogUtil.showHint("支付失败");
            }
        }
    }

    /**
     * 处理订单
     * 微信/支付宝支付结束后，调用满分后台支付接口，处理订单。
     */
    private void processOrder(final int wayType) {
        if (orderPayData.isEmpty()) {
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
                , MfhApplication.getAppContext()) {
        };

        DialogUtil.showHint("系统正在处理订单，请稍候...");
        String tradeNo = orderPayData.get("preOrderId");
        String orderIds = orderPayData.get("orderIds");
        String btype = orderPayData.get("btype");
        String token = orderPayData.get("token");
        orderPayData.clear();
        PayApiImpl.mfhAccountPay(tradeNo, orderIds, Integer.valueOf(btype), token, responseCallback);
    }


    /**
     * 反馈支付结果给H5
     *
     * @param errorCode 0 成功/-1 失败/-2 取消
     */
    private void notifyPayResult(int errorCode) {
        hideProgressDialog();
        btnSubmit.setEnabled(true);

//                        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", errorCode);
        if (errorCode == 0) {
            getActivity().setResult(RESULT_OK);
            getActivity().finish();
        } else {
//            getActivity().setResult(RESULT_CANCELED);
//            getActivity().finish();
        }
    }


    public void onEventMainThread(WxPayEvent event) {
        ZLogger.d(String.format("onEventMainThread: %d-%s", event.getErrCode(), event.getErrStr()));
        try {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

            switch (event.getErrCode()) {
                //成功，展示成功页面
                case 0: {
                    //如果支付成功则去后台查询支付结果再展示用户实际支付结果。注意一定不能以客户端
                    // 返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
                    processOrder(PayApi.WAYTYPE_WXPAY);
                }
                break;
                //错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                case -1: {
                    notifyPayResult(-1);
                    DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", event.getErrCode(), event.getErrStr()));

                }
                break;
                //用户取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
                case -2: {
                    notifyPayResult(-2);
                    DialogUtil.showHint("取消微信充值");
                }
            }
        } catch (Exception e) {
            ZLogger.e("parseWxpayResp failed, " + e.toString());
        }
    }

}

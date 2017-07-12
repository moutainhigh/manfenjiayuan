package com.mfh.litecashier.components.customer.topup;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import org.greenrobot.eventbus.EventBus;

/**
 * 充值
 * Created by bingshanguxue on 17/5/19.
 */
public abstract class BaseTopupFragment extends BaseFragment {

    public static final String EXTRA_KEY_ORDER_ID       = "orderId";//订单编号：（设备编号_本地数据库订单编号）
    public static final String EXTRA_KEY_ORDER_BARCODE  = "orderBarcode";//订单条码：（本地数据库订单条码）
    public static final String EXTRA_KEY_BIZ_TYPE       = "bizType";//业务类型
    public static final String EXTRA_KEY_SUBJECT        = "subject";//主题
    public static final String EXTRA_KEY_BODY           = "body";   //内容
    public static final String EXTRA_KEY_TOTAL_AMOUNT   = "total_amount";   //金额
    public static final String EXTRA_KEY_CUSTOMER_ID   = "customerId";   //会员编号

    protected int payType;//支付类型

    protected String bizType;//业务类型
    protected String orderBarcode;//订单条码（本地数据库）
    protected Long orderId;//本地数据库订单编号
    protected String subject;//订单标题
    protected String body;//对交易或商品的描述

    //设备号＋订单编号＋时间（每次提交订单时自动生成）
    protected String outTradeNo;//商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
//    protected Double handleAmount   = 0D;//应收金额
    protected Double paidAmount     = 0D;//实收金额
    protected Double lastPaidAmount = 0D;//上一次支付金额,支付异常时查询订单状态
    protected Long customerId;//会员编号

//    protected Double rechargeAmount = 0D;//找零金额
    protected BroadcastReceiver receiver;

    protected boolean bPayProcessing = false;//支付状态

    protected abstract void onPayProcess(String message);
    protected abstract void onPayFailed(final int payStatus, final String msg, int color, boolean isException);
    protected abstract void onPayFinished(final Double paidAmount, String msg, int color);

    /**
     * 设置支付类型*/
    protected int getPayType(){
        return WayType.NA;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        handleIntent();
        payType = getPayType();

        initProgressDialog("正在支付订单", " 支付成功", "支付失败");
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
    }

    public void handleIntent(){
        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            bizType = args.getString(EXTRA_KEY_BIZ_TYPE, "");
            orderId = args.getLong(EXTRA_KEY_ORDER_ID);
            orderBarcode = args.getString(EXTRA_KEY_ORDER_BARCODE, "");
            subject = args.getString(EXTRA_KEY_SUBJECT, "");
            body = args.getString(EXTRA_KEY_BODY, "");
            customerId = args.getLong(EXTRA_KEY_CUSTOMER_ID);
        }
    }

    /**
     * 注册监听器
     */
    protected void registerReceiver() {
    }

    /**
     * 生成商户订单号(64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。)
     * orderId(设备号＋订单编号)＋时间（每次提交订单时自动生成）
     * */
    public void generateOutTradeNo(){
        outTradeNo = CashierFactory.genTradeNo(orderId, true);
        ZLogger.d(String.format("%s支付－交易编号：%s", WayType.getWayTypeName(payType), outTradeNo));
    }

    /**
     * 支付宝条码支付/微信扫码支付--创建支付订单
     * @param paidAmount 支付金额
     * @param authCode 扫描的支付码
     * @return json对象
     */
    protected JSONObject generateOrderInfo(Double paidAmount, String authCode) {
        JSONObject orderInfo = new JSONObject();
        //商户订单号： 1_100014_1445935035219
        orderInfo.put("out_trade_no", outTradeNo);
        orderInfo.put("scene", "bar_code");
        orderInfo.put("auth_code", authCode);
        orderInfo.put("total_amount", MUtils.formatDouble(paidAmount, ""));
//        orderInfo.put("discountable_amount", MStringUtil.formatAmount(discountableAmount));
        orderInfo.put("subject", subject);
        orderInfo.put("body", body);
        orderInfo.put("operator_id", MfhLoginService.get().getHumanId());//商户操作员编号
        orderInfo.put("store_id", MfhLoginService.get().getCurOfficeId());//商户门店编号
        orderInfo.put("terminal_id", SharedPrefesManagerFactory.getTerminalId());
        orderInfo.put("seller_id", MfhLoginService.get().getSpid());//租户ID
        //会员充值
        orderInfo.put("customerId", customerId);

        return orderInfo;
    }

    /**
     * 支付宝条码支付--POS发起支付请求，后台向支付宝请求支付<br>
     * <b>应用场景实例：</b>收银员使用扫码设备读取用户手机支付宝“付款码”后，将二维码或条码信息通过本接口上送至支付宝发起支付。<br>
     * 免密支付,直接返回支付结果，
     * 验密支付,返回10003(支付处理中)状态,然后POS轮询查询订单状态
     * @param authCode 支付宝支付授权码
     */
    protected void submitOrder(String authCode) {
        if (bPayProcessing) {
            ZLogger.d("正在进行支付宝支付，不用重复发起请求");
            return;
        }

        if (StringUtils.isEmpty(authCode)) {
            bPayProcessing = false;
            DialogUtil.showHint("请输入授权码");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            bPayProcessing = false;
            return;
        }

        bPayProcessing = true;
        onPayProcess("正在发送支付请求...");
        lastPaidAmount = paidAmount;
        EventBus.getDefault().post(new TopupActionEvent(TopupActionEvent.TOPUP_PROCESS, null));

        generateOutTradeNo();

        //TODO
    }



}

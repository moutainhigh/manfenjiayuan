package com.mfh.litecashier.ui.fragment.pay;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.CashierFactory;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 结算
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public abstract class BasePayFragment extends BaseFragment {

    public static final String EXTRA_KEY_ORDER_ID       = "orderId";//订单编号：（设备编号_本地数据库订单编号）
    public static final String EXTRA_KEY_ORDER_BARCODE  = "orderBarcode";//订单条码：（本地数据库订单条码）
    public static final String EXTRA_KEY_BIZ_TYPE       = "bizType";//业务类型
    public static final String EXTRA_KEY_SUBJECT        = "subject";//主题
    public static final String EXTRA_KEY_BODY           = "body";   //内容
    public static final String EXTRA_KEY_HANDLE_AMOUNT  = "handleAmount";   //应收金额
    public static final String EXTRA_KEY_MEMBERINFO     = "memberInfo";     //会员信息

    static final Double MAX_RECHARGE_AMOUNT = 100D;

    int payType;//支付类型

    String bizType;//业务类型
    String orderBarcode;//订单条码（本地数据库）
    Long orderId;//本地数据库订单编号
    String subject;//订单标题
    String body;//对交易或商品的描述

    //设备号＋订单编号＋时间（每次提交订单时自动生成）
    String outTradeNo;//商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
    Double handleAmount   = 0D;//应收金额
    Double paidAmount     = 0D;//实收金额
    Double rechargeAmount = 0D;//找零金额
    BroadcastReceiver receiver;

    boolean bPayProcessing = false;//支付状态

    /**
     * 设置支付类型*/
    protected int getPayType(){
        return WayType.NA;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        handleIntent();
        payType = getPayType();
//        ZLogger.d("paytype:" + payType);

        initProgressDialog("正在支付订单", " 支付成功", "支付失败");
    }

    @Override
    public void onResume() {
        super.onResume();

//        DialogUtil.showHint(WayType.name(payType));
//        ZLogger.d("OnResume");
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

//        ZLogger.d("onPause");
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
        }
    }

    public void onInitializeMode(){}
    public void onActiveMode(){}
    public void onDeactiveMode(){}

    /**
     * 生成商户订单号(64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。)
     * orderId(设备号＋订单编号)＋时间（每次提交订单时自动生成）
     * */
    public void generateOutTradeNo(){
        outTradeNo = CashierFactory.genTradeNo(orderId, true);
        ZLogger.df(String.format("%s支付－交易编号：%s", WayType.name(payType), outTradeNo));
    }

    /**
     * 注册监听器
     */
    void registerReceiver() {
    }

    /**
     * 应收金额发生改变*/
    void onHandleAmountChnaged(){
    }

    /**
     * 计算收取金额
     * */
    void calculatePaidAmount(){
    }

    /**
     * 计算找零金额,需要先计算收取金额
     * */
    void calculateCharge(){
        calculatePaidAmount();
        rechargeAmount = paidAmount - handleAmount;
    }

    /**
     * 支付订单
     * */
    void submitOrder(){
        //验证参数
        //生成商户订单号
        generateOutTradeNo();
        ZLogger.df(String.format("支付订单--(%s) 商户订单号:%s 应付金额:%.6f 收取金额:%.6f 找零金额:%.6f",
                WayType.name(payType), outTradeNo, handleAmount, paidAmount, rechargeAmount));
    }

    /**
     * 支付开始
     * */
    void onPayStart(String message){

    }

    void onPayProcess(String message){

    }

    void onPayFailed(){

    }

    void onPayError(){

    }

    void onPaySuccess(){

    }

}

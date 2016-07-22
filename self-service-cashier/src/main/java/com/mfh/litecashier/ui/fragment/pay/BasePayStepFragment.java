package com.mfh.litecashier.ui.fragment.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfoImpl;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfo;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.com.SerialManager;

/**
 * Created by bingshanguxue on 7/5/16.
 */
public abstract class BasePayStepFragment extends BaseFragment {

    public static final String EXTRA_KEY_CASHIER_ORDERINFO = "cashierOrderInfo";
    public static final String EXTRA_KEY_IS_CLEAR_ORDER = "isClearOrder";

    protected CashierOrderInfo cashierOrderInfo = null;
    protected Integer curPayType = WayType.NA;

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

    }

    public void reload(CashierOrderInfo cashierOrderInfo) {
        this.cashierOrderInfo = cashierOrderInfo;
        refresh();
    }

    protected abstract void refresh();

    /**
     * 支付处理中
     */
    public void onPayStepProcess(PaymentInfo paymentInfo) {
        ZLogger.df(String.format("支付处理中：\n%s", JSONObject.toJSONString(cashierOrderInfo)));

        if (paymentInfo == null) {
            return;
        }
        ZLogger.df(String.format("支付记录：\n%s", JSONObject.toJSONString(paymentInfo)));

        //保存订单支付记录
        // TODO: 7/1/16 保存完支付记录后必须要立刻更新订单数据，否则如果支持多种支付方式的话，
        // 下一次保存支付记录的时候，数据会不准确
        CashierAgent.updateCashierOrder(cashierOrderInfo, paymentInfo);
    }

    /**
     * 支付状态更新
     */
    public void onUpdate(PaymentInfo paymentInfo) {
        ZLogger.df(String.format("支付状态更新：\n%s", JSONObject.toJSONString(cashierOrderInfo)));

        if (paymentInfo == null) {
            return;
        }
        ZLogger.df(String.format("支付记录：\n%s", JSONObject.toJSONString(paymentInfo)));

        //保存订单支付记录并更新订单
        // TODO: 7/1/16 保存完支付记录后必须要立刻更新订单数据，否则如果支持多种支付方式的话，
        // 下一次保存支付记录的时候，数据会不准确
        CashierAgent.updateCashierOrder(cashierOrderInfo, paymentInfo);

        //有现金支付时才打开钱箱
        if ((paymentInfo.getPayType() & WayType.CASH) == WayType.CASH
                && paymentInfo.getPaidAmount() > 0) {
            SerialManager.openMoneyBox();
            ZLogger.df(String.format(">>开钱箱：收银：%.2f,找零:%.2f",
                    paymentInfo.getPaidAmount(), paymentInfo.getChange()));
        }

        // TODO: 7/5/16 注意这里需要重新生成订单支付信息，如果允许多次支付的话，可能还需要清空页面优惠券信息。
        cashierOrderInfo = CashierFactory.makeCashierOrderInfo(cashierOrderInfo.getBizType(),
                cashierOrderInfo.getPosTradeNo(), cashierOrderInfo.getVipMember());
        ZLogger.df(String.format("%s 支付完成：重新生成结算信息：\n%s", WayType.name(curPayType),
                JSONObject.toJSONString(cashierOrderInfo)));

        //根据实际应用场景，金额小于1分即认为支付完成
        Double handleAmount = CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo);
        if (handleAmount < 0.01) {
            //修改订单支付信息（支付金额，支付状态）
            CashierAgent.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_FINISH);

            onPayFinished();
        } else {
            onPayStepFinish();
        }
    }


    /**
     * 支付步骤成功
     */
    public void onPayStepFinish() {
        ZLogger.df(String.format("%s 支付成功：准备关闭支付窗口：\n%s", WayType.name(curPayType),
                JSONObject.toJSONString(cashierOrderInfo)));
    }

    /**
     * 订单失败or异常
     */
    public void onPayStepFailed(PaymentInfo paymentInfo, String errMsg) {
        ZLogger.df(String.format("%s 订单支付失败or异常：%s\n%s", WayType.name(curPayType),
                errMsg, JSONObject.toJSONString(cashierOrderInfo)));

        if (paymentInfo == null) {
            return;
        }
        ZLogger.df(String.format("支付记录：\n%s", JSONObject.toJSONString(paymentInfo)));

        //保存订单支付记录并更新订单
        // TODO: 7/1/16 保存完支付记录后必须要立刻更新订单数据，否则如果支持多种支付方式的话，
        // 下一次保存支付记录的时候，数据会不准确
        CashierAgent.updateCashierOrder(cashierOrderInfo, paymentInfo);
    }

    /**
     * 支付成功
     */
    public void onPayFinished() {
        DialogUtil.showHint("支付成功");
        ZLogger.df(String.format("%s 支付成功：准备关闭支付窗口：\n%s", WayType.name(curPayType),
                JSONObject.toJSONString(cashierOrderInfo)));

        Intent data = new Intent();
        data.putExtra(EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    /**
     * 支付取消
     */
    public void onPayCancel() {
        CashierAgent.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_STAY_PAY);

        ZLogger.df(String.format("%s 支付取消：准备关闭支付窗口：\n%s", WayType.name(curPayType),
                JSONObject.toJSONString(cashierOrderInfo)));

        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 订单异常
     * */
    public void onPayException(){
        CashierAgent.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_EXCEPTION);
        ZLogger.df(String.format("%s 订单异常：准备关闭支付窗口：\n%s", WayType.name(curPayType),
                JSONObject.toJSONString(cashierOrderInfo)));

        Intent data = new Intent();
        data.putExtra(EXTRA_KEY_IS_CLEAR_ORDER, true);
//        data.putExtra(EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
//        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().setResult(Activity.RESULT_CANCELED, data);
        getActivity().finish();
    }

}

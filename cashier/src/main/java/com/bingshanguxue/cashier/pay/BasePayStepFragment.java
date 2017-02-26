package com.bingshanguxue.cashier.pay;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.bingshanguxue.cashier.v1.CashierOrderInfoImpl;
import com.bingshanguxue.cashier.v1.PaymentInfo;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.HumanAuthTempHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 7/5/16.
 */
public abstract class BasePayStepFragment extends BaseFragment {

    public static final String EXTRA_KEY_CASHIER_ORDERINFO = "cashierOrderInfo";
    public static final String EXTRA_KEY_IS_CLEAR_ORDER = "isClearOrder";

    protected CashierOrderInfo cashierOrderInfo = null;
    protected Integer curPayType = WayType.NA;


    private CommonDialog cancelPayDialog = null;
    private NumberInputDialog phoneInputDialog = null;//手机号


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
        CashierAgent.updateCashierOrder(cashierOrderInfo.getBizType(), cashierOrderInfo.getPosTradeNo(),
                cashierOrderInfo.getVipMember(), paymentInfo);
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
        CashierAgent.updateCashierOrder(cashierOrderInfo.getBizType(), cashierOrderInfo.getPosTradeNo(),
                cashierOrderInfo.getVipMember(), paymentInfo);

        //有现金支付时才打开钱箱
        if ((paymentInfo.getPayType() & WayType.CASH) == WayType.CASH
                && paymentInfo.getPaidAmount() > 0) {
            PrinterFactory.getPrinterManager().openMoneyBox();
            ZLogger.df(String.format(">>开钱箱：收银：%.2f,找零:%.2f",
                    paymentInfo.getPaidAmount(), paymentInfo.getChange()));
        }

        // TODO: 7/5/16 注意这里需要重新生成订单支付信息，如果允许多次支付的话，可能还需要清空页面优惠券信息。
        cashierOrderInfo = CashierAgent.makeCashierOrderInfo(cashierOrderInfo.getBizType(),
                cashierOrderInfo.getPosTradeNo(), cashierOrderInfo.getVipMember());

        //根据实际应用场景，金额小于1分即认为支付完成
        Double handleAmount = CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo);

        ZLogger.df(String.format("%s 支付完成，handleAmount=%f：重新生成结算信息：\n%s",
                WayType.name(curPayType), handleAmount, JSONObject.toJSONString(cashierOrderInfo)));
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
        ZLogger.df(String.format("%s 支付成功：\n%s", WayType.name(curPayType),
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
        CashierAgent.updateCashierOrder(cashierOrderInfo.getBizType(), cashierOrderInfo.getPosTradeNo(),
                cashierOrderInfo.getVipMember(), paymentInfo);
    }

    /**
     * 支付成功
     */
    public void onPayFinished() {
        DialogUtil.showHint("支付成功");

        if ((curPayType & WayType.ALI_F2F) == WayType.ALI_F2F
                || (curPayType & WayType.WX_F2F) == WayType.WX_F2F) {
            bindHuman();
        } else {
            back2MainActivity();
        }
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
     */
    public void onPayException() {
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

    /**
     * 取消支付
     */
    public void cancelSettle() {
        // TODO: 7/21/16 这里要做判断，当前是不是正在支付订单，正在支付订单的时候不能关闭窗口
//        setResult(Activity.RESULT_CANCELED);
//        finish();
        if (cancelPayDialog == null) {
            cancelPayDialog = new CommonDialog(getActivity());
            cancelPayDialog.setCancelable(true);
            cancelPayDialog.setCanceledOnTouchOutside(true);
            cancelPayDialog.setMessage("确定要取消支付吗？");
        }
        cancelPayDialog.setPositiveButton("订单异常", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onPayException();

//                getActivity().setResult(Activity.RESULT_CANCELED);
//                getActivity().finish();
            }
        });
        cancelPayDialog.setNegativeButton("取消支付", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                onPayCancel();

                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });
        cancelPayDialog.show();
    }


    private void back2MainActivity() {
        ZLogger.df(String.format("%s 支付成功：准备关闭支付窗口：\n%s", WayType.name(curPayType),
                JSONObject.toJSONString(cashierOrderInfo)));
        Intent data = new Intent();
        data.putExtra(EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    /**
     * 绑定用户
     */
    private void bindHuman() {
        List<PosOrderPayEntity> payEntities = PosOrderPayService.get()
                .queryAllBy(String.format("orderId = '%d' and payType = '%d' and paystatus = '%d'",
                        cashierOrderInfo.getOrderId(),
                        cashierOrderInfo.getPayType(),
                        PosOrderPayEntity.PAY_STATUS_FINISH));
        if (payEntities == null || payEntities.size() <= 0) {
            back2MainActivity();
            return;
        }

        final PosOrderPayEntity posOrderPayEntity = payEntities.get(0);
        Map<String, String> options = new HashMap<>();
        options.put("payType", String.valueOf(curPayType));
        options.put("outTradeNo", posOrderPayEntity.getOutTradeNo());
        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        HumanAuthTempHttpManager.getInstance().getPayTempUserBindHuman(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.df("查询该笔支付者绑定的平台用户失败：" + e.toString());
                        back2MainActivity();
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        ZLogger.df("查询该笔支付者绑定的平台用户成功：" + data);

                        if (!StringUtils.isEmpty(data)) {
                            PosOrderEntity orderEntity = CashierAgent.fetchOrderEntity(BizType.POS,
                                    cashierOrderInfo.getPosTradeNo());
                            if (orderEntity != null) {
                                orderEntity.setHumanId(Long.valueOf(data));
                                PosOrderService.get().saveOrUpdate(orderEntity);
                            }
                            back2MainActivity();
                        } else {
                            bindCashierOrderStep1(curPayType, posOrderPayEntity.getOutTradeNo());
                        }
                    }
                });
    }

    /**
     * 绑定订单到平台用户
     */
    private void bindCashierOrderStep1(final Integer payType, final String outTradeNo) {
        if (phoneInputDialog == null) {
            phoneInputDialog = new NumberInputDialog(getActivity());
            phoneInputDialog.setCancelable(true);
            phoneInputDialog.setCanceledOnTouchOutside(true);
        }
        phoneInputDialog.initializeBarcode(EditInputType.PHONE, "手机号码", "手机号码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        bindCashierOrderStep2(payType, outTradeNo, value);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {
                        back2MainActivity();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!phoneInputDialog.isShowing()) {
            phoneInputDialog.show();
        }
    }

    /**
     * 绑定订单到平台用户
     */
    private void bindCashierOrderStep2(Integer payType, String outTradeNo, String phoneNumber) {
        Map<String, String> options = new HashMap<>();
        options.put("payType", String.valueOf(payType));
        options.put("outTradeNo", outTradeNo);
        options.put("mobile", phoneNumber);
        HumanAuthTempHttpManager.getInstance().bindPayTempUserBindHuman(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        DialogUtil.showHint(e.getMessage());
                        back2MainActivity();
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        ZLogger.df("绑定用户成功: " + data);
                        DialogUtil.showHint("绑定成功");

                        if (!StringUtils.isEmpty(data)) {
                            PosOrderEntity orderEntity = CashierAgent.fetchOrderEntity(BizType.POS,
                                    cashierOrderInfo.getPosTradeNo());
                            if (orderEntity != null) {
                                orderEntity.setHumanId(Long.valueOf(data));
                                PosOrderService.get().saveOrUpdate(orderEntity);
                            }
                        }
                        back2MainActivity();
                    }

                });
    }

}

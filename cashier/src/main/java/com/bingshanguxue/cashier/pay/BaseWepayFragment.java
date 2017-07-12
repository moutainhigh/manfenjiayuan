package com.bingshanguxue.cashier.pay;

import android.os.Bundle;

import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.http.WePayHttpManager;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 10/06/2017.
 */

public class BaseWepayFragment extends BasePayFragment {
    protected Double lastPaidAmount = 0D;//上一次支付金额,支付异常时查询订单状态

    protected void onBarpayProcessing(String msg){}
    protected void onBarpayFailed(final int payStatus, final String msg, int color, boolean isException){}
    protected void onBarpayFinished(final Double paidAmount, String msg, int color) {}


    @Override
    protected int getPayType() {
        return WayType.WX_F2F;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void calculatePaidAmount() {
        paidAmount = handleAmount;
    }

    @Override
    protected void submitOrder() {
        bPayProcessing = true;
        onBarpayProcessing("正在发送支付请求...");
        lastPaidAmount = paidAmount;

        super.submitOrder();

        Bundle args = new Bundle();
        args.putSerializable(PayActionEvent.KEY_PAYMENT_INFO,
                PaymentInfo.create(outTradeNo, payType,
                        PosOrderPayEntity.PAY_STATUS_PROCESS,
                        paidAmount, paidAmount, 0D, null));
        EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_PROCESS, args));
    }

    protected void wepayBarPay(String authCode){
        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", generateOrderInfo(lastPaidAmount, authCode).toJSONString());
        options.put("bizType", bizType);
        options.put("chId", MfhApi.WXPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        WePayHttpManager.getInstance().wepayBarPay(options,
                new Subscriber<MResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.w("微信条码支付异常:" + e.toString());
                        onBarpayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, e.getMessage(), getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, "微信条码支付失败，无响应", getErrorTextColor(), false);
                            return;
                        }
                        ZLogger.d(String.format("微信条码支付:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));
                        switch (stringMResponse.getCode()) {
                            //{"code":"0","msg":"Success","version":"1","data":""}
                            //10000--业务处理成功（订单支付成功）
                            case 0: {
                                onBarpayFinished(lastPaidAmount, "支付成功", getOkTextColor());
                            }
                            break;
                            //下单成功等待用户输入密码
                            //{"code":"1","msg":" order success pay inprocess","version":"1","data":""}
                            //订单创建成功支付处理中(验密支付)
                            //10003，业务处理中,该结果码只有在条码支付请求 API 时才返回，代表付款还在进行中，需要调用查询接口查询最终的支付结果
                            // 条码支付请求 API 返回支付处理中(返回码 10003)时，此时若用户支付宝钱包在线则会唤起支付宝钱包的快捷收银台，
                            // 用户可输入密码支付。商户需要在设定的轮询时间内，通过订单查询 API 查询订单状态，若返回付款成功，则表示支付成功。
                            case 1: {
                                queryOrder(outTradeNo, lastPaidAmount);
                            }
                            break;
                            ////交易创建失败
                            //40004--错误码：ACQ.INVALID_PARAMETER错误描述：支付失败，交易参数异常，请顾客刷新付款码后重新收款。如再次收款失败，请联系管理员处理。[INVALID_PARAMETER]
                            //40004--错误码：ACQ.PAYMENT_AUTH_CODE_INVALID错误描述：支付失败，获取顾客账户信息失败，请顾客刷新付款码后重新收款，如再次收款失败，请联系管理员处理。[SOUNDWAVE_PARSER_FAIL]
                            default: {//-1
                                onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, stringMResponse.getMsg(), getErrorTextColor(), false);
//                                onBarpayFailed(rspBody.getReturnInfo(), Color.parseColor("#FE5000"), true);
//                                queryOrder(outTradeNo, lastPaidAmount);
                            }
                            break;
                        }
                    }
                });
    }

    /**
     * 微信支付--轮询查询订单状态
     * <b>应用场景实例：</b>本接口提供微信支付订单的查询的功能，商户可以通过本接口主动查询订单状态，完成下一步的业务逻辑。<br>
     * 需要调用查询接口的情况：<br>
     * 1. 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；<br>
     * 2. 调用扫码支付支付接口后，返回系统错误或未知交易状态情况；<br>
     * 3. 调用扫码支付请求后，如果结果返回处理中（返回结果中的code等于10003）的状态；<br>
     * 4. 调用撤销接口API之前，需确认该笔交易目前支付状态。<br>
     */
    protected void queryOrder(final String outTradeNo, final Double paidAmount) {
        onBarpayProcessing("正在查询订单状态...");

        Map<String, String> options = new HashMap<>();
        options.put("out_trade_no", outTradeNo);
        options.put("chId", MfhApi.WXPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        WePayHttpManager.getInstance().query(options,
                new Subscriber<MResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.w("查询订单状态失败:" + e.toString());
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onBarpayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, e.getMessage(), getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, "微信条码支付失败，无响应", getErrorTextColor(), false);
                            return;
                        }
                        ZLogger.d(String.format("微信条码支付状态查询:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));

                        switch (stringMResponse.getCode()) {
                            case 0:
                                onBarpayFinished(paidAmount, "支付成功", getOkTextColor());
                                break;
                            //{"code":"-1","msg":"继续查询","version":"1","data":""}
                            // 支付结果不明确，需要收银员继续查询或撤单
                            case -1: //-1
                                onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, stringMResponse.getMsg(), getErrorTextColor(), true);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default: //-2
                                onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, stringMResponse.getMsg(), getErrorTextColor(), false);
                                break;
                        }
                    }
                });
    }

    /**
     * 微信支付--撤单
     * <b>应用场景实例：</b>调用微信支付接口时未返回明确的返回结果（如系统错误或网络异常），可使用本接口将交易进行撤销。<br>
     * 如果用户支付失败，微信会将此订单关闭；如果用户支付成功，微信会将支付的资金退还给用户。<br>
     * 撤销只支持24小时内的交易，超过24小时要退款可以调用申请退款接口，如果需要明确订单状态可以调用查询订单接口。<br>
     * 只有发生支付系统超时或者支付结果未知时可调用撤销，其他正常支付 的单如需实现相同功能请调用申请退款 API。提交支付交易后调用【查询订单 API】， 没有明确的支付结果再调用【撤销订单 API】。
     */
    protected void cancelOrder(String outTradeNo) {
        onBarpayProcessing("正在发送撤单请求...");

        Map<String, String> options = new HashMap<>();
        options.put("out_trade_no", outTradeNo);
        options.put("chId", MfhApi.WXPAY_CHANNEL_ID);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        WePayHttpManager.getInstance().cancelOrder(options,
                new Subscriber<MResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.w("撤单失败:" + e.toString());
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        onBarpayFailed(PosOrderPayEntity.PAY_STATUS_EXCEPTION, e.getMessage(), getErrorTextColor(), true);
                    }

                    @Override
                    public void onNext(MResponse<String> stringMResponse) {
                        if (stringMResponse == null) {
                            onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, "微信条码支付取消订单，无响应", getErrorTextColor(), false);
                            return;
                        }
                        ZLogger.d(String.format("微信条码支付取消订单:%s--%s", stringMResponse.getCode(), stringMResponse.getMsg()));

                        switch (stringMResponse.getCode()) {
                            case 0:
                                onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, "订单已取消", getOkTextColor(), false);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default: //-2
                                onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, stringMResponse.getMsg(), getErrorTextColor(), false);
                                break;
                        }
                    }
                });
    }


}

package com.mfh.litecashier.components.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.chinaums.mis.bank.BankDAO;
import com.chinaums.mis.bank.ICallBack;
import com.chinaums.mis.bean.RequestPojo;
import com.chinaums.mis.bean.ResponsePojo;
import com.chinaums.mis.bean.TransCfx;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.payOrder.PayOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.pay.umsips.RequestConstants;
import com.mfh.framework.pay.umsips.RspCode;
import com.mfh.framework.pay.umsips.TransType;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MSubscriber;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.hardware.SerialManager;
import com.mfh.litecashier.ui.widget.PayProcessView;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * 银联卡支付
 * Created by bingshanguxue on 15/8/31.
 */
public class PayByBandcardFragment extends BasePayFragment {

    @BindView(R.id.ll_pay_info)
    RelativeLayout llPayInfo;
    @BindView(R.id.payProcessView)
    PayProcessView payProcessView;
    @BindView(R.id.fab_pay)
    ImageButton fabPay;


    private BankDAO bankDAO;
    private boolean isRunningThread;
    private RequestPojo request;
    private ResponsePojo response;
    private TransCfx transCFX;

    private class BackCall implements ICallBack {
        private BackCall() {
        }

        public void getCallBack(String stateCode, String stateTips) {
            ZLogger.i("银联支付：stateCode=" + stateCode + "|" + "stateTips=" + stateTips);
        }
    }

//    //创建ServiceConnection对象
//    private ServiceConnection conn = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            // TODO Auto-generated method stub
//            DialogUtil.showHint("onServiceConnected called");
//            bank = IBank.Stub.asInterface(service);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            // TODO Auto-generated method stub
//            DialogUtil.showHint("onServiceDisconnected called");
//            bank = null;
//        }
//
//    };


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pay_bankcard;
    }

    @Override
    protected int getPayType() {
        return WayType.BANKCARD;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);


        payProcessView.init(120, "点击重试", "查询订单", "撤销订单",
                new PayProcessView.onCustomerViewListener() {

                    @Override
                    public void onAction1() {
                        payProcessView.setState(PayProcessView.STATE_INIT, null);
                    }

                    /**
                     * 查询订单状态
                     * 因网络或系统异常导致支付状态不明时调用
                     */
                    @Override
                    public void onAction2() {
                        submitOrder();

                    }

                    /**
                     * 撤单
                     * 因网络或系统异常导致支付状态不明时调用
                     */
                    @Override
                    public void onAction3() {
                        llPayInfo.setVisibility(View.VISIBLE);
                        payProcessView.setVisibility(View.GONE);
                    }

                });

//        Intent service = new Intent("com.chinaums.mis.umsips");
//        bindService(service, conn, Service.BIND_AUTO_CREATE);

    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO,主动去请求当前价格
//        EventBus.getDefault().post(new MfPayEvent(CashierConstants.PAYTYPE_BANKCARD, MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (payProcessView != null) {
            payProcessView.onDestory();
        }
    }

    @Override
    protected void calculatePaidAmount() {
        paidAmount = handleAmount;
    }

    /**
     * 注册监听器
     */
    @Override
    protected void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BA_HANDLE_AMOUNT_CHANGED_BANK);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZLogger.d("onReceive.action=" + intent.getAction());
                if (intent.getAction().equals(Constants.BA_HANDLE_AMOUNT_CHANGED_BANK)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        isRunningThread = false;
                        handleAmount = extras.getDouble(EXTRA_KEY_HANDLE_AMOUNT);
                        payProcessView.setState(PayProcessView.STATE_INIT, null);

                        calculateCharge();
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(receiver, intentFilter);
    }

    @Override
    @OnClick(R.id.fab_pay)
    protected void submitOrder() {
        super.submitOrder();

        if (isRunningThread) {
            DialogUtil.showHint("\u5df2\u7ecf\u6709\u4ea4\u6613\u5728\u8fdb\u884c\uff01\uff01\uff01");
            return;
        }


//        onPayProcess("初始化交易基础信息，请稍候......");
        onPayProcess("正在处理订单，请稍候......");
        initBasicValue();
        consumer(handleAmount);
    }

    /**
     * 消费,正常情况，5秒钟POS机可以跳转到刷卡页面。
     */
    private void consumer(Double amount) {
        initBankRequestValue(amount, TransType.CONSUMER);
        new Thread(new Runnable() {
            public void run() {
                try {
                    isRunningThread = true;
                    ZLogger.d("正在消费，请稍候......");
                    bankDAO = new BankDAO();
                    bankDAO.getCallBack(new BackCall());
                    response = bankDAO.bankall(transCFX, request);
                    ZLogger.d(String.format("消费结束：%s", response.toString()));
                    if (RspCode.CODE_00.equals(response.getRspCode())) {
                        //签到成功，发起消费
                        //00交易成功622760******0655
                        // 0000000000010104中国银行0000490000021653175640983970050412898320554115217558772360412165317
//                        if (getActivity() != null) {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                }
//                            });
//                        }

                        createPayOrder(response.getTraceNo(), response.getBankCode(),
                                response.getCardNo(), response.getAmount(),
                                response.getMerchId(), response.getTermId());

                    } else {
                        //交易失败
                        //E4版本校验失败,密钥不正确
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED,
                                            response.getRspChin(), false);
                                }
                            });
                        }
                    }
                    isRunningThread = false;
                } catch (Exception e) {
                    ZLogger.e(e.toString());
                    isRunningThread = false;
                    if (getActivity() != null) {
                        getActivity()
                                .runOnUiThread(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED,
                                                               response.getRspChin(), true);
                                                   }
                                               }
                                );
                    }

                }
            }

        }
        )
                .start();
    }

    /**
     * 撤销
     */
    private void revocation(Double amount) {
        onPayProcess("正在撤销订单，请稍候......");
        initBankRequestValue(amount, TransType.REVOCATION);
        new Thread(new Runnable() {
            public void run() {
                try {
//                    isRunningThread = true;
                    ZLogger.d("正在撤销订单，请稍候......");
                    bankDAO = new BankDAO();
                    bankDAO.getCallBack(new BackCall());
                    response = bankDAO.bankall(transCFX, request);
                    ZLogger.wf(String.format("撤销订单结束：%s", response.toString()));
                    if (RspCode.CODE_00.equals(response.getRspCode())) {
                        //签到成功，发起消费
                        //00交易成功622760******0655    0000000000010104中国银行0000490000021653175640983970050412898320554115217558772360412165317

//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                createPayOrder(response.getTraceNo(), response.getBankCode(), response.getCardNo(),
//                                        response.getAmount(), response.getMerchId(), response.getTermId());
//                            }
//                        });
                    } else {
                        //交易失败
                        //E4版本校验失败,密钥不正确
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, response.getRspChin(), false);
                                }
                            });
                        }
                    }
//                    isRunningThread = false;
                } catch (Exception e) {
                    ZLogger.e(e.toString());
//                    isRunningThread = false;
                    if (getActivity() != null) {
                        getActivity()
                                .runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                onBarpayFailed(PosOrderPayEntity.PAY_STATUS_FAILED, response.getRspChin(), true);

                                            }
                                        }
                                );
                    }

                }
            }

        }
        )
                .start();
    }

    @Override
    protected void onPayStart(String message) {
        super.onPayStart(message);
        llPayInfo.setVisibility(View.GONE);
        payProcessView.setState(PayProcessView.STATE_PROCESS, message);
    }

    @Override
    protected void onPayProcess(String message) {
        super.onPayProcess(message);
        try {
            llPayInfo.setVisibility(View.GONE);
            payProcessView.setState(PayProcessView.STATE_PROCESS, message);

            Bundle args = new Bundle();
            args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO,
                    PaymentInfo.create(outTradeNo, payType,
                            PosOrderPayEntity.PAY_STATUS_PROCESS,
                            paidAmount, paidAmount, 0D, null));
            EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_PROCESS, args));
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
        }
    }

    /**
     * 支付成功
     */
    private void onBarpayFinished(final Double paidAmount, String msg) {
        try {
            payProcessView.setState(PayProcessView.STATE_SUCCESS, msg);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Bundle args = new Bundle();
                    args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO,
                            PaymentInfo.create(outTradeNo, payType,
                                    PosOrderPayEntity.PAY_STATUS_FINISH,
                                    paidAmount, paidAmount, 0D, null));
                    EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED, args));

                    llPayInfo.setVisibility(View.VISIBLE);
                    payProcessView.setVisibility(View.GONE);

                    bPayProcessing = false;
                }
            }, 300);
        } catch (Exception ex) {
            ZLogger.ef(ex.toString());
        }
    }

    /**
     * 交易失败
     */
    private void onBarpayFailed(int payStatus, String msg, boolean isException) {
        try {
            ZLogger.ef(String.format("银联支付失败：%s", msg));
            if (isException) {
                payProcessView.setState(PayProcessView.STATE_ERROR, msg);
            } else {
                payProcessView.setState(PayProcessView.STATE_FAILED, msg);
            }

            Bundle args = new Bundle();
            args.putSerializable(PayStep1Event.KEY_PAYMENT_INFO,
                    PaymentInfo.create(outTradeNo, payType,
                            payStatus,
                            paidAmount, paidAmount, 0D, null));
            args.putString(PayStep1Event.KEY_ERROR_MESSAGE, msg);
            EventBus.getDefault().post(new PayStep1Event(PayStep1Event.PAY_ACTION_PAYSTEP_FAILED, args));

            bPayProcessing = false;
        } catch (Exception ex) {
            ZLogger.ef(ex.toString());
        }


//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                llPayInfo.setVisibility(View.VISIBLE);
//                payProcessView.setVisibility(View.GONE);
//
////                bPayProcessing = false;
//            }
//        }, 2000);
    }

    /**
     * 初始化交易基础信息
     * 域名：upos.chinaums.com
     * ULINK传统：端口 19003，TPDU 6000030000
     */
    public void initBasicValue() {
        this.transCFX = new TransCfx();
        //公网标志位(ssl_on)	int		8	是	0-表示专线 1-表示公网
        this.transCFX.setSsl_on(0);
        //主机ip地址(ip)	String		16	是	主机ip地址
//        this.transCFX.setIp("upos.chinaums.com");
        this.transCFX.setIp(SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_UMSIPS_IP, "10.139.93.98"));//
        //主机端口号(port)	int		8	是	主机端口号
        this.transCFX.setPort(Integer.valueOf(SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_UMSIPS_PORT, "19003")));//19003
        //终端号(termId)	String	8	是	终端号
        this.transCFX.setTermId(SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_UMSIPS_TERMID, "55877236"));
        //终端信息(term_info)   	String		64	否	终端信息（公网相关）
        this.transCFX.setTerm_info("");
        //终端序列号(ssl_sn)	String		39	否	终端序列号（公网相关）
        this.transCFX.setSsl_sn("");
        //数字证书路径(ssl_cert)	String		200	否	数字证书路径（公网相关）
        this.transCFX.setSsl_cert("");
        //TPDU传送协议数据单元(tpdu)    String		10	是
        this.transCFX.setTpdu("6000030000");
        //商户号(mchtId)  String	15	是	商户号
        String mchtId = SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_UMSIPS_MCHTID,
                "898320554115217");
        this.transCFX.setMchtId(mchtId);
        //SN密文(authSN)	String		60	是	SN密文
        // TODO: 8/25/16 商户号多的情况，可能需要云端同步 
        if (mchtId.equalsIgnoreCase("898320554115217")) {
            this.transCFX.setAuthSN("277797D4DE797832B650C201EE08DC5300C1771A372DCC6E08E57799A377CF91");
        } else if (mchtId.equalsIgnoreCase("898320554115269")) {
            this.transCFX.setAuthSN("9907666AAB467BBF3A3067A9BE71FD7B54AC0A6D7B8AB996BB5C097417D453B6");
        } else {
            this.transCFX.setAuthSN("277797D4DE797832B650C201EE08DC5300C1771A372DCC6E08E57799A377CF91");
        }
        //串口值(devPath)	String		50	是	串口值
        this.transCFX.setDevPath(SerialManager.getUmsipsPort());
        //波特率(baudRate)	int		8	是	波特率
        //不同型号的波特率不同，9600&115200
        this.transCFX.setBaudRate(Integer.valueOf(SerialManager.getUmsipsBaudrate()));
//        ZLogger.d("ums.transCFX: " + this.transCFX.toString());
    }

    /**
     * 初始化交易请求
     */
    public void initBankRequestValue(Double amount, String transType) {
        this.request = new RequestPojo();
        //操作员号(operid)  String	operId	8	必填	左对齐，不足右补空格
//        MfhLoginService.get().getCurrentGuId();
        this.request.setOperId(CashierHelper.getOperateId(8));
        //POS机号(posId)    String    8   必填	左对齐，不足右补空格(0)
        this.request.setPosId(CashierHelper.getTerminalId(8));
        //交易类型(transtype)   String  2   必填
        this.request.setTransType(transType);
        //交易金额(amount)  String  12  必填  精确到分，不足左补0。比如1.23元应填写000000000123。
        this.request.setAmount(StringUtils.alignRight(String.format("%.0f", amount * 100), '0', 12));
        /**交易附加域(transMemo)	String		VAR	是	"交易类型附加信息域，域格式为data1&data2&......&dataN
         例如：手机充值需要传入运营商和手机号，则填充：01&15866668888"*/
        this.request.setTransMemo(RequestConstants.APPTYPE_BANK_CARD);
        ZLogger.d("ums.request: " + this.request.toString());
    }

    /**
     * 提交支付记录
     * 订单编号重复，会导致失败，返回null
     *
     * @param traceNO  交易凭证号	String	6	是
     * @param bankCode 发卡行代码    String	4	否	消费、撤销和退货返回，其它交易无
     * @param cardNO   卡号         String	19	否	19位卡号，消费、撤销和退货返回屏蔽卡号，其它交易无
     * @param amount   交易金额     String	12	否	交易金额，精确到分，不足左补0。比如1.23元应填写000000000123。
     * @param merchId  商户号	String	15	是
     * @param termId   终端号	String	8	是
     */
    private void createPayOrder(String traceNO, String bankCode, String cardNO, String amount,
                                String merchId, String termId) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);

            onBarpayFinished(handleAmount, response.getRspChin());
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("humanId", MfhLoginService.get().getHumanId());//订单发起人
        jsonObject.put("pointId", cardNO);//该人在该支付渠道下的标识，如微信openid、扫码支付授权码、银联银行卡号等
        jsonObject.put("transactionId", traceNO);//支付服务生成的正式订单号// TODO: 4/7/16
        jsonObject.put("appid", bankCode);//哪个银联
        jsonObject.put("mchId", merchId);//商户号
        jsonObject.put("nonceStr", orderId);//pos内部订单号
        jsonObject.put("spbillCreateIp", termId);//发起ip地址,或扫码支付pos机号
        jsonObject.put("wayType", WayType.BANKCARD);//支付途径，参见CommonAccountHistory.WAY_TYPE_CASH等系列
        jsonObject.put("totalFee", amount);//充值金额，单位分
        jsonObject.put("body", body);//订单描述
        jsonObject.put("status", 2);//订单状态，0-发起，1-支付中（预提交），2-成功， 3-失败
        jsonObject.put("sellOffice", MfhLoginService.get().getCurOfficeId());//网点，部门或公司编号，优先是部门
        jsonObject.put("bizType", bizType);//业务类型
        jsonObject.put("tagOne", "");//备用

        if (RxHttpManager.RELEASE) {
            Map<String, String> options = new HashMap<>();
            options.put("jsonStr", jsonObject.toJSONString());
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

            RxHttpManager.getInstance().createPayOrder(options,
                    new MSubscriber<String>() {

//                        @Override
//                        public void onError(Throwable e) {
//                            ZLogger.ef("提交支付记录失败:" + e.toString());
//                            onBarpayFinished(handleAmount, response.getRspChin());
//                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {
                            ZLogger.ef("提交支付记录失败:" + e.toString());
                            onBarpayFinished(handleAmount, response.getRspChin());
                        }

                        @Override
                        public void onNext(String s) {
                            ZLogger.i(String.format("提交支付记录成功:%s", s));
                            onBarpayFinished(handleAmount, response.getRspChin());
                        }

                    });
        } else {
            PayOrderApiImpl.create(jsonObject.toJSONString(), responseCallback);
        }
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(final IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":""}
                    if (rspData != null) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        ZLogger.i(String.format("提交支付记录成功:%s", retValue.getValue()));
                    }

                    onBarpayFinished(handleAmount, response.getRspChin());
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                    //{"code":"1","msg":"短信验证码验证不对，请重新输入!","version":"1","data":null}
                    ZLogger.e(String.format("提交支付记录失败:%s", errMsg));
                    // TODO: 16/4/7

                    onBarpayFinished(handleAmount, response.getRspChin());
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };


}

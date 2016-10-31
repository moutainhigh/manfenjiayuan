package com.manfenjiayuan.mixicook_vip.ui.topup;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.bingshanguxue.vector_uikit.ProfileView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.AlipayConstants;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.widget.LabelView1;
import com.manfenjiayuan.mixicook_vip.wxapi.PayEvent;
import com.manfenjiayuan.mixicook_vip.wxapi.PayResultWrapper;
import com.manfenjiayuan.mixicook_vip.wxapi.WXUtil;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.pay.AppPrePayRsp;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.api.pay.PayApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.pay.alipay.AppPayResp;
import com.mfh.framework.pay.alipay.AppPayRespWrapper;
import com.mfh.framework.pay.alipay.OrderInfoUtil2_0;
import com.mfh.framework.pay.alipay.PayResult;
import com.mfh.framework.uikit.base.BaseFragment;

import net.sourceforge.simcpux.WXHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 充值
 *
 * @author bingshanguxue created on 2015-04-13
 * @since Framework 1.0
 */
public class TopupFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.profileView)
    ProfileView mProfileView;
    @Bind(R.id.amountRecyclerView)
    RecyclerView mRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private TopupAdapter mTopupAdapter;
    @Bind(R.id.action_alipay)
    LabelView1 labelAlipay;
    @Bind(R.id.action_wepay)
    LabelView1 labelWepay;
    private static final int PAY_ACTION_ALIPAY = 1;
    private static final int PAY_ACTION_WEPAY = 2;
    private int curPayAction = PAY_ACTION_ALIPAY;


    public TopupFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_topup;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("充值");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        mProfileView.setAvatarUrl(MfhLoginService.get().getHeadimage());
        mProfileView.setPrimaryText(MfhLoginService.get().getHumanName());
        mProfileView.setSecondaryText(MfhLoginService.get().getTelephone());
        initRecyclerView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(PayEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("PayEvent(%d)", eventId));
        switch (eventId) {
            case PayEvent.EVENT_ID_ONPAYRESP: {
                btnSubmit.setEnabled(true);
                PayResultWrapper payResultWrapper = event.getPayResultWrapper();
                if (payResultWrapper != null) {
                    ZLogger.d(JSON.toJSONString(payResultWrapper));
                    //0	成功	展示成功页面
                    if (payResultWrapper.getErrCode() == 0) {
//                        topupFailed();
                        getActivity().setResult(Activity.RESULT_OK, null);
                        getActivity().finish();
                    }
                    //-1	错误	可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                    else if (payResultWrapper.getErrCode() == 0) {
                    }
                    //-2	用户取消	无需处理。发生场景：用户不支付了，点击取消，返回APP。
                    else if (payResultWrapper.getErrCode() == 0) {
                    }
                }
            }
            break;
        }
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

        List<TopAmount> entities = new ArrayList<>();
        entities.add(new TopAmount(0.01D, false));
        entities.add(new TopAmount(1D, false));
        entities.add(new TopAmount(20D, false));
        entities.add(new TopAmount(50D, false));
        entities.add(new TopAmount(100D, false));
        entities.add(new TopAmount(200D, false));
        entities.add(new TopAmount(300D, false));
        entities.add(new TopAmount(500D, false));
        entities.add(new TopAmount(1000D, false));
        mTopupAdapter = new TopupAdapter(getActivity(), entities);
        mTopupAdapter.setOnAdapterLitener(new TopupAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                topupStep1(mTopupAdapter.getEntity(position));
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mRecyclerView.setAdapter(mTopupAdapter);

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
        } else if ((curPayAction & PAY_ACTION_WEPAY) == PAY_ACTION_WEPAY) {
            labelAlipay.setChecked(false);
            labelWepay.setChecked(true);
        } else {
            labelAlipay.setChecked(false);
            labelWepay.setChecked(false);
        }
    }

    @Bind(R.id.button_submit)
    Button btnSubmit;

    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        TopAmount amount = mTopupAdapter.getCurEntity();
        topupStep1(amount);
    }

    /**
     * 充值:选择充值方式
     *
     * @param topAmount 支付金额：单位为元，最小金额为0.01元。
     */
    private void topupStep1(final TopAmount topAmount) {
        if (topAmount == null) {
            DialogUtil.showHint("请选择充值金额");
            btnSubmit.setEnabled(true);
            return;
        }

        Double amount = topAmount.getAmount();
        if ((curPayAction & PAY_ACTION_ALIPAY) == PAY_ACTION_ALIPAY) {
            if (!BizConfig.RELEASE) {
                alipay("满分家园账单充值", "支付宝充值", MUtils.formatDouble(amount, ""),
                        MUtils.genOutTradeNo());
            } else {
                topupStep2(WayType.ALI, MUtils.formatDouble(amount, ""));
            }
        } else if ((curPayAction & PAY_ACTION_WEPAY) == PAY_ACTION_WEPAY) {
            if (!BizConfig.RELEASE) {
                WXHelper.getInstance(getContext()).getPrepayId();
            } else {
                topupStep2(WayType.WEIXIN, MUtils.formatDouble(amount, ""));
            }
        } else {
            DialogUtil.showHint("请选择支付方式");
            btnSubmit.setEnabled(true);
        }
    }

    /**
     * 充值:预充值
     *
     * @param amount 支付金额：单位为元，最小金额为0.01元。
     */
    private void topupStep2(final Integer wayType, final String amount) {
//        emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
//        animProgress.setVisibility(View.VISIBLE);

        if (!NetworkUtils.isConnect(getContext())) {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            btnSubmit.setEnabled(true);
            return;
        }

        if (TextUtils.isEmpty(amount)) {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            btnSubmit.setEnabled(true);

            return;
        }

        if (WayType.ALI.equals(wayType)) {
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);

                            topupFailed(-1);
                        }

                        @Override
                        public void processResult(IResponseData rspData) {
//                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            //商户网站唯一订单号
                            String outTradeNo = retValue.getValue();
                            // TODO: 16/10/2016 支付宝推荐，金额由后台返回
                            ZLogger.d("prePayResponse: " + outTradeNo);
                            if (!TextUtils.isEmpty(outTradeNo)) {
                                //支付宝充值
                                alipay("满分家园账单充值", "支付宝充值", amount, outTradeNo);
                            } else {
                                topupFailed(-1);
                                DialogUtil.showHint("outTradeNo 不能为空");
                            }
                        }
                    }
                    , String.class
                    , MfhApplication.getAppContext()) {
            };

            PayApiImpl.prePay(MfhLoginService.get().getCurrentGuId(), amount, wayType,
                    WXUtil.genNonceStr(), responseCallback);
        } else if (WayType.WEIXIN.equals(wayType)) {
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<AppPrePayRsp,
                    NetProcessor.Processor<AppPrePayRsp>>(
                    new NetProcessor.Processor<AppPrePayRsp>() {
                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            topupFailed(-1);
                        }

                        @Override
                        public void processResult(IResponseData rspData) {
//                        com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<AppPrePayRsp> retValue = (RspBean<AppPrePayRsp>) rspData;
                            AppPrePayRsp prePayResponse = retValue.getValue();
                            ZLogger.d("prePayResponse: " + prePayResponse.toString());
                            String prepayId = prePayResponse.getPrepayId();

                            if (prepayId != null) {
                                WXHelper.getInstance(getContext()).sendPayReq(prepayId);
                            } else {
                                topupFailed(-1);
                                DialogUtil.showHint("prepayId 不能为空");
                            }
                        }
                    }
                    , AppPrePayRsp.class
                    , MfhApplication.getAppContext()) {
            };

            PayApiImpl.prePayForApp(PayApi.WEPAY_CONFIGID_MIXICOOK,
                    MfhLoginService.get().getCurrentGuId(), amount, wayType,
                    WXUtil.genNonceStr(), BizType.RECHARGE, responseCallback);
        } else {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            btnSubmit.setEnabled(true);
        }
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     * 支付行为需要在独立的非ui线程中执行
     * <p>
     * 系统繁忙，请稍后再试（ALI64）
     */
    public void alipay(final String subject, final String body, final String amount,
                       final String outTradeNo) {
        String bizContent = OrderInfoUtil2_0.buildBizContent(body, subject, outTradeNo, "30m",
                amount, AlipayConstants.SELLER);
        ZLogger.d("bizContent: \n" + bizContent);

        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(AlipayConstants.APPID,
                OrderInfoUtil2_0.ALIPAY_TRADE_APPPAY, AlipayConstants.CHARSET,
                TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS),
                AlipayConstants.ALIPAY_NOTIFY_URL, bizContent);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
        ZLogger.d("orderParam: \n" + orderParam);

        String sign = OrderInfoUtil2_0.getSign(params, AlipayConstants.RSA_PRIVATE);
        ZLogger.d("sign: \n" + sign);
        if (StringUtils.isEmpty(sign)) {
            DialogUtil.showHint("签名失败");
        }

        final String orderInfo = orderParam + "&" + sign;
        ZLogger.d("orderInfo:\n" + orderInfo);

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    // 构造PayTask 对象
                    PayTask alipay = new PayTask(getActivity());

                    // 调用支付接口，获取支付结果
                    Map<String, String> result = alipay.payV2(orderInfo, true);
                    //"系统繁忙，请稍候再试(ALI40247)" https://openclub.alipay.com/read.php?tid=250&fid=2
//                    ZLogger.d("支付宝支付完成:" + result.toString());

                    // 解析结果
//                parseAlipayResp(result);
                    //resultStatus={6001};memo={操作已经取消。};result={}
                    Message msg = new Message();
                    msg.what = ALI_PAY_FLAG;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    ZLogger.e(e.toString());
                }
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
                    parseAlipayResp((Map<String, String>) msg.obj);
                    break;
                }
                case ALI_CHECK_FLAG: {
                    DialogUtil.showHint("检查结果为：" + msg.obj);
                    btnSubmit.setEnabled(true);
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 解析支付宝处理结果
     * <p>
     * 支付宝sdk对商户的请求支付数据处理完成后，会将结果同步反馈给商户app端。
     * 1. 同步返回的数据，商户可以按照下文描述的方式在服务端验证，验证通过后，可以认为本次用户付款成功。
     * 有些时候会出现商户app在支付宝付款阶段被关闭导致无法正确收到同步结果，此时支付结果可以完全依赖服务端的异步通知。
     * 2. 由于同步通知和异步通知都可以作为支付完成的凭证，且异步通知支付宝一定会确保发送给商户服务端。
     * 为了简化集成流程，商户可以将同步结果仅仅作为一个支付结束的通知（忽略执行校验），
     * 实际支付是否成功，完全依赖服务端异步通知。
     *
     * @param resp
     */
    private void parseAlipayResp(Map<String, String> resp) {
        PayResult payResult = new PayResult(resp);
//        resultStatus={9000};memo={};result={partner="2088011585033309"&seller_id="finance@manfenjiayuan.com"&out_trade_no="138761"&subject="商品名称"&body="商品详情"&total_fee="0.01"&notify_url="http://devnew.manfenjiayuan.com/pmc/pmcstock/notifyOrder"&service="mobile.securitypay.pay"&payment_type="1"&_input_charset="utf-8"&it_b_pay="30m"&return_url="m.alipay.com"&success="true"&sign_type="RSA"&sign="OoNoZHMgXQ81Irh/DnCjEhfaEuL5lIqjxCgs05+gV/oIUUqjMffmeRf4fPuXwVsC4XpjQjdNLnCLgXqfIvpAYdt3bqDXEGV1BojgEJl1bz8HCrvT8YIAgPMY/0S9qzCDwuMNcDhcTo2dilK2isUE5AD1MjYtgmtEIWG3WDJNqIA="}
        ZLogger.d("parseAlipayResp: " + payResult.toString());

        /**
         对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
         */
        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
        String resultStatus = payResult.getResultStatus();

        DialogUtil.showHint(payResult.getMemo());

        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
        if (TextUtils.equals(resultStatus, "9000")) {
            // 注意：该笔订单是否真实支付成功，需要依赖服务端的异步通知。
            // TODO: 16/10/2016 同步通知验证
            AppPayRespWrapper appPayRespWrapper = JSONObject.toJavaObject(JSONObject.parseObject(resultInfo), AppPayRespWrapper.class);
            AppPayResp appPayResp = appPayRespWrapper.getAlipay_trade_app_pay_response();
            btnSubmit.setEnabled(true);
            getActivity().setResult(Activity.RESULT_OK, null);
            getActivity().finish();

        } else {
            // 注意：该笔订单是否真实支付成功，需要依赖服务端的异步通知。
            // 判断resultStatus 为非“9000”则代表可能支付失败
            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                btnSubmit.setEnabled(true);
            } else if (TextUtils.equals(resultStatus, "6001")) {
                topupFailed(-2);
            } else {
                topupFailed(-1);
                //6001,支付取消
                //6002,网络连接出错
                //4000,支付失败
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
            }
        }
    }

    /**
     * 充值失败
     */
    private void topupFailed(int errCode) {
        btnSubmit.setEnabled(true);
    }


}

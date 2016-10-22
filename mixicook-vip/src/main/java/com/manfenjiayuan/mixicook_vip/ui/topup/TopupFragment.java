package com.manfenjiayuan.mixicook_vip.ui.topup;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.bingshanguxue.vector_uikit.ProfileView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.AlipayConstants;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.widget.LabelView1;
import com.manfenjiayuan.mixicook_vip.wxapi.WXUtil;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.pay.AppPrePayRsp;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.api.pay.PayApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.pay.alipay.AppPayResp;
import com.mfh.framework.pay.alipay.AppPayRespWrapper;
import com.mfh.framework.pay.alipay.OrderInfoUtil2_0;
import com.mfh.framework.pay.alipay.PayResult;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;

import net.sourceforge.simcpux.WXHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.Bind;

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

    public TopupFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_topup;
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

        List<Double> entities = new ArrayList<>();
        entities.add(0.01D);
        entities.add(1D);
        entities.add(20D);
        entities.add(50D);
        entities.add(100D);
        entities.add(200D);
        entities.add(300D);
        entities.add(500D);
        entities.add(1000D);
        mTopupAdapter = new TopupAdapter(getActivity(), entities);
        mTopupAdapter.setOnAdapterLitener(new TopupAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                topupStep1(mTopupAdapter.getEntity(position));
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mRecyclerView.setAdapter(mTopupAdapter);
    }

    private CommonDialog topupDialog = null;

    /**
     * 充值:选择充值方式
     *
     * @param amount 支付金额：单位为元，最小金额为0.01元。
     */
    private void topupStep1(final Double amount) {
        if (amount == null) {
            return;
        }

        if (topupDialog == null) {
            topupDialog = DialogHelper
                    .getPinterestDialogCancelable(getContext());

            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.dialogview_topup, null);
            LabelView1 wepayPV = (LabelView1) view.findViewById(R.id.action_wepay);
            wepayPV.setOnViewListener(new LabelView1.OnViewListener() {
                @Override
                public void onClick(View v, boolean isChecked) {
                    topupDialog.dismiss();
//                    topupStep2(PayApi.WAYTYPE_WXPAY, MUtils.formatDouble(amount, ""));

                    WXHelper.getInstance(getContext()).getPrepayId();
                }

                @Override
                public void onClickCheck(boolean isChecked) {
                    topupDialog.dismiss();
                    topupStep2(PayApi.WAYTYPE_WXPAY, MUtils.formatDouble(amount, ""));
                }

                @Override
                public void onCheckedChanged(boolean isChecked) {
//                    if (isChecked) {
//                        topupDialog.dismiss();
//                        topupStep2(PayApi.WAYTYPE_WXPAY, MUtils.formatDouble(amount, ""));
//                    }
                }
            });
            LabelView1 alipayPV = (LabelView1) view.findViewById(R.id.action_alipay);
            alipayPV.setOnViewListener(new LabelView1.OnViewListener() {
                @Override
                public void onClick(View v, boolean isChecked) {
                    topupDialog.dismiss();
                    topupStep2(PayApi.WAYTYPE_ALIPAY, MUtils.formatDouble(amount, ""));
                    alipay("满分家园账单充值", "支付宝充值", MUtils.formatDouble(amount, ""), MUtils.genOutTradeNo());
                }

                @Override
                public void onClickCheck(boolean isChecked) {
                    topupDialog.dismiss();
//                    topupStep2(PayApi.WAYTYPE_ALIPAY, MUtils.formatDouble(amount, ""));
                    alipay("满分家园账单充值", "支付宝充值", MUtils.formatDouble(amount, ""), MUtils.genOutTradeNo());

                }

                @Override
                public void onCheckedChanged(boolean isChecked) {
//                    if (isChecked) {
//                        topupDialog.dismiss();
//                        topupStep2(PayApi.WAYTYPE_ALIPAY, MUtils.formatDouble(amount, ""));
//                    }
                }
            });
//            LabelView1 mfpayPV = (LabelView1) view.findViewById(R.id.action_mfpay);
//            mfpayPV.setOnViewListener(new LabelView1.OnViewListener() {
//
//                @Override
//                public void onClick(View v, boolean isChecked) {
//                    topupDialog.dismiss();
//                    DialogUtil.showHint("等待，有时候也是一种美。");
//                }
//
//                @Override
//                public void onClickCheck(boolean isChecked) {
//                    topupDialog.dismiss();
//                    DialogUtil.showHint("等待，有时候也是一种美。");
//                }
//
//                @Override
//                public void onCheckedChanged(boolean isChecked) {
////                    if (isChecked) {
////                        topupDialog.dismiss();
////                        DialogUtil.showHint("等待，有时候也是一种美。");
////                    }
//                }
//            });

            topupDialog.setContent(view);
        }
        if (!topupDialog.isShowing()) {
            topupDialog.show();
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
            return;
        }

        if (TextUtils.isEmpty(amount)) {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }

        if (wayType == PayApi.WAYTYPE_ALIPAY) {
            //回调
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
        } else if (wayType == PayApi.WAYTYPE_WXPAY) {
            //回调
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

            PayApiImpl.prePayForApp(MfhLoginService.get().getCurrentGuId(), amount, wayType,
                    WXUtil.genNonceStr(), responseCallback);
        } else {
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
            DialogUtil.showHint("参数传递错误");
            return;
        }
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
        ZLogger.d("orderInfo:\n" + orderInfo);

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
     * 支付宝sdk对商户的请求支付数据处理完成后，会将结果同步反馈给商户app端。
     * 1. 同步返回的数据，商户可以按照下文描述的方式在服务端验证，验证通过后，可以认为本次用户付款成功。
     * 有些时候会出现商户app在支付宝付款阶段被关闭导致无法正确收到同步结果，此时支付结果可以完全依赖服务端的异步通知。
     * 2. 由于同步通知和异步通知都可以作为支付完成的凭证，且异步通知支付宝一定会确保发送给商户服务端。
     * 为了简化集成流程，商户可以将同步结果仅仅作为一个支付结束的通知（忽略执行校验），
     * 实际支付是否成功，完全依赖服务端异步通知。
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
            // TODO: 16/10/2016 同步通知验证
            AppPayRespWrapper appPayRespWrapper = JSONObject.toJavaObject(JSONObject.parseObject(resultInfo), AppPayRespWrapper.class);
            AppPayResp appPayResp = appPayRespWrapper.getAlipay_trade_app_pay_response();
            if (appPayResp.getCode().equals("10000")){
                DialogUtil.showHint("支付成功");
            }
            else{
                DialogUtil.showHint(appPayResp.getMsg());
            }
        } else {
            // 注意：该笔订单是否真实支付成功，需要依赖服务端的异步通知。
            // 判断resultStatus 为非“9000”则代表可能支付失败
            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                DialogUtil.showHint("支付结果确认中");
            } else if (TextUtils.equals(resultStatus, "6001")) {
                topupFailed(-2);
                DialogUtil.showHint("支付取消");
            } else {
                topupFailed(-1);
                //6001,支付取消
                //6002,网络连接出错
                //4000,支付失败
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                DialogUtil.showHint("支付失败");
            }
        }
    }

    /**
     * 充值失败
     */
    private void topupFailed(int errCode) {

    }


}

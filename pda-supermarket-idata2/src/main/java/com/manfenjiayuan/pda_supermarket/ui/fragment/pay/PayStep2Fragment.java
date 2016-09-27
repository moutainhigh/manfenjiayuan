package com.manfenjiayuan.pda_supermarket.ui.fragment.pay;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.OrderMarketRules;
import com.manfenjiayuan.pda_supermarket.bean.PayAmount;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.CouponRule;
import com.manfenjiayuan.pda_supermarket.cashier.CashierAgent;
import com.manfenjiayuan.pda_supermarket.cashier.CashierFactory;
import com.manfenjiayuan.pda_supermarket.cashier.CashierOrderInfo;
import com.manfenjiayuan.pda_supermarket.cashier.CashierOrderInfoImpl;
import com.manfenjiayuan.pda_supermarket.cashier.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.cashier.PaymentInfoImpl;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderPayEntity;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.CommonUserAccountApi;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.framework.uikit.widget.AvatarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;



/**
 * 会员支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PayStep2Fragment extends BasePayStepFragment {
    public static final String EXTRA_KEY_PAYTYPE = "payType";
    public static final String EXTRA_KEY_PAY_SUBTYPE = "paySubType";//0:会员卡，1:付款码，2:手机号
    public static final String EXTRA_KEY_VIP_CARID = "vipCardId";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.iv_vip_header)
    AvatarView ivMemberHeader;
    @Bind(R.id.tv_vip_brief)
    TextView tvVipBrief;
    @Bind(R.id.labelHandleAmount)
    MultiLayerLabel tvHandleAmount;
    @Bind(R.id.labelRuleDiscount)
    MultiLayerLabel tvRuleDiscount;
    @Bind(R.id.labelCouponDiscount)
    MultiLayerLabel tvCouponAmount;
    @Bind(R.id.labelScore)
    MultiLayerLabel tvScore;
    @Bind(R.id.labelDealPayAmount)
    MultiLayerLabel tvDealPayAmount;
    @Bind(R.id.coupon_list)
    RecyclerViewEmptySupport couponRecyclerView;
    @Bind(R.id.empty_view)
    ImageView emptyView;
    private PayCouponAdapter couponAdapter;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    private NumberInputDialog mEnterPasswordDialog = null;

    //0:会员卡，1:付款码，2:手机号
    private int paySubType = 2;
    private String vipCardId;
    private Human mMemberInfo = null;
    private List<OrderMarketRules> mOrderMarketRules;//当前用户的促销规则和卡券

    private String bizType;
    private Long orderId;
    private String outTradeNo;
    private Double handleAmount;

    public static PayStep2Fragment newInstance(Bundle args) {
        PayStep2Fragment fragment = new PayStep2Fragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier_pay_2;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ZLogger.df(String.format("打开会员支付页面，%s", StringUtils.decodeBundle(args)));
        if (args != null) {
            cashierOrderInfo = (CashierOrderInfo) args.getSerializable(EXTRA_KEY_CASHIER_ORDERINFO);
            curPayType = args.getInt(EXTRA_KEY_PAYTYPE);
            paySubType = args.getInt(EXTRA_KEY_PAY_SUBTYPE);
            vipCardId = args.getString(EXTRA_KEY_VIP_CARID);
        }

        toolbar.setTitle("收银");
//        setSupportActionBar(toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setNavigationIcon(R.mipmap.ic_toolbar_back_normal);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        ivMemberHeader.setBorderWidth(3);
        ivMemberHeader.setBorderColor(Color.parseColor("#e8e8e8"));

        initCouponRecyclerView();

        if (cashierOrderInfo == null) {
            DialogUtil.showHint("订单支付数据错误");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            reload(cashierOrderInfo);
            //自动加载会员信息
            refreshVipMemberInfo(cashierOrderInfo.getVipMember());
        }
    }

    @Override
    protected void onScanCode(String code) {

    }

    @Override
    public void onResume() {
        super.onResume();
        couponRecyclerView.requestFocus();
    }

    private void initCouponRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        couponRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        couponRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        couponRecyclerView.setEmptyView(emptyView);
//        couponRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //添加分割线
//        couponRecyclerView.addItemDecoration(new GridItemDecoration2(CashierApp.getAppContext(), 1,
//                getActivity().getResources().getColor(R.color.material_red_500), 0,
//                getActivity().getResources().getColor(R.color.material_red_500), 0.1f,
//                getActivity().getResources().getColor(R.color.material_red_500), 0));


        couponAdapter = new PayCouponAdapter(getContext(), null);
        couponAdapter.setOnAdapterListener(new PayCouponAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged() {
            }

            @Override
            public void onToggleItem(CouponRule couponRule) {
                couponsDiscount(couponRule);
            }
        });
        couponRecyclerView.setAdapter(couponAdapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void reload(CashierOrderInfo cashierOrderInfo) {
        super.reload(cashierOrderInfo);

        //2016-07-08 交易号（设备编号＋订单编号＋时间戳）一旦发生交易，固定不变，后台会做判断是否重复支付。
        outTradeNo = CashierFactory.genTradeNo(orderId, true);
        ZLogger.df(String.format("会员支付－交易编号：%s", outTradeNo));
    }

    @Override
    protected void refresh() {
        if (cashierOrderInfo != null) {
            orderId = cashierOrderInfo.getOrderId();
            bizType = String.valueOf(cashierOrderInfo.getBizType());

            handleAmount = CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo);
            ZLogger.df(String.format("刷新收银信息，应收金额:%f\n%s", handleAmount,
                    JSONObject.toJSONString(cashierOrderInfo)));

            tvHandleAmount.setTopText(String.format("%.2f",
                    CashierOrderInfoImpl.getUnpayAmount(cashierOrderInfo)));
            tvRuleDiscount.setTopText(String.format("%.2f",
                    CashierOrderInfoImpl.getRuleDiscountAmount(cashierOrderInfo)));
            tvCouponAmount.setTopText(String.format("%.2f",
                    CashierOrderInfoImpl.getCouponDiscountAmount(cashierOrderInfo)));
            tvScore.setTopText(String.format("%.2f", Math.abs(handleAmount / 2)));
            tvDealPayAmount.setTopText(String.format("%.2f", handleAmount));

            ZLogger.df(String.format("会员支付(%d)-应收金额:%f",
                    paySubType, handleAmount));
        }
    }

    /**
     * 加载会员信息
     */
    private void refreshVipMemberInfo(Human memberInfo) {
        mMemberInfo = memberInfo;
        ZLogger.df(String.format("刷新会员信息：%s", JSON.toJSONString(memberInfo)));
        if (memberInfo != null) {
            ivMemberHeader.setAvatarUrl(memberInfo.getHeadimageUrl());
            tvVipBrief.setText(String.format("%s\n%s", memberInfo.getName(),
                    memberInfo.getMobile()));

            //自动加载优惠券
            loadCoupons();
        } else {
            ivMemberHeader.setImageResource(R.drawable.chat_tmp_user_head);
            tvVipBrief.setText("");
        }
    }


    /**
     * 加载优惠券列表
     */
//    @OnClick(R.id.empty_view)
    public void loadCoupons() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在加载优惠券信息...", false);
        btnSubmit.setEnabled(false);
        couponAdapter.setEntityList(null);
        if (cashierOrderInfo == null || mMemberInfo == null) {
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("payType", curPayType);
        jsonObject.put("humanId", mMemberInfo.getId());
        jsonObject.put("btype", cashierOrderInfo.getBizType());
        jsonObject.put("discount", cashierOrderInfo.getDiscountRate());
        jsonObject.put("createdDate", TimeUtil.format(new Date(), TimeCursor.FORMAT_YYYYMMDDHHMMSS));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        jsonObject.put("items", cashierOrderInfo.getProductsInfo());
        jsonArray.add(jsonObject);

        CashierApiImpl.findMarketRulesByOrderInfos(jsonArray.toJSONString(), marketRulesRC);
    }

    NetCallBack.QueryRsCallBack marketRulesRC = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<OrderMarketRules>(new PageInfo(1, 20)) {
        @Override
        public void processQueryResult(RspQueryResult<OrderMarketRules> rs) {
            //此处在主线程中执行。
            hideProgressDialog();
            List<OrderMarketRules> marketRules = new ArrayList<>();

            int retSize = rs.getReturnNum();
            //订单拆分，POS场景取第一个即可。
            if (retSize > 0) {
                for (EntityWrapper<OrderMarketRules> entityWrapper : rs.getRowDatas()) {
                    marketRules.add(entityWrapper.getBean());
                }
            }
            ZLogger.df(String.format("加载促销规则和优惠券成功：\n%s",
                    JSON.toJSONString(marketRules)));
            mOrderMarketRules = marketRules;
            //保存卡券
            cashierOrderInfo.couponPrivilege(mOrderMarketRules);
            //显示拆分后的卡券
            couponAdapter.digest(mOrderMarketRules);
            //计算会员/优惠券折扣金额
            couponsDiscount(null);
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
//            DialogUtil.showHint("加载卡券失败");
            btnSubmit.setEnabled(true);
            showProgressDialog(ProgressDialog.STATUS_ERROR, "加载卡券失败...", true);
        }
    }, OrderMarketRules.class, AppContext.getAppContext());

    /**
     * 计算会员/优惠券优惠金额
     */
    private void couponsDiscount(final CouponRule couponRule) {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在计算优惠金额...", true);
        btnSubmit.setEnabled(false);
        if (mMemberInfo == null) {
            //失败要重置状态
            if (couponRule != null) {
                couponRule.toggleSelected();
                couponAdapter.notifyDataSetChanged();
            }
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            if (couponRule != null) {
                couponRule.toggleSelected();
                couponAdapter.notifyDataSetChanged();
            }
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        final Map<Long, List<CouponRule>> selectCouponsMap = couponAdapter.getSelectSplitCoupons();

        JSONArray jsonstr = new JSONArray();
        JSONObject orderInfo = new JSONObject();
        orderInfo.put("humanId", mMemberInfo.getId());
        orderInfo.put("payType", curPayType);
//        jsonObject.put("discount", cashierOrderInfo.getDiscountRate());
//        jsonObject.put("discount", 1);
        orderInfo.put("amount", cashierOrderInfo.getFinalAmount() - cashierOrderInfo.getPaidAmount());
        orderInfo.put("createdDate", TimeCursor.InnerFormat.format(new Date()));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        orderInfo.put("items", cashierOrderInfo.getProductsInfo());

        JSONObject jsonstrItem = new JSONObject();
        jsonstrItem.put("rules", CashierAgent.getRuleIds(cashierOrderInfo.getOrderMarketRules()));
        jsonstrItem.put("couponsIds", CashierAgent.getSelectCouponIds(selectCouponsMap,
                cashierOrderInfo.getOrderId()));
        jsonstrItem.put("orderInfo", orderInfo);
        jsonstr.add(jsonstrItem);

        //保存
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<PayAmount,
                NetProcessor.Processor<PayAmount>>(
                new NetProcessor.Processor<PayAmount>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"14.0"}}
//                        {"code":"0","msg":"查询成功!","version":"1","data":[6.0,6.0]}
                        RspListBean<PayAmount> retValue = (RspListBean<PayAmount>) rspData;
                        List<PayAmount> amountArray = retValue.getValue();
                        if (cashierOrderInfo.saveCouponDiscount(amountArray, selectCouponsMap)) {
                            //刷新会员优惠的积分
                            if (couponAdapter != null) {
                                couponAdapter.setVipScore(CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo) / 2);
                            }
                            refresh();
                        } else {
                            if (couponRule != null) {
                                couponRule.toggleSelected();
                                couponAdapter.notifyDataSetChanged();
                            }
                        }
                        btnSubmit.setEnabled(true);
                        hideProgressDialog();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.ef(errMsg);
                        if (couponRule != null) {
                            couponRule.toggleSelected();
                            couponAdapter.notifyDataSetChanged();
                        }
                        btnSubmit.setEnabled(true);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }
                }
                , PayAmount.class
                , AppContext.getAppContext()) {
        };

        CashierApiImpl.getPayAmountByOrderInfos(cashierOrderInfo.getBizType(),
                jsonstr.toJSONString(), responseCallback);
    }

    @OnClick(R.id.button_submit)
    public void submitOrder() {
        btnSubmit.setEnabled(false);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        if (paySubType == 0) {
            onPreSubmit();

            CommonUserAccountApi.payDirectByCard(vipCardId,
                    MUtils.formatDouble(handleAmount, ""), bizType,
                    outTradeNo, payRespCallback);
        } else if (paySubType == 1) {
            onPreSubmit();

            CommonUserAccountApi.payDirectByAccount(mMemberInfo.getId(), null,
                    MUtils.formatDouble(handleAmount, ""), bizType,
                    outTradeNo, payRespCallback);
        } else {
            enterPayPassword();
        }
    }

    private void onPreSubmit(){
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在支付订单...", false);

        handleAmount = CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo);

        PaymentInfo paymentInfo = PaymentInfoImpl.genPaymentInfo(outTradeNo, curPayType,
                PosOrderPayEntity.PAY_STATUS_PROCESS,
                handleAmount, handleAmount, 0D,
                cashierOrderInfo.getDiscountInfo());

        onPayStepProcess(paymentInfo);
    }
    /**
     * 输入支付密码
     */
    private void enterPayPassword() {
        if (mEnterPasswordDialog == null) {
            mEnterPasswordDialog = new NumberInputDialog(getActivity());
            mEnterPasswordDialog.setCancelable(true);
            mEnterPasswordDialog.setCanceledOnTouchOutside(true);
        }
        mEnterPasswordDialog.initializeBarcode(EditInputType.BARCODE, "支付密码", "支付密码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
//                        inlvBarcode.setInputString(value);
                        CommonUserAccountApi.payDirectByAccount(mMemberInfo.getId(), value,
                                MUtils.formatDouble(handleAmount, ""), bizType,
                                outTradeNo, payRespCallback);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        if (!mEnterPasswordDialog.isShowing()){
            mEnterPasswordDialog.show();
        }
    }

    NetCallBack.NetTaskCallBack payRespCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"操作成功!","version":"1","data":""}
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    String retStr = retValue.getValue();
                    ZLogger.df(String.format("%s %s 支付成功: %s", outTradeNo,
                            WayType.name(curPayType), retStr));
//                    bPayProcessing = false;
                    onUpdate(PaymentInfoImpl.genPaymentInfo(outTradeNo, curPayType,
                            PosOrderPayEntity.PAY_STATUS_FINISH,
                            handleAmount, handleAmount, 0D,
                            cashierOrderInfo.getDiscountInfo()));
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知
                    //{"code":"5","msg":"余额不足，请先充值!","version":"1","data":null}
//                    bPayProcessing = false;
                    PaymentInfo paymentInfo = PaymentInfoImpl.genPaymentInfo(outTradeNo, curPayType,
                            PosOrderPayEntity.PAY_STATUS_FAILED,
                            handleAmount, handleAmount, 0D,
                            cashierOrderInfo.getDiscountInfo());
                    onPayStepFailed(paymentInfo, errMsg);
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };

    @Override
    public void onPayStepFinish() {
        super.onPayStepFinish();
        refresh();
    }

    @Override
    public void onPayStepFailed(PaymentInfo paymentInfo, String errMsg) {
        super.onPayStepFailed(paymentInfo, errMsg);

        btnSubmit.setEnabled(true);
        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
    }

    @Override
    public void onPayCancel() {
        super.onPayCancel();
    }

    @Override
    public void onPayFinished() {
        btnSubmit.setEnabled(true);
        hideProgressDialog();
        super.onPayFinished();
    }

}

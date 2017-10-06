package com.manfenjiayuan.pda_supermarket.ui.pay.order;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.CashierAgent;
import com.manfenjiayuan.pda_supermarket.cashier.CashierBenchObservable;
import com.manfenjiayuan.pda_supermarket.cashier.CashierFactory;
import com.manfenjiayuan.pda_supermarket.cashier.CashierProvider;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosOrderPayEntity;
import com.manfenjiayuan.pda_supermarket.cashier.model.CashierOrderInfo;
import com.manfenjiayuan.pda_supermarket.cashier.model.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.cashier.model.wrapper.CouponRule;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.httpmgr.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.httpmgr.PmcStockHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;


/**
 * 会员支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PayStep2Fragment extends BasePdaPayStepFragment {
    public static final String EXTRA_KEY_PAYTYPE = "payType";
    public static final String EXTRA_KEY_PAY_SUBTYPE = "paySubType";//0:会员卡，1:付款码，2:手机号
    public static final String EXTRA_KEY_VIP_CARID = "vipCardId";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_vip_header)
    AvatarView ivMemberHeader;
    @BindView(R.id.tv_vip_brief)
    TextView tvVipBrief;
    @BindView(R.id.labelHandleAmount)
    MultiLayerLabel tvHandleAmount;
    @BindView(R.id.labelRuleDiscount)
    MultiLayerLabel tvRuleDiscount;
    @BindView(R.id.labelPromotion)
    MultiLayerLabel labelPromotion;
    @BindView(R.id.labelCouponDiscount)
    MultiLayerLabel tvCouponAmount;
    @BindView(R.id.labelScore)
    MultiLayerLabel tvScore;
    @BindView(R.id.labelDealPayAmount)
    MultiLayerLabel tvDealPayAmount;
    @BindView(R.id.coupon_list)
    RecyclerViewEmptySupport couponRecyclerView;
    @BindView(R.id.empty_view)
    ImageView emptyView;
    private PayCouponAdapter couponAdapter;
    @BindView(R.id.fab_submit)
    FloatingActionButton btnSubmit;

    private NumberInputDialog mEnterPasswordDialog = null;

    //0:会员卡，1:付款码，2:手机号
    private int paySubType = 2;
    private String vipCardId;
    private Human mMemberInfo = null;
    private List<MarketRulesWrapper> mOrderMarketRules;//当前用户的促销规则和卡券

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

        CashierBenchObservable.getInstance().addObserver(mObserver);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mObserver != null) {
            CashierBenchObservable.getInstance().deleteObserver(mObserver);
        }
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
            try {
                orderId = cashierOrderInfo.getOrderId();
                bizType = String.valueOf(cashierOrderInfo.getBizType());

                handleAmount = CashierProvider.getHandleAmount(cashierOrderInfo);

                tvHandleAmount.setTopText(String.format("%.2f",
                        CashierProvider.getUnpayAmount(cashierOrderInfo)));
                tvRuleDiscount.setTopText(String.format("%.2f",
                        CashierBenchObservable.getInstance().getItemRuleAmount()));
                labelPromotion.setTopText(String.format("%.2f",
                        CashierBenchObservable.getInstance().getPackRuleAmount()));
                tvCouponAmount.setTopText(String.format("%.2f",
                        CashierBenchObservable.getInstance().getCouponAmount()));
                tvScore.setTopText(String.format("%.0f", Math.abs(handleAmount / 2)));
                tvDealPayAmount.setTopText(String.format("%.2f", handleAmount));

                //显示应付款
//        SerialManager.show(2, cashierOrderInfo.getHandleAmount());
                ZLogger.df(String.format("会员支付(%d)-应收金额:%f\n%s",
                        paySubType, handleAmount, JSONObject.toJSONString(cashierOrderInfo)));
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.ef(e.toString());
            }

        }
    }

    private Observer mObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            refresh();
        }
    };

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
        Date rightNow = TimeUtil.getCurrentDate();

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("payType", curPayType);
        jsonObject.put("humanId", mMemberInfo.getId());
        jsonObject.put("btype", cashierOrderInfo.getBizType());
        jsonObject.put("discount", cashierOrderInfo.getDiscountRate());
        jsonObject.put("createdDate", TimeUtil.format(rightNow, TimeCursor.FORMAT_YYYYMMDDHHMMSS));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        jsonObject.put("items", cashierOrderInfo.getProductsInfo());
        jsonArray.add(jsonObject);

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonArray.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        PmcStockHttpManager.getInstance().findMarketRulesByOrderInfos(options,
                new MQuerySubscriber<MarketRulesWrapper>(new PageInfo(1, 20)){
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        btnSubmit.setEnabled(true);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, "加载卡券失败...", true);
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MarketRulesWrapper> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        hideProgressDialog();

                        ZLogger.df(String.format("加载促销规则和优惠券成功：\n%s",
                                JSON.toJSONString(dataList)));
                        mOrderMarketRules = dataList;
                        //保存卡券
                        cashierOrderInfo.couponPrivilege(mOrderMarketRules);
                        //显示拆分后的卡券
                        couponAdapter.digest(mOrderMarketRules);
                        //计算会员/优惠券折扣金额
                        couponsDiscount(null);
                    }
                });
    }

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

        final String couponsIds = CashierAgent.getSelectCouponIds(couponAdapter.getEntityList());
        final String rulesIds = CashierAgent.getRuleIds(cashierOrderInfo.getOrderMarketRules());
        Date rightNow = TimeUtil.getCurrentDate();


        JSONObject jsonstr = new JSONObject();
        jsonstr.put("humanId", mMemberInfo.getId());
        jsonstr.put("bizType", String.valueOf(cashierOrderInfo.getBizType()));
        jsonstr.put("payType", curPayType);
//        jsonObject.put("discount", cashierOrderInfo.getDiscountRate());
//        jsonObject.put("discount", 1);
        jsonstr.put("amount", cashierOrderInfo.getFinalAmount() - cashierOrderInfo.getPaidAmount());
        jsonstr.put("createdDate", TimeCursor.InnerFormat.format(rightNow));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        jsonstr.put("items", cashierOrderInfo.getProductsInfo());

        Map<String, String> options = new HashMap<>();
        options.put("version", "2");//"1|2"
        options.put("ruleIds", rulesIds);
        options.put("couponsIds", couponsIds);
        options.put("jsonStr", jsonstr.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        CommonUserAccountHttpManager.getInstance().getPayAmountByOrderInfo(options,
                new Subscriber<PayAmount>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        if (couponRule != null) {
                            couponRule.toggleSelected();
                            couponAdapter.notifyDataSetChanged();
                        }
                        btnSubmit.setEnabled(true);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                    }

                    @Override
                    public void onNext(PayAmount payAmount) {
                        if (payAmount != null) {
                            payAmount.setCouponsIds(couponsIds);
                            payAmount.setRuleIds(rulesIds);
                            cashierOrderInfo.setPayAmount(payAmount);
                            //刷新会员优惠的积分
                            if (couponAdapter != null) {
                                couponAdapter.setVipScore(CashierProvider.getHandleAmount(cashierOrderInfo) / 2);
                            }
                            CashierBenchObservable.getInstance().setCashierOrderInfo(cashierOrderInfo);

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
                });
    }

    @OnClick(R.id.fab_submit)
    public void submitOrder() {
        btnSubmit.setEnabled(false);
//        DeviceUtils.hideSoftInput(getContext(), etBarCode);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        if (paySubType == 0) {
            Map<String, String> options = new HashMap<>();
            if (!StringUtils.isEmpty(vipCardId)) {
                options.put("cardNo", vipCardId);
            }
//        params.put("accountPassword", accountPassword);
            options.put("amount", MUtils.formatDouble(handleAmount, ""));
            options.put("bizType", bizType);
            options.put("orderId", outTradeNo);
            options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

            payDirect(options);
        } else if (paySubType == 1) {
            payByAccountPassword(null);

        } else {
            enterPayPassword();
        }
    }

    private void onPreSubmit(){
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在支付订单...", false);

        handleAmount = CashierProvider.getHandleAmount(cashierOrderInfo);

        PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, curPayType,
                PosOrderPayEntity.PAY_STATUS_PROCESS,
                handleAmount, handleAmount, 0D,
                cashierOrderInfo.getPayAmount());

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
                        payByAccountPassword(value);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        if (!mEnterPasswordDialog.isShowing()){
            mEnterPasswordDialog.show();
        }
    }


    private void payByAccountPassword(String password) {
        onPreSubmit();

        Map<String, String> options = new HashMap<>();
        options.put("humanId", String.valueOf(mMemberInfo.getId()));
        if (!StringUtils.isEmpty(password)) {
            options.put("accountPassword", password);
        }
        options.put("amount", MUtils.formatDouble(handleAmount, ""));
        options.put("bizType", bizType);
        options.put("orderId", outTradeNo);
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        payDirect(options);
    }

    private void payDirect(Map<String, String> options) {
        CommonUserAccountHttpManager.getInstance().payDirect(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, curPayType,
                                PosOrderPayEntity.PAY_STATUS_FAILED,
                                handleAmount, handleAmount, 0D,
                                cashierOrderInfo.getPayAmount());
                        onPayStepFailed(paymentInfo, e.getMessage());
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);

                        ZLogger.df(String.format("%s %s 支付成功", outTradeNo,
                                WayType.getWayTypeName(curPayType)));
                        Double balance = 0D;
                        if (data != null) {
                            balance = Double.valueOf(data);
                        }
//                    bPayProcessing = false;
//                        Human human = cashierOrderInfo.getVipMember();
                        PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, curPayType,
                                PosOrderPayEntity.PAY_STATUS_FINISH,
                                handleAmount, handleAmount,
                                balance,
                                cashierOrderInfo.getPayAmount());

                        onUpdate(paymentInfo);
                    }

                });
    }



    @Override
    public void onPayStepFinish() {
        super.onPayStepFinish();

        btnSubmit.setEnabled(true);
        hideProgressDialog();
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

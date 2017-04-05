package com.mfh.litecashier.ui.fragment.pay;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.hardware.led.LedAgent;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.bingshanguxue.cashier.pay.BasePayStepFragment;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.bingshanguxue.cashier.v1.CashierDesktopObservable;
import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.bingshanguxue.cashier.v1.CashierOrderInfoImpl;
import com.bingshanguxue.cashier.v1.PaymentInfo;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.http.PmcStockHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.PayCouponAdapter;
import com.mfh.litecashier.ui.dialog.EnterPasswordDialog;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * 会员支付
 * Created by bingshanguxue on 15/8/30.
 */
public class PayStep2Fragment extends BasePayStepFragment {
    public static final String EXTRA_KEY_PAYTYPE = "payType";
    public static final String EXTRA_KEY_PAY_SUBTYPE = "paySubType";//0:会员卡，1:付款码，2:手机号
    public static final String EXTRA_KEY_VIP_CARID = "vipCardId";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_vip_header)
    AvatarView ivMemberHeader;
    @BindView(R.id.labelHandleAmount)
    MultiLayerLabel tvHandleAmount;
    @BindView(R.id.labelRuleDiscount)
    MultiLayerLabel tvRuleDiscount;
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
    @BindView(R.id.button_submit)
    Button btnSubmit;

    private EnterPasswordDialog mEnterPasswordDialog = null;

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
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    cancelSettle();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_normal);
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
    public void onResume() {
        super.onResume();
        couponRecyclerView.requestFocus();
    }

    private void initCouponRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(CashierApp.getAppContext(), 3);
        couponRecyclerView.setLayoutManager(gridLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        couponRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        couponRecyclerView.setEmptyView(emptyView);
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
        CashierDesktopObservable.getInstance().setCashierOrderInfo(cashierOrderInfo);
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
            tvScore.setTopText(String.format("%.0f", Math.abs(handleAmount / 2)));
            tvDealPayAmount.setTopText(String.format("%.2f", handleAmount));

            //显示应付款
//        SerialManager.show(2, cashierOrderInfo.getHandleAmount());
            LedAgent.vfdShow(String.format("Total:%.2f", handleAmount));
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

            //自动加载优惠券
            loadCoupons();
        } else {
            ivMemberHeader.setImageResource(R.drawable.chat_tmp_user_head);
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

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonArray.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        PmcStockHttpManager.getInstance().findMarketRulesByOrderInfos(options,
                new MQuerySubscriber<MarketRulesWrapper>(new PageInfo(1, 20)) {
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

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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
        jsonstrItem.put("rules", rulesIds);
        jsonstrItem.put("couponsIds", couponsIds);
        jsonstrItem.put("orderInfo", orderInfo);
        jsonstr.add(jsonstrItem);


        Map<String, String> options = new HashMap<>();
        options.put("bizType", String.valueOf(cashierOrderInfo.getBizType()));
        options.put("jsonStr", jsonstr.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        CommonUserAccountHttpManager.getInstance().getPayAmountByOrderInfos(options,
                new Subscriber<List<PayAmount>>() {
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
                    public void onNext(List<PayAmount> payAmounts) {
                        if (cashierOrderInfo.saveCouponDiscount(payAmounts, couponsIds, rulesIds)) {
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
                });
    }

    @OnClick(R.id.button_submit)
    public void submitOrder() {
        btnSubmit.setEnabled(false);

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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

    private void onPreSubmit() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在支付订单...", false);

        handleAmount = CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo);

        PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, curPayType,
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
            mEnterPasswordDialog = new EnterPasswordDialog(getActivity());
            mEnterPasswordDialog.setCancelable(true);
            mEnterPasswordDialog.setCanceledOnTouchOutside(true);
        }
        mEnterPasswordDialog.init("支付密码", new EnterPasswordDialog.OnEnterPasswordListener() {
            @Override
            public void onSubmit(String password) {
                payByAccountPassword(password);
            }

            @Override
            public void onCancel() {
                btnSubmit.setEnabled(true);
            }
        });
        mEnterPasswordDialog.show();
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

    /**
     * 会员支付
     * */
    private void payDirect(Map<String, String> options) {
        CommonUserAccountHttpManager.getInstance().payDirect(options,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, curPayType,
                                PosOrderPayEntity.PAY_STATUS_FAILED,
                                handleAmount, handleAmount, 0D,
                                cashierOrderInfo.getDiscountInfo());
                        onPayStepFailed(paymentInfo, e.toString());
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.df(String.format("%s %s 支付成功: %s", outTradeNo,
                                WayType.getWayTypeName(curPayType), s));
//                    bPayProcessing = false;
                        PaymentInfo paymentInfo = PaymentInfo.create(outTradeNo, curPayType,
                                PosOrderPayEntity.PAY_STATUS_FINISH,
                                handleAmount, handleAmount, 0D,
                                cashierOrderInfo.getDiscountInfo());

                        onUpdate(paymentInfo);
                    }
                });
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

    @Override
    public void onPayStepFinish() {
        btnSubmit.setEnabled(true);
        hideProgressDialog();
        super.onPayStepFinish();
    }
}

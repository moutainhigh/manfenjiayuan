package com.mfh.litecashier.components.pay;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.CashierBenchObservable;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.CashierProvider;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.hardware.led.LedAgent;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.BasePayStepFragment;
import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
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
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.components.customer.ExchangeScoreDialogFragment;
import com.mfh.litecashier.components.customer.topup.TransferDialogFragment;
import com.mfh.litecashier.ui.adapter.PayCouponAdapter;
import com.mfh.litecashier.ui.widget.CustomerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
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
 * 收银支付
 * Created by bingshanguxue on 15/8/30.
 */
public class CashierPayFragment extends BasePayStepFragment {
    public static final String EXTRA_KEY_VIP_CARID = "vipCardId";


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.customer_view)
    CustomerView mCustomerView;
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
    @BindView(R.id.tabstrip_pay)
    TopSlidingTabStrip paySlidingTabStrip;
    @BindView(R.id.tab_viewpager)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;
    private PayCouponAdapter couponAdapter;
    @BindView(R.id.fab_give)
    ImageButton fabGive;


    /**
     * 0:会员卡，1:付款码，2:手机号
     */
    private int paySubType = 2;
    private String vipCardId;
    private Human mMemberInfo = null;
    private List<MarketRulesWrapper> mOrderMarketRules;//当前用户的促销规则和卡券

    private String bizType;
    private Long orderId;
    private String outTradeNo;
    private Double handleAmount;

    public static CashierPayFragment newInstance(Bundle args) {
        CashierPayFragment fragment = new CashierPayFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier_pay;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ZLogger.d(String.format("打开支付页面，%s", StringUtils.decodeBundle(args)));
        if (args != null) {
            cashierOrderInfo = (CashierOrderInfo) args.getSerializable(EXTRA_KEY_CASHIER_ORDERINFO);
            mMemberInfo = (Human) args.getSerializable(EXTRA_KEY_HUMAN);
            curPayType = args.getInt(EXTRA_KEY_PAYTYPE);
            paySubType = args.getInt(EXTRA_KEY_PAY_SUBTYPE);
            vipCardId = args.getString(EXTRA_KEY_VIP_CARID);
        }

        toolbar.setTitle(R.string.title_cashier);
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

        initCouponRecyclerView();
        initTabs();
        mCustomerView.registerCustomerViewListener(mOnCustomerVierListener);
        mCustomerView.reload(mMemberInfo);

        if (cashierOrderInfo == null) {
            DialogUtil.showHint("订单支付数据错误");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            CashierBenchObservable.getInstance().addObserver(mObserver);

            reload(cashierOrderInfo);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (couponRecyclerView != null) {
            couponRecyclerView.requestFocus();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mObserver != null) {
            CashierBenchObservable.getInstance().deleteObserver(mObserver);
        }
        EventBus.getDefault().unregister(this);
    }

    private void initCouponRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(CashierApp.getAppContext(), 3);
        couponRecyclerView.setLayoutManager(gridLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        couponRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        couponRecyclerView.setEmptyView(emptyView);

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
    public void reload(CashierOrderInfo cashierOrderInfo) {
        super.reload(cashierOrderInfo);

        //2016-07-08 交易号（设备编号＋订单编号＋时间戳）一旦发生交易，固定不变，后台会做判断是否重复支付。
        outTradeNo = CashierFactory.genTradeNo(orderId, true);
        ZLogger.d(String.format("会员支付－交易编号：%s", outTradeNo));

        //自动加载优惠券
        loadCoupons();
    }

    @Override
    protected void refresh() {
        if (cashierOrderInfo != null) {
            try {
                orderId = cashierOrderInfo.getOrderId();
                bizType = String.valueOf(cashierOrderInfo.getBizType());

                handleAmount = CashierProvider.getHandleAmount(cashierOrderInfo);

                tvHandleAmount.setTopText(MUtils.formatDouble(CashierProvider.getUnpayAmount(cashierOrderInfo)));
                tvRuleDiscount.setTopText(MUtils.formatDouble(CashierBenchObservable.getInstance().getItemRuleAmount()));
                labelPromotion.setTopText(MUtils.formatDouble(CashierBenchObservable.getInstance().getPackRuleAmount()));
                tvCouponAmount.setTopText(MUtils.formatDouble(CashierBenchObservable.getInstance().getCouponAmount()));
                tvScore.setTopText(String.format("%.0f", Math.abs(handleAmount / 2)));
                tvDealPayAmount.setTopText(MUtils.formatDouble(handleAmount));

                //显示应付款
//        SerialManager.show(2, cashierOrderInfo.getHandleAmount());
                LedAgent.vfdShow(String.format("Total:%.2f", handleAmount));
                ZLogger.d(String.format("会员支付(%d)-应收金额:%f\n%s",
                        paySubType, handleAmount, JSONObject.toJSONString(cashierOrderInfo)));
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }
        notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PayStep1Event event) {
        ZLogger.d(String.format("CashierPayFragment:%d\n%s",
                event.getAction(), StringUtils.decodeBundle(event.getArgs())));
        switch (event.getAction()) {
            case PayStep1Event.PAY_ACTION_WAYTYPE_UPDATED: {
                notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());
            }
            break;
            //支付处理中
            case PayStep1Event.PAY_ACTION_PAYSTEP_PROCESS: {
                activeMode(false);

                PaymentInfo paymentInfo = (PaymentInfo) event.getArgs()
                        .getSerializable(PayActionEvent.KEY_PAYMENT_INFO);
                onPayStepProcess(paymentInfo);
            }
            break;
            //支付失败
            case PayStep1Event.PAY_ACTION_PAYSTEP_FAILED: {
                activeMode(true);

                PaymentInfo paymentInfo = (PaymentInfo) event.getArgs()
                        .getSerializable(PayActionEvent.KEY_PAYMENT_INFO);
                String errMsg = event.getArgs().getString(PayActionEvent.KEY_ERROR_MESSAGE);
                onPayStepFailed(paymentInfo, errMsg);
            }
            break;
            //支付成功
            case PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED: {
                activeMode(true);

                PaymentInfo paymentInfo = (PaymentInfo) event.getArgs()
                        .getSerializable(PayActionEvent.KEY_PAYMENT_INFO);
                onUpdate(paymentInfo);
            }
            break;
        }
    }

    @Override
    public void back2MainActivity() {
        if (mObserver != null) {
            CashierBenchObservable.getInstance().deleteObserver(mObserver);
        }
        super.back2MainActivity();
    }

    /**
     * */
    public void activeMode(boolean isActive) {
        mViewPager.setScrollEnabled(isActive);
        paySlidingTabStrip.setClickEnabled(isActive);

//        if (isActive) {
//            fabGive.setVisibility(View.VISIBLE);
//        } else {
//            fabGive.setVisibility(View.GONE);
//        }
    }

    /**
     * 赠送
     */
    @OnClick(R.id.fab_give)
    public void onClickGive() {
        showConfirmDialog("确认要赠送吗？",
                getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        CashierAgent.updateCashierOrder(cashierOrderInfo, null, PosOrderEntity.ORDER_STATUS_FINISH);

                        onPayFinished();
                    }
                }, getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ZLogger.i("取消赠送");
                    }
                });
    }

    private CustomerView.OnCustomerVierListener mOnCustomerVierListener = new CustomerView.OnCustomerVierListener() {
        @Override
        public void onClickTopup() {
            TransferDialogFragment mdf = TransferDialogFragment.newInstance(mMemberInfo);
            mdf.setOnDialogListener(mTransferListener);
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            mdf.show(ft, "transferTopup");
        }

        @Override
        public void onClickDeduct() {
            ExchangeScoreDialogFragment mdf = ExchangeScoreDialogFragment.newInstance(mMemberInfo);
            mdf.setOnDialogListener(mExchangeScoreListener);
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            mdf.show(ft, "exchangeScore");
        }
    };

    private TransferDialogFragment.OnDialogListener mTransferListener = new TransferDialogFragment.OnDialogListener() {
        @Override
        public void onSuccess() {
            reloadVIP();
        }

        @Override
        public void onCancel() {
        }
    };

    private ExchangeScoreDialogFragment.OnDialogListener mExchangeScoreListener = new ExchangeScoreDialogFragment.OnDialogListener() {
        @Override
        public void onSuccess() {

            reloadVIP();
        }

        @Override
        public void onCancel() {
        }
    };

    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyPayInfoChanged(page);
            }
        });

        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        Bundle parArgs = new Bundle();
        parArgs.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO,
                mMemberInfo);
        parArgs.putInt(BasePayStepFragment.EXTRA_KEY_PAY_SUBTYPE,
                paySubType);
        parArgs.putLong(BasePayFragment.EXTRA_KEY_ORDER_ID,
                cashierOrderInfo.getOrderId());
        parArgs.putString(BasePayFragment.EXTRA_KEY_BODY,
                cashierOrderInfo.getBody());
        parArgs.putString(BasePayFragment.EXTRA_KEY_ORDER_BARCODE,
                cashierOrderInfo.getPosTradeNo());
        parArgs.putString(BasePayFragment.EXTRA_KEY_SUBJECT,
                cashierOrderInfo.getSubject());
        parArgs.putString(BasePayFragment.EXTRA_KEY_BIZ_TYPE,
                String.valueOf(cashierOrderInfo.getBizType()));
        parArgs.putDouble(BasePayFragment.EXTRA_KEY_HANDLE_AMOUNT,
                CashierProvider.getHandleAmount(cashierOrderInfo));

        if (mMemberInfo != null) {
            mTabs.add(new ViewPageInfo("会员", "会员", PayByVipFragment.class,
                    parArgs));
        }
        mTabs.add(new ViewPageInfo("支付宝", "支付宝", PayByAlipayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("微信", "微信", PayByWxpayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("银行卡", "银行卡", PayByBandcardFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("现金", "现金", PayByCashFragment.class,
                parArgs));

        viewPagerAdapter.addAllTab(mTabs);

        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    /**
     * 通知：应付信息变化
     */

    private void notifyPayInfoChanged(int page) {
        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putDouble(BasePayFragment.EXTRA_KEY_HANDLE_AMOUNT, handleAmount);

        ViewPageInfo viewPageInfo = viewPagerAdapter.getTab(page);
        if ("支付宝".equals(viewPageInfo.tag)) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_ALIPAY);
            curPayType = WayType.ALI_F2F;
        } else if ("微信".equals(viewPageInfo.tag)) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_WX);
            curPayType = WayType.WX_F2F;
        } else if ("银行卡".equals(viewPageInfo.tag)) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_BANK);
            curPayType = WayType.BANKCARD;
        } else if ("会员".equals(viewPageInfo.tag)) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP);
            curPayType = WayType.VIP;
            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, mMemberInfo);
        } else {
            curPayType = WayType.CASH;
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
        }
        ZLogger.i(String.format("切换到‘%s’支付", WayType.getWayTypeName(curPayType)));
        intent.putExtras(extras);
        getContext().sendBroadcast(intent);
    }


    private Observer mObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            refresh();
        }
    };


    private void reloadVIP() {
        if (mMemberInfo == null) {
            return;
        }
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在加载会员信息...", false);

        Map<String, String> options = new HashMap<>();
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        options.put("humanId", String.valueOf(mMemberInfo.getId()));
        RxHttpManager.getInstance().getCustomerByOther(options,
                new Subscriber<Human>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                    }

                    @Override
                    public void onNext(Human human) {
                        hideProgressDialog();

                        if (human != null) {
                            mMemberInfo = human;
                            mCustomerView.reload(human);
                        }
                    }
                });
    }


    /**
     * 加载优惠券列表
     */
//    @OnClick(R.id.empty_view)
    public void loadCoupons() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在加载优惠券信息...", false);
        couponAdapter.setEntityList(null);
        if (cashierOrderInfo == null || mMemberInfo == null) {
            hideProgressDialog();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
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
                new MQuerySubscriber<MarketRulesWrapper>(new PageInfo(1, 20)) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.e(e.toString());
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MarketRulesWrapper> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        hideProgressDialog();

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
        if (mMemberInfo == null) {
            //失败要重置状态
            if (couponRule != null) {
                couponRule.toggleSelected();
                couponAdapter.notifyDataSetChanged();
            }
            hideProgressDialog();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            if (couponRule != null) {
                couponRule.toggleSelected();
                couponAdapter.notifyDataSetChanged();
            }
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
                        hideProgressDialog();
                    }
                });
    }

}

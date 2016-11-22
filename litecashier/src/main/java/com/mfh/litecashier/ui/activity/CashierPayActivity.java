package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.PayCouponAdapter;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.mfh.framework.api.invOrder.CashierApiImpl;
import com.mfh.framework.api.constant.WayType;
import com.mfh.litecashier.bean.CouponRule;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.bean.MarketRules;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.database.logic.PosOrderPayService;
import com.mfh.litecashier.event.MfPayEvent;
import com.mfh.litecashier.ui.dialog.QueryDialog;
import com.mfh.litecashier.ui.fragment.pay.BasePayFragment;
import com.mfh.litecashier.ui.fragment.pay.PayByAlipayFragment;
import com.mfh.litecashier.ui.fragment.pay.PayByBandcardFragment;
import com.mfh.litecashier.ui.fragment.pay.PayByCashFragment;
import com.mfh.litecashier.ui.fragment.pay.PayByCreditFragment;
import com.mfh.litecashier.ui.fragment.pay.PayByMfAccountFragment;
import com.mfh.litecashier.ui.fragment.pay.PayByMfcardFragment;
import com.mfh.litecashier.ui.fragment.pay.PayByWxpayFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;
import com.mfh.litecashier.utils.CashierHelper;

import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 收银订单支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CashierPayActivity extends BaseActivity {
    public static final String EXTRA_KEY_ANIM_TYPE = "animationType";
    public static final String EXTRA_KEY_CASHIER_ORDERINFO = "cashierOrderInfo";

    @Bind(R.id.tv_header_title)
    TextView tvTitle;
    @Bind(R.id.button_header_close)
    ImageButton ibHeaderClose;
    @Bind(R.id.iv_member_header)
    AvatarView ivMemberHeader;
    @Bind(R.id.tv_member_balance)
    TextView tvMemberBalance;
    @Bind(R.id.tv_member_score)
    TextView tvMemberScore;
    @Bind(R.id.tv_handle_amount)
    TextView tvHandleAmount;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @Bind(R.id.tv_discount_amount)
    TextView tvDiscountAmount;
    @Bind(R.id.tv_coupon_amount)
    TextView tvCouponAmount;
    @Bind(R.id.tv_paid_amount)
    TextView tvPaidAmount;
    @Bind(R.id.tabstrip_pay)
    TopSlidingTabStrip paySlidingTabStrip;
    @Bind(R.id.tab_viewpager)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;
    @Bind(R.id.frame_coupon)
    RelativeLayout frameCoupon;
    @Bind(R.id.coupon_list)
    RecyclerViewEmptySupport couponRecyclerView;
    @Bind(R.id.empty_view)
    TextView emptyView;

    private PayCouponAdapter couponAdapter;

    private CashierOrderInfo cashierOrderInfo = null;
    private Integer curPayType = WayType.NA;

    private QueryDialog dialog;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, CashierPayActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_mf_pay;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        handleIntent();

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tvTitle.setText("结算");
        ivMemberHeader.setBorderWidth(3);
        ivMemberHeader.setBorderColor(Color.parseColor("#e8e8e8"));

        //加载结算信息
        if (cashierOrderInfo == null) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        initCouponRecyclerView();
        initTabs();

        refreshVipMemberInfo();
        refreshPayInfo(false);

        //显示应付款
//        SerialManager.show(2, cashierOrderInfo.getHandleAmount());
        SerialManager.vfdShow(String.format("Total:%.2f", cashierOrderInfo.getHandleAmount()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private CommonDialog cancelPayDialog = null;
    /**
     * 取消支付
     */
    @OnClick(R.id.button_header_close)
    public void cancelSettle() {
//        setResult(Activity.RESULT_CANCELED);
//        finish();
        if (cancelPayDialog == null){
            cancelPayDialog = new CommonDialog(this);
            cancelPayDialog.setCancelable(true);
            cancelPayDialog.setCanceledOnTouchOutside(true);
            cancelPayDialog.setMessage("确定要取消支付吗？");
        }
        cancelPayDialog.setPositiveButton("支付异常", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                CashierHelper.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_EXCEPTION);
                cashierOrderInfo.setStatus(PosOrderEntity.ORDER_STATUS_EXCEPTION);
                notifyPayFinished();
            }
        });
        cancelPayDialog.setNegativeButton("取消支付", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                CashierHelper.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_STAY_PAY);
                cashierOrderInfo.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
        cancelPayDialog.show();
    }

    /**
     * 登录会员
     */
    @OnClick(R.id.iv_member_header)
    public void showMemberCard() {
        if (dialog == null) {
            dialog = new QueryDialog(this);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        dialog.init(QueryDialog.DT_MEMBER_CARD, new QueryDialog.DialogListener() {
            @Override
            public void query(String text) {

            }

            @Override
            public void onNextStep(String fee) {
            }

            @Override
            public void onNextStep(Human human) {
                cashierOrderInfo.vipPrivilege(human);
                refreshVipMemberInfo();
                refreshPayInfo(false);

                loadCoupons();
            }

            @Override
            public void onNextStep() {
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }

            cashierOrderInfo = (CashierOrderInfo) intent.getSerializableExtra(EXTRA_KEY_CASHIER_ORDERINFO);
        }
    }


    private void initCouponRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        couponRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        couponRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        couponRecyclerView.setEmptyView(emptyView);
        //添加分割线
        couponRecyclerView.addItemDecoration(new LineItemDecoration(
                this, LineItemDecoration.HORIZONTAL_LIST, 0, 16));


        couponAdapter = new PayCouponAdapter(this, null);
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

    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyPayInfoChanged(page);
                if (page == 1 || page == 2 || page == 4 || page == 5) {
                    if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                        DialogUtil.showHint("网络异常,请选择其他支付方式");
//                        paySlidingTabStrip.setSelected();
                    }
                }
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getSupportFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        Bundle parArgs = new Bundle();
        parArgs.putString(BasePayFragment.EXTRA_KEY_ORDER_ID, cashierOrderInfo.getOrderId());
        parArgs.putString(BasePayFragment.EXTRA_KEY_ORDER_BARCODE, cashierOrderInfo.getOrderBarcode());
        parArgs.putString(BasePayFragment.EXTRA_KEY_SUBJECT, cashierOrderInfo.getSubject());
        parArgs.putString(BasePayFragment.EXTRA_KEY_BODY, cashierOrderInfo.getBody());
        parArgs.putString(BasePayFragment.EXTRA_KEY_BIZ_TYPE, String.valueOf(cashierOrderInfo.getBizType()));

//        cashArags.putString(BasePayFragment.EXTRA_KEY_SUBJECT, cashierOrderInfo.getSubject());
//        cashArags.putString(BasePayFragment.EXTRA_KEY_BODY, cashierOrderInfo.getBody());
//        cashArags.putString(BasePayFragment.EXTRA_KEY_BIZ_TYPE, String.valueOf(cashierOrderInfo.getBizType()));
        mTabs.add(new ViewPageInfo("现金", "现金", PayByCashFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("支付宝", "支付宝", PayByAlipayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("微信", "微信", PayByWxpayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("银行卡", "银行卡", PayByBandcardFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("会员卡", "会员卡", PayByMfcardFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("账户", "账户", PayByMfAccountFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("赊账", "赊账", PayByCreditFragment.class,
                parArgs));

        viewPagerAdapter.addAllTab(mTabs);

        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    public void onEventMainThread(MfPayEvent event) {
        ZLogger.d("onEventMainThread(CashierPayActivity):" + event.getEventId());
        if (event.getEventId() == MfPayEvent.EVENT_ID_QEQUEST_HANDLE_AMOUNT) {
            notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getEventId() == MfPayEvent.EVENT_ID_READ_MFACCOUNT) {//登录会员
            showMemberCard();
        }
        //支付处理中
        else if (event.getEventId() == MfPayEvent.EVENT_ID_PAY_PROCESSING) {
            savePayHistory(event.getArgs());

            ibHeaderClose.setEnabled(false);
            mViewPager.setScrollEnabled(false);
            paySlidingTabStrip.setClickEnabled(false);
            ivMemberHeader.setEnabled(false);
        }
        // 支付成功，已付金额发生改变
        else if (event.getEventId() == MfPayEvent.EVENT_ID_PAY_SUCCEE) {
            savePayHistory(event.getArgs());

            refreshPayInfo(true);

            ibHeaderClose.setEnabled(true);
            mViewPager.setScrollEnabled(true);
            paySlidingTabStrip.setClickEnabled(true);
            ivMemberHeader.setEnabled(true);
        }
        // 支付失败
        else if (event.getEventId() == MfPayEvent.EVENT_ID_PAY_FAILED) {
            savePayHistory(event.getArgs());

            refreshPayInfo(false);

            ibHeaderClose.setEnabled(true);
            mViewPager.setScrollEnabled(true);
            paySlidingTabStrip.setClickEnabled(true);
            ivMemberHeader.setEnabled(true);
        }
    }

    /**
     * 保存支付记录
     * */
    private void savePayHistory(Bundle args){
        if (args != null) {
            Double amount = args.getDouble(MfPayEvent.KEY_AMOUNT);
            int payType = args.getInt(MfPayEvent.KEY_PAY_TYPE);
            String outTradeNo = args.getString(MfPayEvent.KEY_OUTTRADENO);
            int payStatus = args.getInt(MfPayEvent.KEY_PAY_STATUS);
            //保存订单支付记录
            PosOrderPayService.get().pay(cashierOrderInfo.getOrderBarcode(), payType, outTradeNo,
                    amount, payStatus, cashierOrderInfo.getVipMember());

            if (payStatus == PosOrderEntity.ORDER_STATUS_FINISH){
                //修改订单支付信息（支付金额，支付状态）
                //修改订单信息（已付金额，支付）
                cashierOrderInfo.paid(payType, amount);

                if ((cashierOrderInfo.getPayType() & WayType.CASH) == WayType.CASH
                        && amount > 0) {
                    //有现金支付时才打开钱箱
                    SerialManager.openMoneyBox();
                }
            }
        }
    }

    /**
     * 支付完成
     */
    private void notifyPayFinished() {
        ZLogger.d("支付完成" + cashierOrderInfo.getPayType());
        Intent data = new Intent();
        data.putExtra(EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);

        setResult(Activity.RESULT_OK, data);
        finish();
    }

    /**
     * 通知：应付信息变化
     */
    private void notifyPayInfoChanged(int page) {
        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putDouble(BasePayFragment.EXTRA_KEY_HANDLE_AMOUNT, cashierOrderInfo.getHandleAmount());

        if (page == 1) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_ALIPAY);
            curPayType = WayType.ALI_F2F;
        } else if (page == 2) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_WX);
            curPayType = WayType.WX_F2F;
        } else if (page == 3) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_BANK);
            curPayType = WayType.BANKCARD;
        } else if (page == 4) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_MFCARD);
            curPayType = WayType.COUPONS;
        } else if (page == 5) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_MFACCOUNT);
            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, cashierOrderInfo.getVipMember());
            curPayType = WayType.MFACCOUNT;
        } else if (page == 6) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_CREDIT);
            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, cashierOrderInfo.getVipMember());
            curPayType = WayType.CREDIT;
        } else {
            curPayType = WayType.CASH;
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
        }

        intent.putExtras(extras);
        sendBroadcast(intent);
    }

    /**
     * 刷新支付信息
     *
     * @param bAutoCloseOrder 是否自动关闭订单，true,如果支付金额不足时自动关闭订单；false,不关闭订单。
     */
    private void refreshPayInfo(boolean bAutoCloseOrder) {
        ZLogger.df(String.format("更新支付信息－－应收金额:%f, 自动关闭支付窗口=%b",
                cashierOrderInfo.getHandleAmount(), bAutoCloseOrder));

        //根据实际应用场景，金额小于1分即认为支付完成
        if (cashierOrderInfo.getHandleAmount() < 0.01 && bAutoCloseOrder) {
//        if (cashierOrderInfo.getHandleAmount() < 0.000001 && bAutoCloseOrder) {
//        if ( cashierOrderInfo.getHandleAmount() > -0.0000001 && cashierOrderInfo.getHandleAmount() < 0.000001 && bAutoCloseOrder) {
//        if (cashierOrderInfo.getHandleAmount() =< 0 && bAutoCloseOrder){
            //修改订单支付信息（支付金额，支付状态）
            CashierHelper.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_FINISH);
            cashierOrderInfo.setStatus(PosOrderEntity.ORDER_STATUS_FINISH);
            notifyPayFinished();
            return;
        }

        //修改订单支付信息（支付金额，支付状态）
        CashierHelper.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_PROCESS);
        cashierOrderInfo.setStatus(PosOrderEntity.ORDER_STATUS_PROCESS);
        tvHandleAmount.setText(String.format("%.2f", cashierOrderInfo.getHandleAmount()));
        tvTotalAmount.setText(String.format("%.2f", cashierOrderInfo.getRetailAmount()));
        tvDiscountAmount.setText(String.format("%.2f", cashierOrderInfo.getDiscountAmount()));
        tvCouponAmount.setText(String.format("%.2f", cashierOrderInfo.getCouponDiscountAmount()));
        tvPaidAmount.setText(String.format("%.2f", cashierOrderInfo.getPaidAmount()));

        notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());
    }

    /**
     * 刷新会员信息
     */
    private void refreshVipMemberInfo() {
        Human memberInfo = cashierOrderInfo.getVipMember();
        if (memberInfo != null) {
            ivMemberHeader.setAvatarUrl(memberInfo.getHeadimageUrl());
            tvMemberBalance.setText("余额：0");
            tvMemberScore.setText(String.format("积分：%d", memberInfo.getCurScore()));
            tvMemberBalance.setVisibility(View.VISIBLE);
            tvMemberScore.setVisibility(View.VISIBLE);
            frameCoupon.setVisibility(View.VISIBLE);
        } else {
            ivMemberHeader.setImageResource(R.drawable.chat_tmp_user_head);
//            tvMemberBalance.setText("余额：0");
//            tvMemberScore.setText("积分：0");
            tvMemberBalance.setVisibility(View.GONE);
            tvMemberScore.setVisibility(View.GONE);
            frameCoupon.setVisibility(View.GONE);
        }
    }

    /**
     * 加载优惠券列表
     */
    @OnClick(R.id.empty_view)
    public void loadCoupons() {
        Human memberInfo = cashierOrderInfo.getVipMember();
        if (memberInfo == null) {
            showMemberCard();
            return;
        }

        couponAdapter.setEntityList(null);

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("payType", curPayType);
        jsonObject.put("humanId", memberInfo.getGuid());
        jsonObject.put("btype", cashierOrderInfo.getBizType());
        jsonObject.put("discount", cashierOrderInfo.getDiscountRate());
        jsonObject.put("createdDate", TimeCursor.InnerFormat.format(new Date()));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        jsonObject.put("items", cashierOrderInfo.getProductsInfo());
        CashierApiImpl.findMarketRulesByOrderInfo(jsonObject.toJSONString(), queryRsCallBack);
    }

    NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<MarketRules>(new PageInfo(1, 20)) {
        @Override
        public void processQueryResult(RspQueryResult<MarketRules> rs) {
            //此处在主线程中执行。
            int retSize = rs.getReturnNum();

            //订单拆分，POS场景取第一个即可。
            if (retSize > 0) {
                MarketRules marketRules = rs.getRowEntity(0);
                cashierOrderInfo.couponPrivilege(marketRules);
                couponAdapter.setEntityList(marketRules.getRuleBeans(), marketRules.getCoupBeans());
                couponsDiscount();
            } else {
                DialogUtil.showHint("暂无卡券可用");
            }
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
//            DialogUtil.showHint("加载卡券失败");
        }
    }, MarketRules.class, CashierApp.getAppContext());

    /**
     * 计算优惠券优惠
     */
    private void couponsDiscount() {
        Human memberInfo = cashierOrderInfo.getVipMember();
        if (memberInfo == null) {
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }


        final String selectCouponIds = couponAdapter.getSelectCouponIds();

        //保存
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"14.0"}}
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();

                        if (!StringUtils.isEmpty(retStr)) {
                            try {
                                Double amount = Double.valueOf(retStr);

                                cashierOrderInfo.coupon(amount, selectCouponIds);
                                //刷新会员优惠的积分
                                if (couponAdapter != null) {
                                    couponAdapter.setVipScore(amount / 2);
                                }
                                refreshPayInfo(false);
                                //成功
                            } catch (NumberFormatException e1) {
                                ZLogger.ef("计算优惠券优惠--返回的金额格式不正确:" + retStr);
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        btnSubmit.setEnabled(false);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("humanId", memberInfo.getGuid());
        //使用当前支付类型，而不是使用当前订单的支付类型
        jsonObject.put("payType", curPayType);
        jsonObject.put("btype", cashierOrderInfo.getBizType());
        jsonObject.put("amount", cashierOrderInfo.getDealAmount());
//        jsonObject.put("discount", cashierOrderInfo.getDiscountRate());
        jsonObject.put("createdDate", TimeUtil.format(new Date(), TimeCursor.InnerFormat));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        jsonObject.put("items", cashierOrderInfo.getProductsInfo());

        CashierApiImpl.getPayAmountByOrderInfo(selectCouponIds, cashierOrderInfo.getRuleIds(),
                jsonObject.toJSONString(), responseCallback);
    }

    private void couponsDiscount(final CouponRule couponRule) {
        final String selectCouponIds = couponAdapter.getSelectCouponIds();

        Human memberInfo = cashierOrderInfo.getVipMember();
        if (memberInfo == null) {
            //失败要重置状态
            couponRule.toggleSelected();
            couponAdapter.notifyDataSetChanged();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            couponRule.toggleSelected();
            couponAdapter.notifyDataSetChanged();
            return;
        }

        //保存
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"14.0"}}
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();

                        if (!StringUtils.isEmpty(retStr)) {
                            try {
                                Double amount = Double.valueOf(retStr);

                                cashierOrderInfo.coupon(amount, selectCouponIds);
                                refreshPayInfo(false);
                                //成功
                            } catch (NumberFormatException e1) {
                                ZLogger.d("计算优惠券优惠--返回的金额格式不正确:" + retStr);
                                couponRule.toggleSelected();
                                couponAdapter.notifyDataSetChanged();
                            }
                        } else {
                            couponRule.setIsSelected(!couponRule.isSelected());
                            couponAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        btnSubmit.setEnabled(false);
                        couponRule.toggleSelected();
                        couponAdapter.notifyDataSetChanged();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("humanId", memberInfo.getGuid());
        jsonObject.put("payType", curPayType);
        jsonObject.put("btype", cashierOrderInfo.getBizType());
//        jsonObject.put("discount", cashierOrderInfo.getDiscountRate());
//        jsonObject.put("discount", 1);
        jsonObject.put("amount", cashierOrderInfo.getDealAmount());
        jsonObject.put("createdDate", TimeCursor.InnerFormat.format(new Date()));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        jsonObject.put("items", cashierOrderInfo.getProductsInfo());

        CashierApiImpl.getPayAmountByOrderInfo(selectCouponIds, cashierOrderInfo.getRuleIds(),
                jsonObject.toJSONString(), responseCallback);
    }

}

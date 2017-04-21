package com.manfenjiayuan.pda_supermarket.ui.pay.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.CashierOrderInfo;
import com.manfenjiayuan.pda_supermarket.cashier.CashierOrderInfoImpl;
import com.manfenjiayuan.pda_supermarket.cashier.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.ui.pay.BasePayStepFragment;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayActionEvent;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayEvent;
import com.manfenjiayuan.pda_supermarket.ui.pay.PayStep1Event;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;



/**
 * 支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PayStep1Fragment extends BasePayStepFragment {

    private static final int TAB_CASH = 0;
    private static final int TAB_VIP = 1;
    private static final int TAB_ALIPAY = 2;
    private static final int TAB_WX = 3;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_handle_amount)
    TextView tvHandleAmount;
    @BindView(R.id.labelTotalAmount)
    MultiLayerLabel tvTotalAmount;
    @BindView(R.id.labelAdjustAmount)
    MultiLayerLabel tvAdjustAmount;
    @BindView(R.id.tabstrip_pay)
    TopSlidingTabStrip paySlidingTabStrip;
    @BindView(R.id.tab_viewpager)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @BindView(R.id.fab_scan)
    FloatingActionButton btnSweep;


    public static PayStep1Fragment newInstance(Bundle args) {
        PayStep1Fragment fragment = new PayStep1Fragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier_pay_1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ZLogger.df(String.format("打开支付页面，%s", StringUtils.decodeBundle(args)));

        if (args != null) {
            cashierOrderInfo = (CashierOrderInfo) args.getSerializable(EXTRA_KEY_CASHIER_ORDERINFO);
        }

        toolbar.setTitle("收银");
//        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_toolbar_back_normal);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        if (SharedPrefesManagerFactory.isCameraSweepEnabled()) {
            btnSweep.setVisibility(View.VISIBLE);
        } else {
            btnSweep.setVisibility(View.GONE);
        }

        if (cashierOrderInfo == null) {
            DialogUtil.showHint("订单支付数据错误");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            reload(cashierOrderInfo);

            initTabs();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected void refresh() {
        if (cashierOrderInfo != null) {
            //显示应付款
            Double handleAmount = CashierOrderInfoImpl.getUnpayAmount(cashierOrderInfo);

            ZLogger.df(String.format("刷新收银信息，应收金额:%f\n%s", handleAmount,
                    JSONObject.toJSONString(cashierOrderInfo)));

            tvHandleAmount.setText(String.format("%.2f", handleAmount));
            tvTotalAmount.setTopText(String.format("%.2f", cashierOrderInfo.getRetailAmount()));
            tvAdjustAmount.setTopText(String.format("%.2f", cashierOrderInfo.getAdjustAmount()));
        } else {
            tvHandleAmount.setText(String.format("%.2f", 0D));
            tvTotalAmount.setTopText(String.format("%.2f", 0D));
            tvAdjustAmount.setTopText(String.format("%.2f", 0D));
        }

        notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());

        activeMode(true);
    }

    @OnClick(R.id.fab_scan)
    @Override
    protected void zxingSweep() {
        super.zxingSweep();
    }

    public void activeMode(boolean isActive) {
        mViewPager.setScrollEnabled(isActive);
        paySlidingTabStrip.setClickEnabled(isActive);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PayStep1Event event) {
        ZLogger.d(String.format("PayStep1Event:%d\n%s",
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

    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyPayInfoChanged(page);
//                if (page == 1 || page == 2 || page == 4 || page == 5) {
//                    if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
//                        DialogUtil.showHint("网络异常,请选择其他支付方式");
////                        paySlidingTabStrip.setSelected();
//                    }
//                }
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        Bundle parArgs = new Bundle();
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
                CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo));

        mTabs.add(new ViewPageInfo("现金", "现金", PayByCashFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("会员", "会员", PayByVipFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("支付宝", "支付宝", PayByAlipayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("微信", "微信", PayByWxpayFragment.class,
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

        if (page == TAB_ALIPAY) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_ALIPAY);
            curPayType = WayType.ALI_F2F;
            ZLogger.df("切换到‘支付宝扫码’支付");
            btnSweep.setVisibility(View.VISIBLE);
        } else if (page == TAB_WX) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_WX);
            curPayType = WayType.WX_F2F;
            ZLogger.df("切换到‘微信扫码’支付");
            btnSweep.setVisibility(View.VISIBLE);
        } else if (page == TAB_VIP) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP);
            curPayType = WayType.VIP;
            btnSweep.setVisibility(View.VISIBLE);
            ZLogger.df("切换到‘会员’支付");
            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, cashierOrderInfo.getVipMember());
        } else {
            ZLogger.df("切换到‘现金’支付");
            curPayType = WayType.CASH;
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
            btnSweep.setVisibility(View.GONE);
        }

        intent.putExtras(extras);
        getContext().sendBroadcast(intent);
    }

    @Override
    public void onPayFinished() {
        super.onPayFinished();
    }

    @Override
    public void onPayStepFinish() {
        super.onPayStepFinish();
        refresh();
    }

    @Override
    public void onPayCancel() {
        super.onPayCancel();
//        refresh();
    }

    @Override
    public void onPayStepFailed(PaymentInfo paymentInfo, String errMsg) {
        super.onPayStepFailed(paymentInfo, errMsg);
    }

    @Override
    protected void onScanCode(String code) {
        ZLogger.d("扫描结果:" + code);
//        Intent intent = new Intent(Constants.BA_HANDLE_SCANBARCODE);
        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putInt(BasePayFragment.EXTRA_KEY_WAYTYPE, curPayType);
        extras.putString(BasePayFragment.EXTRA_KEY_SCANCODE, code);
        intent.setAction(Constants.BA_HANDLE_SCANBARCODE);
        intent.putExtras(extras);
//        getActivity().sendBroadcast(intent);
        EventBus.getDefault().post(new PayEvent(PayEvent.EVENT_ID_SCAN_PAYCODE, extras));
    }
}

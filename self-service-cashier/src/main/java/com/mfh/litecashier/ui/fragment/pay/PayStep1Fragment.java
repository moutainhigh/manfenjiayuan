package com.mfh.litecashier.ui.fragment.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfoImpl;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderItemInfo;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfo;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.compound.MultiLayerLabel;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.SerialManager;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 首页－－采购
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PayStep1Fragment extends BasePayStepFragment {

    private static final int TAB_CASH = 0;
    private static final int TAB_VIP = 1;
    private static final int TAB_ALIPAY = 2;
    private static final int TAB_WX = 3;
    private static final int TAB_BANK = 4;
//    private static final int TAB_CREDIT     = 6;

    @Bind(R.id.tv_handle_amount)
    TextView tvHandleAmount;
    @Bind(R.id.labelTotalAmount)
    MultiLayerLabel tvTotalAmount;
    @Bind(R.id.labelAdjustAmount)
    MultiLayerLabel tvAdjustAmount;
    @Bind(R.id.labelPaidAmount)
    MultiLayerLabel tvPaidAmount;
    @Bind(R.id.tabstrip_pay)
    TopSlidingTabStrip paySlidingTabStrip;
    @Bind(R.id.tab_viewpager)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;


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

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ZLogger.df(String.format("打开支付页面，%s", StringUtils.decodeBundle(args)));

        if (args != null) {
            cashierOrderInfo = (CashierOrderInfo) args.getSerializable(EXTRA_KEY_CASHIER_ORDERINFO);
        }

        initTabs();

        if (cashierOrderInfo == null) {
            DialogUtil.showHint("订单支付数据错误");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }
        else{
            reload(cashierOrderInfo);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void refresh() {
        if (cashierOrderInfo != null) {
            //显示应付款
            Double handleAmount = CashierOrderInfoImpl.getUnpayAmount(cashierOrderInfo);

            ZLogger.df(String.format("刷新收银信息，应收金额:%f\n%s", handleAmount,
                    JSONObject.toJSONString(cashierOrderInfo)));

//        SerialManager.show(2, cashierOrderInfo.getHandleAmount());
            SerialManager.vfdShow(String.format("Total:%.2f", handleAmount));

            tvHandleAmount.setText(String.format("%.2f", handleAmount));
            tvTotalAmount.setTopText(String.format("%.2f", cashierOrderInfo.getRetailAmount()));
            tvAdjustAmount.setTopText(String.format("%.2f", cashierOrderInfo.getAdjustAmount()));
            tvPaidAmount.setTopText(String.format("%.2f", cashierOrderInfo.getPaidAmount()));
        } else {
            tvHandleAmount.setText(String.format("%.2f", 0D));
            tvTotalAmount.setTopText(String.format("%.2f", 0D));
            tvAdjustAmount.setTopText(String.format("%.2f", 0D));
            tvPaidAmount.setTopText(String.format("%.2f", 0D));
        }

        notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());

        activeMode(true);
    }


    public void activeMode(boolean isActive) {
        mViewPager.setScrollEnabled(isActive);
        paySlidingTabStrip.setClickEnabled(isActive);
    }

    public void onEventMainThread(PayStep1Event event) {
        ZLogger.d(String.format("PayStep1Event:%d\n%s",
                event.getAction(), StringUtils.decodeBundle(event.getArgs())));
        switch (event.getAction()) {
            case PayStep1Event.PAY_ACTION_WAYTYPE_UPDATED: {
                notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());
            }
            break;
            //支付处理中
            case PayStep1Event.PAY_ACTION_PAYSTEP_PROCESS:{
                activeMode(false);

                PaymentInfo paymentInfo = (PaymentInfo) event.getArgs()
                        .getSerializable(PayActionEvent.KEY_PAYMENT_INFO);
                onPayStepProcess(paymentInfo);
            }
            break;
            //支付失败
            case PayStep1Event.PAY_ACTION_PAYSTEP_FAILED:{
                activeMode(true);

                PaymentInfo paymentInfo = (PaymentInfo) event.getArgs()
                        .getSerializable(PayActionEvent.KEY_PAYMENT_INFO);
                String errMsg = event.getArgs().getString(PayActionEvent.KEY_ERROR_MESSAGE);
                onPayStepFailed(paymentInfo, errMsg);
            }
            break;
            //支付成功
            case PayStep1Event.PAY_ACTION_PAYSTEP_FINISHED:{
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
        List<CashierOrderItemInfo> cashierOrderItemInfoList = cashierOrderInfo.getCashierOrderItemInfos();
        if (cashierOrderItemInfoList != null && cashierOrderItemInfoList.size() > 0) {
            parArgs.putLong(BasePayFragment.EXTRA_KEY_ORDER_ID,
                    cashierOrderItemInfoList.get(0).getOrderId());
            parArgs.putString(BasePayFragment.EXTRA_KEY_BODY,
                    cashierOrderItemInfoList.get(0).getBrief());
        }
        parArgs.putString(BasePayFragment.EXTRA_KEY_ORDER_BARCODE,
                cashierOrderInfo.getPosTradeNo());
        parArgs.putString(BasePayFragment.EXTRA_KEY_SUBJECT,
                cashierOrderInfo.getSubject());

        parArgs.putString(BasePayFragment.EXTRA_KEY_BIZ_TYPE,
                String.valueOf(cashierOrderInfo.getBizType()));

//        cashArags.putString(BasePayFragment.EXTRA_KEY_SUBJECT, cashierOrderInfo.getSubject());
//        cashArags.putString(BasePayFragment.EXTRA_KEY_BODY, cashierOrderInfo.getBody());
//        cashArags.putString(BasePayFragment.EXTRA_KEY_BIZ_TYPE, String.valueOf(cashierOrderInfo.getBizType()));

        mTabs.add(new ViewPageInfo("现金", "现金", PayByCashFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("会员", "会员", PayByVipFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("支付宝", "支付宝", PayByAlipayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("微信", "微信", PayByWxpayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("银行卡", "银行卡", PayByBandcardFragment.class,
                parArgs));
//        mTabs.add(new ViewPageInfo("赊账", "赊账", PayByCreditFragment.class,
//                parArgs));

        viewPagerAdapter.addAllTab(mTabs);

        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    /**
     * 通知：应付信息变化
     */
    private void notifyPayInfoChanged(int page) {
        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putDouble(BasePayFragment.EXTRA_KEY_HANDLE_AMOUNT, CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo));

        if (page == TAB_ALIPAY) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_ALIPAY);
            curPayType = WayType.ALI_F2F;
            ZLogger.df("切换到‘支付宝扫码’支付");
        } else if (page == TAB_WX) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_WX);
            curPayType = WayType.WX_F2F;
            ZLogger.df("切换到‘微信扫码’支付");
        } else if (page == TAB_BANK) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_BANK);
            curPayType = WayType.BANKCARD;
            ZLogger.df("切换到‘银行卡’支付");
        } else if (page == TAB_VIP) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP);
            curPayType = WayType.VIP;
            ZLogger.df("切换到‘会员’支付");
            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, cashierOrderInfo.getVipMember());
        }
//        else if (page == TAB_CREDIT) {
//            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_CREDIT);
//            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, cashierOrderInfo.getVipMember());
//            curPayType = WayType.CREDIT;
//        }
        else {
            ZLogger.df("切换到‘现金’支付");
            curPayType = WayType.CASH;
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
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
}

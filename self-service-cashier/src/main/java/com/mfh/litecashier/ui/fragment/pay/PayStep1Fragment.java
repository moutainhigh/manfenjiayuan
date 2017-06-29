package com.mfh.litecashier.ui.fragment.pay;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.hardware.led.LedAgent;
import com.bingshanguxue.cashier.pay.BasePayFragment;
import com.bingshanguxue.cashier.pay.BasePayStepFragment;
import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.bingshanguxue.cashier.pay.PayStep1Event;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.cashier.CashierProvider;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.bingshanguxue.cashier.CashierBenchObservable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 首页－－采购
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PayStep1Fragment extends BasePayStepFragment {

    private static final int TAB_CASH = 3;
    private static final int TAB_VIP = 0;
    private static final int TAB_ALIPAY = 1;
    private static final int TAB_WX = 2;
    private static final int TAB_BANK = 4;
//    private static final int TAB_CREDIT     = 6;


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
    @BindView(R.id.fab_give)
    ImageButton fabGive;


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

        EventBus.getDefault().unregister(this);
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
            Double handleAmount = CashierProvider.getUnpayAmount(cashierOrderInfo);

            ZLogger.df(String.format("刷新收银信息，应收金额:%f\n%s", handleAmount,
                    JSONObject.toJSONString(cashierOrderInfo)));

//        SerialManager.show(2, cashierOrderInfo.getHandleAmount());
            LedAgent.vfdShow(String.format("Total:%.2f", handleAmount));

            tvHandleAmount.setText(String.format("%.2f", handleAmount));
            tvTotalAmount.setTopText(String.format("%.2f", cashierOrderInfo.getRetailAmount()));
            tvAdjustAmount.setTopText(String.format("%.2f", cashierOrderInfo.getAdjustAmount()));
        } else {
            tvHandleAmount.setText(String.format("%.2f", 0D));
            tvTotalAmount.setTopText(String.format("%.2f", 0D));
            tvAdjustAmount.setTopText(String.format("%.2f", 0D));
        }

        CashierBenchObservable.getInstance().setCashierOrderInfo(cashierOrderInfo);
        notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());

        activeMode(true);
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
                        ZLogger.df("取消赠送");
                    }
                });
    }

    /**
     * */
    public void activeMode(boolean isActive) {
        mViewPager.setScrollEnabled(isActive);
        paySlidingTabStrip.setClickEnabled(isActive);
        if (isActive) {
            fabGive.setVisibility(View.VISIBLE);
        } else {
            fabGive.setVisibility(View.GONE);
        }
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
                CashierProvider.getHandleAmount(cashierOrderInfo));

        mTabs.add(new ViewPageInfo("会员", "会员", PayByVipFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("支付宝", "支付宝", PayByAlipayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("微信", "微信", PayByWxpayFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("现金", "现金", PayByCashFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("银行卡", "银行卡", PayByBandcardFragment.class,
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
//        extras.putDouble(BasePayFragment.EXTRA_KEY_HANDLE_AMOUNT,
//                CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo));

        if (page == TAB_ALIPAY) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_ALIPAY);
            curPayType = WayType.ALI_F2F;
        } else if (page == TAB_WX) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_WX);
            curPayType = WayType.WX_F2F;
        } else if (page == TAB_BANK) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_BANK);
            curPayType = WayType.BANKCARD;
        } else if (page == TAB_VIP) {
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_VIP);
            curPayType = WayType.VIP;
            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, cashierOrderInfo.getVipMember());
        }
//        else if (page == TAB_CREDIT) {
//            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED_CREDIT);
//            extras.putSerializable(BasePayFragment.EXTRA_KEY_MEMBERINFO, cashierOrderInfo.getVipMember());
//            curPayType = WayType.CREDIT;
//        }
        else {
            curPayType = WayType.CASH;
            intent.setAction(Constants.BA_HANDLE_AMOUNT_CHANGED);
        }
        ZLogger.df(String.format("切换到‘%s’支付", WayType.getWayTypeName(curPayType)));
        intent.putExtras(extras);
        getContext().sendBroadcast(intent);
    }

    @Override
    public void onPayFinished() {
        super.onPayFinished();
    }

    @Override
    public void onPayCancel() {
        super.onPayCancel();
    }

    @Override
    public void onPayStepFailed(PaymentInfo paymentInfo, String errMsg) {
        super.onPayStepFailed(paymentInfo, errMsg);
    }

    @Override
    public void onPayStepFinish() {
//        btnSubmit.setEnabled(true);
        hideProgressDialog();
        super.onPayStepFinish();
    }
}

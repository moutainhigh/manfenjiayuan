package com.mfh.litecashier.ui.fragment.purchase;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.cashier.CashierProvider;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.business.dialog.AccountQuickPayDialog;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invOrder.InvOrderApi;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItemBrief;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.InvRecvOrderEvent;
import com.mfh.litecashier.event.PurchaseReceiptEvent;
import com.mfh.litecashier.ui.adapter.PurchaseReceiptGoodsAdapter;
import com.mfh.litecashier.utils.ACacheHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 采购－采购收货
 * 1.
 * Created by Nat.ZZN(bingshanguxue) on 16/01/05.
 */
public class PurchaseReceiptFragment extends BaseFragment {
    @BindView(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @BindView(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @BindView(R.id.order_goods_list)
    RecyclerView goodsRecyclerView;
    private PurchaseReceiptGoodsAdapter goodsListAdapter;

    @BindView(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @BindView(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @BindView(R.id.button_pay)
    Button btnPay;

    private InvSendIoOrder curOrder;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_receipt;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();
        initGoodsRecyclerView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 支付
     * */
    @OnClick(R.id.button_pay)
    public void pay() {
        alertPay(curOrder);
    }

    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyOrderRefresh(page);
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

        Bundle args1 = new Bundle();
        args1.putString(InvRecvOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
        args1.putString(InvRecvOrderFragment.EXTRA_KEY_PAYSTATUS, String.valueOf(InvOrderApi.PAY_STATUS_NOT_PAID));
        args1.putString(InvRecvOrderFragment.EXTRA_KEY_CACHEKEY, String.format("%s_%d_%d",
                ACacheHelper.CK_PURCHASE_RECEIPT, InvOrderApi.ORDER_STATUS_RECEIVE, InvOrderApi.PAY_STATUS_NOT_PAID));
        mTabs.add(new ViewPageInfo("未支付", "未支付", InvRecvOrderFragment.class,
                args1));

        Bundle args2 = new Bundle();
        args2.putString(InvRecvOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
        args2.putString(InvRecvOrderFragment.EXTRA_KEY_PAYSTATUS, String.valueOf(InvOrderApi.PAY_STATUS_PAID));
        args2.putString(InvRecvOrderFragment.EXTRA_KEY_CACHEKEY, String.format("%s_%d_%d",
                ACacheHelper.CK_PURCHASE_RECEIPT, InvOrderApi.ORDER_STATUS_RECEIVE, InvOrderApi.PAY_STATUS_PAID));
        mTabs.add(new ViewPageInfo("已支付", "已支付", InvRecvOrderFragment.class,
                args2));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index) {
        Bundle args = new Bundle();
        if (index == 0) {
            args.putString(InvRecvOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
            args.putString(InvRecvOrderFragment.EXTRA_KEY_PAYSTATUS, String.valueOf(InvOrderApi.PAY_STATUS_NOT_PAID));
        } else if (index == 1) {
            args.putString(InvRecvOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
            args.putString(InvRecvOrderFragment.EXTRA_KEY_PAYSTATUS, String.valueOf(InvOrderApi.PAY_STATUS_PAID));
        }
        loadGoodsList(null);
        EventBus.getDefault().post(new InvRecvOrderEvent(InvRecvOrderEvent.EVENT_ID_RELOAD_DATA, args));
    }

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        goodsListAdapter = new PurchaseReceiptGoodsAdapter(getActivity(), null);
        goodsListAdapter.setOnAdapterListener(new PurchaseReceiptGoodsAdapter.OnAdapterListener() {
            @Override
            public void onDataSetChanged() {
            }
        });
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PurchaseReceiptEvent event) {
        ZLogger.d(String.format("PurchaseReceiptEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == PurchaseReceiptEvent.EVENT_ID_RELOAD_DATA) {
            //优先加载缓存显示，同时在后台加载数据
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getAffairId() == PurchaseReceiptEvent.EVENT_ID_RELAOD_ITEM_DATA) {
            Bundle args = event.getArgs();
            if (args != null) {
                loadGoodsList((InvSendIoOrder) args.getSerializable("order"));
            }
        }
    }

    /**
     * 提示支付
     */
    private void alertPay(final InvSendIoOrder receivableOrder) {
        if (receivableOrder == null) {
            return;
        }
        showConfirmDialog("确定要支付订单吗？",
                "支付", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doPayWork(receivableOrder);
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    private AccountQuickPayDialog payDialog;

    private void doPayWork(InvSendIoOrder receivableOrder) {
        if (receivableOrder == null){
            return;
        }

        CashierOrderInfo cashierOrderInfo = CashierAgent.makeCashierOrderInfo(receivableOrder);
        if (cashierOrderInfo == null){
            return;
        }

        //支付
        if (payDialog == null) {
            payDialog = new AccountQuickPayDialog(getActivity());
            payDialog.setCancelable(false);
            payDialog.setCanceledOnTouchOutside(false);
        }
        payDialog.init(String.valueOf(receivableOrder.getId()),
                CashierProvider.getHandleAmount(cashierOrderInfo),
                new AccountQuickPayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed() {
                notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
            }

            @Override
            public void onPayFailed() {

            }

            @Override
            public void onPayCanceled() {

            }
        });
        if (!payDialog.isShowing()){
            payDialog.show();
        }
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(InvSendIoOrder order) {
        curOrder = order;
        if (order == null) {
            tvGoodsQunatity.setText(String.format("商品数：%.2f", 0D));
            tvTotalAmount.setText(String.format("商品金额：%.2f", 0D));

            goodsListAdapter.setEntityList(null);
            btnPay.setVisibility(View.INVISIBLE);
            return;
        }

        tvGoodsQunatity.setText(String.format("商品数：%.2f", curOrder.getCommitGoodsNum()));
        tvTotalAmount.setText(String.format("商品金额：%.2f", curOrder.getCommitPrice()));
        if (InvOrderApi.PAY_STATUS_NOT_PAID.equals(curOrder.getPayStatus())) {
            btnPay.setVisibility(View.VISIBLE);
        }else {
            btnPay.setVisibility(View.INVISIBLE);
        }

        //加载订单明细
        InvSendIoOrderApiImpl.getInvSendIoOrderById(curOrder.getId(), orderdetailRespCallback);
    }

    NetCallBack.NetTaskCallBack orderdetailRespCallback = new NetCallBack.NetTaskCallBack<InvSendIoOrderItemBrief,
            NetProcessor.Processor<InvSendIoOrderItemBrief>>(
            new NetProcessor.Processor<InvSendIoOrderItemBrief>() {
                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        goodsListAdapter.setEntityList(null);
                        return;
                    }
                    //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<InvSendIoOrderItemBrief> retValue = (RspBean<InvSendIoOrderItemBrief>) rspData;
                    InvSendIoOrderItemBrief orderDetail = retValue.getValue();

                    if (orderDetail != null) {
                        goodsListAdapter.setEntityList(orderDetail.getItems());
                    } else {
                        goodsListAdapter.setEntityList(null);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("加载商品失败：" + errMsg);
                    goodsListAdapter.setEntityList(null);
                }
            }
            , InvSendIoOrderItemBrief.class
            , CashierApp.getAppContext()) {
    };

}

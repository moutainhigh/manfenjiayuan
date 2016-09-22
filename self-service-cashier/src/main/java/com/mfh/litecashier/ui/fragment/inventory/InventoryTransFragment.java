package com.mfh.litecashier.ui.fragment.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.bingshanguxue.cashier.v1.CashierOrderInfoImpl;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.framework.api.account.Human;
import com.manfenjiayuan.business.dialog.AccountQuickPayDialog;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItemBrief;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.InvTransOrderEvent;
import com.mfh.litecashier.event.InventoryTransEvent;
import com.mfh.litecashier.ui.adapter.InventoryTransGoodsAdapter;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 库存－－库存调拨<br>
 * 调拨单不需要支付<br>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryTransFragment extends BaseFragment {

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    @Bind(R.id.empty_view)
    View emptyView;
    private InventoryTransGoodsAdapter goodsListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.frame_bottom)
    LinearLayout frameBottom;
    @Bind(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;

    @Bind(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @Bind(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;
    @Bind(R.id.button_create_bill)
    Button btnCreateBill;
    @Bind(R.id.button_pay)
    Button btnPay;
    @Bind(R.id.button_stockIn)
    Button btnStockIn;

    private InvSendIoOrder curOrder;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inventory_allocation;
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
     * 签收订单
     */
    @OnClick(R.id.button_stockIn)
    public void doReceive() {
        btnStockIn.setEnabled(false);

        final InvSendIoOrder invTransOrder = curOrder;
        if (invTransOrder == null || invTransOrder.getId() == null) {
            ZLogger.d("订单无效");
            btnStockIn.setEnabled(true);
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            btnStockIn.setEnabled(true);
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        if (InvOrderApi.PAY_STATUS_NOT_PAID.equals(invTransOrder.getPayStatus())) {
            doPayWork(1, invTransOrder.getId(), invTransOrder.getCommitPrice());
        } else {
            doReceiveWork(invTransOrder.getId());
        }
    }

    public void doReceiveWork(Long orderId) {
        NetCallBack.NetTaskCallBack receiveResponseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                            {"code":"0","msg":"操作成功!","version":"1","data":""}
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();

                        //出库成功:1-556637
                        ZLogger.d("调拨单入库成功:" + retStr);

                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVTRANSORDER_IN_ENABLED, true);
                        //刷新订单列表
                        notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
//                        Bundle args = new Bundle();
//                        args.putLong(InvRecvOrderFragment.EXTRA_KEY_ID, receivableOrder.getId());
//                        EventBus.getDefault().post(new InvRecvOrderEvent(InvRecvOrderEvent.EVENT_ID_REMOVE_ITEM, args));
//
                        btnStockIn.setEnabled(true);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("调拨单入库失败：" + errMsg);
                        btnStockIn.setEnabled(true);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        InvSendIoOrderApiImpl.doReceiveInvSendIoOrder(String.valueOf(orderId), null, receiveResponseCallback);
    }


    /**
     * 支付订单
     */
    @OnClick(R.id.button_pay)
    public void doPay() {
        btnPay.setEnabled(false);


        final InvSendIoOrder invTransOrder = curOrder;
        if (invTransOrder == null || invTransOrder.getId() == null) {
            ZLogger.d("订单无效");
            btnPay.setEnabled(true);
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            btnPay.setEnabled(true);
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        //支付商品金额，配送费不支付
        doPayWork(0, invTransOrder.getId(), invTransOrder.getCommitPrice());
    }

    private AccountQuickPayDialog payDialog;

    private void doPayWork(int dialogType, Long orderId, Double amount) {
        Human human = new Human();
        human.setGuid(String.valueOf(MfhLoginService.get().getCurrentGuId()));
        human.setId(MfhLoginService.get().getUserId());
        human.setHeadimageUrl(MfhLoginService.get().getHeadimage());

        //当前收银信息
        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.setOrderId(orderId);
        cashierOrderInfo.setbCount(1D);
        cashierOrderInfo.setRetailAmount(amount);
        cashierOrderInfo.setFinalAmount(amount);
        cashierOrderInfo.setAdjustAmount(0D);
        cashierOrderInfo.setDiscountRate(1D);
        cashierOrderInfo.setProductsInfo(null);
        cashierOrderInfo.setBizType(BizType.STOCK);
        cashierOrderInfo.setSubject("支付调拨入库单");
        cashierOrderInfo.setVipMember(human);

        //支付
        if (payDialog == null) {
            payDialog = new AccountQuickPayDialog(getActivity());
            payDialog.setCancelable(false);
            payDialog.setCanceledOnTouchOutside(false);
        }
        payDialog.init(dialogType, String.valueOf(orderId),
                CashierOrderInfoImpl.getHandleAmount(cashierOrderInfo),
                new AccountQuickPayDialog.DialogClickListener() {
                    @Override
                    public void onPaySucceed() {
                        //刷新数据
                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVTRANSORDER_IN_ENABLED, true);
                        //刷新订单列表
                        notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
//                        Bundle args = new Bundle();
//                        args.putLong(InvRecvOrderFragment.EXTRA_KEY_ID, receivableOrder.getId());
//                        EventBus.getDefault().post(new InvRecvOrderEvent(InvRecvOrderEvent.EVENT_ID_REMOVE_ITEM, args));
//
                        btnPay.setEnabled(true);
                        btnStockIn.setEnabled(true);
                    }

                    @Override
                    public void onPayFailed() {

                    }

                    @Override
                    public void onPayCanceled() {
                        btnPay.setEnabled(true);
                        btnStockIn.setEnabled(true);
                    }
                });
        payDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_CREATE_STOCK_BATCH: {
                //刷新订单列表
//                goodsListAdapter.notifyDataSetChanged();
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

        // 门店POS新建调拨单后自动出库
        Bundle args1 = new Bundle();
//        args1.putString(InvTransOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvTransOrder.STATUS_ON_TRANS));
        args1.putBoolean(InvTransOrderFragment.EXTRA_KEY_NETFLAG, true);
        args1.putString(InvTransOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%b", ACacheHelper.CK_INVENTORY_TRANS, true));
        mTabs.add(new ViewPageInfo("调入", "调入", InvTransOrderFragment.class, args1));

        Bundle args2 = new Bundle();
//        args2.putString(InvTransOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvTransOrder.STATUS_ON_TRANS));
        args2.putString(InvTransOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%b", ACacheHelper.CK_INVENTORY_TRANS, false));
        args2.putBoolean(InvTransOrderFragment.EXTRA_KEY_NETFLAG, false);
        mTabs.add(new ViewPageInfo("调出", "调出", InvTransOrderFragment.class, args2));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index) {
        Bundle args = new Bundle();

        if (index == 0) {
            args.putBoolean(InvTransOrderFragment.EXTRA_KEY_NETFLAG, true);
        } else if (index == 1) {
            args.putBoolean(InvTransOrderFragment.EXTRA_KEY_NETFLAG, false);
        }
        EventBus.getDefault().post(new InvTransOrderEvent(InvTransOrderEvent.EVENT_ID_RELOAD_DATA, args));
    }


    /**
     * 初始化商品列表
     */
    private void initGoodsRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);

        goodsListAdapter = new InventoryTransGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new InventoryTransGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onDataSetChanged() {
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(InventoryTransEvent event) {
        ZLogger.d(String.format("InventoryTransFragment: InventoryTransEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == InventoryTransEvent.EVENT_ID_INIT_DATA) {
            //优先加载缓存显示，同时在后台加载数据
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getAffairId() == InventoryTransEvent.EVENT_ID_RELOAD_DATA) {
            //优先加载缓存显示，同时在后台加载数据
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getAffairId() == InventoryTransEvent.EVENT_ID_RELAOD_ITEM_DATA) {
            Bundle args = event.getArgs();
            if (args != null) {
                loadGoodsList(args.getBoolean("netFlag"), (InvSendIoOrder) args.getSerializable("order"));
            }
        }
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(boolean netFlag, InvSendIoOrder order) {
        curOrder = order;
        if (order == null) {
            tvGoodsQunatity.setText(String.format("商品数：%.2f", 0D));
            tvTotalAmount.setText(String.format("商品金额：%.2f", 0D));
            btnPay.setVisibility(View.INVISIBLE);
            btnStockIn.setVisibility(View.GONE);

            goodsListAdapter.setEntityList(null);
            return;
        }

        //调入
        if (netFlag) {
            //未签收
            if (InvOrderApi.ORDER_STATUS_ON_TRANS.equals(curOrder.getStatus())) {
                btnStockIn.setVisibility(View.VISIBLE);
                btnPay.setVisibility(View.INVISIBLE);
            }
            //未支付
            else if (InvOrderApi.PAY_STATUS_NOT_PAID.equals(curOrder.getPayStatus())) {
                btnStockIn.setVisibility(View.GONE);
                btnPay.setVisibility(View.VISIBLE);
            } else {
                btnStockIn.setVisibility(View.GONE);
                btnPay.setVisibility(View.INVISIBLE);
            }
        } else {
            //调出
            btnStockIn.setVisibility(View.GONE);
            btnPay.setVisibility(View.INVISIBLE);
        }

        tvGoodsQunatity.setText(String.format("商品数：%.2f", curOrder.getCommitGoodsNum()));
        tvTotalAmount.setText(String.format("商品金额：%.2f", curOrder.getCommitPrice()));

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

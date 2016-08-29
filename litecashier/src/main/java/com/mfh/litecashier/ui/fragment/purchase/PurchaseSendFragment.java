package com.mfh.litecashier.ui.fragment.purchase;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.InvSendOrder;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.bean.OrderStatus;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.InvSendOrderEvent;
import com.mfh.litecashier.event.PurchaseSendEvent;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.mfh.litecashier.ui.activity.ServiceActivity;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.adapter.PurchaseSendGoodsAdapter;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 采购订单
 * Created by bingshanguxue on 15/8/31.
 */
public class PurchaseSendFragment extends BaseFragment implements IInvSendOrderView {
    @Bind(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @Bind(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @Bind(R.id.animProgressBar)
    ProgressBar progressBar;
    @Bind(R.id.order_goods_list)
    RecyclerView goodsRecyclerView;
    private PurchaseSendGoodsAdapter goodsListAdapter;

    @Bind(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @Bind(R.id.button_receive)
    TextView btnReceive;


    private InvSendOrder curOrder;
    private InvSendOrderPresenter invSendOrderPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_send;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        invSendOrderPresenter = new InvSendOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();
        initGoodsRecyclerView();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ZLogger.d("加载采购订单");
                notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (SharedPreferencesHelper.isSyncEnabled(SharedPreferencesHelper.PREF_KEY_SYNC_PURCHASESEND_ORDER_ENABLED) || !readCache())
        //先加载数据
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 收货指定采购订单
     * */
    @OnClick(R.id.button_receive)
    public void createNewReceiptOrder() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_PURCHASE_RECEIPT_ORDER);
        extras.putInt(CreatePurchaseReceiptOrderFragment.EK_ENTERMODE, 1);
        extras.putSerializable(CreatePurchaseReceiptOrderFragment.EK_SENDORDER, curOrder);
//        ServiceActivity.actionStart(getActivity(), extras);

        Intent intent = new Intent(getActivity(), ServiceActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 新建采购单
     * */
    @OnClick(R.id.button_create)
    public void createNewPurchaseOrder() {
//        OrderGoodsActivity.actionStart(getActivity(), null);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FRAGMENT_TYPE_COMMODITY_APPLY);
        SimpleActivity.actionStart(getActivity(), extras);
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

        Bundle args1 = new Bundle();
        args1.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(OrderStatus.STATUS_CONFIRM));
        args1.putString(InvSendOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, OrderStatus.STATUS_CONFIRM));
        mTabs.add(new ViewPageInfo("待收货", "待收货", InvSendOrderFragment.class,
                args1));

        Bundle args2 = new Bundle();
        args2.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(OrderStatus.STATUS_SENDED));
        args2.putString(InvSendOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, OrderStatus.STATUS_SENDED));
        mTabs.add(new ViewPageInfo("部分收货", "部分收货", InvSendOrderFragment.class,
                args2));

        Bundle args3 = new Bundle();
        args3.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(OrderStatus.STATUS_RECEIVE));
        args3.putString(InvSendOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, OrderStatus.STATUS_RECEIVE));
        mTabs.add(new ViewPageInfo("已收货", "已收货", InvSendOrderFragment.class,
                args3));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index){
        loadGoodsList(null);
        Bundle args = new Bundle();
        if (index == 0){
            args.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(OrderStatus.STATUS_CONFIRM));
//            btnReceive.setVisibility(View.VISIBLE);
        }
        else if (index == 1){
            args.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(OrderStatus.STATUS_SENDED));
//            btnReceive.setVisibility(View.VISIBLE);
        } else if (index == 2) {
            args.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(OrderStatus.STATUS_RECEIVE));
//            btnReceive.setVisibility(View.INVISIBLE);
        }
        EventBus.getDefault().post(new InvSendOrderEvent(InvSendOrderEvent.EVENT_ID_RELOAD_DATA, args));
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
        goodsListAdapter = new PurchaseSendGoodsAdapter(getActivity(), null);
        goodsListAdapter.setOnAdapterListener(new PurchaseSendGoodsAdapter.OnAdapterListener() {
            @Override
            public void onDataSetChanged() {
//                refreshBottomBar();
            }
        });
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(PurchaseSendEvent event) {
        ZLogger.d(String.format("PurchaseSendFragment: PurchaseSendEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseSendEvent.EVENT_ID_RELOAD_DATA) {
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        }
        else if (event.getEventId() == PurchaseSendEvent.EVENT_ID_RELAOD_ITEM_DATA) {
            Bundle args = event.getArgs();
            if (args != null){
                loadGoodsList((InvSendOrder)args.getSerializable("order"));
            }
        }
    }

    public void onEventMainThread(PurchaseShopcartSyncEvent event) {
        ZLogger.d(String.format("PurchaseSendFragment: PurchaseShopcartSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_ORDER_SUCCESS) {
            //刷新供应商
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        }
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(InvSendOrder order) {
        curOrder = order;
        if (order == null) {
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：", null, "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：", null, "无", null, null));
            btnReceive.setVisibility(View.INVISIBLE);
            goodsListAdapter.setEntityList(null);
        }
        else{
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：", curOrder.getAskTotalCount(), "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：", curOrder.getGoodsFee(), "无", null, null));
            if (OrderStatus.STATUS_CONFIRM.equals(order.getStatus()) || OrderStatus.STATUS_SENDED.equals(order.getStatus())){
                btnReceive.setVisibility(View.VISIBLE);
            } else if (OrderStatus.STATUS_RECEIVE.equals(order.getStatus())) {
                btnReceive.setVisibility(View.INVISIBLE);
            }

            //加载订单明细
            invSendOrderPresenter.loadOrderItems(curOrder.getId());
        }
    }


    @Override
    public void onQueryInvSendOrderProcess() {

        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onQueryInvSendOrderError(String errorMsg) {
        if (goodsListAdapter != null) {
            goodsListAdapter.setEntityList(null);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onQueryInvSendOrderSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onQueryInvSendOrderItemsSuccess(List<InvSendOrderItem> dataList) {

        if (goodsListAdapter != null) {
            goodsListAdapter.setEntityList(dataList);
        }
        progressBar.setVisibility(View.GONE);
    }
}

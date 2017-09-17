package com.mfh.litecashier.ui.fragment.purchase;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.business.mvp.view.IInvSendOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invOrder.InvOrderApi;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.mvp.MvpFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.InvSendOrderEvent;
import com.mfh.litecashier.event.PurchaseSendEvent;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.presenter.InvSendOrderPresenter2;
import com.mfh.litecashier.ui.adapter.PurchaseSendGoodsAdapter;
import com.mfh.litecashier.utils.ACacheHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 采购订单
 * Created by bingshanguxue on 15/8/31.
 */
public class PurchaseSendFragment2 extends MvpFragment<IInvSendOrderView, InvSendOrderPresenter2>
        implements IInvSendOrderView {
    @BindView(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @BindView(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @BindView(R.id.animProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.order_goods_list)
    RecyclerView goodsRecyclerView;
    private PurchaseSendGoodsAdapter goodsListAdapter;

    @BindView(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @BindView(R.id.tv_total_amount)
    TextView tvTotalAmount;


    private InvSendOrder curOrder;

    @Override
    public InvSendOrderPresenter2 createPresenter() {
        return new InvSendOrderPresenter2();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_send;
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

//        if (SharedPreferencesUltimate.isSyncEnabled(SharedPreferencesUltimate.PREF_KEY_SYNC_PURCHASESEND_ORDER_ENABLED) || !readCache())
        //先加载数据
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        args1.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_CONFIRM));
        args1.putString(InvSendOrderFragment.EXTRA_KEY_SENDTYPE, String.valueOf(InvOrderApi.SENDTYPE_SUPERMARKET_MANUAL));
        args1.putString(InvSendOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, InvOrderApi.ORDER_STATUS_CONFIRM));
        mTabs.add(new ViewPageInfo("待收货", "待收货", InvSendOrderFragment.class,
                args1));

        Bundle args2 = new Bundle();
        args2.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_SENDED));
        args2.putString(InvSendOrderFragment.EXTRA_KEY_SENDTYPE, String.valueOf(InvOrderApi.SENDTYPE_SUPERMARKET_MANUAL));
        args2.putString(InvSendOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, InvOrderApi.ORDER_STATUS_SENDED));
        mTabs.add(new ViewPageInfo("部分收货", "部分收货", InvSendOrderFragment.class,
                args2));

        Bundle args3 = new Bundle();
        args3.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
        args3.putString(InvSendOrderFragment.EXTRA_KEY_SENDTYPE, String.valueOf(InvOrderApi.SENDTYPE_SUPERMARKET_MANUAL));
        args3.putString(InvSendOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, InvOrderApi.ORDER_STATUS_RECEIVE));
        mTabs.add(new ViewPageInfo("已收货", "已收货", InvSendOrderFragment.class,
                args3));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index) {
        Bundle args = new Bundle();
        if (index == 0) {
            args.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_CONFIRM));
        } else if (index == 1) {
            args.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_SENDED));
        } else if (index == 2) {
            args.putString(InvSendOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
        }
        loadGoodsList(null);
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PurchaseSendEvent event) {
        ZLogger.d(String.format("PurchaseSendEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseSendEvent.EVENT_ID_RELOAD_DATA) {
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getEventId() == PurchaseSendEvent.EVENT_ID_RELAOD_ITEM_DATA) {
            Bundle args = event.getArgs();
            if (args != null) {
                loadGoodsList((InvSendOrder) args.getSerializable("order"));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PurchaseShopcartSyncEvent event) {
        ZLogger.d(String.format("PurchaseShopcartSyncEvent(%d)", event.getEventId()));
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
            tvGoodsQunatity.setText(String.format("商品数：%.2f", 0D));
            tvTotalAmount.setText(String.format("商品金额：%.2f", 0D));
            goodsListAdapter.setEntityList(null);
            return;
        }

        tvGoodsQunatity.setText(String.format("商品数：%.2f", curOrder.getAskTotalCount()));
        tvTotalAmount.setText(String.format("商品金额：%.2f", curOrder.getGoodsFee()));

        //加载订单明细
        if (presenter != null) {
            presenter.loadOrderItems(curOrder.getId());
        } else {
            ZLogger.d("presenter is null");
        }
    }


    @Override
    public void onIInvSendOrderViewProcess() {

        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onIInvSendOrderViewError(String errorMsg) {
        if (goodsListAdapter != null) {
            goodsListAdapter.setEntityList(null);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onIInvSendOrderViewSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onIInvSendOrderViewItemsSuccess(List<InvSendOrderItem> dataList) {

        if (goodsListAdapter != null) {
            goodsListAdapter.setEntityList(dataList);
        }
        progressBar.setVisibility(View.GONE);
    }
}

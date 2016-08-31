package com.mfh.litecashier.ui.fragment.online;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.PosOrder;
import com.mfh.litecashier.com.PrintManagerImpl;
import com.mfh.litecashier.event.GoodsListEvent;
import com.mfh.litecashier.event.OnlineOrderFlowEvent;
import com.mfh.litecashier.ui.adapter.StoreOrderflowGoodsAdapter;
import com.mfh.litecashier.ui.fragment.orderflow.GoodsListFragment;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 线上订单－－订单流水
 * Created by bingshanguxue on 15/8/31.
 */
public class OnlineOrderFlowFragment extends BaseFragment {

    @Bind(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @Bind(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @Bind(R.id.fab_print)
    FloatingActionButton fabPrint;
    @Bind(R.id.goods_list)
    RecyclerView goodsRecyclerView;
    private StoreOrderflowGoodsAdapter goodsListAdapter;

    private PosOrder curOrder;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_orderflow_online;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initGoodsRecyclerView();

        initTabs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initTabs() {
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

        Bundle args2 = new Bundle();
        args2.putString(GoodsListFragment.EXTRA_KEY_STATUS, String.valueOf(Constants.ORDER_STATUS_ORDERED));
        args2.putString(GoodsListFragment.EXTRA_KEY_CACHE_KEY, String.format("%s_%d", ACacheHelper.CK_ORDERFLOW_ONLINE, Constants.ORDER_STATUS_ORDERED));
        mTabs.add(new ViewPageInfo("待配送", "待配送", GoodsListFragment.class,
                args2));

        Bundle args3 = new Bundle();
        args3.putString(GoodsListFragment.EXTRA_KEY_STATUS, String.valueOf(Constants.ORDER_STATUS_RECEIVED));
        args3.putString(GoodsListFragment.EXTRA_KEY_CACHE_KEY, String.format("%s_%d", ACacheHelper.CK_ORDERFLOW_ONLINE, Constants.ORDER_STATUS_RECEIVED));
        mTabs.add(new ViewPageInfo("已配送", "已配送", GoodsListFragment.class,
                args3));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index){
        Bundle args = new Bundle();
        if (index == 0){
            args.putString(GoodsListFragment.EXTRA_KEY_STATUS, String.valueOf(Constants.ORDER_STATUS_ORDERED));
        }
        else if (index == 1){
            args.putString(GoodsListFragment.EXTRA_KEY_STATUS, String.valueOf(Constants.ORDER_STATUS_RECEIVED));
        }
        EventBus.getDefault().post(new GoodsListEvent(GoodsListEvent.EVENT_ID_RELOAD_DATA, args));
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
        goodsListAdapter = new StoreOrderflowGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new StoreOrderflowGoodsAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged() {
                if (goodsListAdapter != null && goodsListAdapter.getItemCount() > 0) {
                    fabPrint.setVisibility(View.VISIBLE);
//                    btnPrint.setEnabled(true);
                } else {
                    fabPrint.setVisibility(View.GONE);
//                    btnPrint.setEnabled(false);
                }
            }
        });
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    @OnClick(R.id.fab_print)
    public void printOrder() {
        PrintManagerImpl.printPosOrder(curOrder, true);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(OnlineOrderFlowEvent event) {
        ZLogger.d(String.format("OnlineOrderFlowFragment: OnlineOrderFlowEvent(%d)", event.getEventId()));
        if (event.getEventId() == OnlineOrderFlowEvent.EVENT_ID_RELOAD_DATA) {
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getEventId() == OnlineOrderFlowEvent.EVENT_ID_RELAOD_ITEM_DATA) {
            Bundle args = event.getArgs();
            if (args != null){
                loadGoodsList((PosOrder)args.getSerializable("order"));
            }
        }
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(PosOrder order) {
        curOrder = order;
        if (order == null) {
            goodsListAdapter.setEntityList(null);
            return;
        }

        goodsListAdapter.setEntityList(curOrder.getItems());
    }

}

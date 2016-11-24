package com.mfh.litecashier.ui.fragment.inventory;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.CommodityStockEvent;
import com.mfh.litecashier.event.InventoryTransEvent;
import com.mfh.litecashier.event.StockBatchEvent;
import com.mfh.litecashier.event.StockCheckEvent;
import com.mfh.litecashier.event.StockLossEvent;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;

import java.util.ArrayList;

import butterknife.BindView;
import de.greenrobot.event.EventBus;

/**
 * 首页－－库存
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryFragment extends BaseFragment {
    @BindView(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @BindView(R.id.viewpager_pagecontent)
    ViewPager mViewPager;

    private TopFragmentPagerAdapter viewPagerAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_viewpager;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();
    }

    private void initTabs() {
        mTabStrip.setOnClickTabListener(null);
        //TODO
        mTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                refreshData(page);
            }
        });

        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mTabStrip,
                mViewPager, R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("库存成本", "库存成本", InventoryCostFragment.class,
                null));
        mTabs.add(new ViewPageInfo("批次流水", "批次流水", InventoryIOFragment.class,
                null));
        mTabs.add(new ViewPageInfo("库存盘点", "库存盘点", InventoryCheckFragment.class,
                null));
        mTabs.add(new ViewPageInfo("库存报损", "库存报损", InventoryLossFragment.class,
                null));
        mTabs.add(new ViewPageInfo("库存调拨", "库存调拨", InventoryTransFragment.class,
                null));
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    public void refresh(){
        refreshData(mTabStrip.getCurrentPosition());
    }

    /**
     * 刷新数据
     * */
    private void refreshData(int page){
        if (page == 0){
            EventBus.getDefault().post(new CommodityStockEvent(CommodityStockEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 1){
            EventBus.getDefault().post(new StockBatchEvent(StockBatchEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 2){
            EventBus.getDefault().post(new StockCheckEvent(StockCheckEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 3){
            EventBus.getDefault().post(new StockLossEvent(StockLossEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 4){
            EventBus.getDefault().post(new InventoryTransEvent(InventoryTransEvent.EVENT_ID_INIT_DATA));
        }
    }
}

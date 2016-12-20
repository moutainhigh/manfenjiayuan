package com.mfh.litecashier.ui.fragment.orderflow;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.ExpressDeliveryOrderFlowEvent;
import com.mfh.litecashier.event.ExpressOrderFlowEvent;
import com.mfh.litecashier.event.LaundryOrderFlowEvent;
import com.mfh.litecashier.event.OnlineOrderFlowEvent;
import com.mfh.litecashier.event.OrderFlowEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * 流水
 * Created by bingshanguxue on 15/8/31.
 */
public class OrderFlowFragment extends BaseFragment {

    @BindView(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @BindView(R.id.viewpager_pagecontent)
    ViewPager mViewPager;

    private TopFragmentPagerAdapter viewPagerAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_orderflow;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();
    }

    private void initTabs() {
        mTabStrip.setOnClickTabListener(null);
        mTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                refreshData(page);
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mTabStrip, mViewPager, R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("衣服洗护", "衣服洗护", LaundryOrderFlowFragment.class,
                null));
        mTabs.add(new ViewPageInfo("快递代揽", "快递代揽", ExpressDeliveryOrderFlowFragment.class,
                null));
        mTabs.add(new ViewPageInfo("快递代发", "快递代发", ExpressOrderFlowFragment.class,
                null));
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    public void refresh(){
        if (mTabStrip != null){
            refreshData(mTabStrip.getCurrentPosition());
        }
    }

    /**
     * 刷新数据*/
    private void refreshData(int page){
        if (page == 1){
            EventBus.getDefault().post(new OnlineOrderFlowEvent(OnlineOrderFlowEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 2){
            EventBus.getDefault().post(new LaundryOrderFlowEvent(LaundryOrderFlowEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 3){
            EventBus.getDefault().post(new ExpressDeliveryOrderFlowEvent(ExpressDeliveryOrderFlowEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 4){
            EventBus.getDefault().post(new ExpressOrderFlowEvent(OrderFlowEvent.EVENT_ID_RELOAD_DATA));
        }
    }

}

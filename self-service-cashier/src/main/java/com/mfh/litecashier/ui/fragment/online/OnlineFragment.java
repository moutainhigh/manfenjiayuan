package com.mfh.litecashier.ui.fragment.online;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;

import butterknife.Bind;

/**
 * 首页－－采购
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class OnlineFragment extends BaseFragment {
    @Bind(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @Bind(R.id.viewpager_pagecontent)
    ViewPager mViewPager;

    private TopFragmentPagerAdapter viewPagerAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_viewpager;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, 0);
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

        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mTabStrip, mViewPager, R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("生鲜预定", "生鲜预定", FreshScheduleFragment.class,
                null));
        mTabs.add(new ViewPageInfo("其他", "其他", OnlineOrderFlowFragment.class,
                null));
//        mTabs.add(new ViewPageInfo("自采入库", "自采入库", CommodityPurchaseFragment.class,
//                null));
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
//        if (page == 0){
//            EventBus.getDefault().post(new PurchaseSendEvent(PurchaseSendEvent.EVENT_ID_RELOAD_DATA));
//        }
//        else if (page == 1){
//            EventBus.getDefault().post(new PurchaseReceiptEvent(PurchaseReceiptEvent.EVENT_ID_RELOAD_DATA));
//        }
//        else if (page == 2){
//            EventBus.getDefault().post(new PurchaseReturnEvent(PurchaseReturnEvent.EVENT_ID_RELOAD_DATA));
//        }
    }
}

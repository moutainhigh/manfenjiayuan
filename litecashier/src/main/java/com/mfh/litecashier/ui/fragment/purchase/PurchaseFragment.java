package com.mfh.litecashier.ui.fragment.purchase;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.PurchaseReceiptEvent;
import com.mfh.litecashier.event.PurchaseReturnEvent;
import com.mfh.litecashier.event.PurchaseSendEvent;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;

import java.util.ArrayList;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 首页－－采购
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PurchaseFragment extends BaseFragment {
    @Bind(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @Bind(R.id.viewpager_pagecontent)
    ViewPager mViewPager;

    private TopFragmentPagerAdapter viewPagerAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase;
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

        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mTabStrip, mViewPager, R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("采购订单", "采购订单", PurchaseSendFragment2.class,
                null));
        mTabs.add(new ViewPageInfo("采购收货", "采购收货", PurchaseReceiptFragment.class,
                null));
        mTabs.add(new ViewPageInfo("采购退货", "采购退货", PurchaseReturnFragment.class,
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
        if (page == 0){
            EventBus.getDefault().post(new PurchaseSendEvent(PurchaseSendEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 1){
            EventBus.getDefault().post(new PurchaseReceiptEvent(PurchaseReceiptEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == 2){
            EventBus.getDefault().post(new PurchaseReturnEvent(PurchaseReturnEvent.EVENT_ID_RELOAD_DATA));
        }
    }
}

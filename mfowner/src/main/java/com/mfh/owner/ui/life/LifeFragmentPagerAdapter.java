package com.mfh.owner.ui.life;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;


@SuppressLint("Recycle")
public class LifeFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final Context mContext;
    private TabHost mTabHost;
    private final ViewPager mViewPager;
    private final ArrayList<ViewPageInfo> mTabs = new ArrayList<ViewPageInfo>();

    public LifeFragmentPagerAdapter(FragmentManager fm, TabHost tabHost, ViewPager pager) {
        super(fm);
        mContext = pager.getContext();
        mTabHost = tabHost;
        mTabHost.setup();
        mViewPager = pager;
        mViewPager.setAdapter(this);
    }

    public void addTab(String title, String tag, Class<?> clss, Bundle args) {
        ViewPageInfo viewPageInfo = new ViewPageInfo(title, tag, clss, args);
        addFragment(viewPageInfo);
    }

    public void addAllTab(ArrayList<ViewPageInfo> mTabs) {
        for (ViewPageInfo viewPageInfo : mTabs) {
            addFragment(viewPageInfo);
        }
    }

    private void addFragment(ViewPageInfo info) {
        if (info == null) {
            return;
        }

        // 加入tab title
        View tab1 = LayoutInflater.from(mContext).inflate(com.mfh.owner.R.layout.tab_item, null);
        TextView tv1 = (TextView)tab1.findViewById(com.mfh.owner.R.id.tv_title);
        tv1.setText(info.title);
        mTabHost.addTab(mTabHost.newTabSpec(info.tag).setIndicator(tab1).setContent(com.mfh.owner.R.id.tab_viewpager));

        mTabs.add(info);
        notifyDataSetChanged();
    }

    /**
     * 移除第一次
     */
    public void remove() {
        remove(0);
    }

    /**
     * 移除一个tab
     * 
     * @param index
     *            备注：如果index小于0，则从第一个开始删 如果大于tab的数量值则从最后一个开始删除
     */
    public void remove(int index) {
        if (mTabs.isEmpty()) {
            return;
        }
        if (index < 0) {
            index = 0;
        }
        if (index >= mTabs.size()) {
            index = mTabs.size() - 1;
        }
        mTabs.remove(index);
        mTabHost.removeViews(index, 1);
        notifyDataSetChanged();
    }

    /**
     * 移除所有的tab
     */
    public void removeAll() {
        if (mTabs.isEmpty()) {
            return;
        }
        mTabHost.removeAllViews();
        mTabs.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        ViewPageInfo info = mTabs.get(position);
        return Fragment.instantiate(mContext, info.clss.getName(), info.args);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).title;
    }
}
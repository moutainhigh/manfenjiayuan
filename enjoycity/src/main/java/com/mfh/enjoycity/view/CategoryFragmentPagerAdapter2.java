package com.mfh.enjoycity.view;


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
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.fragments.CategoryFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;

@SuppressLint("Recycle")
public class CategoryFragmentPagerAdapter2 extends FragmentStatePagerAdapter {

    private final Context mContext;
    protected CategorySlidingTabStrip mPagerStrip;
    private final ViewPager mViewPager;
    private final ArrayList<CategoryFragment> mFragments = new ArrayList<>();

    public CategoryFragmentPagerAdapter2(FragmentManager fm,
                                         CategorySlidingTabStrip pageStrip, ViewPager pager) {
        super(fm);
        mContext = pager.getContext();
        mPagerStrip = pageStrip;
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mPagerStrip.setViewPager(mViewPager);
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
        View v = LayoutInflater.from(mContext).inflate(
                R.layout.category_viewpage_fragment_tab_item, null, false);
        TextView title = (TextView) v.findViewById(R.id.tab_title);
        title.setText(info.title);
        mPagerStrip.addTab(v);

        mFragments.add(CategoryFragment.newInstance(info.args));
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
        if (mFragments.isEmpty()) {
            return;
        }
        if (index < 0) {
            index = 0;
        }
        if (index >= mFragments.size()) {
            index = mFragments.size() - 1;
        }
        mFragments.remove(index);
        mPagerStrip.removeTab(index, 1);
        notifyDataSetChanged();
    }

    /**
     * 移除所有的tab
     */
    public void removeAll() {
        if (mFragments.isEmpty()) {
            return;
        }
        mPagerStrip.removeAllTab();
        mFragments.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
//        ViewPageInfo info = mFragments.get(position);
        ZLogger.d("Fragment.instantiate");
//        return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getArguments().getString("EXTRA_KEY_CATEGORY_ID", "");
    }
}
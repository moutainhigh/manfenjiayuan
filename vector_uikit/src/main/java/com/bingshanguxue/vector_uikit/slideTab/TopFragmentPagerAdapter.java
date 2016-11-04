package com.bingshanguxue.vector_uikit.slideTab;


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
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;

@SuppressLint("Recycle")
public class TopFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final Context mContext;
    protected TopSlidingTabStrip mPagerStrip;
    private final ViewPager mViewPager;
    private final ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
    private int tabResId;

    public TopFragmentPagerAdapter(FragmentManager fm,
                                   TopSlidingTabStrip pageStrip, ViewPager pager, int tabResId) {
        super(fm);
        mContext = pager.getContext();
        mPagerStrip = pageStrip;
        mViewPager = pager;
        this.tabResId = tabResId;

        mViewPager.setAdapter(this);
        mPagerStrip.setViewPager(mViewPager);
    }

    public void addTab(String title, String tag, Class<?> clss, Bundle args) {
        ViewPageInfo viewPageInfo = new ViewPageInfo(title, tag, clss, args);
        addFragment(viewPageInfo);

        notifyDataSetChanged();
    }

    public void addAllTab(ArrayList<ViewPageInfo> mTabs) {
        for (ViewPageInfo viewPageInfo : mTabs) {
            addFragment(viewPageInfo);
        }

        notifyDataSetChanged();
    }

    private void addFragment(ViewPageInfo info) {
        if (info == null) {
            return;
        }

        // 加入tab title
        View v = LayoutInflater.from(mContext).inflate(
                tabResId, null, false);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        TextView title = (TextView) v.findViewById(R.id.tab_title);
        if (title != null){
            title.setText(info.title);
        }
        mPagerStrip.addTab(v);

        mTabs.add(info);
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
        mPagerStrip.removeTab(index, 1);
        notifyDataSetChanged();
    }

    /**
     * 移除所有的tab
     */
    public void removeAll() {
        if (mTabs.isEmpty()) {
            return;
        }
        mPagerStrip.removeAllTab();
        mTabs.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mTabs == null){
            return 0;
        }
        return mTabs.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
//        return super.getItemPosition(object);
    }

    //生成新的 Fragment 对象
    @Override
    public Fragment getItem(int position) {
        ViewPageInfo info = mTabs.get(position);
//        ZLogger.d("Fragment.instantiate");
        return Fragment.instantiate(mContext, info.clss.getName(), info.args);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).title;
    }

    public ViewPageInfo getTab(int position) {
        ZLogger.d(String.format("position=%d, size=%d", position, mTabs.size()));
        if (position < 0 || position >= mTabs.size()){
            return null;
        }
        return mTabs.get(position);
    }
}
package com.mfh.litecashier.ui.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.framework.core.utils.DrawableUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.widget.SideSlidingTabStrip;
import com.mfh.litecashier.ui.widget.ViewPageInfo;

import java.util.ArrayList;

@SuppressLint("Recycle")
public class SettingsFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final Context mContext;
    protected SideSlidingTabStrip mPagerStrip;
    private final ViewPager mViewPager;
    private final ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
    private int tabResId;

    private boolean sizeInitialized = false;
    private int blockWidthInPixel, blockHeightInPixexl;

    public SettingsFragmentPagerAdapter(FragmentManager fm,
                                        SideSlidingTabStrip pageStrip, ViewPager pager, int tabResId) {
        super(fm);
        mContext = pager.getContext();
        mPagerStrip = pageStrip;
        mViewPager = pager;
        this.tabResId = tabResId;
        sizeInitialized = false;

        mViewPager.setAdapter(this);
        mPagerStrip.setViewPager(mViewPager);
    }

    public SettingsFragmentPagerAdapter(FragmentManager fm,
                                        SideSlidingTabStrip pageStrip, ViewPager pager,
                                        int tabResId, int blockWidthInPixel, int blockHeightInPixexl) {
        super(fm);
        mContext = pager.getContext();
        mPagerStrip = pageStrip;
        mViewPager = pager;
        this.tabResId = tabResId;
        this.blockWidthInPixel = blockWidthInPixel;
        this.blockHeightInPixexl = blockHeightInPixexl;
        sizeInitialized = true;

        mViewPager.setAdapter(this);
        mPagerStrip.setViewPager(mViewPager);
    }

    public void addTab(String title, int resId, String tag, Class<?> clss, Bundle args) {
        ViewPageInfo viewPageInfo = new ViewPageInfo(title, resId, tag, clss, args);
        addFragment(viewPageInfo);
    }

    public void addTab(ViewPageInfo viewPageInfo) {
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
                tabResId, null, false);
        if (sizeInitialized){
            //in pixel
            v.setLayoutParams(new ViewGroup.LayoutParams(blockWidthInPixel, blockHeightInPixexl));
        }

        TextView title = (TextView) v.findViewById(R.id.tv_buttonText);
        title.setText(info.title);
        ImageView icon = (ImageView) v.findViewById(R.id.iv_buttonImage);
//        icon.setImageResource(info.resId);

        // 使用着色
        icon.setImageDrawable(null);
        icon.setBackgroundResource(info.resId);
        //使用mute()，避免将使用同一个资源的图片都修改了。
        icon.setBackground(DrawableUtils.tintDrawable(icon.getBackground().mutate(),
                ContextCompat.getColorStateList(mContext, R.color.settings_tab_tint_colors)));

                mPagerStrip.addTab(v);

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
package com.manfenjiayuan.loveshopping.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.manfenjiayuan.loveshopping.AMapActivity;
import com.manfenjiayuan.loveshopping.MainTab;
import com.manfenjiayuan.loveshopping.MyFragmentTabHost;
import com.manfenjiayuan.loveshopping.R;
import com.manfenjiayuan.loveshopping.fragment.H5HomeFragment;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.uikit.widget.OnTabReselectListener;

import butterknife.Bind;

public class MainActivity extends AMapActivity {

    //    @Bind(R.id.toolbar)
//    Toolbar toolbar;
    @Bind(R.id.bottom_tab)
    MyFragmentTabHost mTabHost;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

//    @Override
//    protected void initToolBar() {
//        super.initToolBar();
//
//        setSupportActionBar(toolbar);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));

        initBottomTab();
    }

    private void initBottomTab() {
        mTabHost.setup(this, getSupportFragmentManager(), R.id.fragment_container);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
//            mTabHost.getTabWidget().setShowDividers(0);
//        }
        mTabHost.getTabWidget().setDividerDrawable(null);
        initTabItems();
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                ZLogger.d("onTabChanged:" + tabId);
                final int size = mTabHost.getTabWidget().getTabCount();
                for (int i = 0; i < size; i++) {
                    View v = mTabHost.getTabWidget().getChildAt(i);
                    if (i == mTabHost.getCurrentTab()) {
                        v.setSelected(true);
                    } else {
                        v.setSelected(false);
                    }
                }

                ZLogger.d("onTabChanged.tag: " + mTabHost.getCurrentTabTag());
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment == null) {
                    currentFragment = getCurrentFragment2();
                }
                if (currentFragment != null) {
//                    if (currentFragment instanceof MainMineFragment) {
//                        toolbar.setVisibility(View.GONE);
//                        ZLogger.d("MainMineFragment.hide toolbar");
//                    } else if (currentFragment instanceof ShopcartFragment){
//                        toolbar.setVisibility(View.GONE);
//                        ZLogger.d("ShopcartFragment.hide toolbar");
//                    } else if (currentFragment instanceof OrderFragment){
//                        toolbar.setVisibility(View.VISIBLE);
//                        ZLogger.d("OrderFragment.show toolbar");
//                    } else if (currentFragment instanceof HomeFragment){
//                        toolbar.setVisibility(View.VISIBLE);
//                        ZLogger.d("HomeFragment.show toolbar");
//                    }
//                    else{
//                        ZLogger.d("unkown fragment");
//                    }

                    if (currentFragment instanceof OnTabReselectListener) {
                        ((OnTabReselectListener) currentFragment).onTabReselect();
                    }
                }
            }
        });
    }

    private void initTabItems() {
        MainTab[] tabs = MainTab.values();
        final int size = tabs.length;
        for (int i = 0; i < size; i++) {
            MainTab tab = tabs[i];
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(tab.getResName()));
            View indicator = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_indicator, null);
            TextView title = (TextView) indicator.findViewById(R.id.tab_title);
            Drawable drawable = this.getResources().getDrawable(tab.getResIcon());
            title.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            title.setText(getString(tab.getResName()));
            tabSpec.setIndicator(indicator);
            tabSpec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return new View(MainActivity.this);
                }
            });

            Bundle arg = new Bundle();
            if (tab.getClz().isInstance(H5HomeFragment.class)) {
                ZLogger.d("H5HomeFragment");
                arg.putString(H5HomeFragment.ARG_URL, "http://ai.m.taobao.com/index.html?pid=mm_32549094_7052631_23486504");
            } else {
                ZLogger.d("NOT H5HomeFragment");
            }
            mTabHost.addTab(tabSpec, tab.getClz(), arg);

            mTabHost.getTabWidget().setGravity(Gravity.CENTER_VERTICAL);
//            int alt = TipoDisp.alt_tabs(MainActivity.this);
//            mTabHost.getTabWidget().getChildAt(i).setLayoutParams(new LinearLayout
//                    .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT));
            mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = DensityUtil.dip2px(getApplicationContext(), 58);//= mTabHost.getTabWidget().getHeight();// = mTabHost.getTabWidget().getMeasuredHeight();
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.equals(mTabHost.getCurrentTabView())) {
                        Fragment currentFragment = getCurrentFragment();
                        if (currentFragment != null && currentFragment instanceof OnTabReselectListener) {
                            ((OnTabReselectListener) currentFragment).onTabReselect();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        mTabHost.setCurrentTab(0);
    }

    private Fragment getCurrentFragment() {
        ZLogger.d("getCurrentFragment: " + mTabHost.getCurrentTabTag());
        return getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
    }

    private Fragment getCurrentFragment2() {
        ZLogger.d("getCurrentFragment2: " + mTabHost.getCurrentTabTag());
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

}

package com.mfh.enjoycity.ui;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.web.BrowserFragment;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.widget.PagerSlidingTabStrip;
import com.mfh.framework.uikit.widget.ViewPageFragmentAdapter;

import butterknife.Bind;


/**
 * 消息中心
 * */
public class MessageCenterActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    @Bind(R.id.pager_tabstrip)
    PagerSlidingTabStrip mTabStrip;
    @Bind(R.id.messageViewPager)
    ViewPager tabViewPager;
    private ViewPageFragmentAdapter viewPagerAdapter;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_message_center;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_message_center);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MessageCenterActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTabHost();
    }

    private void initTabHost(){
        try{
            viewPagerAdapter = new ViewPageFragmentAdapter(getSupportFragmentManager(), mTabStrip, tabViewPager);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果
            tabViewPager.setOffscreenPageLimit(3);

            ShopTab[] tabs = ShopTab.values();
            for(ShopTab tab : tabs){
                Bundle bundle = new Bundle();
                bundle.putString(BrowserFragment.EXTRA_KEY_REDIRECT_URL, tab.getUrl());
                bundle.putBoolean(BrowserFragment.EXTRA_KEY_OVERRIDE_URL_ATONCE, true);
                viewPagerAdapter.addTab(tab.getName(), tab.getName(), tab.getClz(),
                        bundle);
            }

            //TODO
//        tabViewPager.setCurrentItem(0, true);
        }
        catch(Exception e){
            ZLogger.e("init tabhost failed, " + e.toString());
        }
    }


}

package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.SideSlidingTabStrip;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.SettingsFragmentPagerAdapter;
import com.mfh.litecashier.ui.widget.ViewPageInfo;

import java.util.ArrayList;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 设置－－
 * Created by bingshanguxue on 15/8/31.
 */
public class SettingsFragment extends BaseFragment {

    @Bind(R.id.left_settings_tab)
    SideSlidingTabStrip slidingTabStrip;
    @Bind(R.id.settings_viewpager)
    CustomViewPager mViewPager;

    private SettingsFragmentPagerAdapter viewPagerAdapter;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(Bundle args){
        SettingsFragment fragment = new SettingsFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();

        reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private void initTabs() {
        mViewPager.setScrollEnabled(false);
        slidingTabStrip.setOnPagerChange(new SideSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
//                refreshData(page);
            }
        });

        viewPagerAdapter = new SettingsFragmentPagerAdapter(getChildFragmentManager(),
                slidingTabStrip, mViewPager, R.layout.itemview_leftmenu_settings,
                DensityUtil.dip2px(CashierApp.getAppContext(), 220),
                DensityUtil.dip2px(CashierApp.getAppContext(), 50));
    }

    private void reload(){
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("通用", R.mipmap.ic_toolbar_settings, "通用",
                SettingsCommonFragment.class,
                null));
        mTabs.add(new ViewPageInfo("安全", R.mipmap.ic_settings_security_normal, "安全",
                SettingsSecurityFragment.class,
                null));
        mTabs.add(new ViewPageInfo("通知", R.mipmap.ic_settings_notice_normal, "通知",
                SettingsNoticeFragment.class,
                null));
        mTabs.add(new ViewPageInfo("账户", R.mipmap.ic_settings_account_selected, "账户",
                SettingsAccountFragment.class,
                null));
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()){
            mTabs.add(new ViewPageInfo("开发者选项", R.mipmap.ic_settings_account_selected, "开发者选项",
                    DeveloperOptionsFragment.class,
                    null));
        }

        viewPagerAdapter.removeAll();
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    public void refresh(){
        if (slidingTabStrip != null){
//            refreshData(slidingTabStrip.getCurrentPosition());
        }
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(SettingsFragment.SettingsEvent event) {
        ZLogger.d(String.format(" SettingsEvent(%d)", event.getEventId()));
        if (event.getEventId() == SettingsEvent.EVENT_ID_RELOAD_DATA) {
            reload();
        }
    }

    public static class SettingsEvent {
        public static final int EVENT_ID_RELOAD_DATA        = 0X01;//初始化数据

        private int eventId;
        private Bundle args;//参数

        public SettingsEvent(int eventId) {
            this.eventId = eventId;
        }

        public SettingsEvent(int eventId, Bundle args) {
            this.eventId = eventId;
            this.args = args;
        }

        public int getEventId() {
            return eventId;
        }

        public Bundle getArgs() {
            return args;
        }
    }
}

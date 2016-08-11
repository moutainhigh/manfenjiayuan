package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.OrderflowLocalWarehouseEvent;
import com.mfh.litecashier.event.SettingsDailysettleEvent;
import com.mfh.litecashier.ui.adapter.SettingsFragmentPagerAdapter;
import com.mfh.framework.uikit.widget.SideSlidingTabStrip;
import com.mfh.litecashier.ui.widget.ViewPageInfo;

import java.util.ArrayList;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 设置－－
 * Created by bingshanguxue on 15/8/31.
 */
public class SettingsFragment extends BaseFragment {

    private static final int TAB_INDEX_GOODS = 5;
    private static final int TAB_INDEX_ORDERFLOW_LOACAL_WAREHOUSE = 6;
    private static final int TAB_INDEX_DAILYSETTLE = 7;

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
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();
    }

    private void initTabs() {
        mViewPager.setScrollEnabled(false);
        slidingTabStrip.setOnPagerChange(new SideSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                refreshData(page);
            }
        });

        viewPagerAdapter = new SettingsFragmentPagerAdapter(getChildFragmentManager(),
                slidingTabStrip, mViewPager, R.layout.itemview_leftmenu_settings,
                DensityUtil.dip2px(CashierApp.getAppContext(), 220),
                DensityUtil.dip2px(CashierApp.getAppContext(), 50));

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

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    public void refresh(){
        if (slidingTabStrip != null){
            refreshData(slidingTabStrip.getCurrentPosition());
        }
    }
    /**
     * 刷新数据
     * */
    private void refreshData(int page){
        if (page == TAB_INDEX_GOODS){
            EventBus.getDefault().post(new SettingsGoodsEvent(SettingsGoodsEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == TAB_INDEX_ORDERFLOW_LOACAL_WAREHOUSE){
            EventBus.getDefault().post(new OrderflowLocalWarehouseEvent(OrderflowLocalWarehouseEvent.EVENT_ID_RELOAD_DATA));
        }
        else if (page == TAB_INDEX_DAILYSETTLE){
            EventBus.getDefault().post(new SettingsDailysettleEvent(SettingsDailysettleEvent.EVENT_ID_RELOAD_DATA));
        }
        // TODO: 16/2/25
    }

    public class SettingsGoodsEvent {
        public static final int EVENT_ID_RELOAD_DATA        = 0X01;//初始化数据
        public static final int EVENT_ID_REFRESH_DATA       = 0X02;//刷新数据
        public static final int EVENT_ID_RELAOD_ITEM_DATA   = 0X03;//刷新数据

        private int eventId;
        private Bundle args;//参数

        public SettingsGoodsEvent(int eventId) {
            this.eventId = eventId;
        }

        public SettingsGoodsEvent(int eventId, Bundle args) {
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

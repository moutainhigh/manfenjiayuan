package com.mfh.litecashier.ui.fragment.purchase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 对话框－－ 选择批发商商品(新建采购收货单/采购退货单)
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class SelectWholesalerWithTenantFragment extends BaseFragment {
    public static final String EXTRA_KEY_BARCODE = "barcode";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @Bind(R.id.slidingTab)TopSlidingTabStrip mSlidingTabStrip;
    @Bind(R.id.viewpager)
    CustomViewPager mViewPager;

    private TopFragmentPagerAdapter viewPagerAdapter;
    private String title = "选择发货方";

    public static SelectWholesalerWithTenantFragment newInstance(Bundle args) {
        SelectWholesalerWithTenantFragment fragment = new SelectWholesalerWithTenantFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialogview_select_wholesalerwithtenant;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(SimpleDialogActivity.EXTRA_KEY_TITLE);
        }

        tvHeaderTitle.setText(title);
        initTabs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }


    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        mSlidingTabStrip.setOnClickTabListener(null);
        mSlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
//                notifyPayInfoChanged(page);
//                if (page == 1 || page == 2 || page == 4 || page == 5) {
//                    if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//                        DialogUtil.showHint("网络异常,请选择其他支付方式");
////                        paySlidingTabStrip.setSelected();
//                    }
//                }
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getFragmentManager(),
                mSlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("批发商", "批发商", SelectWholesalerFragment.class,
                null));
        mTabs.add(new ViewPageInfo("门店", "门店", SelectTenantFragment.class,
                null));

        viewPagerAdapter.addAllTab(mTabs);

        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(SelectWholesalerWithTenantEvent event) {
        ZLogger.d(String.format("SelectWholesaleWithTenantFragment: SelectWholesalerWithTenantEvent(%d)", event.getEventId()));
        if (event.getEventId() == SelectWholesalerWithTenantEvent.EVENT_ID_ITEM_SELECTED) {
            //优先加载缓存显示，同时在后台加载数据
            Bundle args = event.getArgs();
            if (args != null && args.containsKey("data")){
                Intent data = new Intent();
                data.putExtra("data", args.getSerializable("data"));
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        }
    }

    public static class SelectWholesalerWithTenantEvent {
        public static final int EVENT_ID_ITEM_SELECTED = 0X01;//初始化数据

        private int eventId;
        private Bundle args;

        public SelectWholesalerWithTenantEvent(int eventId) {
            this.eventId = eventId;
        }

        public SelectWholesalerWithTenantEvent(int eventId, Bundle args) {
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

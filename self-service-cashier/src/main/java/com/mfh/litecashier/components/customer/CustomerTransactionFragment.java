package com.mfh.litecashier.components.customer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.CustomerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 会员交易查询
 * Created by bingshanguxue on 15/8/30.
 */
public class CustomerTransactionFragment extends BaseFragment {
    public static final String EXTRA_KEY_HUMAN = "human";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.customer_view)
    CustomerView mCustomerView;

    @BindView(R.id.tabstrip_pay)
    TopSlidingTabStrip paySlidingTabStrip;
    @BindView(R.id.tab_viewpager)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    private Human mMemberInfo = null;

    public static CustomerTransactionFragment newInstance(Bundle args) {
        CustomerTransactionFragment fragment = new CustomerTransactionFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_customer_transaction;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mMemberInfo = (Human) args.getSerializable(EXTRA_KEY_HUMAN);
        }

        toolbar.setTitle("交易查询");
//        setSupportActionBar(toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_normal);

//自动加载会员信息
        refreshVipMemberInfo(mMemberInfo);
        initTabs();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyLoadData(paySlidingTabStrip.getCurrentPosition());
            }
        }, 1000);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyLoadData(page);
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        Bundle parArgs = new Bundle();
        if (mMemberInfo != null) {
            parArgs.putLong(CustomerGoodsOrderFragment.EXTRA_KEY_HUMANID,
                    mMemberInfo.getId());
        }

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("订单", "订单", CustomerGoodsOrderFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("账户", "账户", CustomerAccountFlowFragment.class,
                parArgs));

        viewPagerAdapter.addAllTab(mTabs);

        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    /**
     * 加载会员信息
     */
    private void refreshVipMemberInfo(Human memberInfo) {
        mMemberInfo = memberInfo;
        mCustomerView.reload(memberInfo);
    }

    private void notifyLoadData(int page) {
        if (page == 0) {
            EventBus.getDefault().post(new CustomerEvent(CustomerEvent.EVENT_ID_CUSTOMER_GOODS_ORDERS_RELOAD, null));
        } else if (page == 1) {
            EventBus.getDefault().post(new CustomerEvent(CustomerEvent.EVENT_ID_CUSTOMER_FLOW_RELOAD, null));
        }
    }

    private void notifyPrintData(int page) {
        if (page == 0) {
            EventBus.getDefault().post(new CustomerEvent(CustomerEvent.EVENT_ID_CUSTOMER_GOODS_ORDERS_PRINT, null));
        } else if (page == 1) {
            EventBus.getDefault().post(new CustomerEvent(CustomerEvent.EVENT_ID_CUSTOMER_FLOW_PRINT, null));
        }
    }

    @OnClick(R.id.fab_print)
    public void printOrders() {
        notifyPrintData(paySlidingTabStrip.getCurrentPosition());
    }

}

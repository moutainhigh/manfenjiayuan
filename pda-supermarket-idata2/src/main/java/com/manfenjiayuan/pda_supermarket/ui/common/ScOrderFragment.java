package com.manfenjiayuan.pda_supermarket.ui.common;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.business.mvp.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.mvp.view.IScOrderView;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * 商城订单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScOrderFragment extends BaseFragment implements IScOrderView {

    public static final String EXTRA_KEY_SCORDER = "scOrder";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @BindView(R.id.viewpager_pagecontent)
    ViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    private ScOrder mScOrder;
    private ScOrderPresenter mScOrderPresenter;

    public static ScOrderFragment newInstance(Bundle args) {
        ScOrderFragment fragment = new ScOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected boolean isResponseBackPressed() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_scorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mScOrderPresenter = new ScOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            mScOrder = (ScOrder) args.getSerializable(EXTRA_KEY_SCORDER);
        }

        mToolbar.setTitle("订单详情");
        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
//        // Set an OnMenuItemClickListener to handle menu item clicks
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_submit) {
//                    submit();
//                }
//                return true;
//            }
//        });
//        // Inflate a menu to be displayed in the toolbar
//        mToolbar.inflateMenu(R.menu.menu_inv_recv);

        initTabs();

        reload();
    }

    @Override
    public boolean onBackPressed() {
//        if (goodsAdapter.getItemCount() > 0) {
//            showConfirmDialog("退出后商品列表将会清空，确定要退出吗？",
//                    "退出", new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//
//                            getActivity().setResult(Activity.RESULT_CANCELED);
//                            getActivity().finish();
//                        }
//                    }, "点错了", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//        } else {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
//        }

        return isResponseBackPressed();
    }


    private void initTabs() {
        mTabStrip.setOnClickTabListener(null);
        mTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
//                refreshData(page);
//                if (page != 0){
//                    mScanBar.requestFocusEnd();
//                }
            }
        });

        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                mTabStrip, mViewPager, com.bingshanguxue.pda.R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("订单", "订单", ScOrderInfoFragment.class,
                null));
        mTabs.add(new ViewPageInfo("条码", "条码", ScOrderBarcodeFragment.class,
                null));
        mTabs.add(new ViewPageInfo("商品", "商品", ScOrderItemsFragment.class,
                null));
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    /**
     * 通知更新
     */
    private void notifyScOrder(boolean isDelay) {
        if (isDelay) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bundle args = new Bundle();
                    args.putSerializable(ScOrderEvent.EXTRA_KEY_SCORDER, mScOrder);
                    EventBus.getDefault().post(new ScOrderEvent(ScOrderEvent.EVENT_ID_UPDATE, args));
                }
            }, 1000);
        } else {
            Bundle args = new Bundle();
            args.putSerializable(ScOrderEvent.EXTRA_KEY_SCORDER, mScOrder);
            EventBus.getDefault().post(new ScOrderEvent(ScOrderEvent.EVENT_ID_UPDATE, args));
        }
    }


    /**
     * 刷新
     */
    private void reload() {
        if (mScOrder == null) {
            DialogUtil.showHint("订单无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
                DialogUtil.showHint(R.string.toast_network_error);
                refresh(mScOrder);
                return;
            }

            //不限状态，订单有可能已经发生变化，但是页面没有刷新。
            mScOrderPresenter.getByBarcode(mScOrder.getBarcode(), null, true);
        }
    }

    private void refresh(ScOrder scOrder) {
        mScOrder = scOrder;

//        if (scOrder == null){
//            DialogUtil.showHint("");
//        }
//        DialogUtil.showHint("订单无效");
//        getActivity().setResult(Activity.RESULT_CANCELED);
//        getActivity().finish();

        notifyScOrder(true);
    }

    @Override
    public void onIScOrderViewProcess() {

    }

    @Override
    public void onIScOrderViewError(String errorMsg) {
        refresh(mScOrder);
    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {
        refresh(data);
    }
}

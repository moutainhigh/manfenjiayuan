package com.manfenjiayuan.pda_supermarket.ui.buyer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderServicedFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderServicingFragment;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;

import butterknife.BindView;


/**
 * 买手——组货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class BuyPrepareOrderFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.scanBar)
    public ScanBar mScanBar;
    @BindView(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @BindView(R.id.viewpager_pagecontent)
    ViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @BindView(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    public static BuyPrepareOrderFragment newInstance(Bundle args) {
        BuyPrepareOrderFragment fragment = new BuyPrepareOrderFragment();

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
        return R.layout.fragment_buyer_scorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }

        mToolbar.setTitle("组货");
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
        mScanBar.setVisibility(View.GONE);

        initTabs();

        btnSubmit.setVisibility(View.GONE);
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
                mTabStrip, mViewPager, R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

        Bundle args = new Bundle();
        //订单已创建，买手待抢单组货
        mTabs.add(new ViewPageInfo("待组货", "待组货", PrepareAbleOrdersFragment.class,
                args));
        //买手抢单后待组货
        args.putInt(ScOrderServicedFragment.EXTRA_KEY_ROLETYPE, 0);
        mTabs.add(new ViewPageInfo("组货中", "组货中", ScOrderServicingFragment.class,
                args));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

}

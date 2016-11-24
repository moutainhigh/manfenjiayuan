package com.manfenjiayuan.pda_supermarket.ui.rider;

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
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderCancelledFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderServicedFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderServicingFragment;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;

import butterknife.BindView;


/**
 * 骑手——线上订单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InstockScOrderFragment extends BaseFragment {

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

    public static InstockScOrderFragment newInstance(Bundle args) {
        InstockScOrderFragment fragment = new InstockScOrderFragment();

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

        setupToolbar();
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


    /**
     * 设置toolbar
     * */
    private void setupToolbar(){
        mToolbar.setTitle("骑手订单");
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

        //订单已创建，买手待抢单组货
//        mTabs.add(new ViewPageInfo("待组货", "待组货", PrepareAbleOrdersFragment.class,
//                args));
        //买手抢单后待组货
//        mTabs.add(new ViewPageInfo("组货中", "组货中", ScOrderServicingFragment.class,
//                args));
        //买手组货完成，骑手待揽件
        mTabs.add(new ViewPageInfo("待配送", "待配送", AcceptableSendOrdersFragment.class,
                genBundle(1, null)));
        //骑手揽件后待妥投
        mTabs.add(new ViewPageInfo("配送中", "配送中", ScOrderServicingFragment.class,
                genBundle(1, "3")));
        //骑手妥投成功
        mTabs.add(new ViewPageInfo("妥投", "妥投", ScOrderServicedFragment.class,
                genBundle(1, "12")));
        //有退货商品
        mTabs.add(new ViewPageInfo("有退货", "有退货", ScOrderCancelledFragment.class,
                genBundle(1, null)));


        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    /**
     * */
    private Bundle genBundle(int roleType, String status){
        Bundle args = new Bundle();
        args.putInt(ScOrderServicedFragment.EXTRA_KEY_ROLETYPE, roleType);
        if (!StringUtils.isEmpty(status)){
            args.putString(ScOrderServicedFragment.EXTRA_KEY_STATUS, status);
        }
        return args;
    }

}

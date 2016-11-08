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
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderCancelledFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderServicedFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderServicingFragment;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;

import butterknife.Bind;


/**
 * 买手——线上订单
 * <table>
 *     <tr><th>订单状态</th><th>变更操作</th><th>说明</th></tr>
 *     <tr>
 *         <td>待购买</td>
 *         <td>用户下单</td>
 *         <td>用户下完订单，尚未为买手抢单</td>
 *     </tr>
 *     <tr>
 *         <td>购买中</td>
 *         <td>买手抢单</td>
 *         <td>买手抢完单后，用户就不能取消订单了</td>
 *     </tr>
 *     <tr>
 *         <td>待配送</td>
 *         <td>1.买手点击“完成”
 2.pos端拣货完成</td>
 *         <td>买手组完货后，可以走收银台进行订单的二次调整，也可以自己点击完成，组完货，买手只需将打包后的商品放在配送处即可，无需关心哪个骑手来送</td>
 *     </tr>
 *     <tr>
 *         <td>配送中</td>
 *         <td>骑手揽货</td>
 *         <td>骑手通过PDA揽件后，订单就变成了配送中</td>
 *     </tr>
 *     <tr>
 *         <td>已完成</td>
 *         <td>骑手妥投</td>
 *         <td>骑手已将订单交付用户，点击完成</td>
 *     </tr>
 *     <tr>
 *         <td>有退货</td>
 *         <td>骑手取消单品</td>
 *         <td>大部分情况用户是只会退部分商品，骑手只需在订单里删除部分商品即可，如果要整单取消，也是相同的操作，只需把所有商品删除即可。有退单的订单应该也是属于“已完成”的订单，有退货应该是订单另外一个维度的属性</td>
 *     </tr>
 *     <tr>
 *         <td>已取消</td>
 *         <td>用户取消</td>
 *         <td>用户下完单后，可以取消该订单，但如果该订单已被买手抢单了，就不能退单了，在用户的订单详情页面里就没有取消的按钮了</td>
 *     </tr>
 *
 * </table>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class BuyScOrderFragment extends BaseFragment {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;
    @Bind(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @Bind(R.id.viewpager_pagecontent)
    ViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    public static BuyScOrderFragment newInstance(Bundle args) {
        BuyScOrderFragment fragment = new BuyScOrderFragment();

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
        return R.layout.fragment_invfind_create;
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

        mToolbar.setTitle("买手订单");
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

        //订单已创建，买手待抢单组货
        mTabs.add(new ViewPageInfo("待组货", "待组货", PrepareAbleOrdersFragment.class,
                genBundle(0, null)));
        //买手抢单后待组货
        mTabs.add(new ViewPageInfo("组货中", "组货中", ScOrderServicingFragment.class,
                genBundle(0, null)));
        //买手组货完成，骑手待揽件
        mTabs.add(new ViewPageInfo("待配送", "待配送", ScOrderServicedFragment.class,
                genBundle(0, "3")));
        //骑手揽件后待妥投
        mTabs.add(new ViewPageInfo("配送中", "配送中", ScOrderServicedFragment.class,
                genBundle(0, "6")));
        //骑手妥投成功
        mTabs.add(new ViewPageInfo("妥投", "妥投", ScOrderServicedFragment.class,
                genBundle(0, "12")));
        //有退货商品
        mTabs.add(new ViewPageInfo("有退货", "有退货", ScOrderCancelledFragment.class,
                genBundle(0, null)));

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

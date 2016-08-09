package com.mfh.litecashier.ui.fragment.purchase;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.mfh.litecashier.ui.fragment.ReportFragment;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 采购商品详情
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class PurchaseGoodsDetailFragment extends BaseFragment {
    public static final String EXTRA_KEY_SKU_NAME   = "skuname";
    public static final String EXTRA_KEY_BARCODE    = "barcode";
    public static final String EXTRA_KEY_IMAGE_URL  = "imageUrl";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @Bind(R.id.iv_header)
    ImageView ivHeader;
    @Bind(R.id.tv_product_name)
    TextView tvProductName;
    @Bind(R.id.tv_barcode)
    TextView tvBarcode;

    @Bind(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @Bind(R.id.viewpager_pagecontent)
    ViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    private String skuName = "";
    private String barcode = "";
    private String imageUrl = "";

    public static PurchaseGoodsDetailFragment newInstance(Bundle args) {
        PurchaseGoodsDetailFragment fragment = new PurchaseGoodsDetailFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_goodsdetail;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            skuName = args.getString(EXTRA_KEY_SKU_NAME);
            barcode= args.getString(EXTRA_KEY_BARCODE);
            imageUrl = args.getString(EXTRA_KEY_IMAGE_URL);
        }

        tvHeaderTitle.setText("商品详情");
        Glide.with(getContext()).load(imageUrl).error(R.mipmap.ic_image_error).into(ivHeader);
        tvProductName.setText(skuName);
        tvBarcode.setText(barcode);

        initTabs();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private void initTabs() {
        mTabStrip.setOnClickTabListener(null);
        //TODO
        mTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
//                refreshData(page);
            }
        });

        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mTabStrip, mViewPager, R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("月销量", "月销量", ReportFragment.class,
                null));
        mTabs.add(new ViewPageInfo("日销量", "日销量", ReportFragment.class,
                null));
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }


    public void refresh(){
//        refreshData(mTabStrip.getCurrentPosition());
    }

}

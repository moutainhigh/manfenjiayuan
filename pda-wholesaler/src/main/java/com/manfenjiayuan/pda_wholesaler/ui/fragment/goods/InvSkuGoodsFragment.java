package com.manfenjiayuan.pda_wholesaler.ui.fragment.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.bizz.goods.GoodsSalesFragment;
import com.bingshanguxue.pda.bizz.goods.ScGoodsSkuEvent;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 商品档案
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvSkuGoodsFragment extends PDAScanFragment implements IInvSkuGoodsView {

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    @Bind(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @Bind(R.id.viewpager_pagecontent)
    ViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    private InvSkuGoods curGoods = null;
    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;

    public static InvSkuGoodsFragment newInstance(Bundle args) {
        InvSkuGoodsFragment fragment = new InvSkuGoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInvSkuGoodsPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
//        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);
//        mTabStrip = (TopSlidingTabStrip) rootView.findViewById(R.id.tab_page);
//        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_pagecontent);

        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
            mToolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().onBackPressed();
                        }
                    });
        } else {
            ZLogger.d("mToolbar is null");
        }

        if (mScanBar != null) {
//            mScanBar.setSoftKeyboardEnabled(true);
            mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }

                @Override
                public void onAction1Click(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }
            });
        } else {
            ZLogger.d("mScanBar is null");
        }

        initTabs();
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
        mScanBar.reset();
        queryByBarcode(code);
    }

    private void initTabs() {
        mTabStrip.setOnClickTabListener(null);
        mTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
//                refreshData(page);
            }
        });

        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                mTabStrip, mViewPager, R.layout.tabitem_text_large);
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("信息", "信息", GoodsInfoFragment.class,
                null));
//        mTabs.add(new ViewPageInfo("供应商", "供应商", GoodsChainFragment.class,
//                null));
        mTabs.add(new ViewPageInfo("销量", "销量", GoodsSalesFragment.class,
                null));
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }


    /**
     * 搜索条码
     */
    public void queryByBarcode(String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            onQueryError("请先扫描商品条码");
            return;
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        if (mInvSkuGoodsPresenter != null) {
            mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
        } else {
            refresh(null, false);
        }
    }



    public void onQueryError(String errorMsg) {
        ZLogger.df(errorMsg);
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        isAcceptBarcodeEnabled = true;

        refresh(null, false);
    }

    /**
     * 刷新信息
     */
    private void refresh(InvSkuGoods invSkuGoods, boolean isEditable) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = invSkuGoods;

        Bundle args = new Bundle();
        if (curGoods != null){
            args.putLong(ScGoodsSkuEvent.EXTRA_KEY_PROSKUID, curGoods.getProSkuId());
        }
        args.putSerializable(ScGoodsSkuEvent.EXTRA_KEY_SCGOODSSKU, curGoods);
        args.putBoolean(ScGoodsSkuEvent.EXTRA_KEY_ISEDITABLE, isEditable);
        EventBus.getDefault().post(new ScGoodsSkuEvent(ScGoodsSkuEvent.EVENT_ID_SKU_UPDATE, args));
    }

    @Override
    public void onIInvSkuGoodsViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);
    }

    @Override
    public void onIInvSkuGoodsViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

        refresh(null, false);
    }

    @Override
    public void onIInvSkuGoodsViewSuccess(InvSkuGoods data) {
        hideProgressDialog();

        refresh(data, true);
    }
}

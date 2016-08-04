package com.manfenjiayuan.pda_supermarket.ui.fragment.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScGoodsSkuFragment extends PDAScanFragment implements IScGoodsSkuView {

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    @Bind(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
    @Bind(R.id.viewpager_pagecontent)
    ViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    private ScGoodsSku curGoods = null;
    private ScGoodsSkuPresenter mScGoodsSkuPresenter = null;

    public static ScGoodsSkuFragment newInstance(Bundle args) {
        ScGoodsSkuFragment fragment = new ScGoodsSkuFragment();

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

        mScGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
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
            mScanBar.setSoftKeyboardEnabled(true);
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
        //TODO
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
        mTabs.add(new ViewPageInfo("供应商", "供应商", GoodsChainFragment.class,
                null));
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

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        if (mScGoodsSkuPresenter != null) {
            mScGoodsSkuPresenter.findGoodsListByBarcode(barcode);
        } else {
            refresh(null);
        }
    }

    /**
     * 查询商品信息
     */
    public void queryByName(String name) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(name)) {
            mScanBar.requestFocus();
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            refresh(null);
            return;
        }

        if (mScGoodsSkuPresenter != null) {
            mScGoodsSkuPresenter.findGoodsListByName(name);
        } else {
            refresh(null);
        }
    }

    public void onQueryError(String errorMsg) {
        ZLogger.df(errorMsg);
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        isAcceptBarcodeEnabled = true;

        refresh(null);
    }

    /**
     * 刷新信息
     */
    private void refresh(ScGoodsSku invSkuGoods) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = invSkuGoods;
        if (curGoods == null) {
            mScanBar.reset();
        }

        Bundle args = new Bundle();
        args.putSerializable("scGoodsSku", curGoods);
        EventBus.getDefault().post(new ScGoodsSkuEvent(ScGoodsSkuEvent.EVENT_ID_SKU_UPDATE, args));
    }


    @Override
    public void onIScGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        onQueryError(errorMsg);
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        if (dataList != null && dataList.size() > 0) {
            ScGoodsSku scGoodsSku = dataList.get(0);
            refresh(scGoodsSku);
        } else {
            DialogUtil.showHint("未找到商品");
            refresh(null);
        }

        hideProgressDialog();
    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku goodsSku) {

    }
}

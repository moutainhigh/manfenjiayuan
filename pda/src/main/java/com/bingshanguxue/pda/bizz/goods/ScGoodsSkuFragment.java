package com.bingshanguxue.pda.bizz.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.dialog.ActionDialog;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
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

import de.greenrobot.event.EventBus;


/**
 * 商品档案
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScGoodsSkuFragment extends PDAScanFragment implements IScGoodsSkuView {

//    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
//    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

//    @Bind(R.id.tab_page)
    TopSlidingTabStrip mTabStrip;
//    @Bind(R.id.viewpager_pagecontent)
    ViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;


    private String curBarcode;
    private ScGoodsSku curGoods = null;
    private ScGoodsSkuPresenter mScGoodsSkuPresenter = null;


    private ActionDialog mActionDialog = null;


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
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);
        mTabStrip = (TopSlidingTabStrip) rootView.findViewById(R.id.tab_page);
        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_pagecontent);

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

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        if (mScGoodsSkuPresenter != null) {
            curBarcode = barcode;
            mScGoodsSkuPresenter.findGoodsListByBarcode(barcode);
        } else {
            refresh(null, false);
        }
    }


    /**
     * 网点商品档案未找到商品提示
     */
    private void noGoodsAlert(final String barcode) {
        if (mActionDialog == null) {
            mActionDialog = new ActionDialog(getActivity());
            mActionDialog.setCancelable(false);
            mActionDialog.setCanceledOnTouchOutside(false);
        }
        mActionDialog.init("无结果", String.format("%s 商品档案未在当前网点登记", barcode),
                new ActionDialog.DialogClickListener() {
                    @Override
                    public void onAction1Click() {
                        mScGoodsSkuPresenter.getByBarcode(barcode);
                    }

                    @Override
                    public void onAction2Click() {
                        hideProgressDialog();
                        refresh(null, false);
                    }

                    @Override
                    public void onAction3Click() {
                    }
                });
        mActionDialog.registerActions("查询平台商品档案", "暂不查询", "");
        if (!mActionDialog.isShowing()) {
            mActionDialog.show();
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

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            refresh(null, false);
            return;
        }

        if (mScGoodsSkuPresenter != null) {
            mScGoodsSkuPresenter.findGoodsListByName(name);
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
    private void refresh(ScGoodsSku invSkuGoods, boolean isEditable) {
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
            refresh(scGoodsSku, true);

            hideProgressDialog();
        } else {
            if (!StringUtils.isEmpty(curBarcode)) {
                noGoodsAlert(curBarcode);
                curBarcode = null;
            } else {
                DialogUtil.showHint("未找到商品");
                hideProgressDialog();
                refresh(null, false);
            }
        }
    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku goodsSku) {
        refresh(goodsSku, false);
        curBarcode = null;
        hideProgressDialog();

        if (goodsSku == null) {
            DialogUtil.showHint("未找到商品");
        }
    }
}

package com.bingshanguxue.pda.bizz.goods;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.bizz.FragmentActivity;
import com.bingshanguxue.pda.dialog.ActionDialog;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.manfenjiayuan.business.presenter.InvSkuBizPresenter;
import com.manfenjiayuan.business.view.IInvSkuBizView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invSkuStore.InvSkuBizBean;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.RegularUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


/**
 * 商品档案
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScGoodsSkuFragment extends PDAScanFragment implements IInvSkuBizView {

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
    private InvSkuBizBean curGoods = null;
    private InvSkuBizPresenter mScGoodsSkuPresenter = null;


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

        mScGoodsSkuPresenter = new InvSkuBizPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);
        mTabStrip = (TopSlidingTabStrip) rootView.findViewById(R.id.tab_page);
        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_pagecontent);

        if (mToolbar != null) {
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
        }

        if (mScanBar != null) {
            mScanBar.setInputType(EditInputType.TEXT);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_INVSKUBIZ_GOODS_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    onIInvSkuBizViewSuccess((InvSkuBizBean) data.getSerializableExtra("invSkuBizBean"));
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
//                if (page != 0){
//                    mScanBar.requestFocusEnd();
//                }
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
//        DeviceUtils.hideSoftInput(getActivity());
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            onQueryError("请先扫描商品条码");
            return;
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }


        if (mScGoodsSkuPresenter != null) {

            if (RegularUtils.matcher(barcode, RegularUtils.PATTERN_BARCODE)) {
                curBarcode = barcode;
                mScGoodsSkuPresenter.getBeanByBizKeys(barcode);
            } else {
                Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_INVSKUBIZ_GOODS);
                extras.putString(InvSkuBizGoodsListFragment.EXTRA_SKUNAME, barcode);
                Intent intent = new Intent(getActivity(), FragmentActivity.class);
                intent.putExtras(extras);
                startActivityForResult(intent, ARCode.ARC_INVSKUBIZ_GOODS_LIST);
            }

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
        mActionDialog.init(barcode, "无结果，商品档案未在当前网点登记",
                new ActionDialog.DialogClickListener() {
                    @Override
                    public void onAction1Click() {
                        mScGoodsSkuPresenter.getBeanByBizKeys(barcode);
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

    public void onQueryError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            ZLogger.df(errorMsg);
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
        isAcceptBarcodeEnabled = true;

        refresh(null, false);
    }

    /**
     * 刷新信息
     */
    private void refresh(InvSkuBizBean invSkuGoods, boolean isEditable) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = invSkuGoods;

        Bundle args = new Bundle();
        if (curGoods != null) {
            args.putLong(ScGoodsSkuEvent.EXTRA_KEY_PROSKUID, curGoods.getProSkuId());
//            args.putLong(ScGoodsSkuEvent.EXTRA_KEY_PROSKUID, curGoods.getProSkuId());
        }
        args.putSerializable(ScGoodsSkuEvent.EXTRA_KEY_SCGOODSSKU, curGoods);
        args.putBoolean(ScGoodsSkuEvent.EXTRA_KEY_ISEDITABLE, isEditable);
        EventBus.getDefault().post(new ScGoodsSkuEvent(ScGoodsSkuEvent.EVENT_ID_SKU_UPDATE, args));
    }

    @Override
    public void onIInvSkuBizViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);

    }

    @Override
    public void onIInvSkuBizViewError(String errorMsg) {
        onQueryError(errorMsg);
    }

    @Override
    public void onIInvSkuBizViewSuccess(InvSkuBizBean data) {
        if (data != null) {
            refresh(data, true);
            curBarcode = null;
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
    public void onIInvSkuBizViewSuccess(PageInfo pageInfo, List<InvSkuBizBean> dataList) {

    }
}

package com.manfenjiayuan.pda_supermarket.ui.buyer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.business.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.view.IScOrderView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderBarcodeFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderEvent;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderInfoFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderItemsFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 新建拣货单：线上订单抢单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class AcceptPrepareOrdersFragment extends BaseFragment implements IScOrderView {

    public static final String EXTRA_KEY_SCORDER = "scOrder";

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


    private ScOrder mScOrder;
    private ScOrderPresenter mScOrderPresenter;

    public static AcceptPrepareOrdersFragment newInstance(Bundle args) {
        AcceptPrepareOrdersFragment fragment = new AcceptPrepareOrdersFragment();

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

        //清空签收数据库
//        InvRecvGoodsService.get().clear();

        mScOrderPresenter = new ScOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            mScOrder = (ScOrder) args.getSerializable(EXTRA_KEY_SCORDER);
        }

        mToolbar.setTitle("抢单");
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
                refresh(null);
                return;
            }

            mScOrderPresenter.getByBarcode(mScOrder.getBarcode(), mScOrder.getStatus(), true);
        }
    }

    private void refresh(ScOrder scOrder) {
        mScOrder = scOrder;
        notifyScOrder(true);
    }

    @Override
    public void onIScOrderViewProcess() {

    }

    @Override
    public void onIScOrderViewError(String errorMsg) {
        refresh(null);
    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {
        refresh(data);
    }


//    /**
//     * 拣货-抢单
//     */
//    @OnClick(R.id.fab_submit)
//    public void submit() {
//        btnSubmit.setEnabled(false);
//        showConfirmDialog("确定要抢单吗？",
//                "确定", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//
//                    }
//                }, "点错了", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                        btnSubmit.setEnabled(true);
//                    }
//                });
//    }

    /**
     * 抢单
     */
    @OnClick(R.id.fab_submit)
    public void submitStep1() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "抢单中...", false);

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onSubmitError(getString(R.string.toast_network_error));
            return;
        }


        ScOrderApiImpl.acceptOrderWhenOrdered(mScOrder.getId(), responseRC);
    }

    NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"更新成功!","version":"1","data":null}
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                    String result = null;
                    if (rspData != null) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        result = retValue.getValue();
                    }

                    //出库成功:1-556637
                    DialogUtil.showHint("抢单成功");
                    ZLogger.d(String.format("抢单成功: %s", result));
                    onSubmitSuccess();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onSubmitError(errMsg);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 提交处理中
     */
    public void onSubmitProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 提交失败
     */
    public void onSubmitError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
            ZLogger.df(errorMsg);
        } else {
            hideProgressDialog();
        }
        btnSubmit.setEnabled(true);
    }

    public void onSubmitSuccess() {
        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
        btnSubmit.setEnabled(true);
        //修改商品信息成功后，清空商品信息
//        refresh(null);
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

}

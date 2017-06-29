package com.manfenjiayuan.pda_supermarket.ui.rider;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.PDAScanManager;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.manfenjiayuan.business.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.view.IScOrderView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.CashierAgent;
import com.manfenjiayuan.pda_supermarket.cashier.model.CashierOrderInfo;
import com.manfenjiayuan.pda_supermarket.database.entity.InstockTempEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.InstockTempService;
import com.manfenjiayuan.pda_supermarket.ui.PrimaryActivity;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderBarcodeFragment;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderEvent;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderInfoFragment;
import com.manfenjiayuan.pda_supermarket.ui.pay.instock.InstockPayFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderApiImpl;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.ScOrderHttpManager;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;


/**
 * 骑手——妥投
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InstockOrderFragment extends PDAScanFragment implements IScOrderView {

    public static final String EXTRA_KEY_SCORDER = "scOrder";

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
    @BindView(R.id.fab_scan)
    FloatingActionButton btnSweep;

    private ScOrder mScOrder;
    private ScOrderPresenter mScOrderPresenter;

    public static InstockOrderFragment newInstance(Bundle args) {
        InstockOrderFragment fragment = new InstockOrderFragment();

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

        //清空签收数据库
        InvRecvGoodsService.get().clear();

        mScOrderPresenter = new ScOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            mScOrder = (ScOrder) args.getSerializable(EXTRA_KEY_SCORDER);
        }

        mToolbar.setTitle("拣货");
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
        mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                queryByBarcode(text);
            }

            @Override
            public void onAction1Click(String text) {
                queryByBarcode(text);
            }
        });
        btnSweep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt(PDAScanManager.ScanBarcodeEvent.KEY_EVENTID,
                        PDAScanManager.ScanBarcodeEvent.EVENT_ID_START_ZXING);
                EventBus.getDefault().post(new PDAScanManager.ScanBarcodeEvent(args));
            }
        });

        if (SharedPrefesManagerFactory.isCameraSweepEnabled()){
            btnSweep.setVisibility(View.VISIBLE);
        }
        else{
            btnSweep.setVisibility(View.GONE);
        }

        initTabs();

        InstockTempService.get().clear();
        refresh(mScOrder, false, true);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_RIDER_INSTOCK_PAY: {
                if (resultCode == Activity.RESULT_OK){
                    submitStep4();
                }
                else{
                    onSubmitError("取消支付");
                    //妥投接口内部还会判断一次，如果未补差价，会自动补差价,如果余额不足则会报错
//                    submitStep4();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        mTabs.add(new ViewPageInfo("商品", "商品", InstockOrderItemsFragment.class,
                null));
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyScOrder() {
        Bundle args = new Bundle();
        args.putSerializable(ScOrderEvent.EXTRA_KEY_SCORDER, mScOrder);
        EventBus.getDefault().post(new ScOrderEvent(ScOrderEvent.EVENT_ID_UPDATE, args));
    }

    private void refresh(ScOrder scOrder, boolean hintEnabled, boolean isDelay) {
        mScOrder = scOrder;
        InstockTempService.get().clear();
        if (mScOrder != null) {
            List<ScOrderItem> items = mScOrder.getItems();
            if (items != null && items.size() > 0) {
                for (ScOrderItem item : items) {
                    InstockTempService.get().saveOrUpdate(item);
                }
            }
            btnSubmit.setEnabled(true);
        }
        else{
            if (hintEnabled){
                DialogUtil.showHint("未找到订单");
            }
            btnSubmit.setEnabled(false);
        }

        if (isDelay) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyScOrder();
                }
            }, 1000);
        } else {
            notifyScOrder();
        }
    }

    /**
     * 拣货-抢单
     */
    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        showConfirmDialog("确定要妥投吗？",
                "确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        submitStep1();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        btnSubmit.setEnabled(true);
                    }
                });
    }

    /**
     * 加载优惠券列表
     */
    public void submitStep1() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        btnSubmit.setEnabled(false);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        JSONArray jsonArray = new JSONArray();
//        List<ScOrderItem> items = mScOrder.getItems();
//        if (items != null && items.size() > 0) {
//            for (ScOrderItem item : items) {
//                JSONObject jsonObject = new JSONObject();
//                // TODO: 19/10/2016
//                jsonObject.put("skuId", item.getSkuId());
//                jsonObject.put("bcount", item.getQuantityCheck());
//                jsonArray.add(jsonObject);
//            }
//        }
        List<InstockTempEntity> entities = InstockTempService.get().queryAll();
        if (entities != null && entities.size() > 0){
            for (InstockTempEntity entity : entities) {
                JSONObject jsonObject = new JSONObject();
                // TODO: 19/10/2016
                jsonObject.put("skuId", entity.getSkuId());
                //收货
                if (entity.getIsEnable() == 1){
                    jsonObject.put("bcount", entity.getCommitCount());
                }
                //退货
                else{
                    jsonObject.put("bcount", 0D);
                }
                jsonArray.add(jsonObject);
            }
        }

        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(mScOrder.getId()));
        options.put("jsonStr", jsonArray.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ScOrderHttpManager.getInstance().updateCommitInfo(options,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onSubmitError(e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.df("发货并通知骑手:" + s);
                        submitStep2();

                    }
                });
    }

    NetCallBack.NetTaskCallBack updateCommitInfoRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"操作成功!","version":"1","data":null}
                    if (rspData != null) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();
                    }
                    submitStep2();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onSubmitError(errMsg);
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };

    /**
     * 加载优惠券列表
     */
    public void submitStep2() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        btnSubmit.setEnabled(false);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        ScOrderApiImpl.checkOddAmount(mScOrder.getId(), checkOddAmountRC);
    }

    NetCallBack.NetTaskCallBack checkOddAmountRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"查询成功!","version":"1","data":{"val":"0.0"}}
                    Double amount = null;

                    if (rspData != null) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String result = retValue.getValue();

                        if (!StringUtils.isEmpty(result)){
                            amount = Double.valueOf(result);
                        }
                    }

                    submitStep3(amount);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onSubmitError(errMsg);
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };


    /**
     * 补差价
     */
    private void submitStep3(Double amount) {
        //返回差额，正值代表需要退钱给用户，负值代表需要用户补钱
        if (amount != null && amount < 0) {
            CashierOrderInfo cashierOrderInfo = CashierAgent.makeCashierOrderInfo(mScOrder, Math.abs(amount));
            Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_INV_RIDER_INSTOCK_PAY);
            extras.putSerializable(InstockPayFragment.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
            Intent intent = new Intent(getActivity(), PrimaryActivity.class);
            intent.putExtras(extras);
            startActivityForResult(intent, ARCode.ARC_RIDER_INSTOCK_PAY);

        } else {
            submitStep4();
        }
    }

    /**
     * 妥投
     */
    private void submitStep4() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        ScOrderApiImpl.arriveToEndCustom(String.valueOf(mScOrder.getId()), responseRC);
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
                    DialogUtil.showHint("订单已妥投");
                    ZLogger.d(String.format("订单已妥投: %s", result));
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

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
        mScanBar.reset();
        queryByBarcode(code);
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

        if (mScOrderPresenter != null) {
            mScOrderPresenter.getByBarcode(barcode, ScOrder.MFHORDER_STATUS_SENDED, true);
        } else {
            refresh(null, false, false);
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

        refresh(null, false, false);
    }


    @Override
    public void onIScOrderViewProcess() {

    }

    @Override
    public void onIScOrderViewError(String errorMsg) {
        onQueryError(errorMsg);
    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {

    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {
        refresh(data, true, false);
    }
}

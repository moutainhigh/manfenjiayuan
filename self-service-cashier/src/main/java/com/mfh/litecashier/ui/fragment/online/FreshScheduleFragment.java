package com.mfh.litecashier.ui.fragment.online;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.InvSendOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendOrderItem;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.impl.InvSendOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.FreshScheduleGoods;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.event.InvSendOrderEvent;
import com.mfh.litecashier.event.PurchaseSendEvent;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.ui.adapter.FreshScheduleGoodsAdapter;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.mfh.litecashier.ui.dialog.TextInputDialog;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 生鲜预定
 * Created by bingshanguxue on 15/8/31.
 */
public class FreshScheduleFragment extends BaseFragment implements IInvSendOrderView {
    @Bind(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @Bind(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    @Bind(R.id.animProgressBar)
    ProgressBar progressBar;
    @Bind(R.id.order_goods_list)
    RecyclerView goodsRecyclerView;
    private FreshScheduleGoodsAdapter goodsListAdapter;

    @Bind(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @Bind(R.id.button_cancel)
    Button btnCancel;
    @Bind(R.id.button_confirm)
    Button btnConfirm;

    private InvSendOrder curOrder;
    private InvSendOrderPresenter invSendOrderPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_fresh_schedule;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        invSendOrderPresenter = new InvSendOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initTabs();
        initGoodsRecyclerView();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ZLogger.d("加载生鲜预定订单");
                notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (SharedPreferencesHelper.isSyncEnabled(SharedPreferencesHelper.PREF_KEY_SYNC_PURCHASESEND_ORDER_ENABLED) || !readCache())
        //先加载数据
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private TextInputDialog mTextInputDialog = null;

    /**
     * 取消订单
     */
    @OnClick(R.id.button_cancel)
    public void cancelOrder() {
        btnCancel.setEnabled(false);

        if (curOrder == null) {
            DialogUtil.showHint("请先选择订单");
            btnCancel.setEnabled(true);
            return;
        }

        if (mTextInputDialog == null) {
            mTextInputDialog = new TextInputDialog(getActivity());
            mTextInputDialog.setCancelable(false);
            mTextInputDialog.setCanceledOnTouchOutside(false);
        }
        mTextInputDialog.initialize("取消订单", "请输入订单取消原因...", true,
                new TextInputDialog.OnTextInputListener() {
                    @Override
                    public void onCancel() {
                        btnCancel.setEnabled(true);
                    }

                    @Override
                    public void onConfirm(String text) {
                        btnCancel.setEnabled(true);
                        if (StringUtils.isEmpty(text)){
                            text = "店长取消。";
                        }
                        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在取消订单...", false);
                        InvSendOrderApiImpl.cancelById(curOrder.getId(), text, cancelRC);
                    }
        });
        if (!mTextInputDialog.isShowing()) {
            mTextInputDialog.show();
        }

//
//        showConfirmDialog("确定要取消订单吗？",
//                "取消", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        btnCancel.setEnabled(true);
//                        InvSendOrderApiImpl.cancelOrderById(curOrder.getId(), cancelRC);
//                    }
//                }, "点错了", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        btnCancel.setEnabled(true);
//                    }
//                });
    }

    NetCallBack.NetTaskCallBack cancelRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    btnCancel.setEnabled(true);
                    if (rspData != null) {
                        notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
                    }
                    showProgressDialog(ProgressDialog.STATUS_DONE, "取消成功", true);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("取消订单失败：" + errMsg);
                    btnCancel.setEnabled(true);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    /**
     * 确认订单&打印订单明细
     */
    @OnClick(R.id.button_confirm)
    public void confirmOrder() {
        btnConfirm.setEnabled(false);

        if (curOrder == null) {
            DialogUtil.showHint("请先选择订单");
            btnConfirm.setEnabled(true);
            return;
        }

        showConfirmDialog("确定要通知客户并打印单据吗？",
                "确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在确认订单...", false);
                        PrintManager.printScheduleOrder(curOrder, goodsListAdapter.getEntityList());
                        InvSendOrderApiImpl.receivePlanOrderOnNet(curOrder.getId(), null, confirmRC);
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        btnConfirm.setEnabled(true);
                    }
                });
    }

    NetCallBack.NetTaskCallBack confirmRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    btnConfirm.setEnabled(true);
                    if (rspData != null) {
                        notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
                    }

                    showProgressDialog(ProgressDialog.STATUS_DONE, "确认成功", true);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("取消订单失败：" + errMsg);
                    btnConfirm.setEnabled(true);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyOrderRefresh(page);
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

        Bundle args1 = new Bundle();
        args1.putString(FreshScheduleOrderFragment.EXTRA_KEY_STATUS,
                String.valueOf(InvOrderApi.ORDER_STATUS_CONFIRM));
        args1.putString(FreshScheduleOrderFragment.EXTRA_KEY_SENDTYPE,
                String.valueOf(InvOrderApi.SENDTYPE_CUSTOMER_MANUAL));
        args1.putString(FreshScheduleOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, InvOrderApi.ORDER_STATUS_CONFIRM));
        mTabs.add(new ViewPageInfo("待确认", "待确认", FreshScheduleOrderFragment.class,
                args1));

        Bundle args2 = new Bundle();
        args2.putString(FreshScheduleOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
        args2.putString(FreshScheduleOrderFragment.EXTRA_KEY_SENDTYPE, String.valueOf(InvOrderApi.SENDTYPE_CUSTOMER_MANUAL));
        args2.putString(FreshScheduleOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, InvOrderApi.ORDER_STATUS_RECEIVE));
        mTabs.add(new ViewPageInfo("已确认", "已确认", FreshScheduleOrderFragment.class,
                args2));

        Bundle args3 = new Bundle();
        args3.putString(FreshScheduleOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_CANCEL));
        args3.putString(FreshScheduleOrderFragment.EXTRA_KEY_SENDTYPE, String.valueOf(InvOrderApi.SENDTYPE_CUSTOMER_MANUAL));
        args3.putString(FreshScheduleOrderFragment.EXTRA_KEY_CACHEKEY,
                String.format("%s_%d", ACacheHelper.CK_PURCHASE_ORDER, InvOrderApi.ORDER_STATUS_CANCEL));
        mTabs.add(new ViewPageInfo("已取消", "已取消", FreshScheduleOrderFragment.class,
                args3));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index) {
        loadGoodsList(null);
        Bundle args = new Bundle();
        if (index == 0) {
            args.putString(FreshScheduleOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_CONFIRM));
            btnCancel.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
            goodsListAdapter.setNumberPickViewVisible(true);
        } else if (index == 1) {
            args.putString(FreshScheduleOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_RECEIVE));
            btnCancel.setVisibility(View.INVISIBLE);
            btnConfirm.setVisibility(View.INVISIBLE);
            goodsListAdapter.setNumberPickViewVisible(false);
        } else if (index == 2) {
            args.putString(FreshScheduleOrderFragment.EXTRA_KEY_STATUS, String.valueOf(InvOrderApi.ORDER_STATUS_CANCEL));
            btnCancel.setVisibility(View.INVISIBLE);
            btnConfirm.setVisibility(View.INVISIBLE);
            goodsListAdapter.setNumberPickViewVisible(false);
        }
        EventBus.getDefault().post(new InvSendOrderEvent(InvSendOrderEvent.EVENT_ID_RELOAD_DATA, args));
    }

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        goodsListAdapter = new FreshScheduleGoodsAdapter(getActivity(), null);
        goodsListAdapter.setOnAdapterListener(new FreshScheduleGoodsAdapter.OnAdapterListener() {
            @Override
            public void onDataSetChanged() {
//                refreshBottomBar();
            }
        });
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(PurchaseSendEvent event) {
        ZLogger.d(String.format("FreshScheduleFragment: PurchaseSendEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseSendEvent.EVENT_ID_RELOAD_DATA) {
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        } else if (event.getEventId() == PurchaseSendEvent.EVENT_ID_RELAOD_ITEM_DATA) {
            Bundle args = event.getArgs();
            if (args != null) {
                loadGoodsList((InvSendOrder) args.getSerializable("order"));
            }
        }
    }

    public void onEventMainThread(PurchaseShopcartSyncEvent event) {
        ZLogger.d(String.format("FreshScheduleFragment: PurchaseShopcartSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_ORDER_SUCCESS) {
            //刷新供应商
            notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
        }
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(InvSendOrder order) {
        curOrder = order;
        if (order == null) {
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：", null, "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：", null, "无", null, null));
            goodsListAdapter.setEntityList(null);
        } else {
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：",
                    curOrder.getAskTotalCount(), "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：",
                    curOrder.getGoodsFee(), "无", null, null));

            //加载订单明细
            invSendOrderPresenter.loadOrderItems(curOrder.getId());
        }
    }

    @Override
    public void onQueryInvSendOrderProcess() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onQueryInvSendOrderError(String errorMsg) {
        if (goodsListAdapter != null) {
            goodsListAdapter.setEntityList(null);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onQueryInvSendOrderSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onQueryInvSendOrderItemsSuccess(List<InvSendOrderItem> dataList) {
        List<FreshScheduleGoods> scheduleGoodsList = new ArrayList<>();
        if (dataList != null && dataList.size() > 0){
            for (InvSendOrderItem invSendOrderItem : dataList){
                FreshScheduleGoods goods = new FreshScheduleGoods();
                goods.setImgUrl(invSendOrderItem.getImgUrl());
                goods.setProductName(invSendOrderItem.getProductName());
                goods.setBarcode(invSendOrderItem.getBarcode());
                goods.setBuyUnit(invSendOrderItem.getBuyUnit());
                goods.setAskTotalCount(invSendOrderItem.getAskTotalCount());
                goods.setQuantityCheck(invSendOrderItem.getAskTotalCount());
                scheduleGoodsList.add(goods);
            }
        }
        if (goodsListAdapter != null) {
            goodsListAdapter.setEntityList(scheduleGoodsList);
        }
        progressBar.setVisibility(View.GONE);
    }
}

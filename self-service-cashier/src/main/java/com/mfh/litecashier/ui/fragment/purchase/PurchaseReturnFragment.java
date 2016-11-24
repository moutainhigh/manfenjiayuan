package com.mfh.litecashier.ui.fragment.purchase;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.framework.api.invSendIoOrder.IInvSendIoOrderView;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItemBrief;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderPresenter;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.PurchaseReturnEvent;
import com.mfh.litecashier.ui.adapter.InvReturnOrderAdapter;
import com.mfh.litecashier.ui.adapter.PurchaseReturnGoodsAdapter;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 采购退货
 * Created by Nat.ZZN(bingshanguxue) on 15/09/24.
 */
public class PurchaseReturnFragment extends BaseListFragment<InvSendIoOrder>
        implements IInvSendIoOrderView {

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private InvReturnOrderAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_view)
    TextView emptyView;

    @BindView(R.id.order_goods_list)
    RecyclerView goodsRecyclerView;
    private PurchaseReturnGoodsAdapter goodsListAdapter;

    @BindView(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @BindView(R.id.tv_total_amount)
    TextView tvTotalAmount;

    private InvSendIoOrder curOrder;
    private InvSendIoOrderPresenter invReturnOrderPresenter;

    public static PurchaseReturnFragment newInstance(Bundle args) {
        PurchaseReturnFragment fragment = new PurchaseReturnFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_return;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        invReturnOrderPresenter = new InvSendIoOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        setupSwipeRefresh();
        initOrderRecyclerView();
        initGoodsRecyclerView();

//        readCache();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(PurchaseReturnEvent event) {
        ZLogger.d(String.format("PurchaseReturnFragment: PurchaseReturnEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseReturnEvent.EVENT_ID_RELOAD_DATA) {
            if (SharedPreferencesUltimate.getBoolean(SharedPreferencesUltimate.PK_SYNC_PURCHASERETURN_ORDER_ENABLED, true)
                    || !readCache()) {
                reload();
            }
        }
    }

    /**
     * 初始化订单列表
     */
    private void initOrderRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        orderRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        orderRecyclerView.setHasFixedSize(true);
        //添加分割线
        orderRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
        //设置列表为空时显示的视图
        orderRecyclerView.setEmptyView(emptyView);
        orderRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动
//                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        orderListAdapter = new InvReturnOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new InvReturnOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                loadGoodsList(orderListAdapter.getCurOrder());
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                loadGoodsList(orderListAdapter.getCurOrder());
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
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
        goodsListAdapter = new PurchaseReturnGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new PurchaseReturnGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onDataSetChanged() {
//                refreshBottomBar();
            }
        });
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public synchronized void reload() {

        if (bSyncInProgress) {
            ZLogger.d("正在加载采购退货订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载采购退货流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        invReturnOrderPresenter.loadReturnOrders(mPageInfo, "1,2,3,4", null, null);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载采购退货订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载采购退货订单流水。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            invReturnOrderPresenter.loadOrders(mPageInfo, "1,2,3,4", null, null);
        } else {
            ZLogger.d("加载采购退货订单流水，已经是最后一页。");
            onLoadFinished();
        }
    }


    @Override
    public void onQueryOrderProcess() {

        onLoadStart();
    }

    @Override
    public void onQueryOrderError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onQueryOrderSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {
        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                ZLogger.d("缓存商品退货订单第一页数据");
                JSONArray cacheArrays = new JSONArray();
                if (dataList != null) {
                    cacheArrays.addAll(dataList);
                }
                if (orderListAdapter != null) {
                    orderListAdapter.setEntityList(dataList);
                }

                ACacheHelper.put(ACacheHelper.CK_PURCHASE_RETURN, cacheArrays.toJSONString());
                SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_SYNC_PURCHASERETURN_ORDER_ENABLED, false);
            } else {
                if (orderListAdapter != null) {
                    orderListAdapter.appendEntityList(dataList);
                }
            }
            ZLogger.d(String.format("加载退货订单结束,pageInfo':page=%d,rows=%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                    orderListAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载商品退货订单失败: %s", ex.toString()));
            onLoadFinished();
        }

    }

    @Override
    public void onQueryOrderItemsSuccess(List<InvSendIoOrderItem> dataList) {

    }


    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_PURCHASE_RETURN);
        List<InvSendIoOrder> cacheData = JSONArray.parseArray(cacheStr, InvSendIoOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d条退货订单", ACacheHelper.CK_PURCHASE_RETURN, cacheData.size()));
//            refreshCategoryGoodsTab(entity.getCategoryId(), cacheData);
            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(cacheData);
            }

            return true;
        }
        return false;
    }


    /**
     * 设置刷新
     */
    private void setupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.swiperefresh_color1, R.color.swiperefresh_color2,
                    R.color.swiperefresh_color3, R.color.swiperefresh_color4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mState == STATE_REFRESH) {
                        return;
                    }

                    reload();
                }
            });
        }
        mState = STATE_NONE;
    }

    /**
     * 设置刷新状态
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            setSwipeRefreshLoadingState();
        } else {
            setSwipeRefreshLoadedState();
        }
    }

    /**
     * 设置顶部正在加载的状态
     */
    private void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);


            mState = STATE_REFRESH;
        }
    }

    /**
     * 设置顶部加载完毕的状态
     */
    private void setSwipeRefreshLoadedState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);

            mState = STATE_NONE;
        }
    }

    /**
     * 刷新底部信息
     */
    private void refreshBottomBar() {
        Double count = 0D;
        Double goodsFee = 0D;
        List<InvSendIoOrderItem> orderItems = goodsListAdapter.getEntityList();
        if (orderItems != null && orderItems.size() > 0) {
            for (InvSendIoOrderItem orderItem : orderItems) {
                count += orderItem.getQuantityCheck();
                goodsFee += orderItem.getAmount();
            }
        }

        tvGoodsQunatity.setText(String.format("商品数：%.2f", count));
        tvTotalAmount.setText(String.format("商品金额：%.2f", goodsFee));
    }

    private void loadGoodsList(InvSendIoOrder invIoOrder) {
        curOrder = invIoOrder;
        if (curOrder == null) {
            tvGoodsQunatity.setText(String.format("商品数：%.2f", 0D));
            tvTotalAmount.setText(String.format("商品金额：%.2f", 0D));

            goodsListAdapter.setEntityList(null);
            return;
        }
        tvGoodsQunatity.setText(String.format("商品数：%.2f", curOrder.getCommitGoodsNum()));
        tvTotalAmount.setText(String.format("商品金额：%.2f", curOrder.getCommitPrice()));

        InvSendIoOrderApiImpl.getInvSendIoOrderById(curOrder.getId(), orderdetailRespCallback);
    }

    NetCallBack.NetTaskCallBack orderdetailRespCallback = new NetCallBack.NetTaskCallBack<InvSendIoOrderItemBrief,
            NetProcessor.Processor<InvSendIoOrderItemBrief>>(
            new NetProcessor.Processor<InvSendIoOrderItemBrief>() {
                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        goodsListAdapter.setEntityList(null);
                        return;
                    }
                    //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<InvSendIoOrderItemBrief> retValue = (RspBean<InvSendIoOrderItemBrief>) rspData;
                    InvSendIoOrderItemBrief orderDetail = retValue.getValue();

                    if (orderDetail != null) {
                        goodsListAdapter.setEntityList(orderDetail.getItems());
                    } else {
                        goodsListAdapter.setEntityList(null);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("加载商品失败：" + errMsg);
                    goodsListAdapter.setEntityList(null);
                }
            }
            , InvSendIoOrderItemBrief.class
            , CashierApp.getAppContext()) {
    };

}

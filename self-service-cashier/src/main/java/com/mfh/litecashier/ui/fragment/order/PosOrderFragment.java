package com.mfh.litecashier.ui.fragment.order;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.cashier.model.PosOrder;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.presenter.OrderflowPresenter;
import com.mfh.litecashier.ui.view.IOrderflowView;
import com.mfh.litecashier.utils.ACacheHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 线下门店订单流水
 * Created by bingshanguxue on 15/8/31.
 */
public class PosOrderFragment extends BaseListFragment<PosOrder>
        implements IOrderflowView {

    public static final String EXTRA_KEY_BTYPE = "btype";
    public static final String EXTRA_KEY_SUBTYPES = "subTypes";
    public static final String EXTRA_KEY_ORDERSTATUS = "orderStatus";
    public static final String EXTRA_KEY_SELLOFFICES = "sellOffices";

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    private LinearLayoutManager linearLayoutManager;
    private PosOrderAdapter orderListAdapter;

    private OrderflowPresenter orderflowPresenter;
    private Integer mBizType;
    private String mSubTypes;
    private String orderStatus;
    private String sellOffices;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inv_sendorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        orderflowPresenter = new OrderflowPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mBizType = args.getInt(EXTRA_KEY_BTYPE);
            mSubTypes = args.getString(EXTRA_KEY_SUBTYPES);
            orderStatus = args.getString(EXTRA_KEY_ORDERSTATUS);
            sellOffices = args.getString(EXTRA_KEY_SELLOFFICES);
        }

        setupSwipeRefresh();
        initOrderRecyclerView();

//        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
//            readCache();
//        } else {
//            reload();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (orderRecyclerView != null) {
            orderRecyclerView.removeOnScrollListener(orderListScrollListener);
        }

        EventBus.getDefault().unregister(this);
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
        orderRecyclerView.addOnScrollListener(orderListScrollListener);

        orderListAdapter = new PosOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new PosOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurPosOrder());
                EventBus.getDefault().post(new OrderEvent(OrderEvent.EVENT_ID_LOAD_POSORDER_ITEMS, args));
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurPosOrder());
                EventBus.getDefault().post(new OrderEvent(OrderEvent.EVENT_ID_LOAD_POSORDER_ITEMS, args));            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
    }

    private RecyclerView.OnScrollListener orderListScrollListener = new RecyclerView.OnScrollListener() {
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
    };

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OrderEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();
        ZLogger.d(String.format("OrderEvent(%d)-%s", eventId, StringUtils.decodeBundle(args)));
        if (eventId == OrderEvent.EVENT_ID_RELOAD_DATA) {
            int bizType = args.getInt("bizType");
            String subType = args.getString(EXTRA_KEY_SUBTYPES);
            if (bizType == mBizType && ObjectsCompact.equals(mSubTypes, subType)){
                reload();
            }
        }
    }

    @Override
    @OnClick(R.id.empty_view)
    public synchronized void reload() {
        super.reload();
        if (bSyncInProgress) {
            ZLogger.d("正在加载线下门店订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        orderflowPresenter.findGoodsOrderList(mBizType, mSubTypes,
                orderStatus, sellOffices, mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线下门店订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载线下门店订单流水。");
            onLoadFinished();
            return;
        }

        // && mPageInfo.getPageNo() <= MAX_PAGE
        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            orderflowPresenter.findGoodsOrderList(mBizType, mSubTypes,
                    orderStatus, sellOffices, mPageInfo);
        } else {
            ZLogger.d("加载线下门店订单流水，已经是最后一页。");
            onLoadFinished();
        }
    }

    @Override
    public void onProcess() {
        onLoadStart();
    }

    @Override
    public void onError(String errorMsg) {
        ZLogger.d("加载线下门店订单流水失败:" + errorMsg);
        onLoadFinished();
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<PosOrder> dataList) {
        try {
            ZLogger.d(String.format("保存线下门店订单流水, 请求%d/%d--%d/%d",
                    pageInfo.getPageNo(), pageInfo.getTotalPage(),
                    (dataList == null ? 0 : dataList.size()), mPageInfo.getPageSize()));

            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
//                    ZLogger.d("缓存线下门店订单流水第一页数据");
                if (orderListAdapter != null) {
                    orderListAdapter.setEntityList(dataList);
                }

                JSONArray cacheArrays = new JSONArray();
                if (dataList != null) {
                    cacheArrays.addAll(dataList);
                }
//                if (orderListAdapter != null) {
//                    orderListAdapter.notifyDataSetChanged();
//                }
                ACacheHelper.put(ACacheHelper.CK_ORDERFLOW_STORE, cacheArrays.toJSONString());
            } else {
                if (orderListAdapter != null) {
                    orderListAdapter.appendEntityList(dataList);
                }
            }

        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载线下门店订单流水失败: %s", ex.toString()));
        }

        ZLogger.d(String.format("更新线下门店订单流水, 请求%d/%d ,已加载(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                orderListAdapter.getItemCount(), mPageInfo.getTotalCount()));
        onLoadFinished();
    }

    /**
     * 加载门店收银缓存数据
     *
     * @return true, 需要重新加载数据;false,不需要重新加载数据
     */
    public synchronized boolean readCache() {
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_ORDERFLOW_STORE);
        List<PosOrder> cacheData = JSONArray.parseArray(cacheStr, PosOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载门店收银缓存数据(%s): %d条订单流水", ACacheHelper.CK_ORDERFLOW_STORE, cacheData.size()));
            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(cacheData);
            }

            return false;
        }
        return true;
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            setSwipeRefreshLoadingState();
        } else {
            setSwipeRefreshLoadedState();
        }
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
     * 设置顶部正在加载的状态
     */
    private void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);

            mState = STATE_REFRESH;
        }

        mProgressBar.setVisibility(View.VISIBLE);
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
        mProgressBar.setVisibility(View.GONE);

    }

}

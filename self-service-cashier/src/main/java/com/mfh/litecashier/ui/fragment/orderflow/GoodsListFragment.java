package com.mfh.litecashier.ui.fragment.orderflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.framework.api.constant.BizType;
import com.mfh.litecashier.bean.PosOrder;
import com.mfh.litecashier.event.GoodsListEvent;
import com.mfh.litecashier.event.OnlineOrderFlowEvent;
import com.mfh.litecashier.presenter.OrderflowPresenter;
import com.mfh.litecashier.ui.adapter.StockOrderflowOrderAdapter;
import com.mfh.litecashier.ui.view.IOrderflowView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 线上销售－－待配送/已配送
 * Created by kun on 15/8/31.
 */
public class GoodsListFragment extends BaseListFragment<PosOrder> implements IOrderflowView{
    public static final String EXTRA_KEY_STATUS = "status";
    public static final String EXTRA_KEY_CACHE_KEY = "cacheKey";
    public static final String EXTRA_KEY_ID = "id";

    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private StockOrderflowOrderAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    private boolean isLoadingMore;

    private OrderflowPresenter orderflowPresenter;

    private String status;
    private String cacheKey;

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
            status = args.getString(EXTRA_KEY_STATUS, "");
            cacheKey = args.getString(EXTRA_KEY_CACHE_KEY, "");
        }

        setupSwipeRefresh();
        initOrderRecyclerView();

//            reload();
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

        orderListAdapter = new StockOrderflowOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new StockOrderflowOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurPosOrder());
                EventBus.getDefault().post(new OnlineOrderFlowEvent(OnlineOrderFlowEvent.EVENT_ID_RELAOD_ITEM_DATA, args));
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurPosOrder());
                EventBus.getDefault().post(new OnlineOrderFlowEvent(OnlineOrderFlowEvent.EVENT_ID_RELAOD_ITEM_DATA, args));
            }
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
    public void onEventMainThread(GoodsListEvent event) {
        ZLogger.d(String.format("PlatformProviderFragment: GoodsListEvent(%d)", event.getEventId()));
        if (event.getEventId() == GoodsListEvent.EVENT_ID_RELOAD_DATA) {
            Bundle args = event.getArgs();
            if (args != null){
                if (status.equals(args.getString(EXTRA_KEY_STATUS, ""))){
                    reload();
                }
            }
        }
    }

    /**
     * 重新加载数据
     */
    @Override
    @OnClick(R.id.empty_view)
    public synchronized void reload() {

//        if (!readCache()){
////            reload();
//            CommoditySyncService.get().sync(CommoditySyncService.SYNC_STEP_DOWNLOAD_SUPPLIERS);
//        }
//
//            onLoadFinished();

        if (bSyncInProgress) {
            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
//        if (entityList == null) {
//            entityList = new ArrayList<>();
//        } else {
//            entityList.clear();
//        }

        orderflowPresenter.loadOrders(BizType.SC, status,
                MfhLoginService.get().getCurOfficeId(), mPageInfo);
        mPageInfo.setPageNo(1);
    }

    @Override
    public void onProcess() {
        onLoadStart();
    }

    @Override
    public void onError(String errorMsg) {
        ZLogger.d("加载线上销售订单流水失败:" + errorMsg);
        onLoadFinished();
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<PosOrder> dataList) {
        try {
            ZLogger.d(String.format("保存线上销售订单流水, 请求%d/%d--%d/%d",
                    pageInfo.getPageNo(), pageInfo.getTotalPage(),
                    (dataList == null ? 0 : dataList.size()), mPageInfo.getPageSize()));
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                if (entityList == null) {
                    entityList = new ArrayList<>();
                }
                else{
                    entityList.clear();
                }
//                    ZLogger.d("缓存线下门店订单流水第一页数据");
                JSONArray cacheArrays = new JSONArray();
                cacheArrays.addAll(dataList);
                ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
            } else {
                if (entityList == null) {
                    entityList = new ArrayList<>();
                }
            }

            if (dataList != null){
                entityList.addAll(dataList);
            }
            //下次进入不自动更新
             SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STORE_ORDERFLOW_ENABLED, false);

        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载线上销售订单流水失败: %s", ex.toString()));
        }

        ZLogger.d(String.format("更新线上销售订单流水, 请求%d/%d ,已加载(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                (entityList == null ? 0 : entityList.size()), mPageInfo.getTotalCount()));
        if (orderListAdapter != null) {
            orderListAdapter.setEntityList(entityList);
        }
        onLoadFinished();
    }

    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(cacheKey);
        List<PosOrder> cacheData = JSONArray.parseArray(cacheStr, PosOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d条线上销售订单", cacheKey, cacheData.size()));
//            refreshCategoryGoodsTab(entity.getCategoryId(), cacheData);
            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(cacheData);
            }
            return true;
        }
        return false;
    }

    /**
     * 翻页加载更多数据
     */
    @Override
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载线上订单订单流水。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            orderflowPresenter.loadOrders(BizType.SC, status,
                    MfhLoginService.get().getCurOfficeId(), mPageInfo);
        } else {
            ZLogger.d("加载线上订单订单流水，已经是最后一页。");
            onLoadFinished();
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
     * 设置刷新状态
     */
    @Override
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

}

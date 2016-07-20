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
import com.manfenjiayuan.business.bean.InvSendOrder;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.InvSendOrderEvent;
import com.mfh.litecashier.event.PurchaseSendEvent;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.mfh.litecashier.ui.adapter.PurchaseSendOrderAdapter;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 采购订单－待收货/部分收货/已收货
 * Created by kun on 15/8/31.
 */
public class InvSendOrderFragment extends BaseListFragment<InvSendOrder>
        implements IInvSendOrderView {
    public static final String EXTRA_KEY_STATUS = "status";
    public static final String EXTRA_KEY_CACHEKEY = "cacheKey";
    public static final String EXTRA_KEY_ID = "id";

    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private PurchaseSendOrderAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    private String status;
    private String cacheKey;
    private InvSendOrderPresenter invSendOrderPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inv_sendorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        invSendOrderPresenter = new InvSendOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            status = args.getString(EXTRA_KEY_STATUS, "");
            cacheKey = args.getString(EXTRA_KEY_CACHEKEY, "");
        }

        setupSwipeRefresh();
        initOrderRecyclerView();

//            reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        orderListAdapter = new PurchaseSendOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new PurchaseSendOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurOrder());
                EventBus.getDefault().post(new PurchaseSendEvent(PurchaseSendEvent.EVENT_ID_RELAOD_ITEM_DATA, args));
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurOrder());
                EventBus.getDefault().post(new PurchaseSendEvent(PurchaseSendEvent.EVENT_ID_RELAOD_ITEM_DATA, args));
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(InvSendOrderEvent event) {
        ZLogger.d(String.format("InvSendOrderFragment: InvSendOrderEvent(%d)", event.getEventId()));
        if (event.getEventId() == InvSendOrderEvent.EVENT_ID_RELOAD_DATA) {
            Bundle args = event.getArgs();
            if (args != null) {
                if (status.equals(args.getString(EXTRA_KEY_STATUS, ""))) {
                    reload();
                }
            }
        } else if (event.getEventId() == InvSendOrderEvent.EVENT_ID_REMOVE_ITEM) {
            Bundle args = event.getArgs();
            if (args != null) {
                orderListAdapter.remove(args.getLong(EXTRA_KEY_ID, 0L));
            }
        }
    }

    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public synchronized void reload() {
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

        invSendOrderPresenter.loadOrders(mPageInfo, true,
                MfhLoginService.get().getCurOfficeId(), "", status);
        mPageInfo.setPageNo(1);
    }

    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(cacheKey);
        List<InvSendOrder> cacheData = JSONArray.parseArray(cacheStr, InvSendOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d条采购订单", cacheKey, cacheData.size()));
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

            invSendOrderPresenter.loadOrders(mPageInfo, true,
                    MfhLoginService.get().getCurOfficeId(), "", status);
        } else {
            ZLogger.d("加载采购订单，已经是最后一页。");
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

    @Override
    public void onQueryInvSendOrderProcess() {

        onLoadStart();
    }

    @Override
    public void onQueryInvSendOrderError(String errorMsg) {


        onLoadFinished();
    }

    @Override
    public void onQueryInvSendOrderSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                ZLogger.d("缓存商品采购订单第一页数据");
                JSONArray cacheArrays = new JSONArray();
                if (dataList != null) {
                    cacheArrays.addAll(dataList);
                }
                if (orderListAdapter != null) {
                    orderListAdapter.setEntityList(dataList);
                }
                ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PURCHASESEND_ORDER_ENABLED, false);
            } else {
                if (orderListAdapter != null) {
                    orderListAdapter.appendEntityList(dataList);
                }
            }

            ZLogger.d(String.format("加载商品采购订单结束,pageInfo':page=%d/%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                    orderListAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载商品采购订单失败: %s", ex.toString()));

            onLoadFinished();
        }
    }

    @Override
    public void onQueryInvSendOrderItemsSuccess(List<InvSendOrderItem> dataList) {

    }
}

package com.mfh.litecashier.ui.fragment.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.manfenjiayuan.business.bean.InvLossOrder;
import com.mfh.litecashier.bean.InvLossOrderItem;
import com.mfh.litecashier.event.StockLossEvent;
import com.mfh.litecashier.ui.adapter.StockLossGoodsAdapter;
import com.mfh.litecashier.ui.adapter.StockLossOrderAdapter;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 库存－－库存报损
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryLossFragment extends BaseFragment {

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private StockLossGoodsAdapter goodsListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.empty_view)
    TextView goodsEmptyView;
    @Bind(R.id.frame_bottom)
    LinearLayout frameBottom;
    @Bind(R.id.tv_quantity)
    TextView tvQuntity;
    @Bind(R.id.tv_amount)
    TextView tvAmount;

    private static final int STATE_NONE = 0;
    private static final int STATE_REFRESH = 1;
    private static final int STATE_LOADMORE = 2;
    private static final int STATE_NOMORE = 3;
    private static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    private static int mState = STATE_NONE;
    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @Bind(R.id.order_empty_view)
    TextView orderEmptyView;
    private StockLossOrderAdapter orderListAdapter;
    @Bind(R.id.button_create)
    Button btnCreate;


    private List<InvLossOrder> orderList = new ArrayList<>();
    private InvLossOrder curOrder;

    private boolean isLoadingMore;

    private boolean bSyncInProgress = false;//是否正在同步
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<InvLossOrderItem> goodsList = new ArrayList<>();


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stock_loss;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        setupSwipeRefresh();
        initOrderRecyclerView();
        initGoodsRecyclerView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }


    /**
     * 新建报损
     */
    @OnClick(R.id.button_create)
    public void createNewInvLossOrder() {
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_PURCHASE_RETURN_ORDER);
//
//        Intent intent = new Intent(getActivity(), ServiceActivity.class);
//        intent.putExtras(extras);
//        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_CREATE_STOCK_BATCH);

        DialogUtil.showHint("@开发君@ 失踪了...");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_CREATE_STOCK_BATCH: {
                //刷新订单列表
//                goodsListAdapter.notifyDataSetChanged();
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        orderRecyclerView.setEmptyView(orderEmptyView);
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
//                        reloadInvLossOrder();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        orderListAdapter = new StockLossOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new StockLossOrderAdapter.OnAdapterListener() {
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
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(goodsEmptyView);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (dy > 0) {
//                    fabShopcart.setVisibility(View.VISIBLE);
                    if ((lastVisibleItem >= totalItemCount - 4) && !isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
//                    fabShopcart.setVisibility(View.GONE);
                }
            }
        });

        goodsListAdapter = new StockLossGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new StockLossGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onDataSetChanged() {
                                                      onLoadFinished();
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(StockLossEvent event) {
        ZLogger.d(String.format("InventoryLossFragment: StockLossEvent(%d)", event.getEventId()));
        if (event.getEventId() == StockLossEvent.EVENT_ID_RELOAD_DATA) {
            if (SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_STOCKLOSS_ORDER_ENABLED, true)
                    || !readInvLossOrderCache()) {
                reloadInvLossOrder();
            }
        }
    }

    /**
     * 读取报损订单缓存
     * 如果没有缓存则重新加载订单列表
     * 如果有缓存则根据同步状态{@link com.mfh.litecashier.utils.SharedPreferencesHelper.PK_SYNC_STOCKLOSS_ORDER_ENABLED}决定是否需要重新加载订单列表。同步状态
     */
    public synchronized boolean readInvLossOrderCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_STOCK_LOSS_ORDER);
        List<InvLossOrder> cacheData = JSONArray.parseArray(cacheStr, InvLossOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载报损订单缓存数据(%s): %d条报损订单", ACacheHelper.CK_STOCK_LOSS_ORDER, cacheData.size()));
//            refreshCategoryGoodsTab(entity.getCategoryId(), cacheData);
            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(cacheData);
            }

            return true;
        }
        return false;
    }

    /**
     * 加载报损订单列表
     */
    @OnClick(R.id.order_empty_view)
    public synchronized void reloadInvLossOrder() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载报损列表。");
            onLoadFinished();
            return;
        }

//        if (bSyncInProgress) {
//            ZLogger.d("正在加载报损列表。");
//            onLoadFinished();
//            return;
//        }

        onLoadStart();

        if (orderList == null) {
            orderList = new ArrayList<>();
        } else {
            orderList.clear();
        }
        InvOrderApiImpl.queryInvLossOrderList(queryOrderListCallback);
    }

    private NetCallBack.QueryRsCallBack queryOrderListCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvLossOrder>(new PageInfo(-1, 30)) {
        @Override
        public void processQueryResult(RspQueryResult<InvLossOrder> rs) {
            //此处在主线程中执行。
            new OrderQueryAsyncTask(pageInfo).execute(rs);
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
            ZLogger.e("加载报损列表失败:" + errMsg);

            onLoadFinished();
        }
    }, InvLossOrder.class, CashierApp.getAppContext());

    public class OrderQueryAsyncTask extends AsyncTask<RspQueryResult<InvLossOrder>, Integer, Long> {
        private PageInfo pageInfo;

        public OrderQueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<InvLossOrder>... params) {
            saveQueryResult(params[0], pageInfo);
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
//            ZLogger.d(String.format("pageInfo':page=%d,rows=%d(%d)", pageInfo.getPageNo(), pageInfo.getPageSize(), (goodsList == null ? 0 : goodsList.size())));

            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(orderList);
            }
            onLoadFinished();
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<InvLossOrder> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
//                mPageInfo = pageInfo;

                if (rs == null) {
                    return;
                }

                //第一页，缓存数据
                if (mPageInfo.getPageNo() == 1) {
                    ZLogger.d("缓存报损订单第一页数据");
                    JSONArray cacheArrays = new JSONArray();
                    if (orderList == null) {
                        orderList = new ArrayList<>();
                    } else {
                        orderList.clear();
                    }
                    for (EntityWrapper<InvLossOrder> wrapper : rs.getRowDatas()) {

                        InvLossOrder invLossOrder = wrapper.getBean();
                        Map<String, String> captioin = wrapper.getCaption();
                        if (invLossOrder != null && captioin != null){
                            invLossOrder.setStatusCaption(captioin.get("status"));
                        }

                        cacheArrays.add(invLossOrder);
                        orderList.add(invLossOrder);
                    }
                    ACacheHelper.put(ACacheHelper.CK_STOCK_LOSS_ORDER, cacheArrays.toJSONString());
                    SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STOCKLOSS_ORDER_ENABLED, false);
                } else {
                    if (orderList == null) {
                        orderList = new ArrayList<>();
                    }
                    for (EntityWrapper<InvLossOrder> wrapper : rs.getRowDatas()) {InvLossOrder invLossOrder = wrapper.getBean();
                        Map<String, String> captioin = wrapper.getCaption();
                        if (invLossOrder != null && captioin != null){
                            invLossOrder.setStatusCaption(captioin.get("status"));
                        }
                        orderList.add(invLossOrder);
                    }
                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("保存报损订单列表失败: %s", ex.toString()));
            }
        }
    }


    /**
     * 开始加载
     */
    private void onLoadStart() {
        isLoadingMore = true;
        bSyncInProgress = true;
        setRefreshing(true);
    }

    /**
     * 加载完成
     */
    private void onLoadFinished() {
        bSyncInProgress = false;
        isLoadingMore = false;
        setRefreshing(false);
    }

    /**
     * 加载订单明细
     */
    private void loadGoodsList(InvLossOrder order) {
        curOrder = order;
        if (order == null) {
            frameBottom.setVisibility(View.VISIBLE);
            tvQuntity.setText(String.format("数量：%.2f", 0D));
            tvAmount.setText(String.format("金额：%.2f", 0D));

            goodsListAdapter.setEntityList(null);
            return;
        }

        //正在盘点
        if (order.getStatus().equals(0)){
            frameBottom.setVisibility(View.INVISIBLE);
        }
        else{
            frameBottom.setVisibility(View.VISIBLE);
            tvQuntity.setText(String.format("数量：%.2f", curOrder.getCommitGoodsNum()));
            tvAmount.setText(String.format("金额：%.2f", curOrder.getCommitPrice()));
        }

        onLoadStart();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存报损列表。");
            onLoadFinished();
            return;
        }

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        //从第一页开始请求，每页最多50条记录
        load(mPageInfo);
        mPageInfo.setPageNo(1);
    }


    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载库存报损。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存报损。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载库存报损，已经是最后一页。");
            onLoadFinished();
        }
    }

    private void load(PageInfo pageInfo) {
        AjaxParams params = new AjaxParams();
        if (curOrder != null) {
            params.put("orderId", String.valueOf(curOrder.getId()));
        }
        params.put("wrapper","true");
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvLossOrderItem>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvLossOrderItem> rs) {
                //此处在主线程中执行。
                new OrderDetailQueryAsyncTask(pageInfo).execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载报损订单明细失败:" + errMsg);
                onLoadFinished();
            }
        }, InvLossOrderItem.class, CashierApp.getAppContext());

        AfinalFactory.postDefault(InvOrderApiImpl.URL_INVLOSSORDERITEM_LIST, params, queryRsCallBack);
    }

    public class OrderDetailQueryAsyncTask extends AsyncTask<RspQueryResult<InvLossOrderItem>, Integer, Long> {
        private PageInfo pageInfo;

        public OrderDetailQueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<InvLossOrderItem>... params) {
            saveQueryResult(params[0], pageInfo);
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(goodsList);
            }
            onLoadFinished();
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<InvLossOrderItem> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
                mPageInfo = pageInfo;
                if (mPageInfo.getPageNo() == 1) {
                    if (goodsList == null) {
                        goodsList = new ArrayList<>();
                    } else {
                        goodsList.clear();
                    }
                }
                else{
                    if (goodsList == null) {
                        goodsList = new ArrayList<>();
                    }
                }

                if (rs == null) {
                    return;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                ZLogger.d(String.format("保存 %d 条报损订单明细", retSize));
                for (EntityWrapper<InvLossOrderItem> wrapper : rs.getRowDatas()){
                    goodsList.add(wrapper.getBean());
                }

            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("保存报损订单明细: %s", ex.toString()));
            }
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

                    reloadInvLossOrder();
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
}

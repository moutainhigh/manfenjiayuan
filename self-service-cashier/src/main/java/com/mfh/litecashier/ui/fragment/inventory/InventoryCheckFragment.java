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
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invCheckOrder.InvCheckOrder;
import com.mfh.framework.api.invCheckOrder.InvCheckOrderApi;
import com.mfh.framework.api.invCheckOrder.InvCheckOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
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
import com.mfh.litecashier.bean.InvCheckOrderItem;
import com.mfh.litecashier.bean.ReceivableOrderDetail;
import com.mfh.litecashier.event.StockCheckEvent;
import com.mfh.litecashier.ui.adapter.StockCheckGoodsAdapter;
import com.mfh.litecashier.ui.adapter.StockCheckOrderAdapter;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 库存－－库存盘点
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryCheckFragment extends BaseFragment {
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private StockCheckGoodsAdapter goodsListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_view)
    TextView goodsEmptyView;
    @BindView(R.id.frame_bottom)
    LinearLayout frameBottom;
    @BindView(R.id.tv_check_quantity)
    TextView tvCheckQuntity;
    @BindView(R.id.tv_system_inventory)
    TextView tvSystemInventory;
    @BindView(R.id.tv_loss_quantity)
    TextView tvLossQuantity;
    @BindView(R.id.tv_loss_amount)
    TextView tvLossAmount;
    @BindView(R.id.button_submit)
    Button btnSubmit;

    public static final int STATE_NONE = 0;
    public static final int STATE_REFRESH = 1;
    public static final int STATE_LOADMORE = 2;
    public static final int STATE_NOMORE = 3;
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    public static int mState = STATE_NONE;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @BindView(R.id.order_empty_view)
    TextView orderEmptyView;
    private StockCheckOrderAdapter orderListAdapter;
    @BindView(R.id.button_create)
    Button btnCreate;

    private List<InvCheckOrder> orderList = new ArrayList<>();
    private InvCheckOrder curOrder;

    private boolean isLoadingMore;
    private boolean bSyncInProgress = false;//是否正在同步
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<InvCheckOrderItem> goodsList = new ArrayList<>();


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stock_check;
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

    @OnClick(R.id.button_submit)
    public void finishOrder() {
        final InvCheckOrder invCheckOrder = orderListAdapter.getCurOrder();
        if (invCheckOrder == null) {
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        DialogUtil.showHint("@开发君@ 失踪了...");

        //回调
//        NetCallBack.NetTaskCallBack batchResponseCallback = new NetCallBack.NetTaskCallBack<String,
//                NetProcessor.Processor<String>>(
//                new NetProcessor.Processor<String>() {
//                    @Override
//                    public void processResult(IResponseData rspData) {
//                        //TODO
//                        if (orderList != null){
//                            orderList.remove(invCheckOrder);
//                        }
//                    }
//
//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        ZLogger.d("盘点确认失败：" + errMsg);
//                        DialogUtil.showHint("盘点确认失败");
//                    }
//                }
//                , String.class
//                , CashierApp.getAppContext()) {
//        };
//
//        CashierApiImpl.finishInvCheckOrder(invCheckOrder.getId(), batchResponseCallback);
    }

    /**
     * 新建盘点
     */
    @OnClick(R.id.button_create)
    public void createNewInvCheckOrder() {

        DialogUtil.showHint("@开发君@ 失踪了...");
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_PURCHASE_RETURN_ORDER);
//
////        ServiceActivity.actionStart(getActivity(), extras);
//
//        Intent intent = new Intent(getActivity(), ServiceActivity.class);
//        intent.putExtras(extras);
//        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_CREATE_STOCK_BATCH);
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
                        reloadInvCheckOrder();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        orderListAdapter = new StockCheckOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new StockCheckOrderAdapter.OnAdapterListener() {
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

        goodsListAdapter = new StockCheckGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new StockCheckGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onDataSetChanged() {
                                                      isLoadingMore = false;
                                                  }

                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(StockCheckEvent event) {
        ZLogger.d(String.format("InventoryCheckFragment: StockCheckEvent(%d)", event.getEventId()));
        if (event.getEventId() == StockCheckEvent.EVENT_ID_RELOAD_DATA) {
            if (SharedPreferencesUltimate.getBoolean(SharedPreferencesUltimate.PK_SYNC_STOCKCHECK_ORDER_ENABLED, true) || !readInvCheckOrderCache()) {
                reloadInvCheckOrder();
            }
        }
    }


    /**
     * 读取盘点订单缓存
     * 如果没有缓存则重新加载订单列表
     * 如果有缓存则根据同步状态{@link SharedPreferencesUltimate.PK_SYNC_STOCKLOSS_ORDER_ENABLED}决定是否需要重新加载订单列表。同步状态
     */
    public synchronized boolean readInvCheckOrderCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_STOCK_CHECK_ORDER);
        List<InvCheckOrder> cacheData = JSONArray.parseArray(cacheStr, InvCheckOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载盘点订单缓存数据(%s): %d条盘点订单", ACacheHelper.CK_STOCK_CHECK_ORDER, cacheData.size()));
//            refreshCategoryGoodsTab(entity.getCategoryId(), cacheData);
            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(cacheData);
            }

            return true;
        }
        return false;
    }

    /**
     * 加载盘点订单列表列表
     */
    @OnClick(R.id.order_empty_view)
    public synchronized void reloadInvCheckOrder() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载盘点订单列表。");
            onLoadFinished();
            return;
        }

        onLoadStart();

        if (orderList == null) {
            orderList = new ArrayList<>();
        } else {
            orderList.clear();
        }
        InvCheckOrderApiImpl.list(new PageInfo(1, MAX_SYNC_PAGESIZE), queryOrderListCallback);
    }

    private NetCallBack.QueryRsCallBack queryOrderListCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvCheckOrder>(new PageInfo(-1, 30)) {
        @Override
        public void processQueryResult(RspQueryResult<InvCheckOrder> rs) {
            //此处在主线程中执行。
            new OrderQueryAsyncTask(pageInfo).execute(rs);
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
            ZLogger.e("加载盘点订单列表失败:" + errMsg);

            onLoadFinished();
        }
    }, InvCheckOrder.class, CashierApp.getAppContext());

    public class OrderQueryAsyncTask extends AsyncTask<RspQueryResult<InvCheckOrder>, Integer, Long> {
        private PageInfo pageInfo;

        public OrderQueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<InvCheckOrder>... params) {
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
        private void saveQueryResult(RspQueryResult<InvCheckOrder> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
//                mPageInfo = pageInfo;

                if (rs == null) {
                    return;
                }

                //第一页，缓存数据
                if (mPageInfo.getPageNo() == 1) {
                    ZLogger.d("缓存盘点订单列表第一页数据");
                    JSONArray cacheArrays = new JSONArray();
                    if (orderList == null) {
                        orderList = new ArrayList<>();
                    } else {
                        orderList.clear();
                    }
                    for (EntityWrapper<InvCheckOrder> wrapper : rs.getRowDatas()) {
                        InvCheckOrder invCheckOrder = wrapper.getBean();
                        Map<String, String> caption = wrapper.getCaption();
                        if (invCheckOrder != null && caption != null) {
                            invCheckOrder.setStatusCaption(caption.get("status"));
                        }

                        cacheArrays.add(invCheckOrder);
                        orderList.add(invCheckOrder);
                    }
                    ACacheHelper
                            .put(ACacheHelper.CK_STOCK_CHECK_ORDER, cacheArrays.toJSONString());
                    SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_SYNC_STOCKCHECK_ORDER_ENABLED, false);
                } else {
                    if (orderList == null) {
                        orderList = new ArrayList<>();
                    }
                    for (EntityWrapper<InvCheckOrder> wrapper : rs.getRowDatas()) {
                        InvCheckOrder invCheckOrder = wrapper.getBean();
                        Map<String, String> caption = wrapper.getCaption();
                        if (invCheckOrder != null && caption != null) {
                            invCheckOrder.setStatusCaption(caption.get("status"));
                        }

                        orderList.add(invCheckOrder);
                    }
                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("保存盘点订单列表列表失败: %s", ex.toString()));
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
    private void loadGoodsList(InvCheckOrder order) {
        curOrder = order;
        if (order == null) {
            tvCheckQuntity.setText(String.format("盘点数：%.2f", 0D));
            tvSystemInventory.setText(String.format("系统库存：%.2f", 0D));
            tvLossQuantity.setText(String.format("盈亏数：%.2f", 0D));
            tvLossAmount.setText(String.format("盈亏金额：%.2f", 0D));
            btnSubmit.setVisibility(View.GONE);

            goodsListAdapter.setEntityList(null);
            return;
        }

        tvCheckQuntity.setText(String.format("盘点数：%.2f", curOrder.getCommitNum()));
        tvSystemInventory.setText(String.format("系统库存：%.2f", curOrder.getInvGoodsNum()));
        tvLossQuantity.setText(String.format("盈亏数：%.2f", curOrder.getLossNum()));
        tvLossAmount.setText(String.format("盈亏金额：%.2f", curOrder.getCommitPrice() - curOrder.getInvPrice()));
        if (curOrder.getStatus().equals(InvCheckOrder.INVCHECK_ORDERSTATUS_PROCESSING)) {
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            btnSubmit.setVisibility(View.GONE);
        }

        onLoadStart();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存盘点列表。");
            onLoadFinished();
            return;
        }

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        if (goodsList == null) {
            goodsList = new ArrayList<>();
        } else {
            goodsList.clear();
        }

        frameBottom.setVisibility(View.VISIBLE);

        //从第一页开始请求，每页最多50条记录
        load(mPageInfo);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载盘点订单明细。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载盘点订单明细。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载盘点订单明细，已经是最后一页。");
            onLoadFinished();
        }
    }


    private void load(PageInfo pageInfo) {
        AjaxParams params = new AjaxParams();

        if (curOrder != null) {
            params.put("orderId", String.valueOf(curOrder.getId()));
        }
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvCheckOrderItem>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvCheckOrderItem> rs) {
                //此处在主线程中执行。
                new ProductQueryAsyncTask(pageInfo).execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载盘点订单明细失败:" + errMsg);
                onLoadFinished();
            }
        }, InvCheckOrderItem.class, CashierApp.getAppContext());

        AfinalFactory.postDefault(InvCheckOrderApi.URL_INVCHECKORDERITEM_LIST, params, queryRsCallBack);
    }

    public class ProductQueryAsyncTask extends AsyncTask<RspQueryResult<InvCheckOrderItem>, Integer, Long> {
        private PageInfo pageInfo;

        public ProductQueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<InvCheckOrderItem>... params) {
            try {
                mPageInfo = pageInfo;
                ZLogger.d(String.format("保存盘点订单明细2,pageInfo':page=%d,rows=%d(%d/%d)",
                        mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                        (goodsList == null ? 0 : goodsList.size()), mPageInfo.getTotalCount()));
                RspQueryResult<InvCheckOrderItem> rs = params[0];
                if (rs != null) {
                    int retSize = rs.getReturnNum();
                    ZLogger.d(String.format("加载 %d 商品", retSize));
                    for (EntityWrapper<InvCheckOrderItem> wrapper : rs.getRowDatas()) {
                        goodsList.add(wrapper.getBean());
                    }
                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("加载盘点订单明细失败: %s", ex.toString()));
            }
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
//            ZLogger.d(String.format("pageInfo':page=%d,rows=%d(%d)", pageInfo.getPageNo(), pageInfo.getPageSize(), (goodsList == null ? 0 : goodsList.size())));

            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(goodsList);
            }
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
//                        return;
                    }

//                    reload();
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

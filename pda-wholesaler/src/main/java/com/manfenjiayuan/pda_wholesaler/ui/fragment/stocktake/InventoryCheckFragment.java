package com.manfenjiayuan.pda_wholesaler.ui.fragment.stocktake;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.InvCheckOrder;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SimpleActivity;
import com.manfenjiayuan.pda_wholesaler.ui.adapter.StockCheckOrderAdapter;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import net.tsz.afinal.core.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 库存－－库存盘点
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryCheckFragment extends BaseFragment {

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
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.order_empty_view)
    TextView orderEmptyView;
    private StockCheckOrderAdapter orderListAdapter;

    private boolean isLoadingMore;

    private boolean bSyncInProgress = false;//是否正在同步
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 15;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<InvCheckOrder> orderList = new ArrayList<>();

    public static InventoryCheckFragment newInstance(Bundle args) {
        InventoryCheckFragment fragment = new InventoryCheckFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inventory_check;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        setupSwipeRefresh();
        initOrderRecyclerView();

        reloadInvCheckOrder();
    }

    /**
     * 初始化订单列表
     */
    private void initOrderRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
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
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        orderListAdapter = new StockCheckOrderAdapter(AppContext.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new StockCheckOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                loadGoodsList(orderListAdapter.getCurOrder());
                //TODO
                InvCheckOrder invCheckOrder = orderListAdapter.getCurOrder();
                if (invCheckOrder == null){
                    DialogUtil.showHint("无法盘点");
                    return;
                }

                if (invCheckOrder.getStatus().equals(InvCheckOrder.INVCHECK_ORDERSTATUS_PROCESSING)){
                    //TODO,切换到盘点页面
                    Bundle extras = new Bundle();
//                        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                    extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FRAGMENT_TYPE_STOCK_TAKE);
                    extras.putLong(StockTakeFragment.EXTRA_KEY_ORDER_ID, invCheckOrder.getId());
                    UIHelper.startActivity(getActivity(), SimpleActivity.class, extras);
                }
//                else if (invCheckOrder.getStatus().equals(InvCheckOrder.INVCHECK_ORDERSTATUS_FREEZE)){
//                    DialogUtil.showHint("盘点已冻结");
//                    InvCheckGoodsService.get().deleteBy(String.format("orderId = '%d'", invCheckOrder.getId()));
//                }
                else if (invCheckOrder.getStatus().equals(InvCheckOrder.INVCHECK_ORDERSTATUS_FINISHED)){
                    DialogUtil.showHint("盘点已结束");

                    //删除无效的盘点数据
                    StockTakeService.get().deleteBy(String.format("orderId = '%d'", invCheckOrder.getId()));
                } else if (invCheckOrder.getStatus().equals(InvCheckOrder.INVCHECK_ORDERSTATUS_CANCELED)) {
                    DialogUtil.showHint("盘点已取消");
                    StockTakeService.get().deleteBy(String.format("orderId = '%d'", invCheckOrder.getId()));
                } else {
                    DialogUtil.showHint("无法盘点");
                    StockTakeService.get().deleteBy(String.format("orderId = '%d'", invCheckOrder.getId()));
                }
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
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
     * 加载盘点订单列表列表
     * */
    @OnClick({R.id.order_empty_view, R.id.button_sync})
    public synchronized void reloadInvCheckOrder() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载盘点订单列表。");
            DialogUtil.showHint(R.string.toast_network_error);
            onLoadFinished();
            return;
        }

        onLoadStart();

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        if (orderList == null) {
            orderList = new ArrayList<>();
        } else {
            orderList.clear();
        }

        //从第一页开始请求，每页最多50条记录
        load(mPageInfo);
        mPageInfo.setPageNo(1);
    }


    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载盘点订单明细。");
            onLoadFinished();
            return;
        }

        if (bSyncInProgress) {
            ZLogger.d("正在加载盘点订单明细。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载类目商品，已经是最后一页。");
            onLoadFinished();
        }
    }

    private void load(PageInfo pageInfo) {
        NetCallBack.QueryRsCallBack queryOrderListCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvCheckOrder>(pageInfo) {
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
        }, InvCheckOrder.class, AppContext.getAppContext());

        InvOrderApiImpl.queryInvCheckOrderList(pageInfo, queryOrderListCallback);
    }


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
                mPageInfo = pageInfo;

                if (rs == null) {
                    return;
                }

                //第一页，缓存数据
                if (mPageInfo.getPageNo() == 1) {
                    if (orderList == null) {
                        orderList = new ArrayList<>();
                    } else {
                        orderList.clear();
                    }
                }

                for (EntityWrapper<InvCheckOrder> wrapper : rs.getRowDatas()) {
                    InvCheckOrder invCheckOrder = wrapper.getBean();
                    Map<String, String> caption = wrapper.getCaption();
                    if (caption != null){
                        invCheckOrder.setStatusCaption(caption.get("status"));
                        invCheckOrder.setStoreTypeCaption(caption.get("storeType"));
                        invCheckOrder.setNetCaption(caption.get("netId"));
                    }
                    orderList.add(wrapper.getBean());
                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("保存盘点订单列表列表失败: %s", ex.toString()));
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

                    reloadInvCheckOrder();
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

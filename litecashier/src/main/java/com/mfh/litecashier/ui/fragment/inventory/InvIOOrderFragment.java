package com.mfh.litecashier.ui.fragment.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetFactory;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.InvIoOrder;
import com.mfh.litecashier.event.InvIOOrderEvent;
import com.mfh.litecashier.event.StockBatchEvent;
import com.mfh.litecashier.ui.adapter.InvIOOrderAdapter;
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
 * 库存批次－所有/出库/入库
 * Created by kun on 15/8/31.
 */
public class InvIOOrderFragment extends BaseFragment {
    public static final String EXTRA_KEY_ORDER_TYPE = "orderType";
    public static final String EXTRA_KEY_CACHE_KEY = "cacheKey";
    public static final String EXTRA_KEY_ID = "id";

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
    private InvIOOrderAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    private boolean isLoadingMore;

    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 20;
    private boolean bSyncInProgress = false;//是否正在同步
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
//    private List<InvIoOrder> orderList = new ArrayList<>();

    private String orderType;
    private String cacheKey;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inv_sendorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            orderType = args.getString(EXTRA_KEY_ORDER_TYPE, "");
            cacheKey = args.getString(EXTRA_KEY_CACHE_KEY, "");
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

        orderListAdapter = new InvIOOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new InvIOOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurOrder());
                EventBus.getDefault().post(new StockBatchEvent(StockBatchEvent.EVENT_ID_RELAOD_ITEM_DATA, args));
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                Bundle args = new Bundle();
                args.putSerializable("order", orderListAdapter.getCurOrder());
                EventBus.getDefault().post(new StockBatchEvent(StockBatchEvent.EVENT_ID_RELAOD_ITEM_DATA, args));
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(InvIOOrderEvent event) {
        ZLogger.d(String.format("InventoryIOFragment: InvIOOrderEvent(%d)", event.getEventId()));
        if (event.getEventId() == InvIOOrderEvent.EVENT_ID_RELOAD_DATA) {
            Bundle args = event.getArgs();
            if (args != null) {
                if (orderType.equals(args.getString(EXTRA_KEY_ORDER_TYPE, ""))) {
                    boolean isNeedReload = true;
                    if (orderType.equals(String.valueOf(InvIoOrder.ORDER_TYPE_IN))) {
                        isNeedReload = SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_INVIOORDER_IN_ENABLED, true);
                    } else if (orderType.equals(String.valueOf(InvIoOrder.ORDER_TYPE_OUT))) {
                        isNeedReload = SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_INVIOORDER_OUT_ENABLED, true);
                    }

                    if (!isNeedReload) {
                        readCache();
                    } else {
                        reload();
                    }
                }
            }
        } else if (event.getEventId() == InvIOOrderEvent.EVENT_ID_REMOVE_ITEM) {
            Bundle args = event.getArgs();
            if (args != null) {
                orderListAdapter.remove(args.getLong(EXTRA_KEY_ID, 0L));
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
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public synchronized void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载批次流水。");
//            onLoadFinished();
            return;
        }

        onLoadStart();

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        load(mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * */
    private void load(PageInfo pageInfo) {
        AjaxParams params = new AjaxParams();
        params.put("orderType", orderType);
        params.put("wrapper", "true");
//        params.put("receiveNetId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());

        ZLogger.d(String.format("加载库存批次开始:page=%d／%d(%d)",
                mPageInfo.getPageNo(), mPageInfo.getTotalPage(), mPageInfo.getPageSize()));

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvIoOrder>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvIoOrder> rs) {
                //此处在主线程中执行。
                new QueryAsyncTask(pageInfo).execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载库存批次失败:" + errMsg);
                onLoadFinished();
            }
        }, InvIoOrder.class, CashierApp.getAppContext());


        NetFactory.getHttp().post(InvOrderApiImpl.URL_INVIOORDER_LIST, params, queryRsCallBack);
    }

    public class QueryAsyncTask extends AsyncTask<RspQueryResult<InvIoOrder>, Integer, Long> {
        private PageInfo pageInfo;
        private JSONArray cacheArrays = new JSONArray();
        private List<InvIoOrder> entityList  = new ArrayList<>();

        public QueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<InvIoOrder>... params) {
            saveQueryResult(params[0], pageInfo);
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            //更新数据源
            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
//                orderList = entityList;
                if (orderListAdapter != null) {
                    orderListAdapter.setEntityList(entityList);
                }

                ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
            } else {
                if (orderListAdapter != null) {
                    orderListAdapter.appendEntityList(entityList);
                }
            }

//            ZLogger.d(String.format("加载库存批次结束,pageInfo':page=%d/%d(%d/%d)",
//                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
//                    (orderList == null ? 0 : orderList.size()), mPageInfo.getTotalCount()));

            if (orderType.equals(String.valueOf(InvIoOrder.ORDER_TYPE_IN))) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVIOORDER_IN_ENABLED, false);
            } else if (orderType.equals(String.valueOf(InvIoOrder.ORDER_TYPE_OUT))) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVIOORDER_OUT_ENABLED, false);
            }
            onLoadFinished();
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<InvIoOrder> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
                mPageInfo = pageInfo;

//                JSONArray cacheArrays = new JSONArray();
//                List<InvIoOrder> entityList  = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<InvIoOrder> wrapper : rs.getRowDatas()) {
                        InvIoOrder invIoOrder = fromEntityWrapper(wrapper);
                        cacheArrays.add(invIoOrder);
                        entityList.add(invIoOrder);
                    }
                }
                ZLogger.d(String.format("保存库存批次数据:page=%d/%d(%d/%d)",
                        mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                        (entityList == null ? 0 : entityList.size()), mPageInfo.getPageSize()));

//                //第一页，缓存数据
//                if (mPageInfo.getPageNo() == 1) {
//                    if (orderList == null) {
//                        orderList = new ArrayList<>();
//                    } else {
//                        orderList.clear();
//                    }
//                    ACache.get(CashierApp.getAppContext(), Constants.CACHE_NAME).put(cacheKey, cacheArrays.toJSONString());
//                } else {
//                    if (orderList == null) {
//                        orderList = new ArrayList<>();
//                    }
//                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("加载商品库存批次失败: %s", ex.toString()));
            }
        }
    }

    private InvIoOrder fromEntityWrapper(EntityWrapper<InvIoOrder> wrapper){
        InvIoOrder entity = wrapper.getBean();
        Map<String, String> caption = wrapper.getCaption();
        if (entity != null && caption != null) {
            entity.setStatusCaption(wrapper.getCaption().get("status"));
            entity.setBizTypeCaption(wrapper.getCaption().get("bizType"));
            entity.setStoreTypeCaption(wrapper.getCaption().get("storeType"));
            entity.setNetName(wrapper.getCaption().get("netId"));
        }
        return entity;
    }

    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(cacheKey);
        List<InvIoOrder> cacheData = JSONArray.parseArray(cacheStr, InvIoOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d条库存批次", cacheKey, cacheData.size()));
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
            ZLogger.d("正在加载 批次流水。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载 批次流水。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载库存批次，已经是最后一页。");
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

}

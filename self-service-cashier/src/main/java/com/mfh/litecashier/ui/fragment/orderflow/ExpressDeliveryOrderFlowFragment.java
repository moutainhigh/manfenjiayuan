package com.mfh.litecashier.ui.fragment.orderflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.CashierApi;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetFactory;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.ReceiveBatchItem;
import com.mfh.litecashier.bean.StockOutItem;
import com.mfh.litecashier.event.ExpressDeliveryOrderFlowEvent;
import com.mfh.litecashier.ui.adapter.ExpressDeliveryOrderflowGoodsAdapter;
import com.mfh.litecashier.ui.adapter.ExpressDeliveryOrderflowOrderAdapter;
import com.mfh.litecashier.utils.ACacheHelper;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 代收快递－订单流水
 * Created by kun on 15/8/31.
 */
public class ExpressDeliveryOrderFlowFragment extends BaseListFragment<ReceiveBatchItem> {
    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @Bind(R.id.empty_view)
    TextView emptyView;
    private LinearLayoutManager linearLayoutManager;
    private ExpressDeliveryOrderflowOrderAdapter orderListAdapter;

    @Bind(R.id.goods_list)
    RecyclerView goodsRecyclerView;
    private ExpressDeliveryOrderflowGoodsAdapter goodsListAdapter;

    @Bind(R.id.frame_bottom)
    LinearLayout frameBottom;
    @Bind(R.id.tv_quantity)
    TextView tvQuantity;
    @Bind(R.id.tv_soft_fee)
    TextView tvSoftFee;
    @Bind(R.id.tv_sms_fee)
    TextView tvSmsFee;
    @Bind(R.id.tv_storage_fee)
    TextView tvStorageFee;
    @Bind(R.id.tv_batch_income)
    TextView tvBatchIncome;

    private ReceiveBatchItem curOrder;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_orderflow_expressdelivery;
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

        readCache();
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

        orderListAdapter = new ExpressDeliveryOrderflowOrderAdapter(CashierApp.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new ExpressDeliveryOrderflowOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //加载批次明细
                loadGoodsList(orderListAdapter.getCurPosOrder());
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                loadGoodsList(orderListAdapter.getCurPosOrder());
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
        goodsListAdapter = new ExpressDeliveryOrderflowGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new ExpressDeliveryOrderflowGoodsAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged() {
            }
        });
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(ExpressDeliveryOrderFlowEvent event) {
        ZLogger.d(String.format("ExpressDeliveryOrderFlowFragment: ExpressDeliveryOrderFlowEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == ExpressDeliveryOrderFlowEvent.EVENT_ID_RELOAD_DATA) {
            if (!readCache()) {
                reload();
            }
        }
    }

    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public synchronized void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载代收快递订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }


        onLoadStart();

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
//        if (entityList == null){
//            entityList = new ArrayList<>();
//        }
//        else{
//            entityList.clear();
//        }

        load(mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 加载数据
     */
    private void load(PageInfo pageInfo) {
        AjaxParams params = new AjaxParams();
        params.put("stockId", MfhLoginService.get().getCurStockId());
        params.put("wrapper", "true");//是否翻译，显示公司名
        params.put("stockType", "2");//2,代表快递
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        params.put("createdDate_far", sdf.format(calendar.getTime()));//结束时期
        calendar.add(Calendar.DATE, 0 - 30);//
        params.put("createdDate", sdf.format(calendar.getTime()));//开始日期
        params.put("orderby", "createdDate");//排序

        ZLogger.d(String.format("加载代收快递订单流水开始,pageInfo':page=%d,rows=%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                (entityList == null ? 0 : entityList.size()), mPageInfo.getTotalCount()));

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ReceiveBatchItem>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ReceiveBatchItem> rs) {
                //此处在主线程中执行。
                new QueryAsyncTask(pageInfo).execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载代收快递订单流水失败:" + errMsg);
                onLoadFinished();
            }
        }, ReceiveBatchItem.class, CashierApp.getAppContext());

        AfinalFactory.postDefault(CashierApi.URL_STOCK_RECEIVEBATCH_COMNQUERY, params, queryRsCallBack);
    }

    public class QueryAsyncTask extends AsyncTask<RspQueryResult<ReceiveBatchItem>, Integer, Long> {
        private PageInfo pageInfo;

        public QueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<ReceiveBatchItem>... params) {
            saveQueryResult(params[0], pageInfo);
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            ZLogger.d(String.format("加载代收快递订单流水结束,pageInfo':page=%d,rows=%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                    (entityList == null ? 0 : entityList.size()), mPageInfo.getTotalCount()));

            if (orderListAdapter != null) {
                orderListAdapter.setEntityList(entityList);
            }
            onLoadFinished();
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<ReceiveBatchItem> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
                mPageInfo = pageInfo;
                ZLogger.d(String.format("保存代收快递订单流水,pageInfo':page=%d,rows=%d(%d/%d)",
                        mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                        (entityList == null ? 0 : entityList.size()), mPageInfo.getTotalCount()));

                if (rs == null) {
                    return;
                }

                //第一页，缓存数据
                if (mPageInfo.getPageNo() == 1) {
                    if (entityList == null) {
                        entityList = new ArrayList<>();
                    } else {
                        entityList.clear();
                    }
                    ZLogger.d("缓存代收快递订单流水第一页数据");
                    JSONArray cacheArrays = new JSONArray();
                    for (EntityWrapper<ReceiveBatchItem> wrapper : rs.getRowDatas()) {
                        ReceiveBatchItem item = wrapper.getBean();
                        item.setCompanyName(wrapper.getCaption().get("companyId"));
                        item.setCourierName(wrapper.getCaption().get("humanId"));
                        item.setStockName(wrapper.getCaption().get("stodkId"));

//                        ZLogger.d("bean:"+ JSONObject.toJSONString(wrapper.getBean()));
//                        ZLogger.d("caption:" + JSONObject.toJSONString(wrapper.getCaption()));
//                        ZLogger.d("data:"+ JSONObject.toJSONString(item));
                        cacheArrays.add(item);
                        entityList.add(item);
                    }
                    ACacheHelper.put(ACacheHelper.CK_ORDERFLOW_EXPRESS_DELIVERY, cacheArrays.toJSONString());
                } else {
                    if (entityList == null) {
                        entityList = new ArrayList<>();
                    }
                    for (EntityWrapper<ReceiveBatchItem> wrapper : rs.getRowDatas()) {
                        ReceiveBatchItem item = wrapper.getBean();
                        item.setCompanyName(wrapper.getCaption().get("companyId"));
                        item.setCourierName(wrapper.getCaption().get("humanId"));
                        item.setStockName(wrapper.getCaption().get("stodkId"));
                        entityList.add(item);
                    }
                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("加载代收快递订单流水失败: %s", ex.toString()));
            }
        }
    }


    private void loadGoodsList(ReceiveBatchItem batchItem) {
        curOrder = batchItem;
        if (curOrder == null) {
//            tvGoodsQunatity.setText(String.format("商品数：%.2f", 0D));
//            tvTotalAmount.setText(String.format("商品金额：%.2f", 0D));
            frameBottom.setVisibility(View.GONE);

            goodsListAdapter.setEntityList(null);
            return;
        }
        frameBottom.setVisibility(View.VISIBLE);

        tvQuantity.setText(String.format("数量:%d", curOrder.getBatchCount()));
        tvSoftFee.setText(Html.fromHtml(String.format("<font color=#000000>软件费: </font><font color=#FF009B4E>-%.2f</font>", curOrder.getBatchCount() * 0.1)));
        tvSmsFee.setText(Html.fromHtml(String.format("<font color=#000000>短信费: </font><font color=#FF009B4E>-%.2f</font>", curOrder.getSmsCount() * 0.1)));
        if (curOrder.getTotalCost() < 0) {
            tvStorageFee.setText(Html.fromHtml(String.format("<font color=#000000>保管费: </font><font color=#FF009B4E>%.2f</font>", curOrder.getTotalCost())));
        } else {
            tvStorageFee.setText(Html.fromHtml(String.format("<font color=#000000>保管费: </font><font color=#FF009B4E>＋%.2f</font>", curOrder.getTotalCost())));
        }

        if (curOrder.getIncome() < 0) {
            tvBatchIncome.setText(Html.fromHtml(String.format("<font color=#000000>批次收益: </font><font color=#FF009B4E>%.2f</font>", curOrder.getIncome())));
        } else {
            tvBatchIncome.setText(Html.fromHtml(String.format("<font color=#000000>批次收益: </font><font color=#FF009B4E>＋%.2f</font>", curOrder.getIncome())));
        }

        //TODO,加载批次明细
        StockApiImpl.findStockOut(curOrder.getId(), batchItemRspCallback);
    }

    private NetCallBack.QueryRsCallBack batchItemRspCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<StockOutItem>(new PageInfo(1, 50)) {
        @Override
        public void processQueryResult(RspQueryResult<StockOutItem> rs) {
            //此处在主线程中执行。
            int retSize = rs.getReturnNum();
            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

            List<StockOutItem> result = new ArrayList<>();
            if (retSize > 0) {
                for (int i = 0; i < retSize; i++) {
                    result.add(rs.getRowEntity(i));
                }
            }
            goodsListAdapter.setEntityList(result);

//            animProgress.setVisibility(View.GONE);
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);

//            animProgress.setVisibility(View.GONE);
        }
    }, StockOutItem.class, CashierApp.getAppContext());

    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_ORDERFLOW_EXPRESS_DELIVERY);
        List<ReceiveBatchItem> cacheData = JSONArray.parseArray(cacheStr, ReceiveBatchItem.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d条代收快递订单流水", ACacheHelper.CK_ORDERFLOW_EXPRESS_DELIVERY, cacheData.size()));
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
            ZLogger.d("正在加载代收快递订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载代收快递订单流水。");
            onLoadFinished();
            return;
        }


        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载代收快递订单流水，已经是最后一页。");
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

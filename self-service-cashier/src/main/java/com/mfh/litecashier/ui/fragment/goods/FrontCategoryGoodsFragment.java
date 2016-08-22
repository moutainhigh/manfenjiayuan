package com.mfh.litecashier.ui.fragment.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration2;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.FrontCategoryGoods;
import com.mfh.litecashier.event.AddCategoryGoodsEvent;
import com.mfh.litecashier.utils.ACacheHelper;

import net.tsz.afinal.core.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 前台类目商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class FrontCategoryGoodsFragment extends BaseListFragment<FrontCategoryGoods> {

    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport mRecyclerView;
    @Bind(R.id.empty_view)
    View emptyView;

    GridLayoutManager mRLayoutManager;
    private FrontCategoryGoodsAdapter adapter;

    private Long parentId;
    private Long categoryId;
    private String cacheKey;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_frontcategory_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        MAX_SYNC_PAGESIZE = 50;
//        mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, 50);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            parentId = args.getLong("parentId");
            categoryId = args.getLong("categoryId");
        }
        //format:CACHE_KEY_FRONT_CATEGORY_GOODS_[parentId]_[categoryId]
        cacheKey = String.format("%s_%d_%d", ACacheHelper.CK_FRONT_CATEGORY_GOODS,
                parentId, categoryId);

        initRecyclerView();
        setupSwipeRefresh();

        //默认先加载缓存数据
        readCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(FrontCategoryGoodsEvent event) {
        ZLogger.d(String.format("FrontCategoryGoodsEvent(%d/%d)",
                event.getAffairId(), event.getCategoryId()));
        if (event.getCategoryId().compareTo(categoryId) != 0) {
            return;
        }
        if (event.getAffairId() == FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA) {
            //内容为空，自动重新加载
            if (adapter == null || adapter.getItemCount() <= 0) {
                //优先读取缓存，加载缓存失败，重新加载
                if (!readCache()) {
                    reload();
                }
            }
            //更新缓存数据
            else {
                readCache();
            }
        }
    }

    private void initRecyclerView() {
        mRLayoutManager = new GridLayoutManager(CashierApp.getAppContext(), 9);

        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
//        mRecyclerView.setWrapperView(mSwipeRefreshLayout);
        //添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
                ContextCompat.getColor(getActivity(), R.color.mf_dividerColorPrimary), 0,
                ContextCompat.getColor(getActivity(), R.color.mf_dividerColorPrimary), 0.1f,
                ContextCompat.getColor(getActivity(), R.color.mf_dividerColorPrimary), 0f));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = mRLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = mRLayoutManager.getItemCount();
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

        adapter = new FrontCategoryGoodsAdapter(CashierApp.getAppContext(), null);
        adapter.setOnAdapterLitener(new FrontCategoryGoodsAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                EventBus.getDefault().post(new AddCategoryGoodsEvent(adapter.getEntity(position)));
            }

            @Override
            public void onItemLongClick(View view, final int position) {

                // TODO: 4/15/16 暂时不支持加入我的功能
//                final FrontCategoryGoods goods = adapter.getEntity(position);
//                if (goods == null) {
//                    return;
//                }
//
//                if (actionDialog == null) {
//                    actionDialog = new ActionDialog(getActivity());
//                    actionDialog.setCancelable(true);
//                    actionDialog.setCanceledOnTouchOutside(true);
//                }
//                actionDialog.setAction1("添加到我的", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        actionDialog.dismiss();
//                        CommonlyGoodsService.get().saveOrUpdate(parentId, goods);
//                    }
//                });
//                actionDialog.show();
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
            }
        });

        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public synchronized void reload() {
        if (bSyncInProgress) {
//            onLoadFinished();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载类目商品。");
            onLoadFinished();
            return;
        }

        onLoadStart();

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        //从第一页开始请求，每页最多50条记录
        load(mPageInfo);
        mPageInfo.setPageNo(1);
    }


    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(cacheKey);
        List<FrontCategoryGoods> cacheData = JSONArray.parseArray(cacheStr, FrontCategoryGoods.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个前台子类目商品", cacheKey, cacheData.size()));
            if (adapter != null) {
                adapter.setEntityList(cacheData);
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
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载类目商品。");
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
//        ZLogger.d(String.format("pageInfo:page=%d,rows=%d(%d)", mPageInfo.getPageNo(), mPageInfo.getPageSize(), (goodsList == null ? 0 : goodsList.size())));
        ZLogger.d(String.format("加载类目商品开始,pageInfo':page=%d,rows=%d(%d)",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(), mPageInfo.getTotalCount()));

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<FrontCategoryGoods>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<FrontCategoryGoods> rs) {
                //此处在主线程中执行。
                new QueryAsyncTask(pageInfo).execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载类目商品失败:" + errMsg);
                onLoadFinished();
            }
        }, FrontCategoryGoods.class, CashierApp.getAppContext());

        ScGoodsSkuApiImpl.findGoodsListByFrontCategory(categoryId, pageInfo, queryRsCallBack);
    }

    public class QueryAsyncTask extends AsyncTask<RspQueryResult<FrontCategoryGoods>, Integer, Long> {
        private PageInfo pageInfo;

        public QueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<FrontCategoryGoods>... params) {
            RspQueryResult<FrontCategoryGoods> rs = params[0];
            try {
                mPageInfo = pageInfo;

                if (rs == null) {
                    return -1L;
                }

                //第一页，缓存数据
                if (mPageInfo.getPageNo() == 1) {
                    if (entityList == null) {
                        entityList = new ArrayList<>();
                    } else {
                        entityList.clear();
                    }
                    ZLogger.d("缓存第一页前台类目数据");
                    JSONArray cacheArrays = new JSONArray();
                    for (EntityWrapper<FrontCategoryGoods> wrapper : rs.getRowDatas()) {
                        cacheArrays.add(wrapper.getBean());
                        entityList.add(wrapper.getBean());
                    }
                    ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
                } else {
                    if (entityList == null) {
                        entityList = new ArrayList<>();
                    }
                    for (EntityWrapper<FrontCategoryGoods> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("加载类目商品失败: %s", ex.toString()));
            }

            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            if (adapter != null) {
                adapter.setEntityList(entityList);
            }
            onLoadFinished();
        }
    }

    /**
     * 设置刷新
     */
    private void setupSwipeRefresh() {
//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.swiperefresh_color1, R.color.swiperefresh_color2,
                    R.color.swiperefresh_color3, R.color.swiperefresh_color4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mState == STATE_REFRESH) {
                        ZLogger.d("正在刷新");
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

package com.manfenjiayuan.pda_supermarket.ui.rider;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.manfenjiayuan.business.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.view.IScOrderView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.PrimaryActivity;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderAdapter;
import com.manfenjiayuan.pda_supermarket.ui.common.ScOrderFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 前登录骑手待配送的订单列表
 * Created by bingshanguxue on 15/8/30.
 */
public class AcceptableSendOrdersFragment extends BaseListFragment<ScOrder>
        implements IScOrderView {


    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;
    private ScOrderAdapter orderAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.animProgress)
    ProgressBar progressBar;


    private ScOrderPresenter mScOrderPresenter;

    public static AcceptableSendOrdersFragment newInstance(Bundle args) {
        AcceptableSendOrdersFragment fragment = new AcceptableSendOrdersFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_template_goods_list_v2;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScOrderPresenter = new ScOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_DEFAULT);
        }
        setupSwipeRefresh();

        initRecyclerView();

        reload();
    }


    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
//        mRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        orderAdapter = new ScOrderAdapter(getActivity(), null, false);
        orderAdapter.setOnAdapterListener(new ScOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                //TODO,跳转至详情页
                Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_SCORDER_DETAIL);
                extras.putSerializable(ScOrderFragment.EXTRA_KEY_SCORDER, orderAdapter.getEntity(position));
                PrimaryActivity.actionStart(getActivity(), extras);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
//                animProgress.setVisibility(View.GONE);
            }
        });

        mRecyclerView.setAdapter(orderAdapter);
    }

    /**
     * 设置刷新
     */
    private void setupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    com.bingshanguxue.pda.R.color.swiperefresh_color1, com.bingshanguxue.pda.R.color.swiperefresh_color2,
                    com.bingshanguxue.pda.R.color.swiperefresh_color3, com.bingshanguxue.pda.R.color.swiperefresh_color4);
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



    @Override
    public void onLoadStart() {
        super.onLoadStart();

        progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onLoadFinished() {
        super.onLoadFinished();

        progressBar.setVisibility(View.GONE);
    }

    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载采购订单。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载采购订单。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        mScOrderPresenter.findAcceptAbleSendOrders(mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载采购订单。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载采购订单。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();
            mScOrderPresenter.findAcceptAbleSendOrders(mPageInfo);
        } else {
            ZLogger.d("加载采购订单，已经是最后一页。");
            onLoadFinished();
        }
    }


    @Override
    public void onIScOrderViewProcess() {
        onLoadStart();
    }

    @Override
    public void onIScOrderViewError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                if (orderAdapter != null) {
                    orderAdapter.setEntityList(dataList);
                }
            } else {
                if (dataList != null && dataList.size() > 0) {
                    if (orderAdapter != null) {
                        orderAdapter.appendEntityList(dataList);
                    }
                }
            }

            ZLogger.d(String.format("加载商品采购订单结束,pageInfo':page=%d/%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                    orderAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.ef(String.format("加载商品采购订单失败: %s", ex.toString()));

            onLoadFinished();
        }
    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {

    }
}

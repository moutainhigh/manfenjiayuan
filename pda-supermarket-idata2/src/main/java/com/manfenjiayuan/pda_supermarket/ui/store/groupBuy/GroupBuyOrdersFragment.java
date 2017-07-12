package com.manfenjiayuan.pda_supermarket.ui.store.groupBuy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.bizz.invloss.InvLossOrderAdapter;
import com.manfenjiayuan.business.presenter.InvLossOrderPresenter;
import com.manfenjiayuan.business.view.IInvLossOrderView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 团购活动订单列表
 * Created by bingshanguxue on 15/8/30.
 */
public class PickupCustomersFragment extends BaseListFragment<InvLossOrder> implements IInvLossOrderView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_view)
    View emptyView;
    private InvLossOrderAdapter orderListAdapter;

    private InvLossOrderPresenter mInvLossOrderPresenter;

    public static PickupCustomersFragment newInstance(Bundle args) {
        PickupCustomersFragment fragment = new PickupCustomersFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inventory_check;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInvLossOrderPresenter = new InvLossOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_DEFAULT);
        }
        setupToolbar();
        setupSwipeRefresh();
        initOrderRecyclerView();

        reload();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inv_check, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 设置toolbar
     */
    private void setupToolbar() {
        mToolbar.setTitle("顾客列表");

        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_sync) {
                    reload();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_inv_check);
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
//        orderRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
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

        orderListAdapter = new InvLossOrderAdapter(AppContext.getAppContext(), null);
        orderListAdapter.setOnAdapterListener(new InvLossOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                InvLossOrder invLossOrder = orderListAdapter.getEntity(position);
                if (invLossOrder == null) {
                    return;
                }

                // TODO: 26/06/2017 打电话给该客户，检查是否支持电话功能。
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
    }

    /**
     * 加载盘点订单列表列表
     */
    @OnClick({R.id.empty_view})
    public void reload() {
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            onLoadFinished();
            return;
        }

        onLoadStart();

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        //从第一页开始请求，每页最多50条记录
        mInvLossOrderPresenter.list(mPageInfo);
        mPageInfo.setPageNo(1);
    }


    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
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
            mInvLossOrderPresenter.list(mPageInfo);
        } else {
            ZLogger.d("加载类目商品，已经是最后一页。");
            onLoadFinished();
        }
    }

    @Override
    public void onIInvLossOrderViewProcess() {
        onLoadStart();
    }

    @Override
    public void onIInvLossOrderViewError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onIInvLossOrderViewSuccess(PageInfo pageInfo, List<InvLossOrder> dataList) {
        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                if (orderListAdapter != null) {
                    orderListAdapter.setEntityList(dataList);
                }
            } else {
                if (dataList != null && dataList.size() > 0) {
                    if (orderListAdapter != null) {
                        orderListAdapter.appendEntityList(dataList);
                    }
                }
            }

            ZLogger.d(String.format("加载商品采购订单结束,pageInfo':page=%d/%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                    orderListAdapter.getItemCount(), mPageInfo.getTotalCount()));

        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.ef(String.format("加载商品采购订单失败: %s", ex.toString()));
        }
        onLoadFinished();
    }

    @Override
    public void onIInvLossOrderViewSuccess(ScOrder data) {

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

package com.mfh.litecashier.ui.fragment.purchase;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.manfenjiayuan.business.bean.InvSendIoOrder;
import com.manfenjiayuan.business.bean.InvSendIoOrderItem;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.PurchaseReturnCreateEvent;
import com.mfh.litecashier.presenter.InvRecvOrderPresenter;
import com.mfh.litecashier.ui.adapter.SelectRecvOrderAdapter;
import com.mfh.litecashier.ui.view.IInvRecvOrderView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 对话框－－ 选择收货订单
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class SelectInvRecvOrderFragment extends BaseListFragment<InvSendIoOrder> implements IInvRecvOrderView {
    public static final String EXTRA_KEY_STATUS = "status";
    public static final String EXTRA_KEY_CACHEKEY = "cacheKey";
    public static final String EK_SENDTENANTID = "sendTenantId";

    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.tv_header_title)
    TextView tvTitle;

    @Bind(R.id.order_list)
    RecyclerViewEmptySupport mRecyclerView;
    private SelectRecvOrderAdapter productAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view)
    TextView emptyView;

    private String status;
    private String cacheKey;
    private String sendTenantId = "";

    private InvRecvOrderPresenter invRecvOrderPresenter;

    public static SelectInvRecvOrderFragment newInstance(Bundle args) {
        SelectInvRecvOrderFragment fragment = new SelectInvRecvOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_select_inv_sendorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        invRecvOrderPresenter = new InvRecvOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            status = args.getString(EXTRA_KEY_STATUS);
            cacheKey = args.getString(EXTRA_KEY_CACHEKEY);
            sendTenantId = args.getString(EK_SENDTENANTID);
        }

        tvTitle.setText("选择收货订单");
        setupSwipeRefresh();
        initRecyclerView();

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            readCache();
        } else {
            reload();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
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

        productAdapter = new SelectRecvOrderAdapter(getActivity(), null);
        productAdapter.setOnAdapterListener(new SelectRecvOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                args.putSerializable("data", productAdapter.getCurPosOrder());
                EventBus.getDefault().post(new PurchaseReturnCreateEvent(PurchaseReturnCreateEvent.EVENT_ID_RELOAD_INV_RECVORDER, args));

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

            @Override
            public void onDataSetChanged() {
            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(cacheKey);
        List<InvSendIoOrder> cacheData = JSONArray.parseArray(cacheStr, InvSendIoOrder.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d条收货订单", cacheKey, cacheData.size()));
//            refreshCategoryGoodsTab(entity.getCategoryId(), cacheData);
            if (productAdapter != null) {
                productAdapter.setEntityList(cacheData);
            }

            return true;
        }
        return false;
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


    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载收货订单。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载收货订单。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        invRecvOrderPresenter.loadOrders(mPageInfo, status, null, sendTenantId);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载收货订单。");
//            onLoadFinished();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载收货订单。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            invRecvOrderPresenter.loadOrders(mPageInfo, status, null, sendTenantId);
        } else {
            ZLogger.d("加载收货订单，已经是最后一页。");
            onLoadFinished();
        }
    }


    @Override
    public void onQueryOrderProcess() {

        onLoadStart();
    }

    @Override
    public void onQueryOrderError(String errorMsg) {

        onLoadFinished();
    }

    @Override
    public void onQueryOrderSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {

        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                ZLogger.d("缓存商品收货订单第一页数据");
                JSONArray cacheArrays = new JSONArray();
                if (dataList != null) {
                    cacheArrays.addAll(dataList);
                }

                if (productAdapter != null) {
                    productAdapter.setEntityList(dataList);
                }

                ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PURCHASERECEIPT_ORDER_ENABLED, false);
            } else {
                if (productAdapter != null) {
                    productAdapter.appendEntityList(dataList);
                }
            }
            ZLogger.d(String.format("加载收货订单结束,pageInfo':page=%d,rows=%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                    productAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载商品收货订单失败: %s", ex.toString()));
            onLoadFinished();
        }


    }

    @Override
    public void onQueryOrderItemsSuccess(List<InvSendIoOrderItem> dataList) {

    }


}

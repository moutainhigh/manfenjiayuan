package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.framework.api.invSendIoOrder.IInvSendIoOrderView;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderPresenter;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.SelectRecvOrderAdapter;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * 对话框 -- 选择采购收货订单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SelectInvRecvOrderDialog extends CommonDialog  implements IInvSendIoOrderView {

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TextView emptyView;
    public static final int STATE_NONE = 0;
    public static final int STATE_REFRESH = 1;
    public static final int STATE_LOADMORE = 2;
    public static final int STATE_NOMORE = 3;
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    public static int mState = STATE_NONE;
    SwipeRefreshLayout mSwipeRefreshLayout;

    private SelectRecvOrderAdapter productAdapter;

    private boolean isLoadingMore;

    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 20;
    private boolean bSyncInProgress = false;//是否正在同步
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<InvSendIoOrder> orderList = new ArrayList<>();
    private String status;
    private String cacheKey;

    private InvSendIoOrderPresenter invRecvOrderPresenter;

    @Override
    public void onQueryOrderProcess() {

    }

    @Override
    public void onQueryOrderError(String errorMsg) {

    }

    @Override
    public void onQueryOrderSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList) {
        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                if (orderList == null) {
                    orderList = new ArrayList<>();
                } else {
                    orderList.clear();
                }
                ZLogger.d("缓存采购收货订单单第一页数据");
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
                    productAdapter.appendEntityList(orderList);
                }
            }
            ZLogger.d(String.format("加载采购收货订单结束,pageInfo':page=%d,rows=%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                    productAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载采购收货订单失败: %s", ex.toString()));
            onLoadFinished();
        }

    }

    @Override
    public void onQueryOrderItemsSuccess(List<InvSendIoOrderItem> dataList) {

    }

    public interface OnDialogListener {
        void onItemSelected(InvSendIoOrder invSendOrder);
    }

    private OnDialogListener listener;

    private SelectInvRecvOrderDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectInvRecvOrderDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_select_sendorder, null);
//        ButterKnife.bind(rootView);


        invRecvOrderPresenter = new InvSendIoOrderPresenter(this);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.order_list);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);

        tvTitle.setText("选择收货订单");
        setupSwipeRefresh();
        initRecyclerView();
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public SelectInvRecvOrderDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);

//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);


        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            readCache();
        } else {
            reload();
        }
    }

    public void init(String status, String cachekey, OnDialogListener listener) {
        this.status = status;
        this.cacheKey = cachekey;
        this.listener = listener;

        this.productAdapter.setEntityList(null);
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getContext(), LineItemDecoration.VERTICAL_LIST));
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
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        productAdapter = new SelectRecvOrderAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new SelectRecvOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismiss();

                if (listener != null) {
                    listener.onItemSelected(productAdapter.getCurPosOrder());
                }
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
            ZLogger.d(String.format("加载缓存数据(%s): %d条采购收货订单", cacheKey, cacheData.size()));
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
    private void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载采购收货订单。");
//            onLoadFinished();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载采购收货订单。");
            onLoadFinished();
            return;
        }


        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        invRecvOrderPresenter.loadOrders(mPageInfo, status, null, null);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载采购收货订单。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载采购收货订单。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            invRecvOrderPresenter.loadOrders(mPageInfo, status, null, null);
        } else {
            ZLogger.d("加载采购收货订单，已经是最后一页。");
            onLoadFinished();
        }
    }

}

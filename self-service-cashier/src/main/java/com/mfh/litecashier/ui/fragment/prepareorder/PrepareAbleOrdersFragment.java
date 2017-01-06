package com.mfh.litecashier.ui.fragment.prepareorder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.manfenjiayuan.business.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IScOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 买手--线上订单抢单列表
 * Created by bingshanguxue on 15/8/31.
 */
public class PrepareAbleOrdersFragment extends BaseListFragment<ScOrder> implements IScOrderView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    private LinearLayoutManager linearLayoutManager;
    private PrepareableOrderAdapter orderAdapter;

    @BindView(R.id.order_goods_list)
    RecyclerView goodsRecyclerView;
    private PrepareableOrderItemsAdapter goodsAdapter;

    @BindView(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @BindView(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @BindView(R.id.button_print)
    Button btnPrint;

    private ScOrderPresenter mScOrderPresenter;
    private ScOrder mScOrder;

    public static PrepareAbleOrdersFragment newInstance(Bundle args) {
        PrepareAbleOrdersFragment fragment = new PrepareAbleOrdersFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_order_prepareable;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        mScOrderPresenter = new ScOrderPresenter(this);

    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("接单");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
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
                if (id == R.id.action_reload) {
                    reload();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_order);

        setupSwipeRefresh();
        initOrderRecyclerView();
        initGoodsRecyclerView();

        reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    /**
     * 接单&打印订单明细
     */
    @OnClick(R.id.button_print)
    public void submitAndPrintOrder() {
//        DialogUtil.showHint("打印单据");
        btnPrint.setEnabled(false);
        if (mScOrder != null) {
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "接单中...", false);

            if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
                DialogUtil.showHint(R.string.toast_network_error);
                hideProgressDialog();
                btnPrint.setEnabled(true);

                return;
            }

            ScOrderApiImpl.acceptOrderWhenOrdered(mScOrder.getId(), responseRC);
        } else {
            ZLogger.d("订单无效");
            btnPrint.setEnabled(true);

        }
    }

    NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"更新成功!","version":"1","data":null}
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                    String result = null;
                    if (rspData != null) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        result = retValue.getValue();
                    }

                    //出库成功:1-556637
                    showProgressDialog(ProgressDialog.STATUS_DONE, "接单成功", true);
//                    DialogUtil.showHint("接单成功");
//                    hideProgressDialog();
                    ZLogger.d(String.format("接单成功: %s，准备打印订单", result));
                    PrinterFactory.getPrinterManager().printPrepareOrder(mScOrder);
                    btnPrint.setEnabled(true);

                    reload();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.ef(errMsg);
                    hideProgressDialog();
                    btnPrint.setEnabled(true);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
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

        goodsAdapter = new PrepareableOrderItemsAdapter(CashierApp.getAppContext(), null);
        goodsRecyclerView.setAdapter(goodsAdapter);
    }


    private void initOrderRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        orderRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        orderRecyclerView.setHasFixedSize(true);
        //添加分割线
        orderRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
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

        orderAdapter = new PrepareableOrderAdapter(getActivity(), null);
        orderAdapter.setOnAdapterListener(new PrepareableOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                reloadOrderItems(orderAdapter.getEntity(position));
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
//                animProgress.setVisibility(View.GONE);
                reloadOrderItems(orderAdapter.getCurScOrder());
            }
        });

        orderRecyclerView.setAdapter(orderAdapter);
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

        mProgressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onLoadFinished() {
        super.onLoadFinished();

        mProgressBar.setVisibility(View.GONE);
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
        mScOrderPresenter.findPrepareAbleOrders(mPageInfo);
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
            mScOrderPresenter.findPrepareAbleOrders(mPageInfo);
        } else {
            ZLogger.d("加载采购订单，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 加载订单明细
     */
    private void reloadOrderItems(ScOrder scOrder) {
        mScOrder = scOrder;
//        List<Item> items = new ArrayList<>();
        if (mScOrder != null) {
//            items.add(mPosOrder);
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：",
                    mScOrder.getBcount(), "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：",
                    mScOrder.getAmount(), "无", null, null));
            btnPrint.setEnabled(true);

            mScOrderPresenter.getByBarcode(scOrder.getBarcode(), scOrder.getStatus(), true);

//            goodsAdapter.setEntityList(mScOrder.getItems());

        } else {
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：", null, "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：", null, "无", null, null));
            btnPrint.setEnabled(false);
            goodsAdapter.setEntityList(null);
        }
//        goodsRecyclerView.setAdapter(new MultiTypeAdapter(items));
    }

    @Override
    public void onIScOrderViewProcess() {

    }

    @Override
    public void onIScOrderViewError(String errorMsg) {

    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {
        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo != null && mPageInfo.getPageNo() == 1) {
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

            ZLogger.d(String.format("加载待组货订单结束,pageInfo':page=%d/%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                    orderAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载待组货订单失败: %s", ex.toString()));

            onLoadFinished();
        }
    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {

        if (data != null){
            goodsAdapter.setEntityList(data.getItems());
        }
        else{
            goodsAdapter.setEntityList(null);
        }
    }
}

package com.mfh.litecashier.components.pickup;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.manfenjiayuan.business.presenter.GroupBuyOrderPresenter;
import com.manfenjiayuan.business.view.IGroupBuyOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.rxapi.bean.GroupBuyActivity;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.CustomerView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;



/**
 * <h1>商品自提</h1>
 * Created by bingshanguxue on 15/12/15.
 */
public class PickupFragment extends BaseListFragment<GroupBuyOrder> implements IGroupBuyOrderView{
    public static final String EXTRA_HUMAN = "human";

    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.customer_view)
    CustomerView mCustomerView;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport mRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    private LinearLayoutManager mRLayoutManager;
    private GroupBuyOrderAdapter mOrderAdapter;


    private Human mHuman;
    private GroupBuyOrderPresenter mGroupBuyOrderPresenter;


    public static PickupFragment newInstance(Bundle args) {
        PickupFragment fragment = new PickupFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGroupBuyOrderPresenter = new GroupBuyOrderPresenter(this);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_pickup;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            ZLogger.d("打开商品自提页面 开始");
            Bundle args = getArguments();
            if (args != null) {
                mHuman = (Human) args.getSerializable(EXTRA_HUMAN);
            }
            tvHeaderTitle.setText("商品自提");
            initRecyclerView();

            mCustomerView.reload(mHuman);

            if (mHuman != null) {
                reload();
            }
            ZLogger.d("打开商品自提页面 结束");
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e("打开商品自提页面 异常" + e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (orderListScrollListener != null) {
            mRecyclerView.removeOnScrollListener(orderListScrollListener);
        }
    }


    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(getActivity());
        mRLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setEmptyView(emptyView);
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

//        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));
        mRecyclerView.addOnScrollListener(orderListScrollListener);

        mOrderAdapter = new GroupBuyOrderAdapter(getActivity(), null);
        mOrderAdapter.setOnAdapterListener(new GroupBuyOrderAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged() {
            }
        });
        mRecyclerView.setAdapter(mOrderAdapter);
//        mRecyclerView.setAdapter(null);
    }

    private RecyclerView.OnScrollListener orderListScrollListener = new RecyclerView.OnScrollListener() {
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
            if (lastVisibleItem >= totalItemCount - 1 && dx > 0) {
                if (!isLoadingMore) {
                    onLoadFinished();
//                    loadMore();
                }
            } else if (dy < 0) {
                isLoadingMore = false;
            }
        }
    };

    @OnClick(R.id.button_print)
    public void printOrder() {
        List<GroupBuyOrder> groupBuyOrders = mOrderAdapter.getSelectedEntityList();
        if (groupBuyOrders == null || groupBuyOrders.size() <= 0) {
            DialogUtil.showHint(R.string.tip_please_select_order);
            return;
        }
        DialogUtil.showHint("打印提货单");
        //多选
        PrinterFactory.getPrinterManager().printPickupOrder(groupBuyOrders);

        mGroupBuyOrderPresenter.receiveAndFinishOrder(groupBuyOrders.get(0).getId());
    }

    @OnClick(R.id.empty_view)
    public void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }


        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        mGroupBuyOrderPresenter.queryHumanOrder(mHuman.getId(), mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载线上订单订单流水。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            mGroupBuyOrderPresenter.queryHumanOrder(mHuman.getId(), mPageInfo);
        } else {
            ZLogger.d("加载采购订单，已经是最后一页。");
            onLoadFinished();
        }
    }


    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        hideProgressDialog();
    }


    @Override
    public void onIGroupBuyOrderViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在加载团购订单", false);
    }

    @Override
    public void onIGroupBuyOrderViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, "正在加载团购订单", true);

        onLoadFinished();
    }

    @Override
    public void onQueryGroupBuyActitiySuccess(PageInfo pageInfo, List<GroupBuyActivity> dataList) {

    }

    @Override
    public void onQueryGroupBuyOrderSuccess(PageInfo pageInfo, List<GroupBuyOrder> dataList) {
        mPageInfo = pageInfo;

        if (pageInfo == null || pageInfo.getPageNo() == 1) {
            mOrderAdapter.setEntityList(dataList);
        } else {
            mOrderAdapter.appendEntityList(dataList);
        }

        onLoadFinished();
    }

    @Override
    public void onIGroupBuyOrderViewSuccess(ScOrder data) {

    }

    @Override
    public void onNotifyTakeGoodsSuccess(String data) {

    }

    @Override
    public void onNotifyReceiveAndFinishOrderSuccess(String data) {
        onLoadFinished();

        getActivity().finish();
    }
}

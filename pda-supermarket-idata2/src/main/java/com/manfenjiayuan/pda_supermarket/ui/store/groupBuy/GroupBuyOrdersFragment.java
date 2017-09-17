package com.manfenjiayuan.pda_supermarket.ui.store.groupBuy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.mvp.presenter.GroupBuyOrderPresenter;
import com.manfenjiayuan.business.mvp.view.IGroupBuyOrderView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.rxapi.bean.GroupBuyActivity;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 团购活动订单列表
 * Created by bingshanguxue on 15/8/30.
 */
public class GroupBuyOrdersFragment extends BaseListFragment<InvLossOrder> implements IGroupBuyOrderView {
    public static final String EXTRA_KEY_ACTIVITY_ID = "activityId";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_view)
    View emptyView;
    @BindView(R.id.fab_submit)
    FloatingActionButton fabSubmit;
    @BindView(R.id.animProgress)
    ProgressBar progressBar;

    private Long activityId;
    private GroupBuyOrderAdapter mGroupBuyOrderAdapter;

    private GroupBuyOrderPresenter mGroupBuyOrderPresenter;

    public static GroupBuyOrdersFragment newInstance(Bundle args) {
        GroupBuyOrdersFragment fragment = new GroupBuyOrdersFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_groupbuy_orders;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroupBuyOrderPresenter = new GroupBuyOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_DEFAULT);
            activityId = args.getLong(EXTRA_KEY_ACTIVITY_ID);
        }
        setupToolbar();
        setupSwipeRefresh();
        initOrderRecyclerView();

        reload();
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

        mGroupBuyOrderAdapter = new GroupBuyOrderAdapter(AppContext.getAppContext(), null);
        mGroupBuyOrderAdapter.setOnAdapterListener(new GroupBuyOrderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                GroupBuyOrder groupBuyOrder = mGroupBuyOrderAdapter.getEntity(position);
                if (groupBuyOrder != null) {
                    selectNotifyWay(groupBuyOrder);
                }
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                if (mGroupBuyOrderAdapter.getItemCount() > 0) {
                    fabSubmit.setVisibility(View.VISIBLE);
                } else {
                    fabSubmit.setVisibility(View.GONE);
                }
            }
        });
        orderRecyclerView.setAdapter(mGroupBuyOrderAdapter);
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

        if (bSyncInProgress) {
            onLoadFinished();
            return;
        }
        onLoadStart();

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        //从第一页开始请求，每页最多50条记录
        mGroupBuyOrderPresenter.queryNetOrder(activityId, mPageInfo);
        mPageInfo.setPageNo(1);
    }

    @OnClick(R.id.fab_submit)
    public void notifyHumanTakeGood() {
        new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert)
//                .setTitle(R.string.notify_human_title)
                .setMessage(R.string.notifyHumanTakeGood)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        mGroupBuyOrderPresenter.notifyTakeGoods(activityId);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }


    private void selectNotifyWay2(final GroupBuyOrder groupBuyOrder) {
        try {
            View.OnClickListener click = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    ZLogger.d("onClick");

                    mNotifyDialog.dismiss();

                    switch (id) {
                        case R.id.tv_phone:
                            ZLogger.d("tv_phone");
                            UIHelper.callPhone(getContext(), groupBuyOrder.getReceivePhone());

                            break;
                        case R.id.tv_message:
                            ZLogger.d("tv_meaasge");
                            mGroupBuyOrderPresenter.notifyHumanTakeGood(groupBuyOrder.getId());
                            break;
                        default:
                            break;
                    }
                }
            };

//            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_notify_type, null);
            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.dialog_select_notify_type, null);
            if (view != null) {
                TextView tvPhone = (TextView) view.findViewById(R.id.tv_phone);
                if (tvPhone != null) {
                    tvPhone.setOnClickListener(click);
                } else {
                    ZLogger.d("cann't find tvPhone");
                }
                TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
                if (tvMessage != null) {
                    tvMessage.setOnClickListener(click);
                } else {
                    ZLogger.d("cann't find tvMessage");
                }
            }

            new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert)
//                .setTitle(R.string.notify_human_title)
//        setSingleChoiceItems
//                .setSingleChoiceItems()
                .setView(view)
//                    .setItems(R.array.notify_human_way, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(final DialogInterface dialog, final int which) {
//                            switch (which) {
//                                case 0:
//                                    UIHelper.callPhone(getContext(), groupBuyOrder.getReceivePhone());
//                                    break;
//                                case 1:
//                                    mGroupBuyOrderPresenter.notifyHumanTakeGood(groupBuyOrder.getId());
//                                    break;
//                            }
//                        }
//                    })
//                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(final DialogInterface dialog, final int which) {
//                        onTxDataSourceChanged();
//                    }
//                })
//                .setNegativeButton(R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }

    }

    private CommonDialog mNotifyDialog;
    private void selectNotifyWay(final GroupBuyOrder groupBuyOrder) {
        if (mNotifyDialog == null){
            mNotifyDialog = DialogHelper.getPinterestDialogCancelable(getContext());

            View.OnClickListener click = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    mNotifyDialog.dismiss();

                    switch (id) {
                        case R.id.tv_phone:
                            UIHelper.callPhone(getContext(), groupBuyOrder.getReceivePhone());
                            break;
                        case R.id.tv_message:
                            mGroupBuyOrderPresenter.notifyHumanTakeGood(groupBuyOrder.getId());

                            break;
                        default:
                            break;
                    }
                }
            };

            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.dialog_select_notify_type, null);
            TextView tvPhone = (TextView) view.findViewById(R.id.tv_phone);
            if (tvPhone != null) {
                tvPhone.setOnClickListener(click);
            }
            TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
            if (tvMessage != null) {
                tvMessage.setOnClickListener(click);
            }
//            view.findViewById(R.id.tv_phone).setOnClickListener(click);
//            view.findViewById(R.id.tv_meaasge).setOnClickListener(click);

            mNotifyDialog.setContent(view);
        }

        if (!mNotifyDialog.isShowing()){
            mNotifyDialog.show();
        }
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
            mGroupBuyOrderPresenter.queryNetOrder(activityId, mPageInfo);
        } else {
            ZLogger.d("加载类目商品，已经是最后一页。");
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

    @Override
    public void onLoadStart() {
        super.onLoadStart();
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void onLoadFinished() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onIGroupBuyOrderViewProcess() {
        onLoadStart();

    }

    @Override
    public void onIGroupBuyOrderViewError(String errorMsg) {
        onLoadFinished();
        DialogUtil.showHint(errorMsg);
    }

    @Override
    public void onQueryGroupBuyActitiySuccess(PageInfo pageInfo, List<GroupBuyActivity> dataList) {
        onLoadFinished();

    }

    @Override
    public void onQueryGroupBuyOrderSuccess(PageInfo pageInfo, List<GroupBuyOrder> dataList) {
        mPageInfo = pageInfo;

        //第一页，缓存数据
        if (mPageInfo.getPageNo() == 1) {
            if (mGroupBuyOrderAdapter != null) {
                mGroupBuyOrderAdapter.setEntityList(dataList);
            }
        } else {
            if (dataList != null && dataList.size() > 0) {
                if (mGroupBuyOrderAdapter != null) {
                    mGroupBuyOrderAdapter.appendEntityList(dataList);
                }
            }
        }

        ZLogger.d(String.format("加载团购活动订单结束,pageInfo':page=%d/%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                mGroupBuyOrderAdapter.getItemCount(), mPageInfo.getTotalCount()));

        onLoadFinished();
    }

    @Override
    public void onIGroupBuyOrderViewSuccess(ScOrder data) {
        onLoadFinished();

    }

    @Override
    public void onNotifyTakeGoodsSuccess(String data) {
        DialogUtil.showHint("发送通知完成");
        onLoadFinished();

//        getActivity().setResult(Activity.RESULT_OK);
//        getActivity().finish();
    }

    @Override
    public void onNotifyReceiveAndFinishOrderSuccess(String data) {
        onLoadFinished();

    }
}

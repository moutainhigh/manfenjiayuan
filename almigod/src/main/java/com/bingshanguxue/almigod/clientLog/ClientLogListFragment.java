package com.bingshanguxue.almigod.clientLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bingshanguxue.almigod.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.clientLog.ClientLog;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 批发商列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ClientLogListFragment extends BaseListFragment<CompanyInfo>
        implements IClientLogView {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ClientLogAdapter companyAdapter;

    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.animProgress)
    ProgressBar progressBar;

    private ClientLogPresenter mInvCompanyPresenter;

    public static ClientLogListFragment newInstance(Bundle args) {
        ClientLogListFragment fragment = new ClientLogListFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_poslist;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MAX_SYNC_PAGESIZE = 30;
        mInvCompanyPresenter = new ClientLogPresenter(this);
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            abilityItem = args.getInt(EXTRA_KEY_ABILITY_ITEM, AbilityItem.TENANT);
//        }
        ZLogger.d("进入POS设备列表页面 开始");
        mToolbar.setTitle("日志列表");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        initRecyclerView();

        reload();
        ZLogger.d("进入POS设备列表页面 结束");
    }


    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
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

        companyAdapter = new ClientLogAdapter(getActivity(), null);
        companyAdapter.setOnAdapterListener(new ClientLogAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TODO,跳转至详情页
                Intent data = new Intent();
                data.putExtra("clientLog", companyAdapter.getEntity(position));

                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
//                animProgress.setVisibility(View.GONE);
            }
        });

        mRecyclerView.setAdapter(companyAdapter);
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
        mInvCompanyPresenter.list(mPageInfo);
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

            mInvCompanyPresenter.list(mPageInfo);
        } else {
            ZLogger.d("加载采购订单，已经是最后一页。");
            onLoadFinished();
        }
    }
    

    @Override
    public void onIClientLogViewProcess() {
        onLoadStart();
    }

    @Override
    public void onIClientLogViewError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onIClientLogViewSuccess(PageInfo pageInfo, List<ClientLog> dataList) {
        try {
            mPageInfo = pageInfo;

            //第一页，缓存数据
            if (mPageInfo.getPageNo() == 1) {
                if (companyAdapter != null) {
                    companyAdapter.setEntityList(dataList);
                }
            } else {
                if (dataList != null && dataList.size() > 0) {
                    if (companyAdapter != null) {
                        companyAdapter.appendEntityList(dataList);
                    }
                }
            }

            ZLogger.d(String.format("加载日志结束,pageInfo':page=%d/%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                    companyAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载日志失败: %s", ex.toString()));

            onLoadFinished();
        }
    }
}

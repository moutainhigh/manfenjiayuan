package com.bingshanguxue.pda.bizz.company;

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

import com.bingshanguxue.pda.R;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.invCompany.IInvCompanyInfoView;
import com.mfh.framework.api.invCompany.InvCompanyPresenter;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;


/**
 * 批发商列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvCompanyListFragment extends BaseListFragment<CompanyInfo>
        implements IInvCompanyInfoView {

    //    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    //    @Bind(R.id.scanBar)
    public ScanBar mScanBar;
    //    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private InvCompanyAdapter companyAdapter;

    //    @Bind(R.id.empty_view)
    View emptyView;
    //    @Bind(R.id.animProgress)
    ProgressBar progressBar;


    private InvCompanyPresenter mInvCompanyPresenter;

    public static InvCompanyListFragment newInstance(Bundle args) {
        InvCompanyListFragment fragment = new InvCompanyListFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invcompany_list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MAX_SYNC_PAGESIZE = 30;
        mInvCompanyPresenter = new InvCompanyPresenter(this);
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);
        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.goods_list);
        emptyView = rootView.findViewById(R.id.empty_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload();
            }
        });
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            status = args.getString(EXTRA_KEY_STATUS);
//        }

        mToolbar.setTitle("选择批发商");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                reload();
            }

            @Override
            public void onAction1Click(String text) {
                reload();
            }
        });
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

        companyAdapter = new InvCompanyAdapter(getActivity(), null);
        companyAdapter.setOnAdapterListener(new InvCompanyAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TODO,跳转至详情页
                Intent data = new Intent();
                data.putExtra("companyInfo", companyAdapter.getEntity(position));

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
//    @OnClick(R.id.empty_view)
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

        mInvCompanyPresenter.list(mPageInfo, mScanBar.getInputText());
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

            mInvCompanyPresenter.list(mPageInfo, mScanBar.getInputText());
        } else {
            ZLogger.d("加载采购订单，已经是最后一页。");
            onLoadFinished();
        }
    }


    @Override
    public void onIInvCompanyInfoViewProcess() {

        onLoadStart();
    }

    @Override
    public void onIInvCompanyInfoViewError(String errorMsg) {

        onLoadFinished();
    }

    @Override
    public void onIInvCompanyInfoViewSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
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

            ZLogger.d(String.format("加载商品采购订单结束,pageInfo':page=%d/%d(%d/%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                    companyAdapter.getItemCount(), mPageInfo.getTotalCount()));

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载商品采购订单失败: %s", ex.toString()));

            onLoadFinished();
        }
    }
}

package com.manfenjiayuan.pda_wholesaler.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.presenter.TenantPresenter;
import com.manfenjiayuan.business.view.ITenantView;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.ui.adapter.SelectPlatformProviderAdapter;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;


/**
 * 选择门店
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SelectTenantDialog extends CommonDialog
        implements ITenantView {

    private View rootView;
    private RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TextView emptyView;
    private ProgressBar progressBar;

    private SelectPlatformProviderAdapter productAdapter;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private boolean bSyncInProgress = false;//是否正在同步
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private TenantPresenter tenantPresenter;
    private Integer abilityItem = AbilityItem.TENANT;


    public interface OnDialogListener {
        void onItemSelected(CompanyInfo companyInfo);
    }

    private OnDialogListener listener;


    @Override
    public String getNameLike() {
        return null;
    }

    @Override
    public void onProcess() {
        onLoadStart();
    }

    @Override
    public void onError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
        mPageInfo = pageInfo;

        //第一页，缓存数据
        if (mPageInfo.getPageNo() == 1) {
            if (productAdapter != null) {
                productAdapter.setEntityList(dataList);
            }

        } else {
            if (productAdapter != null) {
                productAdapter.appendEntityList(dataList);
            }
        }

        ZLogger.d(String.format("保存关联租户:page=%d/%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                productAdapter.getItemCount(), mPageInfo.getTotalCount()));

        onLoadFinished();
    }


    private SelectTenantDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectTenantDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.include_simple_recyclerview, null);
//        ButterKnife.bind(rootView);


        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.recyclerViewEmptySupport);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgressBar);

        initRecyclerView();

        emptyView.setOnClickListener(dialogClickListener);


        tenantPresenter = new TenantPresenter(this);

        setContent(rootView, 0);
    }

    public SelectTenantDialog(Context context) {
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

        reload();
    }

    private View.OnClickListener dialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_header_close: {
                    dismiss();
                }
                break;
                case R.id.empty_view: {
                    reload();
                }
                break;
            }
        }
    };

    public void init(Integer abilityItem, OnDialogListener listener) {
        this.abilityItem = abilityItem;
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
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        productAdapter = new SelectPlatformProviderAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new SelectPlatformProviderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismiss();

                if (listener != null) {
                    listener.onItemSelected(productAdapter.getEntity(position));
                }
            }

            @Override
            public void onDataSetChanged() {
            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 开始加载
     */
    private void onLoadStart() {
        isLoadingMore = true;
        bSyncInProgress = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 加载完成
     */
    private void onLoadFinished() {
        bSyncInProgress = false;
        isLoadingMore = false;
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 重新加载数据
     */
    public void reload() {

        if (bSyncInProgress) {
            ZLogger.d("正在加载关联租户。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载关联租户。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
//        onLoadStart();
//        load(mPageInfo);
        tenantPresenter.getTenants(mPageInfo, getNameLike(), abilityItem);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载关联租户。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载关联租户。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

//            onLoadStart();
//            load(mPageInfo);
            tenantPresenter.getTenants(mPageInfo, getNameLike(), abilityItem);
        } else {
            ZLogger.d("加载关联租户，已经是最后一页。");
            onLoadFinished();
        }
    }

}

package com.manfenjiayuan.mixicook_vip.ui.location;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Subdis;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.companyInfo.CompanyInfoPresenter;
import com.mfh.framework.api.companyInfo.ICompanyInfoView;
import com.mfh.framework.core.location.MfLocationManagerProxy;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 定位
 * Created by bingshanguxue on 6/28/16.
 */
public class LocationFragment extends BaseListFragment<Subdis>
        implements ICompanyInfoView {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_current_pos)
    TextView tvCurPos;

    @BindView(R.id.location_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private CompanyInfoAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @BindView(R.id.empty_view)
    View emptyView;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 40;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private CompanyInfoPresenter mCompanyInfoPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_location;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        mCompanyInfoPresenter = new CompanyInfoPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        toolbar.setTitle("选择网点");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        initGoodsRecyclerView();

        reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
        mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        goodsListAdapter = new CompanyInfoAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListsner(new CompanyInfoAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onItemClick(View view, int position) {

                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      onLoadFinished();

//                                                      refreshFabShopcart();
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

//    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
//        ZLogger.d(String.format("DataSyncEvent(%d)", event.getEventId()));
//        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO_FRESH) {
//            //刷新供应商
//            readCategoryInfoCache();
//        }
//    }

    @OnClick(R.id.tv_more_address)
    public void redirect2Map(){
        UIHelper.startActivity(getActivity(), PoiActivity.class);
    }

    /**
     * 加载商品列表
     * TODO,加载等待窗口
     */
    @OnClick(R.id.empty_view)
    @Override
    public void reload() {
        super.reload();

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

        onLoadStart();
        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        mCompanyInfoPresenter.findServicedNetsForUserPos(null,
                MfLocationManagerProxy.getLastLongitude(getActivity()),
                MfLocationManagerProxy.getLastLatitude(getActivity()), mPageInfo);
//        mSubdisPresenter.findArroundSubdist(MfLocationManagerProxy.getLastLongitude(getActivity()),
//                MfLocationManagerProxy.getLastLatitude(getActivity()), mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    @Override
    public void loadMore() {
        super.loadMore();

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            onLoadStart();

            mCompanyInfoPresenter.findServicedNetsForUserPos(null,
                    MfLocationManagerProxy.getLastLongitude(getActivity()),
                    MfLocationManagerProxy.getLastLatitude(getActivity()), mPageInfo);
//            mSubdisPresenter.findArroundSubdist(MfLocationManagerProxy.getLastLongitude(getActivity()),
//                    MfLocationManagerProxy.getLastLatitude(getActivity()), mPageInfo);
        } else {
            ZLogger.d("加载商品列表，已经是最后一页。");
            onLoadFinished();
        }
    }

    @Override
    public void onLoadStart() {
        super.onLoadStart();
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        hideProgressDialog();
    }


    @Override
    public void onICompanyInfoViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在获取定位数据..." , false);

    }

    @Override
    public void onICompanyInfoViewError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onICompanyInfoViewSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
        mPageInfo = pageInfo;
        //第一页，清空数据
        if (mPageInfo.getPageNo() == 1) {
            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(dataList);
            }
        } else {
            if (goodsListAdapter != null) {
                goodsListAdapter.appendEntityList(dataList);
            }
        }

        ZLogger.d(String.format("保存采购商品,pageInfo':page=%d %d／%d",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(), mPageInfo.getTotalCount()));

        onLoadFinished();
    }
}

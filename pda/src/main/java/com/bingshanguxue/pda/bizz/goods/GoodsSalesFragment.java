package com.bingshanguxue.pda.bizz.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bingshanguxue.pda.R;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.ProductAggDate;
import com.mfh.framework.api.ScApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * 商品－－销量
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsSalesFragment extends BaseListFragment<ProductAggDate> {

//    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport salesRecyclerView;
    private GoodsSalesAdapter goodsAdapter;
    private LinearLayoutManager linearLayoutManager;
//    @Bind(R.id.animProgress)
    ProgressBar progressBar;
//    @Bind(R.id.empty_view)
    View emptyView;

    private Long proSkuId = null;

    public static GoodsSalesFragment newInstance(Bundle args) {
        GoodsSalesFragment fragment = new GoodsSalesFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_sales;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        salesRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.goods_list);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        emptyView = rootView.findViewById(R.id.empty_view);

        initRecyclerView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 验证
     */
    public void onEventMainThread(ScGoodsSkuEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ScGoodsSkuEvent(%d)", eventId));
        switch (eventId) {
            case ScGoodsSkuEvent.EVENT_ID_SKU_UPDATE: {
                proSkuId = args.getLong(ScGoodsSkuEvent.EXTRA_KEY_PROSKUID);
                reload();
            }
            break;

        }
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        salesRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        salesRecyclerView.setHasFixedSize(true);
        //添加分割线
//        salesRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        salesRecyclerView.setEmptyView(emptyView);
        salesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        goodsAdapter = new GoodsSalesAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new GoodsSalesAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, final int position) {
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
                progressBar.setVisibility(View.GONE);
            }
        });

        salesRecyclerView.setAdapter(goodsAdapter);
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

        goodsAdapter.setEntityList(null);

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, 30);

        load(proSkuId, mPageInfo);
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
            load(proSkuId, mPageInfo);
        } else {
            ZLogger.d("加载销量，已经是最后一页。");
            onLoadFinished();
        }
    }

    private void load(Long proSkuId, PageInfo pageInfo){
        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ProductAggDate>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ProductAggDate> rs) {
                        //此处在主线程中执行。
                        mPageInfo = pageInfo;
                        List<ProductAggDate> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ProductAggDate> wrapper : rs.getRowDatas()) {
                                entityList.add(wrapper.getBean());
                            }
                        }

                        if (mPageInfo.getPageNo() == 1) {
                            if (goodsAdapter != null) {
                                goodsAdapter.setEntityList(entityList);
                            }
                        } else {
                            if (entityList.size() > 0) {
                                if (goodsAdapter != null) {
                                    goodsAdapter.appendEntityList(entityList);
                                }
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载商品销量数据失败:" + errMsg);
                    }
                }, ProductAggDate.class, MfhApplication.getAppContext());


        ScApi.productAggDateList(proSkuId, pageInfo, queryRsCallBack);
    }
    

}

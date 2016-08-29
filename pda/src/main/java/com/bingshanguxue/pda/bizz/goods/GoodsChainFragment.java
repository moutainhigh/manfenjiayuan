package com.bingshanguxue.pda.bizz.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bingshanguxue.pda.R;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * 产品SKU批发商列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsChainFragment extends BaseListFragment<ChainGoodsSku> implements IChainGoodsSkuView {

//    @Bind(R.id.office_list)
    RecyclerViewEmptySupport chainRecyclerView;
    private ChainGoodsSkuAdapter goodsAdapter;
    private LinearLayoutManager linearLayoutManager;
//    @Bind(R.id.animProgress)
    ProgressBar progressBar;
//    @Bind(R.id.empty_view)
    View emptyView;

    private ScGoodsSku curGoods;
    private ChainGoodsSkuPresenter mChainGoodsSkuPresenter;

    public static GoodsChainFragment newInstance(Bundle args) {
        GoodsChainFragment fragment = new GoodsChainFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


//    @Override
//    protected boolean isResponseBackPressed() {
//        return false;
//    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_chain;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        mChainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            barcode = args.getString(EXTRA_KEY_BARCODE);
//        }

        chainRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.office_list);
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
                curGoods = (ScGoodsSku) args.getSerializable(ScGoodsSkuEvent.EXTRA_KEY_SCGOODSSKU);
                reload();
            }
            break;

        }
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


    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chainRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        chainRecyclerView.setHasFixedSize(true);
        //添加分割线
//        chainRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        chainRecyclerView.setEmptyView(emptyView);
        chainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        goodsAdapter = new ChainGoodsSkuAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new ChainGoodsSkuAdapter.OnAdapterListener() {
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

        chainRecyclerView.setAdapter(goodsAdapter);
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

        reload(curGoods, mPageInfo);
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
            reload(curGoods, mPageInfo);
        } else {
            ZLogger.d("加载销量，已经是最后一页。");
            onLoadFinished();
        }
    }


    private void reload(ScGoodsSku goods, PageInfo pageInfo){
        if (goods != null){
            if (!NetworkUtils.isConnect(getActivity())) {
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            mChainGoodsSkuPresenter.findSupplyChainGoodsSku(goods.getBarcode(), null, null, pageInfo);
        }
        else{
            goodsAdapter.setEntityList(null);
        }
    }


    @Override
    public void onChainGoodsSkuViewProcess() {
        onLoadStart();
    }

    @Override
    public void onChainGoodsSkuViewError(String errorMsg) {
        onLoadFinished();
        goodsAdapter.setEntityList(null);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        onLoadFinished();
        goodsAdapter.setEntityList(dataList);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(ChainGoodsSku data) {
        onLoadFinished();
    }
}

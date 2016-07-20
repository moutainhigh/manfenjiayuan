package com.manfenjiayuan.mixicook_vip.ui.reserve;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.manfenjiayuan.business.bean.CategoryInfo;
import com.manfenjiayuan.business.bean.CategoryOption;
import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.model.PosCategory;
import com.manfenjiayuan.mixicook_vip.utils.ACacheHelper;
import com.manfenjiayuan.mixicook_vip.utils.SharedPreferencesHelper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.api.impl.CateApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class ReserveFragment extends BaseProgressFragment
        implements IChainGoodsSkuView {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.category_list)
    RecyclerView categoryRecyclerView;
    private ReserveSideCategoryAdapter categoryListAdapter;

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private ReserveGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;

    @Bind(R.id.empty_view)
    TextView emptyView;

    private PosCategory mPosCategory;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 40;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private Long TENANT_ID_MIXICOOK = 135799L;//135266L;//

    private ChainGoodsSkuPresenter inventoryGoodsPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_reserve;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        inventoryGoodsPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        toolbar.setTitle("米西厨房");
        initCategoryRecyclerView();
        initGoodsRecyclerView();

        downloadFreshFrontCategory();

        DialogUtil.showHint("预定");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initCategoryRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        categoryRecyclerView.setHasFixedSize(true);
//        //添加分割线
        categoryRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
        categoryListAdapter = new ReserveSideCategoryAdapter(AppContext.getAppContext(), null);
        categoryListAdapter.setOnAdapterListsner(new ReserveSideCategoryAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPosCategory = categoryListAdapter.getCurOption();
                loadGoodsList();
            }

            @Override
            public void onDataSetChanged() {
                mPosCategory = categoryListAdapter.getCurOption();
                loadGoodsList();
            }
        });
        categoryRecyclerView.setAdapter(categoryListAdapter);
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

        goodsListAdapter = new ReserveGoodsAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListsner(new ReserveGoodsAdapter.OnAdapterListener() {
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

    /**
     * 加载前台类目树
     */
    private boolean readCategoryInfoCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_FRONTEND_CATEGORY_FRESH);
        List<PosCategory> cacheData = JSONArray.parseArray(cacheStr, PosCategory.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个前台生鲜商品类目",
                    ACacheHelper.CK_BACKEND_CATEGORY_FRESH, cacheData.size()));
            //取第一个作为根目录
            categoryListAdapter.setEntityList(cacheData);
            return true;
        }

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_FRONTEND_CATEGORYINFO_FRESH_ENABLED, true);

        return false;
    }

    /**
     * 下载私有前台类目
     */
    private void downloadFreshFrontCategory() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            return;
        }

        ZLogger.df("同步前台自定义（私有）类目开始");
        CateApiImpl.comnqueryCategory(CateApi.DOMAIN_TYPE_PROD, CateApi.FRESH,
                CateApi.CATE_POSITION_FRONT,
                1, TENANT_ID_MIXICOOK, customFrontCategoryRespCallback);
    }

    private NetCallBack.NetTaskCallBack customFrontCategoryRespCallback = new NetCallBack.NetTaskCallBack<CategoryInfo,
            NetProcessor.Processor<CategoryInfo>>(
            new NetProcessor.Processor<CategoryInfo>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("加载前台自定义（私有）类目树失败, " + errMsg);
                }

                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        saveFreshFrontendCategoryInfoCache(null);
                        readCategoryInfoCache();
                        return;
                    }
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<CategoryInfo> retValue = (RspBean<CategoryInfo>) rspData;
                    CategoryInfo categoryInfo = retValue.getValue();

                    downloadCustomFrontCategory2(categoryInfo);
                }
            }
            , CategoryInfo.class
            , AppContext.getAppContext()) {
    };

    private void downloadCustomFrontCategory2(CategoryInfo categoryInfo) {
        if (categoryInfo == null) {
            saveFreshFrontendCategoryInfoCache(null);
            readCategoryInfoCache();
            return;
        }

        List<CategoryOption> options = categoryInfo.getOptions();
        if (options == null || options.size() < 1) {
            ZLogger.df("前台自定义（私有）类目为空");
            saveFreshFrontendCategoryInfoCache(null);
            readCategoryInfoCache();
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            return;
        }

        CategoryOption option = options.get(0);
        ZLogger.df(String.format("同步前台自定义（私有）二级类目(%s)开始", option.getValue()));
        NetCallBack.NetTaskCallBack queryRsCallBack = new NetCallBack.NetTaskCallBack<PosCategory,
                NetProcessor.Processor<PosCategory>>(
                new NetProcessor.Processor<PosCategory>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        List<PosCategory> items = new ArrayList<>();
                        if (rspData != null) {
                            RspListBean<PosCategory> retValue = (RspListBean<PosCategory>) rspData;
                            items = retValue.getValue();
                        }

                        saveFreshFrontendCategoryInfoCache(items);

                        readCategoryInfoCache();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("加载前台自定义（私有）二级类目 失败, " + errMsg);
                        readCategoryInfoCache();
                    }
                }
                , PosCategory.class
                , AppContext.getAppContext()) {
        };

        CateApiImpl.listPublicCategory(option.getCode(), queryRsCallBack);
    }

    /**
     * 缓存前台私有类目树
     */
    private void saveFreshFrontendCategoryInfoCache(List<PosCategory> options) {
        ZLogger.df(String.format("保存POS %d个前台自定义（私有）二级类目",
                (options != null ? options.size() : 0)));
        //缓存数据
        JSONArray cacheArrays = new JSONArray();
        if (options != null && options.size() > 0) {
            for (PosCategory option : options) {
                cacheArrays.add(option);
            }
        }
        ACache.get(AppContext.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_FRONTEND_CATEGORY_FRESH, cacheArrays.toJSONString());

        //设置下次不需要自动更新商品类目，可以在收银页面点击同步按钮修改
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_FRONTEND_CATEGORYINFO_FRESH_ENABLED, false);

    }

    /**
     * 加载商品列表
     * TODO,加载等待窗口
     */
    private void loadGoodsList() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

        if (mPosCategory == null) {
            return;
        }
        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        inventoryGoodsPresenter.loadCompanyChainSkuGoods(mPageInfo,
                mPosCategory.getId(), TENANT_ID_MIXICOOK, null);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    private void loadMore() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

//        if (bSyncInProgress) {
//            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
//            return;
//        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            inventoryGoodsPresenter.loadCompanyChainSkuGoods(mPageInfo,
                    mPosCategory.getId(), TENANT_ID_MIXICOOK, null);
        } else {
            ZLogger.d("加载商品列表，已经是最后一页。");
            onLoadFinished();
        }
    }

    @Override
    public void onProcess() {
        onLoadProcess("正在加载商品数据...");
    }

    @Override
    public void onError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
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

        onLoadFinished();
        ZLogger.d(String.format("保存采购商品,pageInfo':page=%d %d／%d",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(), mPageInfo.getTotalCount()));
    }

    @Override
    public void onQueryChainGoodsSku(ChainGoodsSku chainGoodsSku) {

    }
}

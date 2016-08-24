package com.mfh.litecashier.ui.fragment.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.ProductCatalogApi;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration2;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.LocalFrontCategoryGoods;
import com.bingshanguxue.cashier.database.entity.ProductCatalogEntity;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.ui.activity.FragmentActivity;
import com.mfh.litecashier.ui.dialog.FrontCategoryGoodsDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * POS-本地前台类目商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class LocalFrontCategoryGoodsFragment extends BaseListFragment<LocalFrontCategoryGoods> {

    public static final String KEY_CATEGORY_ID = "categoryId";

    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport mRecyclerView;
    @Bind(R.id.empty_view)
    View emptyView;

    GridLayoutManager linearLayoutManager;
    private LocalFrontCategoryGoodsAdapter2 adapter;

    private Long categoryId;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_frontcategory_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, 40);

    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            categoryId = args.getLong(KEY_CATEGORY_ID);
        }

        ZLogger.d("categoryId=" + categoryId);
        initRecyclerView();
        setupSwipeRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(LocalFrontCategoryGoodsEvent event) {
        ZLogger.d(String.format("LocalFrontCategoryGoodsEvent(%d/%s)",
                event.getEventId(), StringUtils.decodeBundle(event.getArgs())));

        Bundle args = event.getArgs();
        Long categoryId = args.getLong(KEY_CATEGORY_ID);

        if (categoryId.compareTo(this.categoryId) != 0) {
            ZLogger.d(String.format("类目编号 %d不对，请忽略", this.categoryId));
            return;
        }
        if (event.getEventId() == LocalFrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA) {
            reload();
        }
    }

    private void initRecyclerView() {
        linearLayoutManager = new GridLayoutManager(getContext(), 5);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
//        mRecyclerView.setWrapperView(mSwipeRefreshLayout);
        //添加分割线
//        mRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        mRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
                ContextCompat.getColor(getActivity(), R.color.mf_dividerColorPrimary), 1f,
                ContextCompat.getColor(getActivity(), R.color.green_select), 0.01f,
                ContextCompat.getColor(getActivity(), R.color.transparent), 0f));
//        mRecyclerView.addItemDecoration(new GridItemDecoration(5, 1, false));

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
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        adapter = new LocalFrontCategoryGoodsAdapter2(CashierApp.getAppContext(), null);
        adapter.setOnAdapterListener(new LocalFrontCategoryGoodsAdapter2.OnAdapterListener() {
            @Override
            public void onDataSetChanged() {
                onLoadFinished();
            }

            @Override
            public void onClickGoods(LocalFrontCategoryGoods goods) {

                Bundle args = new Bundle();
                args.putSerializable("goods", goods);
                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_CASHIER_FRONTCATA_GOODS, args));

            }

            @Override
            public void onLongClickGoods(LocalFrontCategoryGoods goods) {
                modifyGoods(goods);
            }

            @Override
            public void onClickAction() {
                addMoreGoods();
            }
        });

        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 重新加载数据
     */
    public synchronized void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载数据");
//            onLoadFinished();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载类目商品。");
            onLoadFinished();
            return;
        }

        onLoadStart();
        this.entityList.clear();
        if (adapter != null) {
            adapter.setEntityList(null);
        }

        mPageInfo.reset();
        mPageInfo.setPageNo(1);
        //从第一页开始请求，每页最多50条记录
        load(mPageInfo);
    }


    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载类目商品。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载类目商品，已经是最后一页。");
            onLoadFinished();
        }
    }

    private void load(PageInfo pageInfo) {
        String sqlWhere = String.format("paramValueId = '%d'", categoryId);
        List<ProductCatalogEntity> entities = ProductCatalogService.getInstance().queryAll(sqlWhere, pageInfo);
        if (entities == null || entities.size() < 1) {
            ZLogger.d("没有找到该类目关联的商品。");
            onLoadFinished();
            return;
        }
        ZLogger.d(String.format("共找到%d条类目商品关系(%d/%d-%d)", entities.size(),
                pageInfo.getPageNo(), pageInfo.getTotalPage(), pageInfo.getTotalCount()));

        List<LocalFrontCategoryGoods> productEntities = new ArrayList<>();
        for (ProductCatalogEntity entity : entities) {
            String sqlWhere2 = String.format("productId = '%d'", entity.getCataItemId());

            List<PosProductEntity> productEntities1 = PosProductService.get()
                    .queryAllByDesc(sqlWhere2);
            if (productEntities1 != null && productEntities1.size() > 0) {
                ZLogger.d(String.format("找到%d个商品，spuId=%d",
                        productEntities1.size(), entity.getCataItemId()));
                PosProductEntity entity1 = productEntities1.get(0);
                LocalFrontCategoryGoods goods = new LocalFrontCategoryGoods();
                goods.setType(0);
                goods.setId(entity1.getId());
                goods.setProSkuId(entity1.getProSkuId());
                goods.setProductId(entity1.getProductId());
                goods.setBarcode(entity1.getBarcode());
                goods.setName(entity1.getName());
                goods.setProviderId(entity1.getProviderId());
                goods.setCostPrice(entity1.getCostPrice());
                goods.setUnit(entity1.getUnit());
                goods.setPriceType(entity1.getPriceType());
                goods.setProdLineId(entity1.getProdLineId());
                productEntities.add(goods);
            } else {
                ZLogger.d(String.format("没有找到商品，spuId=%d", entity.getCataItemId()));
            }
        }

        if (mPageInfo.getPageNo() == 1) {
            this.entityList.clear();
        }
        this.entityList.addAll(productEntities);

        ZLogger.d(String.format("类目 %d 共有 %d 个商品", categoryId, this.entityList.size()));
        if (adapter != null) {
            adapter.setEntityList(this.entityList);
        }

        onLoadFinished();
    }


    /**
     * 设置刷新
     */
    private void setupSwipeRefresh() {
//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.swiperefresh_color1, R.color.swiperefresh_color2,
                    R.color.swiperefresh_color3, R.color.swiperefresh_color4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mState == STATE_REFRESH) {
                        ZLogger.d("正在刷新");
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

    /**
     * 添加更多商品
     */
    public void addMoreGoods() {
        PosCategoryGodosTempService.getInstance().clear();

        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE,
                FragmentActivity.FT_ADDMORE_LOCALFRONTGOODS);
        extras.putLong(FrontCategoryFragment.EXTRA_CATEGORY_ID_POS, categoryId);
        extras.putLong(FrontCategoryFragment.EXTRA_CATEGORY_ID, CateApi.FRONT_CATEGORY_ID_POS);

        UIHelper.startActivity(getActivity(), FragmentActivity.class, extras);
    }

    private FrontCategoryGoodsDialog mFrontCategoryGoodsDialog = null;

    /**
     * 修改商品
     */
    private void modifyGoods(final LocalFrontCategoryGoods goods) {
        if (goods == null) {
            return;
        }

        if (mFrontCategoryGoodsDialog == null) {
            mFrontCategoryGoodsDialog = new FrontCategoryGoodsDialog(getActivity());
            mFrontCategoryGoodsDialog.setCancelable(true);
            mFrontCategoryGoodsDialog.setCanceledOnTouchOutside(true);
        }
        mFrontCategoryGoodsDialog.initialzie(2, goods.getCostPrice(), "元",
                new FrontCategoryGoodsDialog.OnResponseCallback() {
                    @Override
                    public void onAction1(Double value) {
                        if (ObjectsCompact.equals(value, goods.getCostPrice())) {
                            ZLogger.d("价格没有变化，不需要修改");
                            return;
                        }
                        changePrice(goods, value);
                    }

                    @Override
                    public void onAction2() {
                        deleteGoods(goods);
                    }

                });
        if (!mFrontCategoryGoodsDialog.isShowing()) {
            mFrontCategoryGoodsDialog.show();
        }
    }

    /**
     * 修改零售价
     */
    private void changePrice(final LocalFrontCategoryGoods goods, final Double costPrice) {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                        //{"code":"0","msg":"更新成功!","version":"1","data":""}
//                                RspValue<String> retValue = (RspValue<String>) rspData;
//                                String retStr = retValue.getValue();
//                                ZLogger.d("修改售价成功:" + retStr);

                        DialogUtil.showHint("修改成功");
                        goods.setCostPrice(costPrice);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("修改失败：" + errMsg);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", goods.getId());
        jsonObject.put("costPrice", costPrice);
        jsonObject.put("tenantId", MfhLoginService.get().getSpid());
        InvSkuStoreApiImpl.update(jsonObject.toJSONString(), responseCallback);
    }

    /**
     * 删除商品
     */
    private void deleteGoods(final LocalFrontCategoryGoods goods) {
        ProductCatalogEntity catalogEntity = null;
        String sqlWhere = String.format("cataItemId = '%d'", goods.getProductId());
        List<ProductCatalogEntity> catalogs = ProductCatalogService.getInstance().queryAllBy(sqlWhere);
        if (catalogs != null && catalogs.size() > 0) {
            catalogEntity = catalogs.get(0);
        }

        if (catalogEntity == null) {
            ZLogger.d("没有找到商品对应的类目关系");
            return;
        }

        //回调
        final ProductCatalogEntity finalCatalogEntity = catalogEntity;
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                        //{"code":"0","msg":"更新成功!","version":"1","data":""}
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();
                        ZLogger.d("删除商品成功:" + retStr);

                        ProductCatalogService.getInstance()
                                .deleteById(String.valueOf(finalCatalogEntity.getId()));
                        reload();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("删除商品失败：" + errMsg);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };


        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }
        ProductCatalogApi.delete(catalogEntity.getId(), responseCallback);

    }


}

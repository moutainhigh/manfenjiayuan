package com.mfh.litecashier.ui.fragment.goods.frontend;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.ProductCatalogEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.InvSkuStoreHttpManager;
import com.mfh.framework.rxapi.http.ProductCatalogManager;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.LocalFrontCategoryGoods;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.ui.ActivityRoute;
import com.mfh.litecashier.ui.activity.FragmentActivity;
import com.mfh.litecashier.ui.dialog.ActionDialog;
import com.mfh.litecashier.ui.dialog.FrontCategoryGoodsDialog;
import com.mfh.litecashier.ui.fragment.goods.IImportGoodsView;
import com.mfh.litecashier.ui.fragment.goods.ImportGoodsPresenter;
import com.mfh.litecashier.ui.fragment.goods.LocalFrontCategoryGoodsEvent;
import com.mfh.litecashier.ui.fragment.goods.backend.BackendCategoryGoodsFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * POS-本地前台类目商品
 * Created by bingshanguxue on 15/8/31.
 */
public class FrontendGoodsFragment extends BaseListFragment<LocalFrontCategoryGoods>
        implements IScGoodsSkuView, IImportGoodsView {

    public static final String KEY_CATEGORY_ID = "categoryId";

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport mRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;

    GridLayoutManager linearLayoutManager;
    private FrontendGoodsAdapter adapter;

    private Long categoryId;
    private String tempBarcode;
    private ActionDialog addGoodsDialog = null;
    private NumberInputDialog barcodeInputDialog = null;
    private FrontCategoryGoodsDialog mFrontCategoryGoodsDialog = null;


    private ScGoodsSkuPresenter mScGoodsSkuPresenter;
    private ImportGoodsPresenter mImportGoodsPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, 40);

        mScGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
        mImportGoodsPresenter = new ImportGoodsPresenter(this);
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LocalFrontCategoryGoodsEvent event) {
        ZLogger.d(String.format("LocalFrontCategoryGoodsEvent(%d/%s)",
                event.getEventId(), StringUtils.decodeBundle(event.getArgs())));

        Bundle args = event.getArgs();
        Long categoryId = args.getLong(KEY_CATEGORY_ID);

        if (this.categoryId == null || categoryId.compareTo(this.categoryId) != 0) {
            ZLogger.w(String.format("类目编号 %d不对，请忽略", this.categoryId));
            return;
        }
        ZLogger.d(String.format("类目编号 %d 更新", this.categoryId));

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
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        mRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 0,
//                ContextCompat.getColor(getActivity(), R.color.mf_dividerColorPrimary), 1f,
//                ContextCompat.getColor(getActivity(), R.color.green_select), 0.01f,
//                ContextCompat.getColor(getActivity(), R.color.transparent), 0f));
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

        adapter = new FrontendGoodsAdapter(CashierApp.getAppContext(), null);
        adapter.setOnAdapterListener(new FrontendGoodsAdapter.OnAdapterListener() {
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
            public void onLongClickGoods(int position, LocalFrontCategoryGoods goods) {
                modifyGoods(position, goods);
            }

            @Override
            public void onClickAction() {
                addMoreGoods();
            }
        });

        mRecyclerView.setAdapter(adapter);
        adapter.setEntityList(null);
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

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            load(mPageInfo);
        } else {
            DialogUtil.showHint("已经是最后一页了");
            ZLogger.d("加载类目商品，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 分页查询数据
     */
    private void load(final PageInfo pageInfo) {
        onLoadStart();

        Observable.create(new Observable.OnSubscribe<List<LocalFrontCategoryGoods>>() {
            @Override
            public void call(Subscriber<? super List<LocalFrontCategoryGoods>> subscriber) {
                List<LocalFrontCategoryGoods> productEntities = new ArrayList<>();

                try {
                    String sqlWhere = String.format("paramValueId = '%d'", categoryId);
                    List<ProductCatalogEntity> entities = ProductCatalogService.getInstance().queryAll(sqlWhere, pageInfo);
                    if (entities != null || entities.size() > 0) {
                        ZLogger.d(String.format("共找到%d条类目商品关系(%d/%d-%d)", entities.size(),
                                pageInfo.getPageNo(), pageInfo.getTotalPage(), pageInfo.getTotalCount()));

                        for (ProductCatalogEntity entity : entities) {
//                        String sqlWhere2 = String.format("productId = '%d' and status = '%d'",
//                                entity.getCataItemId(), 1);
                            String sqlWhere2 = String.format("productId = '%d'", entity.getCataItemId());

                            List<PosProductEntity> productEntities1 = PosProductService.get()
                                    .queryAllByDesc(sqlWhere2);
                            if (productEntities1 != null && productEntities1.size() > 0) {
                                ZLogger.d(String.format("找到%d个商品，spuId=%d",
                                        productEntities1.size(), entity.getCataItemId()));
                                productEntities.add(LocalFrontCategoryGoods.create(productEntities1.get(0)));
                            } else {
                                ZLogger.w(String.format("没有找到商品，spuId=%d", entity.getCataItemId()));
                            }
                        }
                    }
                } catch (Throwable ex) {
                    ZLogger.ef(String.format("加载类目商品失败: %s", ex.toString()));
                }

                subscriber.onNext(productEntities);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LocalFrontCategoryGoods>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<LocalFrontCategoryGoods> localFrontCategoryGoodses) {
                        if (mPageInfo.getPageNo() == 1) {
                            entityList.clear();
                        }
                        if (localFrontCategoryGoodses != null) {
                            entityList.addAll(localFrontCategoryGoodses);
                        }
                        ZLogger.d(String.format("类目 %d 共有 %d 个商品", categoryId, entityList.size()));

                        if (adapter != null) {
                            adapter.setEntityList(entityList);
                        }

                        onLoadFinished();
                    }
                });
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

//                    reload();

                    setRefreshing(false);
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
        if (addGoodsDialog == null) {
            addGoodsDialog = new ActionDialog(getActivity());
            addGoodsDialog.setCancelable(true);
            addGoodsDialog.setCanceledOnTouchOutside(true);
        }
        addGoodsDialog.initialize("添加商品", "", R.mipmap.ic_importfromcenterskus,
                new ActionDialog.OnActionClickListener() {
                    @Override
                    public void onAction1() {
                        redirect2FrontCategory();
                    }

                    @Override
                    public void onAction2() {
                        manualAddGoods();
                    }

                    @Override
                    public void onAction3() {

                    }
                });
        addGoodsDialog.setActions("商品库", "输入商品编号", null);
        if (!addGoodsDialog.isShowing()) {
            addGoodsDialog.show();
        }
    }

    /**
     * 跳转到前台类目商品库
     */
    private void redirect2FrontCategory() {
        PosCategoryGodosTempService.getInstance().clear();

        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE, FragmentActivity.FT_ADDMORE_LOCALFRONTGOODS);
        extras.putLong(BackendCategoryGoodsFragment.EXTRA_CATEGORY_ID_POS, categoryId);
        UIHelper.startActivity(getActivity(), FragmentActivity.class, extras);
    }

    /**
     * 输入商品编号
     */
    private void manualAddGoods() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getActivity());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.BARCODE, "导入商品", "商品条码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
//                        inlvBarcode.setInputString(value);
//                        查询平台商品档案，如果有则导入到前台类目，没有则跳转到商品建档页面
                        tempBarcode = value;
                        if (!StringUtils.isEmpty(value)) {
                            mScGoodsSkuPresenter.getByBarcode(value);
                        }
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    @Override
    public void onIScGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        hideProgressDialog();
        ZLogger.df(errorMsg);
        DialogUtil.showHint("加载商品信息失败");
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {

    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku data) {
        if (data == null) {
            hideProgressDialog();

            ActivityRoute.redirect2StoreIn(getActivity(), tempBarcode);
        } else {
            ZLogger.d("查询成功，准备导入商品到类目中");
            if (mImportGoodsPresenter != null) {
                mImportGoodsPresenter.importFromCenterSkus(categoryId, String.valueOf(data.getProductId()), String.valueOf(data.getProSkuId()));
            } else {
                hideProgressDialog();
            }
//            importFromCenterSkus(String.valueOf(data.getProductId()), String.valueOf(data.getProSkuId()));
        }
    }

    /**
     * 建档
     */
    private void importFromCenterSkus(final String productIds, String proSkuIds) {
        Map<String, String> options = new HashMap<>();
        options.put("proSkuIds", proSkuIds);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().importFromCenterSkus(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("导入商品到本店仓储失败, " + e.toString());
                DialogUtil.showHint("添加商品失败");
                hideProgressDialog();
            }

            @Override
            public void onNext(String s) {
                ZLogger.d("导入商品到本店仓储成功");
                add2Category(productIds);
            }

        });
    }

    /**
     * 导入类目
     */
    private void add2Category(String productIds) {
        Map<String, String> options = new HashMap<>();
        options.put("groupIds", String.valueOf(categoryId));
        options.put("productIds", productIds);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ProductCatalogManager.getInstance().addToCatalog(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("导入前台类目商品失败, " + e.toString());
                DialogUtil.showHint("添加商品失败");
                hideProgressDialog();
            }

            @Override
            public void onNext(String s) {
                ZLogger.d("导入前台类目商品成功");
                hideProgressDialog();
            }

        });
    }

    /**
     * 修改商品
     */
    private void modifyGoods(final int position, final LocalFrontCategoryGoods goods) {
        if (goods == null) {
            return;
        }

        Double costPrice = goods.getCostPrice() != null ? goods.getCostPrice() : 0D;
        if (mFrontCategoryGoodsDialog == null) {
            mFrontCategoryGoodsDialog = new FrontCategoryGoodsDialog(getActivity());
            mFrontCategoryGoodsDialog.setCancelable(true);
            mFrontCategoryGoodsDialog.setCanceledOnTouchOutside(true);
        }
        mFrontCategoryGoodsDialog.initialzie(2, costPrice, "元",
                new FrontCategoryGoodsDialog.OnResponseCallback() {
                    @Override
                    public void onAction1(Double value) {
//                        if (ObjectsCompact.equals(value, costPrice)) {
//                            ZLogger.d("价格没有变化，不需要修改");
//                            return;
//                        }
                        changePrice(position, goods, value);
                    }

                    @Override
                    public void onAction2() {
                        deleteGoods(position, goods);
                    }

                    @Override
                    public void onAction3() {
                        updateStatus(position, goods);
                    }

                });
        if (goods.getStatus() == 1) {
            mFrontCategoryGoodsDialog.setAction3("售罄");
        } else {
            mFrontCategoryGoodsDialog.setAction3("补货");
        }
        if (!mFrontCategoryGoodsDialog.isShowing()) {
            mFrontCategoryGoodsDialog.show();
        }
    }

    /**
     * 修改零售价
     */
    private void changePrice(final int position, final LocalFrontCategoryGoods goods, final Double costPrice) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }


        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", goods.getId());
        jsonObject.put("costPrice", costPrice);
        jsonObject.put("tenantId", MfhLoginService.get().getSpid());

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonObject.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().update(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("修改零售价失败, " + e.toString());
                DialogUtil.showHint("修改零售价失败");
                hideProgressDialog();
            }

            @Override
            public void onNext(String s) {
                DialogUtil.showHint("修改零售价成功");
                goods.setCostPrice(costPrice);
//                        adapter.notifyDataSetChanged();
                adapter.notifyItemChanged(position);

                hideProgressDialog();
            }

        });

    }

    /**
     * 删除商品
     */
    private void deleteGoods(final int position, final LocalFrontCategoryGoods goods) {
        if (goods == null) {
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        //查询商品所属前台类目
        ProductCatalogEntity catalogEntity = null;
        String sqlWhere = String.format("cataItemId = '%d'", goods.getProductId());
        List<ProductCatalogEntity> catalogs = ProductCatalogService.getInstance().queryAllBy(sqlWhere);
        if (catalogs != null && catalogs.size() > 0) {
            catalogEntity = catalogs.get(0);
        }
        if (catalogEntity == null) {
            ZLogger.d("删除商品失败，没有找到商品对应的类目关系");
            return;
        }


        //删除该前台类目下的商品
        final ProductCatalogEntity finalCatalogEntity = catalogEntity;
        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(catalogEntity.getId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ProductCatalogManager.getInstance().delete(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("删除商品失败, " + e.toString());
                DialogUtil.showHint("删除商品失败");
                hideProgressDialog();
            }

            @Override
            public void onNext(String s) {
                DialogUtil.showHint("删除商品成功");
                ProductCatalogService.getInstance()
                        .deleteById(String.valueOf(finalCatalogEntity.getId()));
                reload();

                hideProgressDialog();
            }

        });
    }


    /**
     * 更新商品
     */
    private void updateStatus(final int position, final LocalFrontCategoryGoods goods) {
        if (goods == null) {
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }


        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        int newStatus = 0;
        if (goods.getStatus() == 0) {
            newStatus = 1;
        }

        final int finalNewStatus = newStatus;
        Map<String, String> options = new HashMap<>();
        options.put("status", String.valueOf(newStatus));
        options.put("barcode", goods.getBarcode());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().updateStatus(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.d("更新商品失败, " + e.toString());
                DialogUtil.showHint("更新商品失败");
                hideProgressDialog();
            }

            @Override
            public void onNext(String s) {
                DialogUtil.showHint("更新商品成功");
                //修改门店商品状态后台不推送消息，所以这里直接本地修改，下次同步数据时再去更新。
                goods.setStatus(finalNewStatus);
                PosProductService.get().saveOrUpdate(goods);
                adapter.notifyItemChanged(position);

                hideProgressDialog();
            }

        });
    }

    @Override
    public void onIImportGoodsViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

    }

    @Override
    public void onIImportGoodsViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

    }

    @Override
    public void onIImportGoodsViewSuccess() {
        showProgressDialog(ProgressDialog.STATUS_DONE, "导入商品成功", true);
//        hideProgressDialog();
    }
}

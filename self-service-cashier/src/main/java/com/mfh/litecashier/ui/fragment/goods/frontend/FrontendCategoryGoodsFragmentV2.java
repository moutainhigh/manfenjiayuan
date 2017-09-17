package com.mfh.litecashier.ui.fragment.goods.frontend;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.ProductCatalogEntity;
import com.bingshanguxue.cashier.database.service.PosLocalCategoryService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.mvp.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.mvp.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.category.ScCategoryInfoApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.InvSkuStoreHttpManager;
import com.mfh.framework.rxapi.http.ProductCatalogManager;
import com.mfh.framework.rxapi.http.ScCategoryInfoHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.LocalFrontCategoryGoods;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.service.DataDownloadManager;
import com.mfh.litecashier.ui.ActivityRoute;
import com.mfh.litecashier.ui.activity.FragmentActivity;
import com.mfh.litecashier.ui.dialog.ActionDialog;
import com.mfh.litecashier.ui.dialog.FrontCategoryGoodsDialog;
import com.mfh.litecashier.ui.dialog.ModifyLocalCategoryDialog;
import com.mfh.litecashier.ui.dialog.TextInputDialog;
import com.mfh.litecashier.ui.fragment.goods.IImportGoodsView;
import com.mfh.litecashier.ui.fragment.goods.ImportGoodsPresenter;
import com.mfh.litecashier.ui.fragment.goods.backend.BackendCategoryGoodsFragment;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

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
 * POS-前台类目
 * Created by bingshanguxue on 15/8/30.
 */
public class FrontendCategoryGoodsFragmentV2 extends BaseListFragment<LocalFrontCategoryGoods>
        implements IScGoodsSkuView, IImportGoodsView {
    @BindView(R.id.category_list)
    RecyclerViewEmptySupport categoryRecyclerView;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;

    private FrontendCategoryAdapterV2 mCategoryAdapter;
    GridLayoutManager goodsLayoutManager;
    private FrontendGoodsAdapter mGoodsAdapter;


    private PosLocalCategoryEntity curCategoryEntity;//当前子类目
    private String tempBarcode;

    private ActionDialog addGoodsDialog = null;
    private NumberInputDialog barcodeInputDialog = null;
    private FrontCategoryGoodsDialog mFrontCategoryGoodsDialog = null;
    private ModifyLocalCategoryDialog mModifyLocalCategoryDialog = null;
    private TextInputDialog mTextInputDialog = null;

    private ScGoodsSkuPresenter mScGoodsSkuPresenter;
    private ImportGoodsPresenter mImportGoodsPresenter;


    public static FrontendCategoryGoodsFragmentV2 newInstance(Bundle args) {
        FrontendCategoryGoodsFragmentV2 fragment = new FrontendCategoryGoodsFragmentV2();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_local_frontcategory_v2;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initCategoryRecyclerView();
        initGoodsRecyclerView();

        mScGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
        mImportGoodsPresenter = new ImportGoodsPresenter(this);
        mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, 40);

        reload();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reloadGoods();
            }
        }, 1000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void reload() {
        super.reload();
        reloadCategory();
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataDownloadManager.DataDownloadEvent event) {
        ZLogger.d(String.format("DataDownloadEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataDownloadManager.DataDownloadEvent.EVENT_FRONTEND_CATEGORY_UPDATED) {
            reload();
            reloadGoods();
        } else if (event.getEventId() == DataDownloadManager.DataDownloadEvent.EVENT_PRODUCT_CATALOG_UPDATED) {
            reloadGoods();
        }
    }

    private void reloadCategory() {
        try {
//            Long oldCategoryId = null;
//            PosLocalCategoryEntity oldCategory = mCategoryAdapter.getCurEntity();
//            int oldIndex = mCategoryGoodsTabStrip.getCurrentPosition();
//            ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(oldIndex);
//            if (viewPageInfo != null) {
//                oldCategoryId = viewPageInfo.args.getLong(FrontendGoodsFragment.KEY_CATEGORY_ID);
//            }
//            ZLogger.d(String.format("old id=%d, index=%d", oldCategoryId, oldIndex));
            List<PosLocalCategoryEntity> categoryEntities = PosLocalCategoryService.get().queryAll(null, null);
            mCategoryAdapter.setEntityList(categoryEntities);

//            ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
//            for (PosLocalCategoryEntity category : categoryEntities) {
//                Bundle args = new Bundle();
//                args.putLong(FrontendGoodsFragment.KEY_CATEGORY_ID, category.getId());
//
//                mTabs.add(new ViewPageInfo(category.getName(), category.getName(),
//                        FrontendGoodsFragment.class, args));
////            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
//            }
//            categoryGoodsPagerAdapter.removeAll();
//            categoryGoodsPagerAdapter.addAllTab(mTabs);
//
//            int tabCount = categoryGoodsPagerAdapter.getCount();
//            int newIndex = 0;
//            Long newCategoryId = null;
//            for (int i = 0; i < tabCount; i++) {
//                ViewPageInfo tab = categoryGoodsPagerAdapter.getTab(i);
//                if (tab != null) {
//                    Long categoryId = tab.args.getLong(FrontendGoodsFragment.KEY_CATEGORY_ID);
//                    ZLogger.d(String.format("check id=%d, index=%d", categoryId, i));
//                    if (ObjectsCompact.equals(oldCategoryId, categoryId)) {
//                        newCategoryId = categoryId;
//                        newIndex = i;
//                        break;
//                    }
//                }
//            }
//            ZLogger.d(String.format("new id=%d, index=%d", newCategoryId, newIndex));
//
//            mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
//            mCategoryGoodsViewPager.setCurrentItem(newIndex, false);
//
//            notifyDataChanged(newIndex);
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }


    private void initCategoryRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 5);
        categoryRecyclerView.setLayoutManager(gridLayoutManager);

        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        categoryRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        categoryRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置列表为空时显示的视图
        categoryRecyclerView.setEmptyView(emptyView);
//        mRecyclerView.setWrapperView(mSwipeRefreshLayout);
        //添加分割线
//        categoryRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        mCategoryAdapter = new FrontendCategoryAdapterV2(CashierApp.getAppContext(), null);
        mCategoryAdapter.setOnAdapterListener(new FrontendCategoryAdapterV2.OnAdapterListener() {
            @Override
            public void onDataSetChanged() {
                curCategoryEntity = mCategoryAdapter.getCurEntity();
            }

            @Override
            public void onCategoryClick(PosLocalCategoryEntity categoryEntity) {
                curCategoryEntity = categoryEntity;
                reloadGoods();
            }

            @Override
            public void onCategoryLongclick(PosLocalCategoryEntity categoryEntity) {
                updateCategoryInfo(categoryEntity);
            }

            @Override
            public void onClickAction() {
                addCategory();
            }
        });

        categoryRecyclerView.setAdapter(mCategoryAdapter);
        mCategoryAdapter.setEntityList(null);
    }

    private void initGoodsRecyclerView() {
        goodsLayoutManager = new GridLayoutManager(getContext(), 5);
        goodsRecyclerView.setLayoutManager(goodsLayoutManager);

        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        goodsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
//        mRecyclerView.setWrapperView(mSwipeRefreshLayout);
        //添加分割线
//        mRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        goodsRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        mRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 0,
//                ContextCompat.getColor(getActivity(), R.color.mf_dividerColorPrimary), 1f,
//                ContextCompat.getColor(getActivity(), R.color.green_select), 0.01f,
//                ContextCompat.getColor(getActivity(), R.color.transparent), 0f));
//        mRecyclerView.addItemDecoration(new GridItemDecoration(5, 1, false));

        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = goodsLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = goodsLayoutManager.getItemCount();
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

        mGoodsAdapter = new FrontendGoodsAdapter(CashierApp.getAppContext(), null);
        mGoodsAdapter.setOnAdapterListener(new FrontendGoodsAdapter.OnAdapterListener() {
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

        goodsRecyclerView.setAdapter(mGoodsAdapter);
        mGoodsAdapter.setEntityList(null);
    }


    /**
     * 新增类目
     */
    public void addCategory() {
        ZLogger.d("新增类目");
        final Long parentId = SharedPreferencesUltimate.getLong(SharedPreferencesUltimate.PK_L_CATETYPE_POS_ID, 0L);
        if (parentId.equals(0L)) {
            DialogUtil.showHint("请先创建根目录");
            return;
        }

        if (mTextInputDialog == null) {
            mTextInputDialog = new TextInputDialog(getActivity());
            mTextInputDialog.setCancelable(false);
            mTextInputDialog.setCanceledOnTouchOutside(false);
        }
        mTextInputDialog.initialize("添加栏目", "请输入栏目名称", false,
                new TextInputDialog.OnTextInputListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm(String text) {
                        createCategoryInfo(parentId, text);
                    }
                });
        if (!mTextInputDialog.isShowing()) {
            mTextInputDialog.show();
        }
    }

    /**
     * 创建前台类目
     */
    private void createCategoryInfo(Long parentId, final String nameCn) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("kind", "code");
        jsonObject.put("domain", String.valueOf(CateApi.DOMAIN_TYPE_PROD));
        jsonObject.put("nameCn", nameCn);
        jsonObject.put("catePosition", String.valueOf(CateApi.CATE_POSITION_FRONT));
        jsonObject.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
//        jsonObject.put("cateType", String.valueOf(CateApi.POS));
        jsonObject.put("parentId", parentId);

        ScCategoryInfoHttpManager.getInstance().create(MfhLoginService.get().getCurrentSessionId(),
                jsonObject, new MValueSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e("创建前台类目失败, " + e.getMessage());
                        hideProgressDialog();
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);

                        ZLogger.d("新建前台类目成功:" + data);
                        hideProgressDialog();
                    }

                });
    }


    /**
     * 修改类目名称/删除类目
     */
    private void updateCategoryInfo(PosLocalCategoryEntity categoryEntity) {
        if (mModifyLocalCategoryDialog == null) {
            mModifyLocalCategoryDialog = new ModifyLocalCategoryDialog(getActivity());
            mModifyLocalCategoryDialog.setCanceledOnTouchOutside(true);
            mModifyLocalCategoryDialog.setCancelable(false);
        }

        mModifyLocalCategoryDialog.init(categoryEntity, categoryEntity.getName(),
                new ModifyLocalCategoryDialog.DialogListener() {
                    @Override
                    public void onUpdate(PosLocalCategoryEntity categoryEntity, String nameCn) {
                        doUpdate(categoryEntity, nameCn);
                    }

                    @Override
                    public void onDelete(PosLocalCategoryEntity categoryEntity) {
                        doDelete(categoryEntity);
                    }
                });

        if (!mModifyLocalCategoryDialog.isShowing()) {
            mModifyLocalCategoryDialog.show();
        }
    }

    /**
     * 修改栏目名称
     */
    private void doUpdate(final PosLocalCategoryEntity categoryEntity, final String nameCn) {
        NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.e("创建前台类目失败, " + errMsg);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"删除成功!","version":"1","data":""}
                        //新建类目成功，保存类目信息，并触发同步。
                        try {
//                            if (rspData == null) {
//                                return;
//                            }
//
//                            RspValue<String> retValue = (RspValue<String>) rspData;
//                            String result = retValue.getValue();
//                            Long code = Long.valueOf(result);

                            //本地先假修改，后台数据更新后再去同步
                            categoryEntity.setName(nameCn);
                            PosLocalCategoryService.get().saveOrUpdate(categoryEntity);
                            DialogUtil.showHint("修改成功");

                            reload();

                            //删除或修改类目成功客户端主动去同步数据
//                            DataDownloadManager.get().sync(DataDownloadManager.FRONTENDCATEGORY);

                            hideProgressDialog();

                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        if (categoryEntity == null) {
            DialogUtil.showHint("类目无效");
            return;
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", categoryEntity.getId());
        jsonObject.put("nameCn", nameCn);
        jsonObject.put("catePosition", CateApi.CATE_POSITION_FRONT);
        jsonObject.put("tenantId", MfhLoginService.get().getSpid());
        ScCategoryInfoApi.update(jsonObject.toJSONString(), responseRC);
    }

    /**
     * 删除栏目
     */
    private void doDelete(final PosLocalCategoryEntity categoryEntity) {
        if (categoryEntity == null) {
            DialogUtil.showHint("类目无效");
            return;
        }

        NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.e("创建前台类目失败, " + errMsg);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"删除成功!","version":"1","data":""}
                        //新建类目成功，保存类目信息，并触发同步。
                        try {
//                            if (rspData == null) {
//                                return;
//                            }
//
//                            RspValue<String> retValue = (RspValue<String>) rspData;
//                            String result = retValue.getValue();
//                            Long code = Long.valueOf(result);

                            //本地假删除
                            PosLocalCategoryService.get().deleteById(String.valueOf(categoryEntity.getId()));
                            DialogUtil.showHint("删除成功");
                            reload();

                            //删除或修改类目成功客户端主动去同步数据
//                            DataDownloadManager.get().sync(DataDownloadManager.FRONTENDCATEGORY);
                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
                        hideProgressDialog();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);


        ScCategoryInfoApi.delete(categoryEntity.getId(), responseRC);
    }

    public synchronized void initGoods() {


    }

    /**
     * 重新加载数据
     */
    public synchronized void reloadGoods() {
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
                    if (curCategoryEntity != null) {
                        String sqlWhere = String.format("paramValueId = '%d'",
                                mCategoryAdapter.getCurEntity().getId());
                        List<ProductCatalogEntity> entities = ProductCatalogService.getInstance().queryAll(sqlWhere, pageInfo);
                        if (entities != null && entities.size() > 0) {
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
                        ZLogger.d(String.format("共有 %d 个类目商品", entityList.size()));

                        if (mGoodsAdapter != null) {
                            mGoodsAdapter.setEntityList(entityList);
                        }

                        onLoadFinished();
                    }
                });
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
                ZLogger.e("修改零售价失败, " + e.toString());
                DialogUtil.showHint("修改零售价失败");
                hideProgressDialog();
            }

            @Override
            public void onNext(String s) {
                DialogUtil.showHint("修改零售价成功");
                goods.setCostPrice(costPrice);
//                        adapter.notifyDataSetChanged();
                mGoodsAdapter.notifyItemChanged(position);

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
            hideProgressDialog();
            return;
        }

        //删除该前台类目下的商品--删除商品关系表
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
                ZLogger.e("删除商品失败, " + e.toString());
                DialogUtil.showHint("删除商品失败");
                hideProgressDialog();
            }

            @Override
            public void onNext(String s) {
                DialogUtil.showHint("删除商品成功");
                ProductCatalogService.getInstance()
                        .deleteById(String.valueOf(finalCatalogEntity.getId()));
//                reload();
                mGoodsAdapter.notifyDataSetChanged();

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
                mGoodsAdapter.notifyItemChanged(position);

                hideProgressDialog();
            }

        });
    }


    /**
     * 跳转到前台类目商品库
     */
    private void redirect2FrontCategory() {
        PosCategoryGodosTempService.getInstance().clear();

        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE, FragmentActivity.FT_ADDMORE_LOCALFRONTGOODS);
        extras.putLong(BackendCategoryGoodsFragment.EXTRA_CATEGORY_ID_POS, mCategoryAdapter.getCurEntity().getId());
        UIHelper.startActivity(getActivity(), FragmentActivity.class, extras);
    }

    @Override
    public void onIScGoodsSkuViewProcess() {

    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {

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
                mImportGoodsPresenter.importFromCenterSkus(curCategoryEntity.getId(), String.valueOf(data.getProductId()), String.valueOf(data.getProSkuId()));
            } else {
                hideProgressDialog();
            }
//            importFromCenterSkus(String.valueOf(data.getProductId()), String.valueOf(data.getProSkuId()));
        }
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
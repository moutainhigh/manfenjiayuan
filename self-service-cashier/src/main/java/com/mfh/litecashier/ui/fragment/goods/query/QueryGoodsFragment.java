package com.mfh.litecashier.ui.fragment.goods.query;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.ProductCatalogEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.InvSkuStoreHttpManager;
import com.mfh.framework.rxapi.http.ProductCatalogManager;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.dialog.FrontCategoryGoodsDialog;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * 商品搜索
 * <p>
 * Created by bingshanguxue on 18/5/31.
 */
public class QueryGoodsFragment extends BaseListFragment<PosProductEntity> {

    @BindView(R.id.goodsRecyclerView)
    RecyclerViewEmptySupport goodsRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    private GridLayoutManager linearLayoutManager;
    private QueryGoodsAdapter goodsListAdapter;

    @BindView(R.id.rl_keyboard)
    RelativeLayout rlKeyboard;
    @BindView(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;

    @BindView(R.id.letterRecyclerView)
    RecyclerViewEmptySupport letterRecyclerView;
    private LetterAdapter mLetterAdapter;

    @BindView(R.id.fab_toggle)
    ImageButton ibToggle;
    @BindView(R.id.animProgress)
    ProgressBar progressBar;


    private String keyword;
    private FrontCategoryGoodsDialog mFrontCategoryGoodsDialog = null;


    public interface OnFragmentListener {
        void onAddGoods(PosProductEntity productEntity);
    }

    private OnFragmentListener mOnFragmentListener;

    public void setOnFragmentListener(OnFragmentListener fragmentListener) {
        mOnFragmentListener = fragmentListener;
    }

    public static QueryGoodsFragment newInstance(Bundle args, OnFragmentListener onFragmentListener) {
        QueryGoodsFragment fragment = new QueryGoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
            fragment.setOnFragmentListener(onFragmentListener);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_query;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initGoodsRecyclerView();
        InputNumberLabelView();
        initLetterRecyclerView();

        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (goodsRecyclerView != null) {
            goodsRecyclerView.removeOnScrollListener(orderListScrollListener);
        }
    }

    private void InputNumberLabelView() {
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            //条码枪扫描结束后会自动触发回车键
                            keyword = inlvBarcode.getInputString();
                            reload();
                        }

                    }
                });
        inlvBarcode.registerOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onClickAction1(String text) {
                hideKeyboard();
            }

            @Override
            public void onLongClickAction1(String text) {

            }
        });
//        inlvBarcode.requestFocusEnd();
//        inlvBarcode.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
//                            || inlvBarcode.isSoftKeyboardEnabled()) {
////                        showBarcodeKeyboard();
//                    }
//                }
//
//                inlvBarcode.requestFocusEnd();
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
    }

    /**
     * 初始化商品列表
     */
    private void initGoodsRecyclerView() {
        linearLayoutManager = new GridLayoutManager(getActivity(), 5,
                GridLayoutManager.VERTICAL, false);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
//        letterRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.HORIZONTAL_LIST));
        goodsRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        goodsRecyclerView.addOnScrollListener(orderListScrollListener);

        goodsListAdapter = new QueryGoodsAdapter(getActivity(), null);
        goodsListAdapter.setOnAdapterLitener(new QueryGoodsAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //加入购物车
                PosProductEntity productEntity = goodsListAdapter.getEntity(position);
                if (productEntity != null && mOnFragmentListener != null) {
                    mOnFragmentListener.onAddGoods(productEntity);
                }
                toggleKeyboard();
            }

            @Override
            public void onItemLongClick(int position, PosProductEntity goods) {
                modifyGoods(position, goods);
            }
        });

        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    private RecyclerView.OnScrollListener orderListScrollListener = new RecyclerView.OnScrollListener() {
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
            if (lastVisibleItem >= totalItemCount - 4 && dx > 0) {
                if (!isLoadingMore) {
                    onLoadFinished();
//                    loadMore();
                }
            } else if (dy < 0) {
                isLoadingMore = false;
            }

            if (dy > 0) {
                if (rlKeyboard.getVisibility() == View.VISIBLE) {
                    hideKeyboard();
                }
            }
        }
    };

    private void initLetterRecyclerView() {
        GridLayoutManager mRLayoutManager = new GridLayoutManager(getActivity(), 6);
        letterRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        letterRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        letterRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        letterRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        mLetterAdapter = new LetterAdapter(CashierApp.getAppContext(), null);
        mLetterAdapter.setOnAdapterLitener(new LetterAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Letter entity = mLetterAdapter.getEntity(position);
                if (entity != null) {
//                    DialogUtil.showHint(String.format("选中%s(%s)",
//                            entity.getKey(), entity.getValue()));
                    inlvBarcode.appendInput(entity.getValue());

                }
            }
        });
        letterRecyclerView.setAdapter(mLetterAdapter);

        List<Letter> letters = new ArrayList<>();
        for (String az : Letter.A_Z_0_9) {
            Letter letter = new Letter();
            letter.setKey(az);
            letter.setValue(az);
            letters.add(letter);

        }
        mLetterAdapter.setEntityList(letters);
    }

    @OnClick(R.id.ib_enter)
    public void search() {
        keyword = inlvBarcode.getInputString();
        reload();
    }

    @OnClick(R.id.fab_toggle)
    public void toggleKeyboard() {
        ibToggle.setVisibility(View.GONE);
        rlKeyboard.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        ibToggle.setVisibility(View.VISIBLE);
        rlKeyboard.setVisibility(View.GONE);
    }

    private void refresh() {
//        spinnerTenant.setSelection(0);
//        spinnerStatus.setSelection(0);
//        spinnerCatetype.setSelection(0);
//        insvOrderBarcode.clear();
//        insvOrderBarcode.requestFocus();
    }

    public synchronized void reload() {
        inlvBarcode.clear();
        inlvBarcode.requestFocusEnd();

        if (bSyncInProgress) {
            ZLogger.d("正在加载本地商品库。");
//            onLoadFinished();
            return;
        }

        goodsListAdapter.setEntityList(null);
        onLoadStart();
//        mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
        mPageInfo.reset();
        mPageInfo.setPageNo(1);
//        load(keyword, mPageInfo);
        load(keyword, null);
    }


    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载本地商品库。");
//            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(keyword, mPageInfo);
        } else {
            DialogUtil.showHint("已经是最后一页了");
            ZLogger.d("加载本地商品库，已经是最后一页。");
            onLoadFinished();
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

    /**
     * 加载数据
     * */
    /**
     * 分页查询数据
     */
    private void load(final String abbreviation, final PageInfo pageInfo) {
        onLoadStart();

        Observable.create(new Observable.OnSubscribe<List<PosProductEntity>>() {
            @Override
            public void call(Subscriber<? super List<PosProductEntity>> subscriber) {
                List<PosProductEntity> productEntities = new ArrayList<>();

                try {
                    String sqlWhere = String.format("abbreviation like '%%%s%%'", abbreviation);
                    productEntities = PosProductService.get()
                            .queryAll(sqlWhere, pageInfo);
                } catch (Throwable ex) {
                    ZLogger.ef(String.format("加载商品失败: %s", ex.toString()));
                }

                subscriber.onNext(productEntities);
                subscriber.onCompleted();
            }
        })
                .map(new Func1<List<PosProductEntity>, List<PosProductEntity>>() {
                    @Override
                    public List<PosProductEntity> call(List<PosProductEntity> productEntities) {
                        if (productEntities == null) {
                            ZLogger.d("未查询到满足条件的商品");
                            return  null;
                        }

                        int size = productEntities.size();
                        ZLogger.d(String.format("共查询到 %d 个商品", size));

                        List<PosProductEntity> temp = new ArrayList<>();
                        for (PosProductEntity product : productEntities) {
                            String barcode = product.getBarcode();
                            ZLogger.d(String.format("商品:%s--%s(%s)",
                                    product.getBarcode(), product.getName(), product.getAbbreviation()));
                            if (!StringUtils.isEmpty(barcode) && barcode.length() == 6) {
                                temp.add(product);
                            }
                        }
                        ZLogger.d(String.format("共查询到 %d 个商品满足条件", temp.size()));
                        return temp;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PosProductEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        DialogUtil.showHint(e.getMessage());
                        onLoadFinished();
                    }

                    @Override
                    public void onNext(List<PosProductEntity> posProductEntities) {
                        if (mPageInfo.getPageNo() == 1) {
                            entityList.clear();
                        }
                        if (posProductEntities != null) {
                            entityList.addAll(posProductEntities);
                        } else {
                            DialogUtil.showHint("未找到商品");
                        }
                        ZLogger.d(String.format("共有 %d 个商品", entityList.size()));

                        if (goodsListAdapter != null) {
                            goodsListAdapter.setEntityList(entityList);
                        }
                        goodsRecyclerView.smoothScrollToPosition(0);
                        hideKeyboard();

                        onLoadFinished();
                    }

                });
    }


    /**
     * 修改商品
     */
    private void modifyGoods(final int position, final PosProductEntity goods) {
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
//                        deleteGoods(position, goods);
                    }

                    @Override
                    public void onAction3() {
                        updateStatus(position, goods);
                    }
                });
        mFrontCategoryGoodsDialog.setActions(true, false, true);
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
    private void changePrice(final int position, final PosProductEntity goods, final Double costPrice) {
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
                goodsListAdapter.notifyItemChanged(position);

                hideProgressDialog();
            }

        });

    }

    /**
     * 删除商品
     */
    private void deleteGoods(final int position, final PosProductEntity goods) {
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
                ZLogger.e("删除商品失败, " + e.toString());
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
    private void updateStatus(final int position, final PosProductEntity goods) {
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
                goodsListAdapter.notifyItemChanged(position);

                hideProgressDialog();
            }

        });
    }




}

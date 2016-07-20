
/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.mfh.litecashier.ui.fragment.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.GoodsSupplyInfo;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.CategoryOption;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.bean.wrapper.SearchParamsWrapper;
import com.mfh.litecashier.event.CommodityStockEvent;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.presenter.InventoryGoodsPresenter;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.CommodityCategoryAdapter;
import com.mfh.litecashier.ui.adapter.InventoryCostGoodsAdapter;
import com.mfh.litecashier.ui.dialog.ChangeQuantityDialog;
import com.mfh.litecashier.ui.dialog.SelectGoodsSupplyDialog;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseGoodsDetailFragment;
import com.mfh.litecashier.ui.view.IInventoryView;
import com.mfh.litecashier.ui.widget.InputSearchView;
import com.mfh.litecashier.ui.widget.MOrderLabelView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.PurchaseShopcartHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 库存－－库存成本
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryCostFragment extends BaseProgressFragment
        implements IInventoryView {

    @Bind(R.id.inlv_barcode)
    InputSearchView inlvBarcode;
    @Bind(R.id.inlv_productname)
    InputSearchView inlvProductName;

    @Bind(R.id.button_toggle_conditions)
    Button btnToggleConditions;
    @Bind(R.id.frame_option_conditions)
    LinearLayout frameOptionConditions;
    @Bind(R.id.spinner_price_type)
    Spinner spinnerPriceType;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private InventoryCostGoodsAdapter goodsListAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.button_category_back)
    TextView btnCategoryBack;
    @Bind(R.id.tv_category_title)
    TextView tvCategoryTitle;
    @Bind(R.id.category_list)
    RecyclerView categoryRecyclerView;
    private CommodityCategoryAdapter categoryListAdapter;

    @Bind(R.id.empty_view)
    TextView emptyView;
    @Bind(R.id.fab_shopcart)
    FloatingActionButton fabShopcart;

    private List<CategoryOption> rootOptions = new ArrayList<>();
    //使用ArrayMap替代HashMap,提高效率
    private ArrayMap<Integer, CategoryOption> optionsMap = new ArrayMap<>();
    private int currentLevel;

    //搜索条件
    private SearchParamsWrapper searchParams;

    private ChangeQuantityDialog changeDialog = null;
    private ChangeQuantityDialog quantityCheckDialog = null;

    private boolean isLoadingMore;
    private boolean bSyncInProgress = false;//是否正在同步
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<ScGoodsSku> goodsList = new ArrayList<>();

    private InventoryGoodsPresenter inventoryGoodsPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inventory_cost;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        searchParams = new SearchParamsWrapper();
        inventoryGoodsPresenter = new InventoryGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> priceTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.pricetype_name_query, R.layout.mfh_spinner_item_text);
        priceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriceType.setAdapter(priceTypeAdapter);
        spinnerPriceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (searchParams == null) {
                    searchParams = new SearchParamsWrapper();
                }
                searchParams.setPriceType(spinnerPriceType.getSelectedItem().toString());

                loadGoodsList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerPriceType.setSelection(0);
        initBarCodeInput();
        initProductNameInput();
        initGoodsRecyclerView();
        initCategoryRecyclerView();

        inlvBarcode.requestFocus();
        initOrderByStatus(searchParams.getSortType());
        resetSearchParams();

        loadData();
    }


    @Override
    public void onResume() {
        super.onResume();
        inlvBarcode.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_toggle_conditions)
    public void toggleConditions() {
        if (btnToggleConditions.isSelected()) {
            btnToggleConditions.setSelected(false);
            btnToggleConditions.setText("展开");
            frameOptionConditions.setVisibility(View.GONE);
        } else {
            btnToggleConditions.setSelected(true);
            btnToggleConditions.setText("收起");
            frameOptionConditions.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 返回上一层
     */
    @OnClick(R.id.button_category_back)
    public void backupCategory() {
        optionsMap.remove(currentLevel--);
        refreshCategoryList();
    }

    private void resetSearchParams() {
        this.inlvBarcode.clear(false);
        this.inlvProductName.clear(false);
        if (this.searchParams == null) {
            this.searchParams = new SearchParamsWrapper();
        }
    }

    private void initOrderByStatus(int sortType) {
        try {
            orderLabelQuantity.setOrderEnabled(false);
            orderLabelMonthlysales.setOrderEnabled(false);
            switch (sortType) {
                case SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC:
                case SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC:
                    orderLabelQuantity.setOrderEnabled(true);
                    break;
                case SearchParamsWrapper.SORT_BY_MONTHLY_SALES_DESC:
                case SearchParamsWrapper.SORT_BY_MONTHLY_SALES_ASC:
                    orderLabelMonthlysales.setOrderEnabled(true);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    @Bind(R.id.orderLabel_quantity)
    MOrderLabelView orderLabelQuantity;

    /**
     * 按当前库存降序排列
     */
    @OnClick(R.id.orderLabel_quantity)
    public void orderByStockQuantity() {
        if (goodsListAdapter == null) {
            return;
        }

        orderLabelMonthlysales.setOrderEnabled(false);
        orderLabelQuantity.setOrderEnabled(true);
        if (orderLabelQuantity.getCurrentOrderStatus() == MOrderLabelView.ORDER_STATUS_DESC_SELECTED) {
            searchParams.setSortType(SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC);
        } else if (orderLabelQuantity.getCurrentOrderStatus() == MOrderLabelView.ORDER_STATUS_ASC_SELECTED) {
            searchParams.setSortType(SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC);
        }

        loadGoodsList();
    }

    @Bind(R.id.orderLabel_monthlysales)
    MOrderLabelView orderLabelMonthlysales;

    /**
     * 按当前库存降序排列
     */
    @OnClick(R.id.orderLabel_monthlysales)
    public void orderByMonthlySales() {
        if (goodsListAdapter == null) {
            return;
        }
        orderLabelMonthlysales.setOrderEnabled(true);
        orderLabelQuantity.setOrderEnabled(false);
        if (orderLabelMonthlysales.getCurrentOrderStatus() == MOrderLabelView.ORDER_STATUS_DESC_SELECTED) {
            searchParams.setSortType(SearchParamsWrapper.SORT_BY_MONTHLY_SALES_DESC);
        } else if (orderLabelMonthlysales.getCurrentOrderStatus() == MOrderLabelView.ORDER_STATUS_ASC_SELECTED) {
            searchParams.setSortType(SearchParamsWrapper.SORT_BY_MONTHLY_SALES_ASC);
        }

        loadGoodsList();
    }

    @OnClick(R.id.fab_shopcart)
    public void redirectToShopcart() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FRAGMENT_TYPE_COMMODITY_APPLY_SHOPCART);

//        SimpleActivity.actionStart(getActivity(), extras);

        Intent intent = new Intent(getActivity(), SimpleActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_APPLY_SHOPCART);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_APPLY_SHOPCART: {
                inlvBarcode.requestFocus();
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化条码输入
     */
    private void initBarCodeInput() {
        inlvBarcode.setInputSubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
//        inlvBarcode.requestFocus();
        inlvBarcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        loadGoodsList();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
//        inlvBarcode.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (searchParams == null) {
//                    searchParams = new SearchParamsWrapper();
//                }
//                searchParams.setBarcode(s.toString());
//            }
//        });
        inlvBarcode.setOnViewListener(new InputSearchView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                loadGoodsList();
            }
        });
    }

    private void initProductNameInput() {
        inlvProductName.setInputSubmitEnabled(true);
        inlvProductName.setSoftKeyboardEnabled(true);
        inlvProductName.config(InputSearchView.INPUT_TYPE_TEXT);
//        inlvProductName.requestFocus();
        inlvProductName.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        loadGoodsList();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
//        inlvProductName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (searchParams == null) {
//                    searchParams = new SearchParamsWrapper();
//                }
//                searchParams.setProductName(s.toString());
//            }
//        });
        inlvProductName.setOnViewListener(new InputSearchView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                loadGoodsList();
            }
        });
    }


    private void initGoodsRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
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
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动
//                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
                if (dy > 0) {
//                    fabShopcart.setVisibility(View.VISIBLE);
                    if ((lastVisibleItem >= totalItemCount - 1) && !isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
//                    fabShopcart.setVisibility(View.GONE);
                }
            }
        });

        goodsListAdapter = new InventoryCostGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new InventoryCostGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onDataSetChanged() {
                                                      onLoadFinished();
                                                  }

                                                  @Override
                                                  public void onUpdateCostPrice(ScGoodsSku goods) {
                                                      updateCostPrice(goods);
                                                  }

                                                  @Override
                                                  public void onUpdateUpperLimit(ScGoodsSku goods) {
                                                      updateUpperLimit(goods);
                                                  }

                                                  @Override
                                                  public void onOrderItem(ScGoodsSku goods) {
                                                      orderGoods(goods);
                                                  }

                                                  @Override
                                                  public void onShowDetail(ScGoodsSku goods) {
                                                      Bundle extras = new Bundle();
                                                      extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                                                      extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_PURCHASE_GOODSDETAIL);
                                                      extras.putString(PurchaseGoodsDetailFragment.EXTRA_KEY_SKU_NAME, goods.getSkuName());
                                                      extras.putString(PurchaseGoodsDetailFragment.EXTRA_KEY_BARCODE, goods.getBarcode());
                                                      extras.putString(PurchaseGoodsDetailFragment.EXTRA_KEY_IMAGE_URL, goods.getImgUrl());
                                                      SimpleDialogActivity.actionStart(getActivity(), extras);
                                                  }

                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    private void initCategoryRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        categoryRecyclerView.setHasFixedSize(true);
        //添加分割线
        categoryRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
        categoryListAdapter = new CommodityCategoryAdapter(CashierApp.getAppContext(), null);
        categoryListAdapter.setOnAdapterListsner(new CommodityCategoryAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                CategoryOption option = categoryListAdapter.getCurOption();
                if (option != null && option.isHasChild()) {
                    //加载子类目
//                    loadSubCategory(option.getCode());
                    optionsMap.put(++currentLevel, option);
                    refreshCategoryList();
                } else {
                    loadGoodsList();
                }
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
//                animProgress.setVisibility(View.GONE);
                loadGoodsList();
            }
        });
        categoryRecyclerView.setAdapter(categoryListAdapter);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(CommodityStockEvent event) {
        ZLogger.d(String.format("InventoryCostFragment: CommodityStockEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == CommodityStockEvent.EVENT_ID_RELOAD_DATA) {
            inlvBarcode.requestFocus();
            loadData();
        }
    }

    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
        ZLogger.d(String.format("InventoryCostFragment: DataSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO) {
            //刷新供应商
            readCategoryInfoCache();
        }
    }

    public void onEventMainThread(PurchaseShopcartSyncEvent event) {
        ZLogger.d(String.format("InventoryCostFragment: PurchaseShopcartSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_DATASET_CHANGED
                || event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_ORDER_SUCCESS) {
            refreshFabShopcart();
        }
    }


    /**
     * 刷新购物车
     */
    private void refreshFabShopcart() {
        if (PurchaseShopcartHelper.getInstance().getItemCount() <= 0) {
            fabShopcart.setImageResource(R.mipmap.ic_fab_shopcart_white);
        } else {
            fabShopcart.setImageDrawable(CashierHelper.createFabDrawable(PurchaseShopcartHelper.getInstance().getItemCount()));
        }
//        fabShopcart.setVisibility(View.VISIBLE);
    }

    /**
     * 加载商品类目
     */
    public void loadData() {
        refreshFabShopcart();

        //加载后台类目树
        if (!readCategoryInfoCache()) {
            DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO);
        }
    }

    /**
     * 加载后台类目树
     */
    private boolean readCategoryInfoCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_STOCKGOODS_CATEGORY);
        List<CategoryOption> cacheData = JSONArray.parseArray(cacheStr, CategoryOption.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个后台商品类目", ACacheHelper.CK_STOCKGOODS_CATEGORY, cacheData.size()));
            initCategoryList(cacheData, true);

            return true;
        }

        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);

        return false;
    }

    /**
     * 初始化类目列表
     */
    private void initCategoryList(List<CategoryOption> options, boolean isNeedRefresh) {
        if (!isNeedRefresh) {
            return;
        }

        this.rootOptions = options;

        optionsMap.clear();
        currentLevel = 0;
        searchParams.setCategoryId("");
        searchParams.setCategoryName("全部");
        refreshCategoryList();
    }

    /**
     * 刷新类目列表
     */
    private void refreshCategoryList() {
        CategoryOption option = optionsMap.get(currentLevel);

        if (searchParams == null) {
            searchParams = new SearchParamsWrapper();
        }

        if (option != null) {
            List<CategoryOption> items = option.getItems();
            btnCategoryBack.setVisibility(View.VISIBLE);

            searchParams.setCategoryId(option.getCode());
            searchParams.setCategoryName(option.getValue());

//            tvCategoryTitle.setText(String.format("%s(%d)", curOption.getValue(), items == null ? 0 : items.size()));
            categoryListAdapter.setEntityList(items);
        } else {
            btnCategoryBack.setVisibility(View.INVISIBLE);


            searchParams.setCategoryId("");
            searchParams.setCategoryName("全部");

            categoryListAdapter.setEntityList(rootOptions);
        }
        tvCategoryTitle.setText(String.format("%s", searchParams.getCategoryName()));
    }

    /**
     * 刷新标题
     */
    private void refreshCategoryTitle() {
        if (tvCategoryTitle == null) {
            return;
        }
        if (searchParams != null) {
//            tvCategoryTitle.setText(String.format("%s(%d)", curOption.getValue(), goodsListAdapter.getItemCount()));
            tvCategoryTitle.setText(String.format("%s(%d)", searchParams.getCategoryName(), mPageInfo.getTotalCount()));
        } else {
            tvCategoryTitle.setText(String.format("全部(%d)", mPageInfo.getTotalCount()));
        }
    }

    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
        isLoadingMore = true;
        bSyncInProgress = true;
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        bSyncInProgress = false;
        isLoadingMore = false;
    }

    /**
     * 加载商品列表
     */
    @OnClick(R.id.empty_view)
    public void loadGoodsList() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        inventoryGoodsPresenter.loadInventoryGoods(mPageInfo, getCategoryId(), getBarcode(),
                getName(), getSortType(), getPriceType());
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载库存商品。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存商品。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            inventoryGoodsPresenter.loadInventoryGoods(mPageInfo, getCategoryId(), getBarcode(),
                    getName(), getSortType(), getPriceType());
        } else {
            ZLogger.d("加载库存商品，已经是最后一页。");
            onLoadFinished();
        }
    }

    public Long getOtherTenantId() {
        return null;
    }

    public String getCategoryId() {
        return searchParams.getCategoryId();
    }

    public String getBarcode() {
        return inlvBarcode.getInputString();
    }

    public String getName() {
        return inlvProductName.getInputString();
    }

    public String getPriceType() {
        if (searchParams.getPriceType().equals(SearchParamsWrapper.PRICE_TYPE_NAME_NUMBER)) {
            return "0";
        } else if (searchParams.getPriceType().equals(SearchParamsWrapper.PRICE_TYPE_NAME_WEIGHT)) {
            return "1";
        }

        return "";
    }

    public int getSortType() {
        return searchParams.getSortType();
    }

    @Override
    public void onProcess() {
        onLoadProcess("正在加载数据...");
    }

    @Override
    public void onError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onData(ScGoodsSku data) {

    }

    @Override
    public void onList(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        mPageInfo = pageInfo;
        //第一页，清空数据
        if (mPageInfo.getPageNo() == 1) {
            if (goodsList == null) {
                goodsList = new ArrayList<>();
            } else {
                goodsList.clear();
            }
        } else {
            if (goodsList == null) {
                goodsList = new ArrayList<>();
            }
        }
        if (dataList != null) {
            goodsList.addAll(dataList);
        }

        if (goodsListAdapter != null) {
            goodsListAdapter.setEntityList(goodsList);
            refreshCategoryTitle();
        }
        onLoadFinished();
        ZLogger.d(String.format("保存库存商品,pageInfo':page=%d,rows=%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                (goodsList == null ? 0 : goodsList.size()), mPageInfo.getTotalCount()));

        inlvBarcode.clear(false);
//        resetSearchParams();
    }

    /**
     * 修改商品售价
     */
    private void updateCostPrice(final ScGoodsSku goods) {
        if (changeDialog == null) {
            changeDialog = new ChangeQuantityDialog(getActivity());
            changeDialog.setCancelable(false);
            changeDialog.setCanceledOnTouchOutside(false);
        }
        changeDialog.init("零售价", 2, goods.getCostPrice(), new ChangeQuantityDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(final Double quantity) {
                if (quantity == null || quantity.compareTo(goods.getCostPrice()) == 0) {
                    ZLogger.d("售价不变");
                    return;
                }
                if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                    DialogUtil.showHint(getString(R.string.toast_network_error));
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", goods.getId());
                jsonObject.put("costPrice", quantity);
//                jsonObject.put("quantity", goods.getQuantity());
//                jsonObject.put("lowerLimit", goods.getUpperLimit());
                jsonObject.put("tenantId", MfhLoginService.get().getSpid());

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
//
//                                //出库成功:1-556637
//                                ZLogger.d("修改售价成功:" + retStr);

                                goods.setCostPrice(quantity);
                                goodsListAdapter.notifyDataSetChanged();

                                //切换到收银页面后需要同步商品
                                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PRODUCTS_ENABLED, true);
                            }

                            @Override
                            protected void processFailure(Throwable t, String errMsg) {
                                super.processFailure(t, errMsg);
                                ZLogger.d("修改售价失败：" + errMsg);
                            }
                        }
                        , String.class
                        , CashierApp.getAppContext()) {
                };
                StockApiImpl.updateStockGoods(jsonObject.toJSONString(), responseCallback);
            }
        });
        if (!changeDialog.isShowing()) {
            changeDialog.show();
        }
    }

    /**
     * 修改商品售价
     */
    private void updateUpperLimit(final ScGoodsSku goods) {
        if (changeDialog == null) {
            changeDialog = new ChangeQuantityDialog(getActivity());
            changeDialog.setCancelable(false);
            changeDialog.setCanceledOnTouchOutside(false);
        }
        changeDialog.init("排面库存", 2, goods.getCostPrice(), new ChangeQuantityDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(final Double quantity) {
                if (quantity == null || quantity.compareTo(goods.getCostPrice()) == 0) {
                    ZLogger.d("排面库存不变");
                    return;
                }
                if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                    DialogUtil.showHint(getString(R.string.toast_network_error));
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", goods.getId());
//                jsonObject.put("costPrice", quantity);
//                jsonObject.put("quantity", goods.getQuantity());
                jsonObject.put("upperLimit", quantity);
                jsonObject.put("tenantId", MfhLoginService.get().getSpid());

                //回调
                NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                        NetProcessor.Processor<String>>(
                        new NetProcessor.Processor<String>() {
                            @Override
                            public void processResult(IResponseData rspData) {
                                //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                                RspValue<String> retValue = (RspValue<String>) rspData;
                                String retStr = retValue.getValue();

                                //出库成功:1-556637
                                ZLogger.d("修改排面库存成功:" + retStr);

                                goods.setCostPrice(quantity);
                                goodsListAdapter.notifyDataSetChanged();

                                //切换到收银页面后需要同步商品
                                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PRODUCTS_ENABLED, true);
                            }

                            @Override
                            protected void processFailure(Throwable t, String errMsg) {
                                super.processFailure(t, errMsg);
                                ZLogger.d("修改排面库存失败：" + errMsg);
                            }
                        }
                        , String.class
                        , CashierApp.getAppContext()) {
                };
                StockApiImpl.updateStockGoods(jsonObject.toJSONString(), responseCallback);
            }
        });
        if (!changeDialog.isShowing()) {
            changeDialog.show();
        }
    }

    /**
     * 订购商品
     */
    private void orderGoods(ScGoodsSku goods) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        List<GoodsSupplyInfo> supplyInfos = goods.getSupplyItems();
        if (supplyInfos != null && supplyInfos.size() == 1) {
            PurchaseShopcartGoodsWrapper wrapper = PurchaseShopcartGoodsWrapper
                    .fromSupplyGoods(goods, supplyInfos.get(0), IsPrivate.PLATFORM);
            changeQuantity(wrapper);
        } else {
            querySupply(goods, supplyInfos);
        }
    }

    private SelectGoodsSupplyDialog selectGoodsSupplyDialog = null;

    private void querySupply(ScGoodsSku scGoodsSku, List<GoodsSupplyInfo> supplyInfos) {
        if (selectGoodsSupplyDialog == null) {
            selectGoodsSupplyDialog = new SelectGoodsSupplyDialog(getActivity());
            selectGoodsSupplyDialog.setCancelable(false);
            selectGoodsSupplyDialog.setCanceledOnTouchOutside(false);
        }
        selectGoodsSupplyDialog.init(scGoodsSku, supplyInfos,
                new SelectGoodsSupplyDialog.OnDialogListener() {
                    @Override
                    public void onSupplySelected(PurchaseShopcartGoodsWrapper goodsWrapper, GoodsSupplyInfo supplyInfo) {
                        changeQuantity(goodsWrapper);
                    }
                });

        selectGoodsSupplyDialog.show();
    }

    private void changeQuantity(final PurchaseShopcartGoodsWrapper wrapper) {
        if (wrapper == null) {
            return;
        }
        if (quantityCheckDialog == null) {
            quantityCheckDialog = new ChangeQuantityDialog(getActivity());
            quantityCheckDialog.setCancelable(false);
            quantityCheckDialog.setCanceledOnTouchOutside(false);
        }
//        wrapper.getQuantityCheck()
        quantityCheckDialog.init("采购量", 2, 0D, new ChangeQuantityDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(Double quantity) {
                if (quantity < 0D) {
                    DialogUtil.showHint("采购量不能为空");
                    return;
                }
                if (quantity < wrapper.getStartNum()) {
                    DialogUtil.showHint("采购量不能低于起配量");
                    return;
                }

                wrapper.setQuantityCheck(quantity);
                PurchaseShopcartHelper.getInstance().addToShopcart(wrapper);

                //刷新购物车
                refreshFabShopcart();
            }
        });
        quantityCheckDialog.show();
    }
}

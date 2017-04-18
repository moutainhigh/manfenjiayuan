
/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.mfh.litecashier.ui.fragment.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.category.CategoryOption;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.SearchParamsWrapper;
import com.mfh.litecashier.event.CommodityStockEvent;
import com.mfh.litecashier.service.DataDownloadManager;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.categoty.CommodityCategoryAdapter;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseGoodsDetailFragment;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.ui.widget.InputSearchView;
import com.mfh.litecashier.ui.widget.MOrderLabelView;
import com.mfh.litecashier.utils.ACacheHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 库存－－库存成本
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InventoryCostFragment extends BaseProgressFragment
        implements IScGoodsSkuView {

    @BindView(R.id.inlv_barcode)
    InputSearchView inlvBarcode;
    @BindView(R.id.inlv_productname)
    InputSearchView inlvProductName;

    @BindView(R.id.button_toggle_conditions)
    Button btnToggleConditions;
    @BindView(R.id.frame_option_conditions)
    LinearLayout frameOptionConditions;
    @BindView(R.id.spinner_price_type)
    Spinner spinnerPriceType;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private InventoryCostGoodsAdapter goodsListAdapter;
    private LinearLayoutManager linearLayoutManager;

    @BindView(R.id.button_category_back)
    TextView btnCategoryBack;
    @BindView(R.id.tv_category_title)
    TextView tvCategoryTitle;
    @BindView(R.id.category_list)
    RecyclerView categoryRecyclerView;
    private CommodityCategoryAdapter categoryListAdapter;

    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.orderLabel_quantity)
    MOrderLabelView orderLabelQuantity;

    @BindView(R.id.orderLabel_monthlysales)
    MOrderLabelView orderLabelMonthlysales;


    private List<CategoryOption> rootOptions = new ArrayList<>();
    //使用ArrayMap替代HashMap,提高效率
    private ArrayMap<Integer, CategoryOption> optionsMap = new ArrayMap<>();
    private int currentLevel;

    //搜索条件
    private SearchParamsWrapper searchParams;

    private DoubleInputDialog changeDialog = null;

    private boolean isLoadingMore;
    private boolean bSyncInProgress = false;//是否正在同步
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private ScGoodsSkuPresenter inventoryGoodsPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inventory_cost;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        searchParams = new SearchParamsWrapper();
        inventoryGoodsPresenter = new ScGoodsSkuPresenter(this);
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
                String selectedPriceType = spinnerPriceType.getSelectedItem().toString();
                if (!ObjectsCompact.equals(searchParams.getPriceType(), selectedPriceType)){
                    searchParams.setPriceType(selectedPriceType);

                    ZLogger.d("计价类型发生改变");
                    loadGoodsList();
                }

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

    /**
     * 初始化条码输入
     */
    private void initBarCodeInput() {
//        inlvBarcode.requestFocus();
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER){
                    loadGoodsList();
                }
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
    }

    private void initProductNameInput() {
        inlvProductName.setSoftKeyboardEnabled(true);
        inlvProductName.config(InputSearchView.INPUT_TYPE_TEXT);
//        inlvProductName.requestFocus();
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER){
                    loadGoodsList();
                }
            }
        });
        inlvBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvBarcode.isSoftKeyboardEnabled()) {
                        showBarcodeKeyboard();
                    }
                }

                inlvBarcode.requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
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
    }

    private NumberInputDialog barcodeInputDialog;
    /**
     * 显示条码输入界面
     * 相当于扫描条码
     */
    private void showBarcodeKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getContext());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.BARCODE, "商品条码", "商品条码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        inlvBarcode.setInputString(value);
                        loadGoodsList();
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
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
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
                    if ((lastVisibleItem >= totalItemCount - 4) && !isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
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
                } else {ZLogger.d("选中类目");
                    loadGoodsList();
                }
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
//                animProgress.setVisibility(View.GONE);
                ZLogger.d("类目发生变化");
                loadGoodsList();
            }
        });
        categoryRecyclerView.setAdapter(categoryListAdapter);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommodityStockEvent event) {
        ZLogger.d(String.format("CommodityStockEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == CommodityStockEvent.EVENT_ID_RELOAD_DATA) {
            inlvBarcode.requestFocus();
            loadData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataDownloadManager.DataDownloadEvent event) {
        ZLogger.d(String.format("DataDownloadEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataDownloadManager.DataDownloadEvent.EVENT_BACKEND_CATEGORYINFO_UPDATED) {
            //刷新供应商
            readCategoryInfoCache();
        }
    }

    /**
     * 加载商品类目
     */
    public void loadData() {
        if (!readCategoryInfoCache()) {
            DataDownloadManager.get().sync(DataDownloadManager.BACKENDCATEGORYINFO);
        }
    }

    /**
     * 加载后台类目树
     */
    private boolean readCategoryInfoCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_BACKEND_CATEGORY_TREE);
        List<CategoryOption> cacheData = JSONArray.parseArray(cacheStr, CategoryOption.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个后台商品类目", ACacheHelper.CK_BACKEND_CATEGORY_TREE, cacheData.size()));
            initCategoryList(cacheData, true);

            return true;
        }

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
        searchParams.setCategoryId(null);
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

            searchParams.setCategoryId(null);
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
            tvCategoryTitle.setText(String.format("%s(%d)",
                    searchParams.getCategoryName(), mPageInfo.getTotalCount()));
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
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        int sortType = searchParams.getSortType();
        String orderby = null;
        boolean orderbydesc = false;
        if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC) {
            orderby = "gku.quantity";
            orderbydesc = true;
        } else if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC) {
            orderby = "gku.quantity";
            orderbydesc = false;
        } else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_DESC) {
            orderby = "gku.sell_month_num";
            orderbydesc = true;
        } else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_ASC) {
            orderby = "gku.sell_month_num";
            orderbydesc = false;
        }

        inventoryGoodsPresenter.listScGoodsSku(mPageInfo,
                searchParams.getCategoryId(), getBarcode(),
                getName(), orderby, orderbydesc, getPriceType());

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
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载库存商品。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();


            int sortType = searchParams.getSortType();
            String orderby = null;
            boolean orderbydesc = false;
            if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC) {
                orderby = "gku.quantity";
                orderbydesc = true;
            } else if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC) {
                orderby = "gku.quantity";
                orderbydesc = false;
            } else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_DESC) {
                orderby = "gku.sell_month_num";
                orderbydesc = true;
            } else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_ASC) {
                orderby = "gku.sell_month_num";
                orderbydesc = false;
            }

            inventoryGoodsPresenter.listScGoodsSku(mPageInfo,
                    searchParams.getCategoryId(), getBarcode(),
                    getName(), orderby, orderbydesc, getPriceType());
        } else {
            ZLogger.d("加载库存商品，已经是最后一页。");
            onLoadFinished();
        }
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

    /**
     * 修改商品售价
     */
    private void updateCostPrice(final ScGoodsSku goods) {
        if (changeDialog == null) {
            changeDialog = new DoubleInputDialog(getActivity());
            changeDialog.setCancelable(false);
            changeDialog.setCanceledOnTouchOutside(false);
        }
        changeDialog.init("零售价", 2, goods.getCostPrice(), new DoubleInputDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(final Double quantity) {
                if (quantity == null || quantity.compareTo(goods.getCostPrice()) == 0) {
                    ZLogger.d("售价不变");
                    return;
                }
                if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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
//                                ZLogger.d("修改售价成功:" + retStr);

                                goods.setCostPrice(quantity);
                                goodsListAdapter.notifyDataSetChanged();
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
                InvSkuStoreApiImpl.update(jsonObject.toJSONString(), responseCallback);
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
            changeDialog = new DoubleInputDialog(getActivity());
            changeDialog.setCancelable(false);
            changeDialog.setCanceledOnTouchOutside(false);
        }
        changeDialog.init("排面库存", 2, goods.getCostPrice(), new DoubleInputDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(final Double quantity) {
                if (quantity == null || quantity.compareTo(goods.getCostPrice()) == 0) {
                    ZLogger.d("排面库存不变");
                    return;
                }
                if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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
                InvSkuStoreApiImpl.update(jsonObject.toJSONString(), responseCallback);
            }
        });
        if (!changeDialog.isShowing()) {
            changeDialog.show();
        }
    }

    @Override
    public void onIScGoodsSkuViewProcess() {
        onLoadProcess("正在加载数据...");
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> scGoodsSkus) {
        mPageInfo = pageInfo;
        //第一页，清空数据
        if (mPageInfo.getPageNo() == 1) {
            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(scGoodsSkus);
            }
        } else {
            if (goodsListAdapter != null) {
                goodsListAdapter.appendEntityList(scGoodsSkus);
            }
        }

        refreshCategoryTitle();
        onLoadFinished();
        ZLogger.d(String.format("保存库存商品,pageInfo':page=%d,rows=%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                goodsListAdapter.getItemCount(), mPageInfo.getTotalCount()));
    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku goodsSku) {

    }
}

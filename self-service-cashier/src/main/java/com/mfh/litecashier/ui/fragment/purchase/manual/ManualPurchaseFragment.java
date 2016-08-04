package com.mfh.litecashier.ui.fragment.purchase.manual;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.manfenjiayuan.business.bean.CategoryOption;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.GoodsSupplyInfo;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.compound.OptionalLabel;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.bean.wrapper.SearchParamsWrapper;
import com.mfh.litecashier.database.entity.PurchaseOrderEntity;
import com.mfh.litecashier.database.logic.PurchaseGoodsService;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.presenter.PurchasePresenter;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.CommodityCategoryAdapter;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;
import com.mfh.litecashier.ui.dialog.SelectGoodsSupplyDialog;
import com.mfh.litecashier.ui.dialog.SelectInvCompanyInfoDialog;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseGoodsDetailFragment;
import com.mfh.litecashier.ui.fragment.purchase.intelligent.IIntelligentPurchaseView;
import com.mfh.litecashier.ui.fragment.purchase.intelligent.IntelligentPurchasePresenter;
import com.mfh.litecashier.ui.view.IPurchaseView;
import com.mfh.litecashier.ui.widget.InputSearchView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 手动订货
 * Created by bingshanguxue on 17/07/20.
 */
public class ManualPurchaseFragment extends BaseProgressFragment
        implements IPurchaseView, IIntelligentPurchaseView {

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

    @Bind(R.id.label_platform_provider)
    OptionalLabel labelPlatformProvider;
    private SelectInvCompanyInfoDialog selectPlatformProviderDialog;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private ManualPurchaseGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;

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
    private DoubleInputDialog quanticyCheckDialog = null;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private PurchasePresenter mPurchasePresenter;
    private IntelligentPurchasePresenter mIntelligentPurchasePresenter;

    public static ManualPurchaseFragment newInstance(Bundle args) {
        ManualPurchaseFragment fragment = new ManualPurchaseFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_manual;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        mPurchasePresenter = new PurchasePresenter(this);
        mIntelligentPurchasePresenter = new IntelligentPurchasePresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        initCategoryRecyclerView();
        initGoodsRecyclerView();

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
        labelPlatformProvider.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                if (searchParams == null) {
                    searchParams = new SearchParamsWrapper();
                }
                searchParams.setCompanyInfo(null);
                loadGoodsList();
            }
        });
        initBarCodeInput();
        initProductNameInput();

        searchParams = new SearchParamsWrapper();
        inlvBarcode.requestFocus();

        refreshFabShopcart();

        loadData();

        selectPlatformProvider();
    }

    @Override
    public void onPause() {
        super.onPause();
        //保存搜索条件
        try {
            if (searchParams != null) {
                JSONArray cacheArrays = new JSONArray();
                cacheArrays.add(searchParams);
                ZLogger.d(String.format("cacheStr=%s", cacheArrays.toJSONString()));
                ACacheHelper.put(ACacheHelper.TCK_PURCHASE_SEARCH_PARAMS, cacheArrays.toJSONString());
            }
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
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
     * 返回上一层
     */
    @OnClick(R.id.button_category_back)
    public void backupCategory() {
        optionsMap.remove(currentLevel--);
        refreshCategoryList();
    }

    @OnClick(R.id.fab_shopcart)
    public void redirectToShopcart() {
        fabShopcart.setEnabled(false);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_PURCHASE_MANUAL_SHOPCART);

//        SimpleActivity.actionStart(getActivity(), extras);

        Intent intent = new Intent(getActivity(), SimpleActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_APPLY_SHOPCART);

        fabShopcart.setEnabled(true);
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
     * 选择平台供应商
     */
    @OnClick(R.id.label_platform_provider)
    public void selectPlatformProvider() {
        //TODO,判断商品是否存在多个供应链，若存在多个，则提示选择供应链
        if (selectPlatformProviderDialog == null) {
            selectPlatformProviderDialog = new SelectInvCompanyInfoDialog(getActivity());
            selectPlatformProviderDialog.setCancelable(false);
            selectPlatformProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectPlatformProviderDialog.init(new SelectInvCompanyInfoDialog.OnDialogListener() {
                    @Override
                    public void onItemSelected(CompanyInfo companyInfo) {
                        if (companyInfo == null) {
                            getActivity().setResult(Activity.RESULT_CANCELED);
                            getActivity().finish();
                        } else {
                            if (searchParams == null) {
                                searchParams = new SearchParamsWrapper();
                            }
                            searchParams.setCompanyInfo(companyInfo);
                            labelPlatformProvider.setLabelText(companyInfo.getName());
                            loadGoodsList();
                        }
                    }

                    @Override
                    public void onCancel() {
                        if (searchParams == null) {
                            searchParams = new SearchParamsWrapper();
                        }
                        CompanyInfo companyInfo = searchParams.getCompanyInfo();

                        if (companyInfo == null) {
                            getActivity().setResult(Activity.RESULT_CANCELED);
                            getActivity().finish();
                        }
//                        else {
//                            labelPlatformProvider.setLabelText(companyInfo.getName());
//                            loadGoodsList();
//                        }
                    }

                });
        if (!selectPlatformProviderDialog.isShowing()) {
            selectPlatformProviderDialog.show();
        }
    }

    /**
     * 渲染搜索条件
     */
    private void renderSearchParams() {
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_SEARCH_PARAMS);
        if (searchParams == null) {
            searchParams = new SearchParamsWrapper();
        }

        inlvBarcode.setInputString(searchParams.getBarcode());
        inlvProductName.setInputString(searchParams.getProductName());
        CompanyInfo companyInfo = searchParams.getCompanyInfo();
        if (companyInfo != null) {
            labelPlatformProvider.setLabelText(companyInfo.getName());
        } else {
            labelPlatformProvider.setLabelText("");
        }

        switch (searchParams.getPriceType()) {
            case SearchParamsWrapper.PRICE_TYPE_NAME_NA: {
                spinnerPriceType.setSelection(0);
            }
            break;
            case SearchParamsWrapper.PRICE_TYPE_NAME_NUMBER: {
                spinnerPriceType.setSelection(1);
            }
            break;
            case SearchParamsWrapper.PRICE_TYPE_NAME_WEIGHT: {
                spinnerPriceType.setSelection(2);
            }
            break;
            default:
                spinnerPriceType.setSelection(-1);
                break;
        }

        inlvBarcode.requestFocus();
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
        inlvBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchParams == null) {
                    searchParams = new SearchParamsWrapper();
                }
                searchParams.setBarcode(s.toString());
            }
        });
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

        inlvProductName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchParams == null) {
                    searchParams = new SearchParamsWrapper();
                }
                searchParams.setProductName(s.toString());
            }
        });
        inlvProductName.setOnViewListener(new InputSearchView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                loadGoodsList();
            }
        });
    }

    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
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
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        goodsListAdapter = new ManualPurchaseGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new ManualPurchaseGoodsAdapter.OnAdapterListener() {

                                                  @Override
                                                  public void addToShopcart(ScGoodsSku goods, int quantity) {
                                                      addGoodsToShopcart(goods, quantity);
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

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      onLoadFinished();
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    /**
     * 添加到购物车
     */
    private void addGoodsToShopcart(final ScGoodsSku goods, int quantity) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        PurchaseShopcartGoodsWrapper wrapper = PurchaseShopcartGoodsWrapper
                .fromSupplyGoods(goods, goods.getSupplyItem(), IsPrivate.PLATFORM);

        if (quantity == 0){
            PurchaseGoodsService.getInstance().deleteById(String.valueOf(goods.getId()));
            PurchaseHelper.getInstance().removeGoods(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL,
                    wrapper);
        }
        else{
            wrapper.setQuantityCheck(Double.valueOf(String.valueOf(quantity)));
            PurchaseHelper.getInstance()
                    .addToShopcart(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL, wrapper, true);
        }

        refreshFabShopcart();

//        List<GoodsSupplyInfo> supplyInfos = goods.getSupplyItems();
//        if (supplyInfos != null) {
//            if (supplyInfos.size() == 1) {
//                PurchaseShopcartGoodsWrapper wrapper = PurchaseShopcartGoodsWrapper
//                        .fromSupplyGoods(goods, supplyInfos.get(0), IsPrivate.PLATFORM);
//                wrapper.setQuantityCheck(Double.valueOf(String.valueOf(quantity)));
//                PurchaseHelper.getInstance()
//                        .addToShopcart(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL, wrapper);
////                changeQuantity(wrapper);
//            } else {
//                CompanyInfo companyInfo = searchParams.getCompanyInfo();
//                if (companyInfo != null && companyInfo.getTenantId() != null) {
//                    for (GoodsSupplyInfo supplyInfo : supplyInfos) {
//                        if (companyInfo.getTenantId().equals(supplyInfo.getSupplyId())) {
//
//
////                            changeQuantity(wrapper);
//                            return;
//                        }
//                    }
//                }
////                querySupply(goods, supplyInfos);
//            }
//        }
//        else {
//            querySupply(goods, supplyInfos);
//        }
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
                    public void onSupplySelected(PurchaseShopcartGoodsWrapper goodsWrapper,
                                                 GoodsSupplyInfo supplyInfo) {
                        changeQuantity(goodsWrapper);
                    }
                });

        selectGoodsSupplyDialog.show();
    }

    private void changeQuantity(final PurchaseShopcartGoodsWrapper wrapper) {
        if (wrapper == null) {
            return;
        }
        if (quanticyCheckDialog == null) {
            quanticyCheckDialog = new DoubleInputDialog(getActivity());
            quanticyCheckDialog.setCancelable(false);
            quanticyCheckDialog.setCanceledOnTouchOutside(false);
        }
        //goods.getQuantityCheck()
        quanticyCheckDialog.init("采购量", 2, 0D, new DoubleInputDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(Double quantity) {
                if (quantity < 0D) {
                    DialogUtil.showHint("采购量不能为空");
                    return;
                }
                if (quantity < wrapper.getStartNum()) {
                    DialogUtil.showHint(String.format("采购量不能低于起配量 %.2f", wrapper.getStartNum()));
                    return;
                }

                wrapper.setQuantityCheck(quantity);
                PurchaseHelper.getInstance()
                        .addToShopcart(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL, wrapper, true);

                //刷新购物车
                refreshFabShopcart();
            }
        });
        quanticyCheckDialog.show();
    }

    private void initCategoryRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        categoryRecyclerView.setHasFixedSize(true);
//        //添加分割线
        categoryRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
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

    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
        ZLogger.d(String.format("DataSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO) {
            //刷新供应商
            readCategoryInfoCache();
        }
    }

    public void onEventMainThread(PurchaseShopcartSyncEvent event) {
        ZLogger.d(String.format("PurchaseShopcartSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_DATASET_CHANGED
                || event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_ORDER_SUCCESS) {
            //刷新
            refreshFabShopcart();
        }
    }

    /**
     * 刷新购物车
     */
    private void refreshFabShopcart() {
        int count = PurchaseHelper.getInstance()
                .getOrderItemCount(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL);
        if (count <= 0) {
            fabShopcart.setImageResource(R.mipmap.ic_fab_shopcart_white);
        } else {
            fabShopcart.setImageDrawable(CashierHelper.createFabDrawable(count));
        }
    }

    /**
     * 加载商品类目
     */
    private void loadData() {
        onLoadProcess("正在加载数据...");
        //恢复搜索条件
        try {
            String cacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_PURCHASE_SEARCH_PARAMS);
            ZLogger.d(String.format("cacheStr=%s", cacheStr));
            List<SearchParamsWrapper> cacheData = JSONArray.parseArray(cacheStr, SearchParamsWrapper.class);
            if (cacheData != null && cacheData.size() > 0) {
                searchParams = cacheData.get(0);
            }

            renderSearchParams();
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

        //加载后台类目树
        if (!readCategoryInfoCache()) {
            DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO);
        }
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

            categoryListAdapter.setEntityList(items);
        } else {
            btnCategoryBack.setVisibility(View.INVISIBLE);


            searchParams.setCategoryId(null);
            searchParams.setCategoryName("全部");

            categoryListAdapter.setEntityList(rootOptions);
        }

        tvCategoryTitle.setText(String.format("%s", searchParams.getCategoryName()));
    }

    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
        isLoadingMore = true;
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
//        bSyncInProgress = false;
        isLoadingMore = false;
    }


    /**
     * 加载商品列表
     * TODO,加载等待窗口
     */
    private void loadGoodsList() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        mPurchasePresenter.loadPurchaseGoods(mPageInfo, searchParams.getCategoryId(),
                getOtherTenantId(),
                getBarcode(), getName(), getSortType(), getPriceType());
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    private void loadMore() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
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

            mPurchasePresenter.loadPurchaseGoods(mPageInfo, searchParams.getCategoryId(),
                    getOtherTenantId(),
                    getBarcode(), getName(), getSortType(), getPriceType());
        } else {
            ZLogger.d("加载商品列表，已经是最后一页。");
            onLoadFinished();
        }
    }

    public Long getOtherTenantId() {
        CompanyInfo companyInfo = searchParams.getCompanyInfo();
        if (companyInfo != null) {
            return companyInfo.getTenantId();
        } else {
            return null;
        }
    }

    public String getBarcode() {
        return searchParams.getBarcode();
    }

    public String getName() {
        return searchParams.getProductName();
    }

    public String getPriceType() {
        if (searchParams.getPriceType().equals(SearchParamsWrapper.PRICE_TYPE_NAME_NUMBER)) {
            return "0";
        } else if (searchParams.getPriceType().equals(SearchParamsWrapper.PRICE_TYPE_NAME_WEIGHT)) {
            return "1";
        }

        return null;
    }

    public int getSortType() {
        return searchParams.getSortType();
    }

    /**
     * 加载后台类目树
     */
    private boolean readCategoryInfoCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_STOCKGOODS_CATEGORY);
        List<CategoryOption> cacheData = JSONArray.parseArray(cacheStr, CategoryOption.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个后台商品类目",
                    ACacheHelper.CK_STOCKGOODS_CATEGORY, cacheData.size()));
            initCategoryList(cacheData, true);

            return true;
        }

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);

        return false;
    }

    @Override
    public void onLoadPurchaseGoodsProcess() {
        onLoadProcess("正在加载数据...");
    }

    @Override
    public void onLoadPurchaseGoodsError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onLoadPurchaseGoodsFinished(PageInfo pageInfo, List<ScGoodsSku> dataList) {
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
        ZLogger.d(String.format("保存采购商品,pageInfo':page=%d,rows=%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getPageSize(),
                goodsListAdapter.getItemCount(), mPageInfo.getTotalCount()));
    }

    /**
     * 智能订货
     * */
    @OnClick(R.id.fab_intelligent)
    public void intelligentPurchase(){
        CompanyInfo companyInfo = searchParams.getCompanyInfo();
        if (companyInfo != null) {
            mIntelligentPurchasePresenter.loadGoodsList(companyInfo.getId());
        } else {
            selectPlatformProvider();
        }

    }

    @Override
    public void onIntelligentPurchaseProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在为您智能订货...", false);
    }

    @Override
    public void onIntelligentPurchaseError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
    }

    @Override
    public void onIntelligentPurchaseSuccess(List<InvSendOrderItem> dataList) {
        CompanyInfo companyInfo = searchParams.getCompanyInfo();

        if (dataList != null && dataList.size() > 0 && companyInfo != null) {
            new IntelligentAsyncTask(companyInfo)
                    .execute(dataList);
        }
        else{
            showProgressDialog(ProgressDialog.STATUS_DONE, "智能订货完成", true);
        }

    }


    private class IntelligentAsyncTask extends AsyncTask<List<InvSendOrderItem>, Integer, Long> {
        private CompanyInfo companyInfo;


        public IntelligentAsyncTask(CompanyInfo companyInfo) {
            this.companyInfo = companyInfo;
        }

        @Override
        protected Long doInBackground(List<InvSendOrderItem>... params) {
            saveQueryResult(params[0], companyInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            refreshFabShopcart();

            goodsListAdapter.notifyDataSetChanged();
            showProgressDialog(ProgressDialog.STATUS_DONE, "智能订货完成", true);
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(List<InvSendOrderItem> goodsList, CompanyInfo companyInfo) {//此处在主线程中执行。
            try {
                for (InvSendOrderItem invSendOrderItem : goodsList) {
                    PurchaseShopcartGoodsWrapper goodsWrapper = PurchaseShopcartGoodsWrapper
                            .fromIntelligentOrderItem(invSendOrderItem, companyInfo,
                                    IsPrivate.PLATFORM);
                    PurchaseHelper.getInstance()
                            .addToShopcart(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL, goodsWrapper, false);

                }
                PurchaseHelper.getInstance().arrange(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL);

            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("保存智能订货商品失败: %s", ex.toString()));
            }
        }
    }
}

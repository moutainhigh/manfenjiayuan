package com.mfh.litecashier.ui.fragment.purchase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.manfenjiayuan.business.bean.CategoryInfo;
import com.manfenjiayuan.business.bean.CategoryOption;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.mvp.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.mvp.view.IChainGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.api.invOrder.CateApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.compound.OptionalLabel;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.PosCategory;
import com.mfh.litecashier.bean.wrapper.SearchParamsWrapper;
import com.mfh.litecashier.database.entity.PurchaseShopcartEntity;
import com.mfh.litecashier.database.logic.PurchaseShopcartService;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.service.CloudSyncManager;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.FrontendFreshCategoryAdapter;
import com.mfh.litecashier.ui.adapter.PurchaseFruitGoodsAdapter;
import com.mfh.litecashier.ui.dialog.SelectWholesalerDialog;
import com.mfh.litecashier.ui.dialog.SyncDataDialog;
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
 * 水果采购
 * Created by bingshanguxue on 15/12/15.
 */
public class PurchaseFruitFragment extends BaseProgressFragment
        implements IChainGoodsSkuView {
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
    private SelectWholesalerDialog mSelectWholesalerDialog;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private PurchaseFruitGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;

    @Bind(R.id.category_list)
    RecyclerView categoryRecyclerView;
    private FrontendFreshCategoryAdapter categoryListAdapter;

    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.fab_shopcart)
    FloatingActionButton fabShopcart;

    //搜索条件
    private CompanyInfo mCompanyInfo;
    private PosCategory mPosCategory;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 40;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private ChainGoodsSkuPresenter inventoryGoodsPresenter;

    private SyncDataDialog mSyncDataDialog = null;

    public static PurchaseFruitFragment newInstance(Bundle args) {
        PurchaseFruitFragment fragment = new PurchaseFruitFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_fresh;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        inventoryGoodsPresenter = new ChainGoodsSkuPresenter(this);
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
//                if (searchParams == null) {
//                    searchParams = new SearchParamsWrapper();
//                }
//                searchParams.setPriceType(spinnerPriceType.getSelectedItem().toString());
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
                selectPlatformProvider();
//                mCompanyInfo = null;
//                saveFreshFrontendCategoryInfoCache(null);
////                if (searchParams == null) {
////                    searchParams = new SearchParamsWrapper();
////                }
////                searchParams.setProviderId(null);
////                searchParams.setProviderName("");
//                loadGoodsList();
            }
        });
        initBarCodeInput();
        initProductNameInput();

//        searchParams = new SearchParamsWrapper();

        inlvBarcode.requestFocus();

//        refreshFabShopcart();

        CloudSyncManager.get().importFromChainSku();
        selectPlatformProvider();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSyncDataDialog != null) {
            mSyncDataDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSyncDataDialog != null) {
            mSyncDataDialog.dismiss();
            mSyncDataDialog = null;
        }

        EventBus.getDefault().unregister(this);
    }


    @OnClick(R.id.fab_shopcart)
    public void redirectToShopcart() {
//        DialogUtil.showHint("跳转到购物车");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_PURCHASE_FRUIT_SHOPCART);

        Intent intent = new Intent(getActivity(), SimpleActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_APPLY_SHOPCART);
//        UIHelper.startActivity(getActivity(), SimpleActivity.class, extras);
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
        if (mSelectWholesalerDialog == null) {
            mSelectWholesalerDialog = new SelectWholesalerDialog(getActivity());
            mSelectWholesalerDialog.setCancelable(false);
            mSelectWholesalerDialog.setCanceledOnTouchOutside(false);
        }
        mSelectWholesalerDialog.init(String.valueOf(AbilityItem.CASCADE), new SelectWholesalerDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                mCompanyInfo = companyInfo;
                if (companyInfo == null) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                } else {
//                    if (searchParams == null) {
//                        searchParams = new SearchParamsWrapper();
//                    }
//                    searchParams.setProviderId(companyInfo.getId());
//                    searchParams.setProviderName(companyInfo.getName());
                    labelPlatformProvider.setLabelText(mCompanyInfo.getName());

                    //加载前台类目树
                    if (readCategoryInfoCache()) {
                        loadGoodsList();
                    }
                    downloadFreshFrontCategory();

                    //同步批发商商品库
                    CloudSyncManager.get().importFromChainSku(companyInfo.getTenantId(),
                            String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRUIT));
                }
            }

            @Override
            public void onCancel() {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });
        if (!mSelectWholesalerDialog.isShowing()) {
            mSelectWholesalerDialog.show();
        }
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
//                if (searchParams == null) {
//                    searchParams = new SearchParamsWrapper();
//                }
//                searchParams.setBarcode(s.toString());
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
//                if (searchParams == null) {
//                    searchParams = new SearchParamsWrapper();
//                }
//                searchParams.setProductName(s.toString());
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

        goodsListAdapter = new PurchaseFruitGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new PurchaseFruitGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onShowDetail(ChainGoodsSku goods) {
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

                                                      refreshFabShopcart();
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
//        //添加分割线
        categoryRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
        categoryListAdapter = new FrontendFreshCategoryAdapter(CashierApp.getAppContext(), null);
        categoryListAdapter.setOnAdapterListsner(new FrontendFreshCategoryAdapter.AdapterListener() {
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

    public void onEventMainThread(PurchaseShopcartSyncEvent event) {
        ZLogger.d(String.format("PurchaseShopcartSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_DATASET_CHANGED
                || event.getEventId() == PurchaseShopcartSyncEvent.EVENT_ID_ORDER_SUCCESS) {
            goodsListAdapter.notifyDataSetChanged();
            refreshFabShopcart();
        }
    }


    public void onEventMainThread(PurchaseFreshEvent event) {
        ZLogger.d(String.format("PurchaseFreshEvent(%d)", event.getEventId()));
        if (event.getEventId() == PurchaseFreshEvent.EVENT_ID_SYNC_START) {

            syncConfirm();
        }
    }

    /**
     * 同步商品档案
     */
    private void syncConfirm() {
        if (mSyncDataDialog == null) {
            mSyncDataDialog = new SyncDataDialog(getActivity());
            mSyncDataDialog.setCancelable(false);
            mSyncDataDialog.setCanceledOnTouchOutside(true);
        }
        mSyncDataDialog.init("同步商品档案", new SyncDataDialog.DialogClickListener() {
            @Override
            public void onFullscale() {
                if (mCompanyInfo != null){
                    //清空米西生鲜商品档案同步游标
                    String cursorKey1 = String.format("%s_%d_%s",
                            SharedPreferencesHelper.PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR,
                            mCompanyInfo.getTenantId(),
                            String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRUIT));
                    SharedPreferencesHelper.set(cursorKey1, "");

                    CloudSyncManager.get().importFromChainSku(mCompanyInfo.getTenantId(),
                            String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRUIT));
                }
            }

            @Override
            public void onIncremental() {
                if (mCompanyInfo != null){
                    CloudSyncManager.get().importFromChainSku(mCompanyInfo.getTenantId(),
                            String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRUIT));
                }
            }
        });
        if (!mSyncDataDialog.isShowing()) {
            mSyncDataDialog.show();
        }
    }

    /**
     * 刷新购物车
     */
    private void refreshFabShopcart() {
        List<PurchaseShopcartEntity> entityList = PurchaseShopcartService.getInstance().getFreshGoodsList();

        if (entityList != null && entityList.size() > 0) {
            fabShopcart.setImageDrawable(CashierHelper.createFabDrawable(entityList.size()));
        } else {
            fabShopcart.setImageResource(R.mipmap.ic_fab_shopcart_white);
        }
    }

    /**
     * 加载商品类目
     */
    private void loadData() {
//        renderSearchParams();

        //加载前台类目树
        if (!readCategoryInfoCache()) {
            onLoadProcess("正在加载类目数据...");
            DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO_FRESH);
        }
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

        if (mPosCategory == null) {
            return;
        }
        //初始化
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        inventoryGoodsPresenter.loadCompanyChainSkuGoods(mPageInfo, mPosCategory.getId(),
                getOtherTenantId(), inlvBarcode.getInputString());
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

            inventoryGoodsPresenter.loadCompanyChainSkuGoods(mPageInfo,
                    mPosCategory.getId(), getOtherTenantId(), inlvBarcode.getInputString());
        } else {
            ZLogger.d("加载商品列表，已经是最后一页。");
            onLoadFinished();
        }
    }

    public Long getOtherTenantId() {
//        return searchParams.getProviderId();
        return mCompanyInfo.getId();
    }

    public String getBarcode() {
        return inlvBarcode.getInputString();
//        return searchParams.getBarcode();
    }

    public String getName() {
        return inlvProductName.getInputString();
//        return searchParams.getProductName();
    }

    public String getPriceType() {
        String priceType = spinnerPriceType.getSelectedItem().toString();
        if (priceType.equals(SearchParamsWrapper.PRICE_TYPE_NAME_NUMBER)) {
            return "0";
        } else if (priceType.equals(SearchParamsWrapper.PRICE_TYPE_NAME_WEIGHT)) {
            return "1";
        }

        return null;
    }

    public int getSortType() {
        return -1;
//        return searchParams.getSortType();
    }


    /**
     * 加载前台类目树
     */
    private boolean readCategoryInfoCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_FRONTEND_CATEGORY_FRUIT);
        List<PosCategory> cacheData = JSONArray.parseArray(cacheStr, PosCategory.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个前台生鲜商品类目",
                    ACacheHelper.CK_FRONTEND_CATEGORY_FRUIT, cacheData.size()));
            //取第一个作为根目录
            categoryListAdapter.setEntityList(cacheData);
            return true;
        }

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_FRONTEND_CATEGORYINFO_FRUIT_ENABLED, true);

        return false;
    }


    /**
     * 下载私有前台类目
     */
    private void downloadFreshFrontCategory() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            return;
        }

        ZLogger.df("同步前台自定义（私有）类目开始");
        CateApiImpl.comnqueryCategory(CateApi.DOMAIN_TYPE_PROD, CateApi.FRUIT,
                CateApi.CATE_POSITION_FRONT,
                1, mCompanyInfo.getTenantId(), customFrontCategoryRespCallback);
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
            , CashierApp.getAppContext()) {
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

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
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
                , CashierApp.getAppContext()) {
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
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_FRONTEND_CATEGORY_FRUIT, cacheArrays.toJSONString());

        //设置下次不需要自动更新商品类目，可以在收银页面点击同步按钮修改
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_FRONTEND_CATEGORYINFO_FRUIT_ENABLED, false);

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

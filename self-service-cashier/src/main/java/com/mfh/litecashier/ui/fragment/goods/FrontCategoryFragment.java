package com.mfh.litecashier.ui.fragment.goods;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.ProductCatalogApi;
import com.mfh.framework.api.category.ScCategoryInfoApi;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.PosCategory;
import com.mfh.litecashier.database.entity.PosCategoryGoodsTempEntity;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 收银－－前台类目
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class FrontCategoryFragment extends BaseFragment {

    public static final String EXTRA_CATEGORY_ID = "categoryId";
    public static final String EXTRA_CATEGORY_ID_POS = "posFrontCategoryId";


    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @Bind(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    private Long posFrontCategoryId;//pos本地前台类目
    private Long categoryId;
    private String cacheKey;
    private List<PosCategory> curCategoryList;//当前子类目

    public static FrontCategoryFragment newInstance(Bundle args) {
        FrontCategoryFragment fragment = new FrontCategoryFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_front_category;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        init(getArguments());

        mToolbar.setTitle("选择商品");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_reload) {
                    reload();
                }
                else if (id == R.id.action_submit) {
                    submit();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_addprodudts2category);

        initCategoryGoodsView();
        reload();
    }

    public void init(Bundle args) {
        if (args != null) {
            this.posFrontCategoryId = args.getLong(EXTRA_CATEGORY_ID_POS);
            this.categoryId = args.getLong(EXTRA_CATEGORY_ID);
        }
        this.cacheKey = String.format("%s_%d",
                ACacheHelper.CK_FRONT_CATEGORY_ID, categoryId);
    }

    private void initCategoryGoodsView() {
        mCategoryGoodsTabStrip.setOnClickTabListener(null);
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                Long categoryId = curCategoryList.get(page).getId();
                EventBus.getDefault().post(new FrontCategoryGoodsEvent(FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }

    /**
     * 加载数据
     */
    public void reload() {
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
            String cacheStr = ACacheHelper.getAsString(cacheKey);
            List<PosCategory> cacheData = JSONArray.parseArray(cacheStr, PosCategory.class);
            if (cacheData != null && cacheData.size() > 0) {
                ZLogger.d(String.format("加载缓存数据(%s): %d个前台子类目", cacheKey, cacheData.size()));
                refreshCategoryGoodsTab(cacheData);
                loadSubCategory(categoryId, false);
                return;
            }
        }
        loadSubCategory(categoryId, true);
    }

    /**
     * 刷新子类目
     */
    private void refreshCategoryGoodsTab(List<PosCategory> items) {
        if (curCategoryList == null) {
            curCategoryList = new ArrayList<>();
        } else {
            curCategoryList.clear();
        }
        if (items != null) {
            curCategoryList.addAll(items);
        }
//        PosCategory rootClone = new PosCategory();
//        rootClone.setNameCn("全部");
//        rootClone.setId(categoryId);
//        rootClone.setParentId(categoryId);
//        curCategoryList.add(rootClone);

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        for (PosCategory category : curCategoryList) {
            Bundle args = new Bundle();
            args.putLong(EXTRA_CATEGORY_ID_POS, posFrontCategoryId);
            args.putLong("parentId", category.getParentId());
            args.putLong("categoryId", category.getId());

            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(),
                    FrontCategoryGoodsFragment.class, args));
//            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
        }
        categoryGoodsPagerAdapter.removeAll();
        categoryGoodsPagerAdapter.addAllTab(mTabs);

        mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
        if (mCategoryGoodsViewPager.getCurrentItem() == 0) {
            //如果直接加载，可能会出现加载两次的问题
            if (curCategoryList != null && curCategoryList.size() > 0) {
                Long categoryId = curCategoryList.get(0).getId();

                EventBus.getDefault().post(new FrontCategoryGoodsEvent(FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
        } else {
            mCategoryGoodsViewPager.setCurrentItem(0, false);
        }
    }

    /**
     * 加载前台类目子类目
     */
    private void loadSubCategory(final Long categoryId, final boolean isNeedRefresh) {
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
                        ZLogger.d(String.format("加载POS %d 前台子类目", (items != null ? items.size() : 0)));

                        //缓存数据
                        JSONArray cacheArrays = new JSONArray();
                        if (items != null && items.size() > 0) {
                            for (PosCategory item : items) {
                                cacheArrays.add(item);
                            }
                        }
                        ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
                        if (isNeedRefresh) {
                            refreshCategoryGoodsTab(items);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载POS前台子类目 失败, " + errMsg);
                        if (isNeedRefresh) {
                            refreshCategoryGoodsTab(null);
                        }
                    }
                }
                , PosCategory.class
                , CashierApp.getAppContext()) {
        };

        ScCategoryInfoApi.getCodeValue(categoryId, queryRsCallBack);
    }

    /**
     * 添加商品到类目
     */
    private void submit() {
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在发送请求...", false);

        List<PosCategoryGoodsTempEntity> entities = PosCategoryGodosTempService.getInstance().queryAll();

        if (entities == null || entities.size() < 1) {
            hideProgressDialog();
            DialogUtil.showHint("请先选择商品");
            return;
        }

        StringBuilder productIds = new StringBuilder();
        final StringBuilder proSkuIds = new StringBuilder();
        for (PosCategoryGoodsTempEntity entity : entities) {
            if (productIds.length() > 0) {
                productIds.append(",");
            }
            productIds.append(entity.getProductId());

            if (proSkuIds.length() > 0) {
                proSkuIds.append(",");
            }
            proSkuIds.append(entity.getProSkuId());
        }

        importFromCenterSkus(productIds.toString(), proSkuIds.toString());

    }


    /**
     * 建档
     * */
    private void importFromCenterSkus(final String productIds, String proSkuIds) {
        NetCallBack.NetTaskCallBack importRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("导入商品到本店仓储失败, " + errMsg);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //新建类目成功，保存类目信息，并触发同步。
                        ZLogger.df("导入商品到本店仓储成功");
                        add2Category(productIds);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        InvSkuStoreApiImpl.importFromCenterSkus(proSkuIds, importRC);
    }

    /**
     * 导入类目
     * */
    private void add2Category(String productIds){
        NetCallBack.NetTaskCallBack submitRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("导入前台类目商品失败, " + errMsg);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //新建类目成功，保存类目信息，并触发同步。
//                        {"code":"0","msg":"操作成功!","version":"1","data":""}
                        ZLogger.df("导入前台类目商品成功");
                        hideProgressDialog();
//                        if (rspData == null) {
//                            return;
//                        }

//                        DataSyncManagerImpl.get().sync();

                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };


        ProductCatalogApi.add2Category(String.valueOf(posFrontCategoryId),
                productIds, submitRC);
    }

}
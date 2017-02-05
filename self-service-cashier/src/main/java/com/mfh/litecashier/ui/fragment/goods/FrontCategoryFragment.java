package com.mfh.litecashier.ui.fragment.goods;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.bingshanguxue.vector_uikit.widget.NaviAddressView;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.anon.sc.ProductCatalogApi;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.ScCategoryInfoHttpManager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PosCategoryGoodsTempEntity;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.fragment.tenant.TenantCategoryListFragment;
import com.mfh.litecashier.utils.ACacheHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * 收银－－前台类目
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class FrontCategoryFragment extends BaseFragment {

    public static final String EXTRA_CATEGORY_ID_POS = "posFrontCategoryId";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.addressView)
    NaviAddressView mAddressView;
    @BindView(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @BindView(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    private Long posFrontCategoryId;//pos本地前台类目
    private Long categoryId;
    private CategoryInfo mCategoryInfo;//租户前台类目
    private String cacheKey;
    private List<CategoryInfo> curCategoryList;//当前子类目

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

//        mToolbar.setTitle("选择商品");
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
        //加载上一次选择的类目
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_LAST_TENANT_POSFRONTCATEGORY);
        ZLogger.d(String.format("cacheStr=%s", cacheStr));
        mCategoryInfo = JSONObject.toJavaObject(JSON.parseObject(cacheStr), CategoryInfo.class);

        if (mCategoryInfo != null){
            categoryId = mCategoryInfo.getId();
//            mToolbar.setTitle(mCategoryInfo.getNameCn());
            mAddressView.setText(mCategoryInfo.getNameCn());
            reload();
        }
        else{
            mAddressView.setText("");
            selectCategory();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_TENANT_CATEGORYLIST: {
                CategoryInfo categoryInfo = null;
                if (resultCode == Activity.RESULT_OK && data != null) {
                    categoryInfo = (CategoryInfo) data.getSerializableExtra("categoryInfo");
                }

                if (categoryInfo == null){
                    getActivity().finish();
                }
                else{
                    ACacheHelper.put(ACacheHelper.TCK_LAST_TENANT_POSFRONTCATEGORY,
                            JSONObject.toJSONString(categoryInfo));

                    mCategoryInfo = categoryInfo;
                    categoryId = mCategoryInfo.getId();
                    mAddressView.setText(mCategoryInfo.getNameCn());
                    reload();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void init(Bundle args) {
        if (args != null) {
            this.posFrontCategoryId = args.getLong(EXTRA_CATEGORY_ID_POS);
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
                EventBus.getDefault().post(new FrontCategoryGoodsEvent(
                        FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
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
            List<CategoryInfo> cacheData = JSONArray.parseArray(cacheStr, CategoryInfo.class);
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
    private void refreshCategoryGoodsTab(List<CategoryInfo> items) {
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
        for (CategoryInfo category : curCategoryList) {
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
        Map<String, String> options = new HashMap<>();
        options.put("parentId", String.valueOf(categoryId));
        ScCategoryInfoHttpManager.getInstance().getCodeValue(options, new Subscriber<List<CategoryInfo>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.d("加载POS前台子类目 失败, " + e.toString());
                if (isNeedRefresh) {
                    refreshCategoryGoodsTab(null);
                }
            }

            @Override
            public void onNext(List<CategoryInfo> categoryInfos) {
                ZLogger.d(String.format("加载POS %d 前台子类目", (categoryInfos != null ? categoryInfos.size() : 0)));

                //缓存数据
                JSONArray cacheArrays = new JSONArray();
                if (categoryInfos != null && categoryInfos.size() > 0) {
                    for (CategoryInfo item : categoryInfos) {
                        cacheArrays.add(item);
                    }
                }
                ACacheHelper.put(cacheKey, cacheArrays.toJSONString());
                if (isNeedRefresh) {
                    refreshCategoryGoodsTab(categoryInfos);
                }
            }
        });
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
     * 选择商品库
     * */
    @OnClick(R.id.addressView)
    public void selectCategory(){
        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FT_TENANT_POSCATEGORYLIST);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
        HostServer hostServer = GlobalInstanceBase.getInstance().getHostServer();
        if (hostServer != null){
            extras.putLong(TenantCategoryListFragment.EXTRA_KEY_TENANTID, hostServer.getSaasId());
        }

        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_TENANT_CATEGORYLIST);
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
                        ZLogger.df("导入商品到本店仓储成功，等待后台发送1101消息");
                        add2Category(productIds);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        InvSkuStoreApiImpl.importFromCenterSkus(proSkuIds, importRC);
    }

    /**
     * 导入前台类目
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
//                        {"code":"0","msg":"操作成功!","version":"1","data":""}
                        ZLogger.df("导入商品到前台类目成功，等待后台发送1105消息");
                        hideProgressDialog();
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
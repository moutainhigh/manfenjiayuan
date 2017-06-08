package com.mfh.litecashier.ui.fragment.goods.backend;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.category.CategoryOption;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PosCategoryGoodsTempEntity;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.ui.fragment.goods.FrontCategoryGoodsEvent;
import com.mfh.litecashier.ui.fragment.goods.IImportGoodsView;
import com.mfh.litecashier.ui.fragment.goods.ImportGoodsPresenter;
import com.mfh.litecashier.utils.ACacheHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 从平台商品库导入商品到pos前台类目
 *
 * Created by bingshanguxue on 15/8/30.
 */
public class BackendCategoryGoodsFragment extends BaseFragment implements IImportGoodsView {

    public static final String EXTRA_CATEGORY_ID_POS = "posFrontCategoryId";//从哪个pos前台类目进来到

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @BindView(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    private Long posFrontCategoryId;//pos本地前台类目
    private List<CategoryOption> curCategoryList;//当前子类目

    private ImportGoodsPresenter mImportGoodsPresenter;


    public static BackendCategoryGoodsFragment newInstance(Bundle args) {
        BackendCategoryGoodsFragment fragment = new BackendCategoryGoodsFragment();

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImportGoodsPresenter = new ImportGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        init(getArguments());

        mToolbar.setTitle("POS商品库");
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
                    submitStep1();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_addprodudts2category);

        initCategoryGoodsView();

        refreshCategoryGoodsTab();
        reload();
    }

    public void init(Bundle args) {
        if (args != null) {
            this.posFrontCategoryId = args.getLong(EXTRA_CATEGORY_ID_POS);
        }
    }

    private void initCategoryGoodsView() {
        mCategoryGoodsTabStrip.setOnClickTabListener(null);
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                String categoryCode = curCategoryList.get(page).getCode();
                EventBus.getDefault().post(new FrontCategoryGoodsEvent(
                        FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryCode));
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }

    /**
     * 加载数据
     */
    public void reload() {
    }

    /**
     * 刷新子类目
     */
    private void refreshCategoryGoodsTab() {
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_BACKEND_CATEGORY_TREE);
        List<CategoryOption> items = JSONArray.parseArray(cacheStr, CategoryOption.class);

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
        for (CategoryOption category : curCategoryList) {
            Bundle args = new Bundle();
            args.putLong(EXTRA_CATEGORY_ID_POS, posFrontCategoryId);
//            args.putLong("parentId", category.getParentId());
            args.putString("categoryId", category.getCode());

            mTabs.add(new ViewPageInfo(category.getValue(), category.getValue(),
                    BackendGoodsFragment.class, args));
//            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
        }
        categoryGoodsPagerAdapter.removeAll();
        categoryGoodsPagerAdapter.addAllTab(mTabs);

        mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
        if (mCategoryGoodsViewPager.getCurrentItem() == 0) {
            //如果直接加载，可能会出现加载两次的问题
            if (curCategoryList != null && curCategoryList.size() > 0) {
                String categoryCode = curCategoryList.get(0).getCode();

                EventBus.getDefault().post(new FrontCategoryGoodsEvent(FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryCode));
            }
        } else {
            mCategoryGoodsViewPager.setCurrentItem(0, false);
        }
    }

    /**
     * 导入商品到类目 - step1
     */
    private void submitStep1() {
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

        if (mImportGoodsPresenter != null) {
            mImportGoodsPresenter.importFromCenterSkus(posFrontCategoryId,
                    productIds.toString(), proSkuIds.toString());
        } else {
            hideProgressDialog();
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
        DialogUtil.showHint("导入商品成功");
        hideProgressDialog();
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();

    }
}
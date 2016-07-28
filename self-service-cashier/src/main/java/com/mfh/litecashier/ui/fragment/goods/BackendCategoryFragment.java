package com.mfh.litecashier.ui.fragment.goods;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.manfenjiayuan.business.bean.CategoryOption;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.FrontCategoryGoodsEvent;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 后台类目商品列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class BackendCategoryFragment extends BaseFragment {
    @Bind(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @Bind(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    private List<CategoryOption> curCategoryList;//当前子类目

    public static BackendCategoryFragment newInstance(Bundle args) {
        BackendCategoryFragment fragment = new BackendCategoryFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier_category;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initCategoryGoodsView();
        reload();
    }

    private void initCategoryGoodsView() {
        mCategoryGoodsTabStrip.setOnClickTabListener(null);
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(page);

                Long categoryId = viewPageInfo.args.getLong("categoryId");
                EventBus.getDefault().post(new FrontCategoryGoodsEvent(FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }

    /**
     * 加载数据
     */
    public void reload() {

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
            refreshCategoryGoodsTab(cacheData);

            return true;
        }

        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);

        return false;
    }

    /**
     * 刷新子类目
     */
    private void refreshCategoryGoodsTab(List<CategoryOption> items) {
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
            args.putLong("categoryId", category.getCode());
            mTabs.add(new ViewPageInfo(category.getValue(), category.getValue(),
                    BackendCategoryGoodsFragment.class, args));
//            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
        }
        categoryGoodsPagerAdapter.removeAll();
        categoryGoodsPagerAdapter.addAllTab(mTabs);

        mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
//        if (mCategoryGoodsViewPager.getCurrentItem() == 0) {
//            //如果直接加载，可能会出现加载两次的问题
//            if (curCategoryList != null && curCategoryList.size() > 0) {
//                EventBus.getDefault()
//                        .post(new FrontCategoryGoodsEvent(FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
//            }
//        } else {
            mCategoryGoodsViewPager.setCurrentItem(0, false);
//        }
    }

}
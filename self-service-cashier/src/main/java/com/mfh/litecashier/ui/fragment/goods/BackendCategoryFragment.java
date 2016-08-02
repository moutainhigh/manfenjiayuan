package com.mfh.litecashier.ui.fragment.goods;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.manfenjiayuan.business.bean.CategoryOption;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
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

    @Bind(R.id.letter_list)
    RecyclerView letterRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private LetterAdapter mLetterAdapter;

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
        initLetterRecyclerView();
        reload();
    }

    private void initCategoryGoodsView() {
        mCategoryGoodsTabStrip.setOnClickTabListener(null);
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(page);

                EventBus.getDefault().post(new PosCategoryGoodsEvent(PosCategoryGoodsEvent.EVENT_ID_RELOAD_DATA,
                        viewPageInfo.args));
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }

    private void initLetterRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getContext(), 6);
        letterRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        letterRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        letterRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.5f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        mLetterAdapter = new LetterAdapter(CashierApp.getAppContext(), null);
        mLetterAdapter.setOnAdapterLitener(new LetterAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Letter entity = mLetterAdapter.getEntity(position);
                if (entity != null) {
                    DialogUtil.showHint(String.format("选中%s(%s)",
                            entity.getName(), entity.getValue()));
                    ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(mCategoryGoodsTabStrip.getCurrentPosition());
                    Bundle args = viewPageInfo.args;
                    args.putString("sortLetter", entity.getValue());
                    EventBus.getDefault().post(new PosCategoryGoodsEvent(PosCategoryGoodsEvent.EVENT_ID_SORT_UPDATE, args));
                }
            }
        });
        letterRecyclerView.setAdapter(mLetterAdapter);

        // TODO: 7/28/16
        fetchLetters();
    }

    public static String[] A_Z = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};

    private void fetchLetters() {
        List<Letter> letters = new ArrayList<>();

        for (String az : A_Z) {
            Letter letter = new Letter();
            letter.setName(az);
            letter.setValue(az);
            letters.add(letter);

        }
        mLetterAdapter.setEntityList(letters);
    }

    /**
     * 加载数据
     */
    private void reload() {
//        if (!readCategoryInfoCache()) {
//            DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_BACKEND_CATEGORYINFO);
//        }

        List<CategoryOption> categoryOptions = new ArrayList<>();
        categoryOptions.add(makeSimpleCategoryInstance(3891L, "蔬菜豆菇"));
        categoryOptions.add(makeSimpleCategoryInstance(2448L, "新鲜水果"));
        categoryOptions.add(makeSimpleCategoryInstance(3903L, "海鲜水产"));
        categoryOptions.add(makeSimpleCategoryInstance(3942L, "肉禽蛋品"));
        categoryOptions.add(makeSimpleCategoryInstance(3958L, "熟食料理"));
        categoryOptions.add(makeSimpleCategoryInstance(3977L, "水台饮品"));
        categoryOptions.add(makeSimpleCategoryInstance(3175L, "烘焙面包"));
        refreshCategoryTab(categoryOptions);
    }

    private CategoryOption makeSimpleCategoryInstance(Long code, String value) {
        CategoryOption option = new CategoryOption();
        option.setCode(code);
        option.setValue(value);
        return option;
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
            refreshCategoryTab(cacheData);

            return true;
        }

        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);

        return false;
    }

    /**
     * 刷新类目TAB
     */
    private void refreshCategoryTab(List<CategoryOption> items) {
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        if (items != null && items.size() > 0) {
            for (CategoryOption category : items) {
                Bundle args = new Bundle();
                args.putLong("categoryId", category.getCode());
                mTabs.add(new ViewPageInfo(category.getValue(), category.getValue(),
                        BackendCategoryGoodsFragment.class, args));
//            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
            }
        }
        categoryGoodsPagerAdapter.removeAll();
        categoryGoodsPagerAdapter.addAllTab(mTabs);

        mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ZLogger.d("加载采购订单");
                ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(mCategoryGoodsTabStrip.getCurrentPosition());

                EventBus.getDefault().post(new PosCategoryGoodsEvent(PosCategoryGoodsEvent.EVENT_ID_RELOAD_DATA,
                        viewPageInfo.args));
            }
        }, 1000);

        if (mCategoryGoodsViewPager.getCurrentItem() == 0) {
//            //如果直接加载，可能会出现加载两次的问题
            if (mTabs.size() > 0) {
                EventBus.getDefault().post(new PosCategoryGoodsEvent(PosCategoryGoodsEvent.EVENT_ID_RELOAD_DATA,
                        mTabs.get(0).args));

//                EventBus.getDefault()
//                        .post(new PosCategoryGoodsEvent(PosCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
        } else {
            mCategoryGoodsViewPager.setCurrentItem(0, false);
        }
    }

}
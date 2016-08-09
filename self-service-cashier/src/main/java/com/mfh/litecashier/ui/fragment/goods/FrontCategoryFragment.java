package com.mfh.litecashier.ui.fragment.goods;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.framework.api.impl.CateApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.PosCategory;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 收银－－前台类目(公共&自定义)
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class FrontCategoryFragment extends BaseFragment {
    @Bind(R.id.tv_service_title)
    TextView tvTitle;
    @Bind(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @Bind(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    private Long categoryId;
    private String title;
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
        return R.layout.fragment_cashier_category;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        init(getArguments());

        initCategoryGoodsView();
        reload();
    }

    /**
     * 关闭二级服务台
     */
    @OnClick(R.id.btn_service_back)
    public void close() {
        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_HIDE_RIGHTSLIDE));
    }

    public void init(Bundle args) {
        if (args != null) {
            this.categoryId = args.getLong("categoryId");
            this.title = args.getString("title");
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

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }

    /**
     * 加载数据
     */
    public void reload() {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }

        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME).getAsString(cacheKey);
        List<PosCategory> cacheData = JSONArray.parseArray(cacheStr, PosCategory.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个前台子类目", cacheKey, cacheData.size()));
            refreshCategoryGoodsTab(cacheData);

            if (SharedPreferencesHelper.isSyncFrontCategorySubEnabled()) {
                //加载子类目
                loadSubCategory(categoryId, false);
            }
        } else {
            //加载子类目
            loadSubCategory(categoryId, true);
        }
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
        PosCategory rootClone = new PosCategory();
        rootClone.setNameCn("全部");
        rootClone.setId(categoryId);
        rootClone.setParentId(categoryId);
        curCategoryList.add(rootClone);

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        for (PosCategory category : curCategoryList) {
            Bundle args = new Bundle();
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
                        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                                .put(cacheKey, cacheArrays.toJSONString());
                        SharedPreferencesHelper.setSyncFrontCategorySubEnabled(false);

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

        CateApiImpl.listPublicCategory(categoryId, queryRsCallBack);
    }

}
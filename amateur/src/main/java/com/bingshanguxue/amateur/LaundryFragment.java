package com.mfh.litecashier.ui.fragment.cashier;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.manfenjiayuan.business.bean.CategoryInfo;
import com.manfenjiayuan.business.bean.CategoryOption;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.api.impl.CateApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.PosCategory;
import com.mfh.litecashier.event.LaundryGoodsEvent;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 收银服务－－洗衣
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class LaundryFragment extends BaseFragment {
    @Bind(R.id.btn_service_back)
    Button btnBack;
    @Bind(R.id.tv_service_title)
    TextView tvTitle;
    @Bind(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @Bind(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    private List<PosCategory> curCategoryList;//当前子类目

    public static LaundryFragment newInstance(Bundle args) {
        LaundryFragment fragment = new LaundryFragment();

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
        btnBack.setText("返回（收银）");
        tvTitle.setText("洗衣");

        initCategoryGoodsView();
        reload();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 关闭二级服务台
     */
    @OnClick(R.id.btn_service_back)
    public void close() {
//        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_HIDE_LAUNDRY));
//        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_HIDE_RIGHTSLIDE));
    }

    private void initCategoryGoodsView() {
        mCategoryGoodsTabStrip.setOnClickTabListener(null);
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                Long categoryId = curCategoryList.get(page).getId();
                EventBus.getDefault().post(new LaundryGoodsEvent(LaundryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }

    public void reload(){
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String cacheStr = ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .getAsString(ACacheHelper.CK_PUBLIC_LAUNDRY_FRONT_CATEGORY);
        List<PosCategory> cacheData = JSONArray.parseArray(cacheStr, PosCategory.class);
        if (cacheData != null && cacheData.size() > 0){
            ZLogger.d(String.format("加载缓存数据(%s): %d个洗衣子类目", ACacheHelper.CK_PUBLIC_LAUNDRY_FRONT_CATEGORY, cacheData.size()));
            refreshCategoryGoodsTab(cacheData);

            if (SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_PUBLIC_LAUNDRY_FRONTCATEGORY_ENABLED, true)){
                //加载子类目
//                loadSubCategory(CashierApi.POS_CATEGORY_LAUNDRY, false);
                downloadPublicFrontCategory(false);
            }
        } else{
            //加载子类目
//            loadSubCategory(CashierApi.POS_CATEGORY_LAUNDRY, true);
            downloadPublicFrontCategory(true);
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
        rootClone.setId(CateApi.POS_CATEGORY_LAUNDRY);
        rootClone.setParentId(CateApi.POS_CATEGORY_LAUNDRY);
        curCategoryList.add(rootClone);


        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        for (PosCategory category : curCategoryList) {
            Bundle args = new Bundle();
            args.putLong("parentId", category.getParentId());
            args.putLong("categoryId", category.getId());

            mTabs.add(new ViewPageInfo(category.getNameCn(),
                    category.getNameCn(), LaundryGoodsFragment.class, args));
//            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
        }
        categoryGoodsPagerAdapter.removeAll();
        categoryGoodsPagerAdapter.addAllTab(mTabs);

        mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
        if (mCategoryGoodsViewPager.getCurrentItem() == 0) {
            //如果直接加载，可能会出现加载两次的问题
            if (curCategoryList != null && curCategoryList.size() > 0) {
                Long categoryId = curCategoryList.get(0).getId();

                EventBus.getDefault().post(new LaundryGoodsEvent(LaundryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
        } else {
            mCategoryGoodsViewPager.setCurrentItem(0, false);
        }
    }

    /**
     * 下载公共洗衣类目
     * */
    private void downloadPublicFrontCategory(final boolean isNeedRefresh){

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())){
            return;
        }

        ZLogger.d("DataSync--同步洗衣公共类目开始");

        NetCallBack.NetTaskCallBack publicFrontCategoryRespCallback = new NetCallBack.NetTaskCallBack<CategoryInfo,
                NetProcessor.Processor<CategoryInfo>>(
                new NetProcessor.Processor<CategoryInfo>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("DataSync--加载洗衣公共类目树失败, " + errMsg);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData == null) {
                            saveLaundryFrontCategoryInfoCache(null);
                            return;
                        }
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<CategoryInfo> retValue = (RspBean<CategoryInfo>) rspData;
                        CategoryInfo categoryInfo = retValue.getValue();

                        downloadPublicFrontCategory2(categoryInfo, isNeedRefresh);
                    }
                }
                , CategoryInfo.class
                , CashierApp.getAppContext()) {
        };

        CateApiImpl.comnqueryCategory(CateApi.DOMAIN_TYPE_PROD, CateApi.CATE_POSITION_FRONT,
                CateApi.LAUNDRY, 1, null, publicFrontCategoryRespCallback);
    }

    /**
     * 加载洗衣类目子类目
     * */
    private void downloadPublicFrontCategory2(CategoryInfo categoryInfo, final boolean isNeedRefresh){
        if (categoryInfo == null){
            saveLaundryFrontCategoryInfoCache(null);
            return;
        }

        List<CategoryOption> options = categoryInfo.getOptions();
        if (options == null || options.size() < 1) {
            ZLogger.d("DataSync--洗衣公共类目为空");
            saveLaundryFrontCategoryInfoCache(null);
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())){
            //不做任何处理，不清空缓存数据。
            return;
        }

        //取第一个作为当前类目
        CategoryOption option = options.get(0);
        ZLogger.d(String.format("DataSync--同步洗衣公共二级类目(%s)开始", option.getValue()));

        NetCallBack.NetTaskCallBack queryRsCallBack = new NetCallBack.NetTaskCallBack<PosCategory,
                NetProcessor.Processor<PosCategory>>(
                new NetProcessor.Processor<PosCategory>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        List<PosCategory> items = new ArrayList<>();
                        if (rspData != null){
                            RspListBean<PosCategory> retValue = (RspListBean<PosCategory>) rspData;
                            items = retValue.getValue();
                        }
                        ZLogger.d(String.format("DataSync--加载POS %d个洗衣公共二级类目", (items != null ? items.size() : 0)));

                        saveLaundryFrontCategoryInfoCache(items);

                        if (isNeedRefresh){
                            refreshCategoryGoodsTab(items);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        1:12345卡芯片号不存在，请重新输入!
                        ZLogger.d("DataSync--加载POS洗衣公共二级类目 失败, " + errMsg);
                        if (isNeedRefresh){
                            refreshCategoryGoodsTab(null);
                        }
                    }
                }
                , PosCategory.class
                , CashierApp.getAppContext()) {
        };

        CateApiImpl.listPublicCategory(option.getCode(), queryRsCallBack);
    }


    /**
     * 缓存洗衣公共类目树
     * */
    private void saveLaundryFrontCategoryInfoCache(List<PosCategory> options){
        //缓存数据
        JSONArray cacheArrays = new JSONArray();
        if (options != null && options.size() > 0) {
            for (PosCategory option : options) {
//                PosCategory category = PosCategory.generateCloud(option.getCode(), option.getValue(), option.);
                cacheArrays.add(option);
            }
        }
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_PUBLIC_LAUNDRY_FRONT_CATEGORY, cacheArrays.toJSONString());

        //设置下次不需要自动更新商品类目，可以在收银页面点击同步按钮修改
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_LAUNDRY_FRONTCATEGORY_ENABLED, false);
    }

}
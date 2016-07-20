package com.mfh.litecashier.ui.fragment.cashier;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.PosCategory;
import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;
import com.mfh.litecashier.database.logic.CommonlyGoodsService;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.event.CommonlyGoodsEvent;
import com.mfh.litecashier.ui.adapter.TopFragmentPagerAdapter;
import com.mfh.litecashier.ui.widget.TopSlidingTabStrip;
import com.mfh.litecashier.utils.CashierHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 收银服务－－常用商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CommonlyFragment extends BaseFragment {
    @Bind(R.id.tv_service_title)
    TextView tvTitle;
    @Bind(R.id.ib_garbage)
    ImageButton ibGarbage;

    @Bind(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @Bind(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    public static CommonlyFragment newInstance(Bundle args) {
        CommonlyFragment fragment = new CommonlyFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier_commonly;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        tvTitle.setText("我的");
        initCategoryGoodsView();

        reload();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @OnClick(R.id.ib_garbage)
    public void clickGarbage() {
        if (ibGarbage.isSelected()) {
            ibGarbage.setSelected(false);
            EventBus.getDefault().post(new CommonlyGoodsEvent(CommonlyGoodsEvent.EVENT_ID_NORMAL_STATUS));
        } else {
            ibGarbage.setSelected(true);
            EventBus.getDefault().post(new CommonlyGoodsEvent(CommonlyGoodsEvent.EVENT_ID_REMOVE_STATUS));
        }
    }

    /**
     * 关闭二级服务台
     */
    @OnClick(R.id.btn_service_back)
    public void close() {
        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_HIDE_RIGHTSLIDE));
    }

    private void initCategoryGoodsView() {
        mCategoryGoodsTabStrip.setOnClickTabListener(null);
        //TODO
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                ibGarbage.setSelected(false);

                EventBus.getDefault().post(new CommonlyGoodsEvent(CommonlyGoodsEvent.EVENT_ID_RELOAD_DATA));
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(), mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }

    /**
     * 加载数据
     */
    public void reload() {
        try{

            ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

            List<PosCategory> cloudCategoryList = CashierHelper.readFrontCatetoryCache();
            if (cloudCategoryList != null && cloudCategoryList.size() > 0){
                for (PosCategory category : cloudCategoryList) {
                    //只显示有商品的类目
                    List<CommonlyGoodsEntity> entityList = CommonlyGoodsService.get().queryAllByDesc(String.format("categoryId = '%d'", category.getId()));
                    if (entityList != null && entityList.size() > 0) {
                        Bundle args = new Bundle();
                        args.putLong("categoryId", category.getId());
                        mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), CommonlyGoodsFragment.class, args));
                    }
                }
            }

            mTabs.add(new ViewPageInfo("全部", "全部", CommonlyGoodsFragment.class, null));
            categoryGoodsPagerAdapter.removeAll();
            categoryGoodsPagerAdapter.addAllTab(mTabs);
            mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
            mCategoryGoodsViewPager.setCurrentItem(0);

            ibGarbage.setSelected(false);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }

    }


}
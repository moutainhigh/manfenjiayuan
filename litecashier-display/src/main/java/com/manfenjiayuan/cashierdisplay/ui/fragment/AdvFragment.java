package com.manfenjiayuan.cashierdisplay.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.cashierdisplay.R;
import com.manfenjiayuan.cashierdisplay.ui.adapter.AdvertisementPagerAdapter;
import com.manfenjiayuan.cashierdisplay.ui.view.AdvertisementViewPager;
import com.mfh.framework.uikit.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by bingshanguxue on 16/3/25.
 */
public class AdvFragment extends BaseFragment {


    @Bind(R.id.tv_adv)
    TextView tvAdv;
    @Bind(R.id.viewpager_adv)
    AdvertisementViewPager advertiseViewPager;
    private AdvertisementPagerAdapter advertisePagerAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_adv;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            cashierOrderInfo = (CashierOrderInfo)args.getSerializable("cashierOrderInfo");
//        }

        refresh();
    }

    private void refresh(){
//TODO,加载广告数据，然后再填充广告
        List<String> advList = new ArrayList<>();
        //multi
        advList.add("http://resource.manfenjiayuan.cn/product/thumbnail_1294.jpg");
        advList.add(null);
//        advList.add("http://chunchunimage.b0.upaiyun.com/product/3655.JPG!small"));
//        advList.add("http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small"));
//        //simple
//        advList.add("http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small"));
        advertisePagerAdapter = new AdvertisementPagerAdapter(getActivity(),
                advList, null);
        advertiseViewPager.setAdapter(advertisePagerAdapter);

        //TODO,定时切换(每隔5秒切换一次)
        advertiseViewPager.startSlide(5 * 1000);
    }
}

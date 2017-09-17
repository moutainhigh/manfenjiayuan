package com.mfh.litecashier.components;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.uikit.adv.AdvLocalPic;
import com.mfh.framework.uikit.adv.AdvLocalPicAdapter;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * Created by bingshanguxue on 8/9/16.
 */
public class HomeAdvFragment extends BaseFragment{

    @BindView(R.id.viewpager_adv)
    AdvertisementViewPager advertiseViewPager;
    private AdvLocalPicAdapter mPictureAdvPagerAdapter;

    public static HomeAdvFragment newInstance(Bundle args) {
        HomeAdvFragment fragment = new HomeAdvFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home_adv;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        List<AdvLocalPic> localAdvList = new ArrayList<>();
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.hb1));
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.hb4));
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.hb1));
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.hb4));
        mPictureAdvPagerAdapter = new AdvLocalPicAdapter(getContext(), localAdvList, null);
        advertiseViewPager.setAdapter(mPictureAdvPagerAdapter);
        //TODO,定时切换(每隔5秒切换一次)
        advertiseViewPager.startSlide(5 * 1000);
    }
}

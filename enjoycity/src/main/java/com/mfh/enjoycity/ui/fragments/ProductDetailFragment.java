package com.mfh.enjoycity.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.activity.NativeWebViewActivity;
import com.mfh.enjoycity.ui.advertise.AdvertisementViewPager;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;


/**
 * 商品详情·
 * 
 * @author Nat.ZZN created on 2015-04-13
 * @since Framework 1.0
 */
public class ProductDetailFragment extends BaseFragment {

    @Bind(R.id.viewpager_product)
    AdvertisementViewPager bannerViewPager;


    public ProductDetailFragment() {
        super();
    }



    @Override
    public int getLayoutResId() {
        return R.layout.fragment_product_detail;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 注意：不能使用getActivity()启动startActivityForResult，
     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     * */
    private void redirectToNativeWebForResult(String url, boolean bNeedSyncCookie, int requestCode){
        Intent intent = new Intent(getActivity(), NativeWebViewActivity.class);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_BACKASHOMEUP, false);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 注意：不能使用getActivity()启动startActivityForResult，
     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     * */
    private void redirectToJBWebForResult(String url, boolean bNeedSyncCookie, int animType, int requestCode){
        Intent intent = new Intent(getActivity(), HybridActivity.class);
        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, animType);
        startActivityForResult(intent, requestCode);
    }

}

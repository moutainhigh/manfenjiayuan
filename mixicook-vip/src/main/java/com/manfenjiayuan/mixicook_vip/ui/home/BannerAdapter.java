package com.manfenjiayuan.mixicook_vip.ui.home;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 广告图片显示适配器
 * Created by bingshanguxue on 2015/4/20.
 */
public class BannerAdapter<V extends AdvertisementViewPager>  extends PagerAdapter {

    private Context context;//用于接收传递过来的Context对象
    private List<Banner> data = new ArrayList<>();
    private V mViewPager;

    public interface OnBannerAdapterCallback {
        void onRedirectTo(String url);
    }
    private OnBannerAdapterCallback callback;

    public BannerAdapter(Context context, List<Banner> data, V viewPager, OnBannerAdapterCallback callback) {
        this.context = context;
        this.data = data;
        mViewPager = viewPager;
        this.callback = callback;
    }

    //    public BannerAdapter(Context context) {
//        super();
//        this.context = context;
//    }
//
//    public BannerAdapter(Context context, OnBannerAdapterCallback callback) {
//        super();
//        this.context = context;
//        this.callback = callback;
//    }
//
//    public BannerAdapter(Context context, List<Banner> data, OnBannerAdapterCallback callback) {
//        super();
//        this.context = context;
//        this.data = data;
//        this.callback = callback;
//    }

    @Override
    public int getCount() {
        int realCount = getRealCount();
        if (realCount > 1){
            realCount = getFakeCount();
            realCount = realCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : realCount;
        }
        return realCount;
    }

    public int getRealCount(){
        return data != null ? data.size() : 0;
    }

    public int getFakeCount(){
        return getRealCount() * 3;
    }



    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    //java.lang.UnsupportedOperationException: Required method destroyItem was not overridden
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position %= getRealCount();
        final Banner item = data.get(position);

        View view = View.inflate(context, R.layout.itemview_banner, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_banner);

        Glide.with(context)
                .load(item.getUrl())
                .error(R.mipmap.ic_image_error).into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null){
                    callback.onRedirectTo(item.getLink());
                }
            }
        });

        container.addView(view);

        return view;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
//        super.finishUpdate(container);
        int position = mViewPager.getCurrentItem();
        ZLogger.d("finish update before, position=" + position);
        if (position == 0) {
            position = getRealCount();
            mViewPager.setCurrentItem(position, false);
        } else if (position == getCount() - 1) {
            position = getRealCount() - 1;
            mViewPager.setCurrentItem(position, false);
        }
        ZLogger.d("finish update after, position=" + position);
    }


    public void setData(List<Banner> data){
        this.data = data;
        this.notifyDataSetChanged();
    }

}

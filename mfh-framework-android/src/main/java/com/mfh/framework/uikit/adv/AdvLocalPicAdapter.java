package com.mfh.framework.uikit.adv;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mfh.framework.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 广告图片显示适配器
 * Created by bingshanguxue on 2015/4/20.
 */
public class AdvLocalPicAdapter <V extends AdvertisementViewPager> extends PagerAdapter {
    private Context context;//用于接收传递过来的Context对象
    private List<AdvLocalPic> data = new ArrayList<>();
    private V mViewPager;

    public interface AdvertisementCallback {
        void onRedirectTo(String url);
    }
    private AdvertisementCallback callback;
//
//    public AdvLocalPicAdapter(Context context) {
//        this(context, null, null);
//    }
//
//    public AdvLocalPicAdapter(Context context, AdvertisementCallback callback) {
//        this(context, null, callback);
//    }

    public AdvLocalPicAdapter(Context context, List<AdvLocalPic> data, V viewPager,
                              AdvertisementCallback callback) {
        super();
        this.context = context;
        this.data = data;
        this.mViewPager = viewPager;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        int realCount = getRealCount();
        if (realCount > 1) {
            realCount = getFakeCount();
            if (realCount > Integer.MAX_VALUE) {
                realCount = Integer.MAX_VALUE;
            }
        }
        return realCount;
    }

    public int getRealCount() {
        return data != null ? data.size() : 0;
    }

    public int getFakeCount() {
        return getRealCount() * 2;
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
        final AdvLocalPic item = data.get(position);

        View view = View.inflate(context, R.layout.adv_item_pic, null);
        ImageView ivAdv = (ImageView) view.findViewById(R.id.adv_pic);
//        ivAdv.setBackgroundResource(item.getResId());
        ivAdv.setImageResource(item.getResId());


        container.addView(view);

        return view;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
//        super.finishUpdate(container);
        try {
            int position = mViewPager.getCurrentItem();
//        ZLogger.d("finish update before, position=" + position);
            if (position == 0) {
                position = getRealCount();
            } else if ((getCount() - 1 > 0) && position == getCount() - 1) {
                position = getRealCount() - 1;
            }

            mViewPager.setCurrentItem(position, false);
//        ZLogger.d("finish update after, position=" + position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setData(List<AdvLocalPic> data){
        this.data = data;
        this.notifyDataSetChanged();
    }

}

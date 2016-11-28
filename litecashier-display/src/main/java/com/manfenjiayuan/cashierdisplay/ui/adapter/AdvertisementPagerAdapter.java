package com.manfenjiayuan.cashierdisplay.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.cashierdisplay.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 广告图片显示适配器
 * Created by Administrator on 2015/4/20.
 */
public class AdvertisementPagerAdapter extends PagerAdapter {
    private Context context;//用于接收传递过来的Context对象
    private List<String> entityList = new ArrayList<>();

    public interface AdvertisementCallback {
        void onRedirectTo(String url);
    }
    private AdvertisementCallback callback;

    public AdvertisementPagerAdapter(Context context) {
        this(context, null, null);
    }

    public AdvertisementPagerAdapter(Context context, AdvertisementCallback callback) {
        this(context, null, callback);
    }

    public AdvertisementPagerAdapter(Context context, List<String> entityList,
                                     AdvertisementCallback callback) {
        super();
        this.context = context;
        this.entityList = entityList;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return entityList != null ? entityList.size() : 0;
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
    public Object instantiateItem(ViewGroup container, final int position) {
        final String url = entityList.get(position);

        View view = View.inflate(context, R.layout.itemview_adv, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_adv);

        Glide.with(context).load(url).error(R.mipmap.ic_image_error).into(imageView);
        container.addView(view);

        return view;
    }

    public void setEntityList(List<String> entityList){
        this.entityList = entityList;
        this.notifyDataSetChanged();
    }

}

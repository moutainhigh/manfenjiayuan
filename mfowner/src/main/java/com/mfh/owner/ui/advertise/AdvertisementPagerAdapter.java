package com.mfh.owner.ui.advertise;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mfh.owner.R;
import com.mfh.owner.bean.AdvertisePic;

import java.util.ArrayList;
import java.util.List;

/**
 * 广告图片显示适配器
 * Created by Administrator on 2015/4/20.
 */
public class AdvertisementPagerAdapter extends PagerAdapter {
    private Context context;//用于接收传递过来的Context对象
    private List<AdvertisePic> data = new ArrayList<>();

    public interface AdvertisementCallback {
        void onRedirectTo(String url);
    }
    private AdvertisementCallback callback;

    public AdvertisementPagerAdapter(Context context) {
        super();
        this.context = context;
    }

    public AdvertisementPagerAdapter(Context context, AdvertisementCallback callback) {
        super();
        this.context = context;
        this.callback = callback;
    }

    public AdvertisementPagerAdapter(Context context, List<AdvertisePic> data, AdvertisementCallback callback) {
        super();
        this.context = context;
        this.data = data;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == (View)o;
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
        final AdvertisePic item = data.get(position);

        View view = View.inflate(context, R.layout.pageview_item_advertisement, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_advertisement);
        //TODO
        imageView.setImageResource(R.drawable.ic_launcher);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null){
                    callback.onRedirectTo(item.getRedirectUrl());
                }
            }
        });

        container.addView(view);

        return view;
    }

    public void setData(List<AdvertisePic> data){
        this.data = data;
        this.notifyDataSetChanged();
    }

}

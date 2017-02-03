package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.AdvertiseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 广告图片显示适配器
 * Created by bingshanguxue on 2015/4/20.
 */
public class AdvertisementPagerAdapter extends PagerAdapter {
    private Context context;//用于接收传递过来的Context对象
    private List<AdvertiseBean> data = new ArrayList<>();

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

    public AdvertisementPagerAdapter(Context context, List<AdvertiseBean> data,
                                     AdvertisementCallback callback) {
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
        final AdvertiseBean item = data.get(position);

        View view = View.inflate(context, R.layout.fragment_cashier_adv, null);
        if (item.getAdvType() == AdvertiseBean.ADV_TYPE_MULTI){
            LinearLayout frameAdv = (LinearLayout) view.findViewById(R.id.frame_adv);
            LinearLayout frameAdvPic = (LinearLayout) view.findViewById(R.id.frame_adv_pic);
            ImageView ivBigPanel = (ImageView) view.findViewById(R.id.iv_adv_bigpanel);
            frameAdv.setVisibility(View.VISIBLE);
            frameAdvPic.setVisibility(View.VISIBLE);
            ivBigPanel.setVisibility(View.GONE);

            AvatarView ivHeader = (AvatarView) view.findViewById(R.id.iv_adv_header);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_adv_title);
            TextView tvSubTitle = (TextView) view.findViewById(R.id.tv_adv_subtitle);
//        kjb.display(ivHeader, item.getImageUrl(), R.mipmap.ic_image_error, 0, 0,null);
            ivHeader.setAvatarUrl(item.getImageUrl());
            tvTitle.setText(item.getTitle());
            tvSubTitle.setText(item.getSubTitle());

            ImageView ivPic0 = (ImageView) view.findViewById(R.id.adv_pic_0);
            ImageView ivPic1 = (ImageView) view.findViewById(R.id.adv_pic_1);
            ImageView ivPic2 = (ImageView) view.findViewById(R.id.adv_pic_2);
            ImageView ivPic3 = (ImageView) view.findViewById(R.id.adv_pic_3);
            ImageView ivPic4 = (ImageView) view.findViewById(R.id.adv_pic_4);
            ImageView ivPic5 = (ImageView) view.findViewById(R.id.adv_pic_5);
            Glide.with(context).load(item.getImageUrl()).error(R.mipmap.ic_image_error).into(ivPic0);
            Glide.with(context).load(item.getImageUrl()).error(R.mipmap.ic_image_error).into(ivPic1);
            Glide.with(context).load(item.getImageUrl()).error(R.mipmap.ic_image_error).into(ivPic2);
            Glide.with(context).load(item.getImageUrl()).error(R.mipmap.ic_image_error).into(ivPic3);
            Glide.with(context).load(item.getImageUrl()).error(R.mipmap.ic_image_error).into(ivPic4);
            Glide.with(context).load(item.getImageUrl()).error(R.mipmap.ic_image_error).into(ivPic5);
        }
        else{
            LinearLayout frameAdv = (LinearLayout) view.findViewById(R.id.frame_adv);
            LinearLayout frameAdvPic = (LinearLayout) view.findViewById(R.id.frame_adv_pic);
            ImageView ivBigPanel = (ImageView) view.findViewById(R.id.iv_adv_bigpanel);
            frameAdv.setVisibility(View.GONE);
            frameAdvPic.setVisibility(View.GONE);
            ivBigPanel.setVisibility(View.VISIBLE);
        }
        container.addView(view);

        return view;
    }


    public void setData(List<AdvertiseBean> data){
        this.data = data;
        this.notifyDataSetChanged();
    }

}

package com.mfh.framework.uikit.adv;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.R;
import com.mfh.framework.uikit.viewpagertransformer.DepthPageTransformer;
import com.mfh.framework.uikit.widget.HorizontalInnerViewPager;


/**
 * -----------------------------------------------------------------
 *
 * Copyright (C) 2014  ChunChen
 *
 * AdvertisementViewPager
 * 
 * @version: v1.0.0 
 *
 * @author: ZZN
 *
 * Create Date: 2014.08.22
 *
 * -----------------------------------------------------------------
 */
public class AdvertisementViewPager extends RelativeLayout
		implements OnPageChangeListener {

	private Context                     context;
	private HorizontalInnerViewPager mViewPager;
	/** Describe the title of the current page */
	private TextView 		mDescriptionTextView;
	/** Point layout of the current page*/
	private LinearLayout 	mPointLinearLayout;

	/**point image resource id*/
	private int 			mNormalPointImageResid;
	private int 			mSelectPointImageResid;

	private boolean 		isShowPoint = true;
	/**previous page index*/
	private int				mPreviousItem = 0;


	public AdvertisementViewPager(Context context) {
		super(context);
		this.context = context;
		initView();
	}

	public AdvertisementViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int i, float v, int j) {
	}

	@Override
	public void onPageSelected(int position) {
		//设置小圆点
		if (isShowPoint) {
			changePointImageState(position, true);
			changePointImageState(mPreviousItem, false);
			mPreviousItem = position;
		}
	}

	private void initView() {
		View.inflate(context, R.layout.advertisement_scroll_viewpage, this);
		mViewPager = (HorizontalInnerViewPager) this.findViewById(R.id.item_viewpage);
		mDescriptionTextView = (TextView) this.findViewById(R.id.item_tv_desc);
		mPointLinearLayout = (LinearLayout) this.findViewById(R.id.item_llayout_point);
		mNormalPointImageResid = R.drawable.dot_normal;
		mSelectPointImageResid = R.drawable.dot_focus;

		//切换动画
//		mViewPager.setPageTransformer(true, new ScalePageTransformer());
		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.addOnPageChangeListener(this);
//        mViewPager.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (listener != null){
//                    listener.onClick(getCurrentItem());
//                }
//            }
//        });
	}
	
	public void setAdapter(PagerAdapter adapter) {
		mViewPager.setAdapter(adapter);
		if (isShowPoint) {
			mPointLinearLayout.removeAllViews();
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(4, 4, 4, 4);
			for(int i = 0; i < adapter.getCount(); i++) {
				ImageView pointImage = new ImageView(context);
				int resId = 0==i ? mSelectPointImageResid : mNormalPointImageResid;
				pointImage.setLayoutParams(params);
				pointImage.setBackgroundResource(resId);
				mPointLinearLayout.addView(pointImage);
			}
		}
	}
	
	/**
	 * point image resource id
	 * @param normal
	 * @param select
	 */
	public void setPointImageResId(int normal, int select) {
		this.mNormalPointImageResid = normal;
		this.mSelectPointImageResid = select;
	}
	
	/**
	 * Set title of the current page
	 * @param description
	 */
	public void setDesctiption(String description) {
		mDescriptionTextView.setText(description);
	}


	public void setCurrentItem(int item) {
		//fasle,不显示跳转过程的动画
		mViewPager.setCurrentItem(item, false);
	}

	public void setCurrentItem(int item, boolean smoothScroll) {
		//fasle,不显示跳转过程的动画
		mViewPager.setCurrentItem(item, smoothScroll);
	}
	
	public int getCurrentItem() {
		return mViewPager.getCurrentItem();
	}

    public void setShowPoint(boolean isShowPoint) {
        this.isShowPoint = isShowPoint;
    }

    private void nextPage() {
		//最后一页，跳转到第一页
		if (getCurrentItem() == mPointLinearLayout.getChildCount() - 1) {
			setCurrentItem(0, false);
		}
		else {
			setCurrentItem(getCurrentItem() + 1, true);
		}
	}

	private void changePointImageState(int index, boolean isSelect) {
		ImageView poiImageView = (ImageView) mPointLinearLayout.getChildAt(index);
		if (poiImageView == null){
			return;
		}
		int resId = isSelect ? mSelectPointImageResid : mNormalPointImageResid;
		poiImageView.setBackgroundResource(resId);
	}


	/**
	 * start auto slide
	 * @param intervalTime  slide  interval time
	 */
	public void startSlide(int intervalTime) {
		mViewPager.startupTimer(intervalTime);
	}

	/**
	 * stop auto slide
	 * Suggest to destroy at the interface between the time to stop
	 */
	public void stopSlide() {
		mViewPager.shutdownTimer();
	}

	public void setTimerEnabled(boolean timerEnabled) {
		mViewPager.setTimerEnabled(timerEnabled);
	}


}

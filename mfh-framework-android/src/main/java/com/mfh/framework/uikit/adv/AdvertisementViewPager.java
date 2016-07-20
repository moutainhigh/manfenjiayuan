package com.mfh.framework.uikit.adv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
public class AdvertisementViewPager extends RelativeLayout implements OnPageChangeListener {

	private static final int WHAT = 1;

	private Context                     context;
	private HorizontalInnerViewPager mViewPager;
	/** Describe the title of the current page */
	private TextView 		mDescriptionTextView;
	/** Point layout of the current page*/
	private LinearLayout 	mPointLinearLayout;

	/**Whether automatic scroll*/
	private boolean 		isAuto = false;
	private int 			mIntervalTime;
	/** whether stop auto scroll when touching, default is true **/
	private boolean			stopScrollWhenTouch = true;
	private boolean			isStopByTouch = false;

	private Handler 		mHandler;

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
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (stopScrollWhenTouch) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN && isAuto) {
				isStopByTouch = true;
				stopSlide();
			}
			else if (ev.getAction() == MotionEvent.ACTION_UP && isStopByTouch) {
				startSlide(mIntervalTime);
			}
		}
		return super.dispatchTouchEvent(ev);
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

	
	/**
     * set whether stop auto scroll when touching, default is true
     * 
     * @param stopScrollWhenTouch
     */
	public void setStopScrollWhenTouch(boolean stopScrollWhenTouch) {
		this.stopScrollWhenTouch = stopScrollWhenTouch;
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
		int resId = isSelect ? mSelectPointImageResid : mNormalPointImageResid;
		poiImageView.setBackgroundResource(resId);
	}


	/**
	 * start auto slide
	 * @param intervalTime  slide  interval time
	 */
	public void startSlide(int intervalTime) {
		isAuto = true;
		this.mIntervalTime = intervalTime;
		if (mHandler == null) {
			mHandler = new MHandler();
		}
		mHandler.removeMessages(WHAT);
		mHandler.sendEmptyMessageDelayed(WHAT, mIntervalTime);
	}

	/**
	 * stop auto slide
	 * Suggest to destroy at the interface between the time to stop
	 */
	public void stopSlide() {
		isAuto = false;
		if (mHandler != null) {
			mHandler.removeMessages(WHAT);
		}
	}

	@SuppressLint("HandlerLeak")
	private class MHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			if (isAuto) {
				nextPage();
				mHandler.sendEmptyMessageDelayed(WHAT, mIntervalTime);
			}
		}
	}

}

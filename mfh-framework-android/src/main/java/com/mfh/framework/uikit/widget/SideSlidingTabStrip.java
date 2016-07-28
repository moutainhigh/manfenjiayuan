/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mfh.framework.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.mfh.framework.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 垂直滑动菜单
 * Created by bingshanguxue on 15/8/18.
 * */
public class SideSlidingTabStrip extends ScrollView implements
		View.OnClickListener {
	private int currentPosition; // 当前位置
	private int lastOffset;
	private int lastScrollX = 0;
	private float currentPositionOffset; // 当前位置偏移量
	private boolean start;
	private boolean allowWidthFull; // 内容宽度无法充满时，允许自动调整Item的宽度以充满
	private boolean disableViewPager; // 禁用ViewPager
	private View currentSelectedTabView; // 当前标题项
	private Drawable slidingBlockDrawable; // 滑块
	private ViewPager viewPager; // ViewPager
	private ViewGroup tabsLayout; // 标题项布局
	private ViewPager.OnPageChangeListener onPageChangeListener; // 页面改变监听器
	private OnClickTabListener onClickTabListener;
	private OnPagerChangeLis listener;
	private List<View> tabViews;

	public SideSlidingTabStrip(Context context) {
		this(context, null);
	}

	public SideSlidingTabStrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHorizontalScrollBarEnabled(false); // 隐藏横向滑动提示条

		if (attrs != null) {
			TypedArray attrsTypedArray = context.obtainStyledAttributes(attrs,
					R.styleable.PagerSlidingTabStrip);
			if (attrsTypedArray != null) {
				allowWidthFull = attrsTypedArray.getBoolean(
						R.styleable.PagerSlidingTabStrip_allowWidthFull, false);
				slidingBlockDrawable = attrsTypedArray
						.getDrawable(R.styleable.PagerSlidingTabStrip_slidingBlock);
				disableViewPager = attrsTypedArray.getBoolean(
						R.styleable.PagerSlidingTabStrip_disableViewPager,
						false);
				attrsTypedArray.recycle();
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (!allowWidthFull)
			return;
		ViewGroup tabsLayout = getTabsLayout();
		if (tabsLayout == null
				|| tabsLayout.getMeasuredWidth() >= getMeasuredWidth())
			return;
		if (tabsLayout.getChildCount() <= 0)
			return;

		if (tabViews == null) {
			tabViews = new ArrayList<>();
		} else {
			tabViews.clear();
		}
		for (int w = 0; w < tabsLayout.getChildCount(); w++) {
			tabViews.add(tabsLayout.getChildAt(w));
		}

		adjustChildHeightWithParent(
				tabViews,
				getMeasuredHeight() - tabsLayout.getPaddingTop()
						- tabsLayout.getPaddingBottom(), widthMeasureSpec,
				heightMeasureSpec);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 调整views集合中的View，让所有View的高度加起来正好等于parentViewHeight
	 * 
	 * @param views
	 *            子View集合
	 * @param parentViewHeight
	 *            父Vie的 高度
	 * @param parentWidthMeasureSpec
	 *            父View的宽度规则
	 * @param parentHeightMeasureSpec
	 *            父View的高度规则
	 */
	private void adjustChildHeightWithParent(List<View> views,
			int parentViewHeight, int parentWidthMeasureSpec,
			int parentHeightMeasureSpec) {
		// 先去掉所有子View的外边距
		for (View view : views) {
			if (view.getLayoutParams() instanceof MarginLayoutParams) {
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view
						.getLayoutParams();
				parentViewHeight -= lp.topMargin + lp.bottomMargin;
			}
		}

		// 去掉高度大于平均高度的View后再次计算平均高度
		int averageHeight = parentViewHeight / views.size();
		int bigTabCount = views.size();
		while (true) {
			Iterator<View> iterator = views.iterator();
			while (iterator.hasNext()) {
				View view = iterator.next();
				if (view.getMeasuredWidth() > averageHeight) {
					parentViewHeight -= view.getMeasuredHeight();
					bigTabCount--;
					iterator.remove();
				}
			}
			averageHeight = parentViewHeight / bigTabCount;
			boolean end = true;
			for (View view : views) {
				if (view.getMeasuredWidth() > averageHeight) {
					end = false;
				}
			}
			if (end) {
				break;
			}
		}

		// 修改宽度小于新的平均宽度的View的宽度
		for (View view : views) {
			if (view.getMeasuredHeight() < averageHeight) {
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view
						.getLayoutParams();
				layoutParams.height = averageHeight;
				view.setLayoutParams(layoutParams);
				// 再次测量让新宽度生效
				if (layoutParams instanceof MarginLayoutParams) {
					measureChildWithMargins(view, parentWidthMeasureSpec, 0,
							parentHeightMeasureSpec, 0);
				} else {
					measureChild(view, parentWidthMeasureSpec,
							parentHeightMeasureSpec);
				}
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		ViewGroup tabViewGroup = getTabsLayout();
		if (tabViewGroup != null) {
			// 初始化滑块位置以及选中状态
			currentPosition = viewPager != null ? viewPager.getCurrentItem()
					: 0;
			if (!disableViewPager) {
				scrollToChild(currentPosition, 0); // 移动滑块到指定位置
				selectedTab(currentPosition); // 选中指定位置的TAB
			}

			// 给每一个tab设置点击事件，当点击的时候切换Pager
			for (int w = 0; w < tabViewGroup.getChildCount(); w++) {
				View itemView = tabViewGroup.getChildAt(w);
				itemView.setTag(w);
				itemView.setOnClickListener(this);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int index = (Integer) v.getTag();
		if (onClickTabListener != null) {
			onClickTabListener.onClickTab(v, index);
		}
		if (viewPager != null) {
			viewPager.setCurrentItem(index, false);
		}
	}


	public void setCurrentItem(int index, boolean smoothScroll){
		if (viewPager != null) {
			viewPager.setCurrentItem(index, smoothScroll);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (disableViewPager)
			return;
		/* 绘制滑块 */
		ViewGroup tabsLayout = getTabsLayout();
		if (tabsLayout != null && tabsLayout.getChildCount() > 0
				&& slidingBlockDrawable != null) {
			View currentTab = tabsLayout.getChildAt(currentPosition);
			if (currentTab != null) {
				float slidingBlockLeft = currentTab.getLeft();
				float slidingBlockRight = currentTab.getRight();
				if (currentPositionOffset > 0f
						&& currentPosition < tabsLayout.getChildCount() - 1) {
					View nextTab = tabsLayout.getChildAt(currentPosition + 1);
					if (nextTab != null) {
						final float nextTabLeft = nextTab.getLeft();
						final float nextTabRight = nextTab.getRight();
						slidingBlockLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset)
								* slidingBlockLeft);
						slidingBlockRight = (currentPositionOffset
								* nextTabRight + (1f - currentPositionOffset)
								* slidingBlockRight);
					}
				}
				slidingBlockDrawable.setBounds((int) slidingBlockLeft, 0,
						(int) slidingBlockRight, getHeight());
				slidingBlockDrawable.draw(canvas);
			}
		}
	}

	/**
	 * 获取布局
	 */
	private ViewGroup getTabsLayout() {
		if (tabsLayout == null) {
			if (getChildCount() > 0) {
				tabsLayout = (ViewGroup) getChildAt(0);
			} else {
				removeAllViews();
//				tabsLayout = new LinearLayout(getContext());
				tabsLayout = (LinearLayout)LayoutInflater.from(getContext()).inflate(
						R.layout.side_sliding_tabstrip, null, false);
//				addView(tabsLayout, new ViewGroup.LayoutParams(
//						ViewGroup.LayoutParams.WRAP_CONTENT,
//						ViewGroup.LayoutParams.WRAP_CONTENT));
				addView(tabsLayout, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
			}
		}
		return tabsLayout;
	}

	/**
	 * 滚动到指定的位置
	 */
	private void scrollToChild(int position, int offset) {
		ViewGroup tabsLayout = getTabsLayout();
		if (tabsLayout != null && tabsLayout.getChildCount() > 0
				&& position < tabsLayout.getChildCount()) {
			View view = tabsLayout.getChildAt(position);
			if (view != null) {
				// 计算新的X坐标
				int newScrollX = view.getLeft() + offset;
				if (position > 0 || offset > 0) {
					newScrollX -= 240 - getOffset(view.getWidth()) / 2;
				}

				// 如果同上次X坐标不一样就执行滚动
				if (newScrollX != lastScrollX) {
					lastScrollX = newScrollX;
					scrollTo(newScrollX, 0);
				}
			}
		}
	}

	/**
	 * 获取偏移量
	 */
	private int getOffset(int newOffset) {
		if (lastOffset < newOffset) {
			if (start) {
				lastOffset += 1;
				return lastOffset;
			} else {
				start = true;
				lastOffset += 1;
				return lastOffset;
			}
		}
		if (lastOffset > newOffset) {
			if (start) {
				lastOffset -= 1;
				return lastOffset;
			} else {
				start = true;
				lastOffset -= 1;
				return lastOffset;
			}
		} else {
			start = true;
			lastOffset = newOffset;
			return lastOffset;
		}
	}

	/**
	 * 选中指定位置的TAB
	 */
	public void selectedTab(int currentSelectedTabPosition) {
		ViewGroup tabsLayout = getTabsLayout();
		if (currentSelectedTabPosition > -1 && tabsLayout != null
				&& currentSelectedTabPosition < tabsLayout.getChildCount()) {
			if (currentSelectedTabView != null) {
				currentSelectedTabView.setSelected(false);
			}
			currentSelectedTabView = tabsLayout
					.getChildAt(currentSelectedTabPosition);
			if (currentSelectedTabView != null) {
				currentSelectedTabView.setSelected(true);
			}
		}
	}

	/**
	 * 添加Tab
	 */
	public void addTab(View tabView, int index) {
		if (tabView != null) {
			getTabsLayout().addView(tabView, index);
			requestLayout();
		}
	}

	/**
	 * 添加Tab
	 */
	public void addTab(View tabView) {
		addTab(tabView, -1);
	}

	/**
	 * 添加Tab
	 * 
	 * @param tabViews
	 *            可以一次添加多个Tab
	 */
	public void addTab(View... tabViews) {
		if (tabViews != null) {
			for (View view : tabViews) {
				getTabsLayout().addView(view);
			}
			requestLayout();
		}
	}

	/**
	 * 添加Tab
	 */
	public void addTab(List<View> tabViews) {
		if (tabViews != null) {
			for (View view : tabViews) {
				getTabsLayout().addView(view);
			}
			requestLayout();
		}
	}
	
	/**
	 * 移除一个tab
	 * 
	 * @param index
	 */
	public void removeTab(int index) {
		removeTab(index, 1);
	}
	
	/**
	 * 移除tab
	 * 
	 * @param start 开始位置
	 * @param count 移除的数量
	 */
	public void removeTab(int start, int count) {
		int tabCount = getTabCount();
		if (start < 0 || start > tabCount) {
			start = 0;
		}
		if (count < 0 || count > tabCount) {
			count = 1;
		}
		if (count - start > tabCount) {
			count = tabCount - start;
		}
		
		getTabsLayout().removeViews(start, count);
		requestLayout();
	}
	
	/**
	 * 移除所有
	 */
	public void removeAllTab() {
		getTabsLayout().removeAllViews();
		requestLayout();
	}
	
	public View getChild(int idx) {
		return getTabsLayout().getChildAt(idx);
	}

	public View getBadgeView(int i) {
		return getTabsLayout().getChildAt(i).findViewById(R.id.tab_mes);
	}

	/**
	 * 设置ViewPager
	 * 
	 * @param viewPager
	 *            ViewPager
	 */
	public void setViewPager(ViewPager viewPager) {
		if (disableViewPager)
			return;
		this.viewPager = viewPager;
		this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				ViewGroup tabsLayout = getTabsLayout();
				if (position < tabsLayout.getChildCount()) {
					View view = tabsLayout.getChildAt(position);
					if (view != null) {
						currentPosition = position;
						currentPositionOffset = positionOffset;
						scrollToChild(position,
								(int) (positionOffset * view.getWidth()));
						invalidate();
					}
				}
				if (onPageChangeListener != null) {
					onPageChangeListener.onPageScrolled(position,
							positionOffset, positionOffsetPixels);
				}
			}

			@Override
			public void onPageSelected(int position) {

//				ZLogger.d("Main onPageSelected " + position);
				selectedTab(position);
				if (onPageChangeListener != null) {
					onPageChangeListener.onPageSelected(position);
				}
				if (listener != null)
					listener.onChanged(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (onPageChangeListener != null) {
					onPageChangeListener.onPageScrollStateChanged(state);
				}
			}
		});
//		//deprecated
//		this.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//			@Override
//			public void onPageSelected(int position) {
//				selectedTab(position);
//				if (onPageChangeListener != null) {
//					onPageChangeListener.onPageSelected(position);
//				}
//				if (listener != null)
//					listener.onChanged(position);
//			}
//
//			@Override
//			public void onPageScrolled(int nextPagePosition,
//					float positionOffset, int positionOffsetPixels) {
//				ViewGroup tabsLayout = getTabsLayout();
//				if (nextPagePosition < tabsLayout.getChildCount()) {
//					View view = tabsLayout.getChildAt(nextPagePosition);
//					if (view != null) {
//						currentPosition = nextPagePosition;
//						currentPositionOffset = positionOffset;
//						scrollToChild(nextPagePosition,
//								(int) (positionOffset * view.getWidth()));
//						invalidate();
//					}
//				}
//				if (onPageChangeListener != null) {
//					onPageChangeListener.onPageScrolled(nextPagePosition,
//							positionOffset, positionOffsetPixels);
//				}
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int arg0) {
//				if (onPageChangeListener != null) {
//					onPageChangeListener.onPageScrollStateChanged(arg0);
//				}
//			}
//		});
		requestLayout();
	}

	/**
	 * 设置Page切换监听器
	 * 
	 * @param onPageChangeListener
	 *            Page切换监听器
	 */
	public void setOnPageChangeListener(
			ViewPager.OnPageChangeListener onPageChangeListener) {
		this.onPageChangeListener = onPageChangeListener;
	}

	/**
	 * 设置是否充满屏幕
	 * 
	 * @param allowWidthFull
	 *            true：当内容的宽度无法充满屏幕时，自动调整每一个Item的宽度以充满屏幕
	 */
	public void setAllowWidthFull(boolean allowWidthFull) {
		this.allowWidthFull = allowWidthFull;
		requestLayout();
	}

	/**
	 * 设置滑块图片
	 */
	public void setSlidingBlockDrawable(Drawable slidingBlockDrawable) {
		this.slidingBlockDrawable = slidingBlockDrawable;
		requestLayout();
	}


	/**
	 * 获取Tab总数
	 */
	public int getTabCount() {
		ViewGroup tabsLayout = getTabsLayout();
		return tabsLayout != null ? tabsLayout.getChildCount() : 0;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * 设置Tab点击监听器
	 * 
	 * @param onClickTabListener
	 *            Tab点击监听器
	 */
	public void setOnClickTabListener(OnClickTabListener onClickTabListener) {
		this.onClickTabListener = onClickTabListener;
	}

	/**
	 * 设置不使用ViewPager
	 * 
	 * @param disableViewPager
	 *            不使用ViewPager
	 */
	public void setDisableViewPager(boolean disableViewPager) {
		this.disableViewPager = disableViewPager;
		if (viewPager != null) {
			viewPager.addOnPageChangeListener(onPageChangeListener);
			viewPager = null;
		}
		requestLayout();
	}

	/**
	 * Tab点击监听器
	 * 
	 * @author xiaopan
	 * 
	 */
	public interface OnClickTabListener {
		void onClickTab(View tab, int index);
	}

	public void setOnPagerChange(OnPagerChangeLis l) {
		this.listener = l;
	}

	public interface OnPagerChangeLis {
		void onChanged(int page);
	}
}
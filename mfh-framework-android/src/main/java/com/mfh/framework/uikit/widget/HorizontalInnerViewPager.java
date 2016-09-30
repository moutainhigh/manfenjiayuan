package com.mfh.framework.uikit.widget;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自定义ViewPager，解决ViewPagger嵌套使用时不滑动问题。
 * Created by NAT.ZZN on 2015/4/20.
 */
public class HorizontalInnerViewPager extends ViewPager {
    @IntDef({RESUME, PAUSE, DESTROY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LifeCycle {
    }

    public static final int RESUME = 0;
    public static final int PAUSE = 1;
    public static final int DESTROY = 2;
    /**
     * 生命周期状态，保证{@link #mCarouselTimer}在各生命周期选择执行策略
     */
    private int mLifeCycle = RESUME;
    /**
     * 是否正在触摸状态，用以防止触摸滑动和自动轮播冲突
     */
    private boolean mIsTouching = false;
    /**
     * 轮播定时器
     */
    private ScheduledExecutorService mCarouselTimer;
    private static final long DEFAULT_PERIOD = 1000 * 2;

    /**
     * 触摸时按下的点
     **/
    PointF downP = new PointF();
    /**
     * 触摸时当前的点
     **/
    PointF curP = new PointF();

    private GestureDetector mGestureDetector;

    public HorizontalInnerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new XScrollDetector());
    }

    public HorizontalInnerViewPager(Context context) {
        super(context);

        mGestureDetector = new GestureDetector(context, new XScrollDetector());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);//default
        //当拦截触摸事件到达此位置的时候，返回true，
        //说明将onTouch拦截在此控件，进而执行此控件的onTouchEvent
//        return true;
        //接近水平滑动时子控件处理该事件，否则交给父控件处理
//        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //每次进行onTouch事件都记录当前的按下的坐标
        curP.x = ev.getX();
        curP.y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mIsTouching = true;
                //记录按下时候的坐标
                //切记不可用 downP = curP ，这样在改变curP的时候，downP也会改变
                downP.x = ev.getX();
                downP.y = ev.getY();
                //此句代码是为了通知他的父ViewPager现在进行的是本控件的操作，不要对我的操作进行干扰
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                mIsTouching = true;
                float distanceX = curP.x - downP.x;
                float distanceY = curP.y - downP.y;
                //接近水平滑动，ViewPager控件捕获手势，水平滚动
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    //此句代码是为了通知他的父ViewPager现在进行的是本控件的操作，不要对我的操作进行干扰
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    //接近垂直滑动，交给父控件处理
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                mIsTouching = false;
//            //在up时判断是否按下和松手的坐标为一个点
//            //如果是一个点，将执行点击事件，这是我自己写的点击事件，而不是onclick
//            if(downP.x==curP.x && downP.y==curP.y){
////                onSingleTouch();
//                return true;
//            }
                float distanceX = curP.x - downP.x;
                float distanceY = curP.y - downP.y;
                if (Math.abs(distanceX) > Math.abs(distanceY)) {

                }
            }
            break;
            case MotionEvent.ACTION_CANCEL: {
                mIsTouching = false;
            }
            break;
            default:
                break;
        }

        return super.onTouchEvent(ev);
    }

    private class XScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            return super.onScroll(e1, e2, distanceX, distanceY);

            //接近水平滑动时子控件处理该事件，否则交给父控件处理
            return (Math.abs(distanceX) > Math.abs(distanceY));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        shutdownTimer();
        startupTimer(DEFAULT_PERIOD);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        shutdownTimer();
    }

    /**
     * 开启定时器
     * */
    public void startupTimer(long period){
        if (mCarouselTimer == null){
            mCarouselTimer = Executors.newSingleThreadScheduledExecutor();
        }
        mCarouselTimer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                switch (mLifeCycle) {
                    case RESUME:
                        if (!mIsTouching
                                && getAdapter() != null
                                && getAdapter().getCount() > 1) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    setCurrentItem(getCurrentItem() + 1);
                                }
                            });
                        }
                        break;
                    case PAUSE:
                        break;
                    case DESTROY:
                        shutdownTimer();
                        break;
                }
            }
        }, 1000 * 2, period, TimeUnit.MILLISECONDS);
    }
    /**
     * 关闭定时器
     * */
    public void shutdownTimer() {
        if (mCarouselTimer != null && !mCarouselTimer.isShutdown()) {
            mCarouselTimer.shutdown();
        }
        mCarouselTimer = null;
    }
}

package com.mfh.framework.uikit.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mfh.framework.R;


/**
 * 分割线
 * Created by Nat.ZZN(bingshanguxue) on 15/8/14.
 */
public class LineItemDecoration extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{
            R.attr.itemdecoration_line
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;

    private int mOrientation;   //方向
    private int mMargin = 0;    //留白
    private int mStroke = 0;    //描边


    public LineItemDecoration(Context context, int orientation) {
        this(context, orientation, 0, 0);
    }

    public LineItemDecoration(Context context, int orientation, int margin) {
        this(context, orientation, margin, 0);
    }

    public LineItemDecoration(Context context, int orientation, int margin, int stroke) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
        setMargin(margin);
        setStroke(stroke);

    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }


    public void setMargin(int margin) {
        this.mMargin = margin;
    }

    public void setStroke(int stroke) {
        mStroke = stroke;
    }

    //装饰的绘制在Item条目绘制之前调用，所以这有可能被Item的内容所遮挡
    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
//        ZLogger.v("recyclerview - itemdecoration.onDraw()");

        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft() + mMargin;
        final int right = parent.getWidth() - parent.getPaddingRight() - mMargin;

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView v = new RecyclerView(parent.getContext());
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop() + mMargin;
        final int bottom = parent.getHeight() - parent.getPaddingBottom() - mMargin;

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight() + mStroke);
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth() + mStroke, 0);
        }
    }
}

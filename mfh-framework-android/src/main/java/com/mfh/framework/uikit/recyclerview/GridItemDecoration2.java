package com.mfh.framework.uikit.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * Created by Nat.ZZN(bingshanguxue) on 15/8/14.
 */
public class GridItemDecoration2 extends RecyclerView.ItemDecoration {
    private Paint paintInner, paintCellStroke, paintBorder;
    private int paintInnerWidth, paintCellStokeWidth, paintBorderStokeWidth;
    private int offset;

//    Bitmap bitmap;
//    int bitmap_w, bitmap_h;
//    Rect rectSrc;

    public GridItemDecoration2(Context c){
        offset = 10;
        paintInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintInner.setColor(Color.BLUE);
        paintInner.setStyle(Paint.Style.STROKE);
        paintInner.setStrokeWidth(3);

        paintCellStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCellStroke.setColor(Color.RED);
        paintCellStroke.setStyle(Paint.Style.STROKE);
        paintCellStroke.setStrokeWidth(1);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setColor(Color.GREEN);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(10);

//        bitmap = BitmapFactory.decodeResource(
//                c.getResources(),
//                android.R.drawable.ic_menu_info_details);
//        bitmap_w = bitmap.getWidth();
//        bitmap_h = bitmap.getHeight();
//        rectSrc = new Rect(0, 0, bitmap_w, bitmap_h);
    }

    public GridItemDecoration2(Context c, int offset, int innerColor, float paintInnerStokeWidth,
                               int cellStrokeColor, float paintCellStokeWidth,
                               int borderColor, float paintBorderStokeWidth){
        //边框
        this.offset = offset;

        //元素四边颜色
        paintInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintInner.setColor(innerColor);
        paintInner.setStyle(Paint.Style.STROKE);
        paintInner.setStrokeWidth(paintInnerStokeWidth);

        //
        paintCellStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCellStroke.setColor(cellStrokeColor);
        paintCellStroke.setStyle(Paint.Style.STROKE);
        paintCellStroke.setStrokeWidth(paintCellStokeWidth);

        //边框
        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setColor(borderColor);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(paintBorderStokeWidth);
//
//        bitmap = BitmapFactory.decodeResource(
//                c.getResources(),
//                android.R.drawable.ic_menu_info_details);
//        bitmap_w = bitmap.getWidth();
//        bitmap_h = bitmap.getHeight();
//        rectSrc = new Rect(0, 0, bitmap_w, bitmap_h);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(offset, offset, offset, offset);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        c.drawRect(
                0,
                0,
                c.getWidth(),
                c.getHeight(),
                paintBorder);

        for(int i=0; i<parent.getChildCount(); i++){
            final View child = parent.getChildAt(i);
            c.drawRect(
                    layoutManager.getDecoratedLeft(child),
                    layoutManager.getDecoratedTop(child),
                    layoutManager.getDecoratedRight(child),
                    layoutManager.getDecoratedBottom(child),
                    paintInner);
            c.drawRect(
                    layoutManager.getDecoratedLeft(child) + offset,
                    layoutManager.getDecoratedTop(child) + offset,
                    layoutManager.getDecoratedRight(child) - offset,
                    layoutManager.getDecoratedBottom(child) - offset,
                    paintCellStroke);
        }

    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
//        c.drawBitmap(bitmap,
//                rectSrc,
//                new Rect(
//                        c.getWidth()-(2*bitmap_w),
//                        c.getHeight()-(2*bitmap_h),
//                        c.getWidth(),
//                        c.getHeight()),
//                null);
    }

}

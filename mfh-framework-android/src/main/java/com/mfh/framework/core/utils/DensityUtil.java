package com.mfh.framework.core.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 *  dp*ppi/160 = px。比如1dp x 320ppi/160 = 2px。
 * Created by Nat.ZZN(bingshanguxue) on 14-5-27.
 */
public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) ,
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    /**
     *  sp 与 px 的换算公式：sp*ppi/160 = px
     **/
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(pxValue / fontScale + 0.5F);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(spValue * fontScale + 0.5F);
    }

//    public static int getDialogW(Context aty) {
//        new DisplayMetrics();
//        DisplayMetrics dm = aty.getResources().getDisplayMetrics();
//        int w = dm.widthPixels - 100;
//        return w;
//    }
//
//    public static int getScreenW(Context aty) {
//        new DisplayMetrics();
//        DisplayMetrics dm = aty.getResources().getDisplayMetrics();
//        int w = dm.widthPixels;
//        return w;
//    }
//
    public static int getScreenH(Context aty) {
        new DisplayMetrics();
        DisplayMetrics dm = aty.getResources().getDisplayMetrics();
        int h = dm.heightPixels;
        return h;
    }

    // 屏幕宽度（像素）
    public static int getWindowWidth(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    // 屏幕高度（像素）
    public static int getWindowHeight(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }
}

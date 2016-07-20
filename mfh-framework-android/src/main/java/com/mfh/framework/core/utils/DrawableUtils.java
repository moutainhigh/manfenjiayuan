package com.mfh.framework.core.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Created by NAT.ZZN(bingshanguxue) on 16/1/22.
 */
public class DrawableUtils {

    /**
     * 着色
     * */
    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors,
                                        boolean mutateable) {
        if (mutateable){
            return tintDrawable(drawable.mutate(), colors);
        }
        else{
            return tintDrawable(drawable, colors);
        }
    }

    /**
     * 着色
     * */
    public static Drawable tintDrawable(Context context, Drawable drawable,
                                        int colorResId, boolean mutateable) {
        if (mutateable){
            return tintDrawable(context, drawable.mutate(), colorResId);
        }
        else{
            return tintDrawable(context, drawable, colorResId);
        }
    }

    /**
     * 着色
     * */
    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }
    /**
     * 着色
     * */
    public static Drawable tintDrawable(Context context, Drawable drawable, int colorResId) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        ColorStateList colorStateList = context.getResources().getColorStateList(colorResId);
        DrawableCompat.setTintList(wrappedDrawable, colorStateList);
        return wrappedDrawable;
    }
}

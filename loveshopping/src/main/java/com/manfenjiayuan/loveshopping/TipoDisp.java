package com.manfenjiayuan.loveshopping;

import android.content.Context;
import android.util.DisplayMetrics;

public class TipoDisp {

    public static int alt_tabs(Context cont) {
        int alt;
        int dx, dy;
        DisplayMetrics metrics = cont.getResources().getDisplayMetrics();

        dx = metrics.widthPixels;
        dy = metrics.heightPixels;
        if (dx < dy)
            alt = dy / 15;
        else
            alt = dy / 10;

        return alt;
    }
}
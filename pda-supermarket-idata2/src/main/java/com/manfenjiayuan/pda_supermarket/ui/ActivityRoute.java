package com.manfenjiayuan.pda_supermarket.ui;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by bingshanguxue on 17/10/2016.
 */

public class ActivityRoute {
    /**
     * 跳转到商品
     * */
    public static void redirect2Goods(Context context){
        if (context == null){
            return;
        }

        Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_GOODS);
        PrimaryActivity.actionStart(context, extras);
    }
}

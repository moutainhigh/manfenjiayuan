package com.manfenjiayuan.pda_supermarket.ui;

import android.content.Context;
import android.os.Bundle;

import com.bingshanguxue.pda.bizz.goods.ScSkuGoodsStoreInFragment;
import com.mfh.framework.uikit.base.BaseActivity;

/**
 * Created by bingshanguxue on 17/10/2016.
 */

public class ActivityRoute {
    /**
     * 跳转页面
     * */
    public static void redirect2Primary(Context context, int type){
        if (context == null){
            return;
        }

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, type);
        PrimaryActivity.actionStart(context, extras);
    }

    /**
     * 跳转到商品
     * */
    public static void redirect2Goods(Context context){
        if (context == null){
            return;
        }

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_GOODS);
        PrimaryActivity.actionStart(context, extras);
    }

    /**
     * 跳转到商品建档
     * @param storeType  {@link com.mfh.framework.api.constant.StoreType#SUPERMARKET}
     * */
    public static void redirect2StoreIn(Context context, Integer storeType){
        if (context == null){
            return;
        }

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_STORE_IN);
        extras.putInt(ScSkuGoodsStoreInFragment.EXTRA_STORE_TYPE, storeType);
        PrimaryActivity.actionStart(context, extras);
    }
}

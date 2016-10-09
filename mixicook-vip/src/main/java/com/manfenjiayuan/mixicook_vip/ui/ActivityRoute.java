package com.manfenjiayuan.mixicook_vip.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.manfenjiayuan.mixicook_vip.ui.hybrid.HybridFragment;
import com.manfenjiayuan.mixicook_vip.ui.shopcart.ShopcartFragment;
import com.mfh.framework.uikit.base.BaseActivity;

/**
 * Created by bingshanguxue on 08/10/2016.
 */

public class ActivityRoute {
    /**
     * 跳转到URL
     * */
    public static void redirect2Url(Context context, String url){
        if (context == null){
            return;
        }
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_HYBRID);
        extras.putString(HybridFragment.EXTRA_KEY_ORIGINALURL, url);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 跳转到购物车
     */
    public static void redirect2Cart(Context context, Long shopId) {
        if (context == null){
            return;
        }

        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "购物车");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_SHOPCART);
        extras.putLong(ShopcartFragment.EXTRA_KEY_SHOP_ID, shopId);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 支付
     */
    public static void redirect2QuickPay(Context context){
        if (context == null){
            return;
        }
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "支付");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_QUICK_PAY);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 充值
     */
    public static void redirect2Topup(Context context){
        if (context == null){
            return;
        }
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "充值");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_TOPUP);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

}

package com.manfenjiayuan.mixicook_vip.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.manfenjiayuan.mixicook_vip.ui.goods.CategoryGoodsFragment;
import com.manfenjiayuan.mixicook_vip.ui.hybrid.HybridFragment;
import com.manfenjiayuan.mixicook_vip.ui.shopcart.ShopcartFragment;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

/**
 * Created by bingshanguxue on 08/10/2016.
 */

public class ActivityRoute {
    /**
     * 跳转到URL
     */
    public static void redirect2Url(Context context, String url) {
        if (context == null) {
            return;
        }

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_HYBRID);
        extras.putString(HybridFragment.EXTRA_KEY_ORIGINALURL, url);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 跳转到URL
     */
    public static void redirect2Url2(Context context, String url) {
        if (context == null || StringUtils.isEmpty(url)) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_HYBRID);
        extras.putString(HybridFragment.EXTRA_KEY_ORIGINALURL, url);
        Intent intent = new Intent(context, FragmentActivity.class);
        //Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag
//        if (context instanceof Activity){
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }

        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 跳转到购物车
     */
    public static void redirect2Cart(Context context, Reciaddr reciaddr, CompanyInfo companyInfo) {
        if (context == null) {
            return;
        }

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_SHOPCART);
        if (reciaddr != null){
            extras.putSerializable(ShopcartFragment.EXTRA_KEY_ADDRESSINFO, reciaddr);
        }
        if (companyInfo != null){
            extras.putSerializable(ShopcartFragment.EXTRA_KEY_COMPANYINFO, companyInfo);
        }
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 支付
     */
    public static void redirect2QuickPay(Context context) {
        if (context == null) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_QUICK_PAY);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 通知
     */
    public static void redirect2Notification(Context context) {
        if (context == null) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_MY_NOTIFICATION);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /**
     * 充值
     */
    public static void redirect2Topup(Context context) {
        if (context == null) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_TOPUP);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    public static void redirect2Topup2(Activity activity) {
        if (context == null) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_TOPUP);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        activity.startActivityForResult(intent, ARCode.ARC_MY_TOPUP);
    }

    /**
     * 商品类目
     */
    public static void redirect2CategoryGoods(Context context, Long netId,
                                              Long frontCategoryId, String categoryName) {
        if (context == null) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_CATEGORY_GOODS);
        extras.putLong(CategoryGoodsFragment.EXTRA_KEY_NET_ID, netId);
        extras.putLong(CategoryGoodsFragment.EXTRA_KEY_FRONTCATEGORY_ID, frontCategoryId);
        extras.putString(CategoryGoodsFragment.EXTRA_KEY_CATEGORYNAME, categoryName);
        Intent intent = new Intent(context, FragmentActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

}

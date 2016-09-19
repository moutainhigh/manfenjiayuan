package com.mfh.litecashier.ui;

import android.content.Context;
import android.os.Bundle;

import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.ui.activity.FragmentActivity;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.fragment.goods.ScSkuGoodsStoreInFragment;

/**
 * 活动视图路由
 * Created by bingshanguxue on 9/8/16.
 */
public class ActivityRoute {

    public static void redirect(){

    }

    /**
     * 消息管理器
     */
    public static void redirect2MsgMgr(Context context) {
        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE,
                FragmentActivity.FT_CANARY_MESSAGE_MGR);
        UIHelper.startActivity(context, FragmentActivity.class, extras);
    }

    /**
     * 商品建档
     * */
    public static void redirect2StoreIn(Context context, String barcode){
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE,
                SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleDialogActivity.FRAGMENT_TYPE_CREATE_PURCHASE_GOODS);
        extras.putString(ScSkuGoodsStoreInFragment.EXTRY_KEY_BARCODE, barcode);
        UIHelper.startActivity(context, SimpleDialogActivity.class, extras);
    }

}

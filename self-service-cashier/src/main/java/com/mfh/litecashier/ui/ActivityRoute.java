package com.mfh.litecashier.ui;

import android.content.Context;
import android.os.Bundle;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.ui.activity.FragmentActivity;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.fragment.goods.ScSkuGoodsStoreInFragment;

/**
 * 活动视图路由
 * Created by bingshanguxue on 9/8/16.
 */
public class ActivityRoute {

    /**
     * 手动订货&智能订货
     */
    public static void redirect2Purchase(Context context) {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_PURCHASE_MANUAL);
        UIHelper.startActivity(context, SimpleActivity.class, extras);
    }

    /**
     * 库存
     */
    public static void redirect2Inventory(Context context) {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_INVENTORY);
        UIHelper.startActivity(context, SimpleActivity.class, extras);
    }

    /**
     * 单据
     */
    public static void redirect2Receipt(Context context) {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_RECEIPT);
        UIHelper.startActivity(context, SimpleActivity.class, extras);
    }

    /**
     * 设置
     */
    public static void redirect2Settings(Context context) {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_SETTINGS);
        UIHelper.startActivity(context, SimpleActivity.class, extras);
    }

    /**
     * 现金授权
     */
    public static void redirect2CashQuota(Context context) {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FT_CANARY_CASH_QUOTA);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
//        extras.putString(DailySettleFragment.EXTRA_KEY_DATETIME, datetime);
//        extras.putBoolean(DailySettleFragment.EXTRA_KEY_CANCELABLE, cancelable);
        UIHelper.startActivity(context, SimpleDialogActivity.class, extras);
    }

    /**
     * 商品
     */
    public static void redirect2CanaryGoods(Context context) {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_CANARY_GOODS);
        UIHelper.startActivity(context, SimpleActivity.class, extras);
    }

    /**
     * 订单流水
     */
    public static void redirect2CanaryOrderflow(Context context) {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_CANARY_ORDERFLOW);
        UIHelper.startActivity(context, SimpleActivity.class, extras);
    }

    /**
     * 订单列表
     */
    public static void redirect2OrderList(Context context) {
        ZLogger.df(">>>打开线上订单页面");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE, FragmentActivity.FT_ORDER);
        UIHelper.startActivity(context, FragmentActivity.class, extras);

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

    /**
     * 积分兑换
     * */
    public static void redirect2ExchangeScore(Context context){
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE,
                SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleDialogActivity.FT_EXCHANGE_SCORE);
        UIHelper.startActivity(context, SimpleDialogActivity.class, extras);
    }

    /**
     * 买手接单
     * */
    public static void redirect2PrepareAbleOrders(Context context){
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE,
                SimpleDialogActivity.DT_MIDDLE);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleDialogActivity.FT_PREPAREABLE_ORDER);
        UIHelper.startActivity(context, SimpleDialogActivity.class, extras);
    }

    /**
     * 商品流水
     */
    public static void redirect2GoodsFlow(Context context) {
//        ZLogger.df(String.format("准备日结：datetime = %s, cancelable = %b", datetime, cancelable));
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FT_GOODS_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
//        extras.putString(DailySettleFragment.EXTRA_KEY_DATETIME, datetime);
        UIHelper.startActivity(context, SimpleDialogActivity.class, extras);

    }

    /**
     * 一键反馈
     * */
    public static void redirect2OnekeyFeedback(Context context){
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE,
                SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleDialogActivity.FT_ONEKEY_FEEDBACK);
        UIHelper.startActivity(context, SimpleDialogActivity.class, extras);
    }
}

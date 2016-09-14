package com.mfh.litecashier.ui;

import android.content.Context;
import android.os.Bundle;

import com.mfh.framework.uikit.UIHelper;
import com.mfh.litecashier.ui.activity.FragmentActivity;

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
}

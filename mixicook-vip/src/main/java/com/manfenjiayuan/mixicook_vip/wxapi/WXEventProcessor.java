package com.manfenjiayuan.mixicook_vip.wxapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;


/**
 * Created by bingshanguxue on 2015/6/5.
 */
public class WXEventProcessor extends BroadcastReceiver implements IWXAPIEventHandler {
    //override broadcast receiver
    @Override
    public void onReceive(Context context, Intent intent) {

    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {

    }
}

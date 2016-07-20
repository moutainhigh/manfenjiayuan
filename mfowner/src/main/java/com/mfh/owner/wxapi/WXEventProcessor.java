package com.mfh.owner.wxapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

/**
 * Created by Administrator on 2015/6/5.
 */
public class WXEventProcessor extends BroadcastReceiver implements IWXAPIEventHandler{
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

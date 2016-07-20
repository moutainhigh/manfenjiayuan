package com.manfenjiayuan.pda_supermarket.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.igexin.sdk.PushConsts;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.helper.PayloadHelper;

/**
 * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息
 * Created by Nat.ZZN(bingshanguxue) on 2015/08/17.
 */
public class PushDemoReceiver extends BroadcastReceiver {

    private static final String KEY_CLIENT_ID   = "clientid";
    private static final String KEY_PAYLOAD     = "payload";
    private static final String KEY_APPID       = "appid";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        ZLogger.df(String.format("PushDemoReceive--onReceive, action=%d", bundle.getInt("action")));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:{
                // 获取透传（payload）数据
                byte[] payload = bundle.getByteArray(KEY_PAYLOAD);
                if (payload != null){
                    String data = new String(payload);
                    parsePushPayload(context, data);
                }
                // String appid = bundle.getString(KEY_APPID);
            }
                break;
            case PushConsts.GET_CLIENTID:{
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String clientId = bundle.getString(KEY_CLIENT_ID);
                if(clientId != null){
                    ZLogger.df(String.format("PushDemoReceive--cashier.clientId=%s", clientId));
                    IMConfig.savePushClientId(clientId);
                }

                // TODO:
                /* 第三方应用需要将ClientID上传到第三方服务器，并且将当前用户帐号和ClientID进行关联，
                以便以后通过用户帐号查找ClientID进行消息推送。有些情况下ClientID可能会发生变化，为保证获取最新的ClientID，
                请应用程序在每次获取ClientID广播后，都能进行一次关联绑定 */
                //注册到消息桥
                IMClient.getInstance().registerBridge();
//                UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_PARTER_REFRESH);
            }
                break;
            case PushConsts.THIRDPART_FEEDBACK:
                break;

            case PushConsts.GET_SDKONLINESTATE:
                ZLogger.d("PushDemoReceive--sdk.online");
                break;
            case PushConsts.GET_SDKSERVICEPID:
                // 重新初始化sdk
//                ZLogger.d("initializing getui sdk...");
//                PushManager.getInstance().initialize(CashierApp.getAppContext());
                break;
            default:
                break;
        }
    }

    /**
     * 处理透传（payload）数据
     * */
    private static void parsePushPayload(Context context, String data){
        Integer bizType = PayloadHelper.getJsonMsgType(data);//获取推送的数据类型
        ZLogger.df(String.format("PushDemoReceive--payload=(%s)%s", bizType, data));
    }
}

package com.jabra.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jabra.bean.Contacts;
import com.jabra.bean.Message;
import com.jabra.data.ContactsSortList;
import com.jabra.data.MsgSortList;
import com.jabra.listener.MsgListener;
import com.jabra.utils.EmptyUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.tencent.mm.sdk.ext.MMOpenApiCaller;

import net.sourceforge.simcpux.wxapi.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WXMsgReceiver
        extends BaseReceiver {
    private static final String EXTRA_NOTIFY_TYPE = "EXTRA_EXT_OPEN_NOTIFY_TYPE";
    private static final String EXTRA_USER_DATA = "EXTRA_EXT_OPEN_USER_DATA";
    private static final int MAX_GET_UNREAD_ONCE = 15;
    private static final String NOTIFY_TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    private static final String TAG = "WXMsgReceiver";
    private MsgListener mOnMsgListener;
    private Handler mainHandler = new Handler();
    private boolean registerThreadFlag;
    private Handler subThreadHandler;

    private ContactsSortList getContactsMsg(Context context, ArrayList<String> paramArrayList) {
        ContactsSortList localContactsSortList = new ContactsSortList();
        Iterator localIterator = paramArrayList.iterator();
        for (; ; ) {
            if (!localIterator.hasNext()) {
                return localContactsSortList;
            }
            String str1 = (String) localIterator.next();
            ZLogger.d("receive userData = " + str1);
            try {
                String[] arrayOfString = str1.split(",");
                String str2 = arrayOfString[0];
                MsgSortList localMsgSortList = getUnreadMessage(context, str2,
                        Integer.parseInt(arrayOfString[1]));
                localMsgSortList.sort();
                Contacts localContacts = new Contacts();
                localContacts.setUserId(str2);
                if (EmptyUtil.notEmpty(localMsgSortList)) {
                    localContacts.setNickName(((Message) localMsgSortList.getFirst()).getFromUserNickName());
                }
                localContacts.setMsgSortList(localMsgSortList);
                localContactsSortList.put(localContacts);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }

    }

    /**
     * 获取未读消息
     *
     * @param count 消息数量
     */
    private MsgSortList getUnreadMessage(Context context, String paramString, int count) {
        ZLogger.i("接收广播的count:" + count);
        MsgSortList localMsgSortList = new MsgSortList();
        if (count <= 0) {
            return localMsgSortList;
        }

        int i;
        MMOpenApiCaller.MMResult localMMResult = null;
        if (count > MAX_GET_UNREAD_ONCE) {
            i = MAX_GET_UNREAD_ONCE;
            localMMResult = MMOpenApiCaller.getUnReadMsg(context, Constants.APP_ID, i, paramString);
        }
        for (; ; ) {
            int j;
            try {
                List localList = (List) localMMResult.data;
                String[] arrayOfString = new String[localList.size()];
                j = 0;
                if (j >= localList.size()) {
                    MMOpenApiCaller.msgSetReaded(context, Constants.APP_ID, arrayOfString);
                } else {
                    Message localMessage = new Message((MMOpenApiCaller.MsgItem) localList.get(j));
                    String str1 = localMessage.getContent();
                    String str2 = "";//paramContext.getString(2131230777);
                    Object[] arrayOfObject = new Object[1];
                    arrayOfObject[0] = localMessage.getFromUserNickName();
                    //paramContext.getString(2131230778)
                    if ((str1.contains(String.format(str2, arrayOfObject))) || (localMessage.getContent().contains("paramContext.getString(2131230778)"))) {
                        arrayOfString[j] = localMessage.getMsgId();
                    } else {
                        localMsgSortList.add(localMessage);
                        arrayOfString[j] = localMessage.getMsgId();
                        ZLogger.d("MsgItem:");
                        ZLogger.d("msgType:" + localMessage.getContentType());
                        ZLogger.d("msgId: " + localMessage.getMsgId());
                        ZLogger.d("fromUserId: " + localMessage.getFromUserId());
                        ZLogger.d("fromUserNickName: " + localMessage.getFromUserNickName());
                        ZLogger.d("content: " + localMessage.getContent());
                        ZLogger.d("status: " + localMessage.getStatus());
                        ZLogger.d("createTime: " + localMessage.getCreateTime());
                        ZLogger.d("/Jabra_Social/api_log", "receive msgId = " + localMessage.getMsgId());
                    }
                }
            } catch (Exception localException) {
                localException.printStackTrace();
                count -= MAX_GET_UNREAD_ONCE;
            }
            break;
//      i = paramInt;
//      break label47;
//      j++;
        }
        return localMsgSortList;
    }

    public void onReceive(final Context context, Intent intent) {
        ZLogger.d(intent.getAction());
        String notifyType = intent.getStringExtra(EXTRA_NOTIFY_TYPE);
        ZLogger.d("notifyType:" + notifyType);

        if (NOTIFY_TYPE_NEW_MESSAGE.equals(notifyType)) {
            final ArrayList localArrayList = intent.getStringArrayListExtra(EXTRA_USER_DATA);
            if (EmptyUtil.isEmpty(localArrayList) || (((String) localArrayList.get(0)).startsWith("o-"))) {
                ZLogger.w("Invalid userData: " + localArrayList);
                return;
            }

            this.subThreadHandler.post(new Runnable() {
                public void run() {
                    final ContactsSortList localContactsSortList = WXMsgReceiver.this.getContactsMsg(context, localArrayList);
                    if (EmptyUtil.isEmpty(localContactsSortList)) {
                        ZLogger.w("find no ContactsSortList");
                        return;
                    }

                    WXMsgReceiver.this.mainHandler.post(new Runnable() {
                        public void run() {
                            if (WXMsgReceiver.this.mOnMsgListener != null) {
                                WXMsgReceiver.this.mOnMsgListener.onReceive(localContactsSortList);
                            }
                        }
                    });
                }
            });
        }

    }

    public void register(Context context, MsgListener msgListener) {
        this.mOnMsgListener = msgListener;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.tencent.mm.plugin.openapi.Intent.ACTION_NOTIFY_MSG");
        intentFilter.addCategory("com.tencent.mm.category.com.xpg.jabra.proto");
        context.registerReceiver(this, intentFilter);

        registerWX(context);

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                WXMsgReceiver.this.subThreadHandler = new Handler();
                Looper.loop();
            }
        }).start();
    }

    public void registerWX(final Context context) {
        ZLogger.d("registerWX");
        this.registerThreadFlag = true;
        new Thread(new Runnable() {
            public void run() {
                for (; ; ) {
                    if (!WXMsgReceiver.this.registerThreadFlag) {
                        return;
                    }
                    int i = MMOpenApiCaller.registerMsgListener(context,
                            Constants.APP_ID, 1, 2, 6, 2).retCode;
                    if (i == 1) {
                        ZLogger.d("registerWX OK");
                        return;
                    }
                    ZLogger.d("registerWX error: " + i);
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception localException) {
                    }
                }
            }
        }).start();
    }

    public void stopRegisterThread() {
        this.registerThreadFlag = false;
    }

    public void unregister(Context paramContext) {
        try {
            paramContext.unregisterReceiver(this);
            this.registerThreadFlag = false;
            return;
        } catch (Exception localException) {
            for (; ; ) {
                localException.printStackTrace();
            }
        }
    }
}

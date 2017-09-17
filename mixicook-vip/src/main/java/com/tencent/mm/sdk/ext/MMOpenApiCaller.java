package com.tencent.mm.sdk.ext;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.LinkedList;
import java.util.List;

public class MMOpenApiCaller {
    static final String AMR_NB_HEAD = "#!AMR\n";
    public static final String KEY_BOOLEAN_isRoom = "isRoom";
    public static final String KEY_INT_GETTYPE = "getType";
    public static final String KEY_INT_audioFormat = "audioFormat";
    public static final String KEY_INT_channelConfig = "channelConfig";
    public static final String KEY_INT_contentType = "contentType";
    public static final String KEY_INT_msgType = "msgType";
    public static final String KEY_INT_playType = "playType";
    public static final String KEY_INT_playVoiceRet = "playVoiceRet";
    public static final String KEY_INT_sampleRateInHz = "sampleRateInHz";
    public static final String KEY_INT_status = "status";
    public static final String KEY_INT_voiceLen = "voiceLen";
    public static final String KEY_INT_voiceType = "voiceType";
    public static final String KEY_LONG_createTime = "createTime";
    public static final String KEY_STRING_ARR_NICKNAMES = "nickNames";
    public static final String KEY_STRING_ARR_USERIDS = "userIds";
    public static final String KEY_STRING_avatar = "avatar";
    public static final String KEY_STRING_content = "content";
    public static final String KEY_STRING_filePath = "filePath";
    public static final String KEY_STRING_fromUserId = "fromUserId";
    public static final String KEY_STRING_fromUserNickName = "fromUserNickName";
    public static final String KEY_STRING_msgId = "msgId";
    public static final String KEY_STRING_openid = "openid";
    private static final String TAG = "MicroMsg.ext.MMOpenApiCaller";
    private static final String URI_FORMAT_DECODE_VOICE = "content://com.tencent.mm.sdk.comm.provider/decodeVoice?appid=%s";
    private static final String URI_FORMAT_GET_AVATAR = "content://com.tencent.mm.sdk.comm.provider/getAvatar?appid=%s";
    private static final String URI_FORMAT_GET_UNREADMSG = "content://com.tencent.mm.sdk.comm.provider/unReadMsgs?appid=%s&source=%s&count=%d";
    private static final String URI_FORMAT_REGISTER_MSG_LSITENER = "content://com.tencent.mm.sdk.comm.provider/registerMsgListener?appid=%s&op=%d&scene=%d&msgType=%d&msgState=%d";
    private static final String URI_FORMAT_SET_READED = "content://com.tencent.mm.sdk.comm.provider/setReaded?appid=%s&source=%s";
    private static final String URI_FORMAT_TO_CHATTING = "content://com.tencent.mm.sdk.comm.provider/to_chatting?appid=%s&source=%s";
    public static final int VOICE_TYPE_PCM = 1;

    public static MMResult decodeVoice(final Context context, String paramString, final String[] paramArrayOfString) {
        MMResult localMMResult = new MMResult();
        localMMResult.retCode = 4;
        if ((context == null) || (isNullOrNil(paramString))) {
            localMMResult.retCode = 2;
            return localMMResult;
        }
        final MMCursor localMMCursor = new MMCursor();
        LinkedList localLinkedList = null;
        for (; ; ) {
            final Uri localUri;
            try {
                localUri = Uri.parse(String.format("content://com.tencent.mm.sdk.comm.provider/decodeVoice?appid=%s", new Object[]{paramString}));
                if (Build.VERSION.SDK_INT < 16) {
                    break;
                }
                ApiTask.doTask(16, new ApiTask.TaskRunnable() {
                    public void run() {
                        try {
                            ContentProviderClient localContentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(localUri);
                            if (localContentProviderClient == null) {
                                return;
                            }
                            localMMCursor.cursor = localContentProviderClient.query(localUri, null, null, paramArrayOfString, null);
                            return;
                        } catch (RemoteException localRemoteException) {
                            localRemoteException.printStackTrace();
                            ZLogger.w("exception in decodeVoice 1, " + localRemoteException.getMessage());
                        }
                    }
                });
                if (localMMCursor.cursor == null) {
                    break;
                }
                localLinkedList = new LinkedList();
                if (!localMMCursor.cursor.moveToFirst()) {
                    break;
                }
                if (localMMCursor.cursor.getColumnCount() != 0) {
                    break;
                }
                localMMCursor.cursor.close();
                localMMResult.retCode = 3;
                return localMMResult;
            } catch (Exception localException1) {
                localException1.printStackTrace();
                ZLogger.w("exception in decodeVoice 2, " + localException1.getMessage());
            }
            if ((localMMCursor == null) || (localMMCursor.cursor == null)) {
                break;
            }
            try {
                localMMCursor.cursor.close();
                return localMMResult;
            } catch (Exception localException2) {
                localException2.printStackTrace();
                return localMMResult;
            }
//            label213:
//            localMMCursor.cursor = context.getContentResolver().query(localUri, null, null, paramArrayOfString, null);
        }
//        label234:
        do {
            VoiceData localVoiceData = new VoiceData();
            localVoiceData.convertFrom(localMMCursor.cursor);
            localLinkedList.add(localVoiceData);
        } while (localMMCursor.cursor.moveToNext());
//        label276:
        localMMCursor.cursor.close();
        localMMResult.retCode = 1;
        localMMResult.data = localLinkedList;
        return localMMResult;
    }

    public static MMResult getAvatar(final Context context, String paramString, final String[] paramArrayOfString) {
        MMResult localMMResult = new MMResult();
        localMMResult.retCode = 4;
        if ((context == null) || (isNullOrNil(paramString)) || (paramArrayOfString == null) || (paramArrayOfString.length <= 0)) {
            localMMResult.retCode = 2;
            return localMMResult;
        }
        final MMCursor localMMCursor = new MMCursor();
        LinkedList localLinkedList = null;
        for (; ; ) {
            final Uri localUri;
            try {
                localUri = Uri.parse(String.format("content://com.tencent.mm.sdk.comm.provider/getAvatar?appid=%s", new Object[]{paramString}));
                if (Build.VERSION.SDK_INT < 16) {
                    break;
                }
                ApiTask.doTask(16, new ApiTask.TaskRunnable() {
                    public void run() {
                        try {
                            ContentProviderClient localContentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(localUri);
                            if (localContentProviderClient == null) {
                                return;
                            }
                            localMMCursor.cursor = localContentProviderClient.query(localUri, null, null, paramArrayOfString, null);
                            return;
                        } catch (RemoteException localRemoteException) {
                            localRemoteException.printStackTrace();
                            ZLogger.w("exception in getAvatar 1, " + localRemoteException.getMessage());
                        }
                    }
                });
                if (localMMCursor.cursor == null) {
                    break;
                }
                localLinkedList = new LinkedList();
                if (!localMMCursor.cursor.moveToFirst()) {
                    break;
                }
                if (localMMCursor.cursor.getColumnCount() != 0) {
                    break;
                }
                localMMCursor.cursor.close();
                localMMResult.retCode = 3;
                return localMMResult;
            } catch (Exception localException1) {
                localException1.printStackTrace();
                ZLogger.w("exception in getAvatar 2, " + localException1.getMessage());
            }
            if ((localMMCursor == null) || (localMMCursor.cursor == null)) {
                break;
            }
            try {
                localMMCursor.cursor.close();
                return localMMResult;
            } catch (Exception localException2) {
                localException2.printStackTrace();
                return localMMResult;
            }
//            label222:
//            localMMCursor.cursor = context.getContentResolver().query(localUri, null, null, null, null);
        }
//        label243:
        do {
            AvatarData localAvatarData = new AvatarData();
            localAvatarData.convertFrom(localMMCursor.cursor);
            localLinkedList.add(localAvatarData);
        } while (localMMCursor.cursor.moveToNext());
//        label285:
        localMMCursor.cursor.close();
        localMMResult.retCode = 1;
        localMMResult.data = localLinkedList;
        return localMMResult;
    }

    public static int getPcmVoiceLen(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        int i = paramInt1 / paramInt2;
        if (paramInt3 == 3) {
            i /= 2;
        }
        if (paramInt4 == 2) {
            i /= 2;
        }
        return i;
    }

    public static String getSupportFuncs(Context context) {
        if (context == null) {
            return null;
        }
        try {
            String str = context.getPackageManager()
                    .getApplicationInfo("com.tencent.mm", PackageManager.GET_META_DATA)
                    .metaData.getString("wechat_fun_support");
            return str;
        } catch (Exception localException) {
            ZLogger.e("exception in getSupportFuncs " + localException.getMessage());
            localException.printStackTrace();
        }
        return null;
    }

    /**
     * 获取未读消息
     * @param appid wxOpenID
     * @param count 消息数量
     * */
    public static MMResult getUnReadMsg(final Context context, String appid, int count, final String selectionArgs) {
        MMResult localMMResult = new MMResult();
        localMMResult.retCode = 4;

        if ((context == null) || (isNullOrNil(appid))) {
            localMMResult.retCode = 2;
            ZLogger.w("context == null or appid isNullOrNil");
            return localMMResult;
        }

        final MMCursor localMMCursor = new MMCursor();
        LinkedList localLinkedList = null;

        do {
            if (Build.VERSION.SDK_INT < 16) {
                ZLogger.w("Build.VERSION.SDK_INT < 16， 停止查询");
                break;
            }

            final Uri localUri;
            Object[] arrayOfObject = new Object[3];
            arrayOfObject[0] = appid;
            arrayOfObject[1] = "openapi";
            arrayOfObject[2] = Integer.valueOf(count);
            localUri = Uri.parse(String.format(URI_FORMAT_GET_UNREADMSG, arrayOfObject));

            ZLogger.d("开始查询：" + localUri.toSafeString());
            ApiTask.doTask(16, new ApiTask.TaskRunnable() {
                public void run() {
                    try {
                        ContentProviderClient localContentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(localUri);
                        if (localContentProviderClient == null) {
                            ZLogger.w("get ContentProviderClient failed");
                            return;
                        }

                        String[] arrayOfString = new String[1];
                        arrayOfString[0] = selectionArgs;
                        localMMCursor.cursor = localContentProviderClient.query(localUri, null, null, arrayOfString, null);
                        if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            localContentProviderClient.close();
                        } else {
                            localContentProviderClient.release();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        ZLogger.e("ContentProviderClient exception: " + e.getMessage());
                    }
                }
            });
        } while (localMMCursor.cursor == null);

        localLinkedList = new LinkedList();
        if (!localMMCursor.cursor.moveToFirst()) {
            ZLogger.w("localMMCursor.cursor is empty, 停止查询");
            localMMResult.retCode = 3;
        }
        else if (localMMCursor.cursor.getColumnCount() != 0) {
            ZLogger.w("localMMCursor.cursor column is empty, 停止查询");
            localMMResult.retCode = 3;
        } else {
            do {
                MsgItem localMsgItem = new MsgItem();
                localMsgItem.convertFrom(localMMCursor.cursor);
                localLinkedList.add(localMsgItem);
            } while (localMMCursor.cursor.moveToNext());

            localMMResult.retCode = 1;
        }
        localMMResult.data = localLinkedList;

        try {
            localMMCursor.cursor.close();
        } catch (Exception localException2) {
            localException2.printStackTrace();
        }

        return localMMResult;
    }

    /* Error */
    public static boolean isAmr(String paramString) {
        // Byte code:
        //   0: aconst_null
        //   1: astore_1
        //   2: new 299	java/io/RandomAccessFile
        //   5: dup
        //   6: aload_0
        //   7: ldc_w 301
        //   10: invokespecial 304	java/io/RandomAccessFile:<init>	(Ljava/lang/String;Ljava/lang/String;)V
        //   13: astore_2
        //   14: ldc 8
        //   16: invokevirtual 307	java/lang/String:length	()I
        //   19: newarray <illegal type>
        //   21: astore 8
        //   23: aload_2
        //   24: aload 8
        //   26: iconst_0
        //   27: ldc 8
        //   29: invokevirtual 307	java/lang/String:length	()I
        //   32: invokevirtual 311	java/io/RandomAccessFile:read	([BII)I
        //   35: istore 9
        //   37: iload 9
        //   39: iconst_m1
        //   40: if_icmpne +23 -> 63
        //   43: aload_2
        //   44: ifnull +7 -> 51
        //   47: aload_2
        //   48: invokevirtual 312	java/io/RandomAccessFile:close	()V
        //   51: iconst_0
        //   52: ireturn
        //   53: astore 13
        //   55: aload 13
        //   57: invokevirtual 313	java/io/IOException:printStackTrace	()V
        //   60: goto -9 -> 51
        //   63: new 123	java/lang/String
        //   66: dup
        //   67: aload 8
        //   69: invokespecial 316	java/lang/String:<init>	([B)V
        //   72: ldc 8
        //   74: invokevirtual 319	java/lang/String:endsWith	(Ljava/lang/String;)Z
        //   77: istore 10
        //   79: iload 10
        //   81: ifeq +96 -> 177
        //   84: aload_2
        //   85: ifnull +7 -> 92
        //   88: aload_2
        //   89: invokevirtual 312	java/io/RandomAccessFile:close	()V
        //   92: iconst_1
        //   93: ireturn
        //   94: astore 12
        //   96: aload 12
        //   98: invokevirtual 313	java/io/IOException:printStackTrace	()V
        //   101: goto -9 -> 92
        //   104: astore_3
        //   105: aload_3
        //   106: invokevirtual 172	java/lang/Exception:printStackTrace	()V
        //   109: ldc 77
        //   111: new 174	java/lang/StringBuilder
        //   114: dup
        //   115: ldc_w 321
        //   118: invokespecial 179	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   121: aload_3
        //   122: invokevirtual 183	java/lang/Exception:getMessage	()Ljava/lang/String;
        //   125: invokevirtual 187	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   128: invokevirtual 190	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   131: invokestatic 196	android/util/Log:w	(Ljava/lang/String;Ljava/lang/String;)I
        //   134: pop
        //   135: aload_1
        //   136: ifnull -85 -> 51
        //   139: aload_1
        //   140: invokevirtual 312	java/io/RandomAccessFile:close	()V
        //   143: iconst_0
        //   144: ireturn
        //   145: astore 7
        //   147: aload 7
        //   149: invokevirtual 313	java/io/IOException:printStackTrace	()V
        //   152: iconst_0
        //   153: ireturn
        //   154: astore 4
        //   156: aload_1
        //   157: ifnull +7 -> 164
        //   160: aload_1
        //   161: invokevirtual 312	java/io/RandomAccessFile:close	()V
        //   164: aload 4
        //   166: athrow
        //   167: astore 5
        //   169: aload 5
        //   171: invokevirtual 313	java/io/IOException:printStackTrace	()V
        //   174: goto -10 -> 164
        //   177: aload_2
        //   178: ifnull +16 -> 194
        //   181: aload_2
        //   182: invokevirtual 312	java/io/RandomAccessFile:close	()V
        //   185: iconst_0
        //   186: ireturn
        //   187: astore 11
        //   189: aload 11
        //   191: invokevirtual 313	java/io/IOException:printStackTrace	()V
        //   194: iconst_0
        //   195: ireturn
        //   196: astore 4
        //   198: aload_2
        //   199: astore_1
        //   200: goto -44 -> 156
        //   203: astore_3
        //   204: aload_2
        //   205: astore_1
        //   206: goto -101 -> 105
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	209	0	paramString	String
        //   1	205	1	localObject1	Object
        //   13	192	2	localRandomAccessFile	java.io.RandomAccessFile
        //   104	18	3	localException1	Exception
        //   203	1	3	localException2	Exception
        //   154	11	4	localObject2	Object
        //   196	1	4	localObject3	Object
        //   167	3	5	localIOException1	java.io.IOException
        //   145	3	7	localIOException2	java.io.IOException
        //   21	47	8	arrayOfByte	byte[]
        //   35	6	9	i	int
        //   77	3	10	bool	boolean
        //   187	3	11	localIOException3	java.io.IOException
        //   94	3	12	localIOException4	java.io.IOException
        //   53	3	13	localIOException5	java.io.IOException
        // Exception table:
        //   from	to	target	type
        //   47	51	53	java/io/IOException
        //   88	92	94	java/io/IOException
        //   2	14	104	java/lang/Exception
        //   139	143	145	java/io/IOException
        //   2	14	154	finally
        //   105	135	154	finally
        //   160	164	167	java/io/IOException
        //   181	185	187	java/io/IOException
        //   14	37	196	finally
        //   63	79	196	finally
        //   14	37	203	java/lang/Exception
        //   63	79	203	java/lang/Exception
        return true;
    }

    public static boolean isNullOrNil(String paramString) {
        return (paramString == null) || (paramString.length() <= 0);
    }

    public static boolean isSupportFun(Context context, String paramString) {
        String str = getSupportFuncs(context);
        if ((str == null) || (str.length() <= 0) || (paramString == null) || (paramString.length() <= 0)) {
        }
        for (; ; ) {
//            return false;
            String[] arrayOfString = str.split(",");
            int i = arrayOfString.length;
            for (int j = 0; j < i; j++) {
                if (arrayOfString[j].trim().equals(paramString)) {
                    return true;
                }
            }
        }
    }

    /**
     * 跳转到微信会话页面
     * */
    public static int jumpToChattingUI(final Context context, String appid, final String selecttion) {
        if ((context == null) || (isNullOrNil(appid)) ) {//|| (isNullOrNil(selecttion))
            ZLogger.w("invalid params");
            return 2;
        }
        final MMCursor localMMCursor = new MMCursor();
        try {
            final Uri localUri = Uri.parse(String.format("content://com.tencent.mm.sdk.comm.provider/to_chatting?appid=%s&source=%s", new Object[]{appid, "openapi"}));
            ZLogger.d(localUri.toSafeString());

            if (Build.VERSION.SDK_INT >= 16) {
                ApiTask.doTask(16, new ApiTask.TaskRunnable() {
                    public void run() {
                        try {
                            ContentProviderClient localContentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(localUri);
                            if (localContentProviderClient == null) {
                                ZLogger.w("ContentProviderClient is null");
                                return;
                            }
                            String[] arrayOfString = new String[1];
                            arrayOfString[0] = selecttion;
                            localMMCursor.cursor = localContentProviderClient.query(localUri, null, null, arrayOfString, null);
                            return;
                        } catch (RemoteException localRemoteException) {
                            localRemoteException.printStackTrace();
                            ZLogger.e("exception in jumpToChattingUI 1, " + localRemoteException.getMessage());
                        }
                    }
                });
            }
            if (localMMCursor.cursor == null) {
                localMMCursor.cursor = context.getContentResolver().query(localUri, null, null, new String[]{selecttion}, null);
            }

            try {
                if (localMMCursor.cursor.moveToFirst()) {
                    if (localMMCursor.cursor.getColumnCount() == 0) {
                        localMMCursor.cursor.close();
                        return 3;
                    }
                    if (localMMCursor.cursor.getInt(localMMCursor.cursor.getColumnIndex("retCode")) == 1) {
                        localMMCursor.cursor.close();
                        return 1;
                    }
                }
                localMMCursor.cursor.close();
            } catch (Exception localException2) {
                localException2.printStackTrace();
            }
        } catch (Exception localException1) {
            localException1.printStackTrace();
            ZLogger.w("exception in jumpToChattingUI 2, " + localException1.getMessage());
            if ((localMMCursor == null) || (localMMCursor.cursor == null)) {
            }
        }
        return 1;
    }

    public static int msgSetReaded(final Context context, String paramString, final String[] paramArrayOfString) {
        if ((context == null) || (isNullOrNil(paramString)) || (paramArrayOfString == null) || (paramArrayOfString.length <= 0)) {
            return 2;
        }
        final MMCursor localMMCursor = new MMCursor();
        try {
            final Uri localUri = Uri.parse(String.format("content://com.tencent.mm.sdk.comm.provider/setReaded?appid=%s&source=%s", new Object[]{paramString, "openapi"}));
            if (Build.VERSION.SDK_INT >= 16) {
                ApiTask.doTask(16, new ApiTask.TaskRunnable() {
                    public void run() {
                        try {
                            ContentProviderClient localContentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(localUri);
                            if (localContentProviderClient == null) {
                                return;
                            }
                            localMMCursor.cursor = localContentProviderClient.query(localUri, null, null, paramArrayOfString, null);
                            return;
                        } catch (RemoteException localRemoteException) {
                            localRemoteException.printStackTrace();
                            ZLogger.w("exception in msgSetReaded 1, " + localRemoteException.getMessage());
                        }
                    }
                });
            }
            while (localMMCursor.cursor != null) {
                localMMCursor.cursor.close();
//                break;
                localMMCursor.cursor = context.getContentResolver().query(localUri, null, null, paramArrayOfString, null);
            }
            return 1;
        } catch (Exception localException1) {
            localException1.printStackTrace();
            ZLogger.w("exception in msgSetReaded 2, " + localException1.getMessage());
            if ((localMMCursor != null) && (localMMCursor.cursor != null)) {
            }
            try {
                localMMCursor.cursor.close();
                return 4;
            } catch (Exception localException2) {
                for (; ; ) {
                    localException2.printStackTrace();
                }
            }
        }
    }

    public static MMResult registerMsgListener(final Context context, String appid, int op, int scene, int msgType, int msgState) {
        ZLogger.d("registerMsgListener");
        MMResult localMMResult = new MMResult();
        localMMResult.retCode = 4;
        if ((context == null) || (isNullOrNil(appid))) {
            localMMResult.retCode = 2;
            ZLogger.w("context == null or appid isNullOrNil");
            return localMMResult;
        }

        final MMCursor localMMCursor = new MMCursor();

        do {
            if (Build.VERSION.SDK_INT < 16) {
                ZLogger.w("Build.VERSION.SDK_INT < 16， 停止查询");
                break;
            }

            final Uri localUri;

            Object[] arrayOfObject = new Object[5];
            arrayOfObject[0] = appid;
            arrayOfObject[1] = Integer.valueOf(op);
            arrayOfObject[2] = Integer.valueOf(scene);
            arrayOfObject[3] = Integer.valueOf(msgType);
            arrayOfObject[4] = Integer.valueOf(msgState);
            localUri = Uri.parse(String.format(URI_FORMAT_REGISTER_MSG_LSITENER, arrayOfObject));
            ZLogger.d("开始查询：" + localUri.toSafeString());

            ApiTask.doTask(16, new ApiTask.TaskRunnable() {
                public void run() {
                    try {
                        ContentProviderClient localContentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(localUri);
                        if (localContentProviderClient == null) {
                            ZLogger.w("get ContentProviderClient failed");
                            return;
                        }
                        localMMCursor.cursor = localContentProviderClient.query(localUri, null, null, null, null);
                    } catch (RemoteException localRemoteException) {
                        localRemoteException.printStackTrace();
                        ZLogger.w("exception in registerMsgListener 1, " + localRemoteException.getMessage());
                    }
                }
            });
        } while (localMMCursor.cursor == null);

        if (!localMMCursor.cursor.moveToFirst()) {
            ZLogger.w("localMMCursor.cursor is empty, 停止查询");
            localMMResult.retCode = 3;
        }
        else if (localMMCursor.cursor.getColumnCount() != 0) {
            ZLogger.w("localMMCursor.cursor column is empty, 停止查询");
            localMMResult.retCode = 3;
        } else {
            if (localMMCursor.cursor.getInt(localMMCursor.cursor.getColumnIndex("retCode")) == 1) {
                localMMResult.retCode = 1;
                if (localMMCursor.cursor.getColumnIndex("selfId") != -1) {
                    localMMResult.data = localMMCursor.cursor.getString(localMMCursor.cursor.getColumnIndex("selfId"));
                }
            }
        }

        try {
            localMMCursor.cursor.close();
        } catch (Exception localException2) {
            localException2.printStackTrace();
        }

        return  localMMResult;
    }

    public static class AvatarData {
        public String avatar = "avatar";
        public String openid = KEY_STRING_openid;

        public void convertFrom(Cursor paramCursor) {
            if (paramCursor == null) {
            }
            String[] arrayOfString;
            do {
//                return;
                arrayOfString = paramCursor.getColumnNames();
            } while (arrayOfString == null);
            int i = 0;
            int j = arrayOfString.length;
//            label22:
            if (i < j) {
                if (!KEY_STRING_openid.equals(arrayOfString[i])) {
//                    break;
                }
                this.openid = paramCursor.getString(i);
            }
            for (; ; ) {
                i++;
//                break;
//                break;
//                label56:
                if ("avatar".equals(arrayOfString[i])) {
                    this.avatar = paramCursor.getString(i);
                }
            }
        }
    }

    private static class MMCursor {
        public Cursor cursor;
    }

    public static class MMResult {
        public Object data;
        public int retCode;
    }

    public static class MsgItem {
        public String content;
        public int contentType;
        public long createTime;
        public String fromUserId;
        public String fromUserNickName;
        public String msgId;
        public int msgType;
        public int status;

        public void convertFrom(Cursor paramCursor) {
            if (paramCursor == null) {
            }
            String[] arrayOfString;
            do {
//                return;
                arrayOfString = paramCursor.getColumnNames();
            } while (arrayOfString == null);
            int i = 0;
            int j = arrayOfString.length;
            label22:
            if (i < j) {
                if (!"msgId".equals(arrayOfString[i])) {
//                    break;
                }
                this.msgId = paramCursor.getString(i);
            }
            for (; ; ) {
                i++;
//                break;
//                break;
//                label56:
                if ("fromUserId".equals(arrayOfString[i])) {
                    this.fromUserId = paramCursor.getString(i);
                } else if ("fromUserNickName".equals(arrayOfString[i])) {
                    this.fromUserNickName = paramCursor.getString(i);
                } else if ("msgType".equals(arrayOfString[i])) {
                    this.msgType = paramCursor.getInt(i);
                } else if ("contentType".equals(arrayOfString[i])) {
                    this.contentType = paramCursor.getInt(i);
                } else if ("content".equals(arrayOfString[i])) {
                    this.content = paramCursor.getString(i);
                } else if ("status".equals(arrayOfString[i])) {
                    this.status = paramCursor.getInt(i);
                } else if ("createTime".equals(arrayOfString[i])) {
                    this.createTime = paramCursor.getLong(i);
                }
            }
        }
    }

    public static class VoiceData {
        public int audioFormat;
        public int channelConfig;
        public String filePath;
        public int sampleRateInHz;
        public int voiceType;

        public void convertFrom(Cursor paramCursor) {
            if (paramCursor == null) {
                return;
            }
            String[] arrayOfString;
            do {
//                return;
                arrayOfString = paramCursor.getColumnNames();
            } while (arrayOfString == null);
            int i = 0;
            int j = arrayOfString.length;
//            label22:
            if (i < j) {
                if (!"voiceType".equals(arrayOfString[i])) {
//                    break;
                }
                this.voiceType = paramCursor.getInt(i);
            }
            for (; ; ) {
                i++;
//                break;
//                break;
//                label56:
                if ("sampleRateInHz".equals(arrayOfString[i])) {
                    this.sampleRateInHz = paramCursor.getInt(i);
                } else if ("channelConfig".equals(arrayOfString[i])) {
                    this.channelConfig = paramCursor.getInt(i);
                } else if ("audioFormat".equals(arrayOfString[i])) {
                    this.audioFormat = paramCursor.getInt(i);
                } else if ("filePath".equals(arrayOfString[i])) {
                    this.filePath = paramCursor.getString(i);
                }
            }
        }
    }
}

package com.manfenjiayuan.mixicook_vip.wxapi.bean;

import android.database.Cursor;

public class MsgItem {
    public String content;
    public int contentType;
    public long createTime;
    public String fromUserId;
    public String fromUserNickName;
    public String msgId;
    public int msgType;
    public int status;

    public void convertFrom(Cursor paramCursor) {
        String[] arrayOfString;
        int i;
        int j;
        if (paramCursor == null) ;
        do {
            do {
//                return;
                arrayOfString = paramCursor.getColumnNames();
            }
            while (arrayOfString == null);
            i = 0;
            j = arrayOfString.length;
        }
        while (i >= j);

        if ("msgId".equals(arrayOfString[i])) {
            this.msgId = paramCursor.getString(i);
        }

        while (true) {
            ++i;
            if ("fromUserId".equals(arrayOfString[i]))
                this.fromUserId = paramCursor.getString(i);
            if ("fromUserNickName".equals(arrayOfString[i]))
                this.fromUserNickName = paramCursor.getString(i);
            if ("msgType".equals(arrayOfString[i]))
                this.msgType = paramCursor.getInt(i);
            if ("contentType".equals(arrayOfString[i]))
                this.contentType = paramCursor.getInt(i);
            if ("content".equals(arrayOfString[i]))
                this.content = paramCursor.getString(i);
            if ("status".equals(arrayOfString[i]))
                this.status = paramCursor.getInt(i);
            if (!("createTime".equals(arrayOfString[i])))
                continue;
            this.createTime = paramCursor.getLong(i);
        }
    }
}
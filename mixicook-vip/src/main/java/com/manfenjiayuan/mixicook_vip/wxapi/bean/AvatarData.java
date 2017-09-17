package com.manfenjiayuan.mixicook_vip.wxapi.bean;

import android.database.Cursor;

public class AvatarData {
    public String avatar = "avatar";
    public String openid = "openid";

    public void convertFrom(Cursor paramCursor) {
        String[] arrayOfString;
        int i;
        int j;
        if (paramCursor == null) ;
        do {
            do {
//          return;
                arrayOfString = paramCursor.getColumnNames();
            }
            while (arrayOfString == null);
            i = 0;
            j = arrayOfString.length;
        }
        while (i >= j);
        if ("openid".equals(arrayOfString[i]))
            this.openid = paramCursor.getString(i);
        while (true) {
//        while (true)
            ++i;
            if (!("avatar".equals(arrayOfString[i])))
                continue;
            this.avatar = paramCursor.getString(i);
        }
    }
}
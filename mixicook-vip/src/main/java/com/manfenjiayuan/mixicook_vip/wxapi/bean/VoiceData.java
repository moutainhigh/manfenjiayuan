package com.manfenjiayuan.mixicook_vip.wxapi.bean;

import android.database.Cursor;

public class VoiceData {
    public int audioFormat;
    public int channelConfig;
    public String filePath;
    public int sampleRateInHz;
    public int voiceType;

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
        if ("voiceType".equals(arrayOfString[i]))
            this.voiceType = paramCursor.getInt(i);
        while (true) {
//            while (true)
                ++i;
            if ("sampleRateInHz".equals(arrayOfString[i]))
                this.sampleRateInHz = paramCursor.getInt(i);
            if ("channelConfig".equals(arrayOfString[i]))
                this.channelConfig = paramCursor.getInt(i);
            if ("audioFormat".equals(arrayOfString[i]))
                this.audioFormat = paramCursor.getInt(i);
            if (!("filePath".equals(arrayOfString[i])))
                continue;
            this.filePath = paramCursor.getString(i);
        }
    }
}
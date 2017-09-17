package com.manfenjiayuan.mixicook_vip.service;

import android.content.Context;

/**
 * Created by bingshanguxue on 15/08/2017.
 */

public class ServiceConfig {
    private Context mContext;

    private ServiceConfig(Context context) {
        mContext = context;
    }

    private static ServiceConfig current;

    public static synchronized ServiceConfig getConfig(Context context) {
        if(current == null) {
            current = new ServiceConfig(context.getApplicationContext());
        }
        return current;
    }
}

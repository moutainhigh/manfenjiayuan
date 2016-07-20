package com.mfh.buyers;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.mfh.buyers.utils.Constants;
import com.mfh.buyers.utils.SensoroHelper;
import com.mfh.buyers.wxapi.WXConstants;
import com.mfh.framework.MfhApplication;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2015/7/10.
 */
public class AppContext extends MfhApplication {

    private static AppContext instance;

    @Override
    protected boolean isReleaseVersion() {
        return true;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        instance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 获得当前app运行的AppContext
     *
     * @return
     */
    public static AppContext getInstance() {
        return instance;
    }

}

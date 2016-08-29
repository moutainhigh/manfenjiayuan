package com.manfenjiayuan.loveshopping;

import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.crash.AppException;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;

import java.io.File;

/**
 * Created by bingshanguxue on 2015/7/10.
 */
public class AppContext extends MfhApplication {

    @Override
    protected boolean isReleaseVersion() {
        return SharedPreferencesManager.isReleaseVersion();
    }

    @Override
    public void onCreate() {

        AppException.CRASH_FOLDER_PATH = getPackageName() + File.separator + "crash";

        super.onCreate();

        ZLogger.CRASH_FOLDER_PATH = getPackageName() + File.separator + "zlogger";

        int pid = android.os.Process.myPid();
        String processAppName = getProcessName(this, pid);
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName != null && processAppName.equalsIgnoreCase(getPackageName())) {
            //初始化IM模块
            IMClient.getInstance().init(getApplicationContext());
        }

        ZLogger.d(String.format("Application--initialize finished(%s)", processAppName));

    }



}

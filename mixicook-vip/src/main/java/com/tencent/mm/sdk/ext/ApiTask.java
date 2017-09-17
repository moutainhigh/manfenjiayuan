package com.tencent.mm.sdk.ext;

import android.os.Build;
import android.util.Log;

import com.mfh.framework.anlaysis.logger.ZLogger;

public class ApiTask {
    private static final String TAG = "MicroMsg.com.tencent.mm.sdk.ext.ApiTask";

    public static boolean doTask(int sdkInt, TaskRunnable paramTaskRunnable) {
        if (paramTaskRunnable == null) {
            ZLogger.d(TAG, "empty task");
        }

        if (Build.VERSION.SDK_INT < sdkInt) {
            return false;
        }
        paramTaskRunnable.run();
        return true;
    }

    public interface TaskRunnable {
        void run();
    }
}

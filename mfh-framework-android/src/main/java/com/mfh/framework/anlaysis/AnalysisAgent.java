package com.mfh.framework.anlaysis;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * Created by bingshanguxue on 8/25/16.
 */
public class AnalysisAgent {
    private static AnalysisAgent instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return CloudSyncManager
     */
    public static AnalysisAgent get() {
        if (instance == null) {
            synchronized (AnalysisAgent.class) {
                if (instance == null) {
                    instance = new AnalysisAgent();
                }
            }
        }
        return instance;
    }

    public static AppInfo getAppInfo(Context context){
        AppInfo appInfo = new AppInfo();

        if (context != null) {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            if (pm == null || packageName == null){
                return appInfo;
            }

            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                if (packageInfo != null){
                    appInfo.setPackageName(packageInfo.packageName);
                    appInfo.setVersionName(packageInfo.versionName);
                    appInfo.setVersionCode(packageInfo.versionCode);
                }

                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        appInfo.setChannelName(applicationInfo.metaData.getString("UMENG_CHANNEL"));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                ZLogger.e(e.toString());
            }
        }

        return appInfo;
    }
}

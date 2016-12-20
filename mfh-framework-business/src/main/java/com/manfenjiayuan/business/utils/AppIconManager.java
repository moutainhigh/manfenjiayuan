package com.manfenjiayuan.business.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.List;

/**
 * Created by bingshanguxue on 13/11/2016.
 */

public class AppIconManager {
    public static final String ACTIVITY_ALIAS_MIXICOOK = "com.manfenjiayuan.pda_supermarket.ui.SplashActivity-mixicook";
    public static final String ACTIVITY_ALIAS_LANLJ = "com.manfenjiayuan.pda_supermarket.ui.SplashActivity-lanlj";
    public static final String ACTIVITY_ALIAS_QIANWJ = "com.manfenjiayuan.pda_supermarket.ui.SplashActivity-qianwj";

    public static final String ACTIVITY_ALIAS_CASHIER_MIXICOOK = "com.mfh.litecashier.ui.activity.SplashActivity-mixicook";
    public static final String ACTIVITY_ALIAS_CASHIER_LANLJ = "com.mfh.litecashier.ui.activity.SplashActivity-lanlj";
    public static final String ACTIVITY_ALIAS_CASHIER_QIANWJ = "com.mfh.litecashier.ui.activity.SplashActivity-qianwj";


    public static void changeIcon(Activity ctx, String activityAlias){
        if (ctx == null){
            return;
        }


        try{
            PackageManager pm = ctx.getPackageManager();
            ActivityManager am = (ActivityManager) ctx.getSystemService(Activity.ACTIVITY_SERVICE);

            // enable/disable activity-aliases component
            String packageName = ctx.getPackageName();
            if ("com.manfenjiayuan.pda_supermarket".equals(packageName)){
                pm.setComponentEnabledSetting(
                        new ComponentName(ctx, ACTIVITY_ALIAS_MIXICOOK),
                        ACTIVITY_ALIAS_MIXICOOK.equals(activityAlias) ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

                pm.setComponentEnabledSetting(
                        new ComponentName(ctx, ACTIVITY_ALIAS_LANLJ),
                        ACTIVITY_ALIAS_LANLJ.equals(activityAlias) ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

                pm.setComponentEnabledSetting(
                        new ComponentName(ctx, ACTIVITY_ALIAS_QIANWJ),
                        ACTIVITY_ALIAS_QIANWJ.equals(activityAlias) ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
            else if ("com.mfh.litecashier".equals(packageName)){
                pm.setComponentEnabledSetting(
                        new ComponentName(ctx, ACTIVITY_ALIAS_CASHIER_MIXICOOK),
                        ACTIVITY_ALIAS_CASHIER_MIXICOOK.equals(activityAlias) ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

                pm.setComponentEnabledSetting(
                        new ComponentName(ctx, ACTIVITY_ALIAS_CASHIER_LANLJ),
                        ACTIVITY_ALIAS_CASHIER_LANLJ.equals(activityAlias) ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

                pm.setComponentEnabledSetting(
                        new ComponentName(ctx, ACTIVITY_ALIAS_CASHIER_QIANWJ),
                        ACTIVITY_ALIAS_CASHIER_QIANWJ.equals(activityAlias) ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }

            // Find launcher and kill it
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            List<ResolveInfo> resolves = pm.queryIntentActivities(i,0);
            for(ResolveInfo res : resolves){
                if(res.activityInfo != null){
                    am.killBackgroundProcesses(res.activityInfo.packageName);
                }
            }
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }

        // Change ActionBar icon
//        ctx.getActionBar().setIcon(iconId);
    }

}

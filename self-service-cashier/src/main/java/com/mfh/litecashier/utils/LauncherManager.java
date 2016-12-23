package com.mfh.litecashier.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 13/11/2016.
 */

public class LauncherManager {
    public static final String ALIAS_MIXICOOK = "com.mfh.litecashier.ui.activity.SplashActivity-mixicook";
    public static final String ALIAS_LANLJ = "com.mfh.litecashier.ui.activity.SplashActivity-lanlj";
    public static final String ALIAS_QIANWJ = "com.mfh.litecashier.ui.activity.SplashActivity-qianwj";


    /**
     * 设置应用入口图标
     * */
    public static void changeLauncherIcon(Activity ctx, String filterClassName){
        if (ctx == null){
            return;
        }

        //查询需要开启的Component,如果找到即开启并关闭其他的
        try{
            ComponentName defaultComponentName = ctx.getComponentName();
            ZLogger.d(String.format("defaultComponentName=%s", defaultComponentName.toString()));

            List<ComponentName> aliasComponentNames = new ArrayList<>();
            aliasComponentNames.add(new ComponentName(ctx, ALIAS_MIXICOOK));
            aliasComponentNames.add(new ComponentName(ctx, ALIAS_LANLJ));
            aliasComponentNames.add(new ComponentName(ctx, ALIAS_QIANWJ));

            PackageManager pm = ctx.getPackageManager();

            boolean isDefault = true;
            if (!StringUtils.isEmpty(filterClassName)){
                // enable/disable activity-aliases component
                for (ComponentName componentName1 : aliasComponentNames){
                    String className = componentName1.getClassName();
                    ZLogger.d(String.format("className=%s", className));
                    if (filterClassName.equals(className)){
                        pm.setComponentEnabledSetting(componentName1,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);
                        isDefault = false;
                    }
                    else{
                        pm.setComponentEnabledSetting(componentName1,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                }
            }

            if (isDefault){
                pm.setComponentEnabledSetting(defaultComponentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            }
            else{
                pm.setComponentEnabledSetting(defaultComponentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }

            ActivityManager am = (ActivityManager) ctx.getSystemService(Activity.ACTIVITY_SERVICE);

            // Find launcher and kill it
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            List<ResolveInfo> resolves = pm.queryIntentActivities(i,0);
            for(ResolveInfo res : resolves){
                ZLogger.d(String.format("ResolveInfo=%s", res.toString()));
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


    private void enableComponent(PackageManager pm, ComponentName componentName) {
        if (pm == null){
            return;
        }
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableComponent(PackageManager pm, ComponentName componentName) {
        if (pm == null){
            return;
        }
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}

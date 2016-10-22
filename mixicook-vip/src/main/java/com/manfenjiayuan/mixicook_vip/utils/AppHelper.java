package com.manfenjiayuan.mixicook_vip.utils;

import android.app.Activity;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by bingshanguxue on 12/10/2016.
 */

public class AppHelper {
    private LinkedList<Activity> activityLinkedList = new LinkedList<>();


    private static AppHelper instance = null;

    /**
     * 返回 DataSyncManagerImpl 实例
     *
     * @return
     */
    public static AppHelper getInstance() {
        if (instance == null) {
            synchronized (AppHelper.class) {
                if (instance == null) {
                    instance = new AppHelper();
                }
            }
        }
        return instance;
    }

    //向list中添加Activity
    public void addActivity(Activity activity){
        activityLinkedList.add(activity);
    }

    public void finshActivity(Class<? extends Activity> activityClass){
        for (Activity activity : activityLinkedList) {
            if( activityClass.equals( activity.getClass() ) ){
                activity.finish();
            }
        }
    }

    //结束特定的Activity(s)
    public void finshActivities(Class<? extends Activity>... activityClasses){
        for (Activity activity : activityLinkedList) {
            if( Arrays.asList(activityClasses).contains( activity.getClass() ) ){
                activity.finish();
            }
        }
    }

    //结束所有的Activities
    public void finshAllActivities() {
        for (Activity activity : activityLinkedList) {
            activity.finish();
        }
    }
}

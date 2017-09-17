package com.tencent.mm.sdk.ext;

import android.os.Build.VERSION;
import android.util.Log;

public class ApiTask
{
  private static final String TAG = "MicroMsg.ext.ApiTask";
  
  public static boolean doTask(int paramInt, TaskRunnable paramTaskRunnable)
  {
    if (paramTaskRunnable == null) {
      Log.d("MicroMsg.ext.ApiTask", "empty task");
    }
    while (Build.VERSION.SDK_INT < paramInt) {
      return false;
    }
    paramTaskRunnable.run();
    return true;
  }
  
  public static abstract interface TaskRunnable
  {
    public abstract void run();
  }
}

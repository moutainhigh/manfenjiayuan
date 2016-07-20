package com.mfh.framework.core.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


import com.mfh.framework.core.logger.ZLogger;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2015/5/15.
 */
public class DeviceUtils {
//    public static boolean isTablet() {
//        if (_isTablet == null) {
//            boolean flag;
//            if ((0xf & BaseApplication.context().getResources()
//                    .getConfiguration().screenLayout) >= 3)
//                flag = true;
//            else
//                flag = false;
//            _isTablet = Boolean.valueOf(flag);
//        }
//        return _isTablet.booleanValue();
//    }

    /**
     * 隐藏软键盘
     * @param context
     *          context
     * @param view
     *          the currently focused view
     * */
    public static void hideSoftInput(Context context, View view) {
        if (context == null || view == null){
            return ;
        }

        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideSoftInput(Activity context) {
        if (context == null){
            return ;
        }

        View currentFocusView = context.getCurrentFocus();
        if (currentFocusView == null)
            return ;

        IBinder windowToken = currentFocusView.getWindowToken();
        if (windowToken == null)
            return ;

        if(context.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN){
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null){
                imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }


    /**
     * 显示软键盘
     * @param context
     *          context
     * @param view
     *          the currently focused view, which would like to receive soft keyboard input.
     * */
    public static void showSoftInput(Context context, View view) {
        if (context == null || view == null){
            return ;
        }

        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.showSoftInput(view, 0);
        }
    }

    public static void toggleSoftInput(final Context context) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, 400);
    }

    public static void hideSoftInputEver(Activity activity) {
        View currentFocusView = activity.getCurrentFocus();

        hideSoftInputEver(activity, currentFocusView);
    }

    public static void hideSoftInputEver(Activity activity, View view) {
        if (view == null)
            return ;

        IBinder windowToken = view.getWindowToken();
        if (windowToken == null)
            return ;

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }


    /**
     * 判断是否存在指定包名的应用
     * */
    public static boolean isPackageExist(Context context, String pckName) {
        try {
            PackageInfo pckInfo = context.getPackageManager()
                    .getPackageInfo(pckName, 0);
            if (pckInfo != null)
                return true;
        } catch (PackageManager.NameNotFoundException e) {
            ZLogger.e(e.getMessage());
        }
        return false;
    }

    /**
     * 安装APP
     * */
    public static void installAPK(Context context, File file) {
        if (file == null || !file.exists()){
            ZLogger.e("安装包不存在");
            return;
        }

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 卸载App
     * */
    public static void uninstallApk(Context context, String packageName) {
        if (isPackageExist(context, packageName)) {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
                    packageURI);
            context.startActivity(uninstallIntent);
        }
    }

    public static int getScreenWidth(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        //return display.getWidth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }
        return display.getWidth();
    }

    public static int getScreenHeight(Activity context) {

        Display display = context.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size.y;
        }
        return display.getHeight();
    }
}

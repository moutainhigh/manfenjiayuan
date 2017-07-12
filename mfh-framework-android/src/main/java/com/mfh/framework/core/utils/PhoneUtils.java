package com.mfh.framework.core.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.mfh.framework.R;
import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * Created by bingshanguxue on 8/25/16.
 */
public class PhoneUtils {

    /**
     * 获取手机设备的串号
     * <ol>
     * 它会根据不同的手机设备返回IMEI，MEID或者ESN码，但在使用的过程中有以下问题：
     * <li>非手机设备：最开始搭载Android系统都手机设备，而现在也出现了非手机设备：如平板电脑、电子书、
     * 电视、音乐播放器等。这些设备没有通话的硬件功能，系统中也就没有TELEPHONY_SERVICE，
     * 自然也就无法通过上面的方法获得DEVICE_ID。</li>
     * <li>权限问题：获取DEVICE_ID需要READ_PHONE_STATE权限，如果只是为了获取DEVICE_ID而没有用到
     * 其他的通话功能，申请这个权限一来大才小用，二来部分用户会怀疑软件的安全性。</li>
     * <li>厂商定制系统中的Bug：少数手机设备上，由于该实现有漏洞，会返回垃圾，
     * 如:zeros或者asterisks</li>
     * </ol>
     */
    public static String getImei(Context context) {
        if (context == null){
            return null;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return telephonyManager.getDeviceId();
            } else {
                return null;
            }
        }
        else{
            ZLogger.wf(context.getString(R.string.permission_not_granted,
                    Manifest.permission.READ_PHONE_STATE));
            return null;
        }
    }

    public static String getImsi(Context context) {
        if (context == null){
            return null;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return telephonyManager.getSubscriberId();
            } else {
                return null;
            }
        }
        else{
            ZLogger.wf(context.getString(R.string.permission_not_granted,
                    Manifest.permission.READ_PHONE_STATE));
            return null;
        }
    }

    /**
     * 装有SIM卡的设备，可以通过下面的方法获取到Sim Serial Number
     * <ol>
     * <p/>
     * <li>注意：对于CDMA设备，返回的是一个空值！</li>
     * <li>android.permission.READ_PHONE_STATE)</li>
     * <li>java.lang.SecurityException: getIccSerialNumber: Neither user 10097 nor current process has android.permission.READ_PHONE_STATE.
     * </li>
     * </ol>
     */
    public static String getSimSerialNumber(Context context) {
        if (context == null){
            return null;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return telephonyManager.getSimSerialNumber();
            } else {
                return null;
            }
        } else {
            ZLogger.wf(context.getString(R.string.permission_not_granted,
                    Manifest.permission.READ_PHONE_STATE));
            return null;
        }
    }
}

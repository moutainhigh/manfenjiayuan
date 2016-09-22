package com.mfh.framework.core.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * 网络工具类
 * Created by bingshanguxue on 2014/11/6.
 */
public class NetworkUtils {
    public static final String DEFAULT_WIFI_MACADDRESS = "00-00-00-00-00-00";
    public static final String G2G3 = "2G/3G";
    public static final String WIFI = "Wi-Fi";

    /**
     * Check whether the device has connected network or not.<br>
     * need permissoin{@link android.Manifest.permission#ACCESS_NETWORK_STATE}
     */
    public static boolean isConnect(Context context) {
        if (context == null){
            return false;
        }

        PackageManager packageManager = context.getPackageManager();
        //java.lang.SecurityException: ConnectivityService: Neither user 10103 nor current process has android.permission.ACCESS_NETWORK_STATE.
        if (packageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,
                context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            ZLogger.wf("Neither user 10103 nor current process has android.permission.ACCESS_NETWORK_STATE");
        }

        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conManager != null) {
            try {
                NetworkInfo info = conManager.getActiveNetworkInfo();
                if (info != null) {
                    return info.isAvailable();
//                return e.isConnected();
                }
            } catch (Exception e) {
                ZLogger.ef("检查网络是否连接:" + e.toString());
            }
        }

//        NetworkInfo[] info = cm.getAllNetworkInfo();
//        if (info != null) {
//            for (int i = 0; i < info.length; i++) {
//                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                    return true;
//                }
//            }
//        }

        return false;
    }

    /**
     * Check whether the network is Wifi or not
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 互联网连接的类型
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return null;
        }

        int networkType = info.getType();
        switch (networkType) {
            case ConnectivityManager.TYPE_WIFI: {
                return "wifi";
            }
            case ConnectivityManager.TYPE_MOBILE: {
                int subType = info.getSubtype();
                if (subType == TelephonyManager.NETWORK_TYPE_CDMA ||
                        subType == TelephonyManager.NETWORK_TYPE_GPRS ||
                        subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                    return "2g";
                } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS ||
                        subType == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        subType == TelephonyManager.NETWORK_TYPE_EVDO_A ||
                        subType == TelephonyManager.NETWORK_TYPE_EVDO_0 ||
                        subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                    return "3g";
                } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                    return "4g";
                }
            }
            default: {
                return "Unknown";
            }
        }
    }

    /**
     * 获取网络状态
     * Wi-Fi, GPRS, UMTS, etc.
     */
    public static String[] getNetworkState(Context context) {
        String[] arrayOfString = new String[]{"Unknown", "Unknown"};

        try {
            PackageManager packageManager = context.getPackageManager();
            //java.lang.SecurityException: ConnectivityService: Neither user 10103 nor current process has android.permission.ACCESS_NETWORK_STATE.
            if (packageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,
                    context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                arrayOfString[0] = "Unknown";
            } else {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager == null) {
                    arrayOfString[0] = "Unknown";
                } else {
                    NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(1);
                    if (wifiNetworkInfo != null && wifiNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        arrayOfString[0] = WIFI;
                    } else {
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(0);
                        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            arrayOfString[0] = G2G3;
                            arrayOfString[1] = networkInfo.getSubtypeName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

        return arrayOfString;
    }

    /**
     * 使用手机Wifi或蓝牙的MAC地址作为设备标识
     * <ol>
     * <li>android.permission.ACCESS_WIFI_STATE</li>
     * <li>格式: 54:e4:bd:ff:b4:7c</li>
     * <li>硬件限制：并不是所有的设备都有Wifi和蓝牙硬件，硬件不存在自然也就得不到这一信息。</li>
     * <li>如果Wifi没有打开过，是无法获取其Mac地址的；而蓝牙是只有在打开的时候才能获取到其Mac地址。</li>
     * </ol>
     */
    public static String getWifiMacAddress(Context context) {
        if (context != null) {
            WifiManager wifimanage = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiinfo = wifimanage.getConnectionInfo();
            if (wifiinfo != null) {
                String address = wifiinfo.getMacAddress();
                if (!StringUtils.isEmpty(address)) {
                    return address;
                }
            }
        }
        return DEFAULT_WIFI_MACADDRESS;
    }

    public static String getWifiIpAddress(Context context) {
        if (context != null) {
            WifiManager e = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = e.getConnectionInfo();
            if (wifiInfo != null) {
                return convertIntToIp(wifiInfo.getIpAddress());
            }
        }

        return null;
    }

    private static String convertIntToIp(int i) {
        return (i & 255) + "." + (i >> 8 & 255) + "." + (i >> 16 & 255) + "." + (i >> 24 & 255);
    }

}

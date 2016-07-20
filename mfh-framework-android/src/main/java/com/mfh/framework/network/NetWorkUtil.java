package com.mfh.framework.network;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.mfh.framework.core.utils.StringUtils;

/**
 * 网络工具类
 * Created by 李潇阳 on 2014/11/6.
 */
public class NetWorkUtil {
    /**
     * Check if the device has connected network.
     * */
    public static boolean isConnect(Context context) {
        if(context != null){
            ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(conManager != null) {
                NetworkInfo info = conManager.getActiveNetworkInfo();
                if(info != null) {
                    return info.isAvailable();
//                return e.isConnected();
                }
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
     * Check the network is Wifi or not
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
     * */
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

    public static String[] getNetworkState(Context context) {
        String[] arrayOfString = new String[]{"Unknown", "Unknown"};

        try {
            PackageManager e = context.getPackageManager();

            if(e.checkPermission("android.permission.ACCESS_NETWORK_STATE", context.getPackageName()) != 0) {
                arrayOfString[0] = "Unknown";
                return arrayOfString;
            }

            ConnectivityManager localConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(localConnectivityManager == null) {
                arrayOfString[0] = "Unknown";
                return arrayOfString;
            }

            NetworkInfo localNetworkInfo1 = localConnectivityManager.getNetworkInfo(1);
            if(localNetworkInfo1 != null && localNetworkInfo1.getState() == NetworkInfo.State.CONNECTED) {
                arrayOfString[0] = "Wi-Fi";
                return arrayOfString;
            }

            NetworkInfo localNetworkInfo2 = localConnectivityManager.getNetworkInfo(0);
            if(localNetworkInfo2 != null && localNetworkInfo2.getState() == NetworkInfo.State.CONNECTED) {
                arrayOfString[0] = "2G/3G";
                arrayOfString[1] = localNetworkInfo2.getSubtypeName();
                return arrayOfString;
            }
        } catch (Exception var6) {

        }

        return arrayOfString;
    }

    public static String getWifiAddress(Context context) {
        if(context != null) {
            WifiManager wifimanage = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiinfo = wifimanage.getConnectionInfo();
            if(wifiinfo != null) {
                String address = wifiinfo.getMacAddress();
                if(StringUtils.isEmpty(address)) {
                    address = "00-00-00-00-00-00";
                }

                return address;
            } else {
                return "00-00-00-00-00-00";
            }
        } else {
            return "00-00-00-00-00-00";
        }
    }

    private static String _convertIntToIp(int i) {
        return (i & 255) + "." + (i >> 8 & 255) + "." + (i >> 16 & 255) + "." + (i >> 24 & 255);
    }

    public static String getWifiIpAddress(Context context) {
        if(context != null) {
            try {
                WifiManager e = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiinfo = e.getConnectionInfo();
                if(wifiinfo != null) {
                    return _convertIntToIp(wifiinfo.getIpAddress());
                }

                return null;
            } catch (Exception var3) {

            }
        }

        return null;
    }

    public static boolean isWifi(Context context) {
        if(context != null) {
            try {
                if(getNetworkState(context)[0].equals("Wi-Fi")) {
                    return true;
                }
            } catch (Exception var2) {

            }
        }

        return false;
    }
}

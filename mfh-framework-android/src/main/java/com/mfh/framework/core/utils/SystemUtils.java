package com.mfh.framework.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.anlaysis.DeviceUuidFactory;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by bingshanguxue on 8/25/16.
 */
public class SystemUtils {
    public static String getCpuInfo() {
        IOException localIOException;
        FileReader fileReader;
        FileNotFoundException localFileNotFoundException;
        String str = null;
        try {
            FileReader localFileReader = new FileReader("/proc/cpuinfo");
            BufferedReader localBufferedReader;
            localBufferedReader = new BufferedReader(localFileReader, 1024);
            BufferedReader bufferedReader;
            try {
                str = localBufferedReader.readLine();
                localBufferedReader.close();
                localFileReader.close();
                bufferedReader = localBufferedReader;
            } catch (IOException e3) {
                localIOException = e3;
                bufferedReader = localBufferedReader;
                ZLogger.e("Could not read from file /proc/cpuinfo", localIOException.toString());
                fileReader = localFileReader;
                if (str == null) {
                    return "";
                }
                return str.substring(str.indexOf(58) + 1).trim();
            }
            fileReader = localFileReader;
        } catch (FileNotFoundException e5) {
            localFileNotFoundException = e5;
            ZLogger.e("BaseParameter-Could not open file /proc/cpuinfo",
                    localFileNotFoundException.toString());
            if (str == null) {
                return "";
            }
            return str.substring(str.indexOf(58) + 1).trim();
        }
        if (str == null) {
            return str.substring(str.indexOf(58) + 1).trim();
        }
        return "";
    }

    /**
     * 获取手机的硬件信息
     *
     * @return
     */
    public static JSONObject getMobileInfo() {
        JSONObject jsonObject = new JSONObject();
        //通过反射获取系统的硬件信息
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                //暴力反射 ,获取私有的信息
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                jsonObject.put(name, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static int getSdkVersion() {
        try {
            return VERSION.class.getField("SDK_INT").getInt(null);
        } catch (Exception e) {
            try {
                return Integer.parseInt((String) VERSION.class.getField("SDK").get(null));
            } catch (Exception e2) {
                e2.printStackTrace();
                return 2;
            }
        }
    }

    public static File getRootFolder(String folderName) {
        File primaryExternal = Environment.getExternalStorageDirectory();
        if (primaryExternal == null) {
            return null;
        }
        File rootFolder = new File(String.format("%s%s%s",
                new Object[]{primaryExternal.getAbsolutePath(),
                        File.separator, folderName}));
        if (rootFolder == null || rootFolder.exists()) {
            return rootFolder;
        }
        rootFolder.mkdirs();
        return rootFolder;
    }


    /**
     * 获取App安装包信息
     *
     * @return
     */
    public static PackageInfo getPackageInfo(Context context, int flags) {
        if (context == null) {
            return null;
        }

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            if (pm != null && packageName != null) {
                return pm.getPackageInfo(packageName, flags);
            }
        } catch (PackageManager.NameNotFoundException e) {
            ZLogger.e(e.toString());
        }

        return null;
    }

    /**
     * 获取当前程序内部版本号.若没有或失败返回-1.
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfo(context, 0);
            if (packageInfo != null){
                return packageInfo.versionCode;
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
        return -1;
    }

    /**
     * 获取程序的版本信息
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfo(context, 0);
            if (packageInfo != null){
                return packageInfo.versionName;
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
        return "Unknown";
    }

    /**
     * 获取设备序列号
     * */
    public static String getDeviceUuid(Context context){
        PackageInfo packageInfo = getPackageInfo(context, 0);
        if (packageInfo != null){
            return String.format("%s@%s",
                    packageInfo.packageName,
                    new DeviceUuidFactory(context).getDeviceUuid());
        }
        return null;
    }

    /**
     * <ol>
     * 获取APK当前签名文件的SHA1
     * <li>第一步、打开Android Studio的Terminal工具</li>
     * <li>第二步、输入命令：keytool -v -list -keystore keystore文件路径</li>
     * <li>第三步、输入Keystore密码</li>
     * </ol>
     */
    public static String getSignature(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
            if (packageInfo == null) {
                return null;
            }

            Signature[] signatures = packageInfo.signatures;
            if (signatures == null || signatures.length < 1) {
                ZLogger.d("SHA1: signatures is null");
                return null;
            }

            byte[] cert = signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (byte aPublicKey : publicKey) {
                String appendString = Integer.toHexString(0xFF & aPublicKey)
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(appendString);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            ZLogger.e("SHA1:" + e.toString());
        }
        return null;
    }


    public static String getApplicationLabel(Context context) {
        if (context == null) {
            return null;
        }

        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        if (packageManager == null || packageName == null) {
            return null;
        }

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo == null) {
                return null;
            }

            return packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            ZLogger.e("getApplicationLabel:" + e.toString());
        }
        return null;
    }
}
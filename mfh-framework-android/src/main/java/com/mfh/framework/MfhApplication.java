package com.mfh.framework;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mfh.comn.config.ConfigsParseHelper;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.AppException;
import com.mfh.framework.core.DeviceUuidFactory;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.EncryptUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.SharedPreferencesUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by bingshanguxue on 15/11/13.
 */
public class MfhApplication extends Application {

    private static Context context;

    /**
     * 是否是正式发布版本
     */
    protected boolean isReleaseVersion() {
        return true;
    }

    /**
     * 获取统一配置文件别名，以示区分
     *
     * @return
     * @author zhangyz created on 2013-5-25
     */
    protected String getConfigAlias() {
        return ConfigsParseHelper.configAlias;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        //错误收集
        Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler(this));

        BizConfig.RELEASE = isReleaseVersion();

        //初始化统一配置对象
        initConfig();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 获取应用上下文
     *
     * @return
     * @author zhangyz created on 2014-3-8
     */
    public static Context getAppContext() {
        return context;
    }

    /**
     * 初始化统一配置对象
     */
    private void initConfig() {
        try {
            AssetManager am = getAm();
            if (am != null) {
                InputStream in = getAm().open(getConfigAlias());
                //            String[] files = AppHelper.getAm().list("/");
//            for (int ii = 0; ii < files.length; ii++) {
//                System.out.println(files[ii]);
//            }

                UConfigCache.getInstance(getConfigAlias()).initialize(in);
            } else {
                ZLogger.d("initConfig failed, assets is null");
            }
        } catch (IOException e) {
//            e.printStackTrace();
            ZLogger.e("initConfig failed, " + e.toString());
        }
    }

    /**
     * 获取App唯一标识,若没有则自动生成一个
     *
     * @return
     */
    public static String getAppId() {
        String appid = SharedPreferencesUtil.get(getAppContext(),
                SharedPreferencesManager.PREF_NAME_APP, SharedPreferencesManager.PREF_KEY_APP_UNIQUE_ID, null);

        if (StringUtils.isEmpty(appid)) {
            appid = UUID.randomUUID().toString();

            SharedPreferencesUtil.set(getAppContext(),
                    SharedPreferencesManager.PREF_NAME_APP, SharedPreferencesManager.PREF_KEY_APP_UNIQUE_ID, appid);
        }
        return appid;
    }

    /**
     * 获取UserAgent,登录时填入Header
     */
    public static String getUserAgent() {
        String userAgent = SharedPreferencesUtil.get(getAppContext(),
                SharedPreferencesManager.PREF_NAME_APP, SharedPreferencesManager.PREF_KEY_APP_USERAGENT, null);

        if (StringUtils.isEmpty(userAgent)) {
            StringBuilder ua = new StringBuilder("MFH");
            ua.append('/' + getVersionName() + '_' + getVersionCode());//App版本
            ua.append("/Android");//手机系统平台
            ua.append("/" + android.os.Build.VERSION.RELEASE);//手机系统版本
            ua.append("/" + android.os.Build.MODEL); //手机型号
            ua.append("/" + getAppId());//客户端唯一标识
            userAgent = ua.toString();

            SharedPreferencesUtil.set(getAppContext(),
                    SharedPreferencesManager.PREF_NAME_APP, SharedPreferencesManager.PREF_KEY_APP_USERAGENT, userAgent);
        }
        return userAgent;
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public static PackageInfo getPackageInfo() {
        PackageInfo info = new PackageInfo();
        Context context = getAppContext();
        if (context == null) {
            return info;
        }

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            info = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }

        return info;
    }

    /**
     * 获取当前程序内部版本号.若没有或失败返回-1.
     */
    public static int getVersionCode() {
        try {
            return getPackageInfo().versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取程序的版本信息
     */
    public static String getVersionName() {
        try {
            return getPackageInfo().versionName;
        } catch (Exception e) {
            return "未知";
        }
    }

    /**
     * 获取手机的硬件信息
     *
     * @return
     */
    private String getMobileInfo() {
        StringBuilder sb = new StringBuilder();
        //通过反射获取系统的硬件信息
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                //暴力反射 ,获取私有的信息
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name + "=" + value);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
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
    public static String getWifiMacAddress() {
        WifiManager wifi = (WifiManager) getAppContext().getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();

        return info.getMacAddress();
    }

    public static String getWifiMac15Bit() {
        String macAddress = getWifiMacAddress();
        if (macAddress == null) {
            return getPackageInfo().packageName;
        }
        macAddress = macAddress.replace(":", "");
        macAddress = "000" + macAddress;  //add 000 for 15dit
        macAddress = macAddress.toUpperCase();
        return getPackageInfo().packageName + macAddress;
    }

    public static String getWifiIpAddress() {
        WifiManager wifi = (WifiManager) getAppContext().getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();

        return StringUtils.parseIpAddress(info.getIpAddress());
    }

    /**
     * 获取Mac地址
     */
    public static String getLinuxMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static String getHostAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ZLogger.e("getLocalIpAddress failed, " + ex.toString());
        }
        return null;
    }

    public static String getHostIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ZLogger.e("getLocalIpAddress failed, " + ex.toString());
        }
        return null;
    }

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
    public static String getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) getAppContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            return telephonyManager.getDeviceId();
        } else {
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
    public static String getSimSerialNumber() {
        int permissionCheck = ContextCompat.checkSelfPermission(getAppContext(),
                Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getAppContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return telephonyManager.getSimSerialNumber();
            } else {
                return null;
            }
        } else {
            ZLogger.df(getAppContext().getString(R.string.permission_not_granted,
                    Manifest.permission.READ_PHONE_STATE));
            return null;
        }

    }


    /**
     * ANDROID_ID
     * <ol>
     * <p/>
     * <li></li>
     * </ol>
     */
    public static String getAndroidId() {
        return Settings.Secure.getString(getAppContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取进程名称
     *
     * @return null may be returned if the specified process not found
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    /**
     * 获取asset资源管理器
     *
     * @return
     * @author zhangyz created on 2013-5-25
     */
    public static AssetManager getAm() {
        Context context = getAppContext();
        if (context != null) {
            return context.getAssets();
        }
        return null;
    }

    /**
     * 获取android当前可用内存大小
     */
    public static long getAvailMemory(Context context) {
        if (context == null) {
            return 0;
        }

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.getMemoryInfo(mi);
        }
        //mi.availMem; 当前系统的可用内存
        //return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
//        Log.d("Mfh", "可用内存---->>>" + mi.availMem / (1024 * 1024));
        return mi.availMem / (1024 * 1024);
    }

    /**
     * 获取android当前可用内存大小
     */
    public static long getTotalMemory(Context context) {
        if (context == null) {
            return 0;
        }

        //
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        if (am != null) {
            am.getMemoryInfo(mi);
        }
        //mi.availMem; 当前系统的可用内存
        //return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
//        Log.d("Mfh", "可用内存---->>>" + mi.availMem / (1024 * 1024));
        return mi.totalMem / (1024 * 1024);
    }

    public static void clearMemory(Context context) {
        ActivityManager activityManger = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
        if (list != null)
            for (int i = 0; i < list.size(); i++) {
                ActivityManager.RunningAppProcessInfo apinfo = list.get(i);

                Log.d("Mfh", "pid---->>>>>>>" + apinfo.pid);
                Log.d("Mfh", "processName->> " + apinfo.processName);
                Log.d("Mfh", "importance-->>" + apinfo.importance);
                String[] pkgList = apinfo.pkgList;

                if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                    // Process.killProcess(apinfo.pid);
                    for (String aPkgList : pkgList) {
                        //2.2以上是过时的,请用killBackgroundProcesses代替
                        /**清理不可用的内容空间**/
                        //activityManger.restartPackage(pkgList[j]);
                        activityManger.killBackgroundProcesses(aPkgList);
                    }
                }
            }
    }

    /**
     * <ol>
     * 获取APK当前签名文件的SHA1
     * <li>第一步、打开Android Studio的Terminal工具</li>
     * <li>第二步、输入命令：keytool -v -list -keystore keystore文件路径</li>
     * <li>第三步、输入Keystore密码</li>
     * </ol>
     */
    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (info == null) {
                ZLogger.d("SHA1: packageInfo is null");
                return null;
            }

            Signature[] signatures = info.signatures;
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
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            ZLogger.e("SHA1:" + e.toString());
        }
        return null;
    }

    /**
     * */
    public static void debugPrint() {
        ZLogger.d(String.format("process name:\t%s",
                getProcessName(getAppContext(), android.os.Process.myPid())));
        ZLogger.d(String.format("apk install path:\t%s", getAppContext().getPackageCodePath()));

        // 从AndroidManifest.xml的meta-data中读取SDK配置信息
        String packageName = getAppContext().getPackageName();
        try {
            ApplicationInfo appInfo = getAppContext().getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            StringBuilder sb = new StringBuilder();
            sb.append("metaData:{\n");
            if (appInfo.metaData != null) {
                sb.append(String.format("\tUMENG_APP_KEY\t=%s\n",
                        appInfo.metaData.getString("UMENG_APPKEY")));
                sb.append(String.format("\tGETUI_APP_ID\t=%s\n",
                        appInfo.metaData.getString("PUSH_APPID")));
                sb.append(String.format("\tGETUI_APP_SECRET=%s\n",
                        appInfo.metaData.getString("PUSH_APPSECRET")));
                sb.append(String.format("\tGETUI_APP_KEY\t=%s\n",
                        appInfo.metaData.getString("PUSH_APPKEY")));
            }
            sb.append("}\n");
            ZLogger.d(sb.toString());
        } catch (PackageManager.NameNotFoundException e) {
            ZLogger.e(e.toString());
//            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(getAppContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ZLogger.d(String.format("FileUtil.SDCARD=%s", FileUtil.getSDCardPath()));///mnt/sdcard
            ZLogger.d(String.format("FileUtil.SDCARD_ABS=%s", FileUtil.getSDCardAbsPath()));
//		ZLogger.d(String.format("FileUtil.DATA=%s", FileUtil.DATA));///data/data/com.mfh.cashier/files
            ZLogger.d(String.format("FileUtil.SDCARD_MNT=%s", FileUtil.SDCARD_MNT));
            ZLogger.d(String.format("FileUtil.IS_SDCARD_EXIST=%s",
                    String.valueOf(FileUtil.IS_SDCARD_EXIST)));
        } else {
            ZLogger.df(getAppContext().getString(R.string.permission_not_granted,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE));
        }

        ZLogger.d(String.format("1px=%ddip", DensityUtil.px2dip(getAppContext(), 1)));
        ZLogger.d(String.format("1dip=%dpx", DensityUtil.dip2px(getAppContext(), 1)));


        try {
            //获取NavigationBar的高度
            StringBuilder sb = new StringBuilder();

            Resources resources = getAppContext().getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height",
                    "dimen", "android");
            sb.append(String.format("DisplayMetrics: %d*%d %f%navigation_bar_height:%d\n",
                    resources.getDisplayMetrics().widthPixels,
                    resources.getDisplayMetrics().heightPixels,
                    resources.getDisplayMetrics().density,
                    resources.getDimensionPixelSize(resourceId)));

            sb.append(String.format("SHA1:%s\n", sHA1(getAppContext())));
            sb.append(String.format("AndroidId:%s\n", getAndroidId()));
            sb.append(String.format("simSerialNumber:%s\n", getSimSerialNumber()));
            sb.append(String.format("deviceId(IMEI):%s\n", getDeviceId()));
            sb.append(String.format("linuxMac:%s\n", getLinuxMac()));
            sb.append(String.format("wifiMacAddress:%s\n", getWifiMacAddress()));
            sb.append(String.format("wifiIpAddress:%s\n", getWifiIpAddress()));
            sb.append(String.format("wifiMac15Bit:%s\n", getWifiMac15Bit()));
            sb.append(String.format("hostAddress:%s\n", getHostAddress()));
            sb.append(String.format("hostIpAddress:%s\n", getHostIpAddress()));
            sb.append(String.format("deviceUUID:%s\n",
                    new DeviceUuidFactory(getAppContext()).getDeviceUuid()));
            ZLogger.d(sb.toString());
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

        ZLogger.d(TimeUtil.timeDateFormat12.format(new Date()));//Oct 16, 2015  1:51 PM
        ZLogger.d(new SimpleDateFormat(TimeUtil.DATE_TIME_FORMAT_12_HOUR, Locale.CHINA).
                format(new Date()));//10月 16, 2015  1:51 下午
        ZLogger.d(TimeUtil.timeDateFormat24.format(new Date()));//Oct 16, 2015  13:51
        ZLogger.d(new SimpleDateFormat(TimeUtil.DATE_TIME_FORMAT_24_HOUR, Locale.CHINA)
                .format(new Date()));//10月 16, 2015  13:51

        ZLogger.d(String.format("timeStamp1:%d", System.currentTimeMillis()));
        ZLogger.d(String.format("timeStamp2:%s", String.valueOf(System.currentTimeMillis())));


//        支付方式 1-现金 2- 满分 4-支付宝 8-微信 16-银联
        ZLogger.d(String.format("1&2=%d", 1 & 2));
        ZLogger.d(String.format("1&4=%d", 1 & 4));
        ZLogger.d(String.format("1&8=%d", 1 & 8));
        ZLogger.d(String.format("1&16=%d", 1 & 16));
        ZLogger.d(String.format("2&1=%d", 2 & 1));
        ZLogger.d(String.format("2&4=%d", 2 & 4));
        ZLogger.d(String.format("2&8=%d", 2 & 8));
        ZLogger.d(String.format("2&16=%d", 2 & 16));

//        ZLogger.d(String.format("1^1=%d, \t1|1=%d", 1^1, 1|1));//'1^1' can be replaced with '0','1|1' can be replaced with '1'
        ZLogger.d(String.format("1^2=%d, \t1|2=%d", 1 ^ 2, 1 | 2));
        ZLogger.d(String.format("1^4=%d, \t1|4=%d", 1 ^ 4, 1 | 4));
        ZLogger.d(String.format("1^8=%d, \t1|8=%d", 1 ^ 8, 1 | 8));
        ZLogger.d(String.format("1^16=%d, \t1|16=%d", 1 ^ 16, 1 | 16));
        ZLogger.d(String.format("2^1=%d, \t2|1=%d", 2 ^ 1, 2 | 1));
//        ZLogger.d(String.format("2^2=%d, \t2|2=%d", 2^2, 2|2));
        ZLogger.d(String.format("2^4=%d, \t2|4=%d", 2 ^ 4, 2 | 4));
        ZLogger.d(String.format("2^8=%d, \t2|8=%d", 2 ^ 8, 2 | 8));
        ZLogger.d(String.format("2^16=%d, \t2|16=%d", 2 ^ 16, 2 | 16));

        DecimalFormat decimalFormat = new DecimalFormat("##.##");
        ZLogger.d(String.format("12=%s", decimalFormat.format(12)));
        ZLogger.d(String.format("12.58=%s", decimalFormat.format(12.589)));
        ZLogger.d(String.format("12.54321=%s", decimalFormat.format(12.54321)));

        ZLogger.d("10=" + Integer.toHexString(10));//a
        ZLogger.d("10=" + Integer.toString(10, 16));//a
        ZLogger.d("10=" + Integer.toBinaryString(10));//1010
        for (int i = 0; i < 20; i++) {
            ZLogger.d(String.format("%d = %02X", i, (byte) i));//00,01,02
        }

        String testStr = "12345556665453";
        String testStr2 = "{BNo.12478855";
        String testStr3 = "12306\r\n";
        ZLogger.d(String.format("%s = %s", testStr,
                DataConvertUtil.ByteArrToHex(testStr.getBytes())));
        ZLogger.d(String.format("%s = %s", testStr,
                DataConvertUtil.ByteArrToHex(testStr3.getBytes())));
        ZLogger.d(String.format("%s = %s", testStr,
                DataConvertUtil.ByteArrToHex(testStr.getBytes(), "")));
        ZLogger.d(String.format("%s = %s", testStr,
                DataConvertUtil.ByteArrToHex(testStr2.getBytes(), "")));

//        Integer.toHexString(x & 0Xff);
        String rawHexStr = "0A0D20313735342020202032353020202020203433390A0D20313735342020202032353020202020203433390";

        if (!rawHexStr.startsWith("0A0D")) {
            ZLogger.d(String.format("format is wrong, [%s]", rawHexStr));
        } else {
            String rawDataArr[] = rawHexStr.split("0A0D");
            for (String rawStr : rawDataArr) {
                byte[] rawByteArr = DataConvertUtil.HexToByteArr(rawStr);
                //5+(2)+5+(2)+6=20
                //20:SPACE
                //HEADER:0A0D
                if (rawByteArr.length == 20) {
                    for (int j = 0; j < rawByteArr.length; ++j) {
                        ZLogger.d(String.format("%d-%c", j, (char) rawByteArr[j]));
                    }
                    ZLogger.d(String.format("hex:%s\n byte:%s", rawStr, Arrays.toString(rawByteArr)));
                    String netWeight = DataConvertUtil.bytesToAsciiString(rawByteArr, 0, 5);
                    String unitPrice = DataConvertUtil.bytesToAsciiString(rawByteArr, 7, 5);
                    String totalPrice = DataConvertUtil.bytesToAsciiString(rawByteArr, 14, 5);
                    ZLogger.d(String.format("netWeight=%s, unitPrice=%s, totalPrice=%s",
                            netWeight, unitPrice, totalPrice));
                } else {
                    ZLogger.d(String.format("length is wrong, [%s] ", rawStr));
                }
            }
        }

        try {
            ZLogger.d("0x674d9e76:" + String.valueOf(Long.parseLong("0x674d9e76", 16)));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
        try {
            ZLogger.d("674d9e76:" + String.valueOf(Long.parseLong("674d9e76", 16)));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
        try {
            ZLogger.d("769e4d67:" + String.valueOf(Long.parseLong("769e4d67", 16)));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
        try {
            ZLogger.d("1990085991:" + String.valueOf(Long.parseLong("1990085991", 16)));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

        boolean ret = EncryptUtil.validPwd("732d078bed4c2b0f5ccd6c0442790a453e2ac424", "053927b27f560434", "196715");
        ZLogger.d("validPwd:" + ret);
    }

}

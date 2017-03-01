package com.mfh.framework;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.config.ConfigsParseHelper;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.DeviceUuidFactory;
import com.mfh.framework.anlaysis.crash.AppException;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.EncryptUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.PhoneUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Security;
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

        ZLogger.CRASH_FOLDER_PATH = getPackageName() + File.separator + "zlogger";
//        AppException.CRASH_FOLDER_PATH = getPackageName() + File.separator + "crash";

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
        String appid = SharedPrefesManagerFactory.getAppUniqueId();

        if (StringUtils.isEmpty(appid)) {
            appid = UUID.randomUUID().toString();
            SharedPrefesManagerFactory.setAppUniqueId(appid);
        }
        return appid;
    }



    /**
     * 获取UserAgent,登录时填入Header
     */
    public static String getUserAgent() {
        String userAgent = SharedPrefesManagerFactory.getAppUserAgent();

        if (StringUtils.isEmpty(userAgent)) {
            StringBuilder ua = new StringBuilder();

            AppInfo appInfo = AnalysisAgent.getAppInfo(MfhApplication.getAppContext());
            if (appInfo != null) {
                ua.append(appInfo.getPackageName());
                ua.append('/' + appInfo.getVersionName() + '_' + appInfo.getVersionCode());//App版本
            }

            ua.append("/Android");//手机系统平台
            ua.append("/" + android.os.Build.VERSION.RELEASE);//手机系统版本
            ua.append("/" + android.os.Build.MODEL); //手机型号
            ua.append("/" + getAppId());//客户端唯一标识
            userAgent = ua.toString();

            SharedPrefesManagerFactory.setAppUserAgent(userAgent);
        }
        return userAgent;
    }


    /**
     * 生成序列号:包名＋15位 mac address
     */
    public static String genSerialNo() {
        String packageName = "";
        PackageInfo packageInfo = SystemUtils.getPackageInfo(context, 0);
        if (packageInfo != null) {
            packageName = packageInfo.packageName;
        }

        String macAddress = NetworkUtils.getWifiMacAddress(getAppContext());
        if (macAddress == null) {
            return packageName;
        }
        macAddress = macAddress.replace(":", "");
        macAddress = "000" + macAddress;  //add 000 for 15dit
        macAddress = macAddress.toUpperCase();
        return packageName + macAddress;
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
     * */
    public static void debugPrint() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("apkInstallPath", getAppContext().getPackageCodePath());
        jsonObject.put("pid", android.os.Process.myPid());
        jsonObject.put("processName", getProcessName(getAppContext(), android.os.Process.myPid()));
        jsonObject.put("isNetworkConnected", NetworkUtils.isConnect(getAppContext()));
        jsonObject.put("networkState", NetworkUtils.getNetworkState(getAppContext()));
        jsonObject.put("wifiMacAddress", NetworkUtils.getWifiMacAddress(getAppContext()));
        jsonObject.put("wifiIpAddress", NetworkUtils.getWifiIpAddress(getAppContext()));
        jsonObject.put("IMEI", PhoneUtils.getImei(getAppContext()));
        jsonObject.put("IMSI", PhoneUtils.getImsi(getAppContext()));
        jsonObject.put("simSerialNumber", PhoneUtils.getSimSerialNumber(getAppContext()));
        jsonObject.put("Signature-SHA1", SystemUtils.getSignature(getAppContext()));
        jsonObject.put("Signature-MD5", SystemUtils.getSignatureMD5(getAppContext()));
        jsonObject.put("applicationLabel", SystemUtils.getApplicationLabel(getAppContext()));
        jsonObject.put("cupInfo", SystemUtils.getCpuInfo());
        jsonObject.put("mobileInfo", SystemUtils.getMobileInfo());
        jsonObject.put("sdkVersion", SystemUtils.getSdkVersion());
        jsonObject.put("userAgent", getUserAgent());
        jsonObject.put("appInfo", AnalysisAgent.getAppInfo(getAppContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jsonObject.put("supportedABIS", Build.SUPPORTED_ABIS);
        }
        else{
            jsonObject.put("cupABI", Build.CPU_ABI);
        }
        jsonObject.put("Security.providers", Security.getProviders());


        ZLogger.d(jsonObject.toJSONString());

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
        ZLogger.d(String.format("1dip=%dpx", DensityUtil.dip2px(getAppContext(), 1.0f)));
        ZLogger.d(String.format("1dp=%dpx", DensityUtil.dp2px(getAppContext(), 1)));
        ZLogger.d(String.format("1sp=%dpx", DensityUtil.sp2px(getAppContext(), 1.0f)));
        ZLogger.d(String.format("1sp=%dpx", DensityUtil.sp2px(getAppContext(), 1)));
        ZLogger.d(String.format("1px=%dsp", DensityUtil.px2sp(getAppContext(), 1)));

        try {
            //获取NavigationBar的高度
            StringBuilder sb = new StringBuilder();

            Resources resources = getAppContext().getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height",
                    "dimen", "android");
            sb.append(String.format("DisplayMetrics: " +
                    "\n%d*%d(%d) %f" +
                    "\navigation_bar_height:%d(%d)" +
                    "\n(%d*%d)\n",
                    resources.getDisplayMetrics().widthPixels, resources.getDisplayMetrics().heightPixels,
                    resources.getDisplayMetrics().densityDpi, resources.getDisplayMetrics().density,
                    resources.getDimensionPixelSize(resourceId),
                    DensityUtil.px2dip(getAppContext(), resources.getDimensionPixelSize(resourceId)),
                    DensityUtil.px2dip(getAppContext(), resources.getDisplayMetrics().widthPixels),
                    DensityUtil.px2dip(getAppContext(), resources.getDisplayMetrics().heightPixels)));
            sb.append(String.format("AndroidId:%s\n", getAndroidId()));
            sb.append(String.format("linuxMac:%s\n", getLinuxMac()));
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
        ZLogger.d(String.format("1&4=%d", 1 & 4));
        ZLogger.d(String.format("1&8=%d", 1 & 8));
        ZLogger.d(String.format("1&16=%d", 1 & 16));
        ZLogger.d(String.format("2&1=%d", 2 & 1));
        ZLogger.d(String.format("2&4=%d", 2 & 4));
        ZLogger.d(String.format("2&8=%d", 2 & 8));
        ZLogger.d(String.format("2&16=%d", 2 & 16));

        //'^'异或，有一个为假即为真；'|'或，有一个为真即为真；'&'与，同时为真才为真。
//        ZLogger.d(String.format("1^1=%d, \t1|1=%d", 1^1, 1|1));//'1^1' can be replaced with '0','1|1' can be replaced with '1'
        ZLogger.d(String.format("1^1=%d, \t1|1=%d, \t1&1=%d", 1^1, 1|1,1&1));
        ZLogger.d(String.format("1^2=%d, \t1|2=%d, \t1&2=%d", 1^2, 1|2,1&2));
        ZLogger.d(String.format("1^3=%d, \t1|3=%d, \t1&3=%d", 1^3, 1|3,1&3));
        ZLogger.d(String.format("1^4=%d, \t1|4=%d, \t1&4=%d", 1^4, 1|4,1&2));
//        2^2=0, 	2|2=2 	2&2=2
        ZLogger.d(String.format("2^2=%d, \t2|2=%d, \t2&2=%d", 2^2, 2|2, 2&2));

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


        Double recharegeAmount = 0.0028D;
        if (recharegeAmount < 0.01) {
            ZLogger.d(String.format("%f < 0.01", recharegeAmount));
        } else {
            ZLogger.d(String.format("%f >= 0.01", recharegeAmount));
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

        ZLogger.d(String.format("decodePwd:[%s]-[%s]\n--[%s]",
                "e4526e080f1a395cecb64dad92543cbff8dddd7c",
                "7af0d44c36438091",
                EncryptUtil.decodePwd("7af0d44c36438091", "e4526e080f1a395cecb64dad92543cbff8dddd7c")));
        ZLogger.d(String.format("decodePwd:[%s]-[%s]\n--[%s]",
                "b59fb451d718d8bdeff216bbedc503ed0eb15906",
                "51313a52e04695c9",
                EncryptUtil.decodePwd("51313a52e04695c9", "b59fb451d718d8bdeff216bbedc503ed0eb15906")));
    }

}

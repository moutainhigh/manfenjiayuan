package com.mfh.framework.anlaysis.logger;

import android.util.Log;

import com.mfh.framework.anlaysis.crash.AppException;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * Utility class that 日志记录工具
 * <ol>
 * <li>程序运行日志记录工具</li>
 * <li>参考1:{@see http://kaizige.vip/2016/06/13/klog/  Android专用Log打印工具KLog}</li>
 * <li>颜色参考：A=FF2E0E D=38F838 E=FF6B68 I=1DBB92 V=FFF7EE W=FF9229
 * </li>
 * </ol>
 *
 * @author bingshanguxue
 */
public class ZLogger {
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String NULL_TIPS = "Log with null object";

    public static String TAG = "Mfh";

    public static boolean LOG_ENABLED = true;
    private static boolean bShowLink = false;//是否显示链接

    //Log level
    private static final int VERBOSE = 0x01;
    private static final int DEBUG = 0x02;
    private static final int INFO = 0x03;
    private static final int WARN = 0x04;
    private static final int ERROR = 0x05;
    private static final int ASSET = 0x06;
    private static final int JSON = 0x07;
    private static final int XML = 0x08;


    private static final int VERBOSE_FILE = 0x11;
    private static final int DEBUG_FILE = 0x12;
    private static final int INFO_FILE = 0x13;
    private static final int WARN_FILE = 0x14;
    private static final int ERROR_FILE = 0x15;
    private static final int ASSET_FILE = 0x16;
    private static final int JSON_FILE = 0x17;
    private static final int XML_FILE = 0x18;

    private int logLevel = VERBOSE;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static String CRASH_FOLDER_PATH = "ZLogger";//可以在外面修改


    private static WriteFileThread mWriteFileThread = new WriteFileThread();

    /**
     * 初始化
     *
     * @param tag 设置默认的TAG
     */
    public static void initialize(String tag) {
    }

    /**
     * verbose
     */
    public static void v(String log) {
//        printWrapper(VERBOSE, null, log);
        if (LOG_ENABLED) {
//            Log.v(TAG, String.format("%s %s", log, callMethodAndLine()));
            printWrapper(VERBOSE, TAG, log);
        }
    }

    /**
     * verbose
     */
    public static void v(String tag, String log) {
//        printWrapper(VERBOSE, tag, log);
        if (LOG_ENABLED) {

            Log.v(tag, String.format("%s %s", log, callMethodAndLine()));

            printWrapper(VERBOSE, tag, log);
        }
    }

    /**
     * debug
     */
    public static void d(String log) {
//        printWrapper(DEBUG, null, log);
        if (LOG_ENABLED) {
//            Log.d(String.format("%s--%d/%d", TAG, MfhApplication.getAvailMemory(MfhApplication.getAppContext()), MfhApplication.getTotalMemory(MfhApplication.getAppContext())),
//                    String.format("%s %s", log, callMethodAndLine()));
            printWrapper(DEBUG, TAG, log);
        }

//        saveToSDCardAsync(log);
    }

    /**
     * debug
     */
    public static void d(String tag, String log) {
        if (LOG_ENABLED) {
//            Log.d(tag, String.format("%s %s", log, callMethodAndLine()));
            printWrapper(DEBUG, tag, log);
        }
    }

    /**
     * debug + saveToSdCard
     */
    public static void df(String log) {
//        printWrapper(DEBUG, null, log);
        if (LOG_ENABLED || SharedPrefesManagerFactory.isSuperPermissionGranted()) {
//            Log.d(String.format("%s--%d/%d", TAG, MfhApplication.getAvailMemory(MfhApplication.getAppContext()), MfhApplication.getTotalMemory(MfhApplication.getAppContext())),
//                    String.format("%s %s", log, callMethodAndLine()));

            printWrapper(DEBUG_FILE, TAG, log);
        }
    }

    /**
     * info
     */
    public static void i(String log) {
        if (LOG_ENABLED) {

//            Log.i(TAG, log);
            printWrapper(INFO, TAG, log);
        }
    }

    public static void i2f(String log) {
        if (LOG_ENABLED) {

//            Log.i(TAG, log);
            printWrapper(INFO_FILE, TAG, log);
        }
    }

    /**
     * Warn
     */
    public static void w(String tag, String log) {
        if (LOG_ENABLED) {

//            Log.w(tag, String.format("%s %s", log, callMethodAndLine()));
            printWrapper(WARN, tag, log);
        }
    }

    /**
     * Warn
     */
    public static void w(String log) {
        if (LOG_ENABLED) {

//            Log.w(TAG, String.format("%s %s", log, callMethodAndLine()));
            printWrapper(WARN, TAG, log);
        }
    }

    /**
     * Warn
     */
    public static void wf(String log) {
        if (LOG_ENABLED) {

//            Log.w(TAG, String.format("%s %s", log, callMethodAndLine()));
            printWrapper(WARN_FILE, TAG, log);
        }
    }

    /**
     * info
     */
    public static void i(String tag, String log) {
        if (LOG_ENABLED) {

//            Log.i(tag, String.format("%s %s", log, callMethodAndLine()));
            printWrapper(INFO, tag, log);
        }
    }

    /**
     * error
     */
    public static void e(String log) {
//        if (LOG_ENABLED)
//        Log.e(TAG, String.format("%s %s", log, callMethodAndLine()));

        printWrapper(ERROR_FILE, TAG, log);
    }

    /**
     * error
     */
    public static void e(String tag, String log) {
        Log.e(tag, String.format("%s %s", log, callMethodAndLine()));

        printWrapper(ERROR, tag, log);
    }

    /**
     * debug + saveToSdCard
     */
    public static void ef(String log) {
//        printWrapper(DEBUG, null, log);
//        Log.e(String.format("%s--%d/%d", TAG, MfhApplication.getAvailMemory(MfhApplication.getAppContext()), MfhApplication.getTotalMemory(MfhApplication.getAppContext())),
//                String.format("%s %s", log, callMethodAndLine()));
        printWrapper(ERROR_FILE, TAG, log);
    }

    /**
     * @return 当前的类名(simpleName)
     */
    private static String getClassName() {
        String result;
        StackTraceElement traceElement = (new Exception()).getStackTrace()[2];
        result = traceElement.getClassName();
        int lastIndex = result.lastIndexOf(".");
        result = result.substring(lastIndex + 1, result.length());
        return result;
    }

    /**
     * 显示超链
     */
    private static String callMethodAndLine() {
        if (!bShowLink) {
            return "";
        }
        String result = "at ";
        StackTraceElement traceElement = (new Exception()).getStackTrace()[1];
        result += traceElement.getClassName() + ".";
        result += traceElement.getMethodName();
        result += "(" + traceElement.getFileName();
        result += ":" + traceElement.getLineNumber() + ")  ";
        return result;
    }


    private static void printWrapper(int level, String tagStr, Object objectMsg) {
        if (!LOG_ENABLED) {
            return;
        }
        String[] contents = LogWrapper.wrapper(tagStr, objectMsg);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        print(level, tag, headString + msg);
    }

    private static void print(int level, String tag, String msg) {
        if (!LOG_ENABLED) {
            return;
        }

        switch (level) {
            case VERBOSE:
                Log.v(tag, msg);
                break;
            case DEBUG_FILE:
                saveToSDCardAsync(String.format("%s >> %s", tag, msg));
            case DEBUG:
                Log.d(tag, msg);
                break;
            case INFO:
                Log.i(tag, msg);
                break;
            case WARN:
                Log.w(tag, msg);
                break;
            case ERROR_FILE:
                saveToSDCardAsync(String.format("%s >> %s", tag, msg));
            case ERROR:
                Log.e(tag, msg);
                break;
            case ASSET:
                Log.wtf(tag, msg);

                break;
        }
    }





    /**
     * 刷新显示线程
     */
    private static class WriteFileThread extends Thread {
        private Queue<String> mLogQueue = new LinkedList<>();

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                final String log;
                while ((log = mLogQueue.poll()) != null) {
                    saveToSDCard(log);

                    try {
                        Thread.sleep(50);//显示性能高的话，可以把此数值调小。
                    } catch (Exception e) {
//                        e.printStackTrace();
                        ZLogger.e(e.toString());
                    }
                    break;
                }
            }
        }

        public synchronized void addQueue(String log) {
            mLogQueue.add(log);
        }
    }

    public static File getCrashFile() {
        return FileUtil.getSaveFile(CRASH_FOLDER_PATH, AppException.CRASH_FILE_NAME);
    }

    /**
     * 保存到SD卡中
     */
    private static void saveToSDCardAsync(String message) {
//        if (mWriteFileThread != null){
//            mWriteFileThread.addQueue(message);
//            mWriteFileThread.start();
//        }
//        else{
//            mWriteFileThread = new WriteFileThread();
//        }
        saveToSDCard(message);
    }

    private static void saveToSDCard(String message) {
//        long timestamp = System.currentTimeMillis();
        String time = DATE_FORMAT.format(new Date());
        String fileName = time + ".log";

        File file = FileUtil.getSaveFile(CRASH_FOLDER_PATH, fileName);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(
                    file, true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pw != null) {
            // 导出发生异常的时间
            pw.println(String.format("%s %s",
                    TimeUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSSZ"), message));
            // 导出手机信息
//        dumpPhoneInfo(pw);
            pw.println();
            pw.close();
        }
    }

    /**
     * 删除过期日志文件
     */
    public static void deleteOldFiles(int saveDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0 - saveDate);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
//        ZLogger.d("" + DATE_FORMAT.format(calendar.getTime()));

        String path = FileUtil.getSavePath(CRASH_FOLDER_PATH);
        ZLogger.d(path);
        File file = new File(path);

        if (!file.exists()) {
//            ZLogger.d(String.format("%s 不存在", file.getAbsolutePath()));
            return;
        }

        if (!file.isDirectory()) {
//            ZLogger.d(String.format("%s 不是目录", file.getAbsolutePath()));
            return;
        }

        for (File child : file.listFiles()) {
            String name = child.getName();
            if (child.isDirectory()) {
//                ZLogger.d(String.format("%s是目录，不需要删除", child.getName()));
                continue;
            }

            try {
                //文件名格式是2016-01-01.log，所以格式不对的，也要删除。系统中可能存在很多2016-0001-01.log等等这样的文件。
                if (StringUtils.isEmpty(name) || name.length() != 14) {
                    ZLogger.d(String.format("删除无效日志文件 %s", name));
                    child.delete();
                    continue;
                }
                Date date = DATE_FORMAT.parse(name);
//                ZLogger.d(" child:" + DATE_FORMAT.format(date));
                if (date.before(calendar.getTime())) {
                    ZLogger.d(String.format("日志已经过期，删除%s", name));
                    child.delete();
                } else {
//                    ZLogger.d(String.format("日志有效，%s", child.getName()));
//                    child.delete();
                }
            } catch (ParseException e) {
                child.delete();
//                e.printStackTrace();
                ZLogger.ef(String.format("%s", e.toString()));
            }
        }
    }
}

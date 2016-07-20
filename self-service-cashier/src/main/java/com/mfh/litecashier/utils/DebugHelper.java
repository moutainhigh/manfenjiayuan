package com.mfh.litecashier.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.litecashier.CashierApp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kun on 16/1/21.
 */
public class DebugHelper {

    public static void debug(){
        ZLogger.d(String.format("FileUtil.SDCARD=%s", FileUtil.SDCARD));///mnt/sdcard
        ZLogger.d(String.format("FileUtil.SDCARD_ABS=%s", FileUtil.SDCARD_ABS));
//		ZLogger.d(String.format("FileUtil.DATA=%s", FileUtil.DATA));///data/data/com.mfh.cashier/files
        ZLogger.d(String.format("FileUtil.SDCARD_MNT=%s", FileUtil.SDCARD_MNT));
        ZLogger.d(String.format("FileUtil.IS_SDCARD_EXIST=%s", String.valueOf(FileUtil.IS_SDCARD_EXIST)));

        ZLogger.d(String.format("process name:\t%s", CashierApp.getProcessName(CashierApp.getAppContext(), android.os.Process.myPid())));
        ZLogger.d(String.format("apk install path:\t%s", CashierApp.getAppContext().getPackageCodePath()));

        // 从AndroidManifest.xml的meta-data中读取SDK配置信息
        String packageName = CashierApp.getAppContext().getPackageName();
        try {
            ApplicationInfo appInfo = CashierApp.getAppContext().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            StringBuilder sb = new StringBuilder();
            sb.append("metaData:{\n");
            if (appInfo.metaData != null) {
                sb.append(String.format("\tUMENG_APP_KEY\t=%s\n", appInfo.metaData.getString("UMENG_APPKEY")));
                sb.append(String.format("\tGETUI_APP_ID\t=%s\n", appInfo.metaData.getString("PUSH_APPID")));
                sb.append(String.format("\tGETUI_APP_SECRET=%s\n", appInfo.metaData.getString("PUSH_APPSECRET")));
                sb.append(String.format("\tGETUI_APP_KEY\t=%s\n", appInfo.metaData.getString("PUSH_APPKEY")));
            }
            sb.append("}\n");
            ZLogger.d(sb.toString());
        } catch (PackageManager.NameNotFoundException e) {
            ZLogger.e(e.toString());
//            e.printStackTrace();
        }


        MUtils.getOrderBarCode();

        ZLogger.d(TimeUtil.timeDateFormat12.format(new Date()));//Oct 16, 2015  1:51 PM
        ZLogger.d(new SimpleDateFormat(TimeUtil.DATE_TIME_FORMAT_12_HOUR, Locale.CHINA).format(new Date()));//10月 16, 2015  1:51 下午
        ZLogger.d(TimeUtil.timeDateFormat24.format(new Date()));//Oct 16, 2015  13:51
        ZLogger.d(new SimpleDateFormat(TimeUtil.DATE_TIME_FORMAT_24_HOUR, Locale.CHINA).format(new Date()));//10月 16, 2015  13:51

        ZLogger.d(String.format("timeStamp1:%d", System.currentTimeMillis()));
        ZLogger.d(String.format("timeStamp2:%s", String.valueOf(System.currentTimeMillis())));

        ZLogger.d(String.format("1px=%ddip", DensityUtil.px2dip(CashierApp.getAppContext(), 1)));
        ZLogger.d(String.format("1dip=%dpx", DensityUtil.dip2px(CashierApp.getAppContext(), 1)));

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
        for (int i = 0; i < 20; i++){
            ZLogger.d(String.format("%d = %02X", i, (byte) i));//00,01,02
        }

        String testStr = "12345556665453";
        String testStr2 = "{BNo.12478855";
        String testStr3 = "12306\r\n";
        ZLogger.d(String.format("%s = %s", testStr, DataConvertUtil.ByteArrToHex(testStr.getBytes())));
        ZLogger.d(String.format("%s = %s", testStr, DataConvertUtil.ByteArrToHex(testStr3.getBytes())));
        ZLogger.d(String.format("%s = %s", testStr, DataConvertUtil.ByteArrToHex(testStr.getBytes(), "")));
        ZLogger.d(String.format("%s = %s", testStr, DataConvertUtil.ByteArrToHex(testStr2.getBytes(), "")));

//        Integer.toHexString(x & 0Xff);
        String rawHexStr = "0A0D20313735342020202032353020202020203433390A0D20313735342020202032353020202020203433390";

        if (!rawHexStr.startsWith("0A0D")){
            ZLogger.d(String.format("format is wrong, [%s]", rawHexStr));
        }
        else{
            String rawDataArr[] = rawHexStr.split("0A0D");
            for (String rawStr : rawDataArr){
                byte[] rawByteArr = DataConvertUtil.HexToByteArr(rawStr);
                //5+(2)+5+(2)+6=20
                //20:SPACE
                //HEADER:0A0D
                if (rawByteArr.length == 20){
                    for(int j = 0; j < rawByteArr.length; ++j) {
                        ZLogger.d(String.format("%d-%c", j, (char) rawByteArr[j]));
                    }
                    ZLogger.d(String.format("hex:%s\n byte:%s", rawStr, Arrays.toString(rawByteArr)));
                    String netWeight = DataConvertUtil.bytesToAsciiString(rawByteArr, 0, 5);
                    String unitPrice = DataConvertUtil.bytesToAsciiString(rawByteArr, 7, 5);
                    String totalPrice = DataConvertUtil.bytesToAsciiString(rawByteArr, 14, 5);
                    ZLogger.d(String.format("netWeight=%s, unitPrice=%s, totalPrice=%s", netWeight, unitPrice, totalPrice));
                }
                else{
                    ZLogger.d(String.format("length is wrong, [%s] ", rawStr));
                }
            }
        }

        boolean ret = EncryptUtil.validPwd("732d078bed4c2b0f5ccd6c0442790a453e2ac424", "053927b27f560434", "196715");
        ZLogger.d("validPwd:" + ret);

        try{
            ZLogger.d("0x674d9e76:" + String.valueOf(Long.parseLong("0x674d9e76", 16)));
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }
        try{
            ZLogger.d("674d9e76:" + String.valueOf(Long.parseLong("674d9e76", 16)));
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }
        try{
            ZLogger.d("769e4d67:" + String.valueOf(Long.parseLong("769e4d67", 16)));
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }
        try{
            ZLogger.d("1990085991:" + String.valueOf(Long.parseLong("1990085991", 16)));
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }


//        ZLogger.d("cardNo.decode:" + decode("466CAF31"));
//        ZLogger.d("cardNo.decode:" + decode("0512000105"));
//        ZLogger.d("cardNo.decode:" + decode("0512 000105"));
    }
}

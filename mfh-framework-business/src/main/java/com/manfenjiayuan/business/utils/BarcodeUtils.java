package com.manfenjiayuan.business.utils;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

/**
 * 条码工具
 * Created by bingshanguxue on 14/12/2016.
 */

public class BarcodeUtils {

    public static final int BARCODE_NA = -1;//未设置
    /**
     * 寺冈电子秤——13位条码，以'2'开头；
     * 格式：F CCCCCC XXXXX CD，其中F是标志位，C是条码,X是重量，CD是校验位
     */
    public static final int BARCODE_DIGI = 1;
    public static final int BARCODE_UNKNOWN = 99;

    /**
     * 获取条码类型
     * */
    public static int getType(String baracode){
        if (StringUtils.isEmpty(baracode)){
            return BARCODE_NA;
        }

        if (baracode.startsWith("2") && baracode.length() == 13){
            return BARCODE_DIGI;
        }

        return BARCODE_UNKNOWN;
    }


    /**
     * 获取条码对应的plu码
     * @param baracode 13位条码，以'2'开头
     *                 调用该方法前先判断参数是否有效
     * {@link #BARCODE_DIGI}
     * */
    public static String getDigiPlu(String baracode){
        try{
            String plu = baracode.substring(1, 7);
            //有小数点，单位克转换成千克。
            String weightStr = String.format("%s.%s", baracode.substring(7, 9), baracode.substring(9, 12));
            Double weight = Double.valueOf(weightStr);
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()){
                ZLogger.d(String.format("BARCODE_DIGI 条码：%s, PLU码：%s, 重量：%f",
                        baracode, plu, weight));
            }
            return plu;
        }
        catch (Exception e){
            return null;
        }
    }

}

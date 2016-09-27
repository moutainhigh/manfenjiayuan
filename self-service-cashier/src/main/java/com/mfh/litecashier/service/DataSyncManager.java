package com.mfh.litecashier.service;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class DataSyncManager {

    /**
     * 获取POS商品库同步时间
     */
    public static String getPosLastUpdateCursor() {
        String startCursor = SharedPreferencesHelper.getSyncProductsCursor();
        ZLogger.df(String.format("上次商品更新时间(%s)。", startCursor));

//        //得到指定模范的时间
        if (!StringUtils.isEmpty(startCursor)) {
            try {
                Date d1 = TimeCursor.InnerFormat.parse(startCursor);
                Date rightNow = new Date();
                if (d1.compareTo(rightNow) > 0) {
                    startCursor = TimeCursor.InnerFormat.format(rightNow);
                    ZLogger.df(String.format("上次商品更新时间大于当前时间，使用当前时间(%s)。", startCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(String.format("获取POS商品库同步时间失败: %s", e.toString()));
            }
        }

        return startCursor;
    }

    /**
     * 获取商品和类目关系表开始游标
     */
    public static String getProductCatalogStartCursor() {
        String startCursor = SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_SYNC_PRODUCTCATALOG_STARTCURSOR);
        ZLogger.df(String.format("上次商品和类目关系表更新游标(%s)。", startCursor));

//        //得到指定模范的时间
        if (!StringUtils.isEmpty(startCursor)) {
            try {
                Date d1 = TimeCursor.InnerFormat.parse(startCursor);
                Date rightNow = new Date();
                if (d1.compareTo(rightNow) > 0) {
                    startCursor = TimeCursor.InnerFormat.format(rightNow);
                    ZLogger.df(String.format("上次商品和类目关系表更新游标大于当前时间，使用当前时间(%s)。", startCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(String.format("获取商品和类目关系表开始游标失败: %s", e.toString()));
            }
        }

        return startCursor;
    }
}

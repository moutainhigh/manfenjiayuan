package com.mfh.litecashier.service;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class DataManagerHelper {
    public static final int MAX_SYNC_PAGESIZE = 40;

    /**
     * 获取POS商品档案同步时间
     */
    public static String getPosLastUpdateCursor() {
        String startCursor = SharedPreferencesUltimate.getSyncProductsCursor();
        ZLogger.df(String.format("商品档案同步游标:(%s)。", startCursor));

//        //得到指定模范的时间
        if (!StringUtils.isEmpty(startCursor)) {
            try {
                Date d1 = TimeCursor.InnerFormat.parse(startCursor);
                Date rightNow = new Date();
                if (d1.compareTo(rightNow) > 0) {
                    startCursor = TimeCursor.InnerFormat.format(rightNow);
                    ZLogger.df(String.format("商品档案同步游标大于当前时间，使用当前时间(%s)。", startCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(String.format("获取商品档案同步游标失败: %s", e.toString()));
            }
        }

        return startCursor;
    }

    /**
     * 获取商品和类目关系表开始游标
     */
    public static String getProductCatalogStartCursor() {
        String startCursor = SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_SYNC_PRODUCTCATALOG_STARTCURSOR);
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

    /**
     * 获取订单同步时间游标
     * */
    public static String getPosOrderStartCursor() {
        String lastSyncCursor = SharedPreferencesUltimate
                .getText(SharedPreferencesUltimate.PK_S_POSORDER_SYNC_STARTCURSOR);
        ZLogger.df(String.format("上次订单同步时间游标(%s)。", lastSyncCursor));

        //与当前时间相比，取最小当时间
        if (!StringUtils.isEmpty(lastSyncCursor)) {
            //得到指定模范的时间
            try {
                Date lastSyncDate = TimeCursor.InnerFormat.parse(lastSyncCursor);
                Date rightNow = new Date();
                if (lastSyncDate.compareTo(rightNow) > 0) {
                    lastSyncCursor = TimeCursor.InnerFormat.format(rightNow);
//                    SharedPreferencesUltimate.setPosOrderLastUpdate(d2);
                    ZLogger.df(String.format("上次订单同步时间大于当前时间，使用当前时间(%s)。", lastSyncCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }

        return lastSyncCursor;
    }

    /**
     * 获取订单同步时间游标
     * */
    public static String getPosOrderStartCursor2() {
        Date startDate = null;
        String sqlWhere = String.format("sellerId = '%d' " +
                        "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                PosOrderEntity.SYNC_STATUS_NONE);
        List<PosOrderEntity> orderEntities = PosOrderService.get()
                .queryAllAsc(sqlWhere, null);
        if (orderEntities != null && orderEntities.size() > 0){
            PosOrderEntity orderEntity = orderEntities.get(0);
            startDate = orderEntity.getUpdatedDate();
        }
        String startCursor = TimeUtil.format(startDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS);
        ZLogger.df(String.format("上次订单同步时间游标(%s)。",startCursor));

        //与当前时间相比，取最小当时间
        if (!StringUtils.isEmpty(startCursor)) {
            //得到指定模范的时间
            try {
                Date d1 = TimeCursor.InnerFormat.parse(startCursor);
                Date rightNow = new Date();
                if (d1.compareTo(rightNow) > 0) {
                    startCursor = TimeCursor.InnerFormat.format(rightNow);
//                    SharedPreferencesUltimate.setPosOrderLastUpdate(d2);
                    ZLogger.df(String.format("上次订单同步时间大于当前时间，使用当前时间(%s)。", startCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }

        return startCursor;
    }

    /**
     * 获取待同步的订单
     * */
    public static List<PosOrderEntity> getSyncPosOrders(int syncStatus){
        String sqlWhere = String.format("sellerId = '%d' " +
                        "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                syncStatus);

        return PosOrderService.get().queryAllAsc(sqlWhere, null);
    }
}

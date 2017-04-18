package com.manfenjiayuan.pda_supermarket.service;

import com.bingshanguxue.pda.utils.SharedPrefesManagerUltimate;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.PosOrderService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class DataManagerHelper {
    public static final int MAX_SYNC_PAGESIZE = 40;


    /**
     * 获取订单同步时间游标
     * */
    public static String getPosOrderStartCursor() {
        String lastSyncCursor = SharedPrefesManagerUltimate
                .getText(SharedPrefesManagerUltimate.PK_S_POSORDER_SYNC_STARTCURSOR);
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

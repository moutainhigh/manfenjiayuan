package com.mfh.litecashier.utils;

import android.graphics.Color;
import android.graphics.Typeface;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.vector_uikit.TextDrawable;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.CashierApp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 收银帮助类
 * Created by Nat.ZZN(bingshanguxue) on 15/9/9.
 */
public class CashierHelper {

    /**
     * 删除订单,同时删除对应订单的商品明细和支付记录
     *
     * @param orderEntity 订单
     */
    public static void deleteCashierOrder(PosOrderEntity orderEntity) {
        if (orderEntity == null) {
            return;
        }
        PosOrderService.get().deleteById(String.valueOf(orderEntity.getId()));
        //删除订单明细
        PosOrderItemService.get().deleteBy(String.format("orderId = '%d'", orderEntity.getId()));
        //删除支付记录
        PosOrderPayService.get().deleteBy(String.format("orderId = '%d'", orderEntity.getId()));
    }


    /**
     * 清除过期的订单数据，包括未完成的订单和已经完成并同步的订单
     *
     * @param saveDate 保存的天数
     */
    public static void clearOldPosOrder(int saveDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0 - saveDate);//

        String expireCursor = sdf.format(calendar.getTime());
        ZLogger.d(String.format("订单过期时间(%s)保留最近30天数据。", expireCursor));

        String lastUpdateCursor = SharedPreferencesUltimate.getPosOrderLastUpdate();
        ZLogger.d(String.format("上一次订单同步时间(%s)。", lastUpdateCursor));

        if (!StringUtils.isEmpty(lastUpdateCursor)) {
            //得到指定模范的时间
            try {
                Date d1 = sdf.parse(lastUpdateCursor);
                Date d2 = sdf.parse(expireCursor);
//            Date d2 = new Date();
                if (d2.compareTo(d1) > 0) {
                    ZLogger.d("订单过期时间大于上次更新时间，暂不清除。");
                    return;
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.e(e.toString());
            }
        }

        //清除过期的未完成订单/已经同步的订单
        ZLogger.d("清除过期的未完成订单/已经同步的订单。");
        String sqlWhere = String.format("updatedDate < '%s' and (status != '%d' or syncStatus = '%d')",
                expireCursor, PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.SYNC_STATUS_SYNCED);
        List<PosOrderEntity> orderEntities = PosOrderService.get().queryAllBy(sqlWhere);
        if (orderEntities != null && orderEntities.size() > 0) {
            for (PosOrderEntity orderEntity : orderEntities) {
                Long orderId = orderEntity.getId();
                PosOrderItemService.get().deleteBy(String.format("orderId = '%d", orderId));
                PosOrderPayService.get().deleteBy(String.format("orderId = '%d", orderId));
                PosOrderService.get().deleteById(String.valueOf(orderId));
            }
        }
//        PosOrderService.get().deleteBy(sqlWhere);

        //删除无用的收银订单明细
        ZLogger.d("删除无用的收银订单明细。");
        List<PosOrderItemEntity> orderItemEntities = PosOrderItemService.get().queryAllBy(null);
        if (orderItemEntities != null) {
            for (PosOrderItemEntity entity : orderItemEntities) {
                Long orderId = entity.getOrderId();
                PosOrderEntity orderEntity = PosOrderService.get().getEntityById(String.valueOf(orderId));
                if (orderEntity == null) {
                    PosOrderItemService.get().deleteById(String.valueOf(entity.getId()));
                    PosOrderPayService.get().deleteBy(String.format("orderId = '%d", orderId));
                }
            }
        }
        //删除无用的收银订单支付记录
        ZLogger.d("删除无用的收银订单支付记录。");
        List<PosOrderPayEntity> orderPayEntities = PosOrderPayService.get().queryAllBy(null);
        if (orderItemEntities != null) {
            for (PosOrderPayEntity entity : orderPayEntities) {
                Long orderId = entity.getOrderId();
                PosOrderEntity orderEntity = PosOrderService.get().getEntityById(String.valueOf(orderId));
                if (orderEntity == null) {
                    PosOrderItemService.get().deleteBy(String.format("orderId = '%d", orderId));
                    PosOrderPayService.get().deleteById(String.valueOf(entity.getId()));
                }
            }
        }
    }
    
    /**
     * 购物车数字
     */
    public static TextDrawable createFabDrawable(int number) {
        if (number > 99) {
            //最多显示两位
            number = 99;
        } else if (number < 0) {
            number = 0;
        }

        return TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .toUpperCase()
                .width(DensityUtil.dip2px(CashierApp.getAppContext(), 60))  // width in px
                .height(DensityUtil.dip2px(CashierApp.getAppContext(), 60)) // height in px
                .fontSize(DensityUtil.sp2px(CashierApp.getAppContext(), 50))/* size in px */
                .endConfig()
                .buildRect(String.valueOf(number), Color.TRANSPARENT);
    }

    /**
     * 获取指定长度的设备终端编号,左对齐，不足右补空格(0)
     */
    public static String getTerminalId(int length) {
        String terminalId = SharedPrefesManagerFactory.getTerminalId();

        StringBuilder sb = new StringBuilder();
        sb.append(terminalId);
        int len = length - terminalId.length();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                sb.append("0");
            }
        }

        return sb.toString();
    }

    /**
     * 获取指定长度的操作员编号,左对齐，不足右补空格(0)
     */
    public static String getOperateId(int length) {
        Long humanId = MfhLoginService.get().getHumanId();
        String operateId = humanId != null ? String.valueOf(humanId) : "";

        StringBuilder sb = new StringBuilder();
        sb.append(operateId);
        int len = length - operateId.length();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                sb.append("0");
            }
        }

        return sb.toString();
    }

}

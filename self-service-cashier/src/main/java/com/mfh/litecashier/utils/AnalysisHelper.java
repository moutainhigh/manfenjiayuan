/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.mfh.litecashier.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.bean.wrapper.AccWrapper;
import com.mfh.litecashier.bean.wrapper.AggWrapper;
import com.mfh.litecashier.bean.wrapper.AnalysisItemWrapper;
import com.mfh.litecashier.bean.wrapper.HandOverBill;
import com.bingshanguxue.cashier.database.entity.DailysettleEntity;
import com.bingshanguxue.cashier.database.service.DailysettleService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *  交接班/日结
 * Created by Nat.ZZN(bingshanguxue) on 15/9/9.
 */
public class AnalysisHelper {
    /**
     * 创建交接班订单<br>
     * 使用上一次交接班时间作为本次交接班统计的开始时间<br>
     * */
    public static HandOverBill createHandOverBill(){
        //上一次交接班时间
        Date lastHandoverDate = SharedPreferencesHelper.getLastHandoverDateTime();
        int lastHandoverShiftId = SharedPreferencesHelper.getLastHandoverShiftId();
        ZLogger.d(String.format("创建交接班订单--last shiftId：%d, datetime：%s",
                lastHandoverShiftId, TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(lastHandoverDate)));

        //当前交接班时间
        Date currentHandoverDate = new Date();

        HandOverBill handOverBill = new HandOverBill();
        handOverBill.setOfficeName(MfhLoginService.get().getCurOfficeName());
        handOverBill.setHumanName(MfhLoginService.get().getHumanName());
        handOverBill.setStartDate(lastHandoverDate);//TODO,使用登录时间
        handOverBill.setEndDate(currentHandoverDate);
        handOverBill.setAggItems(null);
        handOverBill.setAccItems(null);

        //班次
        if (TimeUtil.isSameDay(lastHandoverDate, currentHandoverDate)){
            ZLogger.d("上一次交班时间和当前时间是同一天，班次编号 ＋1");
            handOverBill.setShiftId(lastHandoverShiftId + 1);
        }
        else{
            ZLogger.d("上一次交班时间和当前时间不是同一天，需要重置班次编号");
            handOverBill.setShiftId(1);
        }

        return handOverBill;
    }

    /**
     * 判断是否已经（确认）日结
     * @param dailySettleDatetime 日结日期
     * */
    public static boolean validateHaveDateEnd(String dailySettleDatetime){
        DailysettleEntity dailysettleEntity = createDailysettle(dailySettleDatetime);
        if (dailysettleEntity == null){
            ZLogger.d(String.format("创建日结单失败：%s", dailySettleDatetime));
            return false;
        }
        if (dailysettleEntity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_YES){
            ZLogger.d(String.format("日结单已经确认：%s", dailySettleDatetime));
            return true;
        }
        return false;
    }
    public static boolean validateHaveDateEnd(Date dailySettleDate){
        DailysettleEntity dailysettleEntity = createDailysettle(dailySettleDate);
        if (dailysettleEntity == null){
            ZLogger.df(String.format("创建日结单失败：%s",
                    TimeCursor.FORMAT_YYYYMMDD.format(dailySettleDate)));
            return false;
        }
        if (dailysettleEntity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_YES){
            ZLogger.df(String.format("日结单已经确认：%s",
                    TimeCursor.FORMAT_YYYYMMDD.format(dailySettleDate)));
            return true;
        }
        return false;
    }

    /**
     * 创建日结单
     * @param dailySettleDatetime 日结日期
     * */
    public static DailysettleEntity createDailysettle(String dailySettleDatetime){
        //判断日结日期
        Date dailySettleDate = new Date();//日结日期
        if (!StringUtils.isEmpty(dailySettleDatetime)){
            try {
                dailySettleDate = TimeCursor.FORMAT_YYYYMMDDHHMMSS.parse(dailySettleDatetime);
            } catch (ParseException e) {
                ZLogger.e(e.toString());
            }
        }

        return createDailysettle(dailySettleDate);
    }

    /**
     * 创建日结单
     * @param dailySettleDate 日结日期
     * */
    public static DailysettleEntity createDailysettle(Date dailySettleDate){
        //查找日结订单
        String barcode = MUtils.genDateBarcode(BizType.DAILYSETTLE,
                dailySettleDate, "yyyyMMdd");
        List<DailysettleEntity> entityList  = DailysettleService.get()
                .queryAllDesc(String.format("officeId = '%d' and barCode = '%s'",
                        MfhLoginService.get().getCurOfficeId(), barcode), null);

        DailysettleEntity dailysettleEntity;
        if (entityList != null && entityList.size() > 0){
            dailysettleEntity = entityList.get(0);
            if (dailysettleEntity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_NO){
                dailysettleEntity.setDailysettleDate(dailySettleDate);
                dailysettleEntity.setUpdatedDate(new Date());
            }
        }
        else{
            dailysettleEntity = new DailysettleEntity();
            dailysettleEntity.setBarCode(barcode);
            dailysettleEntity.setCreatedDate(new Date());
            dailysettleEntity.setUpdatedDate(new Date());
            dailysettleEntity.setDailysettleDate(dailySettleDate);//日结日期
            dailysettleEntity.setOfficeId(MfhLoginService.get().getCurOfficeId());
            dailysettleEntity.setOfficeName(MfhLoginService.get().getCurOfficeName());
            dailysettleEntity.setHumanName(MfhLoginService.get().getHumanName());
        }

        DailysettleService.get().saveOrUpdate(dailysettleEntity);
        ZLogger.df(String.format("新建or更新日结单：%s\n%s",
                barcode, JSON.toJSONString(dailysettleEntity)));

        return dailysettleEntity;
    }

    /**
     * 删除订单,同时删除对应订单的商品明细和支付记录
     *
     * @param orderEntity 订单
     */
    public static void deleteDailysettle(DailysettleEntity orderEntity){
        if (orderEntity == null){
            return;
        }
        DailysettleService.get().deleteById(String.valueOf(orderEntity.getId()));
        //删除支付记录
        PosOrderPayService.get().deleteBy(String.format("orderId = '%d'",
                orderEntity.getId()));
    }

    /**
     * 清除旧数据
     *
     * @param saveDate 保存的天数
     */
    public static void deleteOldDailysettle(int saveDate) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0 - saveDate);//
        String expireCursor = TimeCursor.InnerFormat.format(calendar.getTime());
        ZLogger.d(String.format("日结过期时间(%s)保留最近30天数据。", expireCursor));

        List<DailysettleEntity> entityList = DailysettleService.get()
                .queryAllBy(String.format("updatedDate < '%s'", expireCursor));
        if (entityList != null && entityList.size() > 0) {
            for (DailysettleEntity entity : entityList) {
                deleteDailysettle(entity);
            }
            ZLogger.d(String.format("清除过期结数据(%s)。", expireCursor));

        } else {
            ZLogger.d(String.format("暂无过期结数据需要清除(%s)。", expireCursor));
        }
    }

    private static AnalysisItemWrapper translateAggItem(AggItem aggItem){
        AnalysisItemWrapper itemWrapper = new AnalysisItemWrapper();
        if (aggItem != null){
            itemWrapper.setCaption(String.format("%s/%s",
                    aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption()));
            itemWrapper.setTurnover(aggItem.getTurnover());
            itemWrapper.setOrderNum(aggItem.getOrderNum());
            itemWrapper.setGrossProfit(aggItem.getGrossProfit());
            itemWrapper.setIsShowIndex(true);
        }
        return itemWrapper;
    }

    private static AnalysisItemWrapper genDefaultAnalysisItemWrapper(String caption){
        AnalysisItemWrapper itemWrapper = new AnalysisItemWrapper();
        itemWrapper.setCaption(caption);
        itemWrapper.setIsShowIndex(true);
        return itemWrapper;
    }

    /**
     * 获取经营分析数据
     * */
    public static List<AnalysisItemWrapper> getAggItemsWrapper(DailysettleEntity dailysettleEntity){
        if (dailysettleEntity == null){
            return null;
        }

        AggWrapper aggWrapper = JSONObject.toJavaObject(JSON.parseObject(dailysettleEntity.getAggData()), AggWrapper.class);

        return getAggAnalysisList(aggWrapper);

    }

    public static List<AnalysisItemWrapper> getAggAnalysisList(AggWrapper aggWrapper){
        if (aggWrapper == null){
            aggWrapper = new AggWrapper();
        }

        List<AnalysisItemWrapper> items = new ArrayList<>();

        List<AggItem> posItems = aggWrapper.getPosItems();
        if (posItems != null && posItems.size() > 0){
            for (AggItem aggItem : posItems){
                items.add(translateAggItem(aggItem));
            }
        }
        else{
            items.add(genDefaultAnalysisItemWrapper("社区超市"));
        }
        List<AggItem> scItems = aggWrapper.getScItems();
        if (scItems != null && scItems.size() > 0){
            for (AggItem aggItem : scItems){
                items.add(translateAggItem(aggItem));
            }
        }
        else{
            items.add(genDefaultAnalysisItemWrapper("线上订单"));
        }
        List<AggItem> laundryItems = aggWrapper.getLaundryItems();
        if (laundryItems != null && laundryItems.size() > 0){
            for (AggItem aggItem : laundryItems){
                items.add(translateAggItem(aggItem));
            }
        }
        else{
            items.add(genDefaultAnalysisItemWrapper("衣物洗护"));
        }
        List<AggItem> pijuItems = aggWrapper.getPijuItems();
        if (pijuItems != null && pijuItems.size() > 0){
            for (AggItem aggItem : pijuItems){
                items.add(translateAggItem(aggItem));
            }
        }
        else{
            items.add(genDefaultAnalysisItemWrapper("皮具护理"));
        }

        List<AggItem> stockItems = aggWrapper.getStockItems();
        if (stockItems != null && stockItems.size() > 0){
            for (AggItem aggItem : stockItems){
                items.add(translateAggItem(aggItem));
            }
        }
        else{
            items.add(genDefaultAnalysisItemWrapper("快递代收"));
        }
        List<AggItem> sendItems = aggWrapper.getSendItems();
        if (sendItems != null && sendItems.size() > 0){
            for (AggItem aggItem : sendItems){
                items.add(translateAggItem(aggItem));
            }
        }
        else{
            items.add(genDefaultAnalysisItemWrapper("快递代揽"));
        }
        List<AggItem> rechargeItems = aggWrapper.getRechargeItems();
        if (rechargeItems != null && rechargeItems.size() > 0){
            for (AggItem aggItem : rechargeItems){
                items.add(translateAggItem(aggItem));
            }
        }
        else{
            items.add(genDefaultAnalysisItemWrapper("转账充值"));
        }

        return items;
    }

    public static AnalysisItemWrapper accItem2AnalysisItemWrapper(AccItem accItem){
        AnalysisItemWrapper item = new AnalysisItemWrapper();
        item.setCaption(accItem.getPayTypeCaption());
        item.setTurnover(accItem.getAmount());
        item.setOrderNum(accItem.getOrderNum());
        item.setIsShowIndex(true);
        return item;
    }
    public static List<AnalysisItemWrapper> getAccAnalysisList(AccWrapper accWrapper){
        if (accWrapper == null){
            return null;
        }

        List<AnalysisItemWrapper> items = new ArrayList<>();

        items.add(accItem2AnalysisItemWrapper(accWrapper.getCashItem()));
        items.add(accItem2AnalysisItemWrapper(accWrapper.getAlipayItem()));
        items.add(accItem2AnalysisItemWrapper(accWrapper.getWxItem()));
        items.add(accItem2AnalysisItemWrapper(accWrapper.getAccountItem()));
        items.add(accItem2AnalysisItemWrapper(accWrapper.getBankItem()));
        items.add(accItem2AnalysisItemWrapper(accWrapper.getRuleItem()));
        return items;
    }

    /**
     * 获取流水分析数据
     * */
    public static List<AnalysisItemWrapper> getAccItemsWrapper(DailysettleEntity dailysettleEntity){
        if (dailysettleEntity == null){
            return null;
        }

        AccWrapper accWrapper = JSON.toJavaObject(JSON.parseObject(dailysettleEntity.getAccData()),
                AccWrapper.class);

        return getAccAnalysisList(accWrapper);
    }

    public static AccItem constructorAccItem(Integer payType, String payTypeCaption) {
        AccItem accItem = new AccItem();
        accItem.setPayType(payType);
        accItem.setPayTypeCaption(payTypeCaption);
        return accItem;
    }


}

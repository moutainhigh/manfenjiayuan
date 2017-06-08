/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.mfh.litecashier.utils;


import com.alibaba.fastjson.JSON;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.litecashier.bean.wrapper.AnalysisItemWrapper;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        Date lastHandoverDate = SharedPreferencesUltimate.getLastHandoverDateTime();
        int lastHandoverShiftId = SharedPreferencesUltimate.getLastHandoverShiftId();
        ZLogger.d(String.format(Locale.getDefault(),
                "创建交接班订单--last shiftId：%d, datetime：%s",
                lastHandoverShiftId,
                TimeUtil.format(lastHandoverDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS)));

        //当前交接班时间
        Date rightNow = TimeUtil.getCurrentDate();

        HandOverBill handOverBill = new HandOverBill();
        handOverBill.setOfficeName(MfhLoginService.get().getCurOfficeName());
        handOverBill.setHumanName(MfhLoginService.get().getHumanName());
        handOverBill.setStartDate(lastHandoverDate);//TODO,使用登录时间
        handOverBill.setEndDate(rightNow);
        handOverBill.setAggItems(null);
        handOverBill.setAccItems(null);

        //班次
        if (TimeUtil.isSameDay(lastHandoverDate, rightNow)){
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
     * 创建日结单
     * @param dailySettleDatetime 日结日期
     * */
    public static DailysettleInfo createDailysettle(String dailySettleDatetime){
        //判断日结日期
        Date dailySettleDate = TimeUtil.getCurrentDate();//日结日期
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
    public static DailysettleInfo createDailysettle(Date dailySettleDate){
        DailysettleInfo dailysettleInfo = new DailysettleInfo();

        dailysettleInfo.setCreatedDate(dailySettleDate);//日结日期
        dailysettleInfo.setUpdatedDate(TimeUtil.getCurrentDate());
        dailysettleInfo.setOfficeId(MfhLoginService.get().getCurOfficeId());
        dailysettleInfo.setOfficeName(MfhLoginService.get().getCurOfficeName());
        dailysettleInfo.setHumanName(MfhLoginService.get().getHumanName());

        ZLogger.d(String.format("新建or更新日结单:\n%s",
                JSON.toJSONString(dailysettleInfo)));

        return dailysettleInfo;
    }

    public static List<AnalysisItemWrapper> wrapperAggItems(List<AggItem> aggItems){
        List<AnalysisItemWrapper> items = new ArrayList<>();

        if (aggItems != null && aggItems.size() > 0){
            for (AggItem aggItem : aggItems){
                AnalysisItemWrapper itemWrapper = new AnalysisItemWrapper();
                itemWrapper.setCaption(String.format("%s/%s",
                        aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption()));
                itemWrapper.setTurnover(aggItem.getTurnover());
                itemWrapper.setOrderNum(aggItem.getOrderNum());
                itemWrapper.setOrigionAmount(aggItem.getOrigionAmount());
                itemWrapper.setGrossProfit(aggItem.getGrossProfit());
                itemWrapper.setSalesBalance(aggItem.getSalesBalance());
                itemWrapper.setIsShowIndex(true);

                items.add(itemWrapper);
            }
        }

        return items;
    }

    public static List<AnalysisItemWrapper> wrapperAccItems( List<AccItem> accItems){
        List<AnalysisItemWrapper> items = new ArrayList<>();

        if (accItems != null && accItems.size() > 0){
            for (AccItem accItem : accItems){
                AnalysisItemWrapper item = new AnalysisItemWrapper();
                item.setCaption(accItem.getPayTypeCaption());
                item.setTurnover(accItem.getAmount());
                item.setOrderNum(accItem.getOrderNum());
                item.setIsShowIndex(true);

                items.add(item);
            }
        }
        return items;
    }

    /**
     * 获取经营分析数据
     * */
    public static List<AnalysisItemWrapper> getAggItemsWrapper(DailysettleInfo dailysettleInfo){
        if (dailysettleInfo == null){
            return null;
        }

        return wrapperAggItems(dailysettleInfo.getAggItems());
    }

    /**
     * 获取流水分析数据
     * */
    public static List<AnalysisItemWrapper> getAccItemsWrapper(DailysettleInfo dailysettleInfo){
        if (dailysettleInfo == null){
            return null;
        }

        return wrapperAccItems(dailysettleInfo.getAccItems());
    }

    /**
     * 获取流水分析数据
     * */
    public static List<AnalysisItemWrapper> getAccItemsWrapper2(DailysettleInfo dailysettleInfo){
        if (dailysettleInfo == null){
            return null;
        }

        return wrapperAccItems(dailysettleInfo.getAccItems2());
    }

}

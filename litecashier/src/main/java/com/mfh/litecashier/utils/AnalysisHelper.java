/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.mfh.litecashier.utils;


import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.bean.wrapper.AccWrapper;
import com.mfh.litecashier.bean.wrapper.AggWrapper;
import com.mfh.litecashier.bean.wrapper.AnalysisItemWrapper;
import com.mfh.litecashier.bean.wrapper.HandOverBill;
import com.mfh.litecashier.database.entity.DailysettleEntity;
import com.mfh.litecashier.database.logic.DailysettleService;
import com.mfh.litecashier.database.logic.PosOrderPayService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *  交接班
 * Created by Nat.ZZN(bingshanguxue) on 15/9/9.
 */
public class AnalysisHelper {

    /**
     * 检查交接班信息
     * */
    public static void validateHandoverInfo(){
        //上一次交接班时间
        String lastHandoverDate = SharedPreferencesHelper.getLastHandoverDateTimeStr();
        int lastHandoverShiftId = SharedPreferencesHelper.getLastHandoverShiftId();
        ZLogger.d(String.format("Initialize--lastHandover：shiftId %d, datetime %s", lastHandoverShiftId, lastHandoverDate));
    }
    /**
     * 创建交接班订单<br>
     * 使用上一次交接班时间作为本次交接班统计的开始时间<br>
     * */
    public static HandOverBill createHandOverBill(){
        //上一次交接班时间
        Date lastHandoverDate = SharedPreferencesHelper.getLastHandoverDateTime();
        int lastHandoverShiftId = SharedPreferencesHelper.getLastHandoverShiftId();
        ZLogger.d(String.format("Create daily settle--last shiftId：%d, datetime：%s",
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
            ZLogger.d("Create handover--上一次交班时间和当前时间是同一天，班次编号 ＋1");
            handOverBill.setShiftId(lastHandoverShiftId + 1);
        }
        else{
            ZLogger.d("Create handover--上一次交班时间和当前时间不是同一天，需要重置班次编号");
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
            ZLogger.d(String.format("validateHaveDateEnd--创建日结单失败：%s", dailySettleDatetime));
            return false;
        }
        if (dailysettleEntity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_YES){
            ZLogger.d(String.format("validateHaveDateEnd--日结单已经确认：%s", dailySettleDatetime));
            return true;
        }
        return false;
    }
    public static boolean validateHaveDateEnd(Date dailySettleDate){
        DailysettleEntity dailysettleEntity = createDailysettle(dailySettleDate);
        if (dailysettleEntity == null){
            ZLogger.d(String.format("validateHaveDateEnd--创建日结单失败：%s",
                    TimeCursor.FORMAT_YYYYMMDD.format(dailySettleDate)));
            return false;
        }
        if (dailysettleEntity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_YES){
            ZLogger.d(String.format("validateHaveDateEnd--日结单已经确认：%s",
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
                        MfhLoginService.get().getCurOfficeId(), barcode), new PageInfo(1, 10));

        DailysettleEntity dailysettleEntity;
        if (entityList != null && entityList.size() > 0){
            ZLogger.d(String.format("createDailysettle--更新日结单：%s", barcode));
            dailysettleEntity = entityList.get(0);
            if (dailysettleEntity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_NO){
                dailysettleEntity.setDailysettleDate(dailySettleDate);
                dailysettleEntity.setUpdatedDate(new Date());
            }
        }
        else{
            ZLogger.d(String.format("createDailysettle--新建日结单：%s", barcode));
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

        return dailysettleEntity;
    }

    /**
     * 删除订单,同时删除对应订单的商品明细和支付记录
     *
     * @param orderEntity 订单
     * @return
     */
    public static void deleteDailysettle(DailysettleEntity orderEntity){
        if (orderEntity == null){
            return;
        }
        DailysettleService.get().deleteById(String.valueOf(orderEntity.getId()));
        //删除支付记录
        PosOrderPayService.get().deleteBy(String.format("orderBarCode = '%s'", orderEntity.getBarCode()));
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
        ZLogger.d(String.format("Initialize--日结过期时间(%s)保留最近30天数据。", expireCursor));

        List<DailysettleEntity> entityList = DailysettleService.get()
                .queryAllBy(String.format("updatedDate < '%s'", expireCursor));
        if (entityList != null && entityList.size() > 0) {
            for (DailysettleEntity entity : entityList) {
                deleteDailysettle(entity);
            }
            ZLogger.d(String.format("Initialize--清除过期结数据(%s)。", expireCursor));

        } else {
            ZLogger.d(String.format("Initialize--暂无过期结数据需要清除(%s)。", expireCursor));
        }
    }

    /**
     * 获取经营分析数据
     * */
    public static List<AnalysisItemWrapper> getAggItemsWrapper(DailysettleEntity dailysettleEntity){
        if (dailysettleEntity == null){
            return null;
        }

        List<AnalysisItemWrapper> items = new ArrayList<>();
        AnalysisItemWrapper item1 = new AnalysisItemWrapper();
        item1.setCaption("线下收银");
        item1.setTurnover(dailysettleEntity.getAggPosAmount());
        item1.setOrderNum(dailysettleEntity.getAggPosOrderNum());
        item1.setIsShowIndex(true);
        items.add(item1);

        AnalysisItemWrapper item2 = new AnalysisItemWrapper();
        item2.setCaption("线上订单");
        item2.setTurnover(dailysettleEntity.getAggScAmount());
        item2.setOrderNum(dailysettleEntity.getAggScOrderNum());
        item2.setIsShowIndex(true);
        items.add(item2);

        AnalysisItemWrapper item3 = new AnalysisItemWrapper();
        item3.setCaption("衣物洗护");
        item3.setTurnover(dailysettleEntity.getAggLaundryAmount());
        item3.setOrderNum(dailysettleEntity.getAggLaundryOrderNum());
        item3.setIsShowIndex(true);
        items.add(item3);

        AnalysisItemWrapper item4 = new AnalysisItemWrapper();
        item4.setCaption("皮具护理");
        item4.setTurnover(dailysettleEntity.getAggPijuAmount());
        item4.setOrderNum(dailysettleEntity.getAggPijuOrderNum());
        item4.setIsShowIndex(true);
        items.add(item4);

        AnalysisItemWrapper item5 = new AnalysisItemWrapper();
        item5.setCaption("快递代收");
        item5.setTurnover(dailysettleEntity.getAggCourierAmount());
        item5.setOrderNum(dailysettleEntity.getAggCourierOrderNum());
        item5.setIsShowIndex(true);
        items.add(item5);

        AnalysisItemWrapper item6 = new AnalysisItemWrapper();
        item6.setCaption("快递代揽");
        item6.setTurnover(dailysettleEntity.getAggExpressAmount());
        item6.setOrderNum(dailysettleEntity.getAggExpressOrderNum());
        item6.setIsShowIndex(true);
        items.add(item6);

        AnalysisItemWrapper item7 = new AnalysisItemWrapper();
        item7.setCaption("转账充值");
        item7.setTurnover(dailysettleEntity.getAggRechargeAmount());
        item7.setOrderNum(dailysettleEntity.getAggRechargeOrderNum());
        item7.setIsShowIndex(true);
        items.add(item7);

//        AnalysisItemWrapper item8 = new AnalysisItemWrapper();
//        item8.setCaption("");
//        item8.setTurnover(this.aggWrapper.getTotalAmount());
//        item8.setOrderNum(this.aggWrapper.getTotalQuantity());
//        item8.setIsShowIndex(false);
//        items.add(item8);

        return items;
    }

    public static List<AnalysisItemWrapper> getAggAnalysisList(AggWrapper aggWrapper){
        if (aggWrapper == null){
            return null;
        }

        List<AnalysisItemWrapper> items = new ArrayList<>();

        AnalysisItemWrapper item1 = new AnalysisItemWrapper();
        item1.setCaption("线下收银");
        item1.setTurnover(aggWrapper.getPosAmount());
        item1.setOrderNum(aggWrapper.getPosQuantity());
        item1.setIsShowIndex(true);
        items.add(item1);

        AnalysisItemWrapper item2 = new AnalysisItemWrapper();
        item2.setCaption("线上订单");
        item2.setTurnover(aggWrapper.getScAmount());
        item2.setOrderNum(aggWrapper.getScQuantity());
        item2.setIsShowIndex(true);
        items.add(item2);

        AnalysisItemWrapper item3 = new AnalysisItemWrapper();
        item3.setCaption("衣物洗护");
        item3.setTurnover(aggWrapper.getLaundryAmount());
        item3.setOrderNum(aggWrapper.getLaundryQuantity());
        item3.setIsShowIndex(true);
        items.add(item3);

        AnalysisItemWrapper item4 = new AnalysisItemWrapper();
        item4.setCaption("皮具护理");
        item4.setTurnover(aggWrapper.getPijuAmount());
        item4.setOrderNum(aggWrapper.getPijuQuantity());
        item4.setIsShowIndex(true);
        items.add(item4);

        AnalysisItemWrapper item5 = new AnalysisItemWrapper();
        item5.setCaption("快递代收");
        item5.setTurnover(aggWrapper.getCourierAmount());
        item5.setOrderNum(aggWrapper.getCourierQuantity());
        item5.setIsShowIndex(true);
        items.add(item5);

        AnalysisItemWrapper item6 = new AnalysisItemWrapper();
        item6.setCaption("快递代揽");
        item6.setTurnover(aggWrapper.getExpressAmount());
        item6.setOrderNum(aggWrapper.getExpressQuantity());
        item6.setIsShowIndex(true);
        items.add(item6);

        AnalysisItemWrapper item7 = new AnalysisItemWrapper();
        item7.setCaption("转账充值");
        item7.setTurnover(aggWrapper.getRechargeAmount());
        item7.setOrderNum(aggWrapper.getRechargeQuantity());
        item7.setIsShowIndex(true);
        items.add(item7);

//        AnalysisItemWrapper item8 = new AnalysisItemWrapper();
//        item8.setCaption("");
//        item8.setTurnover(aggWrapper.getTotalAmount());
//        item8.setOrderNum(aggWrapper.getTotalQuantity());
//        item8.setIsShowIndex(false);
//        items.add(item8);

        return items;
    }

    public static List<AnalysisItemWrapper> getAccAnalysisList(AccWrapper accWrapper){
        if (accWrapper == null){
            return null;
        }

        List<AnalysisItemWrapper> items = new ArrayList<>();

        AnalysisItemWrapper item1 = new AnalysisItemWrapper();
        item1.setCaption("现金收取");
        item1.setTurnover(accWrapper.getCashAmount());
        item1.setOrderNum(accWrapper.getCashQuantity());
        item1.setIsShowIndex(true);
        items.add(item1);

        AnalysisItemWrapper item2 = new AnalysisItemWrapper();
        item2.setCaption("支付宝支付");
        item2.setTurnover(accWrapper.getAlipayAmount());
        item2.setOrderNum(accWrapper.getAlipayQuantity());
        item2.setIsShowIndex(true);
        items.add(item2);

        AnalysisItemWrapper item3 = new AnalysisItemWrapper();
        item3.setCaption("微信支付");
        item3.setTurnover(accWrapper.getWxAmount());
        item3.setOrderNum(accWrapper.getWxQuantity());
        item3.setIsShowIndex(true);
        items.add(item3);

        AnalysisItemWrapper item4 = new AnalysisItemWrapper();
        item4.setCaption("会员账号支付");
        item4.setTurnover(accWrapper.getMemberAccountAmount());
        item4.setOrderNum(accWrapper.getMemberAccountQuantity());
        item4.setIsShowIndex(true);
        items.add(item4);

//        AnalysisItemWrapper item5 = new AnalysisItemWrapper();
//        item5.setCaption("线上订单支付");
//        item5.setTurnover(this.accWrapper.getScAmount());
//        item5.setOrderNum(this.accWrapper.getScQuantity());
//        item5.setIsShowIndex(true);
//        items.add(item5);

        AnalysisItemWrapper item6 = new AnalysisItemWrapper();
        item6.setCaption("银行卡支付");
        item6.setTurnover(accWrapper.getBankcardAmount());
        item6.setOrderNum(accWrapper.getBankcardQuantity());
        item6.setIsShowIndex(true);
        items.add(item6);


//        AnalysisItemWrapper item7 = new AnalysisItemWrapper();
//        item7.setCaption("");
//        item7.setTurnover(this.aggShiftWrapper.getTotalAmount());
//        item7.setOrderNum(this.aggShiftWrapper.getTotalQuantity());
//        item7.setIsShowIndex(false);
//        items.add(item7);

        return items;
    }

    /**
     * 获取流水分析数据
     * */
    public static List<AnalysisItemWrapper> getAccItemsWrapper(DailysettleEntity dailysettleEntity){
        if (dailysettleEntity == null){
            return null;
        }
        List<AnalysisItemWrapper> items = new ArrayList<>();

        AnalysisItemWrapper item1 = new AnalysisItemWrapper();
        item1.setCaption("现金收取");
        item1.setTurnover(dailysettleEntity.getAccCashAmount());
        item1.setOrderNum(dailysettleEntity.getAccCashOrderNum());
        item1.setIsShowIndex(true);
        items.add(item1);

        AnalysisItemWrapper item2 = new AnalysisItemWrapper();
        item2.setCaption("支付宝支付");
        item2.setTurnover(dailysettleEntity.getAccAlipayAmount());
        item2.setOrderNum(dailysettleEntity.getAccAlipayOrderNum());
        item2.setIsShowIndex(true);
        items.add(item2);

        AnalysisItemWrapper item3 = new AnalysisItemWrapper();
        item3.setCaption("微信支付");
        item3.setTurnover(dailysettleEntity.getAccWxAmount());
        item3.setOrderNum(dailysettleEntity.getAccWxOrderNum());
        item3.setIsShowIndex(true);
        items.add(item3);

        AnalysisItemWrapper item4 = new AnalysisItemWrapper();
        item4.setCaption("会员账号支付");
        item4.setTurnover(dailysettleEntity.getAccMemberAccount());
        item4.setOrderNum(dailysettleEntity.getAccMemberOrderNum());
        item4.setIsShowIndex(true);
        items.add(item4);

//        AnalysisItemWrapper item5 = new AnalysisItemWrapper();
//        item5.setCaption("线上订单支付");
//        item5.setTurnover(dailysettleEntity.getAccScAmount());
//        item5.setOrderNum(dailysettleEntity.getAccScOrderNum());
//        item5.setIsShowIndex(true);
//        items.add(item5);

        AnalysisItemWrapper item6 = new AnalysisItemWrapper();
        item6.setCaption("银行卡支付");
        item6.setTurnover(dailysettleEntity.getAccBankcardAmount());
        item6.setOrderNum(dailysettleEntity.getAccBankcardOrderNum());
        item6.setIsShowIndex(true);
        items.add(item6);


//        AnalysisItemWrapper item7 = new AnalysisItemWrapper();
//        item7.setBizTypeCaption("");
//        item7.setTurnover(this.aggShiftWrapper.getTotalAmount());
//        item7.setOrderNum(this.aggShiftWrapper.getTotalQuantity());
//        item7.setIsShowIndex(false);
//        items.add(item7);

        return items;
    }


}

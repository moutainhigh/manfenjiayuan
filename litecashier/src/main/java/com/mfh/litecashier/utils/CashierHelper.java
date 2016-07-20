package com.mfh.litecashier.utils;

import android.graphics.Color;
import android.graphics.Typeface;

import com.alibaba.fastjson.JSONArray;
import com.amulyakhare.textdrawable.TextDrawable;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.bean.PosCategory;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.litecashier.database.entity.PosOrderPayEntity;
import com.mfh.litecashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.logic.PosOrderItemService;
import com.mfh.litecashier.database.logic.PosOrderPayService;
import com.mfh.litecashier.database.logic.PosOrderService;
import com.mfh.litecashier.database.logic.PosProductService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 收银帮助类
 * Created by Nat.ZZN(bingshanguxue) on 15/9/9.
 */
public class CashierHelper {
    /**
     * 清空订单明细
     */
    public static void clearOrderItems(String orderBarCode) {
        if (StringUtils.isEmpty(orderBarCode)) {
            return;
        }
        PosOrderItemService.get().deleteBy(String.format("orderBarCode = '%s'", orderBarCode));
    }

    /**
     * 结算收银订单
     *
     * @param orderBarcode           订单编号
     * @param bizType                业务类型
     * @param customerMembershipInfo 顾客会员信息
     * @param items                  明细
     */
    public static CashierOrderInfo settleCashierOrder(String orderBarcode, Integer bizType,
                                                      Human customerMembershipInfo,
                                                      List<PosOrderItemEntity> items) {
        try {
            //检查参数
            if (StringUtils.isEmpty(orderBarcode)) {
                ZLogger.d("settleCashierOrder－－orderBarcode不能为空。");
                return null;
            }
            if (items == null || items.size() < 1) {
                ZLogger.d("settleCashierOrder－－items不能为空");
                return null;
            }

            ZLogger.d("settleCashierOrder－－" + orderBarcode);
            //查询订单
            PosOrderEntity orderEntity;
            List<PosOrderEntity> orderEntityList = PosOrderService.get()
                    .queryAllBy(String.format("barCode = '%s' and sellerId = '%d'",
                            orderBarcode, MfhLoginService.get().getSpid()));
            if (orderEntityList != null && orderEntityList.size() > 0) {
                orderEntity = orderEntityList.get(0);
            } else {
                ZLogger.d(String.format("settleCashierOrder－－创建新订单 %s", orderBarcode));
                orderEntity = new PosOrderEntity();
                orderEntity.setBarCode(orderBarcode);
                orderEntity.setCreatedDate(new Date());
                orderEntity.setUpdatedDate(new Date());
//        posOrderEntity.setHumanId(MfhLoginService.get().getCurrentGuId());
                orderEntity.setRemark("");
//        posOrderEntity.setCompanyId(MfhLoginService.get().getSpid());// 使用商品的ternantID,见下面

                orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
                orderEntity.setSellerId(MfhLoginService.get().getSpid());// 需要登录
                orderEntity.setCreatedBy(String.valueOf(MfhLoginService.get().getCurrentGuId()));
                orderEntity.setPosId(SharedPreferencesManager.getTerminalId());//设备编号

                orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);//订单状态
                orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_NO);
                orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_NONE);

                orderEntity.setPaidMoney(0D);//已支付金额
                orderEntity.setDiscountAmount(0D);//优惠金额
                orderEntity.setCharge(0D);//找零
                orderEntity.setCouponsIds("");
                orderEntity.setAdjPrice("");
            }

            //更新业务类型
            orderEntity.setBizType(bizType);

            //更新会员信息
            ZLogger.d("settleCashierOrder－－更新会员信息");
            if (customerMembershipInfo != null && !StringUtils.isEmpty(customerMembershipInfo.getGuid())) {
                orderEntity.setHumanId(Long.valueOf(customerMembershipInfo.getGuid()));
                orderEntity.setScore(0D);//会员积分
            }

            //更新订单明细
            if (orderEntity.getPaystatus() == PosOrderEntity.PAY_STATUS_NO){
//            if ((orderEntity.getStatus() & PosOrderEntity.ORDER_STATUS_STAY_PAY) == PosOrderEntity.ORDER_STATUS_STAY_PAY) {
                ZLogger.d("settleCashierOrder－－更新订单明细");
                PosOrderItemService.get().deleteBy(String.format("orderBarCode = '%s'", orderBarcode));
                for (PosOrderItemEntity entity : items) {
                    PosOrderItemService.get().addNewEntity(entity);

//                        orderEntity.setRetailAmount(orderEntity.getRetailAmount() + entity.getAmount());
//                        orderEntity.setActualAmount(orderEntity.getActualAmount() + entity.getFinalAmount());
                }
            }

            //保存订单
            PosOrderService.get().saveOrUpdate(orderEntity);

            //读取支付记录
            ZLogger.d("settleCashierOrder－－读取支付记录");
            Integer payType = WayType.NA;
            Double paidAmount = 0D;
            List<PosOrderPayEntity> payEntityList = PosOrderPayService.get()
                    .queryAllBy(String.format("orderBarCode = '%s' and paystatus = '%d'",
                            orderBarcode, PosOrderPayEntity.PAY_STATUS_FINISH));
            for (PosOrderPayEntity payEntity : payEntityList){
                payType = payType | payEntity.getPayType();
                paidAmount += payEntity.getAmount();
            }

            //生成结算信息
            ZLogger.d("settleCashierOrder－－生成结算信息开始");
            CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
            String orderid = String.format("%s_%d", SharedPreferencesManager.getTerminalId(), orderEntity.getId());
            String subject = String.format("POS订单支付，条码：%s", orderBarcode);
            cashierOrderInfo.initCashierSetle(orderBarcode, bizType, orderid, subject, "", null,
                    payType, paidAmount, items, orderEntity.getStatus());

            ZLogger.d("settleCashierOrder－－生成结算信息完成");
            return cashierOrderInfo;
        } catch (Exception e) {
            ZLogger.e(String.format("settleCashierOrder－－结算收银订单失败: %s", e.toString()));
            return null;
        }
    }

    /**
     * 挂起收银订单
     * @param orderBarcode           订单编号
     * @param items                  明细
     */
    public static boolean hangUpCashierOrder(String orderBarcode, Integer bizType,
                                             Human customerMembershipInfo,
                                             List<PosOrderItemEntity> items) {
        CashierOrderInfo cashierOrderInfo = CashierHelper.settleCashierOrder(orderBarcode,
                bizType, customerMembershipInfo, items);
        if (cashierOrderInfo == null) {
            ZLogger.d("hangUpCashierOrder－－cashierOrderInfo创建失败。");
            return false;
        }

        //更新订单状态：挂单
        return updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_HANGUP);
    }

    /**
     * 更新收银订单
     *
     * @param cashierOrderInfo 订单支付信息
     * @param status 订单状态
     */
    public static boolean updateCashierOrder(CashierOrderInfo cashierOrderInfo, int status) {
        PosOrderEntity orderEntity = findPosOrder(cashierOrderInfo.getOrderBarcode());
        if (orderEntity == null) {
            return false;
        }

        orderEntity.setUpdatedDate(new Date());
        orderEntity.setCompanyId(cashierOrderInfo.getCompanyId());
        orderEntity.setRetailAmount(cashierOrderInfo.getRetailAmount());
        orderEntity.setDiscountAmount(cashierOrderInfo.getDiscountAmount());//折扣价
        orderEntity.setCouponDiscountAmount(cashierOrderInfo.getCouponDiscountAmount());//折扣价
        orderEntity.setPaidAmount(cashierOrderInfo.getPaidAmount());//实际收入金额
        orderEntity.setPaidMoney(cashierOrderInfo.getPaidAmount());
        orderEntity.setBcount(cashierOrderInfo.getbCount());
//        orderEntity.setBizType(cashierOrderInfo.getBizType());
        orderEntity.setCharge(0D - cashierOrderInfo.getHandleAmount());//找零
        orderEntity.setCouponsIds(cashierOrderInfo.getCouponsIds());
        orderEntity.setRuleIds(cashierOrderInfo.getRuleIds());
        orderEntity.setPayType(cashierOrderInfo.getPayType());

        //TODO,会员卡
        try {
            Human human = cashierOrderInfo.getVipMember();
            if (human != null && !StringUtils.isEmpty(human.getGuid())) {
                orderEntity.setHumanId(Long.valueOf(human.getGuid()));
                orderEntity.setScore(0D);//会员积分
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

        //支付完成
        orderEntity.setStatus(status);
        if (status == PosOrderEntity.ORDER_STATUS_FINISH || orderEntity.getPaidMoney().compareTo(0D) > 0){
//            ZLogger.d("订单已经支付");
            orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_YES);
        }
        else {
            orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_NO);
//            ZLogger.d("订单未支付");
        }

        PosOrderService.get().saveOrUpdate(orderEntity);
        return true;
    }

    /**
     * 删除订单,同时删除对应订单的商品明细和支付记录
     *
     * @param orderEntity 订单
     * @return
     */
    public static void deleteCashierOrder(PosOrderEntity orderEntity){
        if (orderEntity == null){
            return;
        }
        PosOrderService.get().deleteById(String.valueOf(orderEntity.getId()));
        //删除订单明细
        PosOrderItemService.get().deleteBy(String.format("orderBarCode = '%s'", orderEntity.getBarCode()));
        //删除支付记录
        PosOrderPayService.get().deleteBy(String.format("orderBarCode = '%s'", orderEntity.getBarCode()));
    }

    /**
     * 查询订单
     *
     * @param orderBarCode 订单条码
     * @return PosOrderEntity
     */
    public static PosOrderEntity findPosOrder(String orderBarCode) {
        if (StringUtils.isEmpty(orderBarCode)) {
            return null;
        }

        List<PosOrderEntity> orderEntityList = PosOrderService.get()
                .queryAllBy(String.format("barCode = '%s' and sellerId = '%d'",
                        orderBarCode, MfhLoginService.get().getSpid()));
        if (orderEntityList == null || orderEntityList.size() < 1) {
            ZLogger.d(String.format("未找到订单: %s", orderBarCode));
            return null;
        }

//        ZLogger.d(String.format("找到 %d 条订单 ", orderEntityList.size()));
        return orderEntityList.get(0);
    }

    /**
     * 查询订单
     *
     * @return PosOrderEntity
     */
    public static List<PosOrderEntity> findHangupOrders() {
        return PosOrderService.get()
                .queryAllBy(String.format("status = '%d' and sellerId = '%d'",
                        PosOrderEntity.ORDER_STATUS_HANGUP, MfhLoginService.get().getSpid()));
    }



    /**
     * 清除旧数据
     *
     * @param saveDate 保存的天数
     */
    public static void clearOldPosOrder(int saveDate) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0 - saveDate);//
        String expireCursor = TimeCursor.InnerFormat.format(calendar.getTime());
        ZLogger.d(String.format("Initialize--订单过期时间(%s)保留最近30天数据。", expireCursor));

        String lastUpdateCursor = SharedPreferencesHelper.getPosOrderLastUpdate();
        ZLogger.d(String.format("Initialize--last posorder upload datetime(%s)。", lastUpdateCursor));

        if (!StringUtils.isEmpty(lastUpdateCursor)) {
            //得到指定模范的时间
            try {
                Date d1 = TimeCursor.InnerFormat.parse(lastUpdateCursor);
                Date d2 = TimeCursor.InnerFormat.parse(expireCursor);
//            Date d2 = new Date();
                if (d2.compareTo(d1) > 0) {
                    ZLogger.d("Initialize--订单过期时间大于上次更新时间，暂不清除。");
                    return;
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.e(e.toString());
            }
        }
        List<PosOrderEntity> entityList = PosOrderService.get()
                .queryAllBy(String.format("updatedDate < '%s'", expireCursor));
        if (entityList != null && entityList.size() > 0) {
            for (PosOrderEntity entity : entityList) {
                //清除订单明细
                clearOrderItems(entity.getBarCode());
                //删除订单
//                PosOrderService.get().deleteById(String.valueOf(entity.getBarCode()));
            }

            //清除订单
            PosOrderService.get().deleteBy(String.format("updatedDate < '%s'", expireCursor));
            ZLogger.d(String.format("Initialize--清除过期订单数据(%s)。", expireCursor));
        } else {
            ZLogger.d(String.format("Initialize--暂无过期订单数据需要清除(%s)。", expireCursor));
        }
    }

    /**
     * 查询商品
     *
     * @param barCode 条形码
     */
    public static PosProductEntity findProduct(String barCode) {
        if (StringUtils.isEmpty(barCode)) {
            return null;
        }

        List<PosProductEntity> entityList = PosProductService.get()
                .queryAllByDesc(String.format("barcode = '%s' and tenantId = '%d'",
                        barCode, MfhLoginService.get().getSpid()));
        if (entityList != null && entityList.size() > 0) {
            ZLogger.d(String.format("找到%d个商品:%s", entityList.size(), barCode));
            return entityList.get(0);
        } else {
            ZLogger.d("未找到商品:" + barCode);
        }

        return null;
    }

    /**
     * 读取前台类目缓存
     */
    public static List<PosCategory> readFrontCatetoryCache() {
        List<PosCategory> entityList = new ArrayList<>();
        //读取缓存，如果有则加载缓存数据，否则重新加载类目；应用每次启动都会加载类目
        String publicCateCache = ACacheHelper.getAsString(ACacheHelper.CK_PUBLIC_FRONT_CATEGORY);
        String customCateCache = ACacheHelper.getAsString(ACacheHelper.CK_CUSTOM_FRONT_CATEGORY);
        List<PosCategory> publicData = JSONArray.parseArray(publicCateCache, PosCategory.class);
        List<PosCategory> customData = JSONArray.parseArray(customCateCache, PosCategory.class);

        if (publicData != null) {
            entityList.addAll(publicData);
        }
        if (customData != null) {
            entityList.addAll(customData);
        }

        return entityList;
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
     * */
    public static String getTerminalId(int length){
        String terminalId =  SharedPreferencesManager.getTerminalId();

        StringBuilder sb = new StringBuilder();
        sb.append(terminalId);
        int len = length - terminalId.length();
        if (len > 0){
            for (int i = 0; i < len; i++){
                sb.append("0");
            }
        }

        return sb.toString();
    }

    /**
     * 获取指定长度的操作员编号,左对齐，不足右补空格(0)
     * */
    public static String getOperateId(int length){
        Long guid = MfhLoginService.get().getCurrentGuId();
        String operateId =  guid != null ? String.valueOf(guid) : "";

        StringBuilder sb = new StringBuilder();
        sb.append(operateId);
        int len = length - operateId.length();
        if (len > 0){
            for (int i = 0; i < len; i++){
                sb.append("0");
            }
        }

        return sb.toString();
    }

}

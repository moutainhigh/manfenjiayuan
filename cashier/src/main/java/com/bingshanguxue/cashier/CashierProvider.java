package com.bingshanguxue.cashier;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.HangupOrder;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesUltimate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 收银信息
 * Created by bingshanguxue on 7/2/16.
 */
public class CashierProvider {
    public static final String PREF_NAME_CASHIER = "pref_cashier_base";
    private static final String PK_LAST_CASHIER_DATETIME = "last_cashier_datetime";  //上一次收银时间
    private static final String PK_LAST_CASHIER_FLOWNUMBER = "last_cashier_flownumber";   //上一次收银流水编号

    /**
     * 获取未支付的金额
     */
    public static Double getUnpayAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        return cashierOrderInfo.getFinalAmount() - cashierOrderInfo.getPaidAmount();
    }

    /**
     * 计算当前应该支付金额, >0,表示应收金额; <0,表示找零金额
     * 公式：应收金额＝（商品总金额－价格调整）－已付金额 －促销优惠 －卡券优惠
     */
    public static Double getHandleAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        Double amount = MathCompact.sub(cashierOrderInfo.getFinalAmount(), cashierOrderInfo.getPaidAmount());
        PayAmount payAmount = cashierOrderInfo.getPayAmount();
        if (payAmount != null) {
            amount -= payAmount.getRuleAmount();
//            amount -= payAmount.getPackRuleAmount();
            amount -= payAmount.getCoupAmount();
        }

        //实际场景中应付金额不会小于1分钱，所以这里要保留两位小数
        //2016-07-04，判断需要放在循环里，因为折扣券是对拆分后的子订单生效，不是整个大订单
        //2016-08－15，现金支付完成后，重新计算应付金额，负数被忽略导致支付窗口没有关闭。
        if (amount < 0.01) {
            amount = 0D;
        }

        //精确到分
        return Double.valueOf(String.format("%.2f", amount));
    }

    /**
     * 更新并返回最新的流水编号
     */
    public static Long nextFlowId() {
        Date rightNow = TimeUtil.getCurrentDate();
        Long flowId;

        Date saveDate;
        String saveDateStr = SharedPrefesUltimate
                .getString(PREF_NAME_CASHIER, PK_LAST_CASHIER_DATETIME);
        if (StringUtils.isEmpty(saveDateStr)) {
            saveDate = rightNow;
            flowId = 1L;
        } else {
            saveDate = TimeUtil.parse(saveDateStr, TimeUtil.FORMAT_YYYYMMDDHHMMSS);
            if (TimeUtil.isSameDay(rightNow, saveDate)) {
                ZLogger.d("上一次收银时间和当前时间是同一天，流水编号 ＋1");
                Long saveId = SharedPrefesUltimate
                        .getLong(PREF_NAME_CASHIER, PK_LAST_CASHIER_FLOWNUMBER, 1L);

                flowId = saveId + 1;
            } else {
                ZLogger.d("上一次收银时间和当前时间不是同一天，需要重置流水编号");
                flowId = 1L;
            }
        }

        SharedPrefesUltimate
                .set(PREF_NAME_CASHIER, PK_LAST_CASHIER_DATETIME
                        , TimeUtil.format(saveDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        SharedPrefesUltimate
                .set(PREF_NAME_CASHIER, PK_LAST_CASHIER_FLOWNUMBER, flowId);

        return flowId;
    }

    /**
     * 查询收银订单
     *
     * @param orderBarcode POS唯一订单号
     */
    public static PosOrderEntity fetchOrderEntity(String orderBarcode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and barCode = '%s'",
                MfhLoginService.get().getSpid(), BizType.POS, orderBarcode);
        List<PosOrderEntity> entities = PosOrderService.get().queryAllBy(sqlOrder);
        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        }
        return null;
    }

    /**
     * 获取订单明细
     */
    public static List<PosOrderItemEntity> fetchOrderItems(Long orderId) {
        return PosOrderItemService.get()
                .queryAllBy(String.format("orderId = '%d'", orderId));
    }

    /**
     * 查询挂单状态的收银订单
     */
    public static List<HangupOrder> fetchHangupOrders() {
        List<HangupOrder> hangupOrders = new ArrayList<>();

        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and status = '%d'",
                MfhLoginService.get().getSpid(), BizType.POS, PosOrderEntity.ORDER_STATUS_HANGUP);
        List<PosOrderEntity> orderEntities = PosOrderService.get().queryAllBy(sqlOrder);

        if (orderEntities != null && orderEntities.size() > 0) {
            for (PosOrderEntity orderEntity : orderEntities) {
                HangupOrder hangupOrder = new HangupOrder();
                hangupOrder.setOrderTradeNo(orderEntity.getBarCode());
                hangupOrder.setFinalAmount(orderEntity.getFinalAmount());
                hangupOrder.setUpdateDate(orderEntity.getUpdatedDate());

                hangupOrders.add(hangupOrder);
            }
        }

        return hangupOrders;
    }

    /**
     * 收银订单结算信息
     *
     * @param orderBarCode 订单流水号
     * @param vipMember    会员
     */
    public static CashierOrderInfo createCashierOrderInfo(String orderBarCode,
                                                          Human vipMember) {
        PosOrderEntity orderEntity = fetchOrderEntity(orderBarCode);
        if (orderEntity == null) {
            return null;
        }

        return createCashierOrderInfo(orderEntity, vipMember);
    }

    public static CashierOrderInfo createCashierOrderInfo(PosOrderEntity orderEntity,
                                                          Human vipMember) {
        Long orderId = orderEntity.getId();
        if (orderId == null) {
            ZLogger.e("订单编号无效");
            return null;
        }

        ZLogger.d("创建订单支付信息：" +JSONObject.toJSONString(orderEntity));
        Double bCount = 0D;
        Double retailAmount = 0D, finalAmount = 0D, vipAmount = 0D;
        StringBuilder sbBody = new StringBuilder();
        JSONArray productsInfo = new JSONArray();

        //订单明细
        List<PosOrderItemEntity> orderItemEntityList = fetchOrderItems(orderId);
        if (orderItemEntityList != null && orderItemEntityList.size() > 0) {
            for (PosOrderItemEntity itemEntity : orderItemEntityList) {
                bCount += itemEntity.getBcount();
                retailAmount += itemEntity.getAmount();
                finalAmount += itemEntity.getFinalAmount();
                vipAmount += itemEntity.getVipAmount();

                if (sbBody.length() > 0) {
                    sbBody.append(",");
                }
                sbBody.append(itemEntity.getSkuName());

                JSONObject item = new JSONObject();
                item.put("goodsId", itemEntity.getGoodsId());
                item.put("skuId", itemEntity.getProSkuId());
                item.put("bcount", itemEntity.getBcount());
                item.put("price", itemEntity.getCostPrice());
                item.put("factAmount", itemEntity.getFinalAmount());
                item.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,
                productsInfo.add(item);
            }
        }

        Double adjustAmount = retailAmount - finalAmount;
        Double discountRate;
        if (retailAmount == 0D) {
            discountRate = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
        } else {
            discountRate = finalAmount / retailAmount;
        }

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.setBizType(BizType.POS);
        cashierOrderInfo.setPosTradeNo(orderEntity.getBarCode());
        cashierOrderInfo.setVipMember(vipMember);
        cashierOrderInfo.setSubject(String.format("订单流水号：%s", orderEntity.getBarCode()));
        cashierOrderInfo.setOrderId(orderEntity.getId());
        cashierOrderInfo.setbCount(bCount);
        cashierOrderInfo.setRetailAmount(retailAmount);
        cashierOrderInfo.setFinalAmount(finalAmount);
        cashierOrderInfo.setAdjustAmount(adjustAmount);
        cashierOrderInfo.setDiscountRate(discountRate);
        cashierOrderInfo.setBody(sbBody.length() > 20 ? sbBody.substring(0, 20) : sbBody.toString());
        cashierOrderInfo.setProductsInfo(productsInfo);

        //读取支付记录
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderId);
        if (payWrapper != null) {
            cashierOrderInfo.setPayType(payWrapper.getPayType());
            cashierOrderInfo.setPaidAmount(payWrapper.getPaidAmount()
                    + payWrapper.getVipDiscount() + payWrapper.getPromotionDiscount()
                    + payWrapper.getCouponDiscount() + vipAmount);
        }

        ZLogger.d(String.format("订单支付信息:%s", JSONObject.toJSONString(cashierOrderInfo)));
        return cashierOrderInfo;
    }

    /**
     * 生成订单同步数据结构
     */
    public static JSONObject wrapperUploadOrder(PosOrderEntity orderEntity) {
        ZLogger.d(String.format("准备同步订单 : (%d/%s) %s", orderEntity.getId(), orderEntity.getBarCode(),
                JSONObject.toJSONString(orderEntity)));
        JSONObject order = new JSONObject();

        order.put("id", orderEntity.getId());//pos机订单编号
        order.put("barCode", orderEntity.getBarCode());
        order.put("status", orderEntity.getStatus());
        order.put("remark", orderEntity.getRemark());//备注
        order.put("bcount", orderEntity.getBcount());//数量
        order.put("adjPrice", MathCompact.sub(orderEntity.getRetailAmount(), orderEntity.getFinalAmount())); //调价金额
        order.put("paystatus", orderEntity.getPaystatus());
        order.put("subType", orderEntity.getSubType());//业务子类型
        order.put("outerNo", orderEntity.getOuterTradeNo());//外部订单编号（外部平台订单组货功能特有）
        order.put("posId", orderEntity.getPosId());//机器编号
        order.put("sellOffice", orderEntity.getSellOffice());//curoffice id
        order.put("sellerId", orderEntity.getSellerId());//spid
        order.put("humanId", orderEntity.getHumanId());//会员编号
        //由后台计算折扣
//        if (orderEntity.getRetailAmount() == 0D) {
//            order.put("discount", Double.valueOf(String.valueOf(Integer.MAX_VALUE)));
//        } else {
//            order.put("discount", (orderEntity.getRetailAmount() - orderEntity.getDiscountAmount())
//                    / orderEntity.getRetailAmount());
//        }

        //使用订单最后更新日期作为订单生效日期
        Date createdDate = orderEntity.getUpdatedDate();
        if (createdDate == null) {
            createdDate = orderEntity.getCreatedDate();
        }
        order.put("createdDate", TimeUtil.format(createdDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        order.put("createdBy", orderEntity.getCreatedBy());

        //读取订单商品明细
        List<PosOrderItemEntity> orderItemEntities = CashierProvider.fetchOrderItems(orderEntity.getId());
        JSONArray items = new JSONArray();
        for (PosOrderItemEntity entity : orderItemEntities) {
            JSONObject item = new JSONObject();
            item.put("goodsId", entity.getGoodsId());
            item.put("productId", entity.getProductId());
            item.put("skuId", entity.getProSkuId());
            item.put("barcode", entity.getBarcode());
            item.put("bcount", entity.getBcount());
            item.put("price", entity.getCostPrice());//原价（零售价）
            item.put("customerPrice", entity.getFinalCustomerPrice());// 会员价（服务端备存）
            item.put("amount", entity.getAmount());//商品预设的原始价格
            item.put("factAmount", entity.getFinalAmount());//订单明细的实际折后销售价格，商品本次销售原价金额(例如抹零)
            //// TODO: 19/04/2017
            //saleAmount，根据ruleAmountMap去计算
            item.put("saleAmount", MathCompact.sub(entity.getFinalAmount(), entity.getVipAmount()));//实际销售金额(扣除了会员优惠后)
            //该条订单明细流水具体的会员折扣规则优惠情况，可能会有多条会员折扣规则适用，其中key是规则id，value是该规则的产生的优惠金额
            String ruleAmountMap = entity.getRuleAmountMap();
            if (ruleAmountMap != null) {
                item.put("ruleAmountMap", JSONObject.parse(ruleAmountMap));
            }
//            item.put("cateType", entity.getCateType());//按类目进行账务清分
            item.put("prodLineId", entity.getProdLineId());//按产品线进行账务清分
            items.add(item);
        }
        order.put("items", items);
        String rpDisAmountMap = orderEntity.getRpDisAmountMap();
        if (rpDisAmountMap != null) {
            order.put("rpDisAmountMap", JSONObject.parse(rpDisAmountMap));
        }

        //2016-07-01 上传订单支付记录到后台
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        if (payWrapper != null) {
            //注意这里上传的支付记录不包括现金找零和会员账户余额
            order.put("payWays", payWrapper.getUploadPayWays());
            //优惠金额（促销规则+卡券）
            Double disAmount = payWrapper.getVipDiscount() + payWrapper.getPromotionDiscount() + payWrapper.getCouponDiscount();
            order.put("disAmount", disAmount);
            //卡券核销
            order.put("couponsIds", payWrapper.getCouponsIds());
            order.put("ruleIds", payWrapper.getRuleIds());
            order.put("payType", payWrapper.getPayType());//支付方式
            Double amount = orderEntity.getFinalAmount() - disAmount;
            order.put("amount", amount);//订单金额，负数表示退单
            if (amount >= 0.01) {
                order.put("score", amount / 2);
            } else {
                order.put("score", 0D);
            }
        } else {
            order.put("amount", orderEntity.getFinalAmount());//实际支付金额
        }

        return order;
    }
}


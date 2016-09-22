package com.bingshanguxue.cashier.v2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderItemService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.DiscountInfo;
import com.bingshanguxue.cashier.model.wrapper.LastOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单拆分机器
 * Created by bingshanguxue on 8/19/16.
 */
@Deprecated
public class CashierAgent {

    /**
     * 获取订单列表
     * */
    public static List<PosOrderEntity> fetchOrderEntities(Integer bizType,
                                                          String orderBarCode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' " +
                        "and barCode = '%s'",
                MfhLoginService.get().getSpid(), bizType, orderBarCode);
        return PosOrderService.get().queryAllBy(sqlOrder);
    }

    /**
     * 查找激活状态的订单
     * @param orderBarCode 订单流水号
     * */
    public static List<PosOrderEntity> fetchActiveOrderEntities(Integer bizType,
                                                                String orderBarCode) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' " +
                        "and barCode = '%s' and isActive = '%d'",
                MfhLoginService.get().getSpid(), bizType, orderBarCode, PosOrderEntity.ACTIVE);
        return PosOrderService.get().queryAllBy(sqlOrder);
    }

    public static List<PosOrderEntity> fetchActiveOrderEntities(Integer bizType, int status) {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' " +
                        "and isActive = '%d' and status = '%d'",
                MfhLoginService.get().getSpid(), bizType, PosOrderEntity.ACTIVE, status);
        return PosOrderService.get().queryAllBy(sqlOrder);
    }


    /**
     * 查找激活状态的订单明细
     * @param orderBarCode 订单流水号
     * */
    public static List<PosOrderItemEntity> fetchActiveOrderItems(String orderBarCode) {
        return fetchOrderItems(fetchActiveOrderEntities(BizType.POS, orderBarCode));
    }

    /**
     * 获取订单明细
     * */
    public static List<PosOrderItemEntity> fetchOrderItems(List<PosOrderEntity> orderEntities) {
        if (orderEntities == null || orderEntities.size() < 1) {
            return null;
        }

        List<PosOrderItemEntity> itemEntities = new ArrayList<>();

        for (PosOrderEntity orderEntity : orderEntities) {
            //加载订单明细
            List<PosOrderItemEntity> temp = PosOrderItemService.get()
                    .queryAllBy(String.format("orderId = '%s'", orderEntity.getId()));
            if (temp != null){
                itemEntities.addAll(temp);
            }
        }

        return itemEntities;
    }

    /**
     * 获取订单明细
     * */
    public static List<PosOrderItemEntity> fetchOrderItems(PosOrderEntity orderEntity) {
        return PosOrderItemService.get()
                .queryAllBy(String.format("orderId = '%s'", orderEntity.getId()));
    }

    /**
     * 上一订单信息
     * */
    public static LastOrderInfo genLastOrderInfo(List<PosOrderEntity> orderEntities){
        LastOrderInfo lastOrderInfo = null;

        if (orderEntities != null && orderEntities.size() > 0) {
            lastOrderInfo = new LastOrderInfo();
            for (PosOrderEntity orderEntity : orderEntities) {
                OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());

                lastOrderInfo.setPayType(lastOrderInfo.getPayType() | payWrapper.getPayType());
                lastOrderInfo.setFinalAmount(lastOrderInfo.getFinalAmount() + orderEntity.getFinalAmount());
                lastOrderInfo.setbCount(lastOrderInfo.getbCount() + orderEntity.getBcount());
                lastOrderInfo.setDiscountAmount(lastOrderInfo.getDiscountAmount() + payWrapper.getRuleDiscount());
                lastOrderInfo.setChangeAmount(lastOrderInfo.getChangeAmount() + payWrapper.getChange());
            }
        }

        return lastOrderInfo;
    }

    /**
     * 结算（POS拆分订单）
     *
     * @param orderBarCode
     * @param shopcartEntities <ol>
     *                         结算
     *                         <li>判断当前收银台购物车的商品是否为空，若不为空，则继续第2步，否则结束；</li>
     *                         <li>生成订单,［并拆单］；</li>
     *                         <li>更新订单明细（需要删除历史记录）；</li>
     *                         <li>结束</li>
     *                         <li>注意，这里保存后的订单里的金额可能还和明细没有对得上，还需要生成结算信息，再次保存。</li>
     *                         </ol>
     */
    public static boolean splitSettle(String orderBarCode, List<CashierShopcartEntity> shopcartEntities) {
        //Step 1 按产品线拆单
        Map<Integer, List<CashierShopcartEntity>> splitMap = new HashMap<>();

        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity shopcartEntity : shopcartEntities) {
                Integer prodLineId = shopcartEntity.getProdLineId();
                List<CashierShopcartEntity> tempEntities = splitMap.get(prodLineId);
                if (tempEntities == null) {
                    tempEntities = new ArrayList<>();
                }
                tempEntities.add(shopcartEntity);

                splitMap.put(prodLineId, tempEntities);
                ZLogger.df(String.format("拆单--产品线编号:%d, %s/%s", prodLineId,
                        shopcartEntity.getBarcode(), shopcartEntity.getName()));
            }
        }

        //Step 3.1 更新已有订单
        List<PosOrderEntity> orderEntities = fetchOrderEntities(BizType.POS, orderBarCode);
        if (orderEntities != null && orderEntities.size() > 0) {
            for (PosOrderEntity orderEntity : orderEntities) {
                Integer subType = orderEntity.getSubType();
                if (splitMap.containsKey(subType)) {
                    orderEntity.setIsActive(1);
                    orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
                    PosOrderService.get().saveOrUpdate(orderEntity);

                    //更新订单明细
                    PosOrderItemService.get().deleteBy(String.format("orderId = '%d'",
                            orderEntity.getId()));
                    List<CashierShopcartEntity> goodsEntities = splitMap.get(subType);
                    for (CashierShopcartEntity goods : goodsEntities) {
                        PosOrderItemService.get().saveOrUpdate(orderEntity.getBarCode(),
                                orderEntity.getId(), goods);
                    }

                    // TODO: 7/7/16 订单明细统计数据保存到订单
                    splitMap.remove(subType);
                } else {
                    //关闭订单，订单明细不清空
                    orderEntity.setIsActive(0);
                    PosOrderService.get().saveOrUpdate(orderEntity);
                }

                ZLogger.df(String.format("订单%s_%d:\n%s", orderBarCode,
                        orderEntity.getId(), JSONObject.toJSONString(orderEntity)));
            }
        }

        //Step 3.2 保存新订单和商品明细
        for (Integer subType : splitMap.keySet()) {
            PosOrderEntity orderEntity = new PosOrderEntity();
            orderEntity.setBarCode(orderBarCode);
            orderEntity.setBizType(BizType.POS);
            orderEntity.setSubType(subType);
            orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
            orderEntity.setSellerId(MfhLoginService.get().getSpid());// 需要登录
            orderEntity.setCreatedBy(String.valueOf(MfhLoginService.get().getCurrentGuId()));
            orderEntity.setPosId(SharedPreferencesManager.getTerminalId());//设备编号
            orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);//订单状态
            orderEntity.setCreatedDate(new Date());
            orderEntity.setUpdatedDate(new Date());
            PosOrderService.get().saveOrUpdate(orderEntity);
            ZLogger.df(String.format("订单%s_%d:\n%s", orderBarCode,
                    orderEntity.getId(), JSONObject.toJSONString(orderEntity)));

            List<CashierShopcartEntity> goodsEntities = splitMap.get(subType);
            for (CashierShopcartEntity goods : goodsEntities) {
                PosOrderItemService.get().saveOrUpdate(orderEntity.getBarCode(),
                        orderEntity.getId(), goods);
            }
        }

        return true;
    }

    /**
     * 订单结算信息
     * @param orderBarCode 订单流水号
     * */
    public static CashierOrderInfo makeCashierOrderInfo(Integer bizType,
                                                        String orderBarCode,
                                                        Human customerMembershipInfo) {
        String subject = String.format("订单信息：流水号：%s，交易类型：%s",
                orderBarCode, BizType.name(bizType));

        //加载拆分订单
        List<CashierOrderItemInfo> cashierOrderItemInfos = new ArrayList<>();
        Double paidAmount = 0D;
        List<PosOrderEntity> orderEntities = fetchActiveOrderEntities(bizType, orderBarCode);
        for (PosOrderEntity orderEntity : orderEntities) {
            cashierOrderItemInfos.add(genCashierorderItemInfo(orderEntity));

            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
            paidAmount += payWrapper.getPaidAmount();
        }

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.initCashierSetle(orderBarCode, bizType, cashierOrderItemInfos,
                subject, customerMembershipInfo, paidAmount);

        return cashierOrderInfo;
    }

    /**
     * 生成订单结算明细信息
     * @param orderEntity 收银订单
     */
    public static CashierOrderItemInfo genCashierorderItemInfo(PosOrderEntity orderEntity) {
        Double bCount = 0D;
        Double retailAmount = 0D;
        Double finalAmount = 0D;
        Double discountAmount = 0D;
        Double discountRate = 0D;
        StringBuilder sbBody = new StringBuilder();
        JSONArray productsInfo = new JSONArray();
        List<PosOrderItemEntity> orderItemEntityList = fetchOrderItems(orderEntity);
        if (orderItemEntityList != null && orderItemEntityList.size() > 0) {
            for (PosOrderItemEntity itemEntity : orderItemEntityList) {
                bCount += itemEntity.getBcount();
                retailAmount += itemEntity.getAmount();
                finalAmount += itemEntity.getFinalAmount();

                if (sbBody.length() > 0) {
                    sbBody.append(",");
                }
                sbBody.append(itemEntity.getName());

                JSONObject item = new JSONObject();
                item.put("goodsId", itemEntity.getGoodsId());
                item.put("skuId", itemEntity.getProSkuId());
                item.put("bcount", itemEntity.getBcount());
                item.put("price", itemEntity.getCostPrice());
                item.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,
                productsInfo.add(item);
            }
        }
        discountAmount = retailAmount - finalAmount;
        if (retailAmount == 0D) {
            discountRate = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
        } else {
            discountRate = finalAmount / retailAmount;
        }

        CashierOrderItemInfo orderItemInfo = new CashierOrderItemInfo();
        orderItemInfo.setOrderId(orderEntity.getId());
        orderItemInfo.setbCount(bCount);
        orderItemInfo.setRetailAmount(retailAmount);
        orderItemInfo.setFinalAmount(finalAmount);
        orderItemInfo.setAdjustDiscountAmount(discountAmount);
        orderItemInfo.setDiscountRate(discountRate);
        orderItemInfo.setBrief(sbBody.length() > 20 ? sbBody.substring(0, 20) : sbBody.toString());
        orderItemInfo.setProductsInfo(productsInfo);
        orderItemInfo.setDiscountInfo(new DiscountInfo(orderEntity.getId()));

        //读取支付记录
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        if (payWrapper != null){
            orderItemInfo.setPayType(payWrapper.getPayType());
            orderItemInfo.setPaidAmount(payWrapper.getPaidAmount());
            orderItemInfo.setChange(payWrapper.getChange());
        }

        ZLogger.d(JSON.toJSONString(orderItemInfo));
        return orderItemInfo;
    }

    /**
     * 更新收银订单
     *
     * @param cashierOrderInfo 订单支付信息
     * @param status           订单状态
     */
    public static boolean updateCashierOrder(CashierOrderInfo cashierOrderInfo, int status) {
        if (cashierOrderInfo == null) {
            return false;
        }
        List<PosOrderEntity> orderEntities = fetchActiveOrderEntities(cashierOrderInfo.getBizType(),
                        cashierOrderInfo.getPosTradeNo());
        if (orderEntities == null || orderEntities.size() <= 0) {
            return false;
        }

        Date updateDate = new Date();
        for (PosOrderEntity orderEntity : orderEntities) {
            CashierOrderItemInfo cashierOrderItemInfo = CashierOrderInfoImpl
                    .getCashierOrderItemInfo(cashierOrderInfo, orderEntity.getId());
            if (cashierOrderItemInfo == null) {
                continue;
            }

            orderEntity.setUpdatedDate(updateDate);
            orderEntity.setRetailAmount(cashierOrderItemInfo.getRetailAmount());
            orderEntity.setFinalAmount(cashierOrderItemInfo.getFinalAmount());
            orderEntity.setDiscountAmount(cashierOrderItemInfo.getAdjustDiscountAmount());//折扣价
            orderEntity.setBcount(cashierOrderItemInfo.getbCount());
//        orderEntity.setBizType(cashierOrderInfo.getBizType());
//            orderEntity.setCouponsIds(cashierOrderItemInfo.getCouponsIds());
//        orderEntity.setRuleIds(cashierOrderInfo.getRuleIds());
//            orderEntity.setPayType(cashierOrderInfo.getPayType());

            Human human = cashierOrderInfo.getVipMember();
            if (human != null) {
                orderEntity.setHumanId(human.getId());
                orderEntity.setScore(0D);//会员积分
            }

            //支付完成
            orderEntity.setStatus(status);
            if (status == PosOrderEntity.ORDER_STATUS_FINISH) {
//            ZLogger.d("订单已经支付");
                orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_YES);
            } else {
                orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_NO);
//            ZLogger.d("订单未支付");
            }

            PosOrderService.get().saveOrUpdate(orderEntity);
            ZLogger.df(String.format("更新订单 %s_%d:\n%s", orderEntity.getBarCode(),
                    orderEntity.getId(), JSONObject.toJSONString(orderEntity)));
        }
        return true;
    }

    /**
     * 保存支付记录并更新支付订单
     *
     * @param cashierOrderInfo 订单结算信息
     * @param paymentInfo      订单支付信息
     *                         适用场景：收银支付金额或者状态发生改变
     */
    public static boolean updateCashierOrder(CashierOrderInfo cashierOrderInfo,
                                             PaymentInfo paymentInfo) {

        //参数检查
        if (cashierOrderInfo == null || paymentInfo == null) {
            return false;
        }

        //更新日期
        Date updateDate = new Date();
        //订单流水号
        String orderBarCode = cashierOrderInfo.getPosTradeNo();
        //会员信息
        Human vipMember = cashierOrderInfo.getVipMember();


        PosOrderEntity orderEntity = null;
        List<PosOrderEntity> orderEntities = fetchActiveOrderEntities(cashierOrderInfo.getBizType(), orderBarCode);
        if (orderEntities != null && orderEntities.size() > 0) {
            orderEntity = orderEntities.get(0);
        }
        if (orderEntity == null) {
            ZLogger.df("订单不存在");
            return false;
        }

        //保存支付记录
        PaymentInfoImpl.saveOrUpdate(paymentInfo, orderEntity, vipMember);

        //更新订单信息
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        orderEntity.setPayType(payWrapper.getPayType());
        orderEntity.setChange(payWrapper.getChange());
        orderEntity.setPaidAmount(payWrapper.getPaidAmount());
        if (vipMember != null) {
            orderEntity.setHumanId(vipMember.getId());
        }
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
        orderEntity.setUpdatedDate(updateDate);

        PosOrderService.get().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("更新订单：\n%s", JSONObject.toJSONString(orderEntity)));

        return true;
    }

    /**
     * 调单1
     */
    public static List<PosOrderItemEntity> resume(String orderBarCode) {
        List<PosOrderEntity> orderEntities = fetchActiveOrderEntities(BizType.POS, orderBarCode);
        if (orderEntities == null || orderEntities.size() < 1) {
            return null;
        }

        List<PosOrderItemEntity> itemEntities = new ArrayList<>();

        for (PosOrderEntity orderEntity : orderEntities) {
            //修改订单状态：挂单改为待支付
            orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
            PosOrderService.get().saveOrUpdate(orderEntity);

            //加载订单明细
            List<PosOrderItemEntity> temp = PosOrderItemService.get()
                    .queryAllBy(String.format("orderId = '%d'", orderEntity.getId()));
            if (temp != null) {
                itemEntities.addAll(temp);
            }
        }

        return itemEntities;
    }
}

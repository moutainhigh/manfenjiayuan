package com.manfenjiayuan.pda_supermarket.cashier.database.service;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.pda_supermarket.cashier.database.dao.PosOrderPayDao;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosOrderPayEntity;
import com.manfenjiayuan.pda_supermarket.cashier.model.PaymentInfo;
import com.manfenjiayuan.pda_supermarket.cashier.model.wrapper.PayWayType;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;

import java.util.Date;
import java.util.List;

/**
 * POS--订单支付流水
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosOrderPayService extends BaseService<PosOrderPayEntity, String, PosOrderPayDao> {
    @Override
    protected Class<PosOrderPayDao> getDaoClass() {
        return PosOrderPayDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PosOrderPayService instance = null;

    /**
     * 返回 PosOrderPayService 实例
     *
     * @return
     */
    public static PosOrderPayService get() {
//        String lsName = PosOrderItemService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new PosOrderItemService();//初始化登录服务
//        }
        if (instance == null) {
            synchronized (PosOrderPayService.class) {
                if (instance == null) {
                    instance = new PosOrderPayService();
                }
            }
        }
        return instance;
    }

    public PosOrderPayEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosOrderPayEntity entity) {
        getDao().save(entity);
    }

    public void saveOrUpdate(PosOrderPayEntity entity) {
        getDao().saveOrUpdate(entity);
    }

    /**
     * 清空历史记录
     */
    public void clear() {
        getDao().deleteAll();
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<PosOrderPayEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<PosOrderPayEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<PosOrderPayEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getDao().queryAllBy(strWhere, orderBy);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public List<PosOrderPayEntity> queryAllByDesc(String strWhere) {
        return getDao().queryAllByDesc(strWhere);
    }

    public void deleteById(String id) {
        try {
            getDao().deleteById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public void deleteBy(String strWhere) {
        try {
            getDao().deleteBy(strWhere);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 保存货更新订单的支付记录
     * 2016-07-01 重构订单，支持订单支付明细
     *
     * @param orderId    关联的订单
     * @param outTradeNo 商户交易订单号，每次发起订单支付请求都不同
     * @param payType    支付方式
     * @param amountType 收入/支出
     * @param amount     收入/找零金额
     * @param status     支付状态
     * @param vipMember  客户信息
     * @param couponsIds 优惠券信息
     */
    public void saveOrUpdate(Long orderId,
                             String outTradeNo, int payType, Integer amountType,
                             Double amount, int status,
                             Human vipMember, String couponsIds, String ruleIds) {
        try {
            //检查参数
            if (StringUtils.isEmpty(outTradeNo) || orderId == null) {
                ZLogger.d("参数无效");
                return;
            }

            Date rightNow = TimeUtil.getCurrentDate();
            PosOrderPayEntity entity;
            //查询订单，更多的匹配条件
            //注意这里要根据orderId，outTradeNo和payType三者确定支付记录的唯一性，
            // 因为一个订单对应多个商户交易订单号，同时一个商户交易订单号也有可能对应多个支付类型
            // （会员支付时，优惠券和规则是在会员支付页面一起支付，公用一个商户交易订单号）
            String sqlWhere = String.format("outTradeNo = '%s' and orderId = '%d' " +
                            "and payType = '%d' and amountType = '%d'",
                    outTradeNo, orderId, payType, amountType);
            List<PosOrderPayEntity> entityList = queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0) {
                entity = entityList.get(0);
            } else {
                entity = new PosOrderPayEntity();
                entity.setCreatedDate(rightNow);
                entity.setOrderId(orderId);
                entity.setOutTradeNo(outTradeNo);
                entity.setPayType(payType);
                entity.setAmountType(amountType);
            }

            entity.setAmount(amount);
            entity.setPaystatus(status);
            if (vipMember != null) {
                entity.setCustomerHumanId(vipMember.getId());
            }
            entity.setCouponsIds(couponsIds);
            entity.setRuleIds(ruleIds);
            entity.setUpdatedDate(rightNow);
            saveOrUpdate(entity);
            ZLogger.d(String.format("保存or更新订单支付流水:%s", JSONObject.toJSONString(entity)));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 保存支付信息
     *
     * @param orderId   关联的订单
     * @param paymentInfo   支付信息
     * @param member    会员信息
     */
    public void savePayInfo(Long orderId, PaymentInfo paymentInfo, Human member) {
        //商户交易订单号
        String outTradeNo = paymentInfo.getOutTradeNo();
        Double paidRemain = paymentInfo.getPaidAmount();//实际支付
        Double changeRemain = paymentInfo.getChange();//找零
        //支付状态
        int status = paymentInfo.getStatus();

        int amountType = PayWayType.TYPE_NA;
        int payType = paymentInfo.getPayType();
        if ((payType & WayType.CASH) == WayType.CASH) {
            amountType = PayWayType.TYPE_CASH;
            //保存找零金额
            if (changeRemain >= 0.01) {
                saveOrUpdate(orderId,
                        outTradeNo, paymentInfo.getPayType(),
                        PayWayType.TYPE_CASH_CHANGE, changeRemain,
                        status, member, null, null);
            }
        } else if ((payType & WayType.ALI_F2F) == WayType.ALI_F2F) {
            amountType = PayWayType.TYPE_ALIPAY_F2F;
        } else if ((payType & WayType.BANKCARD) == WayType.BANKCARD) {
            amountType = PayWayType.TYPE_BANKCARD;
        } else if ((payType & WayType.VIP) == WayType.VIP) {
            amountType = PayWayType.TYPE_VIP;
            saveOrUpdate(orderId,
                    outTradeNo, paymentInfo.getPayType(),
                    PayWayType.TYPE_VIP_BALANCE, changeRemain,
                    status, member, null, null);

            PayAmount payAmount = paymentInfo.getDiscountInfo();
            if (payAmount != null) {
                //会员优惠
                saveOrUpdate(orderId,
                        outTradeNo, WayType.RULES,
                        PayWayType.TYPE_VIP_DISCOUNT,
                        payAmount.getItemRuleAmount(),
                        status, member, payAmount.getCouponsIds(), payAmount.getRuleIds());
                //促销优惠
                saveOrUpdate(orderId,
                        outTradeNo, WayType.RULES,
                        PayWayType.TYPE_VIP_PROMOTION,
                        payAmount.getPackRuleAmount(),
                        status, member, payAmount.getCouponsIds(), payAmount.getRuleIds());
                //优惠券
                saveOrUpdate(orderId,
                        outTradeNo, WayType.RULES,
                        PayWayType.TYPE_VIP_COUPONS,
                        payAmount.getCoupAmount(),
                        status, member, payAmount.getCouponsIds(), payAmount.getRuleIds());
            }
        } else if ((payType & WayType.WX_F2F) == WayType.WX_F2F) {
            amountType = PayWayType.TYPE_WEPAY_F2F;
        } else if ((payType & WayType.TAKEOUT) == WayType.TAKEOUT) {
            amountType = PayWayType.TYPE_THIRD_PARTY;
        }
        //保存实际支付金额
        saveOrUpdate(orderId,
                outTradeNo, payType, amountType, paidRemain, status, member, null, null);
    }
}
package com.manfenjiayuan.pda_supermarket.database.logic;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.pda_supermarket.database.dao.PosOrderPayDao;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderPayEntity;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.DiscountInfo;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;

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
                entity.setCreatedDate(new Date());
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
            entity.setUpdatedDate(new Date());
            saveOrUpdate(entity);
            ZLogger.df(String.format("保存or更新订单支付流水:\n%s",
                    JSONObject.toJSONString(entity)));
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
     */
    public void saveOrUpdate(Long orderId,
                                    String outTradeNo, int payType, Integer amountType,
                                    Double amount, int status,
                                    Human vipMember) {
        saveOrUpdate(orderId, outTradeNo, payType, amountType, amount, status, vipMember, null, null);
    }

    /**
     * 保存或更新订单的支付记录
     * 2016-07-01 重构订单，支持订单支付明细
     *
     * @param outTradeNo   商户交易订单号，每次发起订单支付请求都不同
     * @param status       支付状态
     * @param vipMember    客户信息
     * @param discountInfo 优惠信息
     */
    public void saveOrUpdate(String outTradeNo, int status,
                                    Human vipMember, DiscountInfo discountInfo) {
        if (discountInfo.getEffectAmount() < 0.01){
            return;
        }
        saveOrUpdate(discountInfo.getOrderId(),
                outTradeNo, discountInfo.getPayType(),
                PosOrderPayEntity.AMOUNT_TYPE_IN,
                discountInfo.getEffectAmount(),
                status, vipMember, discountInfo.getCouponsIds(), discountInfo.getRuleIds());
    }


}
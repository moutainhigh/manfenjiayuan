package com.bingshanguxue.cashier.database.service;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.SyncStatus;
import com.bingshanguxue.cashier.database.dao.PosTopupDao;
import com.bingshanguxue.cashier.database.entity.PosTopupEntity;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * POS充值记录
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosTopupService extends BaseService<PosTopupEntity, String, PosTopupDao> {
    @Override
    protected Class<PosTopupDao> getDaoClass() {
        return PosTopupDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PosTopupService instance = null;

    /**
     * 返回 PosOrderPayService 实例
     *
     * @return
     */
    public static PosTopupService get() {
//        String lsName = PosOrderItemService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new PosOrderItemService();//初始化登录服务
//        }
        if (instance == null) {
            synchronized (PosTopupService.class) {
                if (instance == null) {
                    instance = new PosTopupService();
                }
            }
        }
        return instance;
    }

    public PosTopupEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosTopupEntity entity) {
        getDao().save(entity);
    }

    public void saveOrUpdate(PosTopupEntity entity) {
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
    public List<PosTopupEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getDao().queryAll(strWhere, pageInfo);
    }

    public List<PosTopupEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<PosTopupEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getDao().queryAllBy(strWhere, orderBy);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public List<PosTopupEntity> queryAllByDesc(String strWhere) {
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
     * @param quickPayInfo    支付信息
     * @param outTradeNo 商户交易订单号，每次发起订单支付请求都不同
     * @param payType    支付方式
     * @param status     支付状态
     */
    public void saveOrUpdate(QuickPayInfo quickPayInfo, String outTradeNo, Integer payType, int status) {
        try {
            //检查参数
            if (StringUtils.isEmpty(outTradeNo) || quickPayInfo == null) {
                ZLogger.d("参数无效");
                return;
            }

            PosTopupEntity entity;
            //查询订单，更多的匹配条件
            //注意这里要根据orderId，outTradeNo和payType三者确定支付记录的唯一性，
            // 因为一个订单对应多个商户交易订单号，同时一个商户交易订单号也有可能对应多个支付类型
            // （会员支付时，优惠券和规则是在会员支付页面一起支付，公用一个商户交易订单号）
            String sqlWhere = String.format("outTradeNo = '%s'and payType = '%d'",
                    outTradeNo, payType);
            List<PosTopupEntity> entityList = queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0) {
                entity = entityList.get(0);
            } else {
                entity = new PosTopupEntity();
                entity.setCreatedDate(new Date());
                entity.setOutTradeNo(outTradeNo);
                entity.setPayType(payType);
            }

            entity.setBizType(quickPayInfo.getBizType());
            entity.setSubBizType(quickPayInfo.getSubBizType());
            entity.setAmount(quickPayInfo.getAmount());
            entity.setPaystatus(status);
            entity.setSyncStatus(SyncStatus.INIT);
            entity.setUpdatedDate(new Date());
            saveOrUpdate(entity);
            ZLogger.df(String.format("保存or更新支付流水:\n%s",
                    JSONObject.toJSONString(entity)));
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }

    /**
     * 清除旧数据
     *
     * @param saveDate 保存的天数
     */
    public void deleteOldData(int saveDate) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0 - saveDate);//
        String expireCursor = TimeCursor.InnerFormat.format(calendar.getTime());
        ZLogger.d(String.format("清分支付记录过期时间(%s)保留最近%d天数据。", expireCursor, saveDate));

        deleteBy(String.format("updatedDate < '%s'", expireCursor));
    }

}
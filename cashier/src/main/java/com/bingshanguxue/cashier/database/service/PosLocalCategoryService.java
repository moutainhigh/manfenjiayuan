package com.bingshanguxue.cashier.database.service;

import com.bingshanguxue.cashier.database.dao.PosLocalCategoryDao;
import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.List;

/**
 * POS本地类目
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosLocalCategoryService extends BaseService<PosLocalCategoryEntity, String, PosLocalCategoryDao> {
    @Override
    protected Class<PosLocalCategoryDao> getDaoClass() {
        return PosLocalCategoryDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PosLocalCategoryService instance = null;

    /**
     * 返回 PosOrderPayService 实例
     *
     * @return
     */
    public static PosLocalCategoryService get() {
//        String lsName = PosOrderItemService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new PosOrderItemService();//初始化登录服务
//        }
        if (instance == null) {
            synchronized (PosLocalCategoryService.class) {
                if (instance == null) {
                    instance = new PosLocalCategoryService();
                }
            }
        }
        return instance;
    }

    public PosLocalCategoryEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosLocalCategoryEntity entity) {
        getDao().save(entity);
    }

    public void saveOrUpdate(PosLocalCategoryEntity entity) {
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
    public List<PosLocalCategoryEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getDao().queryAll(strWhere, pageInfo);
    }

    public List<PosLocalCategoryEntity> queryAllBy(String strWhere, String orderBy) {
        try {
            return getDao().queryAllBy(strWhere, orderBy);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public List<PosLocalCategoryEntity> queryAllByDesc(String strWhere) {
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

//    /**
//     * 保存货更新订单的支付记录
//     * 2016-07-01 重构订单，支持订单支付明细
//     *
//     * @param quickPayInfo    支付信息
//     * @param outTradeNo 商户交易订单号，每次发起订单支付请求都不同
//     * @param payType    支付方式
//     * @param status     支付状态
//     */
//    public void saveOrUpdate(QuickPayInfo quickPayInfo, String outTradeNo, Integer payType, int status) {
//        try {
//            //检查参数
//            if (StringUtils.isEmpty(outTradeNo) || quickPayInfo == null) {
//                ZLogger.d("参数无效");
//                return;
//            }
//
//            PosLocalCategoryEntity entity;
//            //查询订单，更多的匹配条件
//            //注意这里要根据orderId，outTradeNo和payType三者确定支付记录的唯一性，
//            // 因为一个订单对应多个商户交易订单号，同时一个商户交易订单号也有可能对应多个支付类型
//            // （会员支付时，优惠券和规则是在会员支付页面一起支付，公用一个商户交易订单号）
//            String sqlWhere = String.format("outTradeNo = '%s'and payType = '%d'",
//                    outTradeNo, payType);
//            List<PosLocalCategoryEntity> entityList = queryAllBy(sqlWhere);
//            if (entityList != null && entityList.size() > 0) {
//                entity = entityList.get(0);
//            } else {
//                entity = new PosLocalCategoryEntity();
//                entity.setCreatedDate(new Date());
//                entity.setOutTradeNo(outTradeNo);
//                entity.setPayType(payType);
//            }
//
//            entity.setBizType(quickPayInfo.getBizType());
//            entity.setAmount(quickPayInfo.getAmount());
//            entity.setPaystatus(status);
//            entity.setSyncStatus(SyncStatus.INIT);
//            entity.setUpdatedDate(new Date());
//            saveOrUpdate(entity);
//            ZLogger.df(String.format("保存or更新支付流水:\n%s",
//                    JSONObject.toJSONString(entity)));
//        } catch (Exception e) {
//            ZLogger.e(e.toString());
//        }
//    }
//

}
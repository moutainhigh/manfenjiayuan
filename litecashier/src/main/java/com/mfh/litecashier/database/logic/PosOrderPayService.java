package com.mfh.litecashier.database.logic;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.database.dao.PosOrderPayDao;
import com.mfh.litecashier.database.entity.PosOrderPayEntity;

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
     * 新增支付记录
     *
     * @param orderBarcode 订单条码号,关联订单
     * @param payType      支付方式
     * @param outTradeNo   商户订单编号
     * @param amount       支付金额
     * @param payStatus    支付状态
     * @param member       会员
     */
    public void pay(String orderBarcode, Integer payType, String outTradeNo,
                    Double amount, int payStatus, Human member) {
        try {
            PosOrderPayEntity entity;
            //查询订单，更多的匹配条件
            List<PosOrderPayEntity> entityList = queryAllBy(String
                    .format("orderBarCode = '%s' and payType = '%d' and outTradeNo = '%s'",
                    orderBarcode, payType, outTradeNo));
            if (entityList != null && entityList.size() > 0) {
//            ZLogger.d("支付明细已经存在，更支付明细信息");
                entity = entityList.get(0);
            } else {
//            ZLogger.d("保存支付明细");
                entity = new PosOrderPayEntity();
                entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

                entity.setOrderBarCode(orderBarcode);
                entity.setPayType(payType);
                entity.setOutTradeNo(outTradeNo);
            }

            entity.setAmount(amount);
            entity.setPaystatus(payStatus);
            if (member != null){
                entity.setMemberGUID(member.getGuid());
            }
            entity.setUpdatedDate(new Date());
            saveOrUpdate(entity);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }
}
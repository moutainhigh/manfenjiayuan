package com.bingshanguxue.pda.database.service;

import com.bingshanguxue.pda.database.dao.InvLossStockGoodsDao;
import com.bingshanguxue.pda.database.entity.InvLossStockGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 报损
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvLossStockGoodsService extends BaseService<InvLossStockGoodsEntity, String, InvLossStockGoodsDao> {
    @Override
    protected Class<InvLossStockGoodsDao> getDaoClass() {
        return InvLossStockGoodsDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static InvLossStockGoodsService instance = null;

    /**
     * 返回 IMConversationService 实例
     *
     * @return
     */
    public static InvLossStockGoodsService get() {
        String lsName = InvLossStockGoodsService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new InvLossStockGoodsService();//初始化登录服务
        }
        return instance;
    }

    public boolean entityExistById(String id) {
        try {
            return getDao().entityExistById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return false;
        }
    }

    public InvLossStockGoodsEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(InvLossStockGoodsEntity entity) {
        getDao().save(entity);
    }

    public void update(InvLossStockGoodsEntity entity) {
        getDao().update(entity);
    }

    public void saveOrUpdate(InvLossStockGoodsEntity entity) {
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
    public List<InvLossStockGoodsEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<InvLossStockGoodsEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<InvLossStockGoodsEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<InvLossStockGoodsEntity> queryAllByDesc(String strWhere) {
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
     * 根据条码查商品
     */
    public InvLossStockGoodsEntity queryEntityBy(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        List<InvLossStockGoodsEntity> entityList = queryAllBy(String.format("barcode = '%s'", barcode));
        if (entityList != null && entityList.size() > 0) {
            return entityList.get(0);
        }

        return null;
    }

    /**
     * 验收商品
     */
    public void inspect(InvLossStockGoodsEntity entity, Double quantity) {
        if (entity == null) {
            return;
        }

        if (!entityExistById(String.valueOf(entity.getId()))) {
            return;
        }

        if (quantity == null) {
            quantity = 0D;
        }

        entity.setCheckInv(quantity);
        entity.setUpdatedDate(new Date());
        saveOrUpdate(entity);
    }

    /**
     * 拒收商品
     */
    public void reject(InvLossStockGoodsEntity entity) {
        if (entity == null) {
            return;
        }

        if (entityExistById(String.valueOf(entity.getId()))) {
            entity.setCheckInv(0D);
            entity.setUpdatedDate(new Date());
            saveOrUpdate(entity);
        }
    }
}

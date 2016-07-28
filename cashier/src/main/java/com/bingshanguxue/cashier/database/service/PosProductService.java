package com.bingshanguxue.cashier.database.service;


import com.bingshanguxue.cashier.database.dao.PosProductDao;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.List;

/**
 * POS--商品--库存
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosProductService extends BaseService<PosProductEntity, String, PosProductDao> {
    private static PosProductService instance = null;

    /**
     * 返回 PosProductService 实例
     *
     * @return
     */
    public static PosProductService get() {
        if (instance == null) {
            synchronized (PosProductService.class) {
                if (instance == null) {
                    instance = new PosProductService();
                }
            }
        }
        return instance;
    }


    @Override
    protected Class<PosProductDao> getDaoClass() {
        return PosProductDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }


    public PosProductEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosProductEntity entity) {
        getDao().save(entity);
    }

    public void saveOrUpdate(PosProductEntity entity) {
        getDao().saveOrUpdate(entity);
    }

    /**
     * 清空历史记录
     */
    public void clear() {
        getDao().deleteAll();
    }

    public List<PosProductEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<PosProductEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<PosProductEntity> queryAllBy(String strWhere, String orderBy, PageInfo pageInfo) {
        return getDao().queryAllBy(strWhere, orderBy, pageInfo);
    }

    public List<PosProductEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }
    public List<PosProductEntity> queryAllByDesc(String strWhere) {
        return getDao().queryAllByDesc(strWhere);
    }

    /**
     * 逐条删除
     */
    public void deleteById(String id) {
        try {
            getDao().deleteById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }
}

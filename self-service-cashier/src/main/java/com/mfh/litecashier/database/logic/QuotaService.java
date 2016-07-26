package com.mfh.litecashier.database.logic;


import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.litecashier.database.dao.QuotaDao;
import com.mfh.litecashier.database.entity.QuotaEntity;

import java.util.List;

/**
 * 金额授权模式-现金额度
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class QuotaService extends BaseService<QuotaEntity, String, QuotaDao> {

    private static QuotaService instance = null;
    /**
     * 返回 PosProductSkuService 实例
     * @return
     */
    public static QuotaService get() {
        if (instance == null) {
            synchronized (QuotaService.class) {
                if (instance == null) {
                    instance = new QuotaService();
                }
            }
        }

        return instance;
    }


    @Override
    protected Class<QuotaDao> getDaoClass() {
        return QuotaDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    public QuotaEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(QuotaEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(QuotaEntity entity) {
        getDao().saveOrUpdate(entity);
    }

    /**
     *  逐条删除
     *  */
    public void deleteById(String id){
        try{
            getDao().deleteById(id);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }

    /**
     * 清空历史记录
     * */
    public void clear(){
        getDao().deleteAll();
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return
     */
    public List<QuotaEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<QuotaEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<QuotaEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }
//    public List<QuotaEntity> queryAllByDesc(String strWhere) {
//        return getDao().queryAllByDesc(strWhere);
//    }

    public List<QuotaEntity> queryAllBy(String strWhere, String orderBy) {
        try{
            return getDao().queryAllBy(strWhere, orderBy);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
}

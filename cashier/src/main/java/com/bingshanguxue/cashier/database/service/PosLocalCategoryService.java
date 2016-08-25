package com.bingshanguxue.cashier.database.service;

import com.bingshanguxue.cashier.database.dao.PosLocalCategoryDao;
import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
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

    /**
     * 保存前台类目
     * */
    public void saveOrUpdate(CategoryInfo categoryInfo){
        if (categoryInfo == null){
            return;
        }

        PosLocalCategoryEntity entity = getEntityById(String.valueOf(categoryInfo.getId()));
        if (entity == null){
            entity = new PosLocalCategoryEntity();
            entity.setId(categoryInfo.getId());
        }
        entity.setName(categoryInfo.getNameCn());
        entity.setIsCloudActive(PosLocalCategoryEntity.CLOUD_ACTIVE);
        PosLocalCategoryService.get().saveOrUpdate(entity);
    }

    /**
     * 下线所有类目
     * */
    public void deactiveAll(){
        List<PosLocalCategoryEntity> entities = queryAll(null, null);
        if (entities != null && entities.size() > 0){
            for (PosLocalCategoryEntity entity : entities){
                entity.setIsCloudActive(PosLocalCategoryEntity.CLOUD_DEACTIVE);
                saveOrUpdate(entity);
            }
        }
    }

    public int getCount(){
        List<PosLocalCategoryEntity> entities = queryAll(null, null);
        return entities != null ? entities.size() : 0;
    }


}
package com.bingshanguxue.cashier.database.service;


import com.bingshanguxue.cashier.database.dao.PosProductSkuDao;
import com.bingshanguxue.cashier.database.entity.PosProductSkuEntity;
import com.bingshanguxue.cashier.model.ProductSkuBarcode;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.List;

/**
 * POS--商品--库存
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosProductSkuService extends BaseService<PosProductSkuEntity, String, PosProductSkuDao> {

    private static PosProductSkuService instance = null;
    /**
     * 返回 PosProductSkuService 实例
     * @return
     */
    public static PosProductSkuService get() {
        if (instance == null) {
            synchronized (PosProductSkuService.class) {
                if (instance == null) {
                    instance = new PosProductSkuService();
                }
            }
        }

        return instance;
    }


    @Override
    protected Class<PosProductSkuDao> getDaoClass() {
        return PosProductSkuDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    public PosProductSkuEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosProductSkuEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(PosProductSkuEntity entity) {
        getDao().saveOrUpdate(entity);
    }

    public void saveOrUpdate(ProductSkuBarcode productSku){
        try{
            if (productSku == null){
                return;
            }

            PosProductSkuEntity entity = new PosProductSkuEntity();
            entity.setId(productSku.getId());
            entity.setCreatedDate(productSku.getCreatedDate());
            entity.setUpdatedDate(productSku.getUpdatedDate());
            entity.setMainBarcode(productSku.getMainBarcode());
            entity.setOtherBarcode(productSku.getOtherBarcode());
            entity.setPackFlag(productSku.getPackFlag());
            entity.setTenantId(productSku.getTenantId());
            entity.setCreatedBy(productSku.getCreatedBy());

            saveOrUpdate(entity);
        }
        catch(Exception e){
            ZLogger.e(String.format("保存箱规失败, %s", e.toString()));
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
    public List<PosProductSkuEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<PosProductSkuEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<PosProductSkuEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }
    public List<PosProductSkuEntity> queryAllByDesc(String strWhere) {
        return getDao().queryAllByDesc(strWhere);
    }

}

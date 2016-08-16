package com.mfh.litecashier.database.logic;

import com.bingshanguxue.cashier.model.ProductCatalog;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.litecashier.database.dao.ProductCatalogDao;
import com.mfh.litecashier.database.entity.ProductCatalogEntity;

import java.util.List;

/**
 * <h1>采购订单购物车商品明细</h1><br>
 * <p>
 *
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ProductCatalogService extends BaseService<ProductCatalogEntity, String, ProductCatalogDao> {

    @Override
    protected Class<ProductCatalogDao> getDaoClass() {
        return ProductCatalogDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static ProductCatalogService instance = null;
    /**
     * 返回 DailysettleService 实例
     * @return
     */
    public static ProductCatalogService getInstance() {
        if (instance == null) {
            synchronized (ProductCatalogService.class) {
                if (instance == null) {
                    instance = new ProductCatalogService();
                }
            }
        }
        return instance;
    }

    public ProductCatalogEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(ProductCatalogEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(ProductCatalogEntity msg) {
        getDao().saveOrUpdate(msg);
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
    public List<ProductCatalogEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<ProductCatalogEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<ProductCatalogEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<ProductCatalogEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }public List<ProductCatalogEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<ProductCatalogEntity> queryAllBy(String strWhere) {
        try{
            return getDao().queryAllBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    /**
     *  按条件删除
     *  */
    public void deleteBy(String strWhere){
        try{
            getDao().deleteBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
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
     * 保存生鲜商品
     * */
    public void saveOrUpdate(ProductCatalog goods){
        if (goods == null){
            ZLogger.d("保存商品类目关系失败，参数无效");
            return;
        }

        String sqlWhere = String.format("id = '%d'", goods.getParamValueId());
        ProductCatalogEntity entity = null;
        List<ProductCatalogEntity> entityList =  getDao().queryAllBy(sqlWhere);
        if (entityList != null && entityList.size() > 0){
            entity =  entityList.get(0);
        }
        if (entity == null){
            entity = new ProductCatalogEntity();
            entity.setId(goods.getId());
        }

        entity.setCataItemId(goods.getCataItemId());
        entity.setParamValueId(goods.getParamValueId());
        saveOrUpdate(entity);

    }

}

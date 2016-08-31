package com.mfh.litecashier.database.logic;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.litecashier.bean.wrapper.FrontCategoryGoods;
import com.mfh.litecashier.database.dao.PosCatetoryGoodsTempDao;
import com.mfh.litecashier.database.entity.PosCategoryGoodsTempEntity;

import java.util.List;

/**
 * <h1>采购订单购物车商品明细</h1><br>
 * <p>
 *
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosCategoryGodosTempService extends BaseService<PosCategoryGoodsTempEntity,
        String, PosCatetoryGoodsTempDao> {

    @Override
    protected Class<PosCatetoryGoodsTempDao> getDaoClass() {
        return PosCatetoryGoodsTempDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PosCategoryGodosTempService instance = null;
    /**
     * 返回 DailysettleService 实例
     * @return
     */
    public static PosCategoryGodosTempService getInstance() {
        if (instance == null) {
            synchronized (PosCategoryGodosTempService.class) {
                if (instance == null) {
                    instance = new PosCategoryGodosTempService();
                }
            }
        }
        return instance;
    }

    public PosCategoryGoodsTempEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosCategoryGoodsTempEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(PosCategoryGoodsTempEntity msg) {
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
    public List<PosCategoryGoodsTempEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<PosCategoryGoodsTempEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<PosCategoryGoodsTempEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<PosCategoryGoodsTempEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }public List<PosCategoryGoodsTempEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<PosCategoryGoodsTempEntity> queryAllBy(String strWhere) {
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
    public void saveOrUpdateGoods(FrontCategoryGoods goods){
        if (goods == null){
            ZLogger.d("保存生鲜商品失败，商品无效或商品条码为空");
            return;
        }

        String sqlWhere = String.format("productId = '%d'", goods.getProductId());

        if (goods.isSelected()){
            PosCategoryGoodsTempEntity entity = null;
            List<PosCategoryGoodsTempEntity> entityList =  getDao().queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0){
                entity =  entityList.get(0);
            }
            if (entity == null){
                entity = new PosCategoryGoodsTempEntity();
                entity.setProductId(goods.getProductId());
            }
            entity.setProSkuId(goods.getId());
            saveOrUpdate(entity);
        }
        else{
            deleteBy(sqlWhere);
        }
    }

}

package com.mfh.litecashier.database.logic;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.database.dao.PurchaseGoodsDao;
import com.mfh.litecashier.database.entity.PurchaseGoodsEntity;

import java.util.List;

/**
 * <h1>采购订单购物车商品明细</h1><br>
 * <p>
 *
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PurchaseGoodsService extends BaseService<PurchaseGoodsEntity, String, PurchaseGoodsDao> {

    @Override
    protected Class<PurchaseGoodsDao> getDaoClass() {
        return PurchaseGoodsDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PurchaseGoodsService instance = null;
    /**
     * 返回 DailysettleService 实例
     * @return
     */
    public static PurchaseGoodsService getInstance() {
        if (instance == null) {
            synchronized (PurchaseGoodsService.class) {
                if (instance == null) {
                    instance = new PurchaseGoodsService();
                }
            }
        }
        return instance;
    }

    public PurchaseGoodsEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PurchaseGoodsEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(PurchaseGoodsEntity msg) {
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
    public List<PurchaseGoodsEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<PurchaseGoodsEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<PurchaseGoodsEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<PurchaseGoodsEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }public List<PurchaseGoodsEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<PurchaseGoodsEntity> queryAllBy(String strWhere) {
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
     * 查询采购订单明细
     * */
    public PurchaseGoodsEntity fetchGoods(Integer purchaseType, Long providerId, Long chainSkuId){
        try{
            if (purchaseType == null || providerId == null || chainSkuId == null){
                return null;
            }

            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' " +
                    "and chainSkuId = '%d'", purchaseType, providerId, chainSkuId);
            List<PurchaseGoodsEntity> entityList =  getDao().queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0){
                return entityList.get(0);
            }
        }catch (Exception e){
            ZLogger.e( e.toString());
        }

        return null;
    }

    /**
     * 查询采购订单明细
     * */
    public PurchaseGoodsEntity fetchGoods(Integer purchaseType, Long providerId, String barcode){
        try{
            if (purchaseType == null || providerId == null || StringUtils.isEmpty(barcode)){
                return null;
            }

            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' " +
                    "and barcode = '%d'", purchaseType, providerId, barcode);
            List<PurchaseGoodsEntity> entityList =  getDao().queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0){
                return entityList.get(0);
            }
        }catch (Exception e){
            ZLogger.e( e.toString());
        }

        return null;
    }


    public List<PurchaseGoodsEntity> fetchGoodsEntities(Integer purchaseType, Long providerId){
        try{
            if (purchaseType == null || providerId == null){
                return null;
            }

            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d'",
                    purchaseType, providerId);
            return getDao().queryAllBy(sqlWhere);
        }catch (Exception e){
            ZLogger.e( e.toString());
        }

        return null;
    }


}

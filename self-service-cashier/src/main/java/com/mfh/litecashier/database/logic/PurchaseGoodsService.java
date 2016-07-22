package com.mfh.litecashier.database.logic;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
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

//    /**
//     * 获取购物车中的所有生鲜商品
//     * */
//    public List<PurchaseGoodsEntity> getFreshGoodsList(){
//        return queryAllBy(String.format("purchaseType = '%d'",
//                PurchaseOrderEntity.PURCHASE_TYPE_FRESH));
//    }
//    public void clearFreshGoodsList(){
//        deleteBy(String.format("purchaseType = '%d'",
//                PurchaseGoodsEntity.PURCHASE_TYPE_FRESH));
//    }
//
//    /**
//     * 获取指定批发商的商品列表*/
//    public List<PurchaseGoodsEntity> getFreshGoodsList(Long providerId){
//        try{
//            StringBuilder sb = new StringBuilder();
//            sb.append(String.format("purchaseType = '%d'",
//                    PurchaseGoodsEntity.PURCHASE_TYPE_FRESH));
//            if (providerId != null){
//                sb.append(String.format("and providerId = '%d'",
//                        providerId));
//            }
//
//            return getDao().queryAllBy(sb.toString(), "updatedDate desc");
//        } catch (Exception e){
//            ZLogger.e(e.toString());
//            return null;
//        }
//    }

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


//    /**
//     * 保存生鲜商品
//     * */
//    public void saveOrUpdateFreshGoods(ChainGoodsSku goods, Double quantity){
//        if (goods == null || StringUtils.isEmpty(goods.getBarcode())){
//            ZLogger.d("保存生鲜商品失败，商品无效或商品条码为空");
//            return;
//        }
//
//        PurchaseGoodsEntity PurchaseGoodsEntity = PurchaseGoodsService
//                .getInstance().getFreshGoods(goods.getTenantId(),
//                        goods.getBarcode());
//
//        if (PurchaseGoodsEntity == null){
//            ZLogger.d(String.format("添加新的生鲜商品到购物车:%s", goods.getBarcode()));
//            PurchaseGoodsEntity = new PurchaseGoodsEntity();
//            PurchaseGoodsEntity.setCreatedDate(new Date());
//            PurchaseGoodsEntity.setPurchaseType(PurchaseGoodsEntity.PURCHASE_TYPE_FRESH);
//            PurchaseGoodsEntity.setProviderId(goods.getTenantId());
//            PurchaseGoodsEntity.setProviderName(goods.getCompanyName());
//            PurchaseGoodsEntity.setChainSkuId(goods.getId());
//            PurchaseGoodsEntity.setIsPrivate(IsPrivate.PLATFORM);
//            PurchaseGoodsEntity.setProSkuId(goods.getProSkuId());
//            PurchaseGoodsEntity.setBarcode(goods.getBarcode());
//        }
//        else{
//            ZLogger.d(String.format("更新购物车中生鲜商品:%s", goods.getBarcode()));
//        }
//
//        PurchaseGoodsEntity.setImgUrl(goods.getImgUrl());
//        PurchaseGoodsEntity.setName(goods.getSkuName());
//        PurchaseGoodsEntity.setUnit(goods.getBuyUnit());
//        PurchaseGoodsEntity.setPrice(goods.getHintPrice());
//        PurchaseGoodsEntity.setQuantity(quantity);
//        PurchaseGoodsEntity.setUpdatedDate(new Date());
//        saveOrUpdate(PurchaseGoodsEntity);
//    }

//    public void saveOrUpdateFreshGoods(PurchaseGoodsEntity PurchaseGoodsEntity,
//                                       Double quantity, boolean saveUpdatedDate){
//        if (PurchaseGoodsEntity == null || quantity == null){
//           return;
//        }
//
//        PurchaseGoodsEntity.setQuantity(quantity);
//        if (saveUpdatedDate){
//            PurchaseGoodsEntity.setUpdatedDate(new Date());
//        }
//        saveOrUpdate(PurchaseGoodsEntity);
//    }

}

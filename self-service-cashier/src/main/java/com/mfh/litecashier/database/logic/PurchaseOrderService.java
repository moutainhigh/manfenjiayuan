package com.mfh.litecashier.database.logic;

import com.alibaba.fastjson.JSON;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.database.dao.PurchaseOrderDao;
import com.mfh.litecashier.database.entity.PurchaseOrderEntity;

import java.util.Date;
import java.util.List;

/**
 * <h1>采购订单购物车商品明细</h1><br>
 * <p>
 *
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PurchaseOrderService extends BaseService<PurchaseOrderEntity, String, PurchaseOrderDao> {

    @Override
    protected Class<PurchaseOrderDao> getDaoClass() {
        return PurchaseOrderDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PurchaseOrderService instance = null;
    /**
     * 返回 DailysettleService 实例
     * @return
     */
    public static PurchaseOrderService getInstance() {
        if (instance == null) {
            synchronized (PurchaseOrderService.class) {
                if (instance == null) {
                    instance = new PurchaseOrderService();
                }
            }
        }
        return instance;
    }

    public PurchaseOrderEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PurchaseOrderEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(PurchaseOrderEntity msg) {
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
    public List<PurchaseOrderEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<PurchaseOrderEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<PurchaseOrderEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<PurchaseOrderEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }public List<PurchaseOrderEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<PurchaseOrderEntity> queryAllBy(String strWhere) {
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
//    public List<PurchaseOrderEntity> getFreshGoodsList(){
//        return queryAllBy(String.format("purchaseType = '%d'",
//                PurchaseOrderEntity.PURCHASE_TYPE_FRESH));
//    }
//    public void clearFreshGoodsList(){
//        deleteBy(String.format("purchaseType = '%d'",
//                PurchaseOrderEntity.PURCHASE_TYPE_FRESH));
//    }
//
//    /**
//     * 获取指定批发商的商品列表*/
//    public List<PurchaseOrderEntity> getFreshGoodsList(Long providerId){
//        try{
//            StringBuilder sb = new StringBuilder();
//            sb.append(String.format("purchaseType = '%d'",
//                    PurchaseOrderEntity.PURCHASE_TYPE_FRESH));
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
     * 查询采购订单
     * */
    public PurchaseOrderEntity fetchOrder(Integer purchaseType, Long providerId){
        try{
            if (providerId == null){
                return null;
            }

            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d'",
                    purchaseType, providerId);
            List<PurchaseOrderEntity> entityList =  getDao().queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0){
                return entityList.get(0);
            }
        }catch (Exception e){
            ZLogger.e( e.toString());
        }

        return null;
    }

    /**
     * 查询订单
     * */
    public List<PurchaseOrderEntity> fetchOrders(Integer purchaseType){
        String sqlWhere = String.format("purchaseType = '%d'",
                purchaseType);
        return queryAllBy(sqlWhere);
    }
    public List<PurchaseOrderEntity> fetchOrders(Integer purchaseType, Long providerId){
        String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d'",
                purchaseType, providerId);
        return queryAllBy(sqlWhere);
    }

    /**
     * 保存生鲜商品
     * */
    public void addToShopcart(Integer purchaseType, PurchaseShopcartGoodsWrapper goods){
        PurchaseOrderEntity orderEntity = PurchaseOrderService.getInstance()
                .fetchOrder(purchaseType, goods.getSupplyId());
        if (orderEntity == null){
            orderEntity = new PurchaseOrderEntity();
            orderEntity.setCreatedDate(new Date());
            orderEntity.setPurchaseType(purchaseType);
            orderEntity.setProviderId(goods.getSupplyId());
        }
        orderEntity.setProviderName(goods.getSupplyName());
        orderEntity.setIsPrivate(goods.getIsPrivate());
        orderEntity.setUpdatedDate(new Date());
        PurchaseOrderService.getInstance().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("保存or更新采购订单：\n%s", JSON.toJSONString(orderEntity)));
    }


}

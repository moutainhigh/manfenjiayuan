package com.bingshanguxue.pda.database.service;

import com.bingshanguxue.pda.database.entity.InvCheckGoodsEntity;
import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.bingshanguxue.pda.database.dao.InvCheckGoodsDao;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 库存盘点
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvCheckGoodsService extends BaseService<InvCheckGoodsEntity, String, InvCheckGoodsDao> {
    @Override
    protected Class<InvCheckGoodsDao> getDaoClass() {
        return InvCheckGoodsDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static InvCheckGoodsService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static InvCheckGoodsService get() {
        String lsName = InvCheckGoodsService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new InvCheckGoodsService();//初始化登录服务
        }
        return instance;
    }

    public boolean entityExistById(String id) {
        try{
            return getDao().entityExistById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return false;
        }
    }

    public InvCheckGoodsEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(InvCheckGoodsEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(InvCheckGoodsEntity msg) {
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
    public List<InvCheckGoodsEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<InvCheckGoodsEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<InvCheckGoodsEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }
    public List<InvCheckGoodsEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllBy(strWhere, pageInfo);
    }
    public List<InvCheckGoodsEntity> queryAllByDesc(String strWhere) {
        return getDao().queryAllByDesc(strWhere);
    }

    public void deleteById(String id){
        try{
            getDao().deleteById(id);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }

    public void deleteBy(String strWhere){
        try{
            getDao().deleteBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }

    /**
     * 添加新商品
     * */
    public void addNewEntity(Long orderId, Long shelfNumber, ScGoodsSku productEntity,
                             Double quantity){
        if (StringUtils.isEmpty(orderId) || productEntity == null){
            return;
        }

        InvCheckGoodsEntity entity;
        // 不再对同一商品合并，只保留盘点纪录。
//        List<InvCheckGoodsEntity> entityList = queryAllBy(String.format("orderId = '%s' and barcode = '%s'", orderId, productEntity.getBarcode()));
////        List<InvCheckGoodsEntity> entityList = queryAllBy(String.format("orderId = '%s' and shelfNumber = '%d' and barcode = '%s'", orderId, shelfNumber, productEntity.getBarcode()));
//        if (entityList != null && entityList.size() > 0){
//            entity = entityList.get(0);
//            entity.setShelfNumber(shelfNumber);
//            entity.setUpdatedDate(new Date());
//        }
//        else{
            entity = new InvCheckGoodsEntity();

            entity.setOrderId(orderId);
            entity.setShelfNumber(shelfNumber);

            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
            entity.setUpdatedDate(new Date());

            entity.setGoodsId(productEntity.getId());
            entity.setProductId(productEntity.getProductId());
            entity.setProSkuId(productEntity.getProSkuId());
            entity.setBarcode(productEntity.getBarcode());
            entity.setName(productEntity.getSkuName());
            entity.setSpecNames(productEntity.getShortName());
//        }

        entity.setQuantityCheck(quantity);
        entity.setStatus(InvCheckGoodsEntity.STATUS_NONE);
        entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_INIT);
        entity.setUpdateHint(InvCheckGoodsEntity.HINT_MERGER);

        saveOrUpdate(entity);
    }
}

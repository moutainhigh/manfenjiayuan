package com.manfenjiayuan.pda_supermarket.database.logic;

import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.manfenjiayuan.pda_supermarket.database.dao.StockTakeDao;
import com.manfenjiayuan.pda_supermarket.database.entity.StockTakeEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.anlaysis.logger.ZLogger;
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
public class StockTakeService extends BaseService<StockTakeEntity, String, StockTakeDao> {
    @Override
    protected Class<StockTakeDao> getDaoClass() {
        return StockTakeDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static StockTakeService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static StockTakeService get() {
        String lsName = StockTakeService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new StockTakeService();//初始化登录服务
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

    public StockTakeEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(StockTakeEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(StockTakeEntity msg) {
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
    public List<StockTakeEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<StockTakeEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<StockTakeEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }
    public List<StockTakeEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllBy(strWhere, pageInfo);
    }
    public List<StockTakeEntity> queryAllByDesc(String strWhere) {
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

        StockTakeEntity entity;
        // 不再对同一商品合并，只保留盘点纪录。
//        List<StockTakeEntity> entityList = queryAllBy(String.format("orderId = '%s' and barcode = '%s'", orderId, productEntity.getBarcode()));
////        List<StockTakeEntity> entityList = queryAllBy(String.format("orderId = '%s' and shelfNumber = '%d' and barcode = '%s'", orderId, shelfNumber, productEntity.getBarcode()));
//        if (entityList != null && entityList.size() > 0){
//            entity = entityList.get(0);
//            entity.setShelfNumber(shelfNumber);
//            entity.setUpdatedDate(new Date());
//        }
//        else{
            entity = new StockTakeEntity();

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
        entity.setStatus(StockTakeEntity.STATUS_NONE);
        entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_INIT);
        entity.setUpdateHint(StockTakeEntity.HINT_MERGER);

        saveOrUpdate(entity);
    }
}

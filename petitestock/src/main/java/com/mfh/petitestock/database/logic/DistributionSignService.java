package com.mfh.petitestock.database.logic;

import com.manfenjiayuan.business.bean.InvSendIoOrderItem;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.petitestock.database.dao.DistributionSignDao;
import com.mfh.petitestock.database.entity.DistributionSignEntity;

import java.util.Date;
import java.util.List;

/**
 * POS--商品--签收
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DistributionSignService extends BaseService<DistributionSignEntity, String, DistributionSignDao> {
    @Override
    protected Class<DistributionSignDao> getDaoClass() {
        return DistributionSignDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static DistributionSignService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static DistributionSignService get() {
        String lsName = DistributionSignService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new DistributionSignService();//初始化登录服务
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

    public DistributionSignEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
           ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(DistributionSignEntity entity) {
        getDao().save(entity);
    }

    public void update(DistributionSignEntity entity) {
        getDao().update(entity);
    }

    public void saveOrUpdate(DistributionSignEntity entity) {
        getDao().saveOrUpdate(entity);
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
    public List<DistributionSignEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<DistributionSignEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<DistributionSignEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }
    public List<DistributionSignEntity> queryAllByDesc(String strWhere) {
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
     *  根据条码查商品
     *  */
    public  DistributionSignEntity queryEntityBy(String barcode){
        if (StringUtils.isEmpty(barcode)){
            return null;
        }

        List<DistributionSignEntity> entityList = queryAllBy(String.format("barcode = '%s'", barcode));
        if (entityList != null && entityList.size() > 0){
            return entityList.get(0);
        }

        return null;
    }

    /**
     * 添加新商品
     * */
    private void save(InvSendOrderItem productEntity){
        DistributionSignEntity entity = new DistributionSignEntity();
        entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        entity.setUpdatedDate(new Date());

        entity.setOrderId(productEntity.getOrderId());
        entity.setProductId(productEntity.getId());
        entity.setProSkuId(productEntity.getProSkuId());
        entity.setChainSkuId(productEntity.getChainSkuId());
        entity.setProductName(productEntity.getProductName());
        entity.setPrice(productEntity.getPrice());
        entity.setUnitSpec(productEntity.getUnit());
        if (ObjectsCompact.equals(productEntity.getUnit(), productEntity.getBuyUnit()) &&
                ObjectsCompact.equals(productEntity.getPriceType(), productEntity.getBuyPriceType())) {
            entity.setTotalCount(productEntity.getAskTotalCount());
        } else {
            entity.setTotalCount(0D);
        }
        if (entity.getTotalCount() == null || entity.getPrice() == null){
            entity.setAmount(0D);
        }
        else{
            entity.setAmount(entity.getPrice() * entity.getTotalCount());
        }

        entity.setBarcode(productEntity.getBarcode());
        entity.setProviderId(productEntity.getProviderId());
        entity.setIsPrivate(productEntity.getIsPrivate());

        entity.setQuantityCheck(entity.getTotalCount());
        entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }

    /**
     * 添加新商品
     * */
    private void save(InvSendIoOrderItem productEntity){
        DistributionSignEntity entity = new DistributionSignEntity();
        entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        entity.setUpdatedDate(new Date());

//        entity.setOrderId(productEntity.getOrderId());
        entity.setProductId(productEntity.getId());
        entity.setProSkuId(productEntity.getProSkuId());
        entity.setChainSkuId(productEntity.getChainSkuId());
        entity.setProductName(productEntity.getProductName());
        entity.setTotalCount(productEntity.getQuantityCheck());
        entity.setPrice(productEntity.getPrice());
        entity.setAmount(productEntity.getAmount());
        entity.setUnitSpec(productEntity.getUnitSpec());
        entity.setBarcode(productEntity.getBarcode());
        entity.setProviderId(productEntity.getProviderId());
        entity.setIsPrivate(productEntity.getIsPrivate());

        entity.setQuantityCheck(entity.getTotalCount());
        entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }

    /**
     * */
    public void save(ChainGoodsSku goods){
        if (goods == null){
            return;
        }

        DistributionSignEntity entity = queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new DistributionSignEntity();
        }
        entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        entity.setUpdatedDate(new Date());

//        entity.setOrderId(productEntity.getOrderId());
        entity.setProductId(goods.getId());
        entity.setProSkuId(goods.getProSkuId());
        entity.setChainSkuId(goods.getId());
        entity.setProductName(goods.getSkuName());
        entity.setTotalCount(1D);
        Double singleCostPrice = goods.getSingleCostPrice();
        entity.setPrice(singleCostPrice);
        if (singleCostPrice == null) {
            entity.setAmount(0D);
        } else {
            entity.setAmount(singleCostPrice * 1D);
        }

        entity.setUnitSpec(goods.getUnit());
        entity.setBarcode(goods.getBarcode());
        entity.setProviderId(goods.getTenantId());
        entity.setIsPrivate(IsPrivate.PLATFORM);

        entity.setQuantityCheck(entity.getTotalCount());
        entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }

    public void save(List<InvSendOrderItem> entityList){
        if (entityList != null && entityList.size() > 0){
            for (InvSendOrderItem entity : entityList){
                save(entity);
            }
        }
    }

    public void saveSendIoOrders(List<InvSendIoOrderItem> entityList){
        if (entityList != null && entityList.size() > 0){
            for (InvSendIoOrderItem entity : entityList){
                save(entity);
            }
        }
    }

    /**
     * 验收商品
     * */
    public void inspect(DistributionSignEntity entity, Double quantity){
        if(entity == null){
            return;
        }

        if (!entityExistById(String.valueOf(entity.getId()))){
            return;
        }

        if (quantity == null){
            quantity = 0D;
        }

        entity.setUpdatedDate(new Date());
        entity.setQuantityCheck(quantity);
        entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
        if (ObjectsCompact.equals(entity.getTotalCount(), quantity)){
            entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_OK);
        }
        else{
            if (quantity == 0){
                entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_REJECT);
            }
            else{
                entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_CONFLICT);
            }
        }

        update(entity);

    }

    /**
     * 拒收商品
     * */
    public void reject(DistributionSignEntity entity){
        if(entity == null){
            return;
        }

        if (entityExistById(String.valueOf(entity.getId()))){
            entity.setUpdatedDate(new Date());

            entity.setQuantityCheck(0D);
            entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_REJECT);

            update(entity);
        }
    }
}

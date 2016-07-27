package com.manfenjiayuan.pda_supermarket.database.logic;

import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.pda_supermarket.database.dao.InvReturnGoodsDao;
import com.manfenjiayuan.pda_supermarket.database.entity.InvReturnGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.core.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * POS--商品--签收
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InvReturnGoodsService extends BaseService<InvReturnGoodsEntity, String, InvReturnGoodsDao> {
    @Override
    protected Class<InvReturnGoodsDao> getDaoClass() {
        return InvReturnGoodsDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static InvReturnGoodsService instance = null;

    /**
     * 返回 IMConversationService 实例
     *
     * @return
     */
    public static InvReturnGoodsService get() {
        String lsName = InvReturnGoodsService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new InvReturnGoodsService();//初始化登录服务
        }
        return instance;
    }

    public boolean entityExistById(String id) {
        try {
            return getDao().entityExistById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return false;
        }
    }

    public InvReturnGoodsEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(InvReturnGoodsEntity entity) {
        getDao().save(entity);
    }

    public void update(InvReturnGoodsEntity entity) {
        getDao().update(entity);
    }

    public void saveOrUpdate(InvReturnGoodsEntity entity) {
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
    public List<InvReturnGoodsEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<InvReturnGoodsEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<InvReturnGoodsEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<InvReturnGoodsEntity> queryAllByDesc(String strWhere) {
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
     * 根据条码查商品
     */
    public InvReturnGoodsEntity queryEntityBy(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        List<InvReturnGoodsEntity> entityList = queryAllBy(String.format("barcode = '%s'", barcode));
        if (entityList != null && entityList.size() > 0) {
            return entityList.get(0);
        }

        return null;
    }

    /**
     * 保存采购订单明细
     */
    private void saveInvSendOrderItem(InvSendOrderItem productEntity) {
        InvReturnGoodsEntity entity = new InvReturnGoodsEntity();
        entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        entity.setUpdatedDate(new Date());

        entity.setOrderId(productEntity.getOrderId());
        entity.setProductId(productEntity.getId());
        entity.setProSkuId(productEntity.getProSkuId());
        entity.setChainSkuId(productEntity.getChainSkuId());
        entity.setProductName(productEntity.getProductName());
        entity.setUnitSpec(productEntity.getUnit());

        entity.setPrice(productEntity.getPrice());
        if (ObjectsCompact.equals(productEntity.getUnit(), productEntity.getBuyUnit()) &&
                ObjectsCompact.equals(productEntity.getPriceType(), productEntity.getBuyPriceType())) {
            entity.setTotalCount(productEntity.getAskTotalCount());
        } else {
            entity.setTotalCount(0D);
        }
        if (entity.getTotalCount() == null || entity.getPrice() == null) {
            entity.setAmount(0D);
        } else {
            entity.setAmount(entity.getPrice() * entity.getTotalCount());
        }

        entity.setBarcode(productEntity.getBarcode());
        entity.setProviderId(productEntity.getProviderId());
        entity.setIsPrivate(productEntity.getIsPrivate());

        entity.setQuantityCheck(entity.getTotalCount());
        entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }

    /**
     * 保存发货单明细
     */
    private void saveInvSendIoOrderItem(InvSendIoOrderItem productEntity) {
        InvReturnGoodsEntity entity = new InvReturnGoodsEntity();
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
        entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }


    /**
     * 保存发货单明细
     */
    public void saveSendOrderItems(List<InvSendOrderItem> entityList) {
        clear();
        if (entityList != null && entityList.size() > 0) {
            for (InvSendOrderItem entity : entityList) {
                saveInvSendOrderItem(entity);
            }
        }
    }

    /**
     * 保存采购单明细
     */
    public void saveSendIoOrdersItems(List<InvSendIoOrderItem> entityList) {
        clear();
        if (entityList != null && entityList.size() > 0) {
            for (InvSendIoOrderItem entity : entityList) {
                saveInvSendIoOrderItem(entity);
            }
        }
    }

    /**
     * 验收商品
     */
    public void inspect(InvReturnGoodsEntity entity, Double price, Double quantity) {
        if (entity == null) {
            return;
        }

        if (!entityExistById(String.valueOf(entity.getId()))) {
            return;
        }

        if (price != null){
            entity.setPrice(price);
        }

        if (quantity == null) {
            quantity = 0D;
        }

        entity.setQuantityCheck(quantity);
        if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
            entity.setAmount(0D);
        } else {
            entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
        }
        if (ObjectsCompact.equals(entity.getTotalCount(), quantity)) {
            entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_OK);
        } else {
            if (quantity == 0) {
                entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_REJECT);
            } else {
                entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_CONFLICT);
            }
        }
        entity.setUpdatedDate(new Date());
        saveOrUpdate(entity);
    }

    /**
     * 拒收商品
     */
    public void reject(InvReturnGoodsEntity entity) {
        if (entity == null) {
            return;
        }

        if (entityExistById(String.valueOf(entity.getId()))) {
            entity.setQuantityCheck(0D);
            if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
                entity.setAmount(0D);
            } else {
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_REJECT);
            entity.setUpdatedDate(new Date());
            saveOrUpdate(entity);
        }
    }
}

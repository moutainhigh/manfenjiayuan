package com.manfenjiayuan.pda_supermarket.database.logic;

import com.manfenjiayuan.business.bean.InvSendIoOrderItem;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.pda_supermarket.database.dao.DistributionSignDao;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
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
     *
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
        try {
            return getDao().entityExistById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return false;
        }
    }

    public DistributionSignEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
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
    public DistributionSignEntity queryEntityBy(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        List<DistributionSignEntity> entityList = queryAllBy(String.format("barcode = '%s'", barcode));
        if (entityList != null && entityList.size() > 0) {
            return entityList.get(0);
        }

        return null;
    }

    /**
     * 保存采购订单明细
     */
    private void saveInvSendOrderItem(InvSendOrderItem goods) {
        DistributionSignEntity entity = new DistributionSignEntity();
        entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        entity.setUpdatedDate(new Date());

        entity.setOrderId(goods.getOrderId());
        entity.setProductId(goods.getId());
        entity.setProSkuId(goods.getProSkuId());
        entity.setChainSkuId(goods.getChainSkuId());
        entity.setProductName(goods.getProductName());
        entity.setUnitSpec(goods.getUnit());

        entity.setSendPrice(goods.getPrice());
        if (ObjectsCompact.equals(goods.getUnit(), goods.getBuyUnit()) &&
                ObjectsCompact.equals(goods.getPriceType(), goods.getBuyPriceType())) {
            entity.setSendQuantity(goods.getAskTotalCount());
        } else {
            entity.setSendQuantity(0D);
        }
        if (entity.getSendQuantity() == null || entity.getSendPrice() == null) {
            entity.setSendAmount(0D);
        } else {
            entity.setSendAmount(entity.getSendPrice() * entity.getSendQuantity());
        }
        entity.setReceiveQuantity(entity.getSendQuantity());
        entity.setReceiveAmount(entity.getSendAmount());
        if (entity.getReceiveQuantity() == 0) {
            entity.setReceivePrice(0D);
        } else {
            entity.setReceivePrice(entity.getReceiveAmount() / entity.getReceiveQuantity());
        }

        entity.setBarcode(goods.getBarcode());
        entity.setProviderId(goods.getProviderId());
        entity.setIsPrivate(goods.getIsPrivate());

        entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }

    /**
     * 保存发货单明细
     */
    private void saveInvSendIoOrderItem(InvSendIoOrderItem goods) {
        DistributionSignEntity entity = new DistributionSignEntity();
        entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        entity.setUpdatedDate(new Date());

//        entity.setOrderId(goods.getOrderId());
        entity.setProductId(goods.getId());
        entity.setProSkuId(goods.getProSkuId());
        entity.setChainSkuId(goods.getChainSkuId());
        entity.setProductName(goods.getProductName());
        entity.setUnitSpec(goods.getUnitSpec());
        entity.setBarcode(goods.getBarcode());
        entity.setProviderId(goods.getProviderId());
        entity.setIsPrivate(goods.getIsPrivate());

        entity.setSendQuantity(goods.getQuantityCheck());
        entity.setSendAmount(goods.getAmount());
        entity.setSendPrice(goods.getPrice());
        entity.setReceiveQuantity(entity.getSendQuantity());
        entity.setReceiveAmount(entity.getSendAmount());
        if (entity.getReceiveQuantity() == 0) {
            entity.setReceivePrice(0D);
        } else {
            entity.setReceivePrice(entity.getReceiveAmount() / entity.getReceiveQuantity());
        }
        entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_NONE);

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
    public void inspect(DistributionSignEntity entity, Double amount, Double quantity) {
        if (entity == null) {
            return;
        }

        if (!entityExistById(String.valueOf(entity.getId()))) {
            return;
        }

        if (amount == null) {
            amount = 0D;
        }
        entity.setReceiveAmount(amount);

        if (quantity == null) {
            quantity = 0D;
        }
        entity.setReceiveQuantity(quantity);

        if (entity.getReceiveQuantity() == 0) {
            entity.setReceivePrice(0D);
        } else {
            entity.setReceivePrice(entity.getReceiveAmount() / entity.getReceiveQuantity());
        }

        if (ObjectsCompact.equals(entity.getSendQuantity(), quantity)) {
            entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_OK);
        } else {
            if (quantity == 0) {
                entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_REJECT);
            } else {
                entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_CONFLICT);
            }
        }
        entity.setUpdatedDate(new Date());
        saveOrUpdate(entity);
    }

    /**
     * 拒收商品
     */
    public void reject(DistributionSignEntity entity) {
        if (entity == null) {
            return;
        }

        if (entityExistById(String.valueOf(entity.getId()))) {
            entity.setReceiveQuantity(0D);
            entity.setReceiveAmount(0D);
            entity.setReceivePrice(0D);
            entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_REJECT);
            entity.setUpdatedDate(new Date());
            saveOrUpdate(entity);
        }

    }
}

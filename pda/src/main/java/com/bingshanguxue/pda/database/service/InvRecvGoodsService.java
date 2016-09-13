package com.bingshanguxue.pda.database.service;

import com.bingshanguxue.pda.database.dao.InvRecvGoodsDao;
import com.bingshanguxue.pda.database.entity.InvRecvGoodsEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
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
public class InvRecvGoodsService extends BaseService<InvRecvGoodsEntity, String, InvRecvGoodsDao> {
    @Override
    protected Class<InvRecvGoodsDao> getDaoClass() {
        return InvRecvGoodsDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static InvRecvGoodsService instance = null;

    /**
     * 返回 IMConversationService 实例
     *
     * @return
     */
    public static InvRecvGoodsService get() {
        String lsName = InvRecvGoodsService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new InvRecvGoodsService();//初始化登录服务
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

    public InvRecvGoodsEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(InvRecvGoodsEntity entity) {
        getDao().save(entity);
    }

    public void update(InvRecvGoodsEntity entity) {
        getDao().update(entity);
    }

    public void saveOrUpdate(InvRecvGoodsEntity entity) {
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
    public List<InvRecvGoodsEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<InvRecvGoodsEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<InvRecvGoodsEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<InvRecvGoodsEntity> queryAllByDesc(String strWhere) {
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
    public InvRecvGoodsEntity queryEntityByBarcode(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        List<InvRecvGoodsEntity> entityList = queryAllBy(String.format("barcode = '%s'", barcode));
        if (entityList != null && entityList.size() > 0) {
            return entityList.get(0);
        }

        return null;
    }

    /**
     * 保存采购订单明细
     */
    public void saveInvSendOrderItem(InvSendOrderItem goods) {
        InvRecvGoodsEntity entity = queryEntityByBarcode(goods.getBarcode());
        if (entity == null){
            entity = new InvRecvGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
            entity.setBarcode(goods.getBarcode());
        }
        entity.setUpdatedDate(new Date());
        entity.setProductName(goods.getProductName());
        entity.setUnit(goods.getUnit());

        entity.setReceiptPrice(goods.getPrice());
        // 判断如果采购单位&采购计价类型 和销售单位&销售计价类型不一致，则需要重新输入商品数量
        if (ObjectsCompact.equals(goods.getUnit(), goods.getBuyUnit()) &&
                ObjectsCompact.equals(goods.getPriceType(), goods.getBuyPriceType())) {
            entity.setReceiptQuantity(goods.getAskTotalCount());
        } else {
            entity.setReceiptQuantity(null);
        }
        entity.setReceiveQuantity(entity.getReceiptQuantity());
        entity.setReceivePrice(entity.getReceiptPrice());//默认价格为发货价格
        entity.setReceiveAmount(entity.getReceiveQuantity() * entity.getReceivePrice());

        entity.setProSkuId(goods.getProSkuId());
        entity.setChainSkuId(goods.getChainSkuId());
        entity.setProviderId(goods.getProviderId());
        entity.setIsPrivate(goods.getIsPrivate());

        entity.setInspectStatus(InvRecvGoodsEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }

    /**
     * 保存发货单明细
     */
    public void saveSendOrderItems(List<InvSendOrderItem> entityList) {
//        clear();
        if (entityList != null && entityList.size() > 0) {
            for (InvSendOrderItem entity : entityList) {
                saveInvSendOrderItem(entity);
            }
        }
    }

    /**
     * 保存发货单明细
     */
    public void saveInvSendIoOrderItem(InvSendIoOrderItem goods) {
        InvRecvGoodsEntity entity = queryEntityByBarcode(goods.getBarcode());
        if (entity == null){
            entity = new InvRecvGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
            entity.setBarcode(goods.getBarcode());
        }
        entity.setUpdatedDate(new Date());

        entity.setProSkuId(goods.getProSkuId());
        entity.setChainSkuId(goods.getChainSkuId());
        entity.setProductName(goods.getProductName());
        entity.setUnit(goods.getUnitSpec());
        entity.setProviderId(goods.getProviderId());
        entity.setIsPrivate(goods.getIsPrivate());

        entity.setReceiptQuantity(goods.getQuantityCheck());
        entity.setReceiptPrice(goods.getPrice());
        entity.setReceiveQuantity(entity.getReceiptQuantity());
        entity.setReceivePrice(entity.getReceiptPrice());
        entity.setReceiveAmount(entity.getReceiveQuantity() * entity.getReceivePrice());
        entity.setInspectStatus(InvRecvGoodsEntity.INSPECT_STATUS_NONE);

        saveOrUpdate(entity);
    }

    /**
     * 保存采购单明细
     */
    public void saveSendIoOrderItems(List<InvSendIoOrderItem> entityList) {
//        clear();
        if (entityList != null && entityList.size() > 0) {
            for (InvSendIoOrderItem entity : entityList) {
                saveInvSendIoOrderItem(entity);
            }
        }
    }

    /**
     * 验收商品
     */
    public void inspect(InvRecvGoodsEntity entity, Double amount, Double quantity) {
        if (entity == null) {
            return;
        }

        entity.setReceiveAmount(amount);
        entity.setReceiveQuantity(quantity);

        if (entity.getReceiveQuantity() == 0) {
            entity.setReceivePrice(0D);
        } else {
            entity.setReceivePrice(entity.getReceiveAmount() / entity.getReceiveQuantity());
        }

        if (ObjectsCompact.equals(entity.getReceiptQuantity(), quantity)) {
            entity.setInspectStatus(InvRecvGoodsEntity.INSPECT_STATUS_OK);
        } else {
            if (quantity == 0) {
                entity.setInspectStatus(InvRecvGoodsEntity.INSPECT_STATUS_REJECT);
            } else {
                entity.setInspectStatus(InvRecvGoodsEntity.INSPECT_STATUS_CONFLICT);
            }
        }
        entity.setUpdatedDate(new Date());
        saveOrUpdate(entity);
    }

    /**
     * 拒收商品
     */
    public void reject(InvRecvGoodsEntity entity) {
        if (entity == null) {
            return;
        }

        if (entityExistById(String.valueOf(entity.getId()))) {
            entity.setReceiveQuantity(0D);
            entity.setReceiveAmount(0D);
            entity.setReceivePrice(0D);
            entity.setInspectStatus(InvRecvGoodsEntity.INSPECT_STATUS_REJECT);
            entity.setUpdatedDate(new Date());
            saveOrUpdate(entity);
        }

    }
}

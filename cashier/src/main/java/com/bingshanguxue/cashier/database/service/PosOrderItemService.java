package com.bingshanguxue.cashier.database.service;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.dao.PosOrderItemDao;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.Date;
import java.util.List;

/**
 * POS--销售订单明细
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosOrderItemService extends BaseService<PosOrderItemEntity, String, PosOrderItemDao> {
    @Override
    protected Class<PosOrderItemDao> getDaoClass() {
        return PosOrderItemDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PosOrderItemService instance = null;
    /**
     * 返回 PosOrderItemService 实例
     * @return
     */
    public static PosOrderItemService get() {
        if (instance == null) {
            synchronized (PosOrderItemService.class) {
                if (instance == null) {
                    instance = new PosOrderItemService();
                }
            }
        }
        return instance;
    }

    public PosOrderItemEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosOrderItemEntity entity) {
        getDao().save(entity);
    }

    public void saveOrUpdate(PosOrderItemEntity entity) {
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
    public List<PosOrderItemEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<PosOrderItemEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }
    public List<PosOrderItemEntity> queryAllByDesc(String strWhere) {
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
     * 新增订单明细
     * */
    public void saveOrUpdate(String orderBarCode, Long orderId, CashierShopcartEntity goods){
        if (goods == null){
            ZLogger.d("商品无效");
            return;
        }

        PosOrderItemEntity entity;

        String sqlWhere = String.format("orderBarCode = '%s' and orderId = '%d' and barcode = '%s'",
                orderBarCode, orderId, goods.getBarcode());
        List<PosOrderItemEntity> orderItemEntities = queryAllBy(sqlWhere);
        if (orderItemEntities != null && orderItemEntities.size() > 0){
            entity =  orderItemEntities.get(0);
        }
        else{
            entity = new PosOrderItemEntity();
            entity.setOrderBarCode(orderBarCode);
            entity.setOrderId(orderId);
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProdLineId(goods.getProdLineId());
            entity.setBarcode(goods.getBarcode());
            entity.setGoodsId(goods.getGoodsId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setProductId(goods.getProductId());
            entity.setName(goods.getName());
            entity.setSkuName(goods.getSkuName());
            entity.setShortName(goods.getShortName());
            entity.setUnit(goods.getUnit());
            entity.setProviderId(goods.getProviderId());
            entity.setPriceType(goods.getPriceType());
            entity.setNeedWait(goods.getNeedWait());
        }

        entity.setBcount(goods.getBcount());
        entity.setCostPrice(goods.getCostPrice());
        entity.setCustomerPrice(goods.getCustomerPrice());
        entity.setFinalPrice(goods.getFinalPrice());
        //标准金额
        entity.setAmount(entity.getBcount() * goods.getCostPrice());
        //成交金额
        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
        entity.setUpdatedDate(new Date());

        saveOrUpdate(entity);
        ZLogger.d(String.format("保存or更新订单明细:%s\n%s",
                goods.getBarcode(), JSON.toJSONString(entity)));
    }


}

package com.bingshanguxue.cashier.database.service;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.dao.CashierShopcartDao;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * POS--销售订单明细
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class CashierShopcartService extends BaseService<CashierShopcartEntity, String, CashierShopcartDao> {
    @Override
    protected Class<CashierShopcartDao> getDaoClass() {
        return CashierShopcartDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static CashierShopcartService instance = null;

    /**
     * 返回 CashierShopcartService 实例
     *
     * @return
     */
    public static CashierShopcartService getInstance() {
        if (instance == null) {
            synchronized (CashierShopcartService.class) {
                if (instance == null) {
                    instance = new CashierShopcartService();
                }
            }
        }
        return instance;
    }

    public CashierShopcartEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(CashierShopcartEntity entity) {
        getDao().save(entity);
    }

    public void saveOrUpdate(CashierShopcartEntity entity) {
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
    public List<CashierShopcartEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<CashierShopcartEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<CashierShopcartEntity> queryAllByDesc(String strWhere) {
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
     * 添加新商品
     * */
    public void append(String orderBarCode, PosProductEntity goods,
                             Double bCount) {
        if (StringUtils.isEmpty(orderBarCode) || goods == null) {
            ZLogger.d("参数无效");
            return;
        }

        String sqlWhere = String.format("posTradeNo = '%s' and barcode = '%s'",
                orderBarCode, goods.getBarcode());

        CashierShopcartEntity shopcartEntity;
        List<CashierShopcartEntity> shopcartEntities = queryAllBy(sqlWhere);
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            shopcartEntity = shopcartEntities.get(0);

            shopcartEntity.setBcount(shopcartEntity.getBcount() + bCount);
        } else {
            shopcartEntity = new CashierShopcartEntity();
            shopcartEntity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
            shopcartEntity.setPosTradeNo(orderBarCode);
            shopcartEntity.setBcount(bCount);
            shopcartEntity.setGoodsId(goods.getId());
            shopcartEntity.setProSkuId(goods.getProSkuId());
            shopcartEntity.setBarcode(goods.getBarcode());
            shopcartEntity.setProductId(goods.getProductId());
            shopcartEntity.setName(goods.getName());
            shopcartEntity.setUnit(goods.getUnit());
            shopcartEntity.setCostPrice(goods.getCostPrice());
            shopcartEntity.setProviderId(goods.getProviderId());
            shopcartEntity.setUnit(goods.getUnit());
            shopcartEntity.setPriceType(goods.getPriceType());
            shopcartEntity.setCateType(goods.getCateType());

            //默认会员价使用标准单价计算，可以在后面售价时修改。
            shopcartEntity.setFinalPrice(goods.getCostPrice());
        }

        //标准价金额
        shopcartEntity.setAmount(shopcartEntity.getBcount() * goods.getCostPrice());
        //成交金额
        shopcartEntity.setFinalAmount(shopcartEntity.getBcount() * shopcartEntity.getFinalPrice());

        shopcartEntity.setUpdatedDate(new Date());

        saveOrUpdate(shopcartEntity);
    }

    /**
     * 加载订单明细
     * */
    public void readOrderItems(String orderBarCode, List<PosOrderItemEntity> itemEntities){
        //清空购物车中旧数据
        CashierShopcartService.getInstance().deleteBy(String.format("posTradeNo = '%s'", orderBarCode));

        if (itemEntities == null || itemEntities.size() <= 0){
            return;
        }

        for (PosOrderItemEntity goods : itemEntities){
            String sqlWhere = String.format("posTradeNo = '%s' and barcode = '%s'",
                    orderBarCode, goods.getBarcode());

            CashierShopcartEntity shopcartEntity;
            List<CashierShopcartEntity> shopcartEntities = queryAllBy(sqlWhere);
            if (shopcartEntities != null && shopcartEntities.size() > 0) {
                shopcartEntity = shopcartEntities.get(0);

                shopcartEntity.setBcount(shopcartEntity.getBcount() + goods.getBcount());
            } else {
                shopcartEntity = new CashierShopcartEntity();
                shopcartEntity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
                shopcartEntity.setPosTradeNo(orderBarCode);
                shopcartEntity.setBcount(goods.getBcount());
                shopcartEntity.setGoodsId(goods.getId());
                shopcartEntity.setProSkuId(goods.getProSkuId());
                shopcartEntity.setBarcode(goods.getBarcode());
                shopcartEntity.setProductId(goods.getProductId());
                shopcartEntity.setName(goods.getName());
                shopcartEntity.setUnit(goods.getUnit());
                shopcartEntity.setProviderId(goods.getProviderId());
                shopcartEntity.setUnit(goods.getUnit());
                shopcartEntity.setPriceType(goods.getPriceType());
                shopcartEntity.setCateType(goods.getCateType());

                shopcartEntity.setCostPrice(goods.getCostPrice());
                shopcartEntity.setFinalPrice(goods.getFinalPrice());
            }

            //标准价金额
            shopcartEntity.setAmount(shopcartEntity.getBcount() * goods.getCostPrice());
            //成交金额
            shopcartEntity.setFinalAmount(shopcartEntity.getBcount() * shopcartEntity.getFinalPrice());

            shopcartEntity.setUpdatedDate(new Date());

            saveOrUpdate(shopcartEntity);
            ZLogger.d(JSONObject.toJSONString(shopcartEntity));
        }
    }

}

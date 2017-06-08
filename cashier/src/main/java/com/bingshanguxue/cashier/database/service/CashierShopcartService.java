package com.bingshanguxue.cashier.database.service;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.dao.CashierShopcartDao;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

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
     * 添加新商品到收银台
     *
     * @param orderBarCode
     * @param goods
     * @param bCount
     * @param
     */
    public void append(String orderBarCode, PosProductEntity goods,
                       Double bCount) {
        append(orderBarCode, 100D, goods, bCount);
    }

    /**
     * 添加新商品到收银台
     *
     * @param orderBarCode  订单条码
     * @param orderDiscount 订单折扣
     * @param goods         商品
     * @param bCount        商品数量
     */
    public void append(String orderBarCode, Double orderDiscount, PosProductEntity goods,
                       Double bCount) {
        if (StringUtils.isEmpty(orderBarCode)) {
            ZLogger.d("订单条码不能为空");
            return;
        }
        if (StringUtils.isEmpty(orderBarCode) || goods == null) {
            ZLogger.d("商品无效");
            return;
        }

        if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
            ZLogger.d(String.format("添加商品到收银台:\n"
                            + "orderBarcode=%s\n"
                            + "orderDiscount=%.0f%%\n"
                            + "bCount=%.3f\n"
                            + "goods=%s",
                    orderBarCode, orderDiscount, bCount, JSONObject.toJSONString(goods)));
        }
        String sqlWhere = String.format("posTradeNo = '%s' and barcode = '%s'",
                orderBarCode, goods.getBarcode());

        Date rightNow = TimeUtil.getCurrentDate();
        CashierShopcartEntity shopcartEntity;
        List<CashierShopcartEntity> shopcartEntities = queryAllBy(sqlWhere);
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            shopcartEntity = shopcartEntities.get(0);

            shopcartEntity.setBcount(shopcartEntity.getBcount() + bCount);
        } else {
            shopcartEntity = new CashierShopcartEntity();
            shopcartEntity.setCreatedDate(rightNow);//使用当前日期，表示加入购物车信息
            shopcartEntity.setPosTradeNo(orderBarCode);
            shopcartEntity.setBcount(bCount);
            shopcartEntity.setGoodsId(goods.getId());
            shopcartEntity.setProSkuId(goods.getProSkuId());
            shopcartEntity.setBarcode(goods.getBarcode());
            shopcartEntity.setProductId(goods.getProductId());
            shopcartEntity.setName(goods.getName());
            shopcartEntity.setShortName(goods.getShortName());
            shopcartEntity.setSkuName(goods.getSkuName());
            shopcartEntity.setUnit(goods.getUnit());
            shopcartEntity.setProviderId(goods.getProviderId());
            shopcartEntity.setPriceType(goods.getPriceType());
            shopcartEntity.setProdLineId(goods.getProdLineId());
            shopcartEntity.setNeedWait(goods.getNeedWait());

            shopcartEntity.setCostPrice(goods.getCostPrice());
            shopcartEntity.setCustomerPrice(goods.getCustomerPrice());
            if (orderDiscount != null) {
                shopcartEntity.setFinalPrice(MathCompact.mult(goods.getCostPrice(), orderDiscount / 100));
                shopcartEntity.setFinalCustomerPrice(MathCompact.mult(goods.getCustomerPrice(), orderDiscount / 100));
            } else {
                shopcartEntity.setFinalPrice(goods.getCostPrice());
                shopcartEntity.setFinalCustomerPrice(goods.getCustomerPrice());
            }
        }

        //标准价金额
        shopcartEntity.setAmount(shopcartEntity.getBcount() * shopcartEntity.getCostPrice());
        //成交金额
        shopcartEntity.setFinalAmount(shopcartEntity.getBcount() * shopcartEntity.getFinalPrice());
        shopcartEntity.setUpdatedDate(rightNow);

        saveOrUpdate(shopcartEntity);
    }

    public CashierShopcartEntity getEntityBy(String sqlWhere) {
        CashierShopcartEntity shopcartEntity = null;
        List<CashierShopcartEntity> shopcartEntities = queryAllBy(sqlWhere);
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            shopcartEntity = shopcartEntities.get(0);
        }

        return shopcartEntity;
    }

    /**
     * 加载订单明细
     */
    public void readOrderItems(String orderBarCode, List<PosOrderItemEntity> itemEntities) {
        //清空购物车中旧数据
        CashierShopcartService.getInstance()
                .deleteBy(String.format("posTradeNo = '%s'", orderBarCode));

        if (itemEntities == null || itemEntities.size() <= 0) {
            return;
        }

        for (PosOrderItemEntity goods : itemEntities) {
            String sqlWhere = String.format("posTradeNo = '%s' and barcode = '%s'",
                    orderBarCode, goods.getBarcode());

            Date rightNow = TimeUtil.getCurrentDate();

            CashierShopcartEntity shopcartEntity;
            List<CashierShopcartEntity> shopcartEntities = queryAllBy(sqlWhere);
            if (shopcartEntities != null && shopcartEntities.size() > 0) {
                shopcartEntity = shopcartEntities.get(0);

                shopcartEntity.setBcount(shopcartEntity.getBcount() + goods.getBcount());
            } else {
                shopcartEntity = new CashierShopcartEntity();
                shopcartEntity.setCreatedDate(rightNow);//使用当前日期，表示加入购物车信息
                shopcartEntity.setPosTradeNo(orderBarCode);
                shopcartEntity.setBcount(goods.getBcount());
                shopcartEntity.setGoodsId(goods.getId());
                shopcartEntity.setProSkuId(goods.getProSkuId());
                shopcartEntity.setBarcode(goods.getBarcode());
                shopcartEntity.setProductId(goods.getProductId());
                shopcartEntity.setName(goods.getName());
                shopcartEntity.setSkuName(goods.getSkuName());
                shopcartEntity.setShortName(goods.getShortName());
                shopcartEntity.setUnit(goods.getUnit());
                shopcartEntity.setProviderId(goods.getProviderId());
                shopcartEntity.setUnit(goods.getUnit());
                shopcartEntity.setPriceType(goods.getPriceType());
                shopcartEntity.setProdLineId(goods.getProdLineId());
                shopcartEntity.setNeedWait(goods.getNeedWait());

                shopcartEntity.setCostPrice(goods.getCostPrice());
                shopcartEntity.setCustomerPrice(goods.getCustomerPrice());
                shopcartEntity.setFinalPrice(goods.getFinalPrice());
                shopcartEntity.setFinalCustomerPrice(goods.getFinalCustomerPrice());
            }

            //标准价金额
            shopcartEntity.setAmount(shopcartEntity.getBcount() * goods.getCostPrice());
            //成交金额
            shopcartEntity.setFinalAmount(shopcartEntity.getBcount() * shopcartEntity.getFinalPrice());
            shopcartEntity.setUpdatedDate(rightNow);

            saveOrUpdate(shopcartEntity);
            ZLogger.d(JSONObject.toJSONString(shopcartEntity));
        }
    }

    /**
     * 批量打折
     */
    public void batchDiscount(String posTradeNo, Double discount) {
        List<CashierShopcartEntity> entities = queryAllBy(String.format("posTradeNo = '%s'", posTradeNo));
        if (entities != null && entities.size() > 0) {
            for (CashierShopcartEntity entity : entities) {
                entity.setFinalPrice(MathCompact.mult(entity.getCostPrice(), discount / 100));
                entity.setFinalCustomerPrice(MathCompact.mult(entity.getCustomerPrice(), discount / 100));
                entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
                saveOrUpdate(entity);
            }
        }
    }

}

package com.mfh.litecashier.database.logic;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.mfh.litecashier.database.dao.CommonlyGoodsDao;
import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;

import java.util.Date;
import java.util.List;

/**
 * POS--商品--库存
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class CommonlyGoodsService extends BaseService<CommonlyGoodsEntity, String, CommonlyGoodsDao> {

    private static CommonlyGoodsService instance = null;

    /**
     * 返回 CommonlyGoodsService 实例
     *
     * @return
     */
    public static CommonlyGoodsService get() {
//        String lsName = CommonlyGoodsService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new CommonlyGoodsService();//初始化登录服务
//        }
        if (instance == null) {
            synchronized (CommonlyGoodsService.class) {
                if (instance == null) {
                    instance = new CommonlyGoodsService();
                }
            }
        }
        return instance;
    }


    @Override
    protected Class<CommonlyGoodsDao> getDaoClass() {
        return CommonlyGoodsDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    public CommonlyGoodsEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(CommonlyGoodsEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(CommonlyGoodsEntity entity) {
        getDao().saveOrUpdate(entity);
    }

    public void saveOrUpdate(Long categoryId, ScGoodsSku goods) {
        try {
            if (goods == null) {
                return;
            }

            CommonlyGoodsEntity entity;
            List<CommonlyGoodsEntity> entityList = queryAllByDesc(String.format("barcode = '%s'", goods.getBarcode()));
            if (entityList != null && entityList.size() > 0) {
                entity = entityList.get(0);
//                entity.setCreatedDate(new Date());
                entity.setUpdatedDate(new Date());
                //有可能有更新
                entity.setName(goods.getSkuName());
                entity.setImgUrl(goods.getImgUrl());
                entity.setCategoryId(categoryId);
                entity.setPriceType(goods.getPriceType());
            } else {
                entity = new CommonlyGoodsEntity();
                entity.setId(goods.getId());//商品主键
                entity.setCreatedDate(new Date());
                entity.setUpdatedDate(new Date());
                entity.setProSkuId(goods.getProSkuId());
                entity.setBarcode(goods.getBarcode());
                entity.setProductId(goods.getProductId());
                entity.setName(goods.getSkuName());
                entity.setUnit(goods.getUnit());
                entity.setCostPrice(goods.getCostPrice());
                entity.setQuantity(goods.getQuantity());
                entity.setTenantId(goods.getTenantId());
                entity.setProviderId(goods.getProviderId());
                entity.setImgUrl(goods.getImgUrl());
                entity.setCategoryId(categoryId);
                entity.setPriceType(goods.getPriceType());
//            entity.setStatus(goods.getStatus());
            }

            ZLogger.d(String.format("saveOrUpdate:categoryId = %d, barCode:%s", categoryId, goods.getBarcode()));

            saveOrUpdate(entity);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public void saveOrUpdate(Long categoryId, ChainGoodsSku goods) {
        try {
            if (goods == null) {
                return;
            }

            CommonlyGoodsEntity entity;
            List<CommonlyGoodsEntity> entityList = queryAllByDesc(String.format("barcode = '%s'", goods.getBarcode()));
            if (entityList != null && entityList.size() > 0) {
                entity = entityList.get(0);
                entity.setUpdatedDate(new Date());
                //有可能有更新
                entity.setCategoryId(categoryId);
                entity.setName(goods.getSkuName());
                entity.setImgUrl(goods.getImgUrl());
                entity.setPriceType(goods.getPriceType());
            } else {
                entity = new CommonlyGoodsEntity();
                entity.setId(goods.getId());//商品主键
                entity.setCreatedDate(new Date());
                entity.setUpdatedDate(new Date());
                entity.setCategoryId(categoryId);
                entity.setProSkuId(goods.getProSkuId());
                entity.setBarcode(goods.getBarcode());
                entity.setProductId(goods.getProductId());
                entity.setName(goods.getSkuName());
                entity.setUnit(goods.getUnit());
                entity.setCostPrice(goods.getCostPrice());
                entity.setQuantity(goods.getQuantity());
                entity.setTenantId(goods.getTenantId());
                entity.setProviderId(goods.getTenantId());
                entity.setImgUrl(goods.getImgUrl());
                entity.setPriceType(goods.getPriceType());
                entity.setStatus(goods.getStatus());
            }

            ZLogger.d(String.format("saveOrUpdate:categoryId = %d, barCode:%s",
                    categoryId, goods.getBarcode()));

            saveOrUpdate(entity);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
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
    public List<CommonlyGoodsEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<CommonlyGoodsEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<CommonlyGoodsEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<CommonlyGoodsEntity> queryAllByDesc(String strWhere) {
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

}

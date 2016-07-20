package com.mfh.litecashier.database.logic;


import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.litecashier.bean.PosGoods;
import com.mfh.litecashier.database.dao.PosProductDao;
import com.mfh.litecashier.database.entity.PosProductEntity;

import java.util.List;

/**
 * POS--商品--库存
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosProductService extends BaseService<PosProductEntity, String, PosProductDao> {
    private static PosProductService instance = null;

    /**
     * 返回 PosProductService 实例
     *
     * @return
     */
    public static PosProductService get() {
        if (instance == null) {
            synchronized (PosProductService.class) {
                if (instance == null) {
                    instance = new PosProductService();
                }
            }
        }
        return instance;
    }


    @Override
    protected Class<PosProductDao> getDaoClass() {
        return PosProductDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }


    public PosProductEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosProductEntity entity) {
        getDao().save(entity);
    }

    public void saveOrUpdate(PosProductEntity entity) {
        getDao().saveOrUpdate(entity);
    }

    /**
     * 保存商品
     */
    public void saveOrUpdate(PosGoods posGoods) {
        try {
            if (posGoods == null || posGoods.getId() == null) {
                ZLogger.d("保存POS商品库失败：商品参数无效。");
                return;
            }

            Long id = posGoods.getId();
            PosProductEntity entity = getEntityById(String.valueOf(id));
            if (entity == null) {
                entity = new PosProductEntity();
                entity.setId(id);
            }
            //更新商品信息
            entity.setCreatedDate(posGoods.getCreatedDate());
            entity.setUpdatedDate(posGoods.getUpdatedDate());//使用商品的更新日期

            entity.setProSkuId(posGoods.getProSkuId());
            entity.setBarcode(posGoods.getBarcode());
            entity.setProductId(posGoods.getProductId());
            entity.setName(posGoods.getName());
            entity.setUnit(posGoods.getUnit());
            entity.setCostPrice(posGoods.getCostPrice());
            entity.setQuantity(posGoods.getQuantity());
            entity.setTenantId(posGoods.getTenantId());
            entity.setProviderId(posGoods.getProviderId());
            entity.setStatus(posGoods.getStatus());
            entity.setPriceType(posGoods.getPriceType());
            entity.setPackageNum(posGoods.getPackageNum());
            entity.setProcateId(posGoods.getProcateId());
            entity.setCateType(posGoods.getCateType());

            getDao().saveOrUpdate(entity);
        } catch (Exception e) {
            ZLogger.ef(String.format("保存POS商品库失败：%s", e.toString()));
        }
    }

    /**
     * 清空历史记录
     */
    public void clear() {
        getDao().deleteAll();
    }

    public List<PosProductEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<PosProductEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<PosProductEntity> queryAllBy(String strWhere, String orderBy, PageInfo pageInfo) {
        return getDao().queryAllBy(strWhere, orderBy, pageInfo);
    }

    public List<PosProductEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }
    public List<PosProductEntity> queryAllByDesc(String strWhere) {
        return getDao().queryAllByDesc(strWhere);
    }

    /**
     * 逐条删除
     */
    public void deleteById(String id) {
        try {
            getDao().deleteById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }
}

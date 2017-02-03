package com.bingshanguxue.cashier.database.service;


import com.bingshanguxue.cashier.database.dao.PosProductDao;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.PosGoods;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.PinyinUtils;
import com.mfh.framework.login.logic.MfhLoginService;

import net.tsz.afinal.db.table.KeyValue;

import java.util.ArrayList;
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
     * 清空历史记录
     */
    public void clear() {
        getDao().deleteAll();
    }

    public List<PosProductEntity> queryAll(String strWhere, PageInfo pageInfo) {
        return getDao().queryAll(strWhere, pageInfo);
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

    public void deleteBy(String strWhere) {
        try {
            getDao().deleteBy(strWhere);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 查询本地商品库搜索商品
     * @param barcode 商品条码
     * @return PosProductEntity 如果找到多个返回第一个商品；没有找到返回null.
     * */
    public PosProductEntity findGoods(String barcode) {
        //注意，这里的租户默认是当前登录租户
        List<PosProductEntity> entities = PosProductService.get()
                .queryAllByDesc(String.format("barcode = '%s' and tenantId = '%d'",
                        barcode, MfhLoginService.get().getSpid()));
        if (entities != null && entities.size() > 0) {
            PosProductEntity goods = entities.get(0);
            ZLogger.d(String.format("找到%d个商品:%s[%s]",
                    entities.size(), barcode, goods.getSkuName()));
            return goods;
        }
        else{
            ZLogger.d(String.format("未找到商品:%s", barcode));
        }

        return null;
    }

    /**
     * 保存商品档案
     * */
    public void saveOrUpdate(PosGoods posGoods){
        Long id = posGoods.getId();
        PosProductEntity entity = PosProductService.get().getEntityById(String.valueOf(id));
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
        entity.setSkuName(posGoods.getSkuName());
        entity.setShortName(posGoods.getShortName());
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
        entity.setProdLineId(posGoods.getProdLineId());
        entity.setIsCloudActive(1);//默认有效，即商品和云端数据是同步到

        // TODO: 8/2/16 用不到，影响效率，暂时忽略。
//                    //设置商品名称的拼音和排序字母
//                    String namePinyin = PinyinUtils.getPingYin(posGoods.getName());
//                    entity.setNamePinyin(namePinyin);
//                    String sortLetter = null;
//                    if (!StringUtils.isEmpty(namePinyin)){
//                        sortLetter = namePinyin.substring(0, 1).toUpperCase();
//                    }
//                    if (sortLetter != null && sortLetter.matches("[A-Z]")) {
//                        entity.setNameSortLetter(sortLetter);
//                    } else {
//                        entity.setNameSortLetter("#");
//                    }
//        entity.setAbbreviation(AbbreviationUtil.cn2py(posGoods.getSkuName()));
        entity.setAbbreviation(PinyinUtils.getFirstSpell(posGoods.getSkuName()));
        PosProductService.get().saveOrUpdate(entity);
    }

    /**
     * 假删除数据
     * */
    public void pretendDelete(){
        List<KeyValue> keyValues = new ArrayList<>();
        keyValues.add(new KeyValue("isCloudActive", 0));
        getDao().update(PosProductEntity.class, keyValues, String.format("isCloudActive = %d", 1));

    }

}

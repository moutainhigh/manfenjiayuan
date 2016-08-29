package com.manfenjiayuan.mixicook_vip.database;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * <h1>采购订单购物车商品明细</h1><br>
 * <p>
 *
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PurchaseShopcartService extends BaseService<PurchaseShopcartEntity, String, PurchaseShopcartDao> {

    @Override
    protected Class<PurchaseShopcartDao> getDaoClass() {
        return PurchaseShopcartDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PurchaseShopcartService instance = null;
    /**
     * 返回 DailysettleService 实例
     * @return
     */
    public static PurchaseShopcartService getInstance() {
        if (instance == null) {
            synchronized (PurchaseShopcartService.class) {
                if (instance == null) {
                    instance = new PurchaseShopcartService();
                }
            }
        }
        return instance;
    }

    public PurchaseShopcartEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PurchaseShopcartEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(PurchaseShopcartEntity msg) {
        getDao().saveOrUpdate(msg);
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
    public List<PurchaseShopcartEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<PurchaseShopcartEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<PurchaseShopcartEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<PurchaseShopcartEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }public List<PurchaseShopcartEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<PurchaseShopcartEntity> queryAllBy(String strWhere) {
        try{
            return getDao().queryAllBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    /**
     *  按条件删除
     *  */
    public void deleteBy(String strWhere){
        try{
            getDao().deleteBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }
    /**
     *  逐条删除
     *  */
    public void deleteById(String id){
        try{
            getDao().deleteById(id);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }

    /**
     * 获取购物车中的所有生鲜商品
     * */
    public List<PurchaseShopcartEntity> fetchFreshEntites(){
        return queryAllBy(String.format("purchaseType = '%d'",
                PurchaseShopcartEntity.PURCHASE_TYPE_FRESH));
    }
    public void clearFreshGoodsList(){
        deleteBy(String.format("purchaseType = '%d'",
                PurchaseShopcartEntity.PURCHASE_TYPE_FRESH));
    }

    /**
     * 获取指定批发商的商品列表*/
    public List<PurchaseShopcartEntity> getFreshGoodsList(Long providerId){
        try{
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("purchaseType = '%d'",
                    PurchaseShopcartEntity.PURCHASE_TYPE_FRESH));
            if (providerId != null){
                sb.append(String.format("and providerId = '%d'",
                        providerId));
            }

            return getDao().queryAllBy(sb.toString(), "updatedDate desc");
        } catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    /**
     * 获取指定批发商的生鲜商品
     * */
    public PurchaseShopcartEntity getFreshGoods(Long providerId, String barcode){
        try{
            if (providerId == null || StringUtils.isEmpty(barcode)){
                return null;
            }

            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' and barcode = '%s'",
                    PurchaseShopcartEntity.PURCHASE_TYPE_FRESH, providerId, barcode);
            List<PurchaseShopcartEntity> entityList =  getDao().queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0){
                return entityList.get(0);
            }
        }catch (Exception e){
            ZLogger.e(String.format("getFreshItem failed, %s", e.toString()));
        }

        return null;
    }

//    /**
//     * 保存生鲜商品
//     * */
//    public void saveOrUpdateFreshGoods(FruitScGoodsSkuWrapper goods, Double quantity){
//        if (goods == null || StringUtils.isEmpty(goods.getBarcode())){
//            ZLogger.d("保存生鲜商品失败，商品无效或商品条码为空");
//            return;
//        }
//
//        GoodsSupplyInfo goodsSupplyInfo = goods.getGoodsSupplyInfo();
//        if (goodsSupplyInfo == null){
//            ZLogger.d("保存生鲜商品失败，没有批发商信息");
//            return;
//        }
//
//        PurchaseShopcartEntity purchaseShopcartEntity = PurchaseShopcartService
//                .getInstance().getFreshGoods(goodsSupplyInfo.getSupplyId(),
//                        goods.getBarcode());
//
//        if (purchaseShopcartEntity == null){
//            ZLogger.d(String.format("添加新的生鲜商品到购物车:%s", goods.getBarcode()));
//            purchaseShopcartEntity = new PurchaseShopcartEntity();
//            purchaseShopcartEntity.setCreatedDate(new Date());
//            purchaseShopcartEntity.setPurchaseType(PurchaseShopcartEntity.PURCHASE_TYPE_FRESH);
//            purchaseShopcartEntity.setProviderId(goodsSupplyInfo.getSupplyId());
//            purchaseShopcartEntity.setProviderName(goodsSupplyInfo.getSupplyName());
//            purchaseShopcartEntity.setChainSkuId(goodsSupplyInfo.getOtherTenantSkuId());
//            purchaseShopcartEntity.setIsPrivate(IsPrivate.PLATFORM);
//            purchaseShopcartEntity.setProSkuId(goods.getProSkuId());
//            purchaseShopcartEntity.setBarcode(goods.getBarcode());
//            purchaseShopcartEntity.setImgUrl(goods.getImgUrl());
//            purchaseShopcartEntity.setName(goods.getSkuName());
//            purchaseShopcartEntity.setUnit(goods.getBuyUnit());
//            purchaseShopcartEntity.setPrice(goodsSupplyInfo.getBuyPrice());
//        }
//        else{
//            ZLogger.d(String.format("更新购物车中生鲜商品:%s", goods.getBarcode()));
//        }
//
//        purchaseShopcartEntity.setQuantity(quantity);
//        purchaseShopcartEntity.setUpdatedDate(new Date());
//        saveOrUpdate(purchaseShopcartEntity);
//    }


    /**
     * 保存生鲜商品
     * */
    public void saveOrUpdateFreshGoods(ChainGoodsSku goods, Double quantity){
        if (goods == null || StringUtils.isEmpty(goods.getBarcode())){
            ZLogger.d("保存生鲜商品失败，商品无效或商品条码为空");
            return;
        }

        PurchaseShopcartEntity purchaseShopcartEntity = getFreshGoods(goods.getTenantId(),
                        goods.getBarcode());

        if (purchaseShopcartEntity == null){
            ZLogger.d(String.format("添加新的生鲜商品到购物车:%s", goods.getBarcode()));
            purchaseShopcartEntity = new PurchaseShopcartEntity();
            purchaseShopcartEntity.setCreatedDate(new Date());
            purchaseShopcartEntity.setPurchaseType(PurchaseShopcartEntity.PURCHASE_TYPE_FRESH);
            purchaseShopcartEntity.setProviderId(goods.getTenantId());
            purchaseShopcartEntity.setProviderName(goods.getCompanyName());
            purchaseShopcartEntity.setChainSkuId(goods.getId());
            purchaseShopcartEntity.setIsPrivate(IsPrivate.PLATFORM);
            purchaseShopcartEntity.setProSkuId(goods.getProSkuId());
            purchaseShopcartEntity.setBarcode(goods.getBarcode());
        }
        else{
            ZLogger.d(String.format("更新购物车中生鲜商品:%s", goods.getBarcode()));
        }

        purchaseShopcartEntity.setImgUrl(goods.getImgUrl());
        purchaseShopcartEntity.setName(goods.getSkuName());
        purchaseShopcartEntity.setUnit(goods.getBuyUnit());
        purchaseShopcartEntity.setPrice(goods.getHintPrice());
        purchaseShopcartEntity.setQuantity(quantity);
        purchaseShopcartEntity.setUpdatedDate(new Date());
        saveOrUpdate(purchaseShopcartEntity);
    }

    public void saveOrUpdateFreshGoods(PurchaseShopcartEntity purchaseShopcartEntity,
                                       Double quantity, boolean saveUpdatedDate){
        if (purchaseShopcartEntity == null || quantity == null){
           return;
        }

        purchaseShopcartEntity.setQuantity(quantity);
        if (saveUpdatedDate){
            purchaseShopcartEntity.setUpdatedDate(new Date());
        }
        saveOrUpdate(purchaseShopcartEntity);
    }

}

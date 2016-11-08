package com.manfenjiayuan.mixicook_vip.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.List;

/**
 * <h1>采购订单购物车商品明细</h1><br>
 * <p>
 * 保存首页存在且有价格的商品
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class HomeGoodsTempService extends BaseService<HomeGoodsTempEntity, String, HomeGoodsTempDao> {

    @Override
    protected Class<HomeGoodsTempDao> getDaoClass() {
        return HomeGoodsTempDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static HomeGoodsTempService instance = null;
    /**
     * 返回 DailysettleService 实例
     * @return
     */
    public static HomeGoodsTempService getInstance() {
        if (instance == null) {
            synchronized (HomeGoodsTempService.class) {
                if (instance == null) {
                    instance = new HomeGoodsTempService();
                }
            }
        }
        return instance;
    }

    public HomeGoodsTempEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(HomeGoodsTempEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(HomeGoodsTempEntity msg) {
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
    public List<HomeGoodsTempEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<HomeGoodsTempEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<HomeGoodsTempEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<HomeGoodsTempEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }public List<HomeGoodsTempEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<HomeGoodsTempEntity> queryAllBy(String strWhere) {
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

    public void batch(List<ScGoodsSku> dataList){
        clear();

        if (dataList != null && dataList.size() > 0){
            for (ScGoodsSku goodsSku : dataList){
                saveOrUpdate(goodsSku);
            }
        }
    }


    public void saveOrUpdate(ScGoodsSku goods){
        if (goods == null){
            ZLogger.d("商品无效");
            return;
        }

        try{
            HomeGoodsTempEntity entity = new HomeGoodsTempEntity();
            entity.setId(goods.getId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setBuyUnit(goods.getBuyUnit());
            entity.setCostPrice(goods.getCostPrice());
            entity.setProductId(goods.getProductId());

            saveOrUpdate(entity);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

//    /**
//     * 获取购物车中的所有生鲜商品
//     * */
//    public List<HomeGoodsTempEntity> fetchFreshEntites(){
//        return queryAllBy(String.format("purchaseType = '%d'",
//                HomeGoodsTempEntity.PURCHASE_TYPE_FRESH));
//    }
//    public void clearFreshGoodsList(){
//        deleteBy(String.format("purchaseType = '%d'",
//                HomeGoodsTempEntity.PURCHASE_TYPE_FRESH));
//    }
//
//    /**
//     * 获取指定批发商的商品列表*/
//    public List<HomeGoodsTempEntity> getFreshGoodsList(Long providerId){
//        try{
//            StringBuilder sb = new StringBuilder();
//            sb.append(String.format("purchaseType = '%d'",
//                    HomeGoodsTempEntity.PURCHASE_TYPE_FRESH));
//            if (providerId != null){
//                sb.append(String.format("and providerId = '%d'",
//                        providerId));
//            }
//
//            return getDao().queryAllBy(sb.toString(), "updatedDate desc");
//        } catch (Exception e){
//            ZLogger.e(e.toString());
//            return null;
//        }
//    }
//
//    /**
//     * 获取指定批发商的生鲜商品
//     * */
    public HomeGoodsTempEntity getEntityBy(Long proSkuId){
        try{
            if (proSkuId == null){
                return null;
            }

            String sqlWhere = String.format("proSkuId = '%d'", proSkuId);
            List<HomeGoodsTempEntity> entityList =  getDao().queryAllBy(sqlWhere);
            if (entityList != null && entityList.size() > 0){
                return entityList.get(0);
            }
        }catch (Exception e){
            ZLogger.e(String.format("getEntityBy failed, %s", e.toString()));
        }

        return null;
    }

////    /**
////     * 保存生鲜商品
////     * */

//
//
//    /**
//     * 保存生鲜商品
//     * */
//    public void saveOrUpdateFreshGoods(ChainGoodsSku goods, Double quantity){
//        if (goods == null || StringUtils.isEmpty(goods.getBarcode())){
//            ZLogger.d("保存生鲜商品失败，商品无效或商品条码为空");
//            return;
//        }
//
//        HomeGoodsTempEntity HomeGoodsTempEntity = getFreshGoods(goods.getTenantId(),
//                        goods.getBarcode());
//
//        if (HomeGoodsTempEntity == null){
//            ZLogger.d(String.format("添加新的生鲜商品到购物车:%s", goods.getBarcode()));
//            HomeGoodsTempEntity = new HomeGoodsTempEntity();
//            HomeGoodsTempEntity.setCreatedDate(new Date());
//            HomeGoodsTempEntity.setPurchaseType(HomeGoodsTempEntity.PURCHASE_TYPE_FRESH);
//            HomeGoodsTempEntity.setProviderId(goods.getTenantId());
//            HomeGoodsTempEntity.setProviderName(goods.getCompanyName());
//            HomeGoodsTempEntity.setChainSkuId(goods.getId());
//            HomeGoodsTempEntity.setIsPrivate(IsPrivate.PLATFORM);
//            HomeGoodsTempEntity.setProSkuId(goods.getProSkuId());
//            HomeGoodsTempEntity.setBarcode(goods.getBarcode());
//        }
//        else{
//            ZLogger.d(String.format("更新购物车中生鲜商品:%s", goods.getBarcode()));
//        }
//
//        HomeGoodsTempEntity.setImgUrl(goods.getImgUrl());
//        HomeGoodsTempEntity.setName(goods.getSkuName());
//        HomeGoodsTempEntity.setUnit(goods.getBuyUnit());
//        HomeGoodsTempEntity.setPrice(goods.getHintPrice());
//        HomeGoodsTempEntity.setQuantity(quantity);
//        HomeGoodsTempEntity.setUpdatedDate(new Date());
//        saveOrUpdate(HomeGoodsTempEntity);
//    }
//
//    public void saveOrUpdateFreshGoods(HomeGoodsTempEntity HomeGoodsTempEntity,
//                                       Double quantity, boolean saveUpdatedDate){
//        if (HomeGoodsTempEntity == null || quantity == null){
//           return;
//        }
//
//        HomeGoodsTempEntity.setQuantity(quantity);
//        if (saveUpdatedDate){
//            HomeGoodsTempEntity.setUpdatedDate(new Date());
//        }
//        saveOrUpdate(HomeGoodsTempEntity);
//    }

    public void downloadMyAddress(){

    }
}

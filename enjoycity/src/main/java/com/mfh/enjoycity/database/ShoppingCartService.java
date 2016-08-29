package com.mfh.enjoycity.database;

import android.content.Intent;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSON;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.enjoycity.AppContext;
import com.mfh.enjoycity.bean.HotSaleProductBean;
import com.mfh.enjoycity.bean.ProductBean;
import com.mfh.enjoycity.bean.ShopProductBean;
import com.mfh.enjoycity.bean.ShopcartProductTemp;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.UIHelper;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 购物车
 * Created by Administrator on 14-5-6.
 */
public class ShoppingCartService extends BaseService<ShoppingCartEntity, String, ShoppingCartDao> {
    private SimpleDateFormat format = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);

    @Override
    protected Class<ShoppingCartDao> getDaoClass() {
        return ShoppingCartDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static ShoppingCartService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static ShoppingCartService get() {
        String lsName = ShoppingCartService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new ShoppingCartService();//初始化登录服务
        }
        return instance;
    }

    public ShoppingCartEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(ShoppingCartEntity entity) {
        getDao().save(entity);
    }

    public void update(ShoppingCartEntity entity){
        getDao().update(entity);
    }
    public void saveOrUpdate(ShoppingCartEntity entity) {
        getDao().saveOrUpdate(entity);
    }

    /**
     * 清空历史记录
     * */
    public void clear(){
        getDao().deleteAll();
    }

    public void deleteById(String id){
        try{
            getDao().deleteById(id);
            AppContext.getAppContext().sendBroadcast(new Intent(Constants.BROADCAST_ACTION_SHOPCART_REFRESH));
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }

    public boolean entityExistById(String id) {
        try{
            return getDao().entityExistById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return false;
        }
    }


    public List<ShoppingCartEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<ShoppingCartEntity> queryAll() {
        return getDao().queryAll();
    }

    public Map<Long, List<ShoppingCartEntity>> queryAllByGroup(PageInfo pageInfo) {
        Map<Long, List<ShoppingCartEntity>> entityMap = new ArrayMap<>();

        List<ShoppingCartEntity> entityList = queryAll(pageInfo);
        if (entityList != null && entityList.size()> 0){
            for (ShoppingCartEntity entity : entityList){
                Long shopId = entity.getShopId();

                List<ShoppingCartEntity> list = entityMap.get(shopId);
                if (list == null){
                    list = new ArrayList<>();
                }
                list.add(entity);

                entityMap.put(shopId, list);
            }
        }

        return entityMap;
    }


    public List<ShopProductBean> queryAllForAdapter(){
        List<ShopProductBean> adapterDataList = new ArrayList<>();

        Map<Long, ShopProductBean> dataMap = new ArrayMap<>();

        List<ShoppingCartEntity> entityList = queryAll();
        if (entityList != null && entityList.size()> 0){
            for (ShoppingCartEntity entity : entityList){
                Long shopId = entity.getShopId();

                ShopProductBean adapterData = dataMap.get(shopId);
                if (adapterData == null){
                    adapterData = new ShopProductBean();
                    adapterData.setShopId(entity.getShopId());
                }
                adapterData.addProductEntity(entity);
                dataMap.put(shopId, adapterData);
            }
        }

        for (Long key : dataMap.keySet()){
            adapterDataList.add(dataMap.get(key));
        }
        return adapterDataList;
    }

//    /**
//     * 添加到购物车
//     * */
//    public void addToShopcart(Long shopId, ProductDetail bean){
//        Product product = bean.getProduct();
//
//        String id = String.valueOf(shopId) + String.valueOf(product.getId());
//        if (id != null && getDao().entityExistById(id)){
//            ShoppingCartEntity entity = getDao().getEntityById(id);
//            entity.setProductCount(entity.getProductCount() + 1);
//            getDao().update(entity);
//        }
//        else{
//            ShoppingCartEntity entity = new ShoppingCartEntity();
//            entity.setId(id);
//            entity.setCreatedDate(new Date());
//            entity.setProductId(product.getId());
//            entity.setProductName(product.getName());
////            double discount = bean.getDiscount();
////            if (discount > 0 && discount < 1){
////                entity.setProductPrice(bean.getCostPrice() * discount);
////            }else{
////                entity.setProductPrice(bean.getCostPrice());
////            }
//            if (bean.getCostPrice() != null){
//                entity.setProductPrice(Double.valueOf(bean.getCostPrice()));
//            }else{
//                entity.setProductPrice(0);
//            }
//            entity.setProductImageUrl(bean.getThumbnail());
//            entity.setProductCount(1);
//            entity.setShopId(shopId);
//
//            saveOrUpdate(entity);
//        }
//
//        UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_SHOPCART_REFRESH);
//    }

    /**
     * 加入商品到购物车
     * */
    public void addToShopcart(Long shopId, ProductBean bean){
        String id = String.valueOf(shopId) + String.valueOf(bean.getId());
        if (id != null && getDao().entityExistById(id)){
            ShoppingCartEntity entity = getDao().getEntityById(id);
            entity.setProductCount(entity.getProductCount() + 1);
            update(entity);
        }
        else{
            ShoppingCartEntity entity = new ShoppingCartEntity();
            entity.setId(id);
            entity.setCreatedDate(new Date());
            entity.setProductId(bean.getId());
            entity.setProductName(bean.getName());
//            double discount = bean.getDiscount();
//            if (discount > 0 && discount < 1){
//                entity.setProductPrice(bean.getCostPrice() * discount);
//            }else
            {
                entity.setProductPrice(bean.getCostPrice());
            }
            entity.setProductImageUrl(bean.getThumbnail());
            entity.setProductCount(1);
            entity.setDescription(bean.getDescription());
            entity.setShopId(shopId);

            saveOrUpdate(entity);
        }

        UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_SHOPCART_REFRESH);
    }

    public void addToShopcart(Long shopId, HotSaleProductBean bean){
        String id = String.valueOf(shopId) + String.valueOf(bean.getProductId());
        if (id != null && getDao().entityExistById(id)){
            ShoppingCartEntity entity = getDao().getEntityById(id);
            entity.setProductCount(entity.getProductCount() + 1);
            update(entity);
        }
        else{
            ShoppingCartEntity entity = new ShoppingCartEntity();
            entity.setId(id);
            entity.setCreatedDate(new Date());
            entity.setProductId(bean.getProductId());
            entity.setProductName(bean.getProductName());
            if (bean.getPrice() != null){
//                double discount = bean.getDiscount();
//                if (discount > 0 && discount < 1){
//                    entity.setProductPrice(Double.valueOf(bean.getPrice()) * discount);
//                }else
                {
                    entity.setProductPrice(Double.valueOf(bean.getPrice()));
                }
            }else{
                entity.setProductPrice(0);
            }
            entity.setProductImageUrl(bean.getImgUrl());
            entity.setProductCount(1);
            entity.setShopId(shopId);

            saveOrUpdate(entity);
        }
        AppContext.getAppContext().sendBroadcast(new Intent(Constants.BROADCAST_ACTION_SHOPCART_REFRESH));

    }

    /**
     * 加入购物车，
     * @param jsonStr 格式如下
    //    {
    //        "id": 123,
    //            "productId": 456,
    //            "productName": "商品名",
    //            "productPrice": 88.88,
    //            "productCount": 1,
    //            "productImageUrl": "商品图片链接",
    //            "shopId": 789
    //    }
     * */
    public void addToShopcartFromHybird(String jsonStr){
        ShopcartProductTemp temp = JSON.parseObject(jsonStr, ShopcartProductTemp.class);
        String id = String.valueOf(temp.getShopId()) + String.valueOf(temp.getProductId());

        if (id != null && getDao().entityExistById(id)){
            ShoppingCartEntity entity = getDao().getEntityById(id);
            entity.setProductCount(entity.getProductCount() + temp.getProductCount());
            update(entity);
        }
        else{
            ShoppingCartEntity entity = new ShoppingCartEntity();
            entity.setId(id);
            entity.setCreatedDate(new Date());
            entity.setProductId(temp.getProductId());
            entity.setProductName(temp.getProductName());
            entity.setProductPrice(temp.getProductPrice());
            entity.setProductImageUrl(temp.getProductImageUrl());
            entity.setProductCount(temp.getProductCount());
            entity.setShopId(temp.getShopId());

            saveOrUpdate(entity);
        }

        AppContext.getAppContext().sendBroadcast(new Intent(Constants.BROADCAST_ACTION_SHOPCART_REFRESH));

    }

    public void addToShopcart(ShoppingCartEntity entity, int count){
        if (entity == null){
            return;
        }

        String id = entity.getId();
        if (id == null){
            return;
        }

        if (getDao().entityExistById(id)){
            ShoppingCartEntity orginal = getDao().getEntityById(id);
            orginal.setProductCount(orginal.getProductCount() + count);
            update(orginal);
        }
        else{
            ShoppingCartEntity orginal = new ShoppingCartEntity();
            orginal.setId(id);
            orginal.setCreatedDate(new Date());
            orginal.setProductId(entity.getProductId());
            orginal.setProductName(entity.getProductName());
            orginal.setProductPrice(entity.getProductPrice());
            orginal.setProductImageUrl(entity.getProductImageUrl());
            orginal.setProductCount(count);
            orginal.setShopId(entity.getShopId());

            saveOrUpdate(orginal);
        }
        AppContext.getAppContext().sendBroadcast(new Intent(Constants.BROADCAST_ACTION_SHOPCART_REFRESH));

    }

}

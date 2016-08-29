package com.mfh.litecashier.database.logic;

import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.database.dao.PosOrderItemDao;
import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.litecashier.database.entity.PosProductEntity;

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
//        String lsName = PosOrderItemService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new PosOrderItemService();//初始化登录服务
//        }
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

    public PosOrderItemEntity generate(String orderBarCode, PosProductEntity goods,
                                   Double bCount){
        if (StringUtils.isEmpty(orderBarCode) || goods == null || bCount.compareTo(0D) == 0){
            ZLogger.d("参数无效");
            return null;
        }

        PosOrderItemEntity orderItemEntity = new PosOrderItemEntity();
        orderItemEntity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        orderItemEntity.setUpdatedDate(new Date());

        orderItemEntity.setGoodsId(goods.getId());
        orderItemEntity.setOrderBarCode(orderBarCode);
        orderItemEntity.setProSkuId(goods.getProSkuId());
        orderItemEntity.setBarcode(goods.getBarcode());
        orderItemEntity.setProductId(goods.getProductId());
        orderItemEntity.setName(goods.getName());
        orderItemEntity.setUnit(goods.getUnit());
        orderItemEntity.setCostPrice(goods.getCostPrice());
        orderItemEntity.setBcount(bCount);
        orderItemEntity.setProviderId(goods.getProviderId());

        orderItemEntity.setUnit(goods.getUnit());
        orderItemEntity.setPriceType(goods.getPriceType());

        //默认会员价使用标准单价计算，可以在后面售价时修改。
        orderItemEntity.setFinalPrice(goods.getCostPrice());

        Double standardAmount = orderItemEntity.getBcount() * goods.getCostPrice();//标准金额
        orderItemEntity.setAmount(standardAmount);//标准价金额

        Double finalAmount = orderItemEntity.getBcount() * orderItemEntity.getFinalPrice();//成交金额
        orderItemEntity.setFinalAmount(finalAmount);
        orderItemEntity.setCateType(goods.getCateType());

        return orderItemEntity;
    }


    public PosOrderItemEntity generate(String orderBarCode, ScGoodsSku goods,
                                       Double bCount){
        //检查参数商品数量为0，认为无效。
        if (StringUtils.isEmpty(orderBarCode) || goods == null || bCount.compareTo(0D) == 0){
            ZLogger.d("参数无效");
            return null;
        }

        PosOrderItemEntity orderItemEntity = new PosOrderItemEntity();
        orderItemEntity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        orderItemEntity.setUpdatedDate(new Date());

        orderItemEntity.setGoodsId(goods.getId());
        orderItemEntity.setProSkuId(goods.getProSkuId());
        orderItemEntity.setProductId(goods.getProductId());
        orderItemEntity.setOrderBarCode(orderBarCode);
        orderItemEntity.setBarcode(goods.getBarcode());
        orderItemEntity.setName(goods.getSkuName());
        orderItemEntity.setUnit(goods.getUnit());
        orderItemEntity.setCostPrice(goods.getCostPrice());
        orderItemEntity.setBcount(bCount);
        orderItemEntity.setProviderId(goods.getProviderId());

        orderItemEntity.setUnit(goods.getUnit());
        orderItemEntity.setPriceType(goods.getPriceType());

        //默认会员价使用标准单价计算，可以在后面售价时修改。
        orderItemEntity.setFinalPrice(goods.getCostPrice());

        Double standardAmount = orderItemEntity.getBcount() * goods.getCostPrice();//标准金额
        orderItemEntity.setAmount(standardAmount);//标准价金额

        Double finalAmount = orderItemEntity.getBcount() * orderItemEntity.getFinalPrice();//成交金额
        orderItemEntity.setFinalAmount(finalAmount);

        return orderItemEntity;
    }

    /**
     * 洗衣商品
     * */
    public PosOrderItemEntity fromLaundryGoods(String orderBarCode, ChainGoodsSku goods,
                                   Double bCount){
        //商品数量为0，认为无效。
        if (StringUtils.isEmpty(orderBarCode) || goods == null || bCount.compareTo(0D) == 0){
            return null;
        }

        PosOrderItemEntity orderItemEntity = new PosOrderItemEntity();
        orderItemEntity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        orderItemEntity.setUpdatedDate(new Date());

        orderItemEntity.setGoodsId(goods.getId());
        orderItemEntity.setProSkuId(goods.getProSkuId());
        orderItemEntity.setOrderBarCode(orderBarCode);
        orderItemEntity.setBarcode(goods.getBarcode());
        orderItemEntity.setProductId(goods.getProductId());
        orderItemEntity.setName(goods.getSkuName());
        orderItemEntity.setUnit(goods.getUnit());
        orderItemEntity.setCostPrice(goods.getCostPrice());
        orderItemEntity.setBcount(bCount);
        //
        orderItemEntity.setProviderId(goods.getTenantId());

        orderItemEntity.setUnit(goods.getUnit());
        orderItemEntity.setPriceType(goods.getPriceType());

        //默认会员价使用标准单价计算，可以在后面售价时修改。
        orderItemEntity.setFinalPrice(goods.getCostPrice());

        Double standardAmount = orderItemEntity.getBcount() * goods.getCostPrice();//标准金额
        orderItemEntity.setAmount(standardAmount);//标准价金额

        Double finalAmount = orderItemEntity.getBcount() * orderItemEntity.getFinalPrice();//成交金额
        orderItemEntity.setFinalAmount(finalAmount);

        return orderItemEntity;
    }

    public PosOrderItemEntity generate(String orderBarCode, CommonlyGoodsEntity productEntity,
                                   Double bCount){
        if (StringUtils.isEmpty(orderBarCode) || productEntity == null || bCount.compareTo(0D) == 0){
            return null;
        }

        PosOrderItemEntity orderItemEntity = new PosOrderItemEntity();
        orderItemEntity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        orderItemEntity.setUpdatedDate(new Date());

        orderItemEntity.setGoodsId(productEntity.getId());
        orderItemEntity.setOrderBarCode(orderBarCode);
        orderItemEntity.setProSkuId(productEntity.getProSkuId());
        orderItemEntity.setBarcode(productEntity.getBarcode());
        orderItemEntity.setProductId(productEntity.getProductId());
        orderItemEntity.setName(productEntity.getName());
        orderItemEntity.setUnit(productEntity.getUnit());
        orderItemEntity.setCostPrice(productEntity.getCostPrice());
        orderItemEntity.setBcount(bCount);
        orderItemEntity.setProviderId(productEntity.getProviderId());

        orderItemEntity.setUnit(productEntity.getUnit());
        orderItemEntity.setPriceType(productEntity.getPriceType());

        //默认会员价使用标准单价计算，可以在后面售价时修改。
        orderItemEntity.setFinalPrice(productEntity.getCostPrice());

        Double standardAmount = orderItemEntity.getBcount() * productEntity.getCostPrice();//标准金额
        orderItemEntity.setAmount(standardAmount);//标准价金额

        Double finalAmount = orderItemEntity.getBcount() * orderItemEntity.getFinalPrice();//成交金额
        orderItemEntity.setFinalAmount(finalAmount);

        return orderItemEntity;
    }

    /**
     * 新增订单明细
     * */
    public void addNewEntity(PosOrderItemEntity productEntity){
        if (productEntity == null){
            ZLogger.d("商品无效");
            return;
        }

        PosOrderItemEntity orderItemEntity;
        List<PosOrderItemEntity> entityList = queryAllBy(String.format("barcode = '%s' and orderBarCode = '%s'",
                productEntity.getBarcode(), productEntity.getOrderBarCode()));
        if (entityList != null && entityList.size() > 0){
//            ZLogger.d("商品已经存在，更新商品信息");
            orderItemEntity =  entityList.get(0);

            orderItemEntity.setBcount(productEntity.getBcount());
            orderItemEntity.setCostPrice(productEntity.getCostPrice());
            orderItemEntity.setFinalPrice(productEntity.getCostPrice());
            orderItemEntity.setUpdatedDate(new Date());
        }
        else{
//            ZLogger.d("保存订单明细");
            orderItemEntity = new PosOrderItemEntity();
            orderItemEntity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
            orderItemEntity.setUpdatedDate(new Date());

            orderItemEntity.setOrderBarCode(productEntity.getOrderBarCode());
            orderItemEntity.setBarcode(productEntity.getBarcode());
            orderItemEntity.setGoodsId(productEntity.getGoodsId());
            orderItemEntity.setProSkuId(productEntity.getProSkuId());
            orderItemEntity.setProductId(productEntity.getProductId());
            orderItemEntity.setBcount(productEntity.getBcount());
            orderItemEntity.setCostPrice(productEntity.getCostPrice());
            orderItemEntity.setName(productEntity.getName());
            orderItemEntity.setUnit(productEntity.getUnit());
            orderItemEntity.setProviderId(productEntity.getProviderId());
            //默认会员价使用标准单价计算，可以在后面售价时修改。
            orderItemEntity.setFinalPrice(productEntity.getCostPrice());
            orderItemEntity.setPriceType(productEntity.getPriceType());

        }
        //标准金额
        orderItemEntity.setAmount(orderItemEntity.getBcount() * productEntity.getCostPrice());
        //成交金额
        orderItemEntity.setFinalAmount(orderItemEntity.getBcount() * orderItemEntity.getFinalPrice());

        saveOrUpdate(orderItemEntity);
    }
}

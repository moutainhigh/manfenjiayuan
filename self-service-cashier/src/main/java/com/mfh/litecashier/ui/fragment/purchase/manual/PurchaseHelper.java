package com.mfh.litecashier.ui.fragment.purchase.manual;

import com.alibaba.fastjson.JSON;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.database.entity.PurchaseGoodsEntity;
import com.mfh.litecashier.database.entity.PurchaseOrderEntity;
import com.mfh.litecashier.database.logic.PurchaseGoodsService;
import com.mfh.litecashier.database.logic.PurchaseOrderService;

import java.util.Date;
import java.util.List;

/**
 * 采购商品
 * Created by bingshanguxue on 15/12/15.
 */
public class PurchaseHelper {
    private static PurchaseHelper instance;

    public static PurchaseHelper getInstance() {
        if (instance == null) {
            synchronized (PurchaseHelper.class) {
                if (instance == null) {
                    instance = new PurchaseHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 清空购物车订单&明细
     * */
    public void clear(Integer purchaseType){
        PurchaseOrderService.getInstance()
                .deleteBy(String.format("purchaseType = '%d'", purchaseType));

        PurchaseGoodsService.getInstance()
                .deleteBy(String.format("purchaseType = '%d'", purchaseType));

        // TODO: 7/21/16  
    }

    /**
     * 添加到购物车
     * */
    public void addToShopcart(Integer purchaseType, PurchaseShopcartGoodsWrapper goods){
        //检查商品是否有效
        if (purchaseType == null){
            ZLogger.d("采购类型无效");
            return;
        }
        if (goods == null){
            ZLogger.d("加入购物车失败，商品无效");
            return;
        }
        if (goods.getChainSkuId() == null){
            ZLogger.d("加入购物车失败，商品chainSkuId为空");
            return;
        }
        if (goods.getSupplyId() == null){
            ZLogger.d("加入购物车失败，商品无批发商信息，暂时无法加入购物车");
            return;
        }

        if (goods.getBuyPrice() == null){
            ZLogger.d("加入购物车失败，商品采购价为空");
            return;
        }

        //保存订单
        PurchaseOrderEntity orderEntity = PurchaseOrderService.getInstance()
                .fetchOrder(purchaseType, goods.getSupplyId());
        if (orderEntity == null){
            orderEntity = new PurchaseOrderEntity();
            orderEntity.setCreatedDate(new Date());
            orderEntity.setPurchaseType(purchaseType);
            orderEntity.setProviderId(goods.getSupplyId());
        }
        orderEntity.setProviderName(goods.getSupplyName());
        orderEntity.setIsPrivate(goods.getIsPrivate());
        orderEntity.setUpdatedDate(new Date());
        PurchaseOrderService.getInstance().saveOrUpdate(orderEntity);
        ZLogger.df(String.format("保存or更新采购订单：\n%s", JSON.toJSONString(orderEntity)));

        //更新订单明细
        PurchaseGoodsEntity goodsEntity = PurchaseGoodsService.getInstance()
                .fetchGoods(purchaseType, goods.getSupplyId(), goods.getChainSkuId());
        if (goodsEntity == null){
            goodsEntity = new PurchaseGoodsEntity();
            goodsEntity.setCreatedDate(new Date());
            goodsEntity.setPurchaseType(purchaseType);
            goodsEntity.setProviderId(goods.getSupplyId());
            goodsEntity.setChainSkuId(goods.getChainSkuId());
        }
        goodsEntity.setProSkuId(goods.getProSkuId());
        goodsEntity.setIsPrivate(goods.getIsPrivate());
        goodsEntity.setProductName(goods.getProductName());
        goodsEntity.setQuantityCheck(goods.getQuantityCheck());
        goodsEntity.setBuyPrice(goods.getBuyPrice());//不能为空
        goodsEntity.setBarcode(goods.getBarcode());
        goodsEntity.setUpdatedDate(new Date());
        PurchaseGoodsService.getInstance().saveOrUpdate(goodsEntity);
        ZLogger.df(String.format("保存or更新采购商品：\n%s", JSON.toJSONString(goodsEntity)));

        // 整理
        arrange(purchaseType);
    }

    /**
     * 移除商品
     * */
    public void removeGoods(Integer purchaseType, PurchaseShopcartGoodsWrapper goods){
        //检查商品是否有效
        if (purchaseType == null){
            ZLogger.d("采购类型无效");
            return;
        }
        if (goods == null){
            ZLogger.d("移除商品失败，商品无效");
            return;
        }
        if (goods.getChainSkuId() == null){
            ZLogger.d("移除商品失败，商品chainSkuId为空");
            return;
        }
        if (goods.getSupplyId() == null){
            ZLogger.d("移除商品失败，商品无批发商信息，暂时无法移除商品");
            return;
        }

        //更新订单明细
        String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' " +
                "and chainSkuId = '%d'", purchaseType, goods.getSupplyId(), goods.getChainSkuId());

        PurchaseGoodsService.getInstance().deleteBy(sqlWhere);

        PurchaseHelper.getInstance().arrange(purchaseType);
    }

    /**
     * 整理订单，删除无明细的空订单；更新订单统计数据
     * @param purchaseType 采购类型
     * */
    public void arrange(Integer purchaseType){
        ZLogger.d("整理采购订单");
        List<PurchaseOrderEntity> orderEntities = PurchaseOrderService.getInstance()
                .fetchOrders(purchaseType);
        if (orderEntities == null || orderEntities.size() <= 0){
            return;
        }

        for (PurchaseOrderEntity orderEntity : orderEntities){
            List<PurchaseGoodsEntity> goodsEntities = PurchaseGoodsService.getInstance()
                    .fetchGoodsEntities(purchaseType, orderEntity.getProviderId());

            if (goodsEntities != null && goodsEntities.size() > 0){
                ZLogger.d(String.format("%d 商品数：%d", orderEntity.getProviderId(), goodsEntities.size()));

                Double amount = 0D;
                for (PurchaseGoodsEntity goodsEntity : goodsEntities){
                    amount += goodsEntity.getBuyPrice() * goodsEntity.getQuantityCheck();
                }

                orderEntity.setGoodsNumber(goodsEntities.size());
                orderEntity.setAmount(amount);
                PurchaseOrderService.getInstance().saveOrUpdate(orderEntity);
            }
            else{
                ZLogger.d(String.format("删除订单明细为空的批发商(%d)采购订单", orderEntity.getProviderId()));
                PurchaseOrderService.getInstance().deleteById(String.valueOf(orderEntity.getId()));
            }
        }
    }
    /**
     * 整理指定批发商订单，删除无明细的空订单；更新订单统计数据
     * @param purchaseType 采购类型
     * @param providerId 批发商编号
     * */
    public void arrange(Integer purchaseType, Long providerId){
        List<PurchaseOrderEntity> orderEntities = PurchaseOrderService.getInstance()
                .fetchOrders(purchaseType, providerId);
        if (orderEntities == null || orderEntities.size() <= 0){
            return;
        }

        for (PurchaseOrderEntity orderEntity : orderEntities){
            List<PurchaseGoodsEntity> goodsEntities = PurchaseGoodsService.getInstance()
                    .fetchGoodsEntities(purchaseType, orderEntity.getProviderId());
            if (goodsEntities != null && goodsEntities.size() > 0){
                Double amount = 0D;
                for (PurchaseGoodsEntity goodsEntity : goodsEntities){
                    amount += goodsEntity.getBuyPrice() * goodsEntity.getQuantityCheck();
                }

                orderEntity.setGoodsNumber(goodsEntities.size());
                orderEntity.setAmount(amount);
                PurchaseOrderService.getInstance().saveOrUpdate(orderEntity);
            }
            else{
                PurchaseOrderService.getInstance().deleteById(String.valueOf(orderEntity.getId()));
            }
        }
    }

    /**
     * 获取订单数量
     * */
    public int getOrderCount(Integer purchaseType){
        List<PurchaseOrderEntity> orderEntities = PurchaseOrderService.getInstance()
                .fetchOrders(purchaseType);
        if (orderEntities != null && orderEntities.size() > 0){
            return orderEntities.size();
        }

        return 0;
    }

    /**
     * 获取订单明细数量
     * */
    public int getOrderItemCount(Integer purchaseType){
        int count = 0;
        List<PurchaseOrderEntity> orderEntities = PurchaseOrderService.getInstance()
                .fetchOrders(purchaseType);
        if (orderEntities != null && orderEntities.size() > 0){
            for (PurchaseOrderEntity orderEntity : orderEntities){
                List<PurchaseGoodsEntity> goodsEntities = PurchaseGoodsService.getInstance()
                        .fetchGoodsEntities(purchaseType, orderEntity.getProviderId());
                if (goodsEntities != null && goodsEntities.size() > 0){
                    count += goodsEntities.size();
                }
            }
        }

        return count;
    }

}

package com.manfenjiayuan.business.bean.wrapper;


import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.api.invFindOrder.InvFindOrderItem;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.utils.ObjectsCompact;

import java.io.Serializable;
import java.util.Date;

/**
 * 增采购收货单/退货单 明细
 * Created by bingshanguxue on 15/9/22.
 */
public class CreateOrderItemWrapper implements Serializable {
    private Long proSkuId;//
    private String imgUrl;//图片
    private String unitSpec;//单位
    private String productName;//商品名称

    private Long chainSkuId;//tenantSkuId,otherTenantSkuId
    private String barcode;//条码
    private Double quantityCheck = 0D;//采购数量
    private Double price;//价格
    private Double amount = 0D;//总价

    private Date createdDate; // 创建日期
    private Date updatedDate; //修改日期

    private Long providerId;//供应商or批发商编号
    private Integer isPrivate;//（0：不是 1：是）

    public String getUnitSpec() {
        return unitSpec;
    }

    public void setUnitSpec(String unitSpec) {
        this.unitSpec = unitSpec;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getChainSkuId() {
        return chainSkuId;
    }

    public void setChainSkuId(Long chainSkuId) {
        this.chainSkuId = chainSkuId;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getQuantityCheck() {
        if (quantityCheck == null) {
            return 0D;
        }
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }

    public Double getPrice() {
        if (price == null) {
            return 0D;
        }
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        if (amount == null) {
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * 采购订单明细
     */
    public static CreateOrderItemWrapper fromInvSendOrderItem(InvSendOrderItem goods) {
        if (goods == null) {
            return null;
        }

        CreateOrderItemWrapper itemWrapper = new CreateOrderItemWrapper();
        itemWrapper.setImgUrl(goods.getImgUrl());
        itemWrapper.setBarcode(goods.getBarcode());
        itemWrapper.setChainSkuId(goods.getChainSkuId());
        itemWrapper.setProSkuId(goods.getProSkuId());
        itemWrapper.setProductName(goods.getProductName());
        // 判断如果采购单位&采购计价类型 和销售单位&销售计价类型不一致，则需要重新输入商品数量
        itemWrapper.setUnitSpec(goods.getUnit());
        if (ObjectsCompact.equals(goods.getUnit(), goods.getBuyUnit()) &&
                ObjectsCompact.equals(goods.getPriceType(), goods.getBuyPriceType())) {
            itemWrapper.setQuantityCheck(goods.getAskTotalCount());
        } else {
            itemWrapper.setQuantityCheck(0D);
        }
        itemWrapper.setPrice(goods.getPrice());
        itemWrapper.setAmount(itemWrapper.getPrice() * itemWrapper.getQuantityCheck());
        itemWrapper.setProviderId(goods.getProviderId());
        itemWrapper.setIsPrivate(goods.getIsPrivate());
        itemWrapper.setCreatedDate(new Date());
        itemWrapper.setUpdatedDate(new Date());

        return itemWrapper;
    }

    /**
     * 采购收获订单明细
     */
    public static CreateOrderItemWrapper fromInvSendIoOrderItem(InvSendIoOrderItem goods) {
        if (goods == null) {
            return null;
        }

        CreateOrderItemWrapper itemWrapper = new CreateOrderItemWrapper();
        itemWrapper.setUnitSpec(goods.getUnitSpec());
        itemWrapper.setImgUrl(goods.getImgUrl());
        itemWrapper.setBarcode(goods.getBarcode());
        itemWrapper.setChainSkuId(goods.getChainSkuId());
        itemWrapper.setProSkuId(goods.getProSkuId());
        itemWrapper.setProductName(goods.getProductName());
        itemWrapper.setQuantityCheck(goods.getQuantityCheck());
        itemWrapper.setPrice(goods.getPrice());
        itemWrapper.setAmount(goods.getAmount());
        itemWrapper.setProviderId(goods.getProviderId());
        itemWrapper.setIsPrivate(goods.getIsPrivate());
        itemWrapper.setCreatedDate(new Date());
        itemWrapper.setUpdatedDate(new Date());

        return itemWrapper;
    }


    /**
     * 拣货单明细
     */
    public static CreateOrderItemWrapper fromInvFindOrderItem(InvFindOrderItem goods) {
        if (goods == null) {
            return null;
        }

        CreateOrderItemWrapper itemWrapper = new CreateOrderItemWrapper();
        itemWrapper.setUnitSpec(goods.getUnitSpec());
        itemWrapper.setImgUrl(goods.getImgUrl());
        itemWrapper.setBarcode(goods.getBarcode());
        itemWrapper.setChainSkuId(goods.getTenantSkuId());
        itemWrapper.setProSkuId(goods.getProSkuId());
        itemWrapper.setProductName(goods.getProductName());
        itemWrapper.setQuantityCheck(goods.getQuantityCheck());
        itemWrapper.setPrice(goods.getPrice());
        itemWrapper.setAmount(goods.getAmount());
        itemWrapper.setProviderId(goods.getProviderId());
        itemWrapper.setIsPrivate(goods.getIsPrivate());
        itemWrapper.setCreatedDate(new Date());
        itemWrapper.setUpdatedDate(new Date());

        return itemWrapper;
    }

    public static CreateOrderItemWrapper fromSupplyGoods(ChainGoodsSku goods, Double quantityCheck) {
        if (goods == null) {
            return null;
        }
        CreateOrderItemWrapper itemWrapper = new CreateOrderItemWrapper();
        itemWrapper.setUnitSpec(goods.getUnit());
        itemWrapper.setImgUrl(goods.getImgUrl());
        itemWrapper.setBarcode(goods.getBarcode());
        itemWrapper.setProSkuId(goods.getProSkuId());
        itemWrapper.setProductName(goods.getSkuName());
        if (quantityCheck == null) {
            quantityCheck = 0D;
        }
        itemWrapper.setQuantityCheck(quantityCheck);
        //这里使用单件商品价格
        itemWrapper.setPrice(goods.getSingleCostPrice());
        if (itemWrapper.getPrice() == null) {
            itemWrapper.setAmount(0D);
        } else {
            itemWrapper.setAmount(itemWrapper.getPrice() * itemWrapper.getQuantityCheck());
        }
        itemWrapper.setCreatedDate(new Date());
        itemWrapper.setUpdatedDate(new Date());

        itemWrapper.setChainSkuId(goods.getId());
        itemWrapper.setProviderId(goods.getTenantId());
        itemWrapper.setIsPrivate(IsPrivate.PLATFORM);
        return itemWrapper;
    }


    public static CreateOrderItemWrapper fromStockTakeGoods(ScGoodsSku goods, Double quantityCheck) {
        if (goods == null) {
            return null;
        }
        CreateOrderItemWrapper itemWrapper = new CreateOrderItemWrapper();
        itemWrapper.setUnitSpec(goods.getUnit());
        itemWrapper.setImgUrl(goods.getImgUrl());
        itemWrapper.setBarcode(goods.getBarcode());
        itemWrapper.setProSkuId(goods.getProSkuId());
        itemWrapper.setProductName(goods.getSkuName());
        if (quantityCheck == null) {
            quantityCheck = 0D;
        }
        itemWrapper.setQuantityCheck(quantityCheck);
        //这里使用商品采购价格
        itemWrapper.setPrice(goods.getBuyPrice());
        if (itemWrapper.getPrice() == null) {
            itemWrapper.setAmount(0D);
        } else {
            itemWrapper.setAmount(itemWrapper.getPrice() * itemWrapper.getQuantityCheck());
        }
        itemWrapper.setCreatedDate(new Date());
        itemWrapper.setUpdatedDate(new Date());

        itemWrapper.setChainSkuId(goods.getId());
        itemWrapper.setProviderId(goods.getTenantId());
        itemWrapper.setIsPrivate(IsPrivate.PLATFORM);
        return itemWrapper;
    }

    /**
     * 适用场景：批发商把偶呢
     */
    public static CreateOrderItemWrapper fromInvSkuGoods(InvSkuGoods goods, Double quantityCheck) {
        if (goods == null) {
            return null;
        }
        CreateOrderItemWrapper itemWrapper = new CreateOrderItemWrapper();
        itemWrapper.setUnitSpec(goods.getUnit());
        itemWrapper.setImgUrl("");
        itemWrapper.setBarcode(goods.getBarcode());
        itemWrapper.setProSkuId(goods.getProSkuId());
        itemWrapper.setProductName(goods.getName());
        if (quantityCheck == null) {
            quantityCheck = 0D;
        }
        itemWrapper.setQuantityCheck(quantityCheck);
        //这里使用商品采购价格
        itemWrapper.setPrice(goods.getCostPrice());
        if (itemWrapper.getPrice() == null) {
            itemWrapper.setAmount(0D);
        } else {
            itemWrapper.setAmount(itemWrapper.getPrice() * itemWrapper.getQuantityCheck());
        }
        itemWrapper.setCreatedDate(new Date());
        itemWrapper.setUpdatedDate(new Date());

        itemWrapper.setChainSkuId(goods.getId());
//        itemWrapper.setProviderId(goods.get());
        itemWrapper.setIsPrivate(IsPrivate.PLATFORM);
        return itemWrapper;
    }


    /**
     * 库存商品
     * 使用零售价作为价格
     */
    public static CreateOrderItemWrapper fromStockGoods(ScGoodsSku goods) {
        if (goods == null) {
            return null;
        }
        CreateOrderItemWrapper itemWrapper = new CreateOrderItemWrapper();
        itemWrapper.setUnitSpec(goods.getUnit());
        itemWrapper.setImgUrl(goods.getImgUrl());
        itemWrapper.setBarcode(goods.getBarcode());
        itemWrapper.setProSkuId(goods.getProSkuId());
        itemWrapper.setProductName(goods.getSkuName());
        itemWrapper.setQuantityCheck(1D);
        itemWrapper.setPrice(goods.getCostPrice());
        itemWrapper.setAmount(goods.getCostPrice() * 1D);
        itemWrapper.setCreatedDate(new Date());
        itemWrapper.setUpdatedDate(new Date());

        itemWrapper.setChainSkuId(goods.getTenantSkuId());
        itemWrapper.setProviderId(goods.getProviderId());
        itemWrapper.setIsPrivate(IsPrivate.PLATFORM);
        return itemWrapper;
    }

}

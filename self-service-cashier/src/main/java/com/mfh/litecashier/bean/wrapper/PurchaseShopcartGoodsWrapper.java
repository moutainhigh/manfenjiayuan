package com.mfh.litecashier.bean.wrapper;

import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.GoodsSupplyInfo;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;

import java.io.Serializable;
import java.util.Date;

/**
 * 采购商品：库存商品/批发商商品
 * {@link ScGoodsSku}
 * {@link com.mfh.litecashier.bean.SkuGoods}
 *
 * @author zhangyz created on 2015-9-6
 */
public class PurchaseShopcartGoodsWrapper implements Serializable {
    private Long productId;//所属产品spu编号
    private String imgUrl;//图片链接
    private String productName; // 商品名称
    private Integer priceType;//价格类型0-计件 1-计重
    private String unit; // 单位，如箱、瓶

    private Long proSkuId;//产品sku编号
    private String barcode; //最小商品库存单元的条形码
    private Double packageNum;//箱规


    private Long tenantId;// 租户信息，即微超公司id
    private Long chainSkuId;//批发商产品spu编号，otherTenantSkuId
    private Double quantity = 0D; // 商品数量(库存)
//    private Double sellNum = 0D;//销量
    private Double buyPrice = 0D;   //采购价
    private Long supplyId;//批发商编号
    private String supplyName;//批发商名称
    private Integer isPrivate;//（0：平台 1：自采 3:统采）
    private Date createdDate;
    private Date updatedDate;

    private Double startNum;//起配量
    private Integer status;//1-有效，默认，0-无效
    //有以下属性，其他暂时没有

    private Double quantityCheck;//采购量

    public Long getChainSkuId() {
        return chainSkuId;
    }

    public void setChainSkuId(Long chainSkuId) {
        this.chainSkuId = chainSkuId;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getQuantity() {
        if (quantity == null) {
            return 0D;
        }
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

//    public Double getSellNum() {
//        if (sellNum == null) {
//            return 0D;
//        }
//        return sellNum;
//    }
//
//    public void setSellNum(Double sellNum) {
//        this.sellNum = sellNum;
//    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public String getSupplyName() {
        return supplyName;
    }

    public void setSupplyName(String supplyName) {
        this.supplyName = supplyName;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getStartNum() {
        if (startNum == null) {
            return 0D;
        }
        return startNum;
    }

    public void setStartNum(Double startNum) {
        this.startNum = startNum;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }


    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public Double getPackageNum() {
        if (packageNum == null) {
            return 0D;
        }
        return packageNum;
    }

    public void setPackageNum(Double packageNum) {
        this.packageNum = packageNum;
    }

    /**
     * 批发商商品
     */
    public static PurchaseShopcartGoodsWrapper fromSupplyGoods(ScGoodsSku goods,
                                                               GoodsSupplyInfo supplyInfo, int isPrivate) {
        if (goods == null) {
            return null;
        }
        PurchaseShopcartGoodsWrapper itemwrapper = new PurchaseShopcartGoodsWrapper();
        //注意这里使用otherTenantSkuId字段，而非tenantSkuId字段
//        itemwrapper.setChainSkuId(stockGoods.getTenantSkuId());
        itemwrapper.setProSkuId(goods.getProSkuId());
        itemwrapper.setProductId(goods.getProductId());
        itemwrapper.setImgUrl(goods.getImgUrl());
        itemwrapper.setProductName(goods.getSkuName());
        itemwrapper.setBarcode(goods.getBarcode());
        itemwrapper.setQuantity(goods.getQuantity());
        itemwrapper.setUnit(goods.getBuyUnit());
//        itemwrapper.setSellNum(stockGoods.getSellNumber());
        itemwrapper.setTenantId(goods.getTenantId());
        itemwrapper.setIsPrivate(isPrivate);
        itemwrapper.setQuantityCheck(itemwrapper.getStartNum() > 0 ? itemwrapper.getStartNum() : 1D);//采购量，默认为起配量

        if (supplyInfo != null){
            itemwrapper.setChainSkuId(supplyInfo.getOtherTenantSkuId());//不能为空
            itemwrapper.setSupplyId(supplyInfo.getSupplyId());//不能为空
            itemwrapper.setSupplyName(supplyInfo.getSupplyName());
            itemwrapper.setBuyPrice(supplyInfo.getBuyPrice());//不能为空
            itemwrapper.setStartNum(supplyInfo.getStartNum());
            itemwrapper.setPackageNum(supplyInfo.getPackageNum());
        }

        return itemwrapper;
    }

    /**
     * 智能订货明细
     */
    public static PurchaseShopcartGoodsWrapper fromIntelligentOrderItem(InvSendOrderItem goods,
                                                                    CompanyInfo companyInfo, int isPrivate) {
        if (goods == null) {
            return null;
        }
        PurchaseShopcartGoodsWrapper itemwrapper = new PurchaseShopcartGoodsWrapper();
        //注意这里使用otherTenantSkuId字段，而非tenantSkuId字段
//        itemwrapper.setChainSkuId(stockGoods.getTenantSkuId());
        itemwrapper.setProSkuId(goods.getProSkuId());
//        itemwrapper.setProductId(goods.getpro());
        itemwrapper.setImgUrl(goods.getImgUrl());
        itemwrapper.setProductName(goods.getProductName());
        itemwrapper.setBarcode(goods.getBarcode());
        itemwrapper.setUnit(goods.getBuyUnit());
//        itemwrapper.setSellNum(stockGoods.getSellNumber());
//        itemwrapper.setTenantId(goods.get());
        itemwrapper.setIsPrivate(isPrivate);
        //后台：自动订货这边，因为没有人干预，给你的明细可能是totalCount，如果发现采购单位和销售单位一样，那么会把askTotalCount都加上。
        Double totalCount = goods.getAskTotalCount();
        itemwrapper.setQuantity(totalCount);
        //使用智能订货计算的数量，与起订量无关。默认和总数一致
        itemwrapper.setQuantityCheck(totalCount);

        if (companyInfo != null){
            itemwrapper.setChainSkuId(goods.getChainSkuId());//不能为空
            itemwrapper.setSupplyId(companyInfo.getId());//不能为空
            itemwrapper.setSupplyName(companyInfo.getName());
            itemwrapper.setBuyPrice(goods.getPrice());//不能为空
//            itemwrapper.setStartNum(goods.get());
//            itemwrapper.setPackageNum(goods.getPackageNum());
        }

        return itemwrapper;
    }



}

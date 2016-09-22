package com.bingshanguxue.pda.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * 出入库订单
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="pda_invio_goods_v0001")
public class InvIoGoodsEntity extends MfhEntity<Long> implements ILongId{
//    private String id;
    private Long proSkuId;
    private String productName;//商品名称
    private String barcode;//条码,注意proSkuId和barcode不能同时为空
    private String posId;
    private Double price = 0D;//价格
    private String unit;

    private Double quantityPack = 0D;//按包装规格提交的数量
    private Double quantityCheck = 0D;//提交的数量


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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getQuantityPack() {
        return quantityPack;
    }

    public void setQuantityPack(Double quantityPack) {
        this.quantityPack = quantityPack;
    }

    public Double getQuantityCheck() {
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

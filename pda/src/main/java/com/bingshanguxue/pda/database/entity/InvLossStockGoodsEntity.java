package com.bingshanguxue.pda.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * 报损
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="pda_invloss_stock_goods_v0001")
public class InvLossStockGoodsEntity extends MfhEntity<Long> implements ILongId{

    private Long proSkuId;//
    private String barcode;//条码
    private String productName;//商品名称
    private Double checkInv;//提交的盘点数


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

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Double getCheckInv() {
        return checkInv;
    }

    public void setCheckInv(Double checkInv) {
        this.checkInv = checkInv;
    }
}

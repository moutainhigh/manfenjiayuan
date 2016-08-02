package com.bingshanguxue.pda.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * 报损
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="pda_invloss_goods_v1")
public class InvLossGoodsEntity extends MfhEntity<Long> implements ILongId{

    private Long proSkuId;//
    private String barcode;//条码
    private String productName;//商品名称
    private Double quantityCheck;//实际签收数量，默认与单据数量一致


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
    
    public Double getQuantityCheck() {
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }
}

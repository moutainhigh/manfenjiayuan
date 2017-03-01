package com.mfh.framework.api.anon.sc.productPrice;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class PubSkus extends MfhEntity<Long> {
//    private Long id;//proSkuId
    private Long productId;
    private String barcode = "";
    private String name = "";


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

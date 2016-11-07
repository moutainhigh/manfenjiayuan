package com.mfh.framework.api.anon.sc.product;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class ScProduct implements Serializable {
    private Long id;//productId
    private String barcode = "";
    private String name = "";


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

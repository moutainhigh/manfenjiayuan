package com.mfh.litecashier.ui.fragment.goods;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 8/1/16.
 */
public class ScGoodsSkuWrapper implements Serializable {

    private String skuName; // 产品名称
    private String barcode; //产品条形码
    private Double costPrice; // 商品售价
    private String unit;            //销售单位，单位，如箱、瓶
    private String namePinyin = "";//拼音
    private String nameSortLetter = "";//排序字段

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNamePinyin() {
        return namePinyin;
    }

    public void setNamePinyin(String namePinyin) {
        this.namePinyin = namePinyin;
    }

    public String getNameSortLetter() {
        return nameSortLetter;
    }

    public void setNameSortLetter(String nameSortLetter) {
        this.nameSortLetter = nameSortLetter;
    }
}

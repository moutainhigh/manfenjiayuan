package com.mfh.litecashier.bean.wrapper;

import com.mfh.comn.bean.ILongId;

import java.util.Date;

/**
 * 报损订单明细
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class LossOrderItem implements ILongId, java.io.Serializable{
    private Long id;
    private Long proSkuId;//产品sku编号
    private String barcode;//条码
    private String name;//商品名称
    private Double quantity;//数量
    private Date createdDate; // 创建日期
    private Date updatedDate; //修改日期


    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
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

    public Double getQuantity() {
        if (quantity == null){
            quantity = 0D;
        }
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
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
}

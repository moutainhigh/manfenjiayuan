package com.bingshanguxue.pda.database.entity;

import com.mfh.framework.core.MfhEntity;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;

/**
 * 收货
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name = "pda_invrecv_goods_v0002")
public class InvRecvGoodsEntity extends MfhEntity<Long> implements ILongId {

    //商品属性＃签收
    private String barcode;//条码
    private String productName;//商品名称
    private String unit;//单位
    private Double receivePrice = 0D;//实际签收价格
    private Double receiveQuantity = 0D;//实际签收数量
    private Double receiveAmount = 0D;//实际签收金额

    //商品属性＃供应商
    private Long chainSkuId;//
    private Long proSkuId;//
    private Long providerId;//供应商编号
    private Integer isPrivate;
    private Double singleCostPrice;//批发商报价
    private Double hintPrice;//批发商建议零售价

    //商品属性＃单据
    private Double receiptPrice = 0D;//单据价格
    private Double receiptQuantity = 0D;//单据数量

    public static final int INSPECT_STATUS_NONE = 0;//未验货
    public static final int INSPECT_STATUS_OK = 1;//已验货，正常
    public static final int INSPECT_STATUS_CONFLICT = 2;//已验货，冲突
    public static final int INSPECT_STATUS_REJECT = 3;//已验货，拒收
    private int inspectStatus = INSPECT_STATUS_NONE;


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

    public int getInspectStatus() {
        return inspectStatus;
    }

    public void setInspectStatus(int inspectStatus) {
        this.inspectStatus = inspectStatus;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Long getChainSkuId() {
        return chainSkuId;
    }

    public void setChainSkuId(Long chainSkuId) {
        this.chainSkuId = chainSkuId;
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


    public Double getReceiptPrice() {
        return receiptPrice;
    }

    public void setReceiptPrice(Double receiptPrice) {
        this.receiptPrice = receiptPrice;
    }

    public Double getReceiptQuantity() {
        return receiptQuantity;
    }

    public void setReceiptQuantity(Double receiptQuantity) {
        this.receiptQuantity = receiptQuantity;
    }

    public Double getReceiveQuantity() {
        if (receiveQuantity == null) {
            return 0D;
        }
        return receiveQuantity;
    }

    public void setReceiveQuantity(Double receiveQuantity) {
        this.receiveQuantity = receiveQuantity;
    }

    public Double getReceiveAmount() {
        if (receiveAmount == null) {
            return 0D;
        }
        return receiveAmount;
    }

    public void setReceiveAmount(Double receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public Double getReceivePrice() {
        if (receivePrice == null){
            return 0D;
        }
        return receivePrice;
    }

    public void setReceivePrice(Double receivePrice) {
        this.receivePrice = receivePrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getSingleCostPrice() {
        return singleCostPrice;
    }

    public void setSingleCostPrice(Double singleCostPrice) {
        this.singleCostPrice = singleCostPrice;
    }

    public Double getHintPrice() {
        return hintPrice;
    }

    public void setHintPrice(Double hintPrice) {
        this.hintPrice = hintPrice;
    }
}

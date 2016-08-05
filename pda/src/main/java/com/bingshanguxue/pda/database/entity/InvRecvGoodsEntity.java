package com.bingshanguxue.pda.database.entity;

import com.mfh.framework.core.MfhEntity;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;

/**
 * 收货
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name = "pda_invrecv_goods_v0001")
public class InvRecvGoodsEntity extends MfhEntity<Long> implements ILongId {
    private Long proSkuId;//
    private Long chainSkuId;//
    private String productName;//商品名称

    //单据
    private Double sendPrice;//发货价格
    private Double sendAmount;//发货总价
    private Double sendQuantity;//发货数量

    private Double receiveQuantity;//实际签收数量
    private Double receiveAmount;//实际签收金额
    private Double receivePrice;//实际签收价格

    private String unitSpec;//单位
    private String barcode;//条码

    private Long providerId;//供应商编号
    private Integer isPrivate;//（0：不是 1：是）

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

    public String getUnitSpec() {
        return unitSpec;
    }

    public void setUnitSpec(String unitSpec) {
        this.unitSpec = unitSpec;
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

    public Double getSendPrice() {
        return sendPrice;
    }

    public void setSendPrice(Double sendPrice) {
        this.sendPrice = sendPrice;
    }

    public Double getSendAmount() {
        return sendAmount;
    }

    public void setSendAmount(Double sendAmount) {
        this.sendAmount = sendAmount;
    }

    public Double getSendQuantity() {
        if (sendQuantity == null) {
            return 0D;
        }
        return sendQuantity;
    }

    public void setSendQuantity(Double sendQuantity) {
        this.sendQuantity = sendQuantity;
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
}

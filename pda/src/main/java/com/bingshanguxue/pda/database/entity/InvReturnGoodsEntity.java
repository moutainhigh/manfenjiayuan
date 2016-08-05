package com.bingshanguxue.pda.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * 退货
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="pda_invreturn_goods_v0001")
public class InvReturnGoodsEntity extends MfhEntity<Long> implements ILongId{
//    private String id;
    private Long orderId;//订单编号
    private Long productId;//
    private Long proSkuId;//
    private Long chainSkuId;//
    private String productName;//商品名称
    private Double totalCount;//单据数量
    private Double price;//价格
    private Double amount;//总价
    private String unitSpec;//单位
    private String barcode;//条码

    private Long providerId;//供应商编号
    private Integer isPrivate;//（0：不是 1：是）

    public static final int INSPECT_STATUS_NONE = 0;//未验货
    public static final int INSPECT_STATUS_OK = 1;//已验货，正常
    public static final int INSPECT_STATUS_CONFLICT = 2;//已验货，冲突
    public static final int INSPECT_STATUS_REJECT = 3;//已验货，拒收
    private int inspectStatus = INSPECT_STATUS_NONE;

    private Double quantityCheck;//实际签收数量，默认与单据数量一致


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
    }

    public Double getPrice() {
//        if (price == null){
//            return 0D;
//        }
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public Double getQuantityCheck() {
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }
}

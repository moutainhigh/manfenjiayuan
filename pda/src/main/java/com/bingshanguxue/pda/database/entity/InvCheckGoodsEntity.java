package com.bingshanguxue.pda.database.entity;

import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * 盘点
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="pda_invcheck_goods_v1")
public class InvCheckGoodsEntity extends MfhEntity<Long> implements ILongId{
    private Long orderId;//盘点批次编号
    private Long goodsId;//商品主键
    private Long productId; //产品编号
    private Long proSkuId; //商品sku编号
    private String barcode;//条码
    private String name;//商品名称
    private Double quantityCheck;//当前库存盘点数量
    private String specNames;//规格名称

    public static final int STATUS_NONE = 0;//未盘点
    public static final int STATUS_FINISHED = 1;//已盘点
    public static final int STATUS_CONFLICT = 2;//冲突
    private int status = STATUS_NONE;

    private Integer syncStatus = L2CSyncStatus.SYNC_STATUS_INIT;


    public static final int HINT_THROW = 0;//告警抛出异常
    public static final int HINT_MERGER = 1;//合并相加
    public static final int HINT_OVERRIDE = 2;//覆盖
    public static final int HINT_IGNORE = 3;//忽略
    private int updateHint = HINT_THROW;

    private Long shelfNumber;//货架编号

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    // Column 'barcode' cannot be null
    public void setBarcode(String barcode) {

        if (barcode == null){
            this.barcode = "";
        }
        else{
            this.barcode = barcode;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantityCheck() {
        if (quantityCheck == null){
            return 0D;
        }
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }

    public String getSpecNames() {
        return specNames;
    }

    public void setSpecNames(String specNames) {
        this.specNames = specNames;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUpdateHint() {
        return updateHint;
    }

    public void setUpdateHint(int updateHint) {
        this.updateHint = updateHint;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Long getShelfNumber() {
        return shelfNumber;
    }

    public void setShelfNumber(Long shelfNumber) {
        this.shelfNumber = shelfNumber;
    }
}

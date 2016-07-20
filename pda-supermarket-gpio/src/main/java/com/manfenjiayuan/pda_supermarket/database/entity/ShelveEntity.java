package com.manfenjiayuan.pda_supermarket.database.entity;

import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * 货架&商品绑定
 * 统一货架可以绑定多个
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="sheleve_v1")
public class ShelveEntity extends MfhEntity<Long> implements ILongId{
    private String rackNo = "";//货架编号
    private String barcode = "";//商品条码
    private Integer syncStatus = L2CSyncStatus.SYNC_STATUS_INIT;


    public String getRackNo() {
        return rackNo;
    }

    public void setRackNo(String rackNo) {
        this.rackNo = rackNo;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }
}

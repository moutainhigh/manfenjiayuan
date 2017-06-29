package com.manfenjiayuan.pda_supermarket.cashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * POS--箱规商品
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="tb_pos_procuct_sku")
public class PosProductSkuEntity extends MfhEntity<Long> implements ILongId{
//    private Long id;//最小商品库存单元编号
//    private Long proSkuId;//产品sku编号
    private String mainBarcode; //主条码
    private String otherBarcode; //其他条码
    private int packFlag;//是否是箱规
    private Long tenantId;

//    public Long getProSkuId() {
//        return proSkuId;
//    }
//
//    public void setProSkuId(Long proSkuId) {
//        this.proSkuId = proSkuId;
//    }

    public String getMainBarcode() {
        return mainBarcode;
    }

    public void setMainBarcode(String mainBarcode) {
        this.mainBarcode = mainBarcode;
    }

    public String getOtherBarcode() {
        return otherBarcode;
    }

    public void setOtherBarcode(String otherBarcode) {
        this.otherBarcode = otherBarcode;
    }

    public int getPackFlag() {
        return packFlag;
    }

    public void setPackFlag(int packFlag) {
        this.packFlag = packFlag;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}

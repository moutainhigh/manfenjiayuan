package com.manfenjiayuan.pda_supermarket.cashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 *
 * <ol>
 *     类目商品关系表
 *     <li></li>
 * </ol>
 * Created by <bingshanguxue> on 16-06-02
 */
@Table(name="tb_product_catalog_v001")
public class ProductCatalogEntity extends MfhEntity<Long> implements ILongId{
    private Long paramValueId;
    private Long cataItemId;//商品编号(spuId/productId)，也可能是关联的后台类目编号
    /**是否和云端同步:默认1同步，0不同步*/
    private int isCloudActive = 1;


    public Long getParamValueId() {
        return paramValueId;
    }

    public void setParamValueId(Long paramValueId) {
        this.paramValueId = paramValueId;
    }

    public Long getCataItemId() {
        return cataItemId;
    }

    public void setCataItemId(Long cataItemId) {
        this.cataItemId = cataItemId;
    }

    public int getIsCloudActive() {
        return isCloudActive;
    }

    public void setIsCloudActive(int isCloudActive) {
        this.isCloudActive = isCloudActive;
    }
}
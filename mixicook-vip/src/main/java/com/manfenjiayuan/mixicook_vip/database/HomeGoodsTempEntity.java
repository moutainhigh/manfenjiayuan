package com.manfenjiayuan.mixicook_vip.database;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 *
 * <ol>
 *     采购订单购物车商品明细，对应{@link com.mfh.framework.api.InvOrderApi#URL_INVSENDORDER_ASK_SENDORDER}
 *     <li>生鲜采购</li>
 * </ol>
 * Created by <bingshanguxue> on 16-06-02
 */
@Table(name="tb_homegoodstemp_v0001_t01")
public class HomeGoodsTempEntity extends MfhEntity<Long> implements ILongId{

    private Long proSkuId;
    private String buyUnit;
    private Double costPrice;
    private Long productId;

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public String getBuyUnit() {
        return buyUnit;
    }

    public void setBuyUnit(String buyUnit) {
        this.buyUnit = buyUnit;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}

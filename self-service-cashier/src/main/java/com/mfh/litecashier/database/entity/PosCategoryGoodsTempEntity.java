package com.mfh.litecashier.database.entity;

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
@Table(name="tb_category_goods_temp_v1")
public class PosCategoryGoodsTempEntity extends MfhEntity<Long> implements ILongId{

    private Long productId;//spuId
    private Long proSkuId;

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
}

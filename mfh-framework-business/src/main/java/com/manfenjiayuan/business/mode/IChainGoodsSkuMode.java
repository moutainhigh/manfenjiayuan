package com.manfenjiayuan.business.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

/**
 * 洗衣类目/采购收货单/采购退货单
 * Created by bingshanguxue on 16/3/17.
 */
public interface IChainGoodsSkuMode<D> {

    /**
     * 查询洗衣类目商品商品
     * @param frontCategoryId 类目编号
     * */
    void loadLaundryGoods(PageInfo pageInfo, Long frontCategoryId, Long netId, OnPageModeListener<D> listener);

    void loadCompanyChainSkuGoods(PageInfo pageInfo, Long frontCategoryId, Long companyId, String barcode,
                                  OnPageModeListener<D> listener);


    /**
     * 获取批发商商品
     * @param companyId 批发商编号
     * */
    void findTenantSku(PageInfo pageInfo, Long companyId, String barcode, OnPageModeListener<D> listener);

}

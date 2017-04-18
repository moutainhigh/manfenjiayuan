package com.mfh.litecashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.ScGoodsSkuHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.litecashier.bean.wrapper.SearchParamsWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购商品:生鲜
 * Created by bingshanguxue on 16/3/17.
 */
public class PurchaseMode {

    /**
     * 加载采购商品
     */
    public void loadPurchaseGoods(PageInfo pageInfo, String categoryId, Long otherTenantId,
                                  String barcode, String nameLike, int sortType, String priceType,
                                  final OnPageModeListener<ScGoodsSku> listener) {

        if (listener != null) {
            listener.onProcess();
        }
        Map<String, String> options = new HashMap<>();
        //类目
        if (!StringUtils.isEmpty(categoryId)) {
            options.put("categoryId", categoryId);
        }
        //批发商
        if (otherTenantId != null) {
            options.put("otherTenantId", String.valueOf(otherTenantId));
        }

        //价格类型0-计件 1-计重
        if (!StringUtils.isEmpty(priceType)) {
            options.put("priceType", priceType);
        }
        //排序
        if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC) {
            options.put("orderby", "quantity");
            options.put("orderbydesc", "true");
        } else if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC) {
            options.put("orderby", "quantity");
            options.put("orderbydesc", "false");
        }
//        else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_DESC){
//            params.put("orderby", "gku.sell_month_num");
//            params.put("orderbydesc", "true");
//        }
//        else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_ASC){
//            params.put("orderby", "gku.sell_month_num");
//            params.put("orderbydesc", "false");
//        }
        //条码
        if (!StringUtils.isEmpty(barcode)) {
            options.put("barcode", barcode);
        }
        if (!StringUtils.isEmpty(nameLike)) {
            options.put("nameLike", nameLike);
        }
        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ScGoodsSkuHttpManager.getInstance().findStoreWithChainSku(options,
                new MQuerySubscriber<ScGoodsSku>(pageInfo) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.df("加载采购商品列表失败:" + e.toString());

                        if (listener != null) {
                            listener.onError(e.toString());
                        }
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }
                });
    }
}

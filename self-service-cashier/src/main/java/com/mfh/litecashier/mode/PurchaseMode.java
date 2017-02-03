package com.mfh.litecashier.mode;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.http.ScGoodsSkuHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.bean.wrapper.SearchParamsWrapper;

import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
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
    public void loadPurchaseGoods(PageInfo pageInfo, Long categoryId, Long otherTenantId,
                                  String barcode, String nameLike, int sortType, String priceType,
                                  final OnPageModeListener<ScGoodsSku> listener) {

        if (listener != null) {
            listener.onProcess();
        }
        if (RxHttpManager.isUseRx){
            Map<String, String> options = new HashMap<>();
            //类目
            if (categoryId != null) {
                options.put("categoryId", String.valueOf(categoryId));
            }
            //批发商
            if (otherTenantId != null) {
                options.put("otherTenantId", String.valueOf(otherTenantId));
            }

            //价格类型0-计件 1-计重
            options.put("priceType", priceType);
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
            options.put("nameLike", nameLike);
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

            ScGoodsSkuHttpManager.getInstance().findStoreWithChainSku(options,
                    new MQuerySubscriber<ScGoodsSku>(pageInfo){
                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            if (listener != null){
                                listener.onError(e.toString());
                            }
                        }

                        @Override
                        public void onQueryNext(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                            super.onQueryNext(pageInfo, dataList);
                            if (listener != null){
                                listener.onSuccess(pageInfo, dataList);
                            }
                        }
                    });
        }
        else{
            AjaxParams params = new AjaxParams();
            //类目
            if (categoryId != null) {
                params.put("categoryId", String.valueOf(categoryId));
            }
            //批发商
            if (otherTenantId != null) {
                params.put("otherTenantId", String.valueOf(otherTenantId));
            }

            //价格类型0-计件 1-计重
            params.put("priceType", priceType);
            //排序
            if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC) {
                params.put("orderby", "quantity");
                params.put("orderbydesc", "true");
            } else if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC) {
                params.put("orderby", "quantity");
                params.put("orderbydesc", "false");
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
                params.put("barcode", barcode);
            }
            params.put("nameLike", nameLike);
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
            params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

            NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                    new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
                        @Override
                        public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                            List<ScGoodsSku> entityList = new ArrayList<>();
                            if (rs != null) {
                                for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                                    entityList.add(wrapper.getBean());
                                }
                            }
                            if (listener != null) {
                                listener.onSuccess(pageInfo, entityList);
                            }
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            ZLogger.df("加载采购商品列表失败:" + errMsg);
                            if (listener != null) {
                                listener.onError(errMsg);
                            }
                        }
                    }, ScGoodsSku.class, CashierApp.getAppContext());

            AfinalFactory.postDefault(ScGoodsSkuApi.URL_FIND_STOREWITHCHAINSKU, params, queryRsCallBack);
        }
    }
}

package com.mfh.litecashier.mode;

import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.ScGoodsSkuApi;
import com.mfh.framework.api.impl.ScGoodsSkuApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetFactory;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.bean.wrapper.SearchParamsWrapper;

import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

/**
 * 库存商品：库存成本，批次流水，库存调拨
 * Created by bingshanguxue on 16/3/17.
 */
public class InventoryGoodsMode implements IInventoryGoodsMode<ScGoodsSku> {

    @Override
    public void loadInventoryGoods(PageInfo pageInfo, String categoryId, String barcode, String name,
                     int sortType, String priceType, final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null){
            listener.onProcess();
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> entityList = new ArrayList<>();
                if (rs != null){
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null){
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载库存商品失败:" + errMsg);
                if (listener != null){
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, CashierApp.getAppContext());

//        价格类型0-计件 1-计重
        //排序
        String orderby = null;
        boolean orderbydesc = false;
        if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC) {
            orderby = "gku.quantity";
            orderbydesc = true;
        } else if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC) {
            orderby = "gku.quantity";
            orderbydesc = false;
        }
        else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_DESC){
            orderby = "gku.sell_month_num";
            orderbydesc = true;
        }
        else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_ASC){
            orderby = "gku.sell_month_num";
            orderbydesc = false;
        }

        ScGoodsSkuApiImpl.listScGoodsSku(pageInfo, categoryId, barcode, name,
                orderby, orderbydesc, false, priceType, queryRsCallBack);
    }

    //TODO 使用MAP优化
    @Override
    public void loadPurchaseGoods(PageInfo pageInfo, String categoryId, Long otherTenantId,
                                  String barcode, String nameLike, int sortType, String priceType,
                                  final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null){
            listener.onProcess();
        }
        AjaxParams params = new AjaxParams();

        //类目
        if (!StringUtils.isEmpty(categoryId)) {
            params.put("categoryId", categoryId);
        }
        //批发商
        if (otherTenantId != null) {
            params.put("otherTenantId", String.valueOf(otherTenantId));
        }

//        价格类型0-计件 1-计重
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
        if (!StringUtils.isEmpty(barcode)){
            params.put("barcode", barcode);
        }
        params.put("nameLike", nameLike);
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                List<ScGoodsSku> entityList = new ArrayList<>();
                if (rs != null){
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null){
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载采购商品列表:" + errMsg);
                if (listener != null){
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, CashierApp.getAppContext());

        NetFactory.getHttp().post(ScGoodsSkuApi.URL_SCGOODSKU_FIND_STOREWITHCHAINSKU, params, queryRsCallBack);
    }

    @Override
    public void checkWithBuyInfoByBarcode(String barcode, final OnModeListener<ScGoodsSku> listener) {
        if (listener != null){
            listener.onProcess();
        }
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //查询失败
                        ZLogger.d("查询商品失败:" + errMsg);
                        if (listener != null){
                            listener.onError(errMsg);
                        }
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        ScGoodsSku goods = null;

                        if (rspData != null) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            goods = retValue.getValue();
                        }

                        if (listener != null){
                            listener.onSuccess(goods);
                        }
                    }
                }
                , ScGoodsSku.class
                , CashierApp.getAppContext()) {
        };
        NetFactory.getHttp(true).post(ScGoodsSkuApi.URL_CHECKWITHBUYINFO_BYBARCODE, params, responseCallback);
    }
}

package com.mfh.litecashier.mode;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.AfinalFactory;
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
public class InventoryGoodsMode {

    /**
     * 获取库存商品
     *
     * @param categoryId 类目编号
     */
    public void listScGoodsSku(PageInfo pageInfo, Long categoryId, String barcode, String name,
                                   int sortType, String priceType, final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
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
                ZLogger.df("加载库存商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, CashierApp.getAppContext());

        String orderby = null;
        boolean orderbydesc = false;
        if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_DESC) {
            orderby = "gku.quantity";
            orderbydesc = true;
        } else if (sortType == SearchParamsWrapper.SORT_BY_STOCK_QUANTITY_ASC) {
            orderby = "gku.quantity";
            orderbydesc = false;
        } else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_DESC) {
            orderby = "gku.sell_month_num";
            orderbydesc = true;
        } else if (sortType == SearchParamsWrapper.SORT_BY_MONTHLY_SALES_ASC) {
            orderby = "gku.sell_month_num";
            orderbydesc = false;
        }

        ScGoodsSkuApiImpl.listScGoodsSku(pageInfo, categoryId, barcode, name,
                orderby, orderbydesc, false, priceType, queryRsCallBack);
    }

    /**
     * 根据条码查找租户是否已经发布过该商品，若存在返回信息
     */
    public void checkWithBuyInfoByBarcode(String barcode, final OnModeListener<ScGoodsSku> listener) {
        if (listener != null) {
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
                        ZLogger.df("查询商品失败:" + errMsg);
                        if (listener != null) {
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

                        if (listener != null) {
                            listener.onSuccess(goods);
                        }
                    }
                }
                , ScGoodsSku.class
                , CashierApp.getAppContext()) {
        };
        AfinalFactory.getHttp(true).post(ScGoodsSkuApi.URL_CHECKWITHBUYINFO_BYBARCODE, params, responseCallback);
    }
}

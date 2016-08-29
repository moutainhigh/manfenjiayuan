package com.mfh.framework.api.impl;

import com.mfh.framework.api.StockApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 库存Api
 * Created by bingshanguxue on 4/22/16.
 */
public class StockApiImpl extends StockApi {
    /**
     * 查找可出库的或已出库的包裹列表
     *
     * @param curStock 是否查询当前仓储中的物件,true-可出库;false-已出库
     * @param humanId  明确查询某个人的包裹，可空
     * @param itemType 查询何种包裹,可空，1-洗衣包裹 2-快递包裹 3-商城包裹 6-皮具包裹
     */
    public static void findStockOut(boolean curStock, String humanId, String itemType, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("curStock", String.valueOf(curStock));
        if (!StringUtils.isEmpty(humanId)) {
            params.put("humanId", humanId);
        }
        if (!StringUtils.isEmpty(itemType)) {
            params.put("itemType", itemType);
        }
        params.put("stockIds", MfhLoginService.get().getStockIds());//stockIds:针对哪些仓库，逗号分隔
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//        fh.addHeader("Cookie", SharedPreferencesManager.getLastSessionId());
        AfinalFactory.postDefault(URL_STOCK_FIND_STOCKOUT, params, responseCallback);
    }

    /**
     * 查找可出库的或已出库的包裹列表
     *
     * @param curStock 是否查询当前仓储中的物件,true-可出库;false-已出库
     * @param humanId  明确查询某个人的包裹，可空
     * @param itemType 查询何种包裹,可空，1-洗衣包裹 2-快递包裹 3-商城包裹 6-皮具包裹
     */
    public static void findStockOutByCode(String queryCon, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("curStock", "true");
        if (!StringUtils.isEmpty(queryCon)) {
            params.put("queryCon", queryCon);
        }
        params.put("stockIds", MfhLoginService.get().getStockIds());//stockIds:针对哪些仓库，逗号分隔
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_STOCK_FIND_STOCKOUT, params, responseCallback);
    }

    /**
     * 出库
     */
    public static void stockOut(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_STOCK_OUT, params, responseCallback);
    }

    /**
     * 加载批次明细:
     */
    public static void findStockOut(Long batchId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("batchId", String.valueOf(batchId));//查询指定批次
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_STOCK_FIND_STOCKOUT, params, responseCallback);
    }

}

package com.mfh.framework.api.pmcstock;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 17/10/2016.
 */

public class PmcStockApiImpl extends PmcStockApi {
    public static void findHumanBySecret(String secret, String stockId,
                                         AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("secret", secret);
        params.put("stockId", stockId);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_STOCK_FINDHUMAN_BYSECRET, params, responseCallback);
    }
    /**
     * 根据订单编号，查询订单基本信息和详情包括订单的商品明细{@link #URL_FINDGOODSORDERLIST_BYHUMAN}
     * @param id 订单编号
     * */
    public static void getGoodsOrderListByHuman(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDGOODSORDERLIST_BYHUMAN, params, responseCallback);
    }
    /**
     * 根据订单编号，查询订单基本信息和详情包括订单的商品明细{@link #URL_FINDGOODSORDERLIST_BYHUMAN}
     * @param barcode 订单条码
     * @param humanId 人员编号
     * */
    public static void getGoodsOrderListByHuman(String barcode, Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put("status", "0");
        params.put("humanId", String.valueOf(humanId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDGOODSORDERLIST_BYHUMAN, params, responseCallback);
    }
}

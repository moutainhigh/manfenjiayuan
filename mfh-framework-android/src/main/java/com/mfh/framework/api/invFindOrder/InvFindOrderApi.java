package com.mfh.framework.api.invFindOrder;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 拣货单
 * Created by bingshanguxue on 17/10/2016.
 */

public class InvFindOrderApi {
    private final static String URL_INVFINDORDER = MfhApi.URL_BASE_SERVER + "/invFindOrder/";

    /**
     * 根据拣货单编号或条码检索一个拣货单及其所有明细
     *  /invFindOrder/getById?id=|barcode=451201106160530006
     *  注意拣货单的条码是4开头
     */
    public final static String URL_GETBYID = URL_INVFINDORDER + "getById";


    /**
     * 根据拣货单编号或条码检索一个拣货单及其所有明细
     * {@link #URL_GETBYID InvFindOrder}
     */
    public static void getById(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (id != null) {
            params.put("id", String.valueOf(id));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GETBYID, params, responseCallback);
    }

    /**
     * 根据拣货单编号或条码检索一个拣货单及其所有明细
     * @param barcode 单据条码，采购单以“4”开头。
     */
    public static void getById(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(barcode)) {
            params.put("barcode", barcode);
        }
        params.put("wrapper", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GETBYID, params, responseCallback);
    }
}

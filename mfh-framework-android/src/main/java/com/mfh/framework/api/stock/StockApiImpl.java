package com.mfh.framework.api.stock;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 04/11/2016.
 */

public class StockApiImpl extends StockApi{
    public static void findHumanInfoByMobile(String mobile, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("companyId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("mobile", mobile);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_RECEIVEORDER_FINDHUMANINFO_BYMOBILE, params, responseCallback);
    }

    /**
     * 创建批次
     */
    public static void receiveBatchCreateAndFee(String stockId, Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stockId", stockId);
        jsonObject.put("humanId", humanId);
        jsonObject.put("stockType", 2);//1-订单类 2-快递类 99-库存类
        jsonObject.put("income", 0);
        jsonObject.put("accountPassword", "");

        params.put("jsonStr", jsonObject.toJSONString());

        AfinalFactory.postDefault(URL_RECEIVEBATCH_CREATEANDFEE, params, responseCallback);
    }

    public static void findCompanyByHumanId(Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_RECEIVEBATCH_FINDCOMPANY_BYHUMANID, params, responseCallback);
    }

    /**
     * 入库
     */
    public static void receiveOrderStockInItems(String batchId, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("batchId", batchId);
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_RECEIVEORDER_STOCKINITEMS, params, responseCallback);
    }
    /**
     * 新增快递公司
     * humanId参数不能为空!
     */
    public static void receiveBatchSaveHumanFDCompany(Long humanId, Long companyId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("companyId", companyId == null ? "" : String.valueOf(companyId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_RECEIVEBATCH_SAVEHUMANFDCOMPANY, params, responseCallback);
    }

}

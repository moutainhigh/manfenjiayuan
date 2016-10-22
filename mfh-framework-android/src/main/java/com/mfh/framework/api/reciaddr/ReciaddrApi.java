package com.mfh.framework.api.reciaddr;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 收货地址
 * Created by bingshanguxue on 08/10/2016.
 */

public class ReciaddrApi {
    private final static String URL_IRECIADDR = MfhApi.URL_BASE_SERVER + "/reciaddr/";
    /**
     * 查询所有收货地址
     * http://admin.mixicook.com/pmc/reciaddr/getDefaultAddrsByHuman?humanId=136060
     * */
    private final static String URL_GETALLADDRS_BYHUMAN = URL_IRECIADDR + "getAllAddrsByHuman";
    /**
     * 查询默认收货地址
     * http://admin.mixicook.com/pmc/reciaddr/getDefaultAddrsByHuman?humanId=136060
     * */
    private final static String URL_GETDEFAULTADDRS_BYHUMAN = URL_IRECIADDR + "getDefaultAddrsByHuman";

    /**
     * 创建收货地址
     * http://admin.mixicook.com/pmc/reciaddr/createForHuman?humanId=136060
     * */
    private final static String URL_CREATEFORHUMAN = URL_IRECIADDR + "createForHuman";

    /**
     * 编辑收货地址
     * http://admin.mixicook.com/pmc/reciaddr/createForHuman?humanId=136060
     * */
    private final static String URL_UPDATEFORHUMAN = URL_IRECIADDR + "updateForHuman";

    /**
     * 删除收货地址
     * http://admin.mixicook.com/pmc/reciaddr/delete?humanId=136060
     * */
    private final static String URL_DELETE = URL_IRECIADDR + "delete";


    /**
     * 设置默认收货地址
     * http://admin.mixicook.com/pmc/reciaddr/setDefaultAddrById?humanId=136060
     * */
    private final static String URL_SETDEFAULTADDR_BYID = URL_IRECIADDR + "setDefaultAddrById";


    /**
     * 查询收货地址
     * @param humanId 人员编号
     * */
    public static void getAllAddrsByHuman(Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));

////        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_GETALLADDRS_BYHUMAN, params, responseCallback);
    }

    /**
     * 查询默认收货地址
     * @param humanId 人员编号
     * */
    public static void getDefaultAddrsByHuman(Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));

////        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_GETDEFAULTADDRS_BYHUMAN, params, responseCallback);
    }

    /**
     * 创建收货地址
     * @param humanId 人员编号
     * */
    public static void createForHuman(Long humanId, JSONObject jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("jsonStr", jsonStr.toJSONString());

////        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_CREATEFORHUMAN, params, responseCallback);
    }

    /**
     * 编辑收货地址
     * @param humanId 人员编号
     * */
    public static void updateForHuman(Long humanId, JSONObject jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("jsonStr", jsonStr.toJSONString());

////        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_UPDATEFORHUMAN, params, responseCallback);
    }

    /**
     * 删除收货地址
     * @param id 地址编号
     * */
    public static void delete(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));

////        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_DELETE, params, responseCallback);
    }

    /**
     * 设置默认收货地址
     * @param id 地址编号
     * */
    public static void setDefaultAddrById(Long humanId, Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put("humanId", String.valueOf(humanId));

////        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_SETDEFAULTADDR_BYID, params, responseCallback);
    }
}

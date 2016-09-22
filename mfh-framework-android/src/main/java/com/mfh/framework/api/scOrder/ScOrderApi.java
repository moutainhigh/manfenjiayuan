package com.mfh.framework.api.scOrder;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import static com.mfh.framework.api.MfhApi.PARAM_KEY_JSESSIONID;

/**
 * 商城订单
 * Created by bingshanguxue on 9/22/16.
 */
public class ScOrderApi {
    private final static String URL_SCORDER = MfhApi.URL_BASE_SERVER + "/scOrder/";

    /**
     * 当前登录人员即发货人员，自己进行发货
     * /scOrder/sendOrder?orderId=
     */
    public static final String URL_SENDORDER = URL_SCORDER + "sendOrder";
    /**
     * 新增商城订单
     * */
    public final static String URL_SAVEORDER = URL_SCORDER + "saveOrder";
    /**
     * 查询满分小伙伴服务
     * */
    public final static String URL_COUNT_SERVICEMFHPARTER= URL_SCORDER + "countServiceMfhPartner";
    /**
     * 查询订单
     * /scOrder/getByCode?barcode=9903000000273899
     * */
    public final static String URL_GETBYCODE = URL_SCORDER + "getByCode";

    /**
     * 发货
     * http://devnew.manfenjiayuan.cn/pmc/scOrder/sendOrder?JSESSIONID=9d01cbf0-059b-4ee9-9391-1612e9276165&orderId=557612
     * 1:orderId参数不能为空!
     */
    public static void sendOrder(Long orderId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderId", orderId == null ? "" : String.valueOf(orderId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_SENDORDER, params, responseCallback);
    }

    /**
     * 新增商城订单
     * */
    public static void saveOrder(String order, String items, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("order", order);
        params.put("items", items);
        params.put("needAmount", "true");

        AfinalFactory.postDefault(URL_SAVEORDER, params, responseCallback);
    }

    /**
     * 查询满分小伙伴服务
     * */
    public static void countServiceMfhPartner(AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_COUNT_SERVICEMFHPARTER, params, responseCallback);
    }

    /**
     * 发货
     * http://devnew.manfenjiayuan.cn/pmc/scOrder/sendOrder?JSESSIONID=9d01cbf0-059b-4ee9-9391-1612e9276165&orderId=557612
     * 1:orderId参数不能为空!
     */
    public static void getByCode(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GETBYCODE, params, responseCallback);
    }

}

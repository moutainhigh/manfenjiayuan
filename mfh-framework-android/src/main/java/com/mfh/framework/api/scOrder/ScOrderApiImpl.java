package com.mfh.framework.api.scOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 17/10/2016.
 */

public class ScOrderApiImpl extends ScOrderApi{
    /**
     * 根据条码查询待拣货订单{@link #URL_GETBYBARCODE}
     * @param barcode 订单条码
     * @param status 订单状态
     * @param needDetail 是否返回订单明细详情
     */
    public static void getByBarcode(String barcode, Integer status, boolean needDetail,
                                    AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        if (status != null){
            params.put("status", String.valueOf(status));
        }
        params.put("needDetail", String.valueOf(needDetail));
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GETBYBARCODE, params, responseCallback);
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表{@link #URL_FINDPREPAREABLEORDERS}
     *
     */
    public static void findPrepareAbleOrders(PageInfo pageInfo,
                                             AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put("wrapper", "true");
        params.put("needDetail", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDPREPAREABLEORDERS, params, responseCallback);
    }

    /**
     * 当前登录人员即买手或发货人员，选择一个骑手并进行发货，并且通知骑手
     * @param id 订单编号
     * @param jsonStr 订单明细
     */
    public static void updateCommitInfo(Long id, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put("jsonStr", jsonStr);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_UPDATECOMMITINFO, params, responseCallback);
    }

    /**
     * 当前登录人员即买手或发货人员，选择一个骑手并进行发货，并且通知骑手
     * @param orderId 订单编号
     * @param buyerId 买手编号,可以为空
     * @param transHumanId 骑手编号,可以为空
     *
     */
    public static void prepareOrder(Long orderId, Long buyerId, Long transHumanId,
                                    AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderId", String.valueOf(orderId));
        if (buyerId != null){
            params.put("buyerId", String.valueOf(buyerId));
        }
        if (transHumanId != null){
            params.put("transHumanId", String.valueOf(transHumanId));
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_PREPAREORDER, params, responseCallback);
    }

    /**
     * 当前登录的小伙伴即买手收到消息后点击接单，选择一个骑手并进行发货，并且通知骑手
     * @param id 订单编号
     *
     */
    public static void acceptOrderWhenOrdered(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_ACCEPTORDER_WHENORDERED, params, responseCallback);
    }

    /**
     * 获取当前登录骑手待配送的订单列表{@link #URL_FINDSENDABLEORDERS}
     *
     */
    public static void findSendAbleOrders(PageInfo pageInfo,AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put("wrapper", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDSENDABLEORDERS, params, responseCallback);
    }

    /**
     * 获取指定网点可配送抢单的订单列表，netId若不传则使用当前登录骑手所在网点{@link #URL_FINDACCEPTABLE_SENDORDERS}
     *
     */
    public static void findAcceptAbleSendOrders(PageInfo pageInfo,AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put("wrapper", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDACCEPTABLE_SENDORDERS, params, responseCallback);
    }
    /**
     * 当前登录人员即骑手或配送人员，接单，并开始进行配送物流
     * @param orderId 订单编号
     */
    public static void acceptTransToEndCustom(Long orderId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderId", String.valueOf(orderId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_ACCEPTTRANS_TOENDCUSTOM, params, responseCallback);
    }

    /**
     * 判断是否需要补差价；返回差额，正值代表需要退钱给用户，负值代表需要用户补钱
     * @param id 订单编号
     */
    public static void checkOddAmount(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CHECK_ODDAMOUNT, params, responseCallback);
    }

    /**
     * 若订单需要补差价时，当前登录的骑手调用扫用户码该接口执行补收货款（其实退款给客户也支持）
     * @param id 订单编号
     * @param humanId 被扫码的用户id。
     */
    public static void checkAndReturnOddAmount(Long id, Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put("humanId", String.valueOf(humanId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CHECKANDRETURN_ODDAMOUNT, params, responseCallback);
    }

    /**
     * 若订单需要补差价时，当前登录的骑手调用扫用户码该接口执行补收货款（其实退款给客户也支持）
     * @param id 订单编号
     * @param payType 支付方式{@link com.mfh.framework.api.constant.WayType#ALI_F2F}
     */
    public static void checkAndReturnOddAmount(Long id, Integer payType, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put("payType", String.valueOf(payType));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CHECKANDRETURN_ODDAMOUNT, params, responseCallback);
    }

    /**
     * 当前登录人员即骑手或配送人员，接单，并开始进行配送物流
     * @param orderIds 订单编号,逗号隔开
     */
    public static void arriveToEndCustom(String orderIds, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderIds", orderIds);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_ARRIVE_TOENDCUSTOM, params, responseCallback);
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表{@link #URL_FINDPREPAREABLEORDERS}
     *
     */
    public static void findServicingOrders(PageInfo pageInfo, int roleType,
                                           AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put("roleType", String.valueOf(roleType));
        params.put("wrapper", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_SERVICINGORDERS, params, responseCallback);
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表{@link #URL_FINDPREPAREABLEORDERS}
     * @param roleType roleType=0代表买手，roleType=1代表骑手
     */
    public static void findServicedOrders(PageInfo pageInfo, int roleType, String status,
                                          AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        if (!StringUtils.isEmpty(status)){
            params.put("status", status);
        }
        params.put("roleType", String.valueOf(roleType));
        params.put("wrapper", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_SERVICEDORDERS, params, responseCallback);
    }

    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表{@link #URL_FINDPREPAREABLEORDERS}
     * @param roleType roleType=0代表买手，roleType=1代表骑手
     */
    public static void findCancelOrders(PageInfo pageInfo, int roleType,
                                          AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put("roleType", String.valueOf(roleType));
        params.put("wrapper", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_CANCELORDERS, params, responseCallback);
    }

}

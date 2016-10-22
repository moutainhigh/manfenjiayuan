package com.mfh.framework.api.invSendOrder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 采购订单&预订单
 * Created by bingshanguxue on 6/15/16.
 */
public class InvSendOrderApiImpl extends InvSendOrderApi {

    /**
     * 获取一个采购单的明细信息
     * {@link #URL_INVSENDORDER_GETBYID InvOrderApi}
     * 里面的items为明细信息
     * @param id
     */
    public static void getById(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (id != null) {
            params.put("id", String.valueOf(id));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDORDER_GETBYID, params, responseCallback);
    }

    /**
     * @param barcode 单据条码，采购单以“1”开头。
     * */
    public static void getByBarcode(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(barcode)) {
            params.put("barcode", barcode);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDORDER_GETBYID, params, responseCallback);
    }

    /**
     * 门店人员对指定的收货单(批发商发货过来的)进行签收,同时可以进行支付；同时生成入库单进行入库。
     * 此时有多少货收多少货，实际数量不对也得收；有不对的后面按退货或报损流程处理。
     * /invSendOrder/doReceive 若accountPassword为null代表先不支付;
     * /invSendOrder/doReceive?orderId=1
     */
    public static void doReceive(Long orderId, String accountPassword, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (orderId != null) {
            params.put("orderId", String.valueOf(orderId));
        }
        if (!StringUtils.isEmpty(accountPassword)) {
            params.put("accountPassword", accountPassword);
        }
        if (!StringUtils.isEmpty(jsonStr)) {
            params.put("jsonStr", jsonStr);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDORDER_DORECEIVE, params, responseCallback);
    }

    /**
     * 查询采销单，便于生成收发单。
     * /invSendOrder/list?netFlag=true&netId=&statuss=&status=
     * statuss-查询多个状态；status-查询一个状态
     * @param netId 网点编号
     * @param netFlag true－－门店； false－－供货方
     * @param status 多个状态用逗号分隔。(0-初始 1-审核通过 2-部分发货收货 4-全部结束)
     */
    public static void list(Long netId, boolean netFlag, String status,
                                       PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(netId));
        params.put("netFlag", String.valueOf(netFlag));

        if (status != null && status.contains(",")) {
            params.put("statuss", status);
        } else {
            params.put("status", status);
        }
//        if (!StringUtils.isEmpty(sendTenantId)) {
//            params.put("sendTenantId", sendTenantId);
//        }
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_INVSENDORDER_LIST, params, responseCallback);
    }

    /**
     * 查询采购订单列表
     * /invSendOrder/list?netFlag=true&netId=&statuss=&status=
     * statuss-查询多个状态；status-查询一个状态
     * @param receiveNetId 当前登录网点编号，不能为空
     * @param sendType {@see #SENDTYPE_CUSTOMER_MANUAL}
     * @param status 多个状态用逗号分隔。(0-初始 1-审核通过 2-部分发货收货 4-全部结束)
     * 适用场景：门店查询客户生鲜预定订单
     */
    public static void listInvSendOrdes2(Long receiveNetId, String sendType, String status,
                                       PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("receiveNetId", String.valueOf(receiveNetId));
        if (!StringUtils.isEmpty(sendType)){
            params.put("sendType", sendType);
        }
        if (status != null && status.contains(",")) {
            params.put("statuss", status);
        } else {
            params.put("status", status);
        }
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_INVSENDORDER_LIST, params, responseCallback);
    }
    public static void listInvSendOrdes2(Long receiveNetId, String sendType, String status,
                                         String receiveMobile, PageInfo pageInfo,
                                         AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("receiveNetId", String.valueOf(receiveNetId));
        if (!StringUtils.isEmpty(sendType)){
            params.put("sendType", sendType);
        }
        if (status != null && status.contains(",")) {
            params.put("statuss", status);
        } else {
            params.put("status", status);
        }
        if (!StringUtils.isEmpty(receiveMobile)){
            params.put("receiveMobile", receiveMobile);
        }
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_INVSENDORDER_LIST, params, responseCallback);
    }
    /**
     * 门店对客户的订购单进行收货确认并通知客户前来取货，并设置存放的货架号
     * */
    public static void receivePlanOrderOnNet(Long id, String rackNo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        if (!StringUtils.isEmpty(rackNo)){
            params.put("rackNo", rackNo);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_INVSENDORDER_RECEIVEPLANORDERONNET, params, responseCallback);
    }

    /**
     * 取消订单
     * @param id 订单编号
     * @param clause 取消订单原因
     * 适用场景：门店取消预定订单并通知用户。
     * */
    public static void cancelById(Long id, String clause,
                                       AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        if (!StringUtils.isEmpty(clause)){
            params.put("clause", clause);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_INVSENDORDER_CANCEL, params, responseCallback);
    }

    /**
     * 创建预定单
     * @param sendTenantId 发货网点
     * @param sendDate 发货时间(不能为空)
     * @param remark 备注
     * @param receiveNetId 收货网点
     * @param items 订单明细
     * 适用场景：门店取消预定订单并通知用户。
     * */
    public static void createPlanOrder(Long sendTenantId, String sendDate, String remark,
                                       Long receiveNetId, JSONArray items,
                                       AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sendTenantId", sendTenantId);
        jsonObject.put("sendDate", sendDate);
        jsonObject.put("remark", remark);
        jsonObject.put("netId", receiveNetId);
        jsonObject.put("items", items);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_INVSENDORDER_CREATE_PLANORDER, params, responseCallback);
    }

    /**
     * 商品申请生成订单
     * public static int STATUS_INIT = 0;//生成
     * public static int STATUS_CONFIRM = 1;//审核通过
     * public static int STATUS_SENDED = 2;//已发货
     * public static int STATUS_ON_TRANS = 3;//在途中
     * public static int STATUS_RECEIVE = 4;//已签收
     * public static int STATUS_CANCEL = 9;//已取消
     */
    public static void askSendOrder(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_INVSENDORDER_ASK_SENDORDER, params, responseCallback);
    }

}

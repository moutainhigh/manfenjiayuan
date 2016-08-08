package com.mfh.framework.api.invSendIoOrder;

import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 7/26/16.
 */
public class InvSendIoOrderApiImpl extends InvSendIoOrderApi{

    /**
     * 创建正常发货单
     * 适用场景：门店调拨 & 批发商发货
     */
    public static void createInvSendIoOrder(boolean checkOk, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("checkOk", String.valueOf(checkOk));
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_CREATE, params, responseCallback);
    }

    /**
     * 由门店自行创建收货单（此时因批发商不在平台上故没办法发货）
     * /invSendIoOrder/createRecOrder
     * sendNetId参数不要传递，sendTenantId可以从采购单中拷贝，本身也可以置空；
     * receiveNetId、tenantId可以从采购单中拷贝，没有采购单则可以置空，若置空后台也会自动根据当前登录用户的部门和租户进行填充。
     * items是发货单明细，字段与采购单明细字段基本一致，从采购单中拷贝即可，其中giveCount是赠送数量。没有采购单时需要从头选择商品。
     *
     * @param otherOrderId 采购单订单编号，此处可以为空.
     * @param checkOk      否直接自动审核通过并生成入库单,进一步地如果收货网点就是当前操作用户的登录网点，则还会自动对生成的入库单进行入库操作。
     * @param receiveNetId 可以从采购单中拷贝，没有采购单则可以置空，若置空后台也会自动根据当前登录用户的部门和租户进行填充。
     * @param tenantId     可以从采购单中拷贝，没有采购单则可以置空，若置空后台也会自动根据当前登录用户的部门和租户进行填充。
     */
    public static void createInvSendIoRecOrder(Long otherOrderId, boolean checkOk,
                                               String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (otherOrderId != null) {
            params.put("otherOrderId", String.valueOf(otherOrderId));
        }
        params.put("checkOk", String.valueOf(checkOk));
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_CREATE_RECORDER, params, responseCallback);
    }

    /**
     * 新建退货单
     */
    public static void createBackOrder(Long otherOrderId, boolean checkOk, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (otherOrderId != null) {
            params.put("otherOrderId", String.valueOf(otherOrderId));
        }
        params.put("checkOk", String.valueOf(checkOk));
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CREATE_BACKORDER, params, responseCallback);
    }

    /**
     * 新建调拨单
     */
    public static void createInvSendIoTransOrder(boolean checkOk, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("checkOk", String.valueOf(checkOk));
        params.put("jsonStr", jsonStr);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_CREATE_TRANSORDER, params, responseCallback);
    }

    /**
     * 获取一个收发单详情
     * {@link com.mfh.framework.api.InvOrderApi#URL_INVSENDIOORDER_GETBYID InvOrderApi}
     */
    public static void getInvSendIoOrderById(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (id != null) {
            params.put("id", String.valueOf(id));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_GETBYID, params, responseCallback);
    }

    /**
     * 获取一个收发单详情
     * @param barcode 单据条码，采购单以“2”开头。
     */
    public static void getInvSendIoOrderByBarcode(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(barcode)) {
            params.put("barcode", barcode);
        }
        params.put("wrapper", "true");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_GETBYID, params, responseCallback);
    }

    /**
     * 获取一个收发单详情
     */
    public static void doConfirmInvSendIoOrder(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (id != null) {
            params.put("id", String.valueOf(id));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_DOCONFIRM, params, responseCallback);
    }

    public static void doReceiveInvSendIoOrder(String orderId, String accountPassword, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(orderId)) {
            params.put("orderId", orderId);
        }
        if (!StringUtils.isEmpty(accountPassword)) {
            params.put("accountPassword", accountPassword);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_DORECEIVE, params, responseCallback);
    }


    /**
     * 门店人员对指定的采购配送单手工执行支付：
     * /invSendOrder/doPay?orderId=19&accountPassword=
     * <p/>
     * 1:accountPassword参数不能为空!
     */
    public static void doPayInvSendIoOrder(String orderId, String accountPassword, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderId", orderId);
        params.put("accountPassword", accountPassword);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_INVSENDIOORDER_DOPAY, params, responseCallback);
    }

}

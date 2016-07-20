package com.mfh.litecashier.mode;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetFactory;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.bean.InvTransOrder;

import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

/**
 * 库存调拨订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvTransOrderMode implements IInvTransOrderMode<InvTransOrder> {

    /**
     * 加载采购收货订单列表
     * @param pageInfo
     * @param status 订单状态，可以为空，为空时表示查询所有状态
     * @param payStatus 支付状态，可以为空，为空时表示查询所有状态。
     * @param sendTenantId 发货方网点编号，可以为空，为空时表示查询所有发货发。
     * @param listener
     * */
    @Override
    public void loadOrders(PageInfo pageInfo, boolean netFlag, final OnPageModeListener<InvTransOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        AjaxParams params = new AjaxParams();
//        if (!StringUtils.isEmpty(status)){
//            if (status.contains(",")) {
//                params.put("statuss", status);
//            } else {
//                params.put("status", status);
//            }
//
//            //查询当前网点可以操作待待审核订单
//            if (status.contains("0")) {
//                params.put("sendNetIdNull", "1");
//            }
//        }
//
//        if (!StringUtils.isEmpty(payStatus)) {
//            params.put("payStatus", payStatus);
//        }

//        if (!StringUtils.isEmpty(sendTenantId)) {
//            params.put("sendTenantId", sendTenantId);
//        }

        //待审核订单，只查询当前网点的
        params.put("netFlag", String.valueOf(netFlag));//门店方(true),供货方(false)
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));//收货网点
        params.put("bizType", String.valueOf(InvOrderApi.BIZTYPE_PURCHASE));
        params.put("orderType", String.valueOf(InvOrderApi.ORDERTYPE_RECEIPT));
        params.put("sendStoreType", String.valueOf(InvOrderApi.SENDSTORE_TYPE_RETAIL));
//        params.put("receiveNetId", String.valueOf(MfhLoginService.get().getCurOfficeId()));//收货网点
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));//收货网点所属租户
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvTransOrder>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvTransOrder> rs) {
                //此处在主线程中执行。
                List<InvTransOrder> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<InvTransOrder> wrapper : rs.getRowDatas()) {
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
                //{"code":"1","msg":"null","version":"1","data":null}
                ZLogger.d("加载收货订单失败:" + errMsg);if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, InvTransOrder.class, CashierApp.getAppContext());

        NetFactory.getHttp().post(InvOrderApi.URL_INVSENDIOORDER_LIST, params, queryRsCallBack);
    }
}

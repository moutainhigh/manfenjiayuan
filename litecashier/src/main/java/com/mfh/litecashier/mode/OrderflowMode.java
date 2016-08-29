package com.mfh.litecashier.mode;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.CashierApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetFactory;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.bean.PosOrder;

import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单流水:门店收银/线上销售/衣服洗护/快递代发
 * Created by bingshanguxue on 16/3/17.
 */
public class OrderflowMode implements IOrderflowMode<PosOrder>{

    @Override
    public void loadOrders(Integer btype, String orderStatus, Long sellOffices, PageInfo pageInfo, final OnPageModeListener<PosOrder> listener) {
        if (listener != null){
            listener.onProcess();
        }
        ZLogger.d(String.format("加载订单流水开始:page=%d/%d", pageInfo.getPageNo(), pageInfo.getTotalPage()));

        AjaxParams params = new AjaxParams();
        params.put("orderStatus", orderStatus);
        params.put("btype", String.valueOf(btype));
        params.put("sellOffices", String.valueOf(sellOffices));
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<PosOrder>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<PosOrder> rs) {
                List<PosOrder> entityList = new ArrayList<>();
                if (rs != null){
                    for (EntityWrapper<PosOrder> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null){
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载订单流水失败:" + errMsg);
                if (listener != null){
                    listener.onError(errMsg);
                }
            }
        }, PosOrder.class, CashierApp.getAppContext());

        NetFactory.getHttp().post(CashierApi.URL_STOCK_FIND_GOODS_ORDERLIST, params, queryRsCallBack);
    }
}

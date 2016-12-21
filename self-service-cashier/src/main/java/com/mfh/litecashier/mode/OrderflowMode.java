package com.mfh.litecashier.mode;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.pmcstock.PmcStockApiImpl;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.bingshanguxue.cashier.model.PosOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单流水:门店收银/线上销售/衣服洗护/快递代发
 * Created by bingshanguxue on 16/3/17.
 */
public class OrderflowMode {

    public void findGoodsOrderList(Integer btype, String subTypes, String orderStatus, String sellOffices,
                                   PageInfo pageInfo, final OnPageModeListener<PosOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<PosOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<PosOrder> rs) {
                        List<PosOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<PosOrder> wrapper : rs.getRowDatas()) {
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
                        ZLogger.df("加载流水失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, PosOrder.class, CashierApp.getAppContext());

        PmcStockApiImpl.findGoodsOrderList(btype, subTypes, orderStatus,
                sellOffices, pageInfo, queryRsCallBack);
    }
}

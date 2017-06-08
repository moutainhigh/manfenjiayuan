package com.mfh.litecashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.pmcstock.PosOrder;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, String> options = new HashMap<>();
        if (btype != null){
            options.put("btype", String.valueOf(btype));
        }
        if (!StringUtils.isEmpty(subTypes)){
            options.put("subTypes", subTypes);
        }
        if (!StringUtils.isEmpty(orderStatus)){
            options.put("orderStatus", orderStatus);
        }
        if (!StringUtils.isEmpty(sellOffices)){
            options.put("sellOffices", sellOffices);
        }
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().findGoodsOrderList(options,
                new MQuerySubscriber<PosOrder>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<PosOrder> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.ef("加载流水失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.toString());
                        }
                    }
                });
    }
}

package com.mfh.framework.api.invLossOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.httpmgr.InvLossOrderHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 19/11/2016.
 */

public class InvLossOrderMode {

    /**
     * 盘点列表
     */
    public void list(PageInfo pageInfo, final OnPageModeListener<InvLossOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        InvLossOrderHttpManager.getInstance().list(options,
                new MQuerySubscriber<MEntityWrapper<InvLossOrder>>(pageInfo) {
//                        @Override
//                        public void onQueryNext(PageInfo pageInfo, List<InvLossOrder> dataList) {
//                            super.onQueryNext(pageInfo, dataList);
//                            findCompUserPwdInfoStep3(dataList, pageInfo);
//                        }


                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MEntityWrapper<InvLossOrder>> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        List<InvLossOrder> entityList = new ArrayList<>();
                        if (dataList != null) {
                            for (MEntityWrapper<InvLossOrder> wrapper : dataList) {
                                InvLossOrder invLossOrder = wrapper.getBean();
                                Map<String, String> caption = wrapper.getCaption();
                                if (caption != null) {
                                    invLossOrder.setStatusCaption(caption.get("status"));
                                }
                                entityList.add(invLossOrder);
                            }
                        }

                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.ef("加载报损订单失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }
                });
    }
}

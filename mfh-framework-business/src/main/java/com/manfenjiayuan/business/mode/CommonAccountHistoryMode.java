package com.manfenjiayuan.business.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.bean.CommonAccountFlow;
import com.mfh.framework.rxapi.http.CommonAccountHistoryHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 19/11/2016.
 */

public class CommonAccountHistoryMode {

    /**
     * 盘点列表
     */
    public void queryCustomerFlow(Long humanId, PageInfo pageInfo, final OnPageModeListener<CommonAccountFlow> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("humanId", String.valueOf(humanId));
//        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
//        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        CommonAccountHistoryHttpManager.getInstance().queryCustomerFlow(options,
                new MQuerySubscriber<CommonAccountFlow>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<CommonAccountFlow> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.e("加载报团购活动列表失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }
                });
    }


}

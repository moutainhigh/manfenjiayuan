package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invSendOrder.InvSendOrderItemBrief;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.InvSkuStoreHttpManager;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 7/28/16.
 */
public class InvSkuStoreMode {
    /**
     * 智能订货
     * @param chainCompanyId
     * @param listener
     * */
    public void autoAskSendOrder(Long chainCompanyId,
                                final OnModeListener<InvSendOrderItemBrief> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("chainCompanyId", String.valueOf(chainCompanyId));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().autoAskSendOrder(options,
                new Subscriber<InvSendOrderItemBrief>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.d("智能订货失败：" + e.toString());


                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(InvSendOrderItemBrief invSendOrderItemBrief) {
                        if (listener != null) {
                            listener.onSuccess(invSendOrderItemBrief);
                        }
                    }

                });
    }
}

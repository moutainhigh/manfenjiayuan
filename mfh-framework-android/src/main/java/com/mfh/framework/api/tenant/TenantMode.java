package com.mfh.framework.api.tenant;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

/**
 * 网点
 * Created by bingshanguxue on 16/3/17.
 */
public class TenantMode {

    public void listWhole(int bizDomainType, int domainUrlType, PageInfo pageInfo,
                           final OnPageModeListener<TenantInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
//            options.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        options.put("bizDomainType", String.valueOf(bizDomainType));
        options.put("domainUrlType", String.valueOf(domainUrlType));
        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        RxHttpManager.getInstance().listWhole(MfhApi.URL_TENANT + "/", options,
                new MQuerySubscriber<TenantInfo>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<TenantInfo> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }                        }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.df("加载租户失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.toString());
                        }
                    }
                });
    }

    public void getSaasInfo(String url, Long id, final OnModeListener<SassInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        RxHttpManager.getInstance().getSaasInfo2(url, id,
                new Subscriber<SassInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(SassInfo sassInfo) {
                        if (listener != null) {
                            listener.onSuccess(sassInfo);
                        }
                    }

                });
    }
}

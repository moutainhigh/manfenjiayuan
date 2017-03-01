package com.mfh.framework.api.invCompany;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.InvCompanyHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 7/27/16.
 */
public class InvCompanyMode {

    public void list(PageInfo pageInfo, String shortCodeLike,
                     final OnPageModeListener<CompanyInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("status", String.valueOf(InvCompanyHttpManager.STATUS_ONLINE));
        if (!StringUtils.isEmpty(shortCodeLike)) {
            options.put("shortCode", shortCodeLike);
        }

        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        InvCompanyHttpManager.getInstance().list(options,
                new MQuerySubscriber<CompanyInfo>(pageInfo) {

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<CompanyInfo> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.df("加载批发商失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }
                });
    }
}

package com.manfenjiayuan.business.mvp.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.bean.CompanyHuman;
import com.mfh.framework.rxapi.httpmgr.CompanyHumanHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公司成员
 * Created by bingshanguxue on 16/3/17.
 */
public class CompanyHumanMode {

    /**
     * 加载会员信息
     */
    public void getCustomerByOther(PageInfo pageInfo, String cardNo,
                                   final OnPageModeListener<CompanyHuman> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        options.put("cardNo", cardNo);
//        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());


        CompanyHumanHttpManager.getInstance().findCompanyHumansByPrivateInfo(options,
                new MQuerySubscriber<CompanyHuman>(pageInfo) {

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<CompanyHuman> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }
                });
    }

}

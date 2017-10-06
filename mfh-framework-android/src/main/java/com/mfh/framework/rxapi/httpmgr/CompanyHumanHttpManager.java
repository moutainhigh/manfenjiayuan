package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.rxapi.bean.CompanyHuman;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * 员工账号
 * Created by bingshanguxue on 25/01/2017.
 */

public class CompanyHumanHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final CompanyHumanHttpManager INSTANCE = new CompanyHumanHttpManager();
    }

    //获取单例
    public static CompanyHumanHttpManager getInstance() {
        return CompanyHumanHttpManager.SingletonHolder.INSTANCE;
    }

    private interface CompanyHumanService{
        /**
         * 查询子账号
         * /companyHuman/findCompUserPwdInfo?page=-1&rows=&officeId=
         */
        @GET("companyHuman/findCompUserPwdInfo")
        Observable<MResponse<MRspQuery<CompanyHuman>>> findCompUserPwdInfo(@QueryMap Map<String, String> options);

        /**
         * 查询部门子账号
         * /pmc/companyHuman/listWorkerBeanInfoOfCompany?page=-1&rows=
         */
        @GET("companyHuman/listWorkerBeanInfoOfCompany")
        Observable<MResponse<PayAmount>> listWorkerBeanInfoOfCompany(@QueryMap Map<String, String> options);

        /**
         * 员工接班前刷卡，显示自己所在公司信息,注意若tenantId不传则可能有多个公司
         * /companyHuman/findCompanyHumansByPrivateInfo?cardNo=&tenantId=
         */
        @GET("companyHuman/findCompanyHumansByPrivateInfo")
        Observable<MResponse<MRspQuery<CompanyHuman>>> findCompanyHumansByPrivateInfo(@QueryMap Map<String, String> options);


    }

    public void findCompUserPwdInfo(Map<String, String> options,
                                    MQuerySubscriber<CompanyHuman> subscriber) {
        CompanyHumanService mfhApi = RxHttpManager.createService(CompanyHumanService.class);
        Observable observable = mfhApi.findCompUserPwdInfo(options)
                .map(new MQueryResponseFunc<CompanyHuman>());
        toSubscribe(observable, subscriber);
    }

    public void findCompanyHumansByPrivateInfo(Map<String, String> options,
                                    MQuerySubscriber<CompanyHuman> subscriber) {
        CompanyHumanService mfhApi = RxHttpManager.createService(CompanyHumanService.class);
        Observable observable = mfhApi.findCompanyHumansByPrivateInfo(options)
                .map(new MQueryResponseFunc<CompanyHuman>());
        toSubscribe(observable, subscriber);
    }

}

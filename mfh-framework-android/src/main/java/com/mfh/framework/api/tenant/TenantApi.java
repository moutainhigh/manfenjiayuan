package com.mfh.framework.api.tenant;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;


/**
 *
 * Created by bingshanguxue on 9/28/16.
 */

public class TenantApi {
    /**
     * 域名业务类型
     * */
    public interface BizDomainType{
        int RETAIL = 2;
    }

    /**
     * 域名业务类型
     * */
    public interface DomainUrlType{
        int NORMAL = 0;
    }
//    public static String URL_SUBDIST = MfhApi.URL_BASE_SERVER + "/subdist/";


    /**
     * 查询有哪些生态租户信息，/realmMap/listWhole?bizDomainType=2&domainUrlType=0 其中bizDomainType代表零售类，domainUrlType代表域名地址类型；
     */
    public static String URL_LISTWHOLE = MfhApi.URL_TENANT + "/realmMap/listWhole";
    /**
     * 获取一个生态租户的详细信息 /tenantInfo/getSaasInfo?id=134342,  返回的就是上面的内容
     */
    private static String URL_GET_SAASINFO = MfhApi.URL_BASE_SERVER + "/tenantInfo/getSaasInfo";

    public static void register() {
//        URL_LISTWHOLE = MfhApi.URL_BASE_SERVER + "/realmMap/listWhole";
        URL_GET_SAASINFO = MfhApi.URL_BASE_SERVER + "/tenantInfo/getSaasInfo";
    }


    /**
     * 查询有哪些生态租户信息
     *
     */
    public static void listWhole(int bizDomainType, int domainUrlType, PageInfo pageInfo,
                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("bizDomainType", String.valueOf(bizDomainType));
        params.put("domainUrlType", String.valueOf(domainUrlType));
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        AfinalFactory.postDefault(URL_LISTWHOLE, params, responseCallback);
    }

    /**
     * 搜索小区
     *
     * @param id  编号
     */
    public static void getSaasInfo(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));

        AfinalFactory.postDefault(URL_GET_SAASINFO, params, responseCallback);
    }
    public static void getSaasInfo(String url, Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));

        AfinalFactory.postDefault(url, params, responseCallback);
    }

}

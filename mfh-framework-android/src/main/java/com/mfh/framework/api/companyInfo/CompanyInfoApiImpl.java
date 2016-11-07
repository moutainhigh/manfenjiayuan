package com.mfh.framework.api.companyInfo;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 9/29/16.
 */

public class CompanyInfoApiImpl extends CompanyInfoApi{
    /**
     * 查询租户
     *
     * @param abilityItem {@link AbilityItem}
     */
    public static void findPublicCompanyInfo(String nameLike, Integer abilityItem, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(nameLike)) {
            params.put("nameLike", nameLike);
        }
        params.put("abilityItem", String.valueOf(abilityItem));//能力
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_PUBLICCOMPANYINFO, params, responseCallback);
    }

    public static void getNetInfoById(String id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", id);
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GETNETINFO_BYID, params, responseCallback);
    }

    /**
     * 查询公司
     */
    public static void comnQuery(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("kind", "code");
        params.put("viewId", "2");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_COMPANYINFO_COMNQUERY, params, responseCallback);
    }

    /**
     * 查询公司
     * @param companyId
     */
    public static void getById(Long companyId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(companyId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_COMPANYINFO_GETBYID, params, responseCallback);
    }


}

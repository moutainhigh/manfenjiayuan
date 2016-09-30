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
     * 查询网点
     *
     * @param cityId {@link AbilityItem}
     */
    public static void findServicedNetsForUserPos(Long cityId, String userLng, String userLat,
                                                  PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("cityId", String.valueOf(cityId));
        params.put("userLng", String.valueOf(userLng));
        params.put("userLat", String.valueOf(userLat));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_SERVICEDNETS_FORUSERPOS, params, responseCallback);
    }
}

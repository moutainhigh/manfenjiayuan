package com.mfh.framework.api.netInfo;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 网点
 * Created by bingshanguxue on 18/10/2016.
 */

public class NetInfoApi{
    public static String URL_NETINFO = MfhApi.URL_BASE_SERVER + "/netInfo/";

    /**
     * 根据经纬度查询网点
     *  /netInfo/findServicedNetsForUserPos
     * */
    private static String URL_FINDSERVICEDNETS_FORUSERPOS = URL_NETINFO + "findServicedNetsForUserPos";

    /**
     * 根据经纬度查询网点
     *
     * @param cityId {@link AbilityItem}
     * @param userLng 精度
     * @param userLat 纬度
     */
    public static void findServicedNetsForUserPos(Long cityId, String userLng, String userLat,
                                                  PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("cityId", String.valueOf(cityId));
        if (!StringUtils.isEmpty(userLng)){
            params.put("userLng", userLng);
        }
        if (!StringUtils.isEmpty(userLat)){
            params.put("userLat", userLat);
        }
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDSERVICEDNETS_FORUSERPOS, params, responseCallback);
    }

}

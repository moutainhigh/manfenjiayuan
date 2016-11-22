package com.mfh.framework.api.subdist;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 9/28/16.
 */

public class SubdistApi {
    public static String URL_SUBDIST = MfhApi.URL_BASE_SERVER + "/subdist/";


    /**
     * 搜索小区
     */
    public static String URL_LIST = URL_SUBDIST + "list";
    /**
     * 查询周边小区
     */
    private static String URL_FIND_ARROUND_SUBDIST = URL_SUBDIST + "findArroundSubdist";

    public static void register() {
        URL_SUBDIST = MfhApi.URL_BASE_SERVER + "/subdist/";
        URL_LIST = URL_SUBDIST + "list";
        URL_FIND_ARROUND_SUBDIST = URL_SUBDIST + "findArroundSubdist";
    }


    /**
     * 搜索小区
     */
    public static void list(String cityId, String subdisName, PageInfo pageInfo,
                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("cityID", cityId);
        params.put("subdisName", subdisName);
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        AfinalFactory.postDefault(URL_LIST, params, responseCallback);
    }

    /**
     * 搜索小区
     *
     * @param latitude  不能为空
     * @param longitude 不能为空
     */
    public static void findArroundSubdist(String longitude, String latitude, PageInfo pageInfo,
                                          AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        AfinalFactory.postDefault(URL_FIND_ARROUND_SUBDIST, params, responseCallback);
    }


}

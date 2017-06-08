package com.mfh.framework.api.category;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 8/16/16.
 */
public class ScCategoryInfoApi {
    public static String URL_SC_CATEGORYINFO = MfhApi.URL_BASE_SERVER + "/scCategoryInfo/";

    /**
     * 类目查询－－一级类目
     */
    static String URL_COMNQUERY = URL_SC_CATEGORYINFO + "comnQuery";


    /**
     * pos类目查询接口：/scCategoryInfo/getCodeValue?parentId=6585&page=1&rows=20
     */
    private static String URL_GETCODEVALUE = URL_SC_CATEGORYINFO + "getCodeValue";


    /**
     * 删除类目
     * /scCategoryInfo/delete?id=
     */
    private static String URL_DELETE = URL_SC_CATEGORYINFO + "delete";

    /**
     * 修改类目
     */
    private static String URL_UPDATE = URL_SC_CATEGORYINFO + "update";

    /**
     * 类目查询
     */
    public static String URL_LIST = URL_SC_CATEGORYINFO + "list";

    /**
     * 获取根类目
     * /scCategoryInfo/getTopFrontId?tenantId=136076&cateType=9
     */
    private static String URL_GET_TOPFRONTID = URL_SC_CATEGORYINFO + "getTopFrontId";

    public static void register() {
        URL_SC_CATEGORYINFO = MfhApi.URL_BASE_SERVER + "/scCategoryInfo/";
        URL_COMNQUERY = URL_SC_CATEGORYINFO + "comnQuery";
        URL_GETCODEVALUE = URL_SC_CATEGORYINFO + "getCodeValue";
        URL_DELETE = URL_SC_CATEGORYINFO + "delete";
        URL_UPDATE = URL_SC_CATEGORYINFO + "update";
        URL_LIST = URL_SC_CATEGORYINFO + "list";
        URL_GET_TOPFRONTID = URL_SC_CATEGORYINFO + "getTopFrontId";
    }


    /**
     * 删除类目
     *
     * @param id 类目编号
     */
    public static void delete(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));

        AfinalFactory.postDefault(URL_DELETE, params, responseCallback);
    }

    /**
     * 查询类目列表
     */
    public static void list(int domain, int cateType, int catePosition,
                            int deep, Long tenantId, PageInfo pageInfo,
                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("kind", "code");
        params.put("domain", String.valueOf(domain));
        params.put("cateType", String.valueOf(cateType));
        params.put("catePosition", String.valueOf(catePosition));
        params.put("deep", String.valueOf(deep));//层级
        params.put("parentIdNull", "1");//层级

        if (tenantId != null) {
            params.put("tenantId", String.valueOf(tenantId));
        }
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        AfinalFactory.postDefault(URL_LIST, params, responseCallback);
    }

    /**
     * 修改类目
     */
    public static void update(String jsonStr,
                              AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_UPDATE, params, responseCallback);
    }

    /**
     * 查询前台类目根目录
     */
    public static void getTopFrontId(Integer cateType, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("cateType", String.valueOf(cateType));
//        params.put("tenantId", CATEGORY_TENANT_ID);//使用类目专属ID
//        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        AfinalFactory.postDefault(URL_GET_TOPFRONTID, params, responseCallback);
    }

    /**
     * 返回一级公共子目录
     * /scCategoryInfo/getCodeValue?parentId=6585&page=1&rows=20
     */
    public static void getCodeValue(Long parentId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("parentId", String.valueOf(parentId));
//        params.put("tenantId", CATEGORY_TENANT_ID);//使用类目专属ID
//        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
//        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        AfinalFactory.postDefault(URL_GETCODEVALUE, params, responseCallback);
    }

}

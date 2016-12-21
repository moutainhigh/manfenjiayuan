package com.mfh.framework.api.category;

import com.alibaba.fastjson.JSONObject;
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
     * /scCategoryInfo/create?jsonStr={},
     * 上面tenantId是当前pos机所属租户（注意不是网点，现在还没细化到网点或pos机），catePosition此次为1，domain为0
     */
    private static String URL_CREATE = URL_SC_CATEGORYINFO + "create";

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
        URL_CREATE = URL_SC_CATEGORYINFO + "create";
        URL_DELETE = URL_SC_CATEGORYINFO + "delete";
        URL_UPDATE = URL_SC_CATEGORYINFO + "update";
        URL_LIST = URL_SC_CATEGORYINFO + "list";
        URL_GET_TOPFRONTID = URL_SC_CATEGORYINFO + "getTopFrontId";
    }

    /**
     * private String cateInfo; //类目介绍
     * private Long imageId;//类目logo
     * private Integer catePosition;//
     * private Integer cateOrder;//次序
     * private Integer status;//是否有效 1-有效 0-无效
     * private String remark;//备注
     *
     * @param domain       业务域，0-实物商品 {@link CateApi#DOMAIN_TYPE_PROD}
     * @param catePosition 类目位置 0-系统默认(后台管理类目) 1-前台(用户自定义类目)
     * @param tenantId     当前pos机所属租户编号（注意不是网点，现在还没细化到网点或pos机）
     * @param nameCn       类目中文
     */
    public static void create(int domain, int catePosition, Long tenantId,
                              String nameCn, Integer cateType,
                              AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("kind", "code");
        jsonObject.put("domain", domain);
        jsonObject.put("nameCn", nameCn);
        jsonObject.put("catePosition", catePosition);
        jsonObject.put("tenantId", tenantId);
        jsonObject.put("cateType", cateType);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());

        AfinalFactory.postDefault(URL_CREATE, params, responseCallback);
    }

    public static void create(Long parentId, int domain, int catePosition, Long tenantId,
                              String nameCn, Integer cateType,
                              AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("kind", "code");
        jsonObject.put("domain", domain);
        jsonObject.put("nameCn", nameCn);
        jsonObject.put("catePosition", catePosition);
        jsonObject.put("tenantId", tenantId);
        jsonObject.put("cateType", cateType);
        jsonObject.put("parentId", parentId);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());

        AfinalFactory.postDefault(URL_CREATE, params, responseCallback);
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

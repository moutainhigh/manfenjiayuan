package com.mfh.framework.api.anon.sc.storeRack;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 09/10/2016.
 */

public class ScStoreRackApi {
    /**渠道类型*/
    public static final int CHANNEL_TYPE_WX = 1;//微信端
    public static final int CHANNEL_TYPE_APP = 2;//App端
    public static final int CHANNEL_TYPE_PC = 4;//PC端

    /** 货架类型 */
    public static final int RACK_TYPE_SC_HOME = 0;//商城首页,包含滚屏、导航、促销
    public static final int RACK_TYPE_SHOP_HOME = 1;//店铺主页,每个网点设计一个主页

    private static String URL_ANON_SC_STORERACK = MfhApi.URL_BASE_SERVER + "/anon/sc/storeRack/";

    /**
     * /anon/sc/storeRack/getByShopIdMust?shopId=?&channelTypesItem=2
     * 查询店铺货架编号
     * */
    private static String URL_GETBYSHOPID_MUST= URL_ANON_SC_STORERACK + "getByShopIdMust";

    /**
     * http://admin.mixicook.com/pmc/anon/sc/storeRack/getById?id=76&JSESSIONID=c2a2b05f-932e-425e-86a2-3c275a479cb3
     * 查询货架商品信息
     * */
    private static String URL_GETBYID= URL_ANON_SC_STORERACK + "getById";

    /**
     * http://dev.mixicook.com/pmc/anon/sc/storeRack/getByShopMust?channelTypesItem=2&JSESSIONID=d20cc6ad-4bc8-4554-8188-13eb03fde113&shopId=136076
     * 查询店铺货架信息，原来要调用两个接口来实现，{@link #URL_GETBYSHOPID_MUST} 和 {@link #URL_GETBYID}
     * */
    private static String URL_GET_BYSHOPMUST= URL_ANON_SC_STORERACK + "getByShopMust";

    /**
     * http://dev.mixicook.com/pmc/anon/sc/storeRack/findByShopMust?shopId=136076&channelTypesItem=1&rackTypes=1,2
     * 查找一个网点里指定用途类型的货架，没有使用全局定义的；rackTypes支持多个类型，1-主页，2-广告区
     * */
    private static String URL_FIND_BYSHOPMUST= URL_ANON_SC_STORERACK + "findByShopMust";

    public static void register(){
        URL_ANON_SC_STORERACK = MfhApi.URL_BASE_SERVER + "/anon/sc/storeRack/";
    }

    /**
     * 查询店铺货架编号
     * @param shopId 店铺编号
     */
    public static void getByShopIdMust(Long shopId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (shopId != null) {
            params.put("shopId", String.valueOf(shopId));
        }
        params.put("channelTypesItem", String.valueOf(CHANNEL_TYPE_APP));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GETBYSHOPID_MUST, params, responseCallback);
    }

    /**
     * 查询货架商品信息
     * @param id 货架编号
     */
    public static void getById(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (id != null) {
            params.put("id", String.valueOf(id));
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GETBYID, params, responseCallback);
    }

    /**
     * 查询店铺货架信息
     * @param shopId 店铺编号
     */
    public static void getByShopMust(Long shopId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (shopId != null) {
            params.put("shopId", String.valueOf(shopId));
        }
        params.put("channelTypesItem", String.valueOf(CHANNEL_TYPE_APP));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GET_BYSHOPMUST, params, responseCallback);
    }

    /**
     * 查询店铺货架信息
     * @param shopId 店铺编号
     * @param rackTypes 货架类型，支持多个类型，1-主页，2-广告区
     */
    public static void findByShopMust(Long shopId, String rackTypes, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (shopId != null) {
            params.put("shopId", String.valueOf(shopId));
        }
        if (!StringUtils.isEmpty(rackTypes)){
            params.put("rackTypes", rackTypes);
        }
        params.put("channelTypesItem", String.valueOf(CHANNEL_TYPE_APP));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FIND_BYSHOPMUST, params, responseCallback);
    }

}

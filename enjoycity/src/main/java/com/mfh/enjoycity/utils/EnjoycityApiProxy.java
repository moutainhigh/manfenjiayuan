package com.mfh.enjoycity.utils;

import android.content.Intent;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.enjoycity.AppContext;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrderApi;
import com.mfh.framework.prefs.SharedPrefesBase;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;


/**
 * 网络请求
 * Created by NAT.ZZN on 2015/5/14.
 */
public class EnjoycityApiProxy {


    //KEY
    public final static String PARAM_KEY_HUMAN_ID       = "humanId";
    public final static String PARAM_KEY_JSESSIONID     = "JSESSIONID";
    public final static String PARAM_KEY_DEVICEID       = "deviceId";
    public final static String PARAM_KEY_OLD_PWD        = "oldPwd";
    public final static String PARAM_KEY_NEW_PWD        = "newPwd";
    public final static String PARAM_KEY_CONFIRM_PWD    = "confirmPwd";
    public final static String PARAM_KEY_JSONSTR        = "jsonStr";
    public final static String PARAM_KEY_ID             = "id";
    public final static String PARAM_KEY_NAME           = "name";
    public final static String PARAM_KEY_WXOPENID       = "wxopenid";
    public final static String PARAM_KEY_CHANNEL_ID     = "channelId";
    public final static String PARAM_KEY_AMOUNT         = "amount";
    public final static String PARAM_KEY_ORDER_ID       = "orderId";
    public final static String PARAM_KEY_ORDER_IDS      = "orderIds";
    public final static String PARAM_KEY_BIZ_TYPE       = "btype";
    public final static String PARAM_KEY_NONCESTR       = "nonceStr";
    public final static String PARAM_KEY_WAYTYPE        = "wayType";
    public final static String PARAM_KEY_FILETOUPLOAD   = "fileToUpload";
    public final static String PARAM_KEY_PR_EORDER_ID   = "preOrderId";
    public final static String PARAM_KEY_TOKEN          = "token";
    public final static String PARAM_KEY_SUBDIS_NAME    = "subdisName";
    public final static String PARAM_KEY_CITY_ID        = "cityID";
    public final static String PARAM_KEY_LONGITUDE      = "longitude";
    public final static String PARAM_KEY_LATITUDE       = "latitude";
    public final static String PARAM_KEY_SUBDIS_ID      = "subdisId";


    public final static int WAYTYPE_ALIPAY = 1;//支付宝支付
    public final static int WAYTYPE_WXPAY = 512;//微信支付




    /**
     * 获取摇一摇页面
     * @param deviceId SN
     * */
    public void getWXShopDevicePage(String deviceId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put(PARAM_KEY_DEVICEID, deviceId);

        AfinalFactory.postDefault(EnjoycityApi.URL_WX_SHOP_DEVICE_PAGE, params, responseCallback);
    }


    public static void findArroundMarketShops(Long subdisId, AjaxCallBack<? extends Object> responseCallback){
        if(subdisId == null){
            return;
        }
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_SUBDIS_ID, String.valueOf(subdisId));

        AfinalFactory.postDefault(EnjoycityApi.URL_FIND_ARROUND_MARKET_SHOPS, params, responseCallback);
    }


    /**
     * 新增收货地址
     * @param humanId 人员编号
     * @param receiveName 收件人姓名
     * @param receivePhone 收件人电话
     * @param subdisId 小区编号
     * @param subName 小区名
     * @param addrName 模糊地址名
     * @param houseNumber 公寓编号
     * @param responseCallback
     * */
    public static void createReceiveAddress(String receiveName, String receivePhone,
                                            String subdisId, String subName, String addrName, String houseNumber,
                                            AjaxCallBack<? extends Object> responseCallback){

        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(MfhLoginService.get().getCurrentGuId()));

        JSONObject object = new JSONObject();
        object.put("receiveName", receiveName);//收件人姓名
        object.put("receivePhone", receivePhone);//收件人电话号码
        object.put("subdisId", subdisId);//小区编号
        object.put("subName", subName);//小区名
        object.put("addrName", String.format("%s %s", addrName, houseNumber));//小区地址
//        object.put("addrvalid", addrvalid);//小区楼栋号

        params.put(PARAM_KEY_JSONSTR, object.toJSONString());

        AfinalFactory.postDefault(EnjoycityApi.URL_CREATE_RECEIVE_ADDRESS, params, responseCallback);
    }

    /**
     * 收货地址列表
     * @param humanId 人员编号
     * */
    public static void queryAllReceiveAddress(AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(MfhLoginService.get().getCurrentGuId()));

        AfinalFactory.postDefault(EnjoycityApi.URL_QUERYALL_RECEIVE_ADDRESS, params, responseCallback);
    }



    /**
     * 查询热卖商品
     * */
    public static void queryShopHotSales(Long shopId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("shopId", String.valueOf(shopId));

        AfinalFactory.postDefault(EnjoycityApi.URL_SHOP_HOT_SALES, params, responseCallback);
    }

    /**
     * 查询商品信息
     * */
    public static void queryProductDetail(Long productId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(productId));

        AfinalFactory.postDefault(EnjoycityApi.URL_PRODUCT_DETAIL, params, responseCallback);
    }

    /**
     * 查询一级类目
     * */
    public static void queryRootCategory(Long tenantId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("kind", "code");
        params.put("tenantId", String.valueOf(tenantId));
        params.put("deep", "2");

        AfinalFactory.postDefault(EnjoycityApi.URL_CATEGORYINFO_COMNQUERY, params, responseCallback);
    }

    /**
     * 查询商品,包括子类目的全部商品
     * */
    public static void findProduct(String categoryId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("categoryId", categoryId);

        AfinalFactory.postDefault(EnjoycityApi.URL_FIND_PRODUCT, params, responseCallback);
    }

    /**
     *  我常买,需要用户登录
     * */
    public static void findOfenBuyProducts(Long shopId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("shopId", String.valueOf(shopId));
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(EnjoycityApi.URL_OFEN_BUY_PRODUCTS, params, responseCallback);
    }

    /**
     *  查询满分小伙伴服务,需要用户登录
     * */
    public static void queryMfhParterInService(){
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("processFailure: " + errMsg);
//                        orderPayFailed(-1);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
//                        com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        //正在服务的小伙伴数目
                        ZLogger.d("queryMfhParterInService: " + retValue.getValue());

                        int count = Integer.valueOf(retValue.getValue());
//                        SharedPreferencesHelper.getPreferences(Constants.PREF_NAME_APP_BIZ).edit().putInt(Constants.PREF_KEY_PARTER_COUNT, count).commit();
                        SharedPrefesBase.set(AppContext.getAppContext(), Constants.PREF_NAME_APP_BIZ, Constants.PREF_KEY_PARTER_COUNT, count);

                        MfhApplication.getAppContext().sendBroadcast(new Intent(Constants.BROADCAST_ACTION_PARTER_REFRESH));
                    }
                }
                , String.class
                , MfhApplication.getAppContext())
        {
        };

        ScOrderApi.countServiceMfhPartner(responseCallback);
    }
    /**
     *  获取订单的优惠券信息
     *  @param orderIds 订单Id，支持多个，多个以英文,隔开
     *  @param bType 业务类型 3-商城
     * */
    public static void findCoupons(String orderIds, int bType, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("orderIds", orderIds);
        params.put("bType", String.valueOf(bType));
//        params.put(PARAM_KEY_JSESSIONID, SharedPreferencesHelper.getLastSessionId());

        AfinalFactory.postDefault(EnjoycityApi.URL_FIND_COUPONS, params, responseCallback);
    }

    /**
     * 查询商品促销标签
     * @param productIds format like [1,2] array.
     * */
    public static void findPromoteLabels(Long shopId, String productIds, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("shopId", String.valueOf(shopId));
        params.put("productIds", productIds);

        AfinalFactory.postDefault(EnjoycityApi.URL_FIND_PROMOTE_LABEL, params, responseCallback);
    }

    /**
     * 查询商品促销价格
     * @param productIds format like "1,2", spilt by ','
     * */
    public static void findPromotePrice(Long shopId, String productIds, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("shopId", String.valueOf(shopId));
        params.put("productIds", productIds);

        AfinalFactory.postDefault(EnjoycityApi.URL_FIND_PROMOTE_PRICE, params, responseCallback);
    }

}

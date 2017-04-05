package com.mfh.framework.rxapi.http;


import com.mfh.framework.api.CompanyHuman;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.api.pmcstock.PosOrder;
import com.mfh.framework.api.posRegister.PosRegisterApi;
import com.mfh.framework.api.scGoodsSku.ProductSkuBarcode;
import com.mfh.framework.api.tenant.SassInfo;
import com.mfh.framework.api.tenant.TenantInfo;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.entity.MValue;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 8/29/16.
 */

public interface RxMfhService {
    /**
     * 登录
     * [POST]http://admin.mixicook.com/pmc/login?username=15962213783&password=777777&needMenu=true
     */
    //POST请求需要加上@FormUrlEncoded
    @FormUrlEncoded
    @POST("login")
    Observable<MResponse<UserMixInfo>> login(@Field("username") String username,
                                             @Field("password") String password);
    //正常
    @FormUrlEncoded
    @POST("http://admin.mixicook.com/pmc/login")
    Call<MResponse<UserMixInfo>> login2(@Field("username") String username,
                                        @Field("password") String password);
//    /**登录（正常）*/
//    @FormUrlEncoded
//    @POST("login")
//    Observable<MResponse<UserMixInfo>> login4(@Field("username") String username,
//                                              @Field("password") String password);
    /**会话是否有效*/
//    @FormUrlEncoded
//    @POST("isSessionValid")
//    Observable<MResponse<String>> isSessionValid(@Field("JSESSIONID") String JSESSIONID);
    @GET("isSessionValid")
    Observable<MResponse<String>> isSessionValid(@Query("JSESSIONID") String JSESSIONID);
    /**
     * <b>设备注册接口</b><br>
     * 传递唯一序列号如网卡序列号，后台返回一个整形编号<br>
     * /posRegister/create?jsonStr={"serialNo":"2222"}
     */
//    @POST("posRegister/create")
//    Observable<MResponse<String>> posRegisterCreate(@Body PosRegisterApi.ParamsIn jsonStr);
    @GET("posRegister/create")
    Observable<MResponse<String>> posRegisterCreate(@Query("jsonStr") String jsonStr);
    /**会话是否有效*/
//    @FormUrlEncoded
//    @POST("analysisAccDate/haveNoMoneyEnd")
//    Observable<MResponse<String>> haveNoMoneyEnd(@Field("JSESSIONID") String JSESSIONID);
    @GET("analysisAccDate/haveNoMoneyEnd")
    Observable<MResponse<MValue<String>>> haveNoMoneyEnd(@Query("JSESSIONID") String JSESSIONID);
    /**会话是否有效*/
//    @FormUrlEncoded
//    @POST("scNetRealInfo/needLockPos")
//    Observable<MResponse<String>> needLockPos(@Field("JSESSIONID") String JSESSIONID,
//                                              @Field("netId") Long netId);
    @GET("scNetRealInfo/needLockPos")
    Observable<MResponse<String>> needLockPos(@Query("JSESSIONID") String JSESSIONID,
                                              @Query("netId") Long netId);
    /**获取一个生态租户的详细信息*/
    @GET("tenantInfo/getSaasInfo")
    Observable<MResponse<SassInfo>> getSaasInfo(@Query("id") Long id);
    /**获取能力信息，可以调用接口获取小伙伴详细信息，包括可能存在的认证信息*/
    @GET("queryPrivList")
    Observable<MResponse<String>> queryPrivList(@Query("JSESSIONID") String JSESSIONID);
    @GET("companyHuman/findCompUserPwdInfo")
    Observable<MResponse<MRspQuery<CompanyHuman>>> findCompUserPwdInfo(@QueryMap Map<String, String> options);

    @GET("scProductSkuBarcodes/findShopOtherBarcodes")
    Observable<MResponse<MRspQuery<ProductSkuBarcode>>> findShopOtherBarcodes(@QueryMap Map<String, String> options);

    /**查询有哪些生态租户信息*/
    @GET("realmMap/listWhole")
    Observable<MResponse<MRspQuery<TenantInfo>>> listWhole(@QueryMap Map<String, String> options);


    /**
     * 支付宝条码支付
     *
     * /toAlipayBarTradePay/barPay?jsonStr={out_trade_no:20150929003638,auth_code:289802075510210664,
     * total_amount:0.1,subject:test,terminal_id:001,operator_id:112369}
     *
     * @param outTradeNo         商户订单号,商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
     * @param authCode           支付授权码,用户支付宝钱包中的“付款码”信息
     * @param totalAmount        订单总金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
     * @param discountableAmount 可打折金额
     * @param subject            订单标题
     * @param bizType            业务类型
     * @param chId               支付渠道编号
     */
    @GET("toAlipayBarTradePay/barPay")
    Observable<MResponse<String>> alipayBarPay(@QueryMap Map<String, String> options);
    /**
     * 微信条码支付
     *
     * @param outTradeNo         商户订单号,商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
     * @param authCode           支付授权码,用户支付宝钱包中的“付款码”信息
     * @param totalAmount        订单总金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
     * @param discountableAmount 可打折金额
     * @param subject            订单标题
     */
    @GET("toWxpayBarTradePay/barPay")
    Observable<MResponse<String>> wepayBarPay(@QueryMap Map<String, String> options);

    @GET("payOrder/create")
    Observable<MResponse<String>> createPayOrder(@QueryMap Map<String, String> options);

    @GET("customer/createParamDirect")
    Observable<MResponse<String>> createParamDirect(@QueryMap Map<String, String> options);

    //批量上传订单
//    @FormUrlEncoded
//    @POST("posOrder/batchInOrders")
//    Observable<MResponse<String>> batchInOrders(@Field("JSESSIONID") String JSESSIONID,
//                                             @Body JSONArray jsonStr);
    @POST("posOrder/batchInOrders")
    Observable<MResponse<String>> batchInOrders(@Query("JSESSIONID") String JSESSIONID,
                                                @Query("jsonStr") String jsonStr);
    @GET("pmcstock/findGoodsOrderList")
    Observable<MResponse<MRspQuery<PosOrder>>> findGoodsOrderList(@QueryMap Map<String, String> options);

    //获取指定pos机编号在服务器端已经生成的最大订单id号
    @GET("posOrder/getMaxPosOrderId")
    Observable<MResponse<MValue<String>>> getMaxPosOrderId(@Query("posId") String jsonStr);



    @POST("exit")
    Observable<MResponse<String>> exit(@Query("JSESSIONID") String JSESSIONID);

    //@Body parameters cannot be used with form or multi-part encoding
    @Headers({
            "Accept: application/json",
            "Content-type: application/json"
    })
    //not work
    @POST("login")
    Call<MResponse<UserMixInfo>> login3(@Body PosRegisterApi.ParamsIn jsonStr);

    //not work
    @GET("login")
    Call<MResponse<UserMixInfo>> loginCall(@Query("username") String username,
                                           @Query("password") String password);

//    ava.lang.IllegalArgumentException: Form-encoded method must contain at least one @Field.
//    @FormUrlEncoded
//    @POST("posRegister/list")
//    Observable<MQueryResponse<PosRegister>> posRegisterList();

    //正确
//    @GET("posRegister/list")
//    Observable<MQueryResponse<PosRegister>> posRegisterList2();
    //正确
//    @GET("posRegister/list")
//    Call<MQueryResponse<PosRegister>> posRegisterList3(@Query("netId") Long netId);


    @FormUrlEncoded
    @POST("http://www.weibovideo.com")
    Observable<ResponseBody> getVideoUrl(@Field("weibourl") String weibourl);

    /**查询用户：
     * /pmc/customer/getCustomerByOther?mobile=2123&humanId=31323
     * 返回当前账户余额
     * */
    @GET("customer/getCustomerByOther")
    Observable<MResponse<Human>> getCustomerByOther(@QueryMap Map<String, String> options);


}

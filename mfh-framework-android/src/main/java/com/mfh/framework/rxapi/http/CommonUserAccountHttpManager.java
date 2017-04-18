package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.func.MResponseFunc;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

import static com.mfh.framework.api.commonuseraccount.CommonUserAccountApi.URL_COMMONUSERACCOUNT;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class CommonUserAccountHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final CommonUserAccountHttpManager INSTANCE = new CommonUserAccountHttpManager();
    }

    //获取单例
    public static CommonUserAccountHttpManager getInstance() {
        return CommonUserAccountHttpManager.SingletonHolder.INSTANCE;
    }

    private interface CommonUserAccountService{
        /**
         * pos端提交客户编号、订单基础信息和卡券信息，计算金额
         * <p>POS拆单时用到此接口</p>*/
        @GET("commonuseraccount/getPayAmountByOrderInfos")
        Observable<MResponse<List<PayAmount>>> getPayAmountByOrderInfos(@QueryMap Map<String, String> options);

        /**pos端提交客户编号、订单基础信息和卡券信息，计算金额*/
        @GET("commonuseraccount/getPayAmountByOrderInfo")
        Observable<MResponse<PayAmount>> getPayAmountByOrderInfo(@QueryMap Map<String, String> options);


        /**
         * pos端直接使用满分账户进行余额支支付或积分支付，无具体业务背景:
         * <ol>
         * <li>满分余额支付</li>
         * /commonuseraccount/payDirect?humanId=94182&accountPassword=196735&amount=100000&bizType=3&orderId=123
         * <li>满分扫码积分兑换</li>
         * /commonuseraccount/payDirect?cardNo=245245245254254&score=1000
         * </ol>
         * <p>
         * (若mineCps参数不为空，则相当于支付完毕同时将其废弃，避免再调用一次下面的abandonCouponById接口)
         */
        @GET("commonuseraccount/payDirect")
        Observable<MResponse<String>> payDirect(@QueryMap Map<String, String> options);

        /**
         * 用户注册,
         * 根据手机号新注册或修改一个个人用户,并且建立个人支付账户，需要提供登录密码password和支付密码payPassword:
         * /commonuseraccount/registerUser?humanMobile=18248499111&humanName=zhangyz&password=123456&payPassword=123456
         */
        @GET("commonuseraccount/registerUser")
        Observable<MResponse<Human>> registerUser(@QueryMap Map<String, String> options);

        /**
         * 开卡并激活用户账户
         * /commonuseraccount/activateAccount?cardId=334455667788&ownerId=94182
         */
        @GET("commonuseraccount/activateAccount")
        Observable<MResponse<UserAccount>> activateAccount(@QueryMap Map<String, String> options);

        /**
         * 根据卡号获取用户信息
         * /commonuseraccount/getUserAccountByCardId?cardId=
         */
        @GET("commonuseraccount/getUserAccountByCardId")
        Observable<MResponse<UserAccount>> getUserAccountByCardId(@QueryMap Map<String, String> options);

    }

    public void getPayAmountByOrderInfos(Map<String, String> options, Subscriber<List<PayAmount>> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.getPayAmountByOrderInfos(options)
                .map(new MResponseFunc<List<PayAmount>>());
        toSubscribe(observable, subscriber);
    }

    /**
     * @param bizType 业务类型
     * @param version 版本号，1,只返回payAmount; 2,返回运费优惠券费用
     * @param jsonStr    订单明细信息
     * @param couponsIds 卡券领用号
     * @param ruleIds    规则
     * */
    public void getPayAmountByOrderInfo(Map<String, String> options, Subscriber<PayAmount> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.getPayAmountByOrderInfo(options)
                .map(new MResponseFunc<PayAmount>());
        toSubscribe(observable, subscriber);
    }

    /**
     *
     * 积分兑换（免密）
     * @param humanId 客户编号（扫描的支付码）
     * @param score  积分值
     *
     * 收银订单会员支付（手机号需要输入密码，会员支付码不需要输入密码）
     * @param cardNo  卡芯片号
     * @param humanId         客户编号
     * @param accountPassword 支付密码
     * @param amount          支付金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
     * @param bizType         业务类型
     * @param orderId         pos机本地订单号格式（设备编号＋订单编号），还不算后台生成的订单号
     */
    public void payDirect(Map<String, String> options, Subscriber<String> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.payDirect(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void registerUser(Map<String, String> options, Subscriber<Human> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.registerUser(options)
                .map(new MResponseFunc<Human>());
        toSubscribe(observable, subscriber);
    }

    public void activateAccount(Map<String, String> options, Subscriber<UserAccount> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.activateAccount(options)
                .map(new MResponseFunc<UserAccount>());
        toSubscribe(observable, subscriber);
    }

    public void getUserAccountByCardId(Map<String, String> options, Subscriber<UserAccount> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.getUserAccountByCardId(options)
                .map(new MResponseFunc<UserAccount>());
        toSubscribe(observable, subscriber);
    }


}

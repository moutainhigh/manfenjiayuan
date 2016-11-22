package com.mfh.framework.api.commonuseraccount;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 7/5/16.
 */
public class CommonUserAccountApi {

    public static String URL_COMMONUSERACCOUNT = MfhApi.URL_BASE_SERVER + "/commonuseraccount/";

    /**
     * 修改支付密码
     */
    private static String URL_CHANGE_PAYPASSWORD = URL_COMMONUSERACCOUNT + "changePwd";
    /**
     * 检查支付密码
     */
    private static String URL_CHECK_ACCOUNTPASSWORD = URL_COMMONUSERACCOUNT + "checkPassword";

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
    private static String URL_PAYDIRECT = URL_COMMONUSERACCOUNT + "payDirect";

    /**
     * 用户注册,
     * 根据手机号新注册或修改一个个人用户,并且建立个人支付账户，需要提供登录密码password和支付密码payPassword:
     * /commonuseraccount/registerUser?humanMobile=18248499111&humanName=zhangyz&password=123456&payPassword=123456
     */
    private static String URL_REGISTERUSER = URL_COMMONUSERACCOUNT + "registerUser";

    /**
     * 根据卡号获取用户信息
     * /commonuseraccount/getUserAccountByCardId?cardId=
     */
    private static String URL_GET_USERACCOUNT_BYCARDID = URL_COMMONUSERACCOUNT + "getUserAccountByCardId";

    /**
     * 开卡并激活用户账户
     * /commonuseraccount/activateAccount?cardId=334455667788&ownerId=94182
     */
    static String URL_ACTIVATEACCOUNT = URL_COMMONUSERACCOUNT + "activateAccount";

    /**
     * 会员卡充值:给其他帐号转账
     * /commonuseraccount/activateAccount?cardId=334455667788&ownerId=94182
     */
    static String URL_TRANSFERFROMMYACCOUNT = URL_COMMONUSERACCOUNT + "transferFromMyAccount";

    /**
     * pos端提交客户编号、订单基础信息和卡券信息，计算金额
     */
    static String URL_GETPAYAMOUNT_BYORDERINFO = URL_COMMONUSERACCOUNT + "getPayAmountByOrderInfo";
    static String URL_GET_PAYAMOUNT_BY_ORDERINFOS = URL_COMMONUSERACCOUNT + "getPayAmountByOrderInfos";

    /**
     * (商城/洗衣)订单支付
     */
    static String URL_SCACCOUNT_PAY = URL_COMMONUSERACCOUNT + "scAccountPay";


    /**
     * 满分账户充值
     */
    static String URL_MFACCOUNT_PAY = URL_COMMONUSERACCOUNT + "mfhAccountPay";

    /**
     * 服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
     */
    public static String NOTIFY_ACCOUNT = URL_COMMONUSERACCOUNT + "notifyAccount";

    public static void register() {
        URL_COMMONUSERACCOUNT = MfhApi.URL_BASE_SERVER + "/commonuseraccount/";
        URL_CHANGE_PAYPASSWORD = URL_COMMONUSERACCOUNT + "changePwd";
        URL_CHECK_ACCOUNTPASSWORD = URL_COMMONUSERACCOUNT + "checkPassword";
        URL_PAYDIRECT = URL_COMMONUSERACCOUNT + "payDirect";
        URL_REGISTERUSER = URL_COMMONUSERACCOUNT + "registerUser";
        URL_GET_USERACCOUNT_BYCARDID = URL_COMMONUSERACCOUNT + "getUserAccountByCardId";
        URL_ACTIVATEACCOUNT = URL_COMMONUSERACCOUNT + "activateAccount";
        URL_TRANSFERFROMMYACCOUNT = URL_COMMONUSERACCOUNT + "transferFromMyAccount";
        URL_GETPAYAMOUNT_BYORDERINFO = URL_COMMONUSERACCOUNT + "getPayAmountByOrderInfo";
        URL_GET_PAYAMOUNT_BY_ORDERINFOS = URL_COMMONUSERACCOUNT + "getPayAmountByOrderInfos";
        URL_SCACCOUNT_PAY = URL_COMMONUSERACCOUNT + "scAccountPay";
        URL_MFACCOUNT_PAY = URL_COMMONUSERACCOUNT + "mfhAccountPay";
        NOTIFY_ACCOUNT = URL_COMMONUSERACCOUNT + "notifyAccount";
    }

    /**
     * 修改 支付密码
     * <p>
     * {"code":"0","msg":"操作成功!","version":"1","data":""}
     *
     * @param humanId 登录用户编号
     * @param oldPwd  旧密码
     * @param newPwd  新密码
     *                注：确认新密码在调用接口前做处理，默认确认新密码和新密码相同。
     */
    public static void changePayPassword(Long humanId, String oldPwd, String newPwd, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("oldPwd", oldPwd);
        params.put("newPwd", newPwd);
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CHANGE_PAYPASSWORD, params, responseCallback);
    }

    /**
     * 更新用户个人资料
     *
     * @param accountPassword 支付密码
     */
    public static void checkAccountPassword(Long humanId, String accountPassword, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("accountPassword", accountPassword);
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CHECK_ACCOUNTPASSWORD, params, responseCallback);
    }

    /**
     * pos端直接使用满分账户进行支付
     *
     * @param humanId         客户编号
     * @param accountPassword 支付密码
     * @param amount          支付金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
     * @param bizType         业务类型
     * @param orderId         pos机本地订单号格式（设备编号＋订单编号），还不算后台生成的订单号
     */
    public static void payDirectByAccount(Long humanId, String accountPassword, String amount,
                                          String bizType, String orderId,
                                          AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("accountPassword", accountPassword);
        params.put("amount", amount);
        params.put("bizType", bizType);
        params.put("orderId", orderId);
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
//        params.put("ruleIds", "");//这里的卡券核销为空，统一在订单结束时核销
//        params.put("mineCps", "");//这里的卡券核销为空，统一在订单结束时核销
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_PAYDIRECT, params, responseCallback);
    }

    /**
     * pos端直接使用满分会员卡进行支付(免密)
     *
     * @param cardNo  卡芯片号
     * @param amount  支付金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
     * @param bizType 业务类型
     * @param orderId 机器设备号＋订单号＋时间戳
     */
    public static void payDirectByCard(String cardNo, String amount,
                                       String bizType, String orderId,
                                       AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("cardNo", cardNo);
//        params.put("accountPassword", accountPassword);
        params.put("amount", amount);
        params.put("bizType", bizType);
        params.put("orderId", orderId);
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_PAYDIRECT, params, responseCallback);
    }

    /**
     * 满分扫码积分兑换(免密)
     *
     * @param cardNo 扫描的支付码
     * @param score  积分值
     */
    public static void payDirectBySweepCode(String humanId, Double score,
                                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("cardNo", humanId);
        if (score != null) {
            params.put("score", String.format("%.0f", score));
        }
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_PAYDIRECT, params, responseCallback);
    }


    /**
     * 用户注册
     * <ol>
     * 适用场景
     * <li>门店用户注册</li>
     * </ol>
     *
     * @param humanName 姓名（必填）
     */
    public static void registerUser(String humanMobile, String humanName, String password,
                                    String payPassword, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanMobile", humanMobile);
        if (!StringUtils.isEmpty(humanName)) {
            params.put("humanName", humanName);
        }
        params.put("password", password);
        params.put("payPassword", payPassword);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_REGISTERUSER, params, responseCallback);
    }

    /**
     * {@link #URL_GET_USERACCOUNT_BYCARDID 获取用户信息}
     * <ol>
     * 适用场景
     * <li>门店用户注册</li>
     * </ol>
     *
     * @param cardId 卡芯片号（必填）
     */
    public static void getUserAccountByCardId(String cardId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(cardId)) {
            params.put("cardId", cardId);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GET_USERACCOUNT_BYCARDID, params, responseCallback);
    }
}

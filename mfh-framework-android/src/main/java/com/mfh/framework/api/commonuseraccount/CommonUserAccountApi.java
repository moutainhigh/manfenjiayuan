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
     * 会员卡充值:给其他帐号转账
     * /commonuseraccount/activateAccount?cardId=334455667788&ownerId=94182
     */
    static String URL_TRANSFERFROMMYACCOUNT = URL_COMMONUSERACCOUNT + "transferFromMyAccount";

    /**
     * pos端提交客户编号、订单基础信息和卡券信息，计算金额
     */
    static String URL_GETPAYAMOUNT_BYORDERINFO = URL_COMMONUSERACCOUNT + "getPayAmountByOrderInfo";

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
        URL_REGISTERUSER = URL_COMMONUSERACCOUNT + "registerUser";
        URL_GET_USERACCOUNT_BYCARDID = URL_COMMONUSERACCOUNT + "getUserAccountByCardId";
        URL_TRANSFERFROMMYACCOUNT = URL_COMMONUSERACCOUNT + "transferFromMyAccount";
        URL_GETPAYAMOUNT_BYORDERINFO = URL_COMMONUSERACCOUNT + "getPayAmountByOrderInfo";
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

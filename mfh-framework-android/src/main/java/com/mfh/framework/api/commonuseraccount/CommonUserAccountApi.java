package com.mfh.framework.api.commonuseraccount;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;

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

}

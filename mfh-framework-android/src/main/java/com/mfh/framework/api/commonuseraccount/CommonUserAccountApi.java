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
     * 检查支付密码
     */
    private static String URL_CHECK_ACCOUNTPASSWORD = URL_COMMONUSERACCOUNT + "checkPassword";

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
        URL_CHECK_ACCOUNTPASSWORD = URL_COMMONUSERACCOUNT + "checkPassword";
        URL_SCACCOUNT_PAY = URL_COMMONUSERACCOUNT + "scAccountPay";
        URL_MFACCOUNT_PAY = URL_COMMONUSERACCOUNT + "mfhAccountPay";
        NOTIFY_ACCOUNT = URL_COMMONUSERACCOUNT + "notifyAccount";
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

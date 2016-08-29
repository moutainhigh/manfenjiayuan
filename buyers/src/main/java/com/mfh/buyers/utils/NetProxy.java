package com.mfh.buyers.utils;

import com.mfh.buyers.wxapi.WXUtil;
import com.mfh.framework.api.ScApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 网络请求
 * Created by NAT.ZZN on 2015/5/14.
 */
public class NetProxy {

    //服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
    public static final String ALIPAY_NOTIFY_URL = NetFactory.getServerUrl() + "/commonuseraccount/notifyAccount";
    public static final String ALIPAY_ORDER_NOTIFY_URL = NetFactory.getServerUrl() + "/pmcstock/notifyOrder";

    public final static String PARAM_KEY_JSESSIONID     = "JSESSIONID";
    public final static String PARAM_KEY_DEVICEID       = "deviceId";
    public final static String PARAM_KEY_HUMAN_ID       = "humanId";
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

    //123满分家园，124满分小伙伴，125城市之间
    public final static int WX_PAY_CONFIG_ID = 124;


    /**
     * 获取摇一摇页面
     * @param deviceId SN
     * */
    public void getWXShopDevicePage(String deviceId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put(PARAM_KEY_DEVICEID, deviceId);

        NetFactory.getHttp().post(ScApi.URL_WX_SHOP_DEVICE_PAGE, params, responseCallback);
    }


    /**
     * 预支付
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     *
     * */
    public void prePay(Long humanId, String amount, int wayType,
                       AjaxCallBack<? extends Object> responseCallback){
        FinalHttp fh = NetFactory.getHttp();

        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_WAYTYPE, String.valueOf(wayType));
        params.put(PARAM_KEY_WXOPENID, String.valueOf(humanId));
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(humanId));
        params.put(PARAM_KEY_AMOUNT, amount);
        params.put(PARAM_KEY_NONCESTR, WXUtil.genNonceStr());//随机字符串（32位,不能为空!）

        fh.post(URL_PRE_PAY, params, responseCallback);
    }

    /**
     * 预支付(充值)
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     *
     * */
    public static void prePayForApp(Long humanId, String amount, int wayType,
                             AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(humanId));
        params.put(PARAM_KEY_AMOUNT, amount);
        params.put(PARAM_KEY_NONCESTR, WXUtil.genNonceStr());//随机字符串（32位,不能为空!）
        params.put(PARAM_KEY_WAYTYPE, String.valueOf(wayType));
        if(wayType == WAYTYPE_WXPAY){
            params.put("configId", String.valueOf(WX_PAY_CONFIG_ID));
        }else{
            params.put(PARAM_KEY_WXOPENID, String.valueOf(humanId));
        }

        NetFactory.getHttp().post(URL_PRE_PAY_APP, params, responseCallback);
    }

    /**
     * 订单预支付
     * @param humanId 人员编号
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     *
     * */
    public void prePayOrder(Long humanId, String orderIds, int btype,
                            int wayType,
                       AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_WAYTYPE, String.valueOf(wayType));
        if(wayType == WAYTYPE_ALIPAY){
            params.put(PARAM_KEY_WXOPENID, String.valueOf(humanId));
        }else if(wayType == WAYTYPE_WXPAY){
            params.put("configId", String.valueOf(WX_PAY_CONFIG_ID));
        }else{
            params.put(PARAM_KEY_WXOPENID, String.valueOf(humanId));
        }

        params.put(PARAM_KEY_NONCESTR, WXUtil.genNonceStr());//随机字符串（32位,不能为空!）
        params.put(PARAM_KEY_ORDER_IDS, orderIds);
        params.put(PARAM_KEY_BIZ_TYPE, String.valueOf(btype));

        ZLogger.d(String.format("[POST]prePayOrder: %s?%s", URL_PRE_PAY_ORDER, params.toString()));
        NetFactory.getHttp().post(URL_PRE_PAY_ORDER, params, responseCallback);
    }

    /**
     * 满分家园账户充值
     * @param tradeNo 交易号
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * @param token
     *
     * */
    public static void mfhAccountPay(String tradeNo, String orderIds, int btype,
                            String token,
                            AjaxCallBack<? extends Object> responseCallback){
        FinalHttp fh = NetFactory.getHttp();

        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_PR_EORDER_ID, tradeNo);
        params.put(PARAM_KEY_ORDER_ID, orderIds);
        params.put(PARAM_KEY_TOKEN, token);
        params.put(PARAM_KEY_BIZ_TYPE, String.valueOf(btype));

        ZLogger.d(String.format("[POST]mfhAccountPay: %s?%s", URL_MF_ACCOUNT_PAY, params.toString()));
        fh.post(URL_MF_ACCOUNT_PAY, params, responseCallback);
    }

//    private AjaxCallBack userProfileCallback2 = new AjaxCallBack<Object>() {
//        @Override
//        public void onSuccess(Object o) {
//            super.onSuccess(o);
//            Log.d("Nat: getUserProfile success", o.toString());
//        }
//
//        @Override
//        public void onFailure(Throwable t, String strMsg) {
//            super.onFailure(t, strMsg);
//            Log.d("Nat: getUserProfile failed", strMsg);
//        }
//    };

}

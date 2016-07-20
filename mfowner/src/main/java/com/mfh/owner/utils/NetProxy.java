package com.mfh.owner.utils;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;
import com.mfh.owner.wxapi.WXUtil;

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
    //用户信息
    private final static String URL_MY_PROFILE = MfhApi.URL_BASE_SERVER + "/baseProfile/myProfile";
    //修改登录密码
    private final static String URL_USER_UPDATE_LOGINPWD = MfhApi.URL_BASE_SERVER + "/sys/human/updateUserPassword";

    //摇一摇·店铺
    private final static String URL_WX_SHOP_DEVICE_PAGE = MfhApi.URL_BASE_SERVER + "/wxShopDevicePage/list";
    //更新用户信息:昵称/性别
    private final static String URL_USER_UPDATE = MfhApi.URL_BASE_SERVER + "/sys/human/update";
    //修改头像
    private final static String URL_USER_UPLOAD_HEAD= MfhApi.URL_BASE_SERVER + "/sys/human/uploadHumanHeadImg";

    //服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
    public static final String ALIPAY_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/commonuseraccount/notifyAccount";
    public static final String ALIPAY_ORDER_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/pmcstock/notifyOrder";


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


    public final static int WAYTYPE_ALIPAY = 32;//支付宝支付
    public final static int WAYTYPE_WXPAY = 512;//微信支付

    //123满分家园，124满分小伙伴，125城市之间
    public final static int WX_PAY_CONFIG_ID = 123;


    /**
     * 获取用户个人信息(需要重登录)
     *
     * {"code":"0","msg":"查询成功!","version":"1",
     * "data":{"humanId":245389,"amount":0.0,"score":0.0,"favoriteNum":1,"waitPayNum":4,"waitReceiveNum":0,"waitPraiseNum":0,"serviceOrderNum":0,"shoppingCartNum":1,"cardCouponsNum":0,"defaultStock":null,"defaultSubids":"璞墅小区"}}
     * */
    public void getUserProfile(AjaxCallBack<? extends Object> responseCallback){
        FinalHttp fh = NetFactory.getHttp();
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//        fh.addHeader("Cookie", SharedPreferencesHelper.getLastSessionId());
        fh.post(URL_MY_PROFILE, params, responseCallback);
    }

    /**
     * 获取摇一摇页面
     * @param deviceId SN
     * */
    public static void getWXShopDevicePage(String deviceId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put(PARAM_KEY_DEVICEID, deviceId);

        NetFactory.getHttp().post(URL_WX_SHOP_DEVICE_PAGE, params, responseCallback);
    }

    /**
     * 修改用户密码
     *
     * {"code":"0","msg":"操作成功!","version":"1","data":""}
     *
     * @param humanId 登录用户编号
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     * 注：确认新密码在调用接口前做处理，默认确认新密码和新密码相同。
     * */
    public static void updateUserPassword(Long humanId, String oldPwd, String newPwd, AjaxCallBack<? extends Object> responseCallback){
        FinalHttp fh = NetFactory.getHttp();
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(humanId));
        params.put(PARAM_KEY_OLD_PWD, oldPwd);
        params.put(PARAM_KEY_NEW_PWD, newPwd);
        params.put(PARAM_KEY_CONFIRM_PWD, newPwd);
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        fh.post(URL_USER_UPDATE_LOGINPWD, params, responseCallback);
    }

    /**
     * 修改 支付密码
     *
     * {"code":"0","msg":"操作成功!","version":"1","data":""}
     *
     * @param humanId 登录用户编号
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     * 注：确认新密码在调用接口前做处理，默认确认新密码和新密码相同。
     * */
    public static void updatePayPassword(Long humanId, String oldPwd, String newPwd, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(humanId));
        params.put(PARAM_KEY_OLD_PWD, oldPwd);
        params.put(PARAM_KEY_NEW_PWD, newPwd);
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp().post(URL_USER_UPDATE_PAYPWD, params, responseCallback);
    }

    /**
     * 更新用户个人资料
     * @param humanId
     * @param jsonString Json格式字符串
     * */
    public static void updateProfile(String jsonString, AjaxCallBack<? extends Object> responseCallback){
        FinalHttp fh = NetFactory.getHttp();
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_JSONSTR, jsonString);
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        fh.post(URL_USER_UPDATE, params, responseCallback);
    }

    /**
     * 修改个人头像
     * @param humanId
     * */
    public static void uploadUserHeader(Long humanId, File file, AjaxCallBack<? extends Object> responseCallback){
        FinalHttp fh = NetFactory.getHttp();
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(humanId));
        try {
            params.put(PARAM_KEY_FILETOUPLOAD, file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("不存在的文件:" + file.getAbsolutePath());
        }
        params.put(PARAM_KEY_FILETOUPLOAD, MfhLoginService.get().getCurrentSessionId());
        fh.post(URL_USER_UPLOAD_HEAD, params, responseCallback);
    }


    /**
     * 预支付
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     *
     * */
    public static void prePay(Long humanId, String amount, int wayType,
                       AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_HUMAN_ID, String.valueOf(humanId));
        params.put(PARAM_KEY_AMOUNT, amount);
        params.put(PARAM_KEY_NONCESTR, WXUtil.genNonceStr());//随机字符串（32位,不能为空!）
        params.put(PARAM_KEY_WAYTYPE, String.valueOf(wayType));
        params.put(PARAM_KEY_WXOPENID, String.valueOf(humanId));

        NetFactory.getHttp().post(URL_PRE_PAY, params, responseCallback);
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
        FinalHttp fh = NetFactory.getHttp();

        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_WAYTYPE, String.valueOf(wayType));
        if(wayType == WAYTYPE_ALIPAY){
            params.put(PARAM_KEY_WXOPENID, String.valueOf(humanId));
        }else if(wayType == WAYTYPE_WXPAY){
            params.put(PARAM_KEY_CHANNEL_ID, String.valueOf(WX_PAY_CONFIG_ID));
        }else{
            params.put(PARAM_KEY_WXOPENID, String.valueOf(humanId));
        }

        params.put(PARAM_KEY_NONCESTR, WXUtil.genNonceStr());//随机字符串（32位,不能为空!）
        params.put(PARAM_KEY_ORDER_IDS, orderIds);
        params.put(PARAM_KEY_BIZ_TYPE, String.valueOf(btype));

        ZLogger.d(String.format("[POST]prePayOrder: %s?%s", URL_PRE_PAY_ORDER, params.toString()));
        fh.post(URL_PRE_PAY_ORDER, params, responseCallback);
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

package com.mfh.buyers.utils;


import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.Date;

/**
 * Mobile · H5 · URL
 * Created by Administrator on 2015/5/12.
 */
public class MobileURLConf {
    public static String BASE_URL_APP = "http://devmobile.manfenjiayuan.cn";
    public static String BASE_URL_RESOURCE = "http://devresource.manfenjiayuan.cn/user/";
    //    scheme://host.domain:port/path/filename
    public static String DOMAIN = "devmobile.manfenjiayuan.cn";

    static{
        if(BizConfig.RELEASE){
            BASE_URL_APP = "http://mobile.manfenjiayuan.cn";
            BASE_URL_RESOURCE = "http://resource.manfenjiayuan.cn/user/";
            DOMAIN = "mobile.manfenjiayuan.cn";
        }else{
            BASE_URL_APP = "http://devmobile.manfenjiayuan.cn";
            BASE_URL_RESOURCE = "http://devresource.manfenjiayuan.cn/user/";
            DOMAIN = "devmobile.manfenjiayuan.cn";
        }
    }

    //登录成功跳转（自定义）
    public final static String URL_NATIVIE_REDIRECT_AUTH = BASE_URL_APP + "/native/redirect/success/login.html";

    /** 用户注册·登录 */
    public final static String URL_AUTH_INDEX = BASE_URL_APP + "/m/auth/index.html";
    /** 用户登录 */
    public final static String URL_AUTH_LOGIN = BASE_URL_APP + "/m/auth/login.html";
    /** 新用户注册 */
    public final static String URL_AUTH_REGISTER = BASE_URL_APP + "/m/auth/register.html";
    /** 新用户注册信息填写 */
    public final static String URL_AUTH_REGISTER_PROFILE = BASE_URL_APP + "/m/auth/register.html";
    /** 注册（成功）,忘记密码 */
    public final static String URL_AUTH_FORGET = BASE_URL_APP + "/m/auth/forget.html";
    /** 验证手机号码 */
    public final static String URL_AUTH_VALIDATE_TELPHONE = BASE_URL_APP + "/m/auth/myphone.html";
    /** 重置密码 */
    public final static String URL_AUTH_REPWD = BASE_URL_APP + "/m/auth/repwd.html";
    //个人主页
    public final static String URL_ME = BASE_URL_APP + "/m/me/index.html";
    //收藏
    public final static String URL_ME_FAVOR_COLLECTION = BASE_URL_APP + "/m/me/favor/collection.html";
    //订单(status: 1待付款/2待收货/3待评价)
    public final static String URL_ME_ORDER_MARKET = BASE_URL_APP + "/m/me/order/market_order.html";
    //购物车
    public final static String URL_ME_CART = BASE_URL_APP + "/m/me/cart.html";
    /**套餐日历*/
    public final static String URL_ME_PACKAGES = BASE_URL_APP + "/m/shopping/orderCalendar.html";
    /**预定修改*/
    public final static String URL_ME_PACKAGES_CHANGE = BASE_URL_APP + "/m/shopping/orderChange.html";
    //钱包
    public final static String URL_ME_WALLET = BASE_URL_APP + "/m/me/wallet.html";
    //卡包
    public final static String URL_ME_CARDPACK = BASE_URL_APP + "/m/me/coupon/index.html";
    //包裹
    public final static String URL_ME_PARCEL = BASE_URL_APP + "/m/me/package.html";
    //任务
//    public final static String URL_ME_TASK = BASE_URL_APP + "/m/me/package.html";
    //满分小伙伴
    public final static String URL_ME_MFHPARTER = BASE_URL_APP + "/m/member/index.html";
    //常住小区
    public final static String URL_ME_SUBDIS = BASE_URL_APP + "/m/me/mysubdis.html";
    //收货网点
    public final static String URL_ME_RECEIVESTOCK = BASE_URL_APP + "/m/me/myreceivestock.html";
    //反馈
    public final static String URL_FEEDBACK = BASE_URL_APP + "/m/shopping/feedBack.html";

    //修改支付密码
    public final static String URL_ME_SETTINGS_CHANGE_PAYPWD = BASE_URL_APP + "/m/me/settings/changePWD/payPWD.html";

    //服务
    public final static String URL_ME_SERVER = BASE_URL_APP + "/m/serv/index.html";

    //仓储列表(摇一摇)
    public final static String URL_STOCK_LIST = BASE_URL_APP + "/m/stock/list.html";
    //出库(扫一扫·条形码)
    public final static String URL_STOCK_OUT = BASE_URL_APP + "/m/stock/stockout/stockout_index.html";

    //订单购买
    public final static String URL_MARKET_BUY = BASE_URL_APP + "/m/market/buy.html";
    //商铺
    public final static String URL_MARKET_SHOP = BASE_URL_APP + "/m/market/shop.html";

    /**小伙伴接单*/
    public final static String URL_MFPARTER_TAKE_ORDER = BASE_URL_APP + "/m/member/order/takeover.html";
    /**小伙伴配送*/
    public final static String URL_MFPARTER_DELIVER = BASE_URL_APP + "/m/member/package/receive.html";


    //replace the real url
    public final static String LOCALE_AUTH_LOGIN = "/m/auth/login.html";//.html



    /**
     * 重新组合URL
     * */
    public static String generateUrl(String baseUrl, String params){
        if(baseUrl == null){
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);

        //统一URL参数格式
        //appid
        if(!baseUrl.contains("?")){
            sb.append("?");
        }else{
            sb.append("&");
        }
        sb.append(String.format("appid=%s", MfhApplication.getAppId()));

        //随机字符串
        sb.append("&");
        sb.append(String.format("t=%s", String.valueOf(new Date().getTime())));

        sb.append("&");
        sb.append(String.format("JSESSIONID=%s", MfhLoginService.get().getCurrentSessionId()));

        //添加新参数
        if(params != null){
            sb.append("&");
            sb.append(params);
        }

        return sb.toString();
    }

}

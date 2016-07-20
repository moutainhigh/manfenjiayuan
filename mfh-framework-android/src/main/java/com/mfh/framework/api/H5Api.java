package com.mfh.framework.api;

import com.mfh.framework.BizConfig;

/**
 * H5页面
 * Created by bingshanguxue on 4/19/16.
 */
public class H5Api {
    public static String BASE_URL_APP = "http://devmobile.manfenjiayuan.cn";
    public static String BASE_URL_RESOURCE = "http://devresource.manfenjiayuan.cn/user/";
    //    scheme://host.domain:port/path/filename
    public static String DOMAIN = "devmobile.manfenjiayuan.cn";

    static{
        if(BizConfig.RELEASE){
            BASE_URL_APP = "http://mobile.manfenjiayuan.cn";
            BASE_URL_RESOURCE = "http://resource.manfenjiayuan.cn/user/";
            DOMAIN = "mobile.manfenjiayuan.com";
        }else{
            BASE_URL_APP = "http://devmobile.manfenjiayuan.cn";
            BASE_URL_RESOURCE = "http://devresource.manfenjiayuan.cn/user/";
            DOMAIN = "devmobile.manfenjiayuan.cn";
        }
    }

    public final static String URL_M_VIP = "http://m.vip.com/?f=mxmgge";
    public final static String URL_YHD = "http://m.yhd.com/1?tracker_u=104887410188";
    public final static String URL_TMALL = "http://www.tmall.com/wh/tpl/tmfp-m-wh/jx/index?ali_trackid=2:mm_26632357_8426500_28318607:1438058284_310_2059986409&e=ko4TH20EU7gcQipKwQzePCperVdZeJviK7Vc7tFgwiFRAdhuF14FMdG1qk4zRQOZt4hWD5k2kjPy80H7gHL6a4ddreqVkCMUpBN_LcdyCX4-z0jsbgbe5aUuZxIcp9pfUIgVEmFmgnbDX0-HH2IEVa7A5ve_EYDnFveQ9Ld2jopwTqWNBsAwm-IKl4JSR4lzm5MwX8IrdI_a0nLjfwM40VtnzkQmOHoGIYULNg46oBA&type=2";
    public final static String URL_TUNIU = "http://m.tuniu.com/#p=15192";
    public final static String URL_GANJI = "http://3g.ganji.com/?ca_name=xiaomi_mingzham_008_ggshouye&ca_s=tg_xiaomi&ca_n=sy001&ca_i=ad";
    public final static String URL_TAOBAO = "http://ai.m.taobao.com/index.html?pid=mm_32549094_7052631_23486504";

    public final static String URL_XIAOMI = "http://m.mi.com/v2.html";
    public final static String URL_MEITUAN = "http://i.meituan.com/?nodown&utm_source=waputm_xmllqgg&utm_medium=wap";


    public final static String URL_HOME = "http://ai.m.taobao.com/index.html?pid=mm_32549094_7052631_23486504";
    public final static String URL_LICENSE = "http://www.taobao.com/go/chn/member/agreement.php?spm=a2145.7514111.0.0.5WDSxe";
    public final static String URL_APP_DESCRIPTION = "http://www.taobao.com/go/chn/member/agreement.php?spm=a2145.7514111.0.0.5WDSxe";
    public final static String URL_HELP = "http://www.taobao.com/go/chn/member/agreement.php?spm=a2145.7514111.0.0.5WDSxe";

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
    /**订单（商超专用：城市之间, status: 1待付款/2待收货/3待评价）*/
    public final static String URL_ME_ORDER_MALL = BASE_URL_APP + "/m/me/order/supmkt_order.html";
    /**订单详情（商超专用：城市之间）*/
    public final static String URL_MARKET_ORDER_DETAIL_MALL = BASE_URL_APP + "/m/market/order/detail_supmkt.html";


    /**评价*/
    public final static String URL_EVALUATE_ORDER = BASE_URL_APP + "/app/evoluation.html";
    /**优惠券领取页面*/
    public final static String URL_MARKET_COUPON = BASE_URL_APP + "/m/market/coupon/share.html";

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


    /**首页店铺*/
    public final static String URL_HOME_SHOP = BASE_URL_APP + "/app/shopping.html";


    //replace the real url
    public final static String LOCALE_AUTH_LOGIN = "/m/auth/login.html";//.html
}

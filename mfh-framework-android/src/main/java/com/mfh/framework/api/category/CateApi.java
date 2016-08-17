package com.mfh.framework.api.category;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.net.NetFactory;

/**
 * 类目API
 * Created by bingshanguxue on 16/3/2.
 */
public class CateApi {

    //
    public final static Long FRONT_CATEGORY_ID_LAUNDRY = 3543L;//洗衣
    public final static Long FRONT_CATEGORY_ID_POS = 3407L;//POS


    /**
     * tenantId，代表类目体系的属性，目前130222就是满分家园，可以作为一个变量
     * 注意：与当前登录用户的tenantId不一样
     */
    public final static String CATEGORY_TENANT_ID = NetFactory.getServerUrl("category.tenant.id");

    /**
     * 类目查询－－一级类目
     */
    public final static String URL_CATEGORYINFO_COMNQUERY = MfhApi.URL_BASE_SERVER + "/scCategoryInfo/comnQuery";

    /**
     * pos类目查询接口：/scCategoryInfo/getCodeValue?parentId=6585&page=1&rows=20
     */
    public final static String URL_CATEGORYINFO_GETCODEVALUE = MfhApi.URL_BASE_SERVER + "/scCategoryInfo/getCodeValue";


    public final static int DOMAIN_TYPE_PROD = 0;//实物型产品-默认
    public final static int DOMAIN_TYPE_SERVICE = 1;//服务型产品


    public final static int CATE_POSITION_BACKEND = 0;//后台类目
    public final static int CATE_POSITION_FRONT = 1;//前台类目

    /**
     * 后台类目
     */
    public final static Integer BACKEND_CATE_BTYPE_NORMAL = 0;//普通商品，默认
    public final static Integer BACKEND_CATE_BTYPE_PACKAGE = 1;//商品套餐
    public final static Integer BACKEND_CATE_BTYPE_FRESH = 2;//生鲜类
    public final static Integer BACKEND_CATE_BTYPE_SMOKE = 3;//香烟
    public final static Integer BACKEND_CATE_BTYPE_BAKING = 4;//烘培
    public final static Integer BACKEND_CATE_BTYPE_FRUIT = 5;//水果类

    /**
     * 前台类目
     */
    public final static Integer FRONTEND_CATE_BTYPE_FRESH = 101;//生鲜类


    public static String backendCatetypeName(Integer value) {
        if (value == null) {
            return "Unknow";
        }

        if (value.equals(BACKEND_CATE_BTYPE_NORMAL)) {
            return "普通商品";
        } else if (value.equals(BACKEND_CATE_BTYPE_PACKAGE)) {
            return "商品套餐";
        } else if (value.equals(BACKEND_CATE_BTYPE_FRESH)) {
            return "生鲜";
        } else if (value.equals(BACKEND_CATE_BTYPE_SMOKE)) {
            return "香烟";
        } else if (value.equals(BACKEND_CATE_BTYPE_BAKING)) {
            return "烘培";
        } else if (value.equals(BACKEND_CATE_BTYPE_FRUIT)) {
            return "水果";
        } else {
            return "Unknow";
        }
    }


    /**
     * 前台类目
     */
    public final static Integer PLAT = 0;//满分提供设备、代运营等,如满分提供摇一摇设备
    public final static Integer STOCK = 2;//物品保管
    public final static Integer SC = 3;//线上商城
    public final static Integer HA = 4;//房产中介
    public final static Integer HM = 5;//家政
    public final static Integer PIJU = 6;//皮具护理
    public final static Integer ONE_SHOP = 7;//一站购
    public final static Integer TRANSPORT = 8;//包裹物流
    public final static Integer POS = 9;//线下商超(pos机)
    public final static Integer LAUNDRY = 10;//衣服洗护
    public final static Integer REPAIR = 11;//报修
    public final static Integer DAILYSETTLE = 98;//日结
    public final static Integer RECHARGE = 99;//代充值
    public final static Integer FRESH = 101;//生鲜
    public final static Integer FRUIT = 102;//水果

    public static String frontCatetypeName(Integer value) {
        if (value.equals(PLAT)) {
            return "满分平台";
        } else if (value.equals(STOCK)) {
            return "物品保管";
        } else if (value.equals(SC)) {
            return "线上商城";
        } else if (value.equals(HA)) {
            return "房产中介";
        } else if (value.equals(HM)) {
            return "家政";
        } else if (value.equals(PIJU)) {
            return "皮具护理";
        } else if (value.equals(TRANSPORT)) {
            return "包裹物流";
        } else if (value.equals(POS)) {
            return "线下商超";
        } else if (value.equals(LAUNDRY)) {
            return "衣服洗护";
        } else if (value.equals(REPAIR)) {
            return "报修";
        } else if (value.equals(DAILYSETTLE)) {
            return "日结";
        } else if (value.equals(RECHARGE)) {
            return "代充值";
        } else {
            return "Unknow";
        }
    }
}

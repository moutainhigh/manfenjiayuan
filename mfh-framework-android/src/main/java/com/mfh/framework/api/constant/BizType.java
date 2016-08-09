package com.mfh.framework.api.constant;

/**
 * 业务类型(前后台统一)
 * Created by bingshanguxue on 16/3/2.
 */
public class BizType {
    public final static Integer PLAT            = 0;//满分平台，提供设备、代运营、系统各类后台业务等,如满分提供摇一摇设备
    public final static Integer LAUNDRY         = 1;//衣服洗护
    public final static Integer STOCK           = 2;//快递代收
    public final static Integer SC              = 3;//线上商城
    public final static Integer HA              = 4;//房产中介
    public final static Integer HM              = 5;//家政
    public final static Integer PIJU            = 6;//皮具护理
    public final static Integer POS             = 7;//线下商超(pos机)
    public final static Integer TRANSPORT       = 8;//包裹物流
    public final static Integer INVENTORY       = 9;//商品仓配(供应链和配送运输)
    public final static Integer SUGGEST         = 10;//建议
    public final static Integer REPAIR          = 11;//报修
    public final static Integer SEND            = 12;//快递代揽
    public final static Integer CAR_REPAIR      = 13;//汽车快修
    public final static Integer DAILYSETTLE     = 98;//日结
    public final static Integer RECHARGE        = 99;//代充值

    //自定义业务类型
    public final static Integer CASH_QUOTA        = 2001;//现金授权
    public final static Integer INCOME_DISTRIBUTION        = 2002;//现金授权

    public static String name(Integer value) {
        if (value == null){
            return "";
        }

        if (value.equals(PLAT)) {
            return "满分平台";
        }
        else if (value.equals(LAUNDRY)) {
            return "衣服洗护";
        }
        else if (value.equals(STOCK)) {
            return "物品保管";
        }
        else if (value.equals(SC)) {
            return "线上商城";
        }
        else if (value.equals(HA)) {
            return "房产中介";
        }
        else if (value.equals(HM)) {
            return "家政";
        }
        else if (value.equals(PIJU)) {
            return "皮具护理";
        }
        else if (value.equals(POS)) {
            return "线下商超";
        }
        else if (value.equals(TRANSPORT)) {
            return "包裹物流";
        }
        else if (value.equals(INVENTORY)) {
            return "商品仓配";
        }
        else if (value.equals(SUGGEST)) {
            return "建议";
        }
        else if (value.equals(REPAIR)) {
            return "报修";
        }
        else if (value.equals(DAILYSETTLE)) {
            return "日结";
        }
        else if (value.equals(RECHARGE)) {
            return "代充值";
        }
        else if (value.equals(CASH_QUOTA)) {
            return "自定义－现金授权";
        }
        else if (value.equals(INCOME_DISTRIBUTION)) {
            return "自定义－清分";
        }
        else{
            return "Unknow";
        }
    }
}

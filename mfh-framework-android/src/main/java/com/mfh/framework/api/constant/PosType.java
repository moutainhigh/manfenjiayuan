package com.mfh.framework.api.constant;

/**
 * 业务类型子类型(前后台统一)<br>
 * {@link BizType#POS}<br>
 * Created by bingshanguxue on 16/3/2.
 */
public class PosType {
    public final static Integer POS_STANDARD    = 0;//线下商超－标品（default）
    public final static Integer POS_FRESH       = 2;//线下商超－生鲜
    public final static Integer POS_SMOKE       = 3;//线下商超－香烟
    public final static Integer POS_BAKING      = 4;//线下商超－烘培
    public final static Integer POS_TYPE_ELEM       = 5;//饿了吗
    public final static Integer POS_TYPE_MEITUAN    = 6;//美团
    public final static Integer POS_TYPE_BAIDU      = 7;//百度外卖
    public final static Integer POS_TYPE_KOUBEI     = 8;//口碑
    public final static Integer POS_TYPE_DIANPING   = 9;//大众点评
    public final static Integer POS_TYPE_TELPHONE   = 10;//电话

    public static String name(Integer value) {
        if (value == null){
            return "";
        }
        if (value.equals(POS_STANDARD)) {
            return "社区超市－标品";
        }
        else if (value.equals(POS_FRESH)) {
            return "社区超市－生鲜";
        }
        else if (value.equals(POS_SMOKE)) {
            return "社区超市－香烟";
        }
        else if (value.equals(POS_BAKING)) {
            return "社区超市－烘培";
        }
        else if (value.equals(POS_TYPE_ELEM)) {
            return "饿了么";
        }
        else if (value.equals(POS_TYPE_MEITUAN)) {
            return "美团";
        }
        else if (value.equals(POS_TYPE_BAIDU)) {
            return "百度外卖";
        }
        else if (value.equals(POS_TYPE_KOUBEI)) {
            return "口碑";
        }
        else if (value.equals(POS_TYPE_DIANPING)) {
            return "大众点评";
        }
        else if (value.equals(POS_TYPE_TELPHONE)) {
            return "电话";
        }
        else{
            return "Unknow";
        }
    }
}

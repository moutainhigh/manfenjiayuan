package com.manfenjiayuan.im.constants;

/**
 * 消息技术类型
 * Created by bingshanguxue on 16/3/2.
 */
public class IMTechType {
    public static final String RAW         = "raw";
    public static final String TEXT        = "text";
    public static final String VOICE       = "voice";
    public static final String TUWEN       = "tuwen";
    public static final String IMAGE       = "image";
    public static final String EMOTION     = "emotion";
    public static final String POS         = "position";
    public static final String VIDEO       = "video";
    public static final String CARD        = "card";
    public static final String LINK        = "link";
    public static final String JSON        = "json";
    public static final String TEMPLATE    = "templete";
    public static final String RESOURCE    = "resource";


    public static String name(String value) {
        switch (value) {
            case RAW:
                return "简单类型";
            case TEXT:
                return "文本消息";
            case VOICE:
                return "声音消息";
            case TUWEN:
                return "图文";
            case IMAGE:
                return "图片";
            case EMOTION:
                return "表情/符号";
            case POS:
                return "位置";
            case VIDEO:
                return "视频";
            case CARD:
                return "名片";
            case LINK:
                return "链接";
            case JSON:
                return "对象";
            case TEMPLATE:
                return "模版";
            case RESOURCE:
                return "资源";
            default:
                return "Unkown";
        }
    }
}

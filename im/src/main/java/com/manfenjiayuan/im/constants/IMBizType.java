package com.manfenjiayuan.im.constants;

/**
 * 消息业务类型
 * Created by bingshanguxue on 16/3/2.
 */
public class IMBizType {
    public final static int NOTIFY        = 0;//同原来定义。BizMsgType.SYS
    public final static int CHAT          = 1;//其对应的会话类型为SESSION_TYPE_P2PCHART
    public final static int ASK           = 2;//例如业主通过微信、App发的消息
    public final static int CS            = 3;//例如所有客服人员、机器人对客户咨询消息响应的消息，即管家会话中的消息
    public final static int CMD           = 4;//指令
    public final static int REGISTER      = 5;
    public final static int UNREGISTER    = 6;
    public final static int SCANCODE      = 7;
    //下面两个相当于业务管理消息，其实无需发送到物理端点，到适配器一层即可。
    public final static int BIND          = 8;
    public final static int UNBIND        = 9;
    public final static int ALIVE         = 10;
    public final static int MANAGER       = 100;//(备用,不同于上述业务上的命令消息)
    public final static int ORDER_TRANS_NOTIFY        = 1001;//新订单物流通知需要,通知物流人员前来备货,其中对应的tagOne为订单编号
    public final static int MFPARGER_PEISONG_NOTIFY   = 1002;//订单送达网点通知小伙伴,其中对应的tagOne为订单编号,tagTwo为配送人员
    public final static int MFPARGER_RECEIVE_ORDER    = 1003;//订单创建时通知小伙伴接单,其中对应的tagOne为订单编号
    public final static int EVALUATE_ORDER            = 1004;//订单送达客户通知客户评价,其中对应的tagOne为订单编号
    //1100~1200 POS机&客显
    public final static int TENANT_SKU_UPDATE           = 1101;//网点sku信息更新
    public final static int NEW_PURCHASE_ORDER          = 1102;//新的生鲜预订单
    public final static int CUSTOMER_DISPLAY_PAYORDER   = 1103;//收银机推送订单到客显设备

    public static String name(int value) {
        switch (value) {
            case NOTIFY:
                return "通知";
            case CHAT:
                return "聊天";
            case ASK:
                return "客户咨询";
            case CS:
                return "客服响应";
            case CMD:
                return "命令、菜单";
            case REGISTER:
                return "客户关注";
            case UNREGISTER:
                return "客户取消关注";
            case SCANCODE:
                return "扫描二维码";
            case BIND:
                return "端点绑定";
            case UNBIND:
                return "端点解绑";
            case ALIVE:
                return "心跳";
            case MANAGER:
                return "系统内部控制/内部命令";
            case ORDER_TRANS_NOTIFY:
                return "新订单物流通知";
            case MFPARGER_PEISONG_NOTIFY:
                return "满分小伙伴送达通知";
            case MFPARGER_RECEIVE_ORDER:
                return "满分小伙伴接单通知";
            case EVALUATE_ORDER:
                return "用户评价订单";
            default:
                return "Unkown";
        }
    }
}

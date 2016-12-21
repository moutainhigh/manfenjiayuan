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
    /**
     * 同步商品档案、一品多码关系
     * <ol>
     *     <li>商品档案——增加、删除、修改</li>
     *     <li>前台类目商品——修改零售价</li>
     *     <li>一品多码表——增加、删除、修改</li>
     * </ol>
     * */
    public final static int TENANT_SKU_UPDATE           = 1101;
    public final static int NEW_PURCHASE_ORDER          = 1102;//新的生鲜预订单
    public final static int LOCK_POS_CLIENT_NOTIFY      = 1103;//现金超过授权额度，要求锁定pos机
    public final static int PRE_LOCK_POS_CLIENT_NOTIFY  = 1104;//现金授权额度将要用完，即将锁定pos机
    /**
     * 同步前台类目和商品关系信息
     * <ol>
     *     <li>前台类目——增加、删除</li>
     *     <li>前台类目商品——增加、删除、售罄/补货</li>
     * </ol>
     * */
    public final static int FRONGCATEGORY_GOODS_UPDATE   = 1105;//前台类目商品——删除，售罄/补货
    /**
     * 同步前台类目本身信息
     * <ol>
     *     <li>前台类目——增加、删除、修改</li>
     * </ol>
     * */
    public final static int FRONTCATEGORY_UPDATE   = 1106;//前台类目——增删改，同步类目，
    public final static int ABILITY_UPDATED   = 1110;//删除了买手或骑手，发送1110消息

    public final static int REMOTE_CONTROL_CMD   = 1128;//远程控制指令



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
            case LOCK_POS_CLIENT_NOTIFY:
                return "现金超过授权额度";
            case PRE_LOCK_POS_CLIENT_NOTIFY:
                return "现金授权额度将要用完";
            case EVALUATE_ORDER:
                return "用户评价订单";
            case TENANT_SKU_UPDATE:
                return "网点sku信息更新";
            case NEW_PURCHASE_ORDER:
                return "新的生鲜预订单";
            case REMOTE_CONTROL_CMD:
                return "远程控制";
            default:
                return "Unkown";
        }
    }
}

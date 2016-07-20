package com.manfenjiayuan.im;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息类相关常量
 * Created by bingshanguxue on 16/3/1.
 */
public class IMConstants {
    public static Integer MSG_KIND_0 = 0;//默认属于聊天类
    public static Integer MSG_KIND_1 = 1;//管理类


    public static final String ACTION_MSG_SERVERERROR = "action.msg.servererr";//消息服务器服务异常，但网络正常

    public static final String ACTION_DOWNLOAD_FINISH = "action.download.finish";//第一次开始下载消息会话事件
    public static final String ACTION_REFRESH_ALLUNREAD = "action.refresh.unReadCount";//发送刷新所有未读个数事件
    public static final String ACTION_REFRESH_SESSIONUNREAD = "action.refresh.session.unReadCount";//发送刷新会话未读个数事件
    public static final String ACTION_RECEIVE_MSG = "action.receive.newMsg";//接收到新消息
    public static final String ACTION_RECEIVE_MSG_BACK = "action.receiver.background";//接受到新的消息，后台发消息提示使用
    public static final String ACTION_RECEIVE_SESSION = "action.receive.newSession";//接收到新消息列表
    public static final String ACTION_BEGIN_INPUT = "action.begin.input";//开始输入
    public static final String ACTION_HIDE_MEDIAINPUT = "action.hide.media";//隐藏媒体录入
    public static final String ACTION_SEND_MSG = "action.send.msg";//隐藏媒体录入
    public static final String ACTION_SORT_SCOLL_UNREAD_MSG = "action.sort.scoll.unread.msg";//调转到未读消息
    public static final String ACTION_SAVE_FINISH = "action.save.finish";//   服务组对话保存完成
    public static final String ACTION_DIALOG_MISS = "action.dialog.miss";//消息里的消灭dialog
    public static final String GE_TUI_MSG_SHOW = "ge.tui.msg.show";//个推消息的显示
    public static final String ACTION_REFRESH_UNREAD_COUNT_MAIN = "action.refresh.unread.count.main";
    public static final String ACTION_APP_ENTER_FOREBACKGROUND = "action.app.enter.foreground";//应用程序进入前台

    //intent/bundle 参数
    public static final String EXTRA_NAME_SESSION_ID = "sessionId";//会话编号

    public static final String PARAM_unReadCount = "unReadCount";
    public static final String PARAM_tabIndex = "tabIndex";

    public static final int CODE_REQUEST_XIANGCE = 1;
    public static final int CODE_REQUEST_MATERIAL_LIB = 2;
    public static final int CODE_WORK_ORDER_ADD_XIANGCE = 3;
    public static final int CODE_REQUEST_CYY = 4;

    public static final int MSG_MODE_AREA = 1;//小区模式
    public static final int MSG_MODE_APART = 0;//楼管模式
    public static final int MSG_MODE_TAX = 2;//税务模式
    public static final int MSG_MODE_WORKER = 3;//

    public static final int MSG_NOTIFICATION = 0;//消息提示
    public static final int MSG_NOTIFICATION_SESSIOIN = 1;//会话消息提示
    public static final int NOTIFICATION_NEW_MESSAGE = 2;//会话消息提示


    //类名字符串
    public static final String SERVICE_ADD_ACTIVITY_ClASS_NAME = "add_pmc_work_order";//ServiceAddActivity

    public static String MSG_IMG_DIR = "msgImgDir";

    public final static Long SystemSessionId = -1010101010101L;

//    /**
//     * 获取消息图像的文件访问dao对象
//     * @return
//     */
//    public static FileNetDao getMsgImgFao() {
//        return FineImgView.getFao(null, MSG_IMG_DIR);
//    }

    /**
     * 会话类型范围。随业务类型而定，目前可分为三大类业务会话，物业、商家和满分，每种又可以分为三个变种。
     */
    public static final Integer SESSION_TYPE_P2PCHART = 0;//两端会话,如好友会话、同事会话
    public static final Integer SESSION_TYPE_GROUP = 2;//群组会话
    public static final Integer SESSION_TYPE_GUANJIA_UNBIND = 1;//客户关注后首先置入未绑定客服会话，为简单起见 不再如下面再未区别初始和临时团队。
    public static final Integer SESSION_TYPE_GUANJIA_NORMAL = 101;//已绑定客服团队会话-初始团队  //final Integer GUANJIA_TMP = 102;//已绑定客服团队会话-加入临时成员后的团队

    public static List<Integer> unBindKinds = new ArrayList<Integer>();
    public static List<Integer> bindKinds = new ArrayList<Integer>();

    //客户绑定类型
    public static final Integer CUSTOM_TYPE_UNBIND = 0;//未绑定,等同于GUANJIA_UNBIND
    public static final Integer CUSTOM_TYPE_BIND = 1;  //已关联,等同于GUANJIA_NORMAL
    public static final Integer CUSTOM_TYPE_UNRELATION = 2; //未绑定或已绑定未关联(还未成为会员，包括unbind和 binded但extparam为空)

    //会话业务类型--内置的
    public static final Integer SBY_GUANJIA_NORMAL = 0;//初始会话
    public static final Integer SBY_GUANJIA_EXTEND = 1;//扩展会话,-加入临时成员后的团队

    public static final Integer SBY_P2P_FRIEND = 0;//好友会话
    public static final Integer SBY_P2P_WORKER = 1;//同事会话
    public static final Integer SBY_P2P_MACHINE = 2;//客服机器与粉丝的会话

    public static final Integer SBY_GROUP_NORMAL = 0;//普通好友群组会话,无须tag_one
    public static final Integer SBY_GROUP_COMPANY = 1;//同事群组会话,其tag_one是公司编号或部门编号
    public static final Integer SBY_GROUP_SERVICE = 2;//服务群组会话，其tag_one是订单号，如针对订单,该群中的消息业务类型都是（MSG_BIZTYPE_ASK=2）

    /*public static final Integer SHANGJIA_UNBIND = 10;//客户关注后首先置入商家未绑定客服会话。
    public static final Integer SHANGJIA_NORMAL = 11;//商家客服团队会话-初始团队
    public static final Integer SHANGJIA_TMP = 12;//商家客服团队会话-加入临时成员后的团队

    public static final Integer MFH_UNBIND = 13;//客户关注后首先置入满分未绑定客服会话。
    public static final Integer MFH_NORMAL = 14;//满分客服团队会话-初始团队
    public static final Integer MFH_TMP = 15;//满分客服团队会话-加入临时成员后的团队
*/
    static {
        unBindKinds.add(SESSION_TYPE_GUANJIA_UNBIND);
        bindKinds.add(SESSION_TYPE_GUANJIA_NORMAL);
        //bindKinds.add(GUANJIA_TMP);
    }

    /**
     * 是否属于未绑定类会话
     * @param st
     * @return
     * @author zhangyz created on 2014-11-14
     */
    public static Boolean isUnbind(Integer st) {
        return unBindKinds.contains(st);
    }

    /**
     * 是否属于已绑定类会话
     * @param st
     * @return
     * @author zhangyz created on 2014-11-14
     */
    public static Boolean isbind(Integer st) {
        return bindKinds.contains(st);
    }

    private static Boolean isAllBindInner(List<Integer> sts, List<Integer> sessionKinds) {
        Boolean ret = null;
        for (Integer st : sts) {
            boolean bTmp = sessionKinds.contains(st);
            if (ret == null)
                ret = bTmp;
            else {
                if (!(ret && bTmp))
                    throw new RuntimeException("传入的会话类型不属于同一类!");
            }
        }
        return ret;
    }

    /**
     * 获取一致的会话类型大类
     * @param sts
     * @return 1:已绑定类； 0:未绑定类, null: 普通类，无客服含义
     * @throws //若两种都包含，抛出异常
     * @author zhangyz created on 2014-11-14
     */
    public static Integer isAllBind(List<Integer> sts) {
        Boolean ret = isAllBindInner(sts, bindKinds);
        if (ret == null)
            ret = isAllBindInner(sts, unBindKinds);
        else
            return ret ? CUSTOM_TYPE_BIND : CUSTOM_TYPE_UNBIND;
        if (ret == null)
            return null;
        else
            return ret ? CUSTOM_TYPE_UNBIND : CUSTOM_TYPE_BIND;
    }

    /**
     * 判断bind类型，若没有则返回null
     * @param sts
     * @return
     * @author zhangyz created on 2015-1-22
     */
    public static Integer getBind(Integer sts) {
        if (bindKinds.contains(sts))
            return CUSTOM_TYPE_BIND;
        if (unBindKinds.contains(sts))
            return CUSTOM_TYPE_UNBIND;
        return null;
    }

    //所有业务定义的特殊类型会话从下面值开始
    public static final Integer BASE_BIZ_TYPE = 1000;

    public static class MsgTitle{
        public static final String SYS = "系统通知";
    }
}

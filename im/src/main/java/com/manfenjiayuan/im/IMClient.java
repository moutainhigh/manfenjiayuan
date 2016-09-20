package com.manfenjiayuan.im;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.manfenjiayuan.im.bean.BizMsgParamWithSession;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

/**
 * SDK的入口，主要完成登录，退出，连接管理等功能。也是获取其他模块的入口
 * Created by bingshanguxue on 16/3/4.
 */
public class IMClient {
    private static IMClient instance = null;
    private Context mContext;//上下文
    private IMGroupManager groupManager = null;
    private IMChatManager chatManager = null;

    /**
     * 返回 IMClient 实例
     *
     * @return
     */
    public static IMClient getInstance() {
        if (instance == null) {
            synchronized (IMClient.class) {
                if (instance == null) {
                    instance = new IMClient();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     * */
    public void init(Context context){
        this.mContext = context;
        this.groupManager = new IMGroupManager();
        this.chatManager = new IMChatManager();
    }


    /**
     * 从本地数据库加载群组到内存的操作，如果你的应用中有群组，请加上这句话（要求在每次进入应用的时候调用）
     * */

    /**
     * 注册消息桥(需要登录)
     */
    public void registerBridge(){
        if (!MfhLoginService.get().haveLogined()){
            ZLogger.d("注册消息桥失败，未登录。");
            return;
        }

        //检查参数
        String clientId = IMConfig.getPushClientId();
        Long guid = MfhLoginService.get().getCurrentGuId();

        registerBridge(guid, clientId);
    }

    /**
     * 注册消息桥
     * */
    public void registerBridge(final Long guid, String clientId){
        if(guid == null || guid.equals(-1L)){
            ZLogger.df("注册消息桥失败，guid 无效");
            return;
        }

        if(StringUtils.isEmpty(clientId)){
            ZLogger.df("注册消息桥失败，clientId 无效");
            return;
        }

        if (mContext == null){
            ZLogger.df("请先初始化IMClient");
            return;
        }

          NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<BizMsgParamWithSession,
                NetProcessor.Processor<BizMsgParamWithSession>>(
                new NetProcessor.Processor<BizMsgParamWithSession>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //查询失败
//                        animProgress.setVisibility(View.GONE);
                        ZLogger.df("注册消息桥失败" + errMsg);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"新增成功!","version":"1","data":{"val":"463"}}
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);
                        RspBean<BizMsgParamWithSession> retValue = (RspBean<BizMsgParamWithSession>) rspData;
                        ZLogger.df(String.format("注册消息桥成功: %s", JSON.toJSON(retValue.getValue())));

                        IMConfig.updateIdentify(guid);
                    }
                }
                , BizMsgParamWithSession.class
                , mContext) {
        };

        IMApi.registerBridge(guid, clientId, responseCallback);
    }


    public IMGroupManager groupManager() {
        return groupManager;
    }

    public IMChatManager chatManager() {
        return chatManager;
    }
}

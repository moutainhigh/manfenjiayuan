package com.manfenjiayuan.im;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.manfenjiayuan.im.bean.BizMsgParamWithSession;
import com.manfenjiayuan.im.utils.IMFactory;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import net.tsz.afinal.http.AjaxParams;

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
        if (mContext == null){
            ZLogger.d("请先初始化IMClient");
            return;
        }

        if (!MfhLoginService.get().haveLogined()){
            ZLogger.d("没有登录，暂停注册。");
            return;
        }

        //检查参数
        String clientId = IMConfig.getPushClientId();
        Long guid = MfhLoginService.get().getCurrentGuId();
        if(clientId == null || guid == null || guid.equals(-1L)){
            ZLogger.d("参数无效，clientId和guid不能为空");
            return;
        }
        IMConfig.updateIdentify(guid);

        AjaxParams params = new AjaxParams();
        params.put(MfhApi.PARAM_KEY_CHANNEL_ID, MfhApi.CHANNEL_ID);
        params.put(MfhApi.PARAM_KEY_QUEUE_NAME, MfhApi.PARAM_VALUE_QUEUE_NAME_DEF);
        params.put(MfhApi.PARAM_KEY_JSONSTR, JSON.toJSONString(IMFactory.register(clientId, guid)));
        params.put(MfhApi.PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

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
                    }
                }
                , BizMsgParamWithSession.class
                , mContext) {
        };

        AfinalFactory.postDefault(MfhApi.URL_REGISTER_MESSAGE, params, responseCallback);
    }


    public IMGroupManager groupManager() {
        return groupManager;
    }

    public IMChatManager chatManager() {
        return chatManager;
    }
}

package com.manfenjiayuan.im.database.dao;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.im.IMConstants;
import com.manfenjiayuan.im.bean.BizMsgParamWithSession;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.MfhEntity;
import com.mfh.framework.database.dao.BaseNetDao;
import com.mfh.framework.database.dao.DaoUrl;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import net.tsz.afinal.http.AjaxParams;

import java.text.SimpleDateFormat;

/**
 * 基于网络从java后台获取消息列表
 * Created by Administrator on 14-5-7.
 */
public class EmbMsgNetDao extends BaseNetDao<BizMsgParamWithSession, String> {
    private SimpleDateFormat sdf = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);

    @Override
    protected Class<BizMsgParamWithSession> initPojoClass() {
        return BizMsgParamWithSession.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    private boolean downLoading = false;//正在下载标志

    public boolean isDownLoading() {
        return downLoading;
    }

    public void setDownLoading(boolean downLoading) {
        this.downLoading = downLoading;
    }

    /**
     * 发送消息
     * @param params
     * @param processor
     */
    public void sendMessage(AjaxParams params, NetProcessor.ComnProcessor<EmbMsg> processor) {
        final String picUrl = params.getNormalValue("picUrl");
        NetCallBack.SaveCallBackJson call = new NetCallBack.SaveCallBackJson<EmbMsg> (processor, EmbMsg.class){
            @Override
            protected EmbMsg changeJsonToBean(JSONObject json) {
                return EmbMsg.fromSendMessage(json, picUrl);
            }
        };
        //使用post，因为消息有中文
        params.remove("picUrl");
        params.put(MfhApi.PARAM_KEY_CHANNEL_ID, String.valueOf(MfhApi.CHANNEL_ID));
        params.put(MfhApi.PARAM_KEY_QUEUE_NAME, MfhApi.PARAM_VALUE_QUEUE_NAME_DEF);
        AfinalFactory.postDefault(MfhApi.URL_REGISTER_MESSAGE, params, call);
    }



    @Override
    protected void initUrlInfo(DaoUrl daoUrl) {
        /*daoUrl.setListUrl("/mobile/pmc/msg/getSessionItems");
        daoUrl.setCreateUrl("/mobile/pmc/msg/sendMsg");*/
        //setMsgMode(MsgConstants.MSG_MODE_TAX);
    }

    public void setMsgMode(int msgMode) {
        if (msgMode == IMConstants.MSG_MODE_TAX){
            daoUrl.setListUrl("/mobile/msg/getSessionItems");
            daoUrl.setCreateUrl("/mobile/msg/sendMsg");
        }else {
            daoUrl.setListUrl("/mobile/pmc/msg/getSessionItems");
            daoUrl.setCreateUrl("/mobile/pmc/msg/sendMsg");
        }
    }



    public static void fillDateMsg(JSONObject json, MfhEntity<String> es) throws Exception{
        /*es.setCreatedBy(json.getString("createdBy"));
        es.setUpdatedBy(json.getString("updatedBy"));
        String cdTemp = json.getString("createdDate");
        if (cdTemp != null)
            es.setCreatedDate(JsonParser.defaultFormat.parse(cdTemp));
        cdTemp = json.getString("updatedDate");
        if (cdTemp != null)
            es.setUpdatedDate(JsonParser.defaultFormat.parse(cdTemp));*/
    }

   /* *//**
     * 把json转换成bean
     * @param //json
     * @return
     *//*
    protected EmbMsg changeJsonToBean(JSONObject json) {
        try {
            String strParamJson = json.getString("param");
            JSONObject mainParam = JSONObject.parseObject(strParamJson);
            strParamJson = mainParam.getString("msgBean");
            JSONObject msgMainJson = JSONObject.parseObject(strParamJson);//消息主体内容

            String strMsgJso = msgMainJson.getString("msgBody");

            EmbMsg es = new EmbMsg();

            es.setMsgInfo(strMsgJso);

            //JSONObject msgJson =  JSONObject.parseObject(strMsgJso);
            //es.setMsgContent(msgJson.getString("content"));
            //es.setMediaType(msgJson.getString("type"));

            //es.setSignName(msgMainJson.getString("signname"));//

            es.setId(json.getString("id"));
            es.setSessionId(json.getLong("sessionid"));
            //es.setHumanName(json.getString("humanname"));
            //es.setNickName(json.getString("nicknamebin"));

            //es.setRemark(json.getString("remark"));
            es.setFormatCreateTime(json.getString("createdDate"));

            //es.setMsgTime(NetTaskCallBack.defaultFormat.parse(json.getString("createtime")));
            es.setHeadImageUrl(json.getString("localheadimageurl"));
            //es.setMsgType(json.getInteger("msgtype"));

            es.setGuid(mainParam.getString("fromGuid"));
            fillDateMsg(json, es);

            //本地数据库自己保存
            es.setOwnerId(getContext().getSharedPreferences("login", Activity.MODE_PRIVATE).getString("app.login.name", ""));
            es.setIsRead(0);
            return es;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }*/

   /* @Override
    protected NetCallBack.NetTaskCallBack<EmbMsg, NetProcessor.QueryRsProcessor<EmbMsg>> genQueryCallBack(NetProcessor.QueryRsProcessor<EmbMsg> callBack) {
        final EmbMsgNetDao that = this;
        return new NetCallBack.QueryRsCallBackOfJson<EmbMsg>(callBack, this.getPojoClass(), this.getContext()){
           *//* @Override
            protected EmbMsg changeJsonToBean(JSONObject json) {
                return that.changeJsonToBean(json);
            }*//*

            @Override
            protected void doWhenAnyException(Throwable ex) {
                super.doWhenAnyException(ex);
                setDownLoading(false);//停止下载,以便允许重新开始。
            }

            @Override
            protected void doFailure(Throwable t, String errMsg) {
                super.doFailure(t, errMsg);
                setDownLoading(false);//停止下载,以便允许重新开始。
            }
        };
    }*/

}

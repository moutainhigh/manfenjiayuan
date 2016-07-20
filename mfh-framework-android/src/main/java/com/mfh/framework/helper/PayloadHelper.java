package com.mfh.framework.helper;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.PushUtil;

/**
 * 接收推送数据的工具类
 * Created by 李潇阳 on 2014/9/4.
 */
public class PayloadHelper extends PushUtil {

    /**
     * 获得推送的类型，比如订单
     * @param jsonString
     * @return一般返回的是一个数字字符串
     */
    public static int getJsonMsgType(String jsonString) {
        try{
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            JSONObject msgObj = jsonObject.getJSONObject("msg");
            JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");
            return msgBeanObj.getIntValue("bizType");
        }catch (Exception e){
            ZLogger.e(e.toString());
            return -1;
        }
    }


    /**
     * Notification所需要的返回值
     * @param data
     * @return
     */
    public static String getReturnValue(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONObject msgBean = jsonObject.getJSONObject("msgBean");
        JSONObject msgBody = msgBean.getJSONObject("msgBody");
        String param = msgBody.getString("param");
        JSONObject paramBean = JSONObject.parseObject(param);
        JSONObject paramMsgBean = paramBean.getJSONObject("msgBean");
        JSONObject paramMsgBody = paramMsgBean.getJSONObject("msgBody");
        String response = paramMsgBody.getString("content");
        return response;
    }

    public static String getUserName(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONObject msgBean = jsonObject.getJSONObject("msgBean");
        JSONObject msgBody = msgBean.getJSONObject("msgBody");
        return msgBody.getString("pointName");
    }

    /**
     * 返回sessionId
     * @param data
     * @return
     */
    public static Long getSessionIdByJson(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        Long sessionId = jsonObject.getLong("sessionId");
        return sessionId;
    }

}

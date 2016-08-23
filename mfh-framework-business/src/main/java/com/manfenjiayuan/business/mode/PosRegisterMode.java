package com.manfenjiayuan.business.mode;

import android.os.SystemClock;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.MfhApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.Date;

/**
 * {@link InvSkuGoods}
 * Created by bingshanguxue on 16/3/17.
 */
public class PosRegisterMode {

    public void create(String channelId, String channelPointId, Long netId, final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                            ZLogger.df("get terminal id success:" + retStr);
                            SharedPreferencesManager.setTerminalId(retStr);
                            if (listener != null) {
                                listener.onSuccess(retStr);
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("get terminal id failed(%s),please set manual", errMsg));
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        JSONObject order = new JSONObject();
        order.put("serialNo", MfhApplication.getWifiMac15Bit());
        order.put("channelId", channelId);
        order.put("channelPointId", channelPointId);
        order.put("netId", netId);

        ZLogger.df("register terminal id," + order.toJSONString());
        MfhApiImpl.posRegisterCreate(order.toJSONString(), responseCallback);
    }

    public void update(String terminalId, String channelId, String channelPointId, Long netId, final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                            ZLogger.df("注册设备成功:" + retStr);
                            if (!StringUtils.isEmpty(retStr)){
                                String[] retA = retStr.split(",");
                                if (retA.length > 1){
                                    SharedPreferencesManager.setTerminalId(retA[0]);

                                    // TODO: 8/22/16 修改本地系统时间
                                    ZLogger.d(String.format("当前系统时间1: %s",
                                            TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
                                    Date serverDateTime = TimeUtil.parse(retA[1], TimeUtil.FORMAT_YYYYMMDDHHMMSS);
//                                Date serverDateTime = TimeUtil.parse("2016-08-22 13:09:57", TimeUtil.FORMAT_YYYYMMDDHHMMSS);
                                    if (serverDateTime != null){
                                        //设置时间
                                        try{
                                            boolean isSuccess = SystemClock.setCurrentTimeMillis(serverDateTime.getTime());
                                            ZLogger.d(String.format("修改系统时间 %b: %s", isSuccess,
                                                    TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
                                        }
                                        catch (Exception e){
                                            ZLogger.ef("修改系统时间失败:" + e.toString());
                                        }
                                    }

                                    if (listener != null) {
                                        listener.onSuccess(retA[0]);
                                    }
                                }
                                else{
                                    if (listener != null) {
                                        listener.onError("返回数据不完整");
                                    }
                                }
                            }
                            else{
                                if (listener != null) {
                                    listener.onError("返回数据为空");
                                }
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("get terminal id failed(%s),please set manual", errMsg));
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        JSONObject order = new JSONObject();
        order.put("id", terminalId);
        order.put("channelId", channelId);
        order.put("channelPointId", channelPointId);
        order.put("netId", netId);

        ZLogger.df("register terminal id," + order.toJSONString());
        MfhApiImpl.posRegisterUpdate(order.toJSONString(), responseCallback);
    }

}

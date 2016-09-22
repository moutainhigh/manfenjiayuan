package com.mfh.framework.api.posRegister;

import android.os.SystemClock;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PosRegisterMode {

    public void create(String channelId, String channelPointId, Long netId,
                       final OnModeListener<String> listener) {
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
        order.put("serialNo", MfhApplication.genSerialNo());
        order.put("channelId", channelId);
        order.put("channelPointId", channelPointId);
        order.put("netId", netId);

        PosRegisterApi.create(order.toJSONString(), responseCallback);
    }

    public void update(String terminalId, String channelId, String channelPointId, Long netId,
                       final OnModeListener<String> listener) {
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
                            if (!StringUtils.isEmpty(retStr)) {
                                String[] retA = retStr.split(",");
                                if (retA.length > 1) {
                                    SharedPreferencesManager.setTerminalId(retA[0]);

                                    // TODO: 8/22/16 修改本地系统时间
                                    ZLogger.d(String.format("当前系统时间1: %s",
                                            TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
                                    Date serverDateTime = TimeUtil.parse(retA[1], TimeUtil.FORMAT_YYYYMMDDHHMMSS);
//                                Date serverDateTime = TimeUtil.parse("2016-08-22 13:09:57", TimeUtil.FORMAT_YYYYMMDDHHMMSS);
                                    if (serverDateTime != null) {
                                        //设置时间
                                        try {
                                            boolean isSuccess = SystemClock.setCurrentTimeMillis(serverDateTime.getTime());
                                            ZLogger.d(String.format("修改系统时间 %b: %s", isSuccess,
                                                    TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
                                        } catch (Exception e) {
                                            ZLogger.ef("修改系统时间失败:" + e.toString());
                                        }
                                    }

                                    if (listener != null) {
                                        listener.onSuccess(retA[0]);
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onError("返回数据不完整");
                                    }
                                }
                            } else {
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

        PosRegisterApi.update(order.toJSONString(), responseCallback);
    }

    public void list(PageInfo pageInfo,final OnPageModeListener<PosRegister> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        ZLogger.d(String.format("加载POS设备列表:page=%d/%d", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<PosRegister>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<PosRegister> rs) {
                //此处在主线程中执行。
                List<PosRegister> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<PosRegister> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载关联租户失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, PosRegister.class, MfhApplication.getAppContext());

        PosRegisterApi.list(pageInfo, queryRsCallBack);
    }
}
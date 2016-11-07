package com.mfh.owner.wxapi;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserApi;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.core.logic.AsyncTaskCallBack;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.owner.dao.wexin.WeiXinNetDao;
import com.mfh.owner.entity.weixin.WeiXinUserInfo;

import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2014/11/15.
 */
public class WeiXinAuthService extends BaseService<WeiXinUserInfo, Long, WeiXinNetDao> {
    private WeiXinNetDao netDao;

    public WeiXinAuthService() {
        super();
        netDao = new WeiXinNetDao();
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }


    @Override
    protected Class<WeiXinNetDao> getDaoClass() {
        return null;
    }

    /**
     * 获取用户的AccessToken
     * */
    public void queryAccessToken(String code) {
        Intent intent = new Intent(WXConstants.AUTH_START);
        getContext().sendBroadcast(intent);
        AjaxParams params = new AjaxParams();
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("appid", WXConstants.APP_ID);
        params.put("secret", WXConstants.APP_SECRET);
        ZLogger.d("queryAccessToken " + String.format("%s?%s", WXConstants.ACCESS_TOKEN_URL, params.toString()));
        NetFactory.getHttp().get(WXConstants.ACCESS_TOKEN_URL, params, new AsyncTaskCallBack(){

            @Override
            protected void doSuccess(Object rawValue) {
                try {
                    JSONObject jsonObject = new JSONObject(rawValue.toString());
                    String access_token = jsonObject.getString("access_token");
                    String openid = jsonObject.getString("openid");
                    final AjaxParams checkParams = new AjaxParams();
                    checkParams.put("access_token", access_token);
                    checkParams.put("openid", openid);
                    checkAccessToken(checkParams);
                } catch (JSONException e) {
                    Intent intent = new Intent(WXConstants.AUTH_FAIL);
                    intent.setType("queryAccessToken failed");
                    getContext().sendBroadcast(intent);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                Intent intent = new Intent(WXConstants.AUTH_FAIL);
                intent.setType("JSONException parse failed");
                getContext().sendBroadcast(intent);
                super.onFailure(t, strMsg);
            }
        });
    }

    /**
     * 检验AccessToken 是否正确
     * */
    private void checkAccessToken(final AjaxParams params) {
        NetFactory.getHttp().get(WXConstants.CHECK_ACCESS_TOKEN, params, new AsyncTaskCallBack<Object>() {
            @Override
            protected void doSuccess(Object rawValue) {
                try {
                    JSONObject object = new JSONObject(rawValue.toString());
                    String errcode = object.getString("errcode");
                    if ("0".equals(errcode)) {
                        queryWeixinUserInfo(params);
                    }
                    else{
                        Intent intent = new Intent(WXConstants.AUTH_FAIL);
                        intent.setType("checkAccessToken errorcode:" + errcode);
                        getContext().sendBroadcast(intent);
                    }
                } catch (JSONException e) {
                    Intent intent = new Intent(WXConstants.AUTH_FAIL);
                    intent.setType("checkAccessToken JSONException parse failed");
                    getContext().sendBroadcast(intent);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                Intent intent = new Intent(WXConstants.AUTH_FAIL);
                intent.setType("checkAccessToken failed");
                getContext().sendBroadcast(intent);
                super.onFailure(t, strMsg);
            }
        });
    }

    /**
     * 微信后台查询用户信息
     * */
    private void queryWeixinUserInfo(AjaxParams params) {
        NetFactory.getHttp().get(WXConstants.GET_USER_INFO, params, new AsyncTaskCallBack<Object>() {
            @Override
            protected void doSuccess(Object rawValue) {
                final WeiXinUserInfo info = JSON.parseObject(rawValue.toString(), WeiXinUserInfo.class);
                SharedPreferences.Editor editor = getContext().getSharedPreferences("weixin", Context.MODE_PRIVATE).edit();
                editor.putString("name", info.getNickname());
                editor.putString("headimgurl", info.getHeadimgurl());
                editor.putString("wxopenid", info.getOpenid());
                editor.commit();
                final String clientId = IMConfig.getPushClientId();
                checkBindWx(clientId, info);
            }

            @Override
            protected void doFailure(Throwable t, String errMsg) {
                Intent intent = new Intent(WXConstants.AUTH_FAIL);
                intent.setType("queryWeixinUserInfo failed");
                getContext().sendBroadcast(intent);
                super.doFailure(t, errMsg);
            }
        });
    }

    /**
     * 判断微信是否已经绑定
     * */
    private void checkBindWx(String clientId, final WeiXinUserInfo info) {
        if(clientId == null){
            return;
        }

        netDao.checkBindWx(info.getOpenid(), clientId, new AsyncTaskCallBack() {
            @Override
            protected void doSuccess(Object rawValue) {
                try {
                    Intent intent = new Intent(WXConstants.AUTH_SUCCESS);
                    getContext().sendBroadcast(intent);
                    JSONObject object = new JSONObject(rawValue.toString()).getJSONObject("data");
                    if (object == null || object.length() == 0) { //未绑定
                        intent = new Intent(WXConstants.BIND_TO_XEIXIN);
                        intent.putExtra("userInfo", info);
                        getContext().sendBroadcast(intent);
                    }
                    else {  //已绑定
                        String mobile = object.getString("mobile");
                        String loginName = object.getString("loginName");
                        String password = object.getString("password");
                        MfhLoginService.get().doLoginAsync(loginName, password, new LoginCallback() {
                            @Override
                            public void loginSuccess(UserMixInfo user) {
                                Intent intent = new Intent(WXConstants.LOGIN_SUCCESS);
                                getContext().sendBroadcast(intent);
                            }

                            @Override
                            public void loginFailed(String errMsg) {

                            }
                        }, UserApi.URL_LOGIN, "PO", info.getOpenid());
                    }
                } catch (Exception e) {
                    Intent intent = new Intent(WXConstants.AUTH_FAIL);
                    intent.setType("checkBindWx parse failed");
                    getContext().sendBroadcast(intent);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                Intent intent = new Intent(WXConstants.AUTH_FAIL);
                intent.setType("checkBindWx failed");
                getContext().sendBroadcast(intent);
                super.onFailure(t, strMsg);
            }
        });
    }

    @Override
    public Context getContext() {
        return MfhApplication.getAppContext();
    }
}

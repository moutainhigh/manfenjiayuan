package com.mfh.owner.dao.wexin;

import com.mfh.framework.core.logic.AsyncTaskCallBack;
import com.mfh.framework.database.dao.BaseNetDao;
import com.mfh.framework.database.dao.DaoUrl;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.NetFactory;
import com.mfh.owner.entity.weixin.WeiXinUserInfo;

import net.tsz.afinal.http.AjaxParams;


/**
 * Created by Administrator on 2014/11/17.
 */
public class WeiXinNetDao extends BaseNetDao<WeiXinUserInfo, Long> {
    @Override
    protected void initUrlInfo(DaoUrl daoUrl) {

    }

    @Override
    protected Class<WeiXinUserInfo> initPojoClass() {
        return WeiXinUserInfo.class;
    }

    @Override
    protected Class<Long> initPkClass() {
        return Long.class;
    }

    public void checkBindWx(String wxopenid, String clientId, AsyncTaskCallBack callBack) {
        AjaxParams params = new AjaxParams();
        params.put("wxopenid", wxopenid);
        params.put("clientId", clientId);

        NetFactory.getHttp().post(MfhApi.URL_BASE_SERVER + "/sys/human/checkBindWx", params, callBack);
    }

    public void bindWx(String wxopenid, String mobile, String nickName, String heardimgurl, String clientId, AsyncTaskCallBack callBack) {
        AjaxParams params = new AjaxParams();
        params.put("wxopenid", wxopenid);
        params.put("mobile", mobile);
        params.put("nickName", nickName);
        params.put("heardimgurl", heardimgurl);
        params.put("clientId", clientId);
        NetFactory.getHttp().post(MfhApi.URL_BASE_SERVER + "/sys/human/doBindWx", params, callBack);
    }
}

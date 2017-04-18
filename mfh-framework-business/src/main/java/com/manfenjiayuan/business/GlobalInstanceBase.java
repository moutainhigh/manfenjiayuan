package com.manfenjiayuan.business;

import com.manfenjiayuan.business.hostserver.TenantInfoWrapper;
import com.manfenjiayuan.business.utils.SharedPrefesManagerBase;
import com.manfenjiayuan.im.IMApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.api.tenant.PayCfgId;
import com.mfh.framework.api.tenant.SassInfo;
import com.mfh.framework.api.tenant.TenantInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.rxapi.http.RxHttpManager;

import java.util.List;
import java.util.Observable;

/**
 * Created by bingshanguxue on 16/12/2016.
 */

public class GlobalInstanceBase extends Observable{
    private static GlobalInstanceBase instance = null;
    //租户信息
    private TenantInfoWrapper mHostServer;

    /**
     * 返回 DataDownloadManager 实例
     *
     * @return
     */
    public static GlobalInstanceBase getInstance() {
        if (instance == null) {
            synchronized (GlobalInstanceBase.class) {
                if (instance == null) {
                    instance = new GlobalInstanceBase();
                }
            }
        }
        return instance;
    }


    public GlobalInstanceBase() {
        mHostServer = SharedPrefesManagerBase.getHostServer();

        updateApi(mHostServer);

    }

    public TenantInfoWrapper getHostServer() {
        return mHostServer;
    }

    public void updateHostServer(TenantInfo tenantInfo, SassInfo sassInfo){
        if (tenantInfo == null){
            return;
        }

        TenantInfoWrapper hostServer = new TenantInfoWrapper();
        hostServer.setSaasId(tenantInfo.getSaasId());
        hostServer.setSaasName(tenantInfo.getSaasName());
        hostServer.setArea(tenantInfo.getArea());
        hostServer.setDomainUrl(tenantInfo.getId());

        if (sassInfo != null){
            hostServer.setContact(sassInfo.getContact());
            hostServer.setLogopicUrl(sassInfo.getLogopicUrl());
            hostServer.setMobilenumber(sassInfo.getMobilenumber());
            hostServer.setPayInfos(sassInfo.getPayInfos());
        }
        mHostServer = hostServer;

        SharedPrefesManagerBase.setHostServer(mHostServer);

        updateApi(mHostServer);

        setChanged();    //标记此 Observable对象为已改变的对象
        notifyObservers();    //通知所有观察者
    }

    public void updateHostServer(SassInfo sassInfo){
        if (mHostServer == null || sassInfo == null){
            return;
        }

        mHostServer.setContact(sassInfo.getContact());
        mHostServer.setLogopicUrl(sassInfo.getLogopicUrl());
        mHostServer.setMobilenumber(sassInfo.getMobilenumber());
        mHostServer.setPayInfos(sassInfo.getPayInfos());

        SharedPrefesManagerBase.setHostServer(mHostServer);

        updateApi(mHostServer);
        setChanged();    //标记此 Observable对象为已改变的对象
        notifyObservers();    //通知所有观察者
    }

    /**
     * 获取皮肤名称
     * */
    public String getSkinName(){
        String skinName = "default.skin";

        String domainUrl = mHostServer != null ? mHostServer.getDomainUrl() : null;
        if (!StringUtils.isEmpty(domainUrl)){
            if (domainUrl.startsWith("admin")) {
                skinName = "mixicook.skin";
            }
            else if (domainUrl.startsWith("qianwj")) {
                skinName = "qianwj.skin";
            }
            else if (domainUrl.startsWith("lanlj")) {
                skinName = "lanlj.skin";
            }
        }

        return skinName;
    }

    /**
     * 动态更新API
     * */
    private void updateApi(TenantInfoWrapper hostServer){
        if (hostServer != null){
            RxHttpManager.API_BASE_URL = String.format("http://%s/pmc/", hostServer.getDomainUrl());
            MfhApi.URL_BASE_SERVER = String.format("http://%s/pmc", hostServer.getDomainUrl());
            MobileApi.DOMAIN = hostServer.getDomainUrl();
            List<PayCfgId> payCfgIds = hostServer.getPayInfos();
            if (payCfgIds != null && payCfgIds.size() > 0){
                for (PayCfgId payCfgId : payCfgIds){
                    if ((payCfgId.getPayType() & WayType.ALI_F2F) == WayType.ALI_F2F){
                        PayApi.ALIPAY_CHANNEL_ID = payCfgId.getChId();
                    }
                    else if ((payCfgId.getPayType() & WayType.WX_F2F) == WayType.WX_F2F){
                        PayApi.WXPAY_CHANNEL_ID = payCfgId.getChId();
                    }
                }
            }
        }
        MfhApi.register();
        IMApi.register();

        ZLogger.d(String.format("MfhApi.URL_BASE_SERVER=%s", MfhApi.URL_BASE_SERVER));
        ZLogger.d(String.format("MobileApi.DOMAIN=%s", MobileApi.DOMAIN));
        ZLogger.d(String.format("PayApi.ALIPAY_CHANNEL_ID=%s", PayApi.ALIPAY_CHANNEL_ID));
        ZLogger.d(String.format("PayApi.WXPAY_CHANNEL_ID=%s", PayApi.WXPAY_CHANNEL_ID));
    }

}

package com.mfh.petitestock;


import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.AppException;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;

import java.io.File;

/**
 * Created by NAT.ZZN(bingshanguxue) on 2015/7/10.
 */
public class AppContext extends MfhApplication {

    @Override
    protected boolean isReleaseVersion() {
        //TODO,支持配置开发服务器&正式服务器，需要重新启动
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AppException.CRASH_FOLDER_PATH = getPackageName() + File.separator + "crash";

        super.onCreate();

        ZLogger.CRASH_FOLDER_PATH = getPackageName() + File.separator + "zlogger";

//        //注册应用id到微信
//        WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false).registerApp(WXConstants.APP_ID);
//
        //首次启动
        if(SharedPreferencesManager.isAppFirstStart()){
            //清空旧缓存
//            AppHelper.clearAppCache();
            ZLogger.d(String.format("首次启动:%s-%s", getVersionName(), getVersionCode()));

            SharedPreferencesManager.setTerminalId("");
            SharedPreferencesManager.setAppFirstStart(false);
        }else{
            ZLogger.d(String.format("非首次启动:%s-%s", getVersionName(),  getVersionCode()));
        }
    }

}

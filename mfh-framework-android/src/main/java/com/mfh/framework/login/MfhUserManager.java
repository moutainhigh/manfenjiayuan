package com.mfh.framework.login;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.rxapi.http.RxHttpManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Subscriber;

/**
 * 保存登录相关信息
 * Created by Nat.ZZN on 16/1/25.
 */
public class MfhUserManager {

    //当前登录用户功能模块
    private static List<String> mCurrentModules = new ArrayList<>();

    private static MfhUserManager instance = null;

    /**
     * 获取实例
     *
     * @return
     */
    public static MfhUserManager getInstance() {
        if (instance == null) {
            synchronized (MfhUserManager.class) {
                if (instance == null) {
                    instance = new MfhUserManager();
                }
            }
        }
        return instance;
    }

    /**
     * 退出登录
     * */
    public void logout(final Callback callback) {
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            if (callback != null) {
                callback.onError(-1, "网络未连接");
            }
            return;
        }

        String sessionId = MfhLoginService.get().getCurrentSessionId();
        if (StringUtils.isEmpty(sessionId)) {
            ZLogger.d("会话已经失效");
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        RxHttpManager.getInstance().exit(sessionId, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("退出登录失败:" + e.toString());

                if (callback != null) {
                    callback.onError(-2, e.toString());
                }
            }

            @Override
            public void onNext(String s) {
                ZLogger.df("退出登录成功");
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }


    public void updateModules(){
        String[] moduleNames = MfhLoginService.get().getModuleNameArray();
        if (moduleNames != null){
            mCurrentModules = Arrays.asList(moduleNames);
        }
        else{
            mCurrentModules = new ArrayList<>();
        }
    }

    public boolean containsModule(String moduleName){
        if (mCurrentModules == null){
            return false;
        }

        return mCurrentModules.contains(moduleName);
    }

    /**
     * 检查能力是否存在
     * */
    public static boolean checkModule(String moduleName, List<String> collections){
        if (collections == null){
            return false;
        }

        return collections.contains(moduleName);
    }

    /**
     * 检查能力是否存在
     * */
    public static boolean checkModule(String moduleName, String[] collections){
        if (collections == null){
            return false;
        }

        List<String> modulesList = Arrays.asList(collections);

        return modulesList.contains(moduleName);
    }

}

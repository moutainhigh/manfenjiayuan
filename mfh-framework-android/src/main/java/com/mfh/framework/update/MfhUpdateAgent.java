package com.mfh.framework.update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.R;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.file.FileNetDao;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.uikit.dialog.DialogHelper;

import net.tsz.afinal.http.AjaxParams;

import java.io.File;

/**
 * 应用升级工具类
 * Created by Administrator on 14-6-4.
 */
public class MfhUpdateAgent {
    private Context appContext;
    private FileNetDao fileNetDao;

    private MfhUpdateListener mMfhUpdateListener = null;
    private MfhDownloadListener mMfhDownloadListener = null;


    private Handler updateHandler;
    private static final int MSG_UPDATE_AVAILABLE = 0;//需要更新
    private static final int MSG_UPDATE_LATEST = 1;//已经是最新版本
    private static final int MSG_UPDATE_NOUPDATE = 2;//无更新文件


    public static String APK_NAME_FOR_UPDATE = "appName";//应用程序名，用于检查版本更新
    public static String APK_DOWNLOAD_DIR_NAME = "download";//apk下载存放目录

    public static final String ACTION_APPUPDATE_CHECK_FINISH = "app.update.check.finish";
    public static final String APP_DOWNLOAD_NOTIFY = "app.download.notify";
    public static final String APP_UPDATE_NAME_BY_INIT = "init.activity.app.update.name";


    /**
     * 构造函数
     *
     * @param context
     */
    public MfhUpdateAgent(Context context) {
        this.appContext = context;

        //使用SD卡根路径存储下载的临时文件，若没有sd卡，则使用程序私有目录，比较昂贵
        fileNetDao = new FileNetDao(APK_DOWNLOAD_DIR_NAME, UpdateConfig.URL_APP_UPDATE_DOWNLOAD,
                FileUtil.getSDRootPath());//"/storage/sdcard0"
        fileNetDao.setUseLocalFirst(false);//每次都重新下载
    }

    /**
     * 检查服务器端版本号，若有新版本则启动下载并安装
     *
     * @return
     */
    public void update(Context context, AjaxParams param) {
        updateHandler = new Handler(context.getMainLooper()){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.what == 0 && UpdateConfig.isUpdateAutoPopup()) {
                    YesOrNoToNewVersion((UpdateResponse) msg.obj);
                }

                //外部拦截手动处理更新结果
                if (mMfhUpdateListener != null) {
                    mMfhUpdateListener.onUpdateReturned(msg.what, (UpdateResponse) msg.obj);
                }
            }
        };

        AfinalFactory.getHttp().get(UpdateConfig.URL_APP_UPDATE_VERSIOIN + "?apk=" + APK_NAME_FOR_UPDATE,
                param,
                new NetCallBack.NormalNetTask<UpdateResponse>(UpdateResponse.class) {
                    @Override
                    public void processResult(IResponseData rspData) {
                        RspBean<UpdateResponse> result = (RspBean<UpdateResponse>) rspData;

                        //自动处理更新结果
                        processUpdateResponseInner(result.getValue());
                    }

                    @Override
                    protected void doFailure(Throwable t, String errMsg) {
                        ZLogger.d("doFailure " + errMsg);
                        super.doFailure(t, errMsg);
                        processUpdateResponseInner(null);
                    }
                });
    }

    /**
     * 内部自动处理更新结果
     */
    private void processUpdateResponseInner(UpdateResponse updateResponse) {
        Message msg = new Message();

        int versionCode = -1;
        AppInfo appInfo = AnalysisAgent.getAppInfo(MfhApplication.getAppContext());
        if (appInfo != null){
            versionCode = appInfo.getVersionCode();
        }
        //无更新
        if (updateResponse == null) {
            msg.what = MSG_UPDATE_NOUPDATE;
        }
        else if (updateResponse.getVersionCode() <= versionCode){
            msg.what = MSG_UPDATE_LATEST;
        }
        else{
            msg.what = MSG_UPDATE_AVAILABLE;
        }

        msg.obj = updateResponse;
        updateHandler.sendMessage(msg);
    }

    /**
     * 杀死当前进程
     */
    private void killProcess() {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
        /*ActivityManager activityMan = (ActivityManager)appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> process = activityMan.getRunningAppProcesses();
        int len = process.size();
        for(int i = 0;i<len;i++) {
            if (process.get(i).processName.equals(appContext.getPackageName())) {
                android.os.Process.killProcess(process.get(i).pid);
                break;
            }
        }*/
    }

    /**
     * 提示是否安装更新
     */
    private void YesOrNoToNewVersion(final UpdateResponse updateResponse) {
        AlertDialog.Builder dialog = DialogHelper.getConfirmDialog(appContext,
                appContext.getString(R.string.dialog_message_app_update),
                appContext.getString(R.string.dialog_button_appupdate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startDownload(updateResponse);
                    }
                });
//        dialog.setIcon(R.drawable.ic_launcher);
        dialog.setTitle(appContext.getString(R.string.dialog_title_app_update, updateResponse.getVersionName()));
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 执行下载
     */
    public void startDownload(UpdateResponse updateResponse) {
        if (mMfhDownloadListener != null){
            mMfhDownloadListener.onDownloadStart();
        }

        final Dialog dialog = DialogHelper.genProgressDialog(appContext, false, "正在下载更新程序...");
        //执行下载
        fileNetDao.processFile(updateResponse.getApkName(), new FileNetDao.CallBack() {
            @Override
            public void processFile(File file) {
                try {
                    if (mMfhDownloadListener != null){
                        mMfhDownloadListener.onDownloadEnd(100, "下载完成");
                    }

                    //下载完成，自动执行安装
                    startInstall(file);
                } catch (Throwable e) {
                    if (dialog != null)
                        dialog.dismiss();

                    if (mMfhDownloadListener != null){
                        mMfhDownloadListener.onDownloadEnd(100, e.toString());
                    }
                }
            }

            @Override
            public void onFailure(String fileName, Throwable e) {
                if (dialog != null)
                    dialog.dismiss();
                if (mMfhDownloadListener != null){
                    mMfhDownloadListener.onDownloadEnd(100, e.toString());
                }
            }
        });
    }

    /**
     * 执行安装
     */
    private void startInstall(File file) {
        if (!file.exists())
            throw new RuntimeException("安装包不存在!");

        DeviceUtils.installAPK(appContext, file);

        killProcess();
    }
}

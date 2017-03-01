package com.mfh.litecashier.ui.fragment.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.anlaysis.remoteControl.RemoteControlClient;
import com.mfh.framework.api.res.ResApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.ZipUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.ResHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.service.DataDownloadManager;
import com.mfh.litecashier.ui.dialog.TextInputDialog;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.GlobalInstance;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;
import com.tencent.bugly.beta.Beta;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.mfh.framework.anlaysis.remoteControl.RemoteControlClient.uploadLogFileStep2;

/**
 * 设置－－通用
 * Created by kun on 15/8/31.
 */
public class SettingsCommonFragment extends BaseFragment {

    @BindView(R.id.item_posgoods)
    SettingsItem itemPosGoods;
    //    @Bind(R.id.item_mixi_fresh)
//    SettingsItem itemMixiFresh;
    @BindView(R.id.item_terminal)
    SettingsItem terminalSettingsItem;
    @BindView(R.id.item_version)
    SettingsItem versonSettingsItem;
    @BindView(R.id.item_momory)
    SettingsItem memorySettingsItem;

    @BindView(R.id.toggleItem_softkeyboard)
    ToggleSettingItem tsiSoftKeyboard;
    @BindView(R.id.toggleItem_tts)
    ToggleSettingItem ttsToggleItem;

    private TextInputDialog mTextInputDialog = null;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_common;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        tsiSoftKeyboard.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPrefesManagerFactory.setSoftInputEnabled(isChecked);
            }
        });
        ttsToggleItem.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPrefesManagerFactory.setTtsEnabled(isChecked);
            }
        });


        reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    /**
     * 清空缓存
     */
    @OnClick(R.id.item_momory)
    public void clearCache() {
        showConfirmDialog("系统变慢6，马上清理缓存！",
                "清理", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        AppHelper.clearCacheData();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 商品库恢复出厂设置
     */
    @OnClick(R.id.item_posgoods)
    public void syncGoods() {
        showConfirmDialog("同步商品库到最新版本，同步过程中会先删除历史数据，可能会影响正常收银，确定要同步吗？",
                "全量更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        showProgressDialog(ProgressDialog.STATUS_DONE, "请稍候...", true);

                        // 强制同步
                        SharedPreferencesUltimate.setSyncProductsCursor("");
                        SharedPreferencesUltimate.setPosSkuLastUpdate("");

                        DataDownloadManager.get().syncProducts();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }


//    /**
//     * 米西生鲜
//     */
//    @OnClick(R.id.item_mixi_fresh)
//    public void importFromChainSku() {
//        showConfirmDialog("选择全量更新同步米西生鲜的商品库",
//                "全量更新", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                        //清空米西生鲜商品档案同步游标
//                        String cursorKey1 = String.format("%s_%d_%s",
//                                SharedPreferencesUltimate.PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR,
//                                135799L, String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRESH));
//                        SharedPreferencesUltimate.set(cursorKey1, "");
//                        CloudSyncManager.get().importFromChainSku();
//                    }
//                }, "点错了", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
//    }

    /**
     * 注册设备
     */
    @OnClick(R.id.item_terminal)
    public void registerPlat() {
        GlobalInstance.getInstance().registerPos(getActivity());
    }


    private static long exitTime = 0;
    private static int clickVersionTimes = 0;

    @OnClick(R.id.item_version)
    public void checkUpdate() {
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                Beta.checkUpgrade();
                exitTime = System.currentTimeMillis();
            }
        } else {
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                clickVersionTimes = 1;
                exitTime = System.currentTimeMillis();
//                Beta.checkUpgrade();
            } else {
                clickVersionTimes++;
            }

            if (clickVersionTimes == 8) {
                clickVersionTimes = 0;

                DialogUtil.showHint("恭喜你,你已经获取到超级权限!");
                SharedPrefesManagerFactory.setSuperPermissionGranted(true);
//                ZLogger.LOG_ENABLED = SharedPrefesManagerFactory.isSuperPermissionGranted();

                EventBus.getDefault().post(new SettingsFragment.SettingsEvent(
                        SettingsFragment.SettingsEvent.EVENT_ID_RELOAD_DATA));

                reload();
            } else {
                DialogUtil.showHint(String.format("再点 %d 次即可获得超级权限!", 8 - clickVersionTimes));
            }
        }
    }

    /**
     * 一键反馈:上传日志文件
     */
    @OnClick(R.id.item_onekey_feedback)
    public void onekeyFeedback() {
        if (mTextInputDialog == null) {
            mTextInputDialog = new TextInputDialog(getActivity());
            mTextInputDialog.setCancelable(true);
            mTextInputDialog.setCanceledOnTouchOutside(true);
        }
        mTextInputDialog.initialize("反馈内容", "请输入反馈意见", false,
                new TextInputDialog.OnTextInputListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm(String text) {
                        uploadFile(text);
                    }
                });
        if (!mTextInputDialog.isShowing()) {
            mTextInputDialog.show();
        }
    }
    public void uploadFile(final String feedback) {
        try {
            showProgressDialog(ProgressDialog.STATUS_DONE, "请稍候...", true);
            File zipFile = FileUtil.getSaveFile("", "onekeyfeedback.zip");
            if (zipFile.exists()) {
                zipFile.createNewFile();
            }
            ZipUtils.zipFiles(FileUtil.getSavePath(ZLogger.CRASH_FOLDER_PATH),
                    zipFile);

            if (RxHttpManager.RELEASE) {
                Map<String, String> options = new HashMap<>();
//                options.put("fileToUpload", zipFile.toString());
//                ZLogger.d(zipFile.toString());
                options.put("responseType", "1");
                options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

                String time = ZLogger.DATE_FORMAT.format(new Date());
                String fileName = time + ".log";
                ZLogger.d("fileName: " + fileName);

                File file = FileUtil.getSaveFile("", "onekeyfeedback.zip");//FileUtil.getSaveFile(ZLogger.CRASH_FOLDER_PATH, fileName);
                if (!file.exists()) {
                    return;
                }
                ZLogger.d("file: " + file.getPath());

                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                builder.addFormDataPart("responseType", "1");
                RequestBody requestBody = RequestBody.create(ResHttpManager.ZIP, file);
                builder.addFormDataPart("fileToUpload", file.getName(), requestBody);

                ResHttpManager.getInstance().upload2(MultipartBody.Part.createFormData("responseType", "1"),
                        MultipartBody.Part.createFormData("fileToUpload", file.getName(),
                                requestBody),
                        new MValueSubscriber<Long>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                //retrofit2.adapter.rxjava.HttpException: HTTP 413 Request Entity Too Large
                                ZLogger.ef("一键反馈:" + e.toString());
                                hideProgressDialog();
                            }

                            @Override
                            public void onValue(Long data) {
                                super.onValue(data);
                                StringBuilder sb = new StringBuilder();
                                sb.append(String.format("\n" +
                                                "一键反馈:日志文件编号为 %d\n",
                                        data));
                                sb.append("备注:");
                                if (!StringUtils.isEmpty(feedback)) {
                                    sb.append(feedback).append("\n");
                                }
                                uploadLogFileStep2(sb.toString(), MfhLoginService.get().getLoginName());
                            }
                        });
            } else {
                NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Long,
                        NetProcessor.Processor<Long>>(
                        new NetProcessor.Processor<Long>() {
                            @Override
                            public void processResult(IResponseData rspData) {
                                //{"code":"0","msg":"操作成功!","version":"1","data":""}
                                // {"code":"0","msg":"查询成功!","version":"1","data":null}
                                StringBuilder sb = new StringBuilder();
                                sb.append("一键反馈:\n");
                                if (rspData != null) {
                                    RspValue<Long> retValue = (RspValue<Long>) rspData;
                                    sb.append(String.format("日志文件编号为 %d\n",
                                            retValue.getValue()));
                                }
                                if (!StringUtils.isEmpty(feedback)) {
                                    sb.append(feedback).append("\n");
                                }
                                RemoteControlClient.uploadLogFileStep2(sb.toString(),
                                        MfhLoginService.get().getLoginName());
                                hideProgressDialog();
                            }

                            @Override
                            protected void processFailure(Throwable t, String errMsg) {
                                super.processFailure(t, errMsg);
                                ZLogger.df("查询失败: " + errMsg);
                                hideProgressDialog();
                            }
                        }
                        , Long.class
                        , MfhApplication.getAppContext()) {
                };

                ResApi.upload(zipFile, responseCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }


//    @OnClick(R.id.toggleItem_leddisplay)
//    public void setLedPort() {
//        if (setPortDialog == null) {
//            setPortDialog = new SetPortDialog(getActivity());
//            setPortDialog.setCancelable(false);
//            setPortDialog.setCanceledOnTouchOutside(false);
//        }
//        setPortDialog.initialize("LED客显", PoslabAgent.getPort(),
//                PoslabAgent.getBaudrate(), new SetPortDialog.onDialogClickListener() {
//                    @Override
//                    public void onSetPort(String port, String baudrate) {
//                        tsiLedDisplay.setSubTitle(String.format("端口－[%s]，波特率－[%s]", port, baudrate));
//                        PoslabAgent.set(port);
//                        PoslabAgent.setLedBaudrate(baudrate);
//
//                        //设置串口
//                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_INIT, ""));
//
//                        refresh();
//                    }
//                });
//        if (!setPortDialog.isShowing()) {
//            setPortDialog.show();
//        }
//    }


//    public void setPrinterPort() {
//        if (setPortDialog == null) {
//            setPortDialog = new SetPortDialog(getActivity());
//            setPortDialog.setCancelable(false);
//            setPortDialog.setCanceledOnTouchOutside(false);
//        }
//        setPortDialog.initialize("打印机(GPrinter)", SerialManager.getPrinterPort(),
//                new SetPortDialog.onDialogClickListener() {
//            @Override
//            public void onSetPort(String port, String baudrate) {
//                GPrinterAgent.setPort(port);
//                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_GPRINTER, ""));
//                togglePrinter.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
//                        SerialManager.getPrinterPort(), GPrinterAgent.getBaudrate()));
//            }
//        });
//        if (!setPortDialog.isShowing()) {
//            setPortDialog.show();
//        }
//    }


//    public void setAhScalePort() {
//        if (setPortDialog == null) {
//            setPortDialog = new SetPortDialog(getActivity());
//            setPortDialog.setCancelable(false);
//            setPortDialog.setCanceledOnTouchOutside(false);
//        }
//        setPortDialog.initialize("爱华电子秤(ACS-P215)", AHScaleAgent.getPort(),
//                new SetPortDialog.onDialogClickListener() {
//                    @Override
//                    public void onSetPort(String port, String baudrate) {
//                        AHScaleAgent.setPort(port);
//                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_AHSCALE, ""));
//                        toggleAhscale.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
//                                AHScaleAgent.getPort(),
//                                AHScaleAgent.getBaudrate()));
//                    }
//                });
//        if (!setPortDialog.isShowing()) {
//            setPortDialog.show();
//        }
//    }


    private void reload() {
        try {
            itemPosGoods.setSubTitle(SharedPreferencesUltimate.getSyncProductsCursor());
//            //米西生鲜
//            String cursorKey1 = String.format("%s_%d_%s",
//                    SharedPreferencesUltimate.PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR,
//                    135799L, String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRESH));
//            itemMixiFresh.setSubTitle(SharedPreferencesUltimate.getText(cursorKey1, ""));

            terminalSettingsItem.setSubTitle(SharedPrefesManagerFactory.getTerminalId());

            AppInfo appInfo = AnalysisAgent.getAppInfo(CashierApp.getAppContext());
            if (appInfo != null) {
                versonSettingsItem.setSubTitle(String.format(Locale.US, "%s - %d",
                        appInfo.getVersionName(), appInfo.getVersionCode()));
            }

            tsiSoftKeyboard.setChecked(SharedPrefesManagerFactory.isSoftInputEnabled());
            ttsToggleItem.setChecked(SharedPrefesManagerFactory.isTtsEnabled());


            //        ActivityManager am =  (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            //        ACache cache = ACache.get(CashierApp.getAppContext(), Constants.CACHE_NAME);
//        sbMem.append(String.format("系统总内存:%d\n", memoryInfo.totalMem));
//        sbMem.append(String.format("系统可用内存:%d\n", memoryInfo.availMem));
//        sbMem.append(String.format("低内存阀值:%d\n", memoryInfo.threshold));
//        sbMem.append(String.format("是否处于低内存:%b\n", memoryInfo.lowMemory));
//            memorySettingsItem.setSubTitle("内存:\n");
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

}

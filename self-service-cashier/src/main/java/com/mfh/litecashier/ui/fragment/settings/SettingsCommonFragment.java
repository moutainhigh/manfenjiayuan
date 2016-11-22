package com.mfh.litecashier.ui.fragment.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.hardware.PoslabAgent;
import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.GPrinterAgent;
import com.bingshanguxue.cashier.hardware.scale.AHScaleAgent;
import com.bingshanguxue.cashier.hardware.scale.SMScaleAgent;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.com.PrintManagerImpl;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.hardware.SMScale.FileZillaDialog;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.service.DataDownloadManager;
import com.mfh.litecashier.ui.dialog.SetPortDialog;
import com.mfh.litecashier.ui.dialog.UmsipsDialog;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.GlobalInstance;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;
import com.tencent.bugly.beta.Beta;

import org.century.GreenTagsApi;
import org.century.GreenTagsSettingsDialog;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 设置－－通用
 * Created by kun on 15/8/31.
 */
public class SettingsCommonFragment extends BaseFragment {

    @Bind(R.id.item_posgoods)
    SettingsItem itemPosGoods;
//    @Bind(R.id.item_mixi_fresh)
//    SettingsItem itemMixiFresh;
    @Bind(R.id.item_terminal)
    SettingsItem terminalSettingsItem;
    @Bind(R.id.item_version)
    SettingsItem versonSettingsItem;
    @Bind(R.id.item_momory)
    SettingsItem memorySettingsItem;

    @Bind(R.id.toggleItem_leddisplay)
    ToggleSettingItem tsiLedDisplay;
    @Bind(R.id.toggleItem_printer)
    ToggleSettingItem togglePrinter;
    @Bind(R.id.toggleItem_softkeyboard)
    ToggleSettingItem tsiSoftKeyboard;
    @Bind(R.id.toggleItem_tts)
    ToggleSettingItem ttsToggleItem;

    @Bind(R.id.item_ahscale_rs232)
    ToggleSettingItem toggleAhscale;
    @Bind(R.id.item_smscale_rs232)
    ToggleSettingItem toggleSmscale;
    @Bind(R.id.item_umsips_rs232)
    SettingsItem umsipsRs232SettingsItem;
    @Bind(R.id.item_smscale_ftp)
    ToggleSettingItem toggleSmscaleFtp;
    @Bind(R.id.item_greentags_webservice)
    ToggleSettingItem toggleGreenTags;
    @Bind(R.id.toggle_superPermission)
    ToggleSettingItem toggleSuperPermission;


    private SetPortDialog setPortDialog = null;
    private FileZillaDialog mFileZillaDialog = null;
    private UmsipsDialog mUmsipsDialog = null;
    private GreenTagsSettingsDialog mGreenTagsSettingsDialog = null;


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
        tsiLedDisplay.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                if (GPrinterAgent.isEnabled() != isChecked) {
                    GPrinterAgent.setEnabled(isChecked);

                    EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_INIT, ""));
                }
            }
        });
        togglePrinter.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                if (GPrinterAgent.isEnabled() != isChecked) {
                    GPrinterAgent.setEnabled(isChecked);
                    if (!isChecked){
                        GPrinterAgent.setPort("");
                        SerialManager.setPrinterPort("");

                        togglePrinter.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                                SerialManager.getPrinterPort(),
                                GPrinterAgent.getBaudrate()));
                    }
                    EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_GPRINTER, ""));
                }
            }
        });
        toggleAhscale.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {

                if (AHScaleAgent.isEnabled() != isChecked) {
                    AHScaleAgent.setEnabled(isChecked);
                    if (!isChecked){
                        AHScaleAgent.setPort("");

                        toggleAhscale.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                                AHScaleAgent.getPort(),
                                AHScaleAgent.getBaudrate()));
                    }
                    EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_AHSCALE, ""));
                }

            }
        });
        toggleSmscale.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {

                if (SMScaleAgent.isEnabled() != isChecked) {
                    SMScaleAgent.setEnabled(isChecked);
                    if (isChecked){
                        SMScaleAgent.setEnabled(isChecked);
                        toggleSmscale.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                                SMScaleAgent.getPort(),
                                SMScaleAgent.getBaudrate()));
                    }
                    EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_SMSCALE, ""));
                }

            }
        });
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
        toggleSmscaleFtp.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_B_SYNC_SMSCALE_FTP_ENABLED, isChecked);
            }
        });
        toggleGreenTags.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_B_SYNC_ESL_ENABLED, isChecked);
            }
        });

        toggleSuperPermission.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                boolean isSuperPermissionGranted = SharedPrefesManagerFactory.isSuperPermissionGranted();
                if (isSuperPermissionGranted != isChecked) {
                    SharedPrefesManagerFactory.setSuperPermissionGranted(isChecked);
                    reload();
                }
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

                        DataDownloadManager.get().sync(DataDownloadManager.POSPRODUCTS);
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
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()){
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                Beta.checkUpgrade();
                exitTime = System.currentTimeMillis();
            }
        }
        else{
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                clickVersionTimes = 1;
                exitTime = System.currentTimeMillis();
                Beta.checkUpgrade();
            }
            else{
                clickVersionTimes++;
            }

            if (clickVersionTimes == 8){
                clickVersionTimes = 0;

                DialogUtil.showHint("恭喜你,你已经获取到超级权限!");
                SharedPrefesManagerFactory.setSuperPermissionGranted(true);
                reload();
            }
            else{
                DialogUtil.showHint(String.format("再点 %d 次即可获得超级权限!", 8-clickVersionTimes));
            }
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

    @OnClick(R.id.toggleItem_printer)
    public void setPrinterPort() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.initialize("打印机(GPrinter)", SerialManager.getPrinterPort(),
                new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                SerialManager.setPrinterPort(port);
                GPrinterAgent.setPort(port);
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_GPRINTER, ""));
                togglePrinter.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                        SerialManager.getPrinterPort(), GPrinterAgent.getBaudrate()));
            }
        });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }


    @OnClick(R.id.item_ahscale_rs232)
    public void setAhScalePort() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.initialize("爱华电子秤(ACS-P215)", AHScaleAgent.getPort(),
                new SetPortDialog.onDialogClickListener() {
                    @Override
                    public void onSetPort(String port, String baudrate) {
                        AHScaleAgent.setPort(port);
                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_AHSCALE, ""));
                        toggleAhscale.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                                AHScaleAgent.getPort(),
                                AHScaleAgent.getBaudrate()));
                    }
                });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    @OnClick(R.id.item_smscale_rs232)
    public void configureSmscaleRs232() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.initialize("寺冈电子秤(DS781)", SMScaleAgent.getPort(),
                new SetPortDialog.onDialogClickListener() {
                    @Override
                    public void onSetPort(String port, String baudrate) {
                        SMScaleAgent.setPort(port);
                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_SMSCALE, ""));
                        toggleSmscale.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                                SMScaleAgent.getPort(), SMScaleAgent.getBaudrate()));
                    }
                });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    /**
     * 配置银联参数
     */
    @OnClick(R.id.item_umsips_rs232)
    public void configureUmsipsRs232() {
        if (mUmsipsDialog == null) {
            mUmsipsDialog = new UmsipsDialog(getActivity());
            mUmsipsDialog.setCancelable(false);
            mUmsipsDialog.setCanceledOnTouchOutside(false);
        }
        mUmsipsDialog.init(new UmsipsDialog.onDialogClickListener() {
            @Override
            public void onDatasetChanged() {
                umsipsRs232SettingsItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                        SerialManager.getUmsipsPort(), SerialManager.getUmsipsBaudrate()));
            }
        });
        if (!mUmsipsDialog.isShowing()) {
            mUmsipsDialog.show();
        }
    }


    @OnClick(R.id.item_smscale_ftp)
    public void configureSmscaleFtp() {
        if (mFileZillaDialog == null) {
            mFileZillaDialog = new FileZillaDialog(getActivity());
            mFileZillaDialog.setCancelable(false);
            mFileZillaDialog.setCanceledOnTouchOutside(false);
        }
        mFileZillaDialog.init("FileZila 参数设置", new FileZillaDialog.DialogViewClickListener() {
            @Override
            public void onSubmit() {
                toggleSmscaleFtp.setSubTitle(String.format("%s:%d",
                        SMScaleSyncManager2.FTP_HOST, SMScaleSyncManager2.FTP_PORT));
            }
        });
        if (!mFileZillaDialog.isShowing()) {
            mFileZillaDialog.show();
        }
    }

    @OnClick(R.id.item_greentags_webservice)
    public void showGreenTagsDialog() {
        if (mGreenTagsSettingsDialog == null) {
            mGreenTagsSettingsDialog = new GreenTagsSettingsDialog(getActivity());
            mGreenTagsSettingsDialog.setCancelable(false);
            mGreenTagsSettingsDialog.setCanceledOnTouchOutside(false);
        }
        mGreenTagsSettingsDialog.init(new GreenTagsSettingsDialog.DialogViewClickListener() {
            @Override
            public void onSubmit() {
                toggleGreenTags.setSubTitle(GreenTagsApi.URL);
            }
        });
        if (!mGreenTagsSettingsDialog.isShowing()) {
            mGreenTagsSettingsDialog.show();
        }
    }

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
            tsiLedDisplay.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    PoslabAgent.getPort(), PoslabAgent.getBaudrate()));
            tsiLedDisplay.setChecked(PoslabAgent.isEnabled());

            togglePrinter.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    SerialManager.getPrinterPort(), GPrinterAgent.getBaudrate()));
            togglePrinter.setChecked(GPrinterAgent.isEnabled());

            toggleAhscale.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    AHScaleAgent.getPort(),
                    AHScaleAgent.getBaudrate()));
            toggleAhscale.setChecked(AHScaleAgent.isEnabled());

            toggleSmscale.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    SMScaleAgent.getPort(), SMScaleAgent.getBaudrate()));
            toggleSmscale.setChecked(SMScaleAgent.isEnabled());

            umsipsRs232SettingsItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    SerialManager.getUmsipsPort(), SerialManager.getUmsipsBaudrate()));
            toggleSmscaleFtp.setSubTitle(String.format("%s:%d",
                    SMScaleSyncManager2.FTP_HOST, SMScaleSyncManager2.FTP_PORT));
            toggleSmscaleFtp.setChecked(SharedPreferencesUltimate
                    .getBoolean(SharedPreferencesUltimate.PK_B_SYNC_SMSCALE_FTP_ENABLED, false));
            toggleGreenTags.setSubTitle(GreenTagsApi.URL);
            toggleGreenTags.setChecked(SharedPreferencesUltimate
                    .getBoolean(SharedPreferencesUltimate.PK_B_SYNC_ESL_ENABLED, false));

            tsiSoftKeyboard.setChecked(SharedPrefesManagerFactory.isSoftInputEnabled());
            ttsToggleItem.setChecked(SharedPrefesManagerFactory.isTtsEnabled());

            boolean isSuperPermissionGranted = SharedPrefesManagerFactory.isSuperPermissionGranted();
            if (isSuperPermissionGranted){
                togglePrinter.setVisibility(View.VISIBLE);
                toggleAhscale.setVisibility(View.VISIBLE);
                toggleSmscale.setVisibility(View.VISIBLE);
                toggleSuperPermission.setVisibility(View.VISIBLE);
            }
            else{
                togglePrinter.setVisibility(View.GONE);
                toggleAhscale.setVisibility(View.GONE);
                toggleSmscale.setVisibility(View.GONE);
                toggleSuperPermission.setVisibility(View.GONE);
            }
            toggleSuperPermission.setChecked(isSuperPermissionGranted);

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

    @OnClick(R.id.button_test)
    public void test(){
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and status = '%d'",
                MfhLoginService.get().getSpid(), BizType.POS, PosOrderEntity.ORDER_STATUS_FINISH);
        List<PosOrderEntity> entities =  PosOrderService.get().queryAllDesc(sqlOrder, null);
        if (entities != null && entities.size() > 0){
            PrintManager.printPosOrder(entities.get(0), true);
        }
        PrintManagerImpl.printTest();
    }

}

package com.mfh.litecashier.ui.fragment.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.hardware.scale.AHScaleAgent;
import com.bingshanguxue.cashier.hardware.scale.SMScaleAgent;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.manfenjiayuan.business.presenter.PosRegisterPresenter;
import com.manfenjiayuan.business.view.IPosRegisterView;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.event.SerialPortEvent;
import com.mfh.litecashier.hardware.SMScale.FileZillaDialog;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.service.CloudSyncManager;
import com.mfh.litecashier.ui.dialog.SetPortDialog;
import com.mfh.litecashier.ui.dialog.UmsipsDialog;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.GlobalInstance;
import com.mfh.litecashier.utils.SharedPreferencesHelper;
import com.tencent.bugly.beta.Beta;

import org.century.GreenTagsApi;
import org.century.GreenTagsSettingsDialog;

import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 设置－－通用
 * Created by kun on 15/8/31.
 */
public class SettingsCommonFragment extends BaseFragment implements IPosRegisterView {

    @Bind(R.id.item_posgoods)
    SettingsItem itemPosGoods;
    @Bind(R.id.item_mixi_fresh)
    SettingsItem itemMixiFresh;
    @Bind(R.id.item_terminal)
    SettingsItem terminalSettingsItem;
    @Bind(R.id.item_version)
    SettingsItem versonSettingsItem;
    @Bind(R.id.item_momory)
    SettingsItem memorySettingsItem;

    @Bind(R.id.toggleItem_leddisplay)
    ToggleSettingItem tsiLedDisplay;
    @Bind(R.id.toggleItem_printer)
    ToggleSettingItem printerToggleItem;
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


    private SetPortDialog setPortDialog = null;
    private FileZillaDialog mFileZillaDialog = null;
    private UmsipsDialog mUmsipsDialog = null;
    private GreenTagsSettingsDialog mGreenTagsSettingsDialog = null;
    private PosRegisterPresenter mPosRegisterPresenter;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_common;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        mPosRegisterPresenter = new PosRegisterPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        tsiLedDisplay.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_INIT, ""));
            }
        });
        printerToggleItem.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER_INIT, ""));
            }
        });
        tsiSoftKeyboard.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesManager.setSoftKeyboardEnabled(isChecked);
            }
        });
        ttsToggleItem.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesManager.set(SharedPreferencesManager.PREF_NAME_CONFIG,
                        SharedPreferencesManager.PK_B_TTS_ENABLED, isChecked);
            }
        });
        toggleSmscaleFtp.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_B_SYNC_SMSCALE_FTP_ENABLED, isChecked);
            }
        });
        toggleGreenTags.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_B_SYNC_ESL_ENABLED, isChecked);
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
        refresh();
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
                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SKU_UPDATE_UNREADNUMBER, 99);
                        SharedPreferencesHelper.setSyncProductsCursor("");
                        SharedPreferencesHelper.setPosSkuLastUpdate("");

                        EventBus.getDefault().post(
                                new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_SKU));
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 米西生鲜
     */
    @OnClick(R.id.item_mixi_fresh)
    public void importFromChainSku() {
        showConfirmDialog("选择全量更新同步米西生鲜的商品库",
                "全量更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        //清空米西生鲜商品档案同步游标
                        String cursorKey1 = String.format("%s_%d_%s",
                                SharedPreferencesHelper.PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR,
                                135799L, String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRESH));
                        SharedPreferencesHelper.set(cursorKey1, "");
                        CloudSyncManager.get().importFromChainSku();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

    }

    /**
     * 注册设备
     */
    @OnClick(R.id.item_terminal)
    public void registerPlat() {
        GlobalInstance.getInstance().registerPos(getActivity());
    }

    @OnClick(R.id.item_version)
    public void checkUpdate() {
        Beta.checkUpgrade();
    }

    @OnClick(R.id.toggleItem_leddisplay)
    public void setLedPort() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.initialize("LED客显", SerialManager.getLedPort(),
                SerialManager.getLedBaudrate(), new SetPortDialog.onDialogClickListener() {
                    @Override
                    public void onSetPort(String port, String baudrate) {
                        tsiLedDisplay.setSubTitle(String.format("端口－[%s]，波特率－[%s]", port, baudrate));
                        SerialManager.setLedPort(port);
                        SerialManager.setLedBaudrate(baudrate);

                        //设置串口
                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_INIT, ""));
                        tsiLedDisplay.setChecked(false);

                        refresh();
                    }
                });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    @OnClick(R.id.toggleItem_printer)
    public void setPrinterPort() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.initialize("打印机", SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate(), new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                printerToggleItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]", port, baudrate));
                SerialManager.setPrinterPort(port);
                SerialManager.setPrinterBaudrate(baudrate);

                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER_INIT, ""));
                printerToggleItem.setChecked(false);

                refresh();
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

                        //设置串口
                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_AHSCALE, ""));

                        refresh();
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

                        //设置串口
                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_SMSCALE, ""));

                        refresh();
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
                refresh();
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
                refresh();
            }
        });
        if (!mGreenTagsSettingsDialog.isShowing()) {
            mGreenTagsSettingsDialog.show();
        }
    }

    private void refresh() {
        try {
            itemPosGoods.setSubTitle(SharedPreferencesHelper.getSyncProductsCursor());
            //米西生鲜
            String cursorKey1 = String.format("%s_%d_%s",
                    SharedPreferencesHelper.PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR,
                    135799L, String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRESH));
            itemMixiFresh.setSubTitle(SharedPreferencesHelper.getText(cursorKey1, ""));

            String terminalId = SharedPreferencesManager.getTerminalId();
            terminalSettingsItem.setSubTitle(terminalId);

            AppInfo appInfo = AnalysisAgent.getAppInfo(CashierApp.getAppContext());
            if (appInfo != null) {
                versonSettingsItem.setSubTitle(String.format(Locale.US, "%s - %d",
                        appInfo.getVersionName(), appInfo.getVersionCode()));
            }
            tsiLedDisplay.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    SerialManager.getLedPort(), SerialManager.getLedBaudrate()));
            tsiLedDisplay.setChecked(false);
            printerToggleItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate()));
            printerToggleItem.setChecked(false);

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
            toggleSmscaleFtp.setChecked(SharedPreferencesHelper
                    .getBoolean(SharedPreferencesHelper.PK_B_SYNC_SMSCALE_FTP_ENABLED, false));
            toggleGreenTags.setSubTitle(GreenTagsApi.URL);
            toggleGreenTags.setChecked(SharedPreferencesHelper
                    .getBoolean(SharedPreferencesHelper.PK_B_SYNC_ESL_ENABLED, false));

            tsiSoftKeyboard.setChecked(SharedPreferencesManager.isSoftKeyboardEnabled());
            ttsToggleItem.setChecked(
                    SharedPreferencesManager.getBoolean(SharedPreferencesManager.PREF_NAME_CONFIG,
                            SharedPreferencesManager.PK_B_TTS_ENABLED, true));

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


    @Override
    public void onRegisterPlatProcess() {

    }

    @Override
    public void onRegisterPlatError(String errorMsg) {

    }

    @Override
    public void onRegisterPlatSuccess(String terminalId) {
        SharedPreferencesManager.setTerminalId(terminalId);
        refresh();
        DialogUtil.showHint("注册成功");
    }

    @Override
    public void onPlatUpdate() {
        refresh();
    }

}

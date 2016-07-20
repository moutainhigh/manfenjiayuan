package com.mfh.litecashier.ui.fragment.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.manfenjiayuan.business.presenter.PosRegisterPresenter;
import com.manfenjiayuan.business.view.IPosRegisterView;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.compound.ToggleSettingItem;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.event.SerialPortEvent;
import com.mfh.litecashier.hardware.AHScale.AHScaleAgent;
import com.mfh.litecashier.hardware.SMScale.DigiDS781Agent;
import com.mfh.litecashier.hardware.SMScale.SMScaleSettingsDialog;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager;
import com.mfh.litecashier.service.CloudSyncManager;
import com.mfh.litecashier.ui.dialog.SetPortDialog;
import com.mfh.litecashier.utils.AppHelper;
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
public class SettingsCommonFragment extends BaseFragment implements IPosRegisterView{

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

    @Bind(R.id.item_ahscale_rs232)
    SettingsItem ahscaleToggleItem;
    @Bind(R.id.item_smscale_rs232)
    SettingsItem smscaleRs232SettingsItem;
    @Bind(R.id.item_smscale_ftp)
    SettingsItem smscaleFtpSettingsItem;
    @Bind(R.id.item_greentags_webservice)
    SettingsItem greentagsSettingsItem;


    private SetPortDialog setPortDialog = null;
    private SMScaleSettingsDialog mSMScaleSettingsDialog = null;
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

                        AppHelper.clearCache();
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

                        EventBus.getDefault().post(
                                new AffairEvent(AffairEvent.EVENT_ID_SYNC_DATA_INITIALIZE));
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
    public void registerPlat(){
        if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId())){
            showConfirmDialog("同步商品库到最新版本，同步过程中会先删除历史数据，可能会影响正常收银，确定要同步吗？",
                    "注册", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            mPosRegisterPresenter.register(true);
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        else{
            showConfirmDialog("同步商品库到最新版本，同步过程中会先删除历史数据，可能会影响正常收银，确定要同步吗？",
                    "更新", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            mPosRegisterPresenter.register(true);
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }


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
        setPortDialog.init(SerialManager.getLedPort(), SerialManager.getLedBaudrate(), new SetPortDialog.onDialogClickListener() {
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
        setPortDialog.init(SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate(), new SetPortDialog.onDialogClickListener() {
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
        setPortDialog.init(AHScaleAgent.PORT_ACS_P215, AHScaleAgent.BAUDRATE_ACS_P215, new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                ahscaleToggleItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]", port, baudrate));
                AHScaleAgent.setAcsP215Port(port);
                AHScaleAgent.setAcsP215Baudrate(baudrate);
                AHScaleAgent.initialize();

                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_SCALE_INIT, ""));

                refresh();
            }
        });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    @OnClick(R.id.item_smscale_rs232)
    public void configureSmscaleRs232(){
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.init(DigiDS781Agent.PORT_SCALE_DS781, DigiDS781Agent.BAUDRATE_SCALE_DS781, new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                smscaleRs232SettingsItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]", port, baudrate));
                DigiDS781Agent.setPort(port);
                DigiDS781Agent.setBaudrate(baudrate);
                DigiDS781Agent.initialize();

                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_SCALE_INIT, ""));
//                scalePortSwitchCompat.setChecked(false);

                refresh();
            }
        });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    @OnClick(R.id.item_smscale_ftp)
    public void configureSmscaleFtp(){
        if (mSMScaleSettingsDialog == null) {
            mSMScaleSettingsDialog = new SMScaleSettingsDialog(getActivity());
            mSMScaleSettingsDialog.setCancelable(false);
            mSMScaleSettingsDialog.setCanceledOnTouchOutside(false);
        }
        mSMScaleSettingsDialog.init(new SMScaleSettingsDialog.DialogViewClickListener() {
            @Override
            public void onSubmit() {
                refresh();
            }
        });
        if (!mSMScaleSettingsDialog.isShowing()) {
            mSMScaleSettingsDialog.show();
        }
    }

    @OnClick(R.id.item_greentags_webservice)
    public void showGreenTagsDialog(){
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
            //米西生鲜
            String cursorKey1 = String.format("%s_%d_%s",
                    SharedPreferencesHelper.PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR,
                    135799L, String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRESH));
            itemMixiFresh.setSubTitle(SharedPreferencesHelper.getText(cursorKey1, ""));

            String terminalId = SharedPreferencesManager.getTerminalId();
            terminalSettingsItem.setSubTitle(terminalId);

            versonSettingsItem.setSubTitle(String.format(Locale.US, "%s - %d",
                    CashierApp.getVersionName(), CashierApp.getVersionCode()));
            tsiLedDisplay.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    SerialManager.getLedPort(), SerialManager.getLedBaudrate()));
            tsiLedDisplay.setChecked(false);
            printerToggleItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate()));
            printerToggleItem.setChecked(false);
            ahscaleToggleItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    AHScaleAgent.PORT_ACS_P215, AHScaleAgent.BAUDRATE_ACS_P215));
            smscaleRs232SettingsItem.setSubTitle(String.format("端口－[%s]，波特率－[%s]",
                    DigiDS781Agent.PORT_SCALE_DS781, DigiDS781Agent.BAUDRATE_SCALE_DS781));
            smscaleFtpSettingsItem.setSubTitle(String.format("%s:%d",
                    SMScaleSyncManager.FTP_HOST, SMScaleSyncManager.FTP_PORT));
            greentagsSettingsItem.setSubTitle(GreenTagsApi.URL);
            tsiSoftKeyboard.setChecked(SharedPreferencesManager.isSoftKeyboardEnabled());

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

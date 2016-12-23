package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.hardware.led.PoslabAgent;
import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.PrinterAgent;
import com.bingshanguxue.cashier.hardware.printer.PrinterModel;
import com.bingshanguxue.cashier.hardware.scale.ScaleAgent;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;
import com.mfh.litecashier.R;
import com.mfh.litecashier.hardware.SMScale.FileZillaDialog;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.ui.dialog.UmsipsDialog;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import org.century.GreenTagsApi;
import org.century.GreenTagsSettingsDialog;
import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 设置－－通用
 * Created by kun on 15/8/31.
 */
public class PeripheralFragment extends BaseFragment {

    @BindView(R.id.toggleItem_printer)
    ToggleSettingItem togglePrinter;
    @BindView(R.id.item_scale_rs232)
    ToggleSettingItem toggleScale;
    @BindView(R.id.item_umsips_rs232)
    SettingsItem umsipsRs232SettingsItem;
    @BindView(R.id.toggleItem_leddisplay)
    ToggleSettingItem tsiLedDisplay;
    @BindView(R.id.item_smscale_ftp)
    ToggleSettingItem toggleSmscaleFtp;
    @BindView(R.id.item_greentags_webservice)
    ToggleSettingItem toggleGreenTags;


    private CommonDialog selectScaleDialog = null;
    private CommonDialog selectPrinterDialog = null;

    private FileZillaDialog mFileZillaDialog = null;
    private UmsipsDialog mUmsipsDialog = null;
    private GreenTagsSettingsDialog mGreenTagsSettingsDialog = null;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_peripheral;
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
                if (PoslabAgent.isEnabled() != isChecked) {
                    PoslabAgent.setEnabled(isChecked);

                    EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_INIT, ""));
                }
            }
        });
        togglePrinter.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                if (PrinterAgent.isEnabled() != isChecked) {
                    PrinterAgent.setEnabled(isChecked);
                    EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_GPRINTER, ""));
                }
            }
        });
        toggleScale.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                if (ScaleAgent.isEnabled() != isChecked) {
                    ScaleAgent.setEnabled(isChecked);
                    EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_SCALE, ""));
                }

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

        reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }


    @OnClick(R.id.toggleItem_printer)
    public void selectPrinter(){
        if (selectPrinterDialog == null){
            selectPrinterDialog = DialogHelper.getPinterestDialogCancelable(getActivity());

            View.OnClickListener click = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    selectPrinterDialog.dismiss();

                    int printerType = PrinterAgent.getPrinterType();
                    switch (id) {
                        case R.id.tv_option_1:
                            if (printerType != PrinterModel.PRINTER_TYPE_COMMON){
                                PrinterAgent.setPrinterType(PrinterModel.PRINTER_TYPE_COMMON);
                                togglePrinter.setSubTitle(PrinterAgent.getPrinterName());
                                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_GPRINTER, ""));
                            }
                            break;
                        case R.id.tv_option_2:
                            if (printerType != PrinterModel.PRINTER_TYPE_EMBEDED){
                                PrinterAgent.setPrinterType(PrinterModel.PRINTER_TYPE_EMBEDED);
                                togglePrinter.setSubTitle(PrinterAgent.getPrinterName());
                                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_GPRINTER, ""));
                            }
                            break;
                        default:
                            break;
                    }
                }
            };

            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.dialogview_selectprinter, null);
            view.findViewById(R.id.tv_option_1).setOnClickListener(click);
            view.findViewById(R.id.tv_option_2).setOnClickListener(click);

            selectPrinterDialog.setContent(view);
        }

        if (!selectPrinterDialog.isShowing()){
            selectPrinterDialog.show();
        }
    }
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

    @OnClick(R.id.item_scale_rs232)
    public void selectScale(){
        if (selectScaleDialog == null){
            selectScaleDialog = DialogHelper.getPinterestDialogCancelable(getActivity());

            View.OnClickListener click = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    selectScaleDialog.dismiss();

                    int scaleType = ScaleAgent.getScaleType();
                    switch (id) {
                        case R.id.tv_option_1:
                            if (scaleType != ScaleAgent.SCALE_TYPE_ACS_P215){
                                ScaleAgent.setScaleType(ScaleAgent.SCALE_TYPE_ACS_P215);
                                toggleScale.setSubTitle(ScaleAgent.getScaleName());
                                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_SCALE, ""));
                            }
                            break;
                        case R.id.tv_option_2:
                            if (scaleType != ScaleAgent.SCALE_TYPE_DS_781A){
                                ScaleAgent.setScaleType(ScaleAgent.SCALE_TYPE_DS_781A);
                                toggleScale.setSubTitle(ScaleAgent.getScaleName());
                                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.UPDATE_PORT_SCALE, ""));
                            }
                            break;
                        default:
                            break;
                    }
                }
            };

            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.dialogview_selectscale, null);
            view.findViewById(R.id.tv_option_1).setOnClickListener(click);
            view.findViewById(R.id.tv_option_2).setOnClickListener(click);

            selectScaleDialog.setContent(view);
        }

        if (!selectScaleDialog.isShowing()){
            selectScaleDialog.show();
        }
    }

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
                umsipsRs232SettingsItem.setSubTitle(SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_UMSIPS_TERMID));
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

    @OnClick(R.id.item_receipt)
    public void setReceipt() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ReceiptDialogFragment receiptDialogFragment = new ReceiptDialogFragment();
        receiptDialogFragment.show(fm, "fragment_settings_recept");
    }

    private void reload() {
        try {
            togglePrinter.setSubTitle(PrinterAgent.getPrinterName());
            togglePrinter.setChecked(PrinterAgent.isEnabled());
            toggleScale.setSubTitle(ScaleAgent.getScaleName());
            toggleScale.setChecked(ScaleAgent.isEnabled());
            umsipsRs232SettingsItem.setSubTitle(SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_UMSIPS_TERMID));
            tsiLedDisplay.setChecked(PoslabAgent.isEnabled());

            toggleSmscaleFtp.setSubTitle(String.format("%s:%d",
                    SMScaleSyncManager2.FTP_HOST, SMScaleSyncManager2.FTP_PORT));
            toggleSmscaleFtp.setChecked(SharedPreferencesUltimate
                    .getBoolean(SharedPreferencesUltimate.PK_B_SYNC_SMSCALE_FTP_ENABLED, false));
            toggleGreenTags.setSubTitle(GreenTagsApi.URL);
            toggleGreenTags.setChecked(SharedPreferencesUltimate
                    .getBoolean(SharedPreferencesUltimate.PK_B_SYNC_ESL_ENABLED, false));

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

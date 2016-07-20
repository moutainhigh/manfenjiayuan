package com.mfh.litecashier.ui.fragment.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.impl.MfhApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.hardware.SMScale.SMScaleSettingsDialog;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager;
import com.mfh.litecashier.com.SMScaleDigiAgent;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.event.SerialPortEvent;
import com.mfh.litecashier.ui.dialog.SetPortDialog;
import com.mfh.litecashier.utils.AppHelper;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import org.century.GreenTagsApi;
import org.century.GreenTagsSettingsDialog;

import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 设置－－通用
 * Created by kun on 15/8/31.
 */
public class SettingsCommonFragment extends BaseFragment {
    @Bind(R.id.tv_terminal)
    TextView tvTerminal;
    @Bind(R.id.button_register_terminal)
    Button btnRegisterTerminal;
    @Bind(R.id.tv_version)
    TextView tvVersion;
    @Bind(R.id.tv_memory)
    TextView tvMemory;

    @Bind(R.id.tv_led_port)
    TextView tvLedPort;
    @Bind(R.id.button_set_led_port)
    Button btnSetLedPort;
    @Bind(R.id.switchCompat_led_port)
    SwitchCompat ledPortSwitchCompat;

    @Bind(R.id.tv_printer_port)
    TextView tvPrinterPort;
    @Bind(R.id.button_set_printer_port)
    Button btnSetPrinterPort;
    @Bind(R.id.switchCompat_printer_port)
    SwitchCompat printerPortSwitchCompat;

    @Bind(R.id.tv_scale_port)
    TextView tvScalePort;
    @Bind(R.id.button_set_scale_port)
    Button btnSetScalePort;
    @Bind(R.id.switchCompat_scale_port)
    SwitchCompat scalePortSwitchCompat;


    @Bind(R.id.switchCompat_softkeyboard)
    SwitchCompat scSoftkeyboard;

    @Bind(R.id.tv_smscale_rs232)
    TextView tvSmscaleRs232;
    @Bind(R.id.tv_smscale_ftp)
    TextView tvSmscaleFtp;
    @Bind(R.id.tv_greentags_webservice)
    TextView tvGreentagsWebservice;


    private SetPortDialog setPortDialog = null;
    private SMScaleSettingsDialog mSMScaleSettingsDialog = null;
    private GreenTagsSettingsDialog mGreenTagsSettingsDialog = null;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_common;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        ledPortSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_INIT, ""));
            }
        });
//        printerPortSwitchCompat.setChecked(SharedPreferencesHelper.isSyncOrderEnabled());
        printerPortSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER_INIT, ""));
            }
        });
        scalePortSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_SCALE_INIT, ""));
            }
        });

        scSoftkeyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesManager.setSoftKeyboardEnabled(isChecked);
            }
        });

        refresh();
    }

    /**
     * 注册设备
     */
    @OnClick(R.id.button_register_terminal)
    protected void registerTerminal() {
        btnRegisterTerminal.setEnabled(false);
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnRegisterTerminal.setEnabled(true);
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                            ZLogger.d("初始化－－获取设备号成功:" + retStr);
                            SharedPreferencesManager.setTerminalId(retStr);
                        }
                        refresh();
                        DialogUtil.showHint("注册成功");
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d(String.format("初始化－－获取设备号失败(%s),请在设置中手动激活", errMsg));
                        DialogUtil.showHint(errMsg);
                        btnRegisterTerminal.setEnabled(true);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        JSONObject order = new JSONObject();
        order.put("serialNo", CashierApp.getWifiMac15Bit());

        ZLogger.d("初始化－－注册设备," + order.toJSONString());
        MfhApiImpl.posRegisterCreate(order.toJSONString(), responseCallback);
    }

    /**
     * 清空缓存
     */
    @OnClick(R.id.button_clear_cache)
    public void clearCache() {
        showConfirmDialog(R.string.dialog_message_clean_cache,
                R.string.dialog_button_clean, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        AppHelper.clearCache();
                    }
                }, R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 商品库恢复出厂设置
     */
    @OnClick(R.id.button_sync_goods)
    public void syncGoods() {
        showConfirmDialog("同步商品库到最新版本，同步过程中会先删除历史数据，可能会影响正常收银，确定要同步吗？",
                "同步", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        EventBus.getDefault().post(
                                new AffairEvent(AffairEvent.EVENT_ID_SYNC_DATA_INITIALIZE));
                    }
                }, getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

    }

    @OnClick(R.id.tv_version)
    public void checkUpdate() {
        //无论网络状况是否Wifi，无论用户是否忽略过该版本的更新，都会发起更新检查，代替update(Context context)：
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case UpdateStatus.Yes: // has update
                        UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                        break;
                    case UpdateStatus.No: // has no update
                        DialogUtil.showHint("已经是最新版本");
                        break;
                    case UpdateStatus.NoneWifi: // none wifi
                        DialogUtil.showHint("没有wifi连接， 只在wifi下更新");
                        break;
                    case UpdateStatus.Timeout: // time out
                        DialogUtil.showHint("请求超时");
                        break;
                }
            }
        });
        UmengUpdateAgent.forceUpdate(CashierApp.getAppContext());
    }


    @OnClick(R.id.button_set_led_port)
    public void setLedPort() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.init(SerialManager.getLedPort(), SerialManager.getLedBaudrate(), new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                tvLedPort.setText(String.format("客显: 端口－[%s]，波特率－[%s]", port, baudrate));
                SerialManager.setLedPort(port);
                SerialManager.setLedBaudrate(baudrate);

                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_INIT, ""));
                ledPortSwitchCompat.setChecked(false);
            }
        });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    @OnClick(R.id.button_set_printer_port)
    public void setPrinterPort() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.init(SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate(), new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                tvPrinterPort.setText(String.format("打印机: 端口－[%s]，波特率－[%s]", port, baudrate));
                SerialManager.setPrinterPort(port);
                SerialManager.setPrinterBaudrate(baudrate);

                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER_INIT, ""));
                printerPortSwitchCompat.setChecked(false);
            }
        });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    /**
     * 打印测试
     */
    @OnClick(R.id.button_print_testdata)
    public void printTestData() {
        PosOrderEntity orderEntity = new PosOrderEntity();
        orderEntity.setBarCode("0123456789");
        orderEntity.setCreatedDate(new Date());
        orderEntity.setUpdatedDate(new Date());
//        posOrderEntity.setHumanId(MfhLoginService.get().getCurrentGuId());
        orderEntity.setRemark("");
//        posOrderEntity.setCompanyId(MfhLoginService.get().getSpid());// 使用商品的ternantID,见下面
        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_NONE);
        orderEntity.setSellOffice(MfhLoginService.get().getCurOfficeId());
        orderEntity.setSellerId(MfhLoginService.get().getSpid());// 需要登录
        orderEntity.setPosId(SharedPreferencesManager.getTerminalId());//设备编号//TODO,imei+mac,后面再修改。

        //TODO,会员卡
        orderEntity.setHumanId(null);
        orderEntity.setScore(0D);//会员积分

        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_INIT);
        orderEntity.setPaystatus(PosOrderEntity.PAY_STATUS_NO);
        orderEntity.setPaidMoney(0D);
        orderEntity.setCharge(0D);
        orderEntity.setDiscountAmount(0D);
        orderEntity.setCouponsIds("");
        orderEntity.setAdjPrice("");

        SerialManager.printTestData();
    }

    @OnClick(R.id.button_set_scale_port)
    public void setScalePort() {
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.init(SerialManager.getScalePort(), SerialManager.getScaleBaudrate(), new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                tvScalePort.setText(String.format("电子秤: 端口－[%s]，波特率－[%s]", port, baudrate));
                SerialManager.setScalePort(port);
                SerialManager.setScaleBaudrate(baudrate);

                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_SCALE_INIT, ""));
                scalePortSwitchCompat.setChecked(false);
            }
        });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    @OnClick(R.id.tv_smscale_rs232)
    public void configureSmscaleRs232(){
        if (setPortDialog == null) {
            setPortDialog = new SetPortDialog(getActivity());
            setPortDialog.setCancelable(false);
            setPortDialog.setCanceledOnTouchOutside(false);
        }
        setPortDialog.init(SerialManager.getScalePort(), SerialManager.getScaleBaudrate(), new SetPortDialog.onDialogClickListener() {
            @Override
            public void onSetPort(String port, String baudrate) {
                tvSmscaleRs232.setText(String.format("电子秤: 端口－[%s]，波特率－[%s]", port, baudrate));
                SMScaleDigiAgent.setPort(port);
                SMScaleDigiAgent.setBaudrate(baudrate);
                SMScaleDigiAgent.initialize();

                //设置串口
                EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_SCALE_INIT, ""));
//                scalePortSwitchCompat.setChecked(false);
            }
        });
        if (!setPortDialog.isShowing()) {
            setPortDialog.show();
        }
    }

    @OnClick(R.id.tv_smscale_ftp)
    public void configureSmscaleFtp(){
        if (mSMScaleSettingsDialog == null) {
            mSMScaleSettingsDialog = new SMScaleSettingsDialog(getActivity());
            mSMScaleSettingsDialog.setCancelable(false);
            mSMScaleSettingsDialog.setCanceledOnTouchOutside(false);
        }
        mSMScaleSettingsDialog.refresh();
        if (!mSMScaleSettingsDialog.isShowing()) {
            mSMScaleSettingsDialog.show();
        }
    }

    @OnClick(R.id.tv_greentags_webservice)
    public void showGreenTagsDialog(){
        if (mGreenTagsSettingsDialog == null) {
            mGreenTagsSettingsDialog = new GreenTagsSettingsDialog(getActivity());
            mGreenTagsSettingsDialog.setCancelable(false);
            mGreenTagsSettingsDialog.setCanceledOnTouchOutside(false);
        }
        mGreenTagsSettingsDialog.refresh();
        if (!mGreenTagsSettingsDialog.isShowing()) {
            mGreenTagsSettingsDialog.show();
        }
    }

    private void refresh() {
        try {
            String terminalId = SharedPreferencesManager.getTerminalId();
            btnRegisterTerminal.setEnabled(StringUtils.isEmpty(terminalId));
            tvTerminal.setText(String.format("设备编号：%s", terminalId));

            tvVersion.setText(String.format(Locale.US, "当前版本：%s - %d",
                    CashierApp.getVersionName(), CashierApp.getVersionCode()));
            tvLedPort.setText(String.format("客显：端口－[%s]，波特率－[%s]",
                    SerialManager.getLedPort(), SerialManager.getLedBaudrate()));
            tvPrinterPort.setText(String.format("打印机：端口－[%s]，波特率－[%s]",
                    SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate()));
            tvScalePort.setText(String.format("电子秤：端口－[%s]，波特率－[%s]",
                    SerialManager.getScalePort(), SerialManager.getScaleBaudrate()));
            //TODO
            ledPortSwitchCompat.setChecked(false);
            printerPortSwitchCompat.setChecked(false);
            scalePortSwitchCompat.setChecked(false);
            tvSmscaleRs232.setText(String.format("寺冈电子秤: 端口－[%s]，波特率－[%s]",
                    SMScaleDigiAgent.PORT_SCALE_DS781, SMScaleDigiAgent.BAUDRATE_SCALE_DS781));
            tvSmscaleFtp.setText(String.format("寺冈电子秤: %s",
                    String.format("%s:%d", SMScaleSyncManager.FTP_HOST, SMScaleSyncManager.FTP_PORT)));
            tvGreentagsWebservice.setText(String.format("绿泰电子价签: %s", GreenTagsApi.URL));

            //        ActivityManager am =  (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            //        ACache cache = ACache.get(CashierApp.getAppContext(), Constants.CACHE_NAME);
//        sbMem.append(String.format("系统总内存:%d\n", memoryInfo.totalMem));
//        sbMem.append(String.format("系统可用内存:%d\n", memoryInfo.availMem));
//        sbMem.append(String.format("低内存阀值:%d\n", memoryInfo.threshold));
//        sbMem.append(String.format("是否处于低内存:%b\n", memoryInfo.lowMemory));
            tvMemory.setText("内存:\n");
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }


}

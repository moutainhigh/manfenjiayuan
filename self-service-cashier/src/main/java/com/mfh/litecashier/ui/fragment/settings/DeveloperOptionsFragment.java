package com.mfh.litecashier.ui.fragment.settings;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.anlaysis.remoteControl.RemoteControlClient;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 设置－－开发者选项
 * Created by kun on 15/8/31.
 */
public class DeveloperOptionsFragment extends BaseFragment {
    @BindView(R.id.toggle_superPermission)
    ToggleSettingItem toggleSuperPermission;
    @BindView(R.id.item_factoryreset)
    SettingsItem itemFactoryReset;
    @BindView(R.id.toggleItem_release)
    ToggleSettingItem toggleItemRelease;
    @BindView(R.id.toggleItem_getui)
    ToggleSettingItem toggleGettui;
    @BindView(R.id.tv_display)
    TextView tvDisplay;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_developer_options;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        toggleSuperPermission.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                boolean isSuperPermissionGranted = SharedPrefesManagerFactory.isSuperPermissionGranted();
                if (isSuperPermissionGranted != isChecked) {
                    SharedPrefesManagerFactory.setSuperPermissionGranted(isChecked);
//                    ZLogger.LOG_ENABLED = SharedPrefesManagerFactory.isSuperPermissionGranted();

                    EventBus.getDefault().post(new SettingsFragment.SettingsEvent(
                            SettingsFragment.SettingsEvent.EVENT_ID_RELOAD_DATA));
                }
            }
        });
        toggleItemRelease.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                if (isChecked) {
                    toggleItemRelease.setSubTitle("正式发布");
                } else {
                    toggleItemRelease.setSubTitle("开发测试");
                }

                if (SharedPrefesManagerFactory.isReleaseVersion() != isChecked) {
                    SharedPrefesManagerFactory.setReleaseVersion(isChecked);
                    showConfirmDialog("需要重启应用才能生效？",
                            "立即重启", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    AppHelper.restartApp(CashierApp.getAppContext());
                                }
                            }, "暂不重启", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                }
            }
        });

        toggleGettui.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                if (PushManager.getInstance().isPushTurnedOn(CashierApp.getAppContext()) != isChecked) {
//                    if (isChecked) {
//                    ZLogger.df("初始化推送服务");
//                        PushManager.getInstance().initialize(CashierApp.getAppContext());
//                    } else {
//                    ZLogger.df("完全终止SDK的服务");
//                        PushManager.getInstance().stopService(CashierApp.getAppContext());
//                    }
                    if (isChecked) {
                        ZLogger.df("开启Push推送");
                        //        优先级高于stopService，如果当前是stopService状态，调用turnOnPush之后仍然可以正常推送。
                        PushManager.getInstance().turnOnPush(CashierApp.getAppContext());
                    } else {
                        ZLogger.df("关闭Push推送");
                        PushManager.getInstance().turnOffPush(CashierApp.getAppContext());
                    }
                }
            }
        });

        refresh();
    }

    /**
     * 刷新页面信息
     */
    private void refresh() {
        toggleSuperPermission.setChecked(SharedPrefesManagerFactory.isSuperPermissionGranted());

        if (SharedPrefesManagerFactory.isReleaseVersion()) {
            toggleItemRelease.setChecked(true);
            toggleItemRelease.setSubTitle("正式发布");
        } else {
            toggleItemRelease.setChecked(false);
            toggleItemRelease.setSubTitle("开发测试");
        }
   Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DisplayMetrics: %d*%d %f%navigation_bar_height:%d\n",
                resources.getDisplayMetrics().widthPixels,
                this.getResources().getDisplayMetrics().heightPixels,
                this.getResources().getDisplayMetrics().density,
                resources.getDimensionPixelSize(resourceId)));
//        sb.append(String.format("buildTime: %s", BuildConfig.S))
        tvDisplay.setText(sb.toString());
        //获取当前SDK的服务状态
//        pushServiceSwitchCompact.setChecked(PushManager.getInstance().isPushTurnedOn(CashierApp.getAppContext()));

        toggleGettui.setChecked(PushManager.getInstance().isPushTurnedOn(CashierApp.getAppContext()));
        toggleGettui.setSubTitle(String.format("%s-%s",
                PushManager.getInstance().getClientid(CashierApp.getAppContext()),
                IMConfig.getPushClientId()));
//        toggleGettui.setSubTitle(PushManager.getInstance().getClientid(CashierApp.getAppContext()));

        //获取当前SDK的服务状态
//        pushServiceSwitchCompact.setChecked(PushManager.getInstance().isPushTurnedOn(CashierApp.getAppContext()));
    }

    /**
     * 恢复出厂设置
     */
    @OnClick(R.id.item_factoryreset)
    public void resetFactoryData() {
        showConfirmDialog("此操作会清楚您设备中的所有数据，确定要恢复出厂设置吗？\n(恢复出厂设置后会自动重启)",
                "恢复出厂设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        AppHelper.resetFactoryData(CashierApp.getAppContext());
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    @OnClick(R.id.button_register_msgbridge)
    public void registerMsgBridge() {
        IMClient.getInstance().registerBridge();
    }

    @OnClick(R.id.button_upload_log)
    public void uploadLog() {
        RemoteControlClient.getInstance().uploadLogFileStep1();
    }

    @OnClick(R.id.button_upload_crash)
    public void uploadCrash() {
        RemoteControlClient.getInstance().uploadCrashFileStep1();
    }

    @OnClick(R.id.button_test)
    public void test() {
        String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' and status = '%d'",
                MfhLoginService.get().getSpid(), BizType.POS, PosOrderEntity.ORDER_STATUS_FINISH);
        List<PosOrderEntity> entities = PosOrderService.get().queryAllDesc(sqlOrder, null);
        if (entities != null && entities.size() > 0) {
            PrinterFactory.getPrinterManager().printPosOrder(entities.get(0));
        }
        PrinterFactory.getPrinterManager().printTestPage();
    }
}

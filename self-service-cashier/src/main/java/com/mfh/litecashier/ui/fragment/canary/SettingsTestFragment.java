package com.mfh.litecashier.ui.fragment.canary;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.anlaysis.remoteControl.RemoteControlClient;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 设置－－通用
 * Created by kun on 15/8/31.
 */
public class SettingsTestFragment extends BaseFragment {


    @Bind(R.id.item_factoryreset)
    SettingsItem itemFactoryReset;
    @Bind(R.id.toggleItem_release)
    ToggleSettingItem toggleItemRelease;
    @Bind(R.id.toggleItem_customer_screen)
    ToggleSettingItem toggleItemCustomerScreen;
    @Bind(R.id.toggleItem_hybrid_payment)
    ToggleSettingItem toggleHybridPayment;
    @Bind(R.id.toggleItem_getui)
    ToggleSettingItem toggleGettui;

    @Bind(R.id.tv_display)
    TextView tvDisplay;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_test;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        toggleItemRelease.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                if (isChecked) {
                    toggleItemRelease.setSubTitle("正式发布");
                } else {
                    toggleItemRelease.setSubTitle("开发测试");
                }

                if (SharedPreferencesManager.isReleaseVersion() != isChecked) {
                    SharedPreferencesManager.setReleaseVersion(isChecked);
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
        toggleItemCustomerScreen.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED, isChecked);
            }
        });
        toggleHybridPayment.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean isChecked) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, isChecked);
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
        if (SharedPreferencesManager.isReleaseVersion()) {
            toggleItemRelease.setChecked(true);
            toggleItemRelease.setSubTitle("正式发布");
        } else {
            toggleItemRelease.setChecked(false);
            toggleItemRelease.setSubTitle("开发测试");
        }

        toggleItemCustomerScreen.setChecked(SharedPreferencesHelper
                .getBoolean(SharedPreferencesHelper.PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED, false));

        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        tvDisplay.setText(String.format("DisplayMetrics: %d*%d %f%navigation_bar_height:%d",
                resources.getDisplayMetrics().widthPixels,
                this.getResources().getDisplayMetrics().heightPixels,
                this.getResources().getDisplayMetrics().density,
                resources.getDimensionPixelSize(resourceId)));
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
    public void uploadLog(){
        RemoteControlClient.getInstance().uploadLogFileStep1();
    }
    @OnClick(R.id.button_upload_crash)
    public void uploadCrash(){
        RemoteControlClient.getInstance().uploadCrashFileStep1();
    }
}

package com.mfh.litecashier.ui.fragment.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 设置－－通用
 * Created by kun on 15/8/31.
 */
public class SettingsTestFragment extends BaseFragment {
    @Bind(R.id.tv_release)
    TextView tvRelease;
    @Bind(R.id.switchCompat_release)
    SwitchCompat scRelease;
    @Bind(R.id.tv_display)
    TextView tvDisplay;
    @Bind(R.id.tv_getui)
    TextView tvGeTui;
    @Bind(R.id.switchCompat_push)
    SwitchCompat pushSwitchCompact;
    @Bind(R.id.switchCompat_pad_display)
    SwitchCompat scPadDisplay;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_test;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        scRelease.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferencesManager.setReleaseVersion(true);
                    tvRelease.setText("正式发布");
                } else {
                    SharedPreferencesManager.setReleaseVersion(false);
                    tvRelease.setText("开发测试");
                }
//                DialogUtil.showHint("需要重启才会生效");
//                setReleaseOrDebug();
            }
        });

        pushSwitchCompact.setChecked(true);
        pushSwitchCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //初始化推送服务
                    PushManager.getInstance().initialize(CashierApp.getAppContext());
                } else {
                    //完全终止SDK的服务
                    PushManager.getInstance().stopService(CashierApp.getAppContext());
                }
//                if (isChecked) {
//                    //开启Push推送
//                    //        优先级高于stopService，如果当前是stopService状态，调用turnOnPush之后仍然可以正常推送。
//                    PushManager.getInstance().turnOnPush(CashierApp.getAppContext());
//                } else {
//                    //关闭Push推送
//                    PushManager.getInstance().turnOffPush(CashierApp.getAppContext());
//                }
            }
        });
        scPadDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferencesHelper.set(SharedPreferencesHelper.PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED, true);
                } else {
                    SharedPreferencesHelper.set(SharedPreferencesHelper.PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED, false);
                }
//                DialogUtil.showHint("需要重启才会生效");
//                setReleaseOrDebug();
            }
        });

        refresh();
    }

    private void refresh(){
        if(SharedPreferencesManager.isReleaseVersion()){
            scRelease.setChecked(true);
            tvRelease.setText("正式发布");
        }
        else{
            scRelease.setChecked(false);
            tvRelease.setText("开发测试");
        }

        scPadDisplay.setChecked(SharedPreferencesHelper
                .getBoolean(SharedPreferencesHelper.PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED, false));

        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        tvDisplay.setText(String.format("DisplayMetrics: %d*%d %f%navigation_bar_height:%d", resources.getDisplayMetrics().widthPixels,
                this.getResources().getDisplayMetrics().heightPixels, this.getResources().getDisplayMetrics().density,
                resources.getDimensionPixelSize(resourceId)));
        //获取当前SDK的服务状态
//        pushServiceSwitchCompact.setChecked(PushManager.getInstance().isPushTurnedOn(CashierApp.getAppContext()));


        StringBuilder sbGeTui = new StringBuilder();
        sbGeTui.append("个推:");
        if (PushManager.getInstance().isPushTurnedOn(CashierApp.getAppContext())){
            sbGeTui.append("ON\n");
        }else{
            sbGeTui.append("OFF\n");
        }

        sbGeTui.append(String.format("当前sdk版本为:%s\n", PushManager.getInstance().getVersion(CashierApp.getAppContext())));
        //获取当前ClientID
        sbGeTui.append(String.format("当前应用的cid为:%s\n", PushManager.getInstance().getClientid(CashierApp.getAppContext())));
        tvGeTui.setText(sbGeTui.toString());

        //获取当前SDK的服务状态
//        pushServiceSwitchCompact.setChecked(PushManager.getInstance().isPushTurnedOn(CashierApp.getAppContext()));
    }

    @OnClick(R.id.button_register_msgbridge)
    public void registerMsgBridge(){
        IMClient.getInstance().registerBridge();
    }
}

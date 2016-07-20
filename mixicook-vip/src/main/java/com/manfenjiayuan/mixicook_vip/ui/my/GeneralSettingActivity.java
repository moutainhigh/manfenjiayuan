package com.manfenjiayuan.mixicook_vip.ui.my;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.compound.ToggleSettingItem;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 设置·通用
 *
 * @author bingshanguxue
 */
public class GeneralSettingActivity extends BaseActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.item_2_0)
    SettingsItem btnItem;
    @Bind(R.id.item_location)
    ToggleSettingItem itemLocation;
    @Bind(R.id.item_notification)
    ToggleSettingItem itemNotification;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_general;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.topbar_title_settings_general);
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GeneralSettingActivity.this.onBackPressed();
                    }
                });
        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemLocation.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPreferencesManager.setLocationAcceptEnabled(on);
            }
        });
        itemNotification.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPreferencesManager.setNotificationAcceptEnabled(on);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        itemLocation.setChecked(SharedPreferencesManager.getLocationAcceptEnabled());
        itemNotification.setChecked(SharedPreferencesManager.getNotificationAcceptEnabled());
    }

    /**
     * 显示Dialog
     */
    @OnClick(R.id.item_2_0)
    public void showCleanCacheDialog() {
        showConfirmDialog("系统变慢6，您确定要清除缓存吗？",
                "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
    }
}


package com.mfh.buyers.ui.settings;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.buyers.R;
import com.mfh.buyers.utils.UIHelper;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.compound.ToggleSettingItem;


import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 设置·通用
 * @author ZZN
 * */
public class GeneralSettingActivity extends BaseActivity {
    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.item_2_0)
    SettingsItem btnItem;
    @Bind({R.id.item_1_0, R.id.item_1_1})
    List<ToggleSettingItem> switchItems;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_general;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTopBar();

        switchItems.get(0).init(R.string.label_settings_location, ToggleSettingItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP, new ToggleSettingItem.SettingItemLisener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPreferencesManager.setLocationAcceptEnabled(on);
            }
        });
        switchItems.get(1).init(R.string.label_settings_notification, ToggleSettingItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM, new ToggleSettingItem.SettingItemLisener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPreferencesManager.setNotificationAcceptEnabled(on);
            }
        });

        btnItem.init(new SettingsItemData(R.drawable.material_housekeeping, getString(R.string.label_settings_clearcache), getString(R.string.label_settings_clearcache_description)));
        btnItem.setButtonType(SettingsItem.ThemeType.THEME_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_SINGLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
        tvTopBarTitle.setText(R.string.topbar_title_settings_general);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void refresh(){
        switchItems.get(0).setChecked(SharedPreferencesManager.getLocationAcceptEnabled());
        switchItems.get(1).setChecked(SharedPreferencesManager.getNotificationAcceptEnabled());
    }

    /**
     * 显示Dialog
     * */
    @OnClick(R.id.item_2_0)
    public void showCleanCacheDialog() {
        UIHelper.showCleanCacheDialog(this);
    }
}

package com.mfh.owner.ui.settings;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.owner.R;

import java.util.List;

import butterknife.Bind;


/**
 * 个人资料
 * */
public class AccountSettingActivity extends BaseActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind({R.id.item_1_0, R.id.item_1_1, R.id.item_1_2})
    List<SettingsItem> setttingItems;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_account;
    }

    /**
     * 初始化导航栏视图
     * */
    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle(R.string.topbar_title_settings_account);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.actionbar_back_indicator);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set an OnMenuItemClickListener to handle menu item clicks
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_more) {
//                    showSharePopup(toolbar);
//                }
//                return true;
//            }
//        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setttingItems.get(0).init(new SettingsItemData(R.drawable.material_housekeeping, getString(R.string.label_account_name), ""));
        setttingItems.get(0).setButtonType(SettingsItem.ThemeType.THEME_TEXT_TEXT, SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP);

        setttingItems.get(1).init(new SettingsItemData(R.drawable.material_housekeeping, getString(R.string.label_account_change_login_pwd), "  "));
        setttingItems.get(1).setButtonType(SettingsItem.ThemeType.THEME_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_CENTER);
        setttingItems.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingFragmentActivity.actionStart(AccountSettingActivity.this, 1);
            }
        });
        setttingItems.get(2).init(new SettingsItemData(R.drawable.material_housekeeping, getString(R.string.label_account_change_pay_pwd), getString(R.string.label_account_change_pay_pwd_description)));
        setttingItems.get(2).setButtonType(SettingsItem.ThemeType.THEME_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM);
        setttingItems.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingFragmentActivity.actionStart(AccountSettingActivity.this, 2);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }


    private void refresh(){
        setttingItems.get(0).setDetailText(MfhLoginService.get().getLoginName());
    }
}

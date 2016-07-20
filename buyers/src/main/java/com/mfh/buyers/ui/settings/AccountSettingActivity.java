package com.mfh.buyers.ui.settings;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.buyers.R;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.List;

import butterknife.Bind;


/**
 * 个人资料
 * */
public class AccountSettingActivity extends BaseActivity {
    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;

    @Bind({R.id.item_1_0, R.id.item_1_1, R.id.item_1_2})
    List<SettingsItem> setttingItems;

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings_account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTopBar();

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


    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
        tvTopBarTitle.setText(R.string.topbar_title_settings_account);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void refresh(){
        setttingItems.get(0).setDetailText(MfhLoginService.get().getLoginName());
    }
}

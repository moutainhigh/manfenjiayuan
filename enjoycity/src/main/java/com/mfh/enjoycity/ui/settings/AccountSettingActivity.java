package com.mfh.enjoycity.ui.settings;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.framework.uikit.base.BaseActivity;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.List;

import butterknife.Bind;

/**
 * 个人资料
 */
public class AccountSettingActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    @Bind({R.id.item_1_0, R.id.item_1_1, R.id.item_1_2})
    List<SettingsItem> setttingItems;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_settings_account;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.topbar_title_settings_account);
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AccountSettingActivity.this.onBackPressed();
                    }
                });
        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setttingItems.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingFragmentActivity.actionStart(AccountSettingActivity.this, 1);
            }
        });
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


    private void refresh() {
        setttingItems.get(0).setSubTitle(MfhLoginService.get().getLoginName());
    }
}

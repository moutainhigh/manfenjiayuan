package com.mfh.enjoycity.ui.settings;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.utils.UIHelper;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseActivity;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 设置·通用
 *
 * @author bingshanguxue
 */
public class GeneralSettingActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.item_2_0)
    SettingsItem btnItem;
    @Bind({R.id.item_1_0, R.id.item_1_1})
    List<ToggleSettingItem> switchItems;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_general;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.topbar_title_settings_general);
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
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

        switchItems.get(0).init(new ToggleSettingItem.SettingItemLisener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPrefesManagerFactory.setLocationAcceptEnabled(on);
            }
        });
        switchItems.get(1).init(new ToggleSettingItem.SettingItemLisener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPrefesManagerFactory.setNotificationAcceptEnabled(on);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        switchItems.get(0).setChecked(SharedPrefesManagerFactory.getLocationAcceptEnabled());
        switchItems.get(1).setChecked(SharedPrefesManagerFactory.getNotificationAcceptEnabled());
    }

    /**
     * 显示Dialog
     */
    @OnClick(R.id.item_2_0)
    public void showCleanCacheDialog() {
        UIHelper.showCleanCacheDialog(this);
    }
}


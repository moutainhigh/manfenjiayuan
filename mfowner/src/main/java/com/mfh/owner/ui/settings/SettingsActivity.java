package com.mfh.owner.ui.settings;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.Callback;
import com.mfh.owner.R;
import com.mfh.owner.utils.Constants;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 设置
 * */
public class SettingsActivity extends BaseActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind({ R.id.item_1_0, R.id.item_1_1, R.id.item_1_2, R.id.item_2_0 })
    List<SettingsItem> btnItems;

    private static final String DEFAULT_TELPHONE_NUMBER = "4008866671";

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings;
    }

    /**
     * 初始化导航栏视图
     * */
    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle(R.string.topbar_title_settings);
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

        btnItems.get(0).init(new SettingsItemData(R.drawable.icon_setting_profile, getString(R.string.label_settings_user_profile), ""));
        btnItems.get(0).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP);
        btnItems.get(0).setOnClickListener(myOnClickListener);

        btnItems.get(1).init(new SettingsItemData(R.drawable.icon_settings_account,
                getString(R.string.label_settings_account), ""));
        btnItems.get(1).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_CENTER);
        btnItems.get(1).setOnClickListener(myOnClickListener);

        btnItems.get(2).init(new SettingsItemData(R.drawable.icon_setting_general,
                getString(R.string.label_settings_general), ""));
        btnItems.get(2).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM);
        btnItems.get(2).setOnClickListener(myOnClickListener);

        btnItems.get(3).init(new SettingsItemData(R.drawable.icon_settings_customer_service,
                getString(R.string.label_settings_customer_center), ""));
        btnItems.get(3).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_SINGLE);
        btnItems.get(3).setOnClickListener(myOnClickListener);
    }



    private View.OnClickListener myOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.item_1_0:{
                    UIHelper.startActivity(SettingsActivity.this, UserProfileActivity.class);
                }
                break;
                case R.id.item_1_1:{
                    UIHelper.startActivity(SettingsActivity.this, AccountSettingActivity.class);
                }
                break;
                case R.id.item_1_2:{
                    UIHelper.startActivity(SettingsActivity.this, GeneralSettingActivity.class);
                }
                break;
                case R.id.item_2_0:{
                    UIHelper.callPhone(SettingsActivity.this, DEFAULT_TELPHONE_NUMBER);
                }
                break;
            }
        }
    };

    /**
     * 跳转至登录页面
     * */
    private void redirectToLogin(){
        Intent data = new Intent();
        data.putExtra(Constants.INTENT_KEY_IS_LOGOUT, true);
        setResult(Activity.RESULT_OK, data);

//        Intent intent = new Intent(this, H5AuthActivity.class);
//        intent.putExtra(H5AuthActivity.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_AUTH_GUIDE);
//        intent.putExtra(H5AuthActivity.EXTRA_KEY_AUTH_MODE, 0);
//        startActivity(intent);

        finish();
    }

    /**
     * 显示退出提示框
     * */
    @OnClick(R.id.button_logout)
    public void showLogoutAlert() {
        CommonDialog dialog = new CommonDialog(this);
        dialog.setMessage(R.string.dialog_message_logout);
        dialog.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();

                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 退出当前账号
     * */
    private void logout(){
        MfhUserManager.getInstance().logout(new Callback() {
            @Override
            public void onSuccess() {
                redirectToLogin();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                redirectToLogin();
            }
        });
    }
}

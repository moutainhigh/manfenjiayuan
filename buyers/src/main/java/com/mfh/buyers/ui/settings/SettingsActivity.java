package com.mfh.buyers.ui.settings;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.buyers.utils.UIHelper;
import com.mfh.buyers.R;
import com.mfh.buyers.utils.Constants;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 设置
 * */
public class SettingsActivity extends BaseActivity {
    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind({ R.id.item_1_0, R.id.item_1_1, R.id.item_1_2, R.id.item_2_0 })
    List<SettingsItem> btnItems;

    private static final String DEFAULT_TELPHONE_NUMBER = "4008866671";

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTopBar();

        btnItems.get(0).init(new SettingsItemData(R.drawable.icon_setting_profile, getString(R.string.label_settings_user_profile), ""));
        btnItems.get(0).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP);
        btnItems.get(0).setOnClickListener(myOnClickListener);

        btnItems.get(1).init(new SettingsItemData(R.drawable.icon_settings_account, getString(R.string.label_settings_account), ""));
        btnItems.get(1).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_CENTER);
        btnItems.get(1).setOnClickListener(myOnClickListener);

        btnItems.get(2).init(new SettingsItemData(R.drawable.icon_setting_general, getString(R.string.label_settings_general), ""));
        btnItems.get(2).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM);
        btnItems.get(2).setOnClickListener(myOnClickListener);

        btnItems.get(3).init(new SettingsItemData(R.drawable.icon_settings_customer_service, getString(R.string.label_settings_customer_center), ""));
        btnItems.get(3).setButtonType(SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW, SettingsItem.SeperateLineType.SEPERATE_LINE_SINGLE);
        btnItems.get(3).setOnClickListener(myOnClickListener);
    }

    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
        tvTopBarTitle.setText(R.string.topbar_title_settings);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.item_1_0:{
                    UIHelper.redirectToActivity(SettingsActivity.this, UserProfileActivity.class);
                }
                break;
                case R.id.item_1_1:{
                    UIHelper.redirectToActivity(SettingsActivity.this, AccountSettingActivity.class);
                }
                break;
                case R.id.item_1_2:{
                    UIHelper.redirectToActivity(SettingsActivity.this, GeneralSettingActivity.class);
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
        if(!NetWorkUtil.isConnect(this) || !MfhLoginService.get().haveLogined()){
            redirectToLogin();
            return;
        }

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

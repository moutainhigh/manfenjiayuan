package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.settings.AccountSettingActivity;
import com.mfh.enjoycity.ui.settings.GeneralSettingActivity;
import com.mfh.enjoycity.ui.settings.UserProfileActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.uikit.UIHelper;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 设置
 */
public class SettingsActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.tv_version_name)
    TextView tvVersionName;
    @Bind({R.id.item_profile, R.id.item_account, R.id.item_general, R.id.item_customer_service})
    List<SettingsItem> btnItems;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_settings);
        setSupportActionBar(toolbar);

//        final Drawable backArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
//        if(backArrow!=null){
//            backArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        }
//
//        if(getSupportActionBar()!=null){
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(backArrow);
//        }

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SettingsActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvVersionName.setText(MfhApplication.getVersionName());

        btnItems.get(0).setOnClickListener(myOnClickListener);
        btnItems.get(1).setOnClickListener(myOnClickListener);

        btnItems.get(2).setOnClickListener(myOnClickListener);
        btnItems.get(3).setOnClickListener(myOnClickListener);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_profile: {
                    UIHelper.startActivity(SettingsActivity.this, UserProfileActivity.class);
                }
                break;
                case R.id.item_account: {
                    UIHelper.startActivity(SettingsActivity.this, AccountSettingActivity.class);
                }
                break;
                case R.id.item_general: {
                    UIHelper.startActivity(SettingsActivity.this, GeneralSettingActivity.class);
                }
                break;
                case R.id.item_customer_service: {
                    UIHelper.callPhone(SettingsActivity.this, Constants.DEFAULT_TELPHONE_NUMBER);
                }
                break;
            }
        }
    };


    /**
     * 显示退出提示框
     */
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
     */
    private void logout() {
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

    /**
     * 跳转至登录页面
     */
    private void redirectToLogin() {
        Intent data = new Intent();
        data.putExtra(Constants.INTENT_KEY_IS_LOGOUT, true);
        setResult(Activity.RESULT_OK, data);

//        Intent intent = new Intent(this, H5AuthActivity.class);
//        intent.putExtra(H5AuthActivity.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_AUTH_GUIDE);
//        intent.putExtra(H5AuthActivity.EXTRA_KEY_AUTH_MODE, 0);
//        startActivity(intent);

//        MainActivity.actionStart(SettingsActivity.this, 0);

        finish();
    }
}

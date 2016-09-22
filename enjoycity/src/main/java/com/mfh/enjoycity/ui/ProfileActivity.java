package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.settings.AccountSettingActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.account.UserApi;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.api.constant.Sex;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.UIHelper;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;

import java.util.List;

import butterknife.Bind;


/**
 * 个人信息
 */
public class ProfileActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind({R.id.item_tel, R.id.item_name, R.id.item_sex, R.id.item_logout})
    List<SettingsItem> btnItems;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_profile);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProfileActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolBar();

        btnItems.get(0).setOnClickListener(myOnClickListener);
        btnItems.get(1).setOnClickListener(myOnClickListener);
        btnItems.get(2).setOnClickListener(myOnClickListener);
        btnItems.get(3).setOnClickListener(myOnClickListener);
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_tel: {
                    UIHelper.startActivity(ProfileActivity.this, AboutActivity.class);
                }
                break;
                case R.id.item_name: {
                    UIHelper.startActivity(ProfileActivity.this, AccountSettingActivity.class);
                }
                break;
                case R.id.item_sex: {
                    showUpdateSexDialog();
                }
                break;
                case R.id.item_logout: {
                    showLogoutAlert();
                }
                break;
            }
        }
    };

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

        finish();
    }

    /**
     * 显示退出提示框
     */
    private void showLogoutAlert() {
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

    private void showUpdateSexDialog() {
        final CommonDialog dialog = DialogHelper
                .getPinterestDialogCancelable(this);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                switch (id) {
                    case R.id.tv_male:
                        updateSex("0");
                        break;
                    case R.id.tv_female:
                        updateSex("1");
                        break;
                    default:
                        break;
                }
            }
        };

        View view = LayoutInflater.from(this).inflate(
                R.layout.dialog_set_sex, null);
        view.findViewById(R.id.tv_male).setOnClickListener(click);
        view.findViewById(R.id.tv_female).setOnClickListener(click);

        dialog.setContent(view);
        dialog.show();
    }

    /**
     * 修改性别
     */
    private void updateSex(final Integer sex) {
        if (!NetWorkUtil.isConnect(this)) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                                    RspValue<String> retValue = (RspValue<String>) rspData;
//                                    if(retValue != null){
//                                        Log.d("Nat: updateUserPassword.response", retValue.getValue());
//                                    }

                        MfhLoginService.get().updateSex(sex);

                        Message message = new Message();
                        message.what = MSG_UPDATE_SEX_SUCCESS;
                        uiHandler.sendMessage(message);
                    }

//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        Log.d("Nat: updateProfile.processFailure", errMsg);
//                        DialogUtil.showHint("修改性别失败");
//                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {

        };

        JSONObject object = new JSONObject();
        object.put("id", MfhLoginService.get().getCurrentGuId());
        object.put("sex", sex);
        UserApiImpl.updateProfile(object.toJSONString(), responseCallback);
    }

    private final static int MSG_UPDATE_HEADER_SUCCESS = 1;
    private final static int MSG_UPDATE_SEX_SUCCESS = 0;
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SEX_SUCCESS:
                    DialogUtil.showHint(getString(R.string.toast_change_sex_success));

                    btnItems.get(2).setSubTitle(Sex.formatName1(MfhLoginService.get().getSex()));

                    break;
            }
            super.handleMessage(msg);
        }
    };

}

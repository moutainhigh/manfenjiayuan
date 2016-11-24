package com.manfenjiayuan.mixicook_vip.ui.my;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.SettingsItem;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.api.mobile.Mixicook;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.widget.OnTabReselectListener;
import com.tencent.bugly.beta.Beta;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 设置
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-04-13
 * @since bingshanguxue
 */
public class SettingsFragment extends BaseFragment implements OnTabReselectListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.item_version)
    SettingsItem itemVersion;

    public SettingsFragment() {
        super();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("设置");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        refresh(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.item_bindshop)
    public void bindShop(){
        ActivityRoute.redirect2Url(getActivity(), Mixicook.URL_NET_SHOP_SEARCH);
    }

    @OnClick(R.id.item_loginpwd)
    public void changeLoginPwd(){
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_LOGIN_PASSWORD);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }
    @OnClick(R.id.item_paypwd)
    public void changePayPwd(){
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_PAY_PASSWORD);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @OnClick(R.id.item_general)
    public void generalSettings(){
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_SETTINGS_GENERAL);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }


    /**
     * 加载用户数据
     */
    private void refresh(boolean isAutoReload) {
        AppInfo appInfo = AnalysisAgent.getAppInfo(AppContext.getAppContext());
        if (appInfo != null) {
            itemVersion.setSubTitle(String.format(Locale.US, "%s - %d",
                    appInfo.getVersionName(), appInfo.getVersionCode()));
        }
    }


    @Override
    public void onTabReselect() {
        refresh(true);
    }

    /**
     * 显示退出提示框
     */
    @OnClick(R.id.button_logout)
    public void showLogoutAlert() {
        CommonDialog dialog = new CommonDialog(getActivity());
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
        MfhLoginService.get().clear();

        Intent data = new Intent();
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();

//
////        Intent intent = new Intent(this, H5AuthActivity.class);
////        intent.putExtra(H5AuthActivity.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_AUTH_GUIDE);
////        intent.putExtra(H5AuthActivity.EXTRA_KEY_AUTH_MODE, 0);
////        startActivity(intent);
//
////        MainActivity.actionStart(SettingsActivity.this, 0);
//
    }

    private static long exitTime = 0;
    private static int clickVersionTimes = 0;
    @OnClick(R.id.item_version)
    public void checkUpdate() {
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()){
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                Beta.checkUpgrade();
                exitTime = System.currentTimeMillis();
            }
        }
        else{
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                clickVersionTimes = 1;
                exitTime = System.currentTimeMillis();
                Beta.checkUpgrade();
            }
            else{
                clickVersionTimes++;
            }

            if (clickVersionTimes == 8){
                clickVersionTimes = 0;

                DialogUtil.showHint("恭喜你,你已经获取到超级权限!");
                SharedPrefesManagerFactory.setSuperPermissionGranted(true);
                refresh(true);
            }
            else{
                DialogUtil.showHint(String.format("再点 %d 次即可获得超级权限!", 8-clickVersionTimes));
            }
        }
    }

}

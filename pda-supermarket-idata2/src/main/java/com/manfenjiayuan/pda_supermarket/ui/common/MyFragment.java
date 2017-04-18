package com.manfenjiayuan.pda_supermarket.ui.common;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bingshanguxue.pda.utils.SharedPrefesManagerUltimate;
import com.bingshanguxue.vector_uikit.ProfileView;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.service.DataDownloadManager;
import com.manfenjiayuan.pda_supermarket.service.DemoPushService;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.anlaysis.remoteControl.RemoteControlClient;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.tencent.bugly.beta.Beta;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;


/**
 * 我·
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-04-13
 * @since bingshanguxue
 */
public class MyFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.profileView)
    ProfileView mProfileView;
    @BindView(R.id.item_camerasweep)
    ToggleSettingItem itemCameraSweep;
    @BindView(R.id.item_softinput)
    ToggleSettingItem itemSoftInput;
    @BindView(R.id.item_posgoods)
    SettingsItem itemPosGoods;
    @BindView(R.id.item_terminal)
    SettingsItem terminalSettingsItem;
    @BindView(R.id.item_upgrade)
    SettingsItem upgradeItem;
    @BindView(R.id.item_gettui)
    SettingsItem getuiItem;
    @BindView(R.id.button_logout)
    Button btnLogout;


    public static MyFragment newInstance(Bundle args) {
        MyFragment fragment = new MyFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_my;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("我的");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        itemCameraSweep.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean on) {
                if (SharedPrefesManagerFactory.isCameraSweepEnabled() != on){
                    SharedPrefesManagerFactory.setCameraSweepEnabled(on);
                }
            }
        });
        itemSoftInput.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean on) {
                if (SharedPrefesManagerFactory.isSoftInputEnabled() != on){
                    SharedPrefesManagerFactory.setSoftInputEnabled(on);
                }
            }
        });

        refresh();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.d(String.format("MyFragment.onActivityResult.requestCode=%d, resultCode=%d", requestCode, resultCode));

        switch (requestCode) {
//            case OwnerConstants.ACTIVITY_REQUEST_CODE_SETTINGS:
//                if(resultCode == Activity.RESULT_OK){
//                    if(data.getBooleanExtra(OwnerConstants.INTENT_KEY_IS_LOGOUT, false)){
//                        needLoadData = false;
//                        UIHelper.sendLoginBroadcast(getActivity());
//
//                        //在设置页面点击退出账号后需要将之前打开的页面关掉。
////                        getActivity().finish();
//                        break;
//                    }else{
//
//                    }
//                }
//            case Constants.ACTIVITY_REQUEST_ME_ORDER:
//            case Constants.ACTIVITY_REQUEST_ME_PACKAGE:
//            case Constants.ACTIVITY_REQUEST_ME_CART:
//            case Constants.ACTIVITY_REQUEST_RECEIVE_STOCK:
//            case Constants.ACTIVITY_REQUEST_SUBDIS_SELECT:
//                loadData();//刷新数据
//                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.button_logout)
    /**
     * 显示退出提示框
     */
    public void showLogoutAlert() {
        CommonDialog dialog = new CommonDialog(getContext());
        dialog.setMessage(R.string.dialog_message_logout);
        dialog.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                logout();

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
    public void logout() {
        ZLogger.df("手动退出当前账号");
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在退出当前账号...", false);
//                    // 保存统计数据
//                    MobclickAgent.onKillProcess(CashierApp.getAppContext());
//
//                    //退出程序
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    System.exit(0);
        MfhUserManager.getInstance().logout(new Callback() {
            @Override
            public void onSuccess() {
//                showProgressDialog(ProgressDialog.STATUS_DONE, "正在退出当前账号...", false);
                hideProgressDialog();
                MfhLoginService.get().clear();

                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
//                showProgressDialog(ProgressDialog.STATUS_ERROR, "正在退出当前账号...", true);
                hideProgressDialog();
                MfhLoginService.get().clear();

                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });
    }

    /**
     * 商品库恢复出厂设置
     */
    @OnClick(R.id.item_posgoods)
    public void syncGoods() {
        showConfirmDialog("同步商品库到最新版本，同步过程中会先删除历史数据，可能会影响正常收银，确定要同步吗？",
                "全量更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        showProgressDialog(ProgressDialog.STATUS_DONE, "请稍候...", true);

                        // 强制同步
                        SharedPrefesManagerUltimate.setSyncProductsStartcursor("");

                        DataDownloadManager.get().syncProducts();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 升级
     */
    @OnClick(R.id.item_upgrade)
    public void checkUpgrade() {
        Beta.checkUpgrade();
    }

    @OnLongClick(R.id.item_upgrade)
    public boolean downloadApkFile() {
        showConfirmDialog("打开浏览器下载最新版本APK文件！",
                "去下载", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        UIHelper.openBrowser(getActivity(), "https://beta.bugly.qq.com/pda-supermarket");
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return true;
    }

    @OnClick(R.id.item_remotecontrol)
    public void remoteControl() {
        RemoteControlClient.getInstance().onekeyFeedback();
    }

    @OnClick(R.id.item_gettui)
    public void activeGetui(){
        String cid = PushManager.getInstance().getClientid(AppContext.getAppContext());

        if (PushManager.getInstance().isPushTurnedOn(AppContext.getAppContext())) {
            ZLogger.d("个推服务已经开启");
//                    PushManager.getInstance().stopService(AppContext.getAppContext());
//            PushManager.getInstance().initialize(AppContext.getAppContext());
        } else {
            ZLogger.d("个推服务未开启");

            if (StringUtils.isEmpty(cid)) {
                ZLogger.df("准备初始化个推服务...");
                PushManager.getInstance().initialize(AppContext.getAppContext(), DemoPushService.class);
//                        PushManager.getInstance().turnOnPush(AppContext.getAppContext());
            } else {
                ZLogger.df("准备开启个推服务...");
                PushManager.getInstance().turnOnPush(AppContext.getAppContext());
            }
        }
    }

    /**
     * 注意：不能使用getActivity()启动startActivityForResult，
     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     */
    private void redirectToJBWebForResult(String url, boolean bNeedSyncCookie, int animType) {
        Intent intent = new Intent(getActivity(), HybridActivity.class);
        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, animType);
        startActivity(intent);
    }

    /**
     * 加载用户数据
     */
    private void refresh() {
        mProfileView.setAvatarUrl(MfhLoginService.get().getHeadimage());
        mProfileView.setPrimaryText(MfhLoginService.get().getHumanName());
        mProfileView.setSecondaryText(MfhLoginService.get().getTelephone());
        itemCameraSweep.setChecked(SharedPrefesManagerFactory.isCameraSweepEnabled());
        itemSoftInput.setChecked(SharedPrefesManagerFactory.isSoftInputEnabled());
        terminalSettingsItem.setSubTitle(SharedPrefesManagerFactory.getTerminalId());

        AppInfo appInfo = AnalysisAgent.getAppInfo(AppContext.getAppContext());
        if (appInfo != null) {
            upgradeItem.setSubTitle(String.format(Locale.US, "%s - %d",
                    appInfo.getVersionName(), appInfo.getVersionCode()));
        }

        getuiItem.setSubTitle(String.format("%s-%s",
                PushManager.getInstance().getClientid(AppContext.getAppContext()),
                IMConfig.getPushClientId()));

    }

}

package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.TenantInfoWrapper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import butterknife.BindView;


/**
 * 设置－－账号
 * Created by kun on 15/8/31.
 */
public class SettingsAccountFragment extends BaseFragment {
    @BindView(R.id.iv_logo)
    ImageView ivSassLogo;
    @BindView(R.id.tv_saasName)
    TextView tvSassName;
    @BindView(R.id.tv_domainUrl)
    TextView tvDomainUrl;
    @BindView(R.id.tv_contact)
    TextView tvContact;
    @BindView(R.id.tv_mobilenumber)
    TextView tvMobileNumber;
    @BindView(R.id.tv_curofficeid)
    TextView tvCurOfficeId;
    @BindView(R.id.text_account_sync_interval)
    TextView tvAccountSyncInterval;
    @BindView(R.id.switchCompat_accountSync)
    SwitchCompat accountSyncSwitchCompat;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_account;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        TenantInfoWrapper hostServer = GlobalInstanceBase.getInstance().getHostServer();
        if (hostServer != null){
            Glide.with(getContext())
                    .load(hostServer.getLogopicUrl())
                    .error(R.mipmap.ic_image_error)
                    .into(ivSassLogo);
            tvSassName.setText(hostServer.getSaasName());
            tvDomainUrl.setText(hostServer.getDomainUrl());
            tvContact.setText(hostServer.getContact());
            tvMobileNumber.setText(hostServer.getMobilenumber());
        }
        else{
            Glide.with(getContext())
                    .load("")
                    .error(R.mipmap.ic_image_error)
                    .into(ivSassLogo);
            tvSassName.setText("");
            tvDomainUrl.setText("");
            tvContact.setText("");
            tvMobileNumber.setText("");
        }

        String oficeInfo = String.format("网点: %d(%s)"
                , MfhLoginService.get().getCurOfficeId(), MfhLoginService.get().getCurOfficeName())
                + String.format("\n租户: %d", MfhLoginService.get().getSpid())
                + String.format("\n仓储编号: %s", MfhLoginService.get().getCurStockId())
                + String.format("\n收银员: %s", MfhLoginService.get().getHumanName());
        tvCurOfficeId.setText(oficeInfo);

        tvAccountSyncInterval.setText(String.format("账号同步间隔：%ds", SharedPreferencesUltimate.getSyncCompanyHumanInterval()));
        accountSyncSwitchCompat.setChecked(SharedPreferencesUltimate.isSyncCompanyHumanEnabled());
        accountSyncSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesUltimate.setSyncCompanyHumanEnabled(isChecked);
            }
        });
    }


}

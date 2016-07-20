package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;

/**
 * 设置－－账号
 * Created by kun on 15/8/31.
 */
public class SettingsAccountFragment extends BaseFragment {

    @Bind(R.id.tv_curofficeid)
    TextView tvCurOfficeId;
    @Bind(R.id.text_account_sync_interval)
    TextView tvAccountSyncInterval;
    @Bind(R.id.switchCompat_accountSync)
    SwitchCompat accountSyncSwitchCompat;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_account;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        String oficeInfo = String.format("网点: %d(%s)"
                , MfhLoginService.get().getCurOfficeId(), MfhLoginService.get().getCurOfficeName())
                + String.format("\n租户: %d", MfhLoginService.get().getSpid())
                + String.format("\n仓储编号: %s", MfhLoginService.get().getCurStockId())
                + String.format("\n营业员: %s", MfhLoginService.get().getHumanName());
        tvCurOfficeId.setText(oficeInfo);

        tvAccountSyncInterval.setText(String.format("账号同步间隔：%ds", SharedPreferencesHelper.getSyncCompanyHumanInterval()));
        accountSyncSwitchCompat.setChecked(SharedPreferencesHelper.isSyncCompanyHumanEnabled());
        accountSyncSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesHelper.setSyncCompanyHumanEnabled(isChecked);
            }
        });
    }


}

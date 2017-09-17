package com.manfenjiayuan.mixicook_vip.ui;

import android.os.Bundle;

import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.home.HomeFragment;
import com.manfenjiayuan.mixicook_vip.utils.AppHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.tencent.bugly.beta.Beta;

public class  MainActivity extends BaseActivity {

    private HomeFragment mHomeFragment;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean isMfLocationEnable() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
        AppHelper.getInstance().addActivity(this);

        showHomeFragment();

        Beta.checkUpgrade(false, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void showHomeFragment(){
        if (mHomeFragment == null){
            mHomeFragment = new HomeFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mHomeFragment)
                .commit();
    }

}

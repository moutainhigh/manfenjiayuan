package com.manfenjiayuan.business.route;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.manfenjiayuan.business.R;
import com.manfenjiayuan.business.hostserver.HostServerFragment;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 路由
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class RouteActivity extends BaseActivity implements Route, BackHandledInterface {
    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";

    public static final int FT_APP_HOSTSERVER = 0x01;//选择平台租户

    private int fragmentType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, FragmentActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();

        DeviceUtils.hideSoftInput(this);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        DeviceUtils.hideSoftInput(this);

        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                super.onBackPressed();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    private BaseFragment mBackHandedFragment;

    @Override
    public void setSelectedFragment(BaseFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            isFullscreenEnabled = intent.getBooleanExtra(EXTRA_KEY_FULLSCREEN, false);
            fragmentType = intent.getIntExtra(EXTRA_KEY_FRAGMENT_TYPE, -1);

            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        switch (fragmentType) {
            case FT_APP_HOSTSERVER: {
                HostServerFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = HostServerFragment.newInstance(intent.getExtras());
                } else {
                    fragment = HostServerFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
        }
    }

}

package com.bingshanguxue.almigod;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.almigod.clientLog.ClientLogListFragment;
import com.bingshanguxue.almigod.pos.PosListFragment;
import com.bingshanguxue.almigod.remoteControl.RemoteControlListFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class FragmentActivity extends BaseActivity implements BackHandledInterface {
    public static final String EXTRA_KEY_FRAGMENT_TYPE = "fragmentType";

    public static final int FT_INSPECT_POS_LIST = 0x01;//POS设备列表
    public static final int FT_CLIENTLOG_LIST = 0x02;//日志列表
    public static final int FT_REMOTECONTROL_LIST = 0x03;//远程控制

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
//            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
//            if (animType == ANIM_TYPE_NEW_FLOW) {
////                this.setTheme(R.style.NewFlow);
//            }

            fragmentType = intent.getIntExtra(EXTRA_KEY_FRAGMENT_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        switch (fragmentType) {
            case FT_INSPECT_POS_LIST: {
                ZLogger.d("准备进入POS设备列表页面 开始");

                PosListFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = PosListFragment.newInstance(intent.getExtras());
                } else {
                    fragment = PosListFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                ZLogger.d("准备进入POS设备列表页面 结束");
            }
            break;
            case FT_CLIENTLOG_LIST: {
                ZLogger.d("准备进入日志列表页面 开始");

                ClientLogListFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = ClientLogListFragment.newInstance(intent.getExtras());
                } else {
                    fragment = ClientLogListFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                ZLogger.d("准备进入日志列表页面 结束");
            }
            break;
            case FT_REMOTECONTROL_LIST: {
                ZLogger.d("准备进入日志列表页面 开始");

                RemoteControlListFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = RemoteControlListFragment.newInstance(intent.getExtras());
                } else {
                    fragment = RemoteControlListFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                ZLogger.d("准备进入日志列表页面 结束");
            }
            break;
        }
    }

}

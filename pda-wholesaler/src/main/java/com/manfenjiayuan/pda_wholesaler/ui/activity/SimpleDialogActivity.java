package com.manfenjiayuan.pda_wholesaler.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.manfenjiayuan.pda_wholesaler.R;
import com.bingshanguxue.pda.IData95Activity;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.invsendio.InvIoOrderSplashFragment;

import butterknife.Bind;

/**
 * 对话框
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleDialogActivity extends IData95Activity {
    public static final String EXTRA_KEY_FRAGMENT_TYPE = "fragmentType";
    public static final String EXTRA_KEY_DIALOG_WIDTH_INPIXEL = "dialogWidthInPixel";
    private int fragmentType = 0;
    private int dialogWidthInPixel = 0;


    @Bind(R.id.fragment_container)
    FrameLayout frameLayout;

    public static final int FT_CREATE_INVIOORDER_SPLASH = 0x01;//新建发货单

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SimpleDialogActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_simple_dialog;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return true;
    }

    @Override
    protected boolean finishScannerWhenDestroyEnabled() {
        return false;
    }

    @Override
    protected void initViews() {
        super.initViews();

        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        layoutParams.width = dialogWidthInPixel;
        frameLayout.setLayoutParams(layoutParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
//                this.setTheme(R.style.activity_new_task);
            }

            fragmentType = intent.getIntExtra(EXTRA_KEY_FRAGMENT_TYPE, -1);
            dialogWidthInPixel = intent.getIntExtra(EXTRA_KEY_DIALOG_WIDTH_INPIXEL, 0);
//            ZLogger.d("serviceType=" + serviceType);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        if (fragmentType == FT_CREATE_INVIOORDER_SPLASH) {
            InvIoOrderSplashFragment fragment;
            Intent intent = this.getIntent();
            if (intent != null) {
                fragment = InvIoOrderSplashFragment.newInstance(intent.getExtras());
            } else {
                fragment = InvIoOrderSplashFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}

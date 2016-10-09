package com.manfenjiayuan.mixicook_vip.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import com.manfenjiayuan.mixicook_vip.InputTextFragment;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.home.QuickPayFragment;
import com.manfenjiayuan.mixicook_vip.ui.my.MyProfileFragment;
import com.mfh.framework.uikit.base.BaseActivity;

import butterknife.Bind;

import static com.manfenjiayuan.mixicook_vip.ui.FragmentActivity.FT_QUICK_PAY;

/**
 *
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleActivity extends BaseActivity {
    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";

    public static final int FT_INPUT_TEXT = 0x02;
    public static final int FT_MYPROFILE = 0x04;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    /**
     * 0: 快递代收
     */
    private int fragmentType = 0;
    private String title;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SimpleActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_simple;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SimpleActivity.this.onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_close) {
//                    finish();
//                } else if (id == R.id.action_fresh_shopcart) {
//                    redirectToFruitShopcart();
//                } else if (id == R.id.action_shopcart_standard) {
//                    redirectToStandardShopcart();
//                }
//                return true;
//            }
//        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_empty);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
//                this.setTheme(R.style.NewFlow);
            }

            fragmentType = intent.getIntExtra(EXTRA_KEY_FRAGMENT_TYPE, -1);
            title = intent.getStringExtra(EXTRA_TITLE);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        toolbar.setTitle(title);

        switch (fragmentType) {

            case FT_INPUT_TEXT: {
                InputTextFragment fragment = new InputTextFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_MYPROFILE: {
                MyProfileFragment fragment = new MyProfileFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            default:
                break;
        }
    }

}

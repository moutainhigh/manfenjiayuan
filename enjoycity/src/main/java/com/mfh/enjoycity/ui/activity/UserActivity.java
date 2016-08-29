package com.mfh.enjoycity.ui.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.mfh.enjoycity.AppHelper;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.SettingsActivity;
import com.mfh.enjoycity.ui.fragments.IndividualFragment;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.network.URLHelper;

import butterknife.Bind;


/**
 * 我的**
 */
public class  UserActivity extends BaseActivity {
    private static final String TAG = UserActivity.class.getSimpleName();

    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    private IndividualFragment individualFragment;

    private BroadcastReceiver receiver;

    public static void actionStart(Context context, int animationType) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, animationType);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle("");
//        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserActivity.this.onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_settings) {
//                    UIHelper.redirectToActivity(UserActivity.this, SettingsActivity.class);
                    startActivityForResult(new Intent(UserActivity.this, SettingsActivity.class), Constants.ACTIVITY_REQUEST_CODE_SETTINGS);
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_user);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            // Translucent status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //注释该行，解决底部导航Tab在5.1.1 Nexus手机上和底部状态栏重叠问题。
            // Translucent navigation bar
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        registerReceiver();

        individualFragment = new IndividualFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, individualFragment)
                .show(individualFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
//        final MenuItem settings = menu.findItem(R.id.action_settings);
//        MenuItemCompat.setActionView(settings, R.layout.view_corner_button);
//        final Button btnSettings = (Button) settings.getActionView().findViewById(R.id.corner_button);
//        btnSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UIHelper.redirectToActivity(UserActivity.this, SettingsActivity.class);
//            }
//        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //android.app.IntentReceiverLeaked: Activity com.mfh.life.ui.UserActivity has leaked IntentReceiver com.mfh.life.ui.UserActivity$3@443b09b8 that was originally registered here. Are you missing a call to unregisterReceiver()?
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Constants.ACTIVITY_REQUEST_LOGIN_H5)
        {
            //TODO
            if(resultCode == Activity.RESULT_OK){
                //TODO 判断是从那一个页面返回的
                canRedirectToLogin = true;
                if(individualFragment != null && individualFragment.isVisible()){
                    individualFragment.reloadData();
                }
            }
            else{
                canRedirectToLogin = false;
//                setResult(Activity.RESULT_CANCELED);
//                com.mfh.comna.api.helper.UIHelper.redirectToActivity(UserActivity.this, MainActivity.class);
//                finish();
            }
        }
        else if(requestCode == Constants.ACTIVITY_REQUEST_CODE_SETTINGS){
            if(resultCode == Activity.RESULT_OK){
                if(intent.getBooleanExtra(Constants.INTENT_KEY_IS_LOGOUT, false)){
                    AppHelper.resetMemberAccountData();
                    setResult(Activity.RESULT_OK);
                }
            }
            else{

            }
        }
    }

    /**
     * */
    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, -1);

            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if(animType == ANIM_TYPE_NEW_FLOW){
                this.setTheme(R.style.AppTheme_NewFlow);
            }
        }
    }

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();//接收者只有在activity才起作用。
        filter.addAction(UIHelper.ACTION_REDIRECT_TO_LOGIN_H5);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(UIHelper.ACTION_REDIRECT_TO_LOGIN_H5)){
                    redirectToLogin();
//                    Bundle bundle = new Bundle();
//                    bundle.putBoolean(Constants.EXTRA_KEY_NEED_LOGIN, true);
//                    setResult(Activity.RESULT_CANCELED,null, bundle);
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private boolean canRedirectToLogin = true;
    private void redirectToLogin(){
        if(!canRedirectToLogin){
            return;
        }

        canRedirectToLogin = false;
        AppHelper.resetMemberAccountData();

        //TODO,判断当前页是否需要切换登录页面
        String authUrl = URLHelper.append(H5Api.URL_AUTH_INDEX, "redirect=" + H5Api.URL_NATIVIE_REDIRECT_AUTH);
        startActivityForResult(HybridActivity.loginIntent(UserActivity.this, authUrl), Constants.ACTIVITY_REQUEST_LOGIN_H5);
//        canRedirectToLogin = true;
    }

}

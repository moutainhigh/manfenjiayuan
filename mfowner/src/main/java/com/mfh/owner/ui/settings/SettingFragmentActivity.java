package com.mfh.owner.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.owner.R;

import butterknife.Bind;

/**
 * 设置·修改昵称/修改登录密码/修改支付密码
 * Created by ZZN on 15-05-28.
 */
public class SettingFragmentActivity extends BaseActivity {
    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";

    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.topbar_title) TextView tvTopBarTitle;

//    private Fragment[] fragments;
    private ChangeNicknameFragment nicknameFragment;
    private ChangeLoginPwdFragment loginPwdFragment;
    private ChangePayPwdFragment payPwdFragment;

    /**
     * 0: 修改昵称
     * 1: 修改登录密码
     * */
    private int fragmentType = 0;


    public static void actionStart(Context context, int fragmentType){
        Intent intent = new Intent(context, SettingFragmentActivity.class);
        intent.putExtra(EXTRA_KEY_FRAGMENT_TYPE, fragmentType);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_custom_fragment;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentType = this.getIntent().getIntExtra(EXTRA_KEY_FRAGMENT_TYPE, 0);

        initTopBar();
        initFragments();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null){
            outState.putInt(EXTRA_KEY_FRAGMENT_TYPE, fragmentType);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override//只要发生切换，一定会调用到stop
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {//如果只是锁屏，不会调用destroy。
        super.onDestroy();
    }

    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(fragmentType == 0){
            tvTopBarTitle.setText(R.string.topbar_title_change_nickname);
        }
        else if(fragmentType == 1){
            tvTopBarTitle.setText(R.string.topbar_title_change_pwd_login);
        }
        else if(fragmentType == 2){
            tvTopBarTitle.setText(R.string.topbar_title_change_pwd_pay);
        }
        else{
            tvTopBarTitle.setText("");
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     * */
    private void initFragments(){
        nicknameFragment = new ChangeNicknameFragment();
        loginPwdFragment = new ChangeLoginPwdFragment();
        payPwdFragment = new ChangePayPwdFragment();

//        fragments = new Fragment[]{serviceFragment, surroundFragment};
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, nicknameFragment)
                .add(R.id.fragment_container, loginPwdFragment).add(R.id.fragment_container, payPwdFragment)
                .hide(nicknameFragment).hide(loginPwdFragment).hide(payPwdFragment)
                .commit();

        if(fragmentType == 0){
            getSupportFragmentManager().beginTransaction().show(nicknameFragment).commit();
        }
        else if(fragmentType == 1){
            getSupportFragmentManager().beginTransaction().show(loginPwdFragment).commit();
        }
        else if(fragmentType == 2){
            getSupportFragmentManager().beginTransaction().show(payPwdFragment).commit();
        }
        else{
        }
    }
}

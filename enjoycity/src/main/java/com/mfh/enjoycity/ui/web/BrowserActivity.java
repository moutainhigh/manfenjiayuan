package com.mfh.enjoycity.ui.web;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import com.mfh.enjoycity.R;
import com.mfh.framework.uikit.base.BaseActivity;

import butterknife.Bind;


/**
 * 浏览器
 * */
public class BrowserActivity extends BaseActivity {
    public static final String EXTRA_KEY_REDIRECT_URL = "redirectUrl";
    public static final String EXTRA_KEY_SYNC_COOKIE = "syncCookie";
    public static final String EXTRA_KEY_GOBACK = "goBack";

    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;

    private Context context;

    private BrowserFragment browserFragment;

    private boolean backAsHomeUp = true;//true,关闭网页(默认);false,返回上一页

    public static void actionStart(Activity context, String redirectUrl){
        actionStart(context, redirectUrl, false, false, false);
    }
    public static void actionStart(Activity context, String redirectUrl, boolean bSyncCookie, boolean bGoBack, boolean bAnimation){
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
        intent.putExtra(EXTRA_KEY_GOBACK, bGoBack);
        context.startActivity(intent);

        if(bAnimation){
            //Activity切换动画,缩放+透明
            context.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
    }
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_browser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        initTopBar();

        browserFragment = new BrowserFragment();
        browserFragment.setBrowserListener(new BrowserFragment.BrowserListener() {
            @Override
            public void onTitleChanged(String title) {
                tvTopBarTitle.setText(title);
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, browserFragment)
                .show(browserFragment)
                .commit();
    }

    public void onResume(){
        super.onResume();
    }
    public void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
        tvTopBarTitle.setText("");
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

}

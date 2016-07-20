package com.manfenjiayuan.loveshopping.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.mfh.framework.hybrid.BaseHybridActivity;
import com.mfh.framework.hybrid.JBridgeConf;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;


/**
 * H5 JavascriptBridge · WebView · 与具体业务相关
 *
 * @author bingshanguxue
 */
public class HybridActivity extends BaseHybridActivity {

    public static Intent loginIntent(Activity context, String redirectUrl) {
        Intent intent = new Intent(context, HybridActivity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_JSBRIDGE_ENABLED, true);
        intent.putExtra(EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, 0);
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static void actionStart(Activity context, String redirectUrl){
        actionStart(context, redirectUrl, -1);
    }
    public static void actionStart(Activity context, String redirectUrl, int animationType){
        actionStart(context, redirectUrl, true, animationType);
    }
    public static void actionStart(Activity context, String redirectUrl, boolean backAsHomeUp, int animationType){
        actionStart(context, redirectUrl, false, backAsHomeUp, animationType);
    }
    public static void actionStart(Activity context, String redirectUrl,
                                   boolean bSyncCookie,
                                   boolean backAsHomeUp, int animationType){
        actionStart(context, redirectUrl, true, bSyncCookie, backAsHomeUp, animationType);
    }
    public static void actionStart(Activity context, String redirectUrl,
                                   boolean jsBridgeEnabled,boolean bSyncCookie,
                                   boolean backAsHomeUp, int animationType){
        Intent intent = new Intent(context, HybridActivity.class);
        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
        intent.putExtra(EXTRA_KEY_JSBRIDGE_ENABLED, jsBridgeEnabled);
        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
        intent.putExtra(EXTRA_KEY_BACKASHOMEUP, backAsHomeUp);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, animationType);
        context.startActivity(intent);
    }

    //TODO
//    //actionStartForResult
//    public static void actionStart(Context context, String redirectUrl, boolean bSyncCookie,
//                                   boolean backAsHomeUp, int animationType){
//        Intent intent = new Intent(context, HybridActivity.class);
//        intent.putExtra(EXTRA_KEY_REDIRECT_URL, redirectUrl);
//        intent.putExtra(EXTRA_KEY_SYNC_COOKIE, bSyncCookie);
//        intent.putExtra(EXTRA_KEY_BACKASHOMEUP, backAsHomeUp);
//        context.startActivity(intent);
//
//        //默认无动画（-1）
//        if(animationType == 0){
//            //Activity切换动画,缩放+透明
//            context.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
//        }
//
//        start
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);


    }

    @Override
    protected void initToolBar() {
        super.initToolBar();

//        // Set an OnMenuItemClickListener to handle menu item clicks
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_more) {
//                    showSharePopup(toolbar, myWebView.getUrl());
//                }
//                return true;
//            }
//        });
//
//        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_web);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_web, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * register native method
     */
    @Override
    protected void registerHandle() {
        super.registerHandle();
        if (bridge == null){
            return;
        }
        //保存用户登录信息
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_SAVE_USER_LOGIN_INFO,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        AppHelper.saveUserLoginInfo(data);
                    }
                });

    }



}

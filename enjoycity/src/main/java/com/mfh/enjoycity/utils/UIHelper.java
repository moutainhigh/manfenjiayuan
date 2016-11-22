package com.mfh.enjoycity.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.manfenjiayuan.business.ui.HybridActivity;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.activity.NativeWebViewActivity;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.URLHelper;
import com.mfh.framework.uikit.dialog.CommonDialog;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * Created by Nat on 2015/5/11.
 */
public class UIHelper {
    //Broadcast
    public static final String ACTION_REDIRECT_TO_LOGIN_H5 = "ACTION_REDIRECT_TO_LOGIN_H5";//跳转到登录页面


    public static final String BUNDLE_KEY_RESULE = "result";
    public static final String BUNDLE_KEY_BITMAP = "bitmap";

//    /**
//     * 跳转至Native WebView页面
//     *
//     * @param context
//     * @param url
//     * @param bNeedSyncCookie 是否需要同步COOKIE
//     * */
//    public static void redirectToNativeWebForResult(Activity context, String url, boolean bNeedSyncCookie, int requestCode){
//        Intent intent = new Intent(context, NativeWebViewActivity.class);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_GOBACK, true);
//        context.startActivityForResult(intent, requestCode);
//    }


    /**
     * 网络链接选项
     * */
    public static void showUrlOption(final Activity context, final String url) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setMessage(context.getString(R.string.dialog_message_link_option, url));
        dialog.setPositiveButton(R.string.dialog_button_open, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //满分家园的链接在当前应用的webview打开，其他的链接启动浏览器打开
                if (url.contains(MobileApi.DOMAIN)) {
                    NativeWebViewActivity.actionStart(context, url);
                } else {
                    com.mfh.framework.uikit.UIHelper.openBrowser(context, url);
                }
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * 显示可复制的文本
     * */
    public static void showCopyTextOption(final Context context, final String text) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setMessage(text);
        dialog.setPositiveButton(R.string.dialog_button_copy, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager cbm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                cbm.setText(text);
                DialogUtil.showHint(context.getString(R.string.toast_copy_success));
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 显示出库选项
     * */
    public static void showStockOption(final Activity context, final String text) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setMessage(text);
        dialog.setPositiveButton(R.string.dialog_button_stock_out, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = URLHelper.append(MobileApi.URL_STOCK_OUT,
                        String.format("queryCon=%s",text));
                HybridActivity.actionStart(context, url, true, false, 0);
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 收货地址不在当前定位点附近
     * */
    public static void showLocationAlert(final Activity context) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setMessage("当前配送地址不再您的附近哦");
        dialog.setPositiveButton("切换配送地址", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//                String url = URLHelper.append(MobileURLConf.URL_STOCK_OUT,
//                        String.format("queryCon=%s",text));
//                HybridActivity.actionStart(context, url, true, false, 0);
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("继续购物", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 清除WebView缓存
     */
//    public void clearWebViewCache(){
//
//        //清理Webview缓存数据库
//        try {
//            deleteDatabase("webview.db");
//            deleteDatabase("webviewCache.db");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //WebView 缓存文件
//        File appCacheDir = new File(getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME);
//        Log.e(TAG, "appCacheDir path="+appCacheDir.getAbsolutePath());
//
//        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/webviewCache");
//        Log.e(TAG, "webviewCacheDir path="+webviewCacheDir.getAbsolutePath());
//
//        //删除webview 缓存目录
//        if(webviewCacheDir.exists()){
//            deleteFile(webviewCacheDir);
//        }
//        //删除webview 缓存 缓存目录
//        if(appCacheDir.exists()){
//            deleteFile(appCacheDir);
//        }
//    }
//
//    /**
//     * 递归删除 文件/文件夹
//     *
//     * @param file
//     */
//    public void deleteFile(File file) {
//
//        Log.i(TAG, "delete file path=" + file.getAbsolutePath());
//
//        if (file.exists()) {
//            if (file.isFile()) {
//                file.delete();
//            } else if (file.isDirectory()) {
//                File files[] = file.listFiles();
//                for (int i = 0; i < files.length; i++) {
//                    deleteFile(files[i]);
//                }
//            }
//            file.delete();
//        } else {
//            Log.e(TAG, "delete file no exists " + file.getAbsolutePath());
//        }
//    }

    public static void sendBroadcast(String action){
        Intent intent = new Intent(action);
        MfhApplication.getAppContext().sendBroadcast(intent);
    }

    /**
     * 发送登录广播
     * */
    public static void sendLoginBroadcast(Context context){
        Intent intent = new Intent(ACTION_REDIRECT_TO_LOGIN_H5);
        context.sendBroadcast(intent);
    }

    /**
     * 广播：发送切换首页背景
     * */
    public static void sendToggleTabbarBroadcast(Context context, boolean isVisible){
        Intent intent = new Intent(Constants.BROADCAST_ACTION_TOGGLE_MAIN_TABHOST);
        intent.putExtra(Constants.BROADCAST_KEY_MAIN_TABHOST_VISIBILITY, isVisible);
        context.sendBroadcast(intent);
    }

    /**o
     * */
    public static void sendBroadcasstForChangeBackground(Context context, boolean isVisible){
        Intent intent = new Intent(Constants.BROADCAST_ACTION_CHANGE_BACKGROUND);
        intent.putExtra(Constants.BROADCAST_KEY_BACKGROUND_MASK_VISIBILITY, isVisible);
        context.sendBroadcast(intent);
    }


    /**
     * 显示接收订单对话框
     * */
    public static void showConfirmOrderDialog(final Activity context, String content, final String orderIds) {
        CommonDialog dialog = new CommonDialog(context);

        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_notify, null);
        TextView tvContent = (TextView)view.findViewById(R.id.tv_content);
        tvContent.setText(content);

        dialog.setContent(view);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                http://localhost:82/m/market/order/detail_supmkt.html?orderid=547032
            }
        });
        dialog.setNegativeButton("详情", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String url = URLHelper.append(MobileApi.URL_MARKET_ORDER_DETAIL_MALL,
                        String.format("orderid=%s", orderIds));
                HybridActivity.actionStart(context, url, true, false, 0);
            }
        });
        dialog.show();
    }

    /**
     * 显示确认收货对话框
     * */
    public static void showEvaluateDialog(final Activity context, String content, final String orderIds) {
        CommonDialog dialog = new CommonDialog(context);

        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_notify, null);
        TextView tvContent = (TextView)view.findViewById(R.id.tv_content);
        tvContent.setText(content);

        dialog.setContent(view);
        dialog.setPositiveButton("评价", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                DialogUtil.showHint("跳转至订单详情");

                String url =URLHelper.append(MobileApi.URL_EVALUATE_ORDER,
                        String.format("orderids=%s", orderIds));

                HybridActivity.actionStart(context, url, true, false, 0);
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 显示清除缓存Dialog
     * */
    public static void showCleanCacheDialog(Context context) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setMessage(R.string.dialog_message_clean_cache);
        dialog.setPositiveButton(R.string.dialog_button_clean, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//                AppHelper.clearAppCache();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


}

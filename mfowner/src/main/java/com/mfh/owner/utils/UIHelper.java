package com.mfh.owner.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.core.camera.CameraSessionUtil;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;
import com.mfh.owner.R;
import com.mfh.owner.ui.activity.NativeWebViewActivity;
import com.mfh.owner.ui.web.ComnJBH5Activity;

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
//     * */
//    public static void redirectToNativeWeb(Activity context, String url){
//        redirectToNativeWeb(context, url, false);
//    }
//    /**
//     * 跳转至Native WebView页面
//     *
//     * @param context
//     * @param url
//     * @param bNeedSyncCookie 是否需要同步COOKIE
//     * */
//    public static void redirectToNativeWeb(Activity context, String url, boolean bNeedSyncCookie){
//        Intent intent = new Intent(context, NativeWebViewActivity.class);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_GOBACK, bNeedSyncCookie);
//        context.startActivity(intent);
//        //Activity切换动画,缩放+透明
//        context.overridePendingTransition(com.mfh.comna.R.anim.zoom_in, com.mfh.comna.R.anim.zoom_out);
//    }
//    public static void redirectToNativeWebForResult(Activity context, String url, boolean bNeedSyncCookie, int requestCode){
//        Intent intent = new Intent(context, NativeWebViewActivity.class);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_GOBACK, true);
//        context.startActivityForResult(intent, requestCode);
//    }

    public static void redirectToActivityForResult(Activity context, java.lang.Class<?> cls, int requestCode){
        Intent intent = new Intent(context, cls);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityForResult(intent, requestCode);
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
                String url = MobileURLConf.generateUrl(MobileURLConf.URL_STOCK_OUT,
                        String.format("queryCon=%s",text));
                ComnJBH5Activity.actionStart(context, url, true, false, 0);
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
     * create SettingsItem
     * */
    public static SettingsItem createSettingsItem(View rootView, int id, SettingsItemData data,
                                            SettingsItem.ThemeType themeType, SettingsItem.SeperateLineType seperateLineType,
                                            View.OnClickListener onClickListener){
        SettingsItem item = (SettingsItem) rootView.findViewById(id);
        item.init(data);
        item.setButtonType(themeType, seperateLineType);
        item.setOnClickListener(onClickListener);
        return item;
    }

    /**
     * 选择头像
     * */
    public static void showUpdateHeaderDialog(final Activity context) {
        final CommonDialog dialog = DialogHelper
                .getPinterestDialogCancelable(context);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                switch (id) {
                    case R.id.tv_option_1:
                        context.startActivityForResult(new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                                CameraSessionUtil.REQUEST_CODE_XIANGCE);
                        break;
                    case R.id.tv_option_2:
                        CameraSessionUtil cameraUtil = ServiceFactory.getService(CameraSessionUtil.class.getName());
                        cameraUtil.makeCameraRequest(context);
                        break;
                    default:
                        break;
                }
            }
        };

        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_select_picture, null);
        view.findViewById(R.id.tv_option_1).setOnClickListener(click);
        view.findViewById(R.id.tv_option_2).setOnClickListener(click);

        dialog.setContent(view);
        dialog.show();
    }
    /**
     * 选择头像
     * */
    public static void showSelectPictureDialog(final Activity context) {
        final CommonDialog dialog = DialogHelper
                .getPinterestDialogCancelable(context);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                if(id == R.id.tv_option_1){
                    context.startActivityForResult(new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                            CameraSessionUtil.REQUEST_CODE_XIANGCE);
                }else if(id == R.id.tv_option_2){
                    CameraSessionUtil cameraUtil = ServiceFactory.getService(CameraSessionUtil.class.getName());
                    cameraUtil.makeCameraRequest(context);
                }
            }
        };

        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_select_picture, null);
        view.findViewById(R.id.tv_option_1).setOnClickListener(click);
        view.findViewById(R.id.tv_option_2).setOnClickListener(click);

        dialog.setContent(view);
        dialog.show();
    }

    /**
     * 打开浏览器
     *
     * @param context
     * @param url
     */
    public static void openBrowser(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
//            W/System.err﹕ at java.lang.reflect.Method.invokeNative(Native Method)
//            W/System.err﹕ at dalvik.system.NativeStart.main(Native Method)
            context.startActivity(it);
        } catch (Exception e) {
            //android.content.ActivityNotFoundException: No Activity found to handle Intent { act=android.intent.action.VIEW dat=bonjour ami朋厨烘培 }
            ZLogger.e("openBrowser failed:" + e.toString());
//            e.printStackTrace();
//            ToastMessage(context, "无法浏览此网页", 500);
//            DialogUtil.showHint("无法打开链接");
        }
    }

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
                    openBrowser(context, url);
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

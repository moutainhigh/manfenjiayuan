package com.mfh.framework.uikit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mfh.framework.Constants;
import com.mfh.framework.R;
import com.mfh.framework.core.camera.CameraSessionUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;

/**
 * Created by bingshanguxue on 4/13/16.
 */
public class UIHelper {
    public static final int ACTIVITY_REQUEST_CODE_ZXING_QRCODE = 0X1001;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS   = 0X1020;//读取联系人
    public static final int PERMISSIONS_REQUEST_CALL_PHONE      = 0X1021;//拨打电话
    public static final int PERMISSIONS_REQUEST_CAMERA          = 0X1022;//拍照

    /**
     * 跳转页面
     */
    public static void startActivity(Context context, java.lang.Class<?> cls) {
        if (context == null){
            return;
        }
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }
    public static void startActivity(Context context, java.lang.Class<?> cls, Bundle extras) {
        if (context == null){
            return;
        }
        Intent intent = new Intent(context, cls);
        if (extras != null){
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    public static void startActivityForResult(Activity context, Class<?> cls, int requestCode){
        if (context == null){
            return;
        }
        Intent intent = new Intent(context, cls);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 发送广播
     */
    public static void sendBroadcast(Context context, String action) {
        if (context == null){
            return;
        }
        Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    /**
     * 拨打电话<br>
     * pemission : {@link android.Manifest.permission#CALL_PHONE}<br>
     * <p/>
     * <p>
     * useage :
     * <uses-permission android:name="android.permission.CALL_PHONE"/>
     * </p>
     * <br>
     */
    public static void callPhone(Context context, String phoneNumber) {
        if (context == null || StringUtils.isEmpty(phoneNumber)) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ZLogger.wf("sorry!! please ensure you have granted [CALL_PHONE] perssion.");
            DialogUtil.showHint("请先在设置中开启应用的电话权限");
            return;
        }

        //用intent启动拨打电话
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
//        intent.setAction(Intent.ACTION_CALL);
//        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    /**
     * 打开浏览器
     *
     * @param context
     * @param url
     */
    public static void openBrowser(Context context, String url) {
        if (context == null || StringUtils.isEmpty(url)) {
            return;
        }

        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    /**
     * 选择图片
     */
    public static void selectPicture(final Activity context, String title) {
        if (context == null){
            return;
        }
        final CommonDialog dialog = DialogHelper.getPinterestDialogCancelable(context);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                if (id == R.id.tv_option_1) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    context.startActivityForResult(intent, Constants.REQUEST_CODE_XIANGCE);
                } else if (id == R.id.tv_option_2) {
                    CameraSessionUtil cameraUtil = ServiceFactory.getService(CameraSessionUtil.class.getName());
                    cameraUtil.makeCameraRequest(context);
                }
            }
        };

        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_select_picture, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(title);
        view.findViewById(R.id.tv_option_1).setOnClickListener(click);
        view.findViewById(R.id.tv_option_2).setOnClickListener(click);

        dialog.setContent(view);
        dialog.show();
    }
}

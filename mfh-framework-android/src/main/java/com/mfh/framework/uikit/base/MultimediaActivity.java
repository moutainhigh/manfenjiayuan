package com.mfh.framework.uikit.base;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.mfh.framework.BizConfig;
import com.mfh.framework.Constants;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.ImageUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.UIHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * 多媒体
 */
public class MultimediaActivity extends BaseActivity {
    private static final String TAG = "MultimediaActivity";

    private Uri cameraImageUri;
    private Uri cropUri;
    private String theLarge;
    private String protraitPath;
    private File protraitFile;
    private Bitmap protraitBitmap;


    /**
     * 是否裁剪相册/拍照文件,默认裁剪（true）
     */
    protected boolean isCropCameraFile() {
        return true;
    }
    /**
     * 上传裁剪后的相册/拍照文件
     */
    protected void uploadCropCameraFile(File file) {

    }

    /**
     * 上传原始相册/拍照文件
     */
    protected void uploadOriginalCameraFile(File file) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //相册
                case Constants.REQUEST_CODE_XIANGCE: {
                    if (data != null) {
                        processPhotographAlbumUri(data.getData());
                    }
                }
                break;
                //相机
                case Constants.REQUEST_CODE_CAMERA: {
                    processCameraUri(cameraImageUri);
                }
                break;
                //上传图像
                case Constants.REQUEST_CODE_CROP: {
                    processCropImage();
                }
                break;
                default:{

                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case UIHelper.PERMISSIONS_REQUEST_CAMERA
                    : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    ZLogger.d("permission was granted");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ZLogger.d("permission denied");
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * 选择图片
     *
     * @param context
     */
    public void startImagePick(Activity context) {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    Constants.REQUEST_CODE_XIANGCE);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            context.startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    Constants.REQUEST_CODE_XIANGCE);
        }
    }


    /**
     * 打开相机
     * java.lang.SecurityException: Permission Denial: starting Intent { act=android.media.action.IMAGE_CAPTURE flg=0x3 cmp=com.android.camera/.Camera clip={text/uri-list U:file:///storage/emulated/0/com.mfh.enjoycity/Camera/mfh_20160420102550.jpg} (has extras) } from ProcessRecord{e782737 5417:com.mfh.enjoycity/u0a444} (pid=5417, uid=10444) with revoked permission android.permission.CAMERA
     */
    public void startTakePhoto(Activity context, String savePath) {
        // Check for permission,
        // If the app has the permission, the app can proceed with the operation.
        // If the app does not have the permission, the app has to explicitly ask the user for permission.
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            // Request Permissions
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    android.Manifest.permission.CAMERA)) {
                //true -- if the app has requested this permission previously and the user denied the request.
                ZLogger.d("Show an expanation");
            } else {
                ZLogger.d("No explanation needed");

                ActivityCompat.requestPermissions(context,
                        new String[]{android.Manifest.permission.CAMERA},
                        UIHelper.PERMISSIONS_REQUEST_CALL_PHONE);
            }
            return;
        }
        // Camera permissions is already available, show the camera preview.
//        hasSystemFeature(PackageManager.FEATURE_CAMERA);

        try {
            if (FileUtil.IS_SDCARD_EXIST) {
                File savedir = new File(savePath);
                if (!savedir.exists()) {
                    savedir.mkdirs();
                }
            }
            // 没有挂载SD卡，无法保存文件
            else {
                DialogUtil.showHint("无法保存照片，请检查SD卡是否挂载");
                return;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
                    .format(new Date());
            String fileName = "mfh_" + timeStamp + ".jpg";// 照片命名
            File out = new File(savePath, fileName);
            Uri uri = Uri.fromFile(out);
            cameraImageUri = uri;

            theLarge = savePath + fileName;// 该照片的绝对路径

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                context.startActivityForResult(intent,
                        Constants.REQUEST_CODE_CAMERA);
            }
        } catch (Exception e) {
            ZLogger.e("startTakePhoto failed : " + e.toString());
        }

    }

    /**
     * 拍照后裁剪
     *
     * @param data   原始图片
     */
    private void startActionCrop(Uri data) {
        if (data == null){
            return;
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("output", getTempFile(data));
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ImageUtil.DEF_CROP);// 输出图片大小
        intent.putExtra("outputY", ImageUtil.DEF_CROP);
        intent.putExtra("scale", true);// 去黑边
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        startActivityForResult(intent,
                Constants.REQUEST_CODE_CROP);
    }

    /**
     * 裁剪头像的绝对路径
     */
    private Uri getTempFile(Uri uri) {
        if (FileUtil.IS_SDCARD_EXIST) {
            File savedir = new File(BizConfig.DEFAULT_SAVE_CROP_PATH);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        } else {
            DialogUtil.showHint("无法保存上传的头像，请检查SD卡是否挂载");
            return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
                .format(new Date());

        String thePath = ImageUtil.getAbsolutePathFromNoStandardUri(uri);
        // 如果是标准Uri
        if (StringUtils.isEmpty(thePath)) {
            thePath = ImageUtil.getAbsoluteImagePath(this, uri);
        }
        String ext = FileUtil.getFileFormat(thePath);
        ext = StringUtils.isEmpty(ext) ? "jpg" : ext;
        // 照片命名
        String cropFileName = "mfh_crop_" + timeStamp + "." + ext;
        // 裁剪头像的绝对路径
        protraitPath = BizConfig.DEFAULT_SAVE_CROP_PATH + cropFileName;
        protraitFile = new File(protraitPath);

        cropUri = Uri.fromFile(protraitFile);
        return this.cropUri;
    }

    /**
     * 处理相册图片
     */
    private void processPhotographAlbumUri(Uri uri) {
        if (uri == null){
            return;
        }

        if (isCropCameraFile()){
            startActionCrop(uri);// 裁剪图片
        }
        else{
            uploadOriginalCameraFile(ImageUtil.uriToCompressFile(MultimediaActivity.this, uri));
        }
    }

    /**
     * 处理拍照/相册图片
     */
    private void processCameraUri(Uri uri) {
        if (uri == null){
            return;
        }

        if (isCropCameraFile()){
            startActionCrop(uri);// 裁剪图片
        }
        else{
            uploadOriginalCameraFile(ImageUtil.uriToCompressFile(MultimediaActivity.this, uri));
        }
    }

    /**
     * 处理裁剪后的图片
     */
    private void processCropImage() {
        // 获取头像缩略图
        if (!StringUtils.isEmpty(protraitPath) && protraitFile.exists()) {
            protraitBitmap = ImageUtil.loadImgThumbnail(protraitPath, 200, 200);
        } else {
            ZLogger.d("图像不存在");
            return;
        }

        uploadCropCameraFile(protraitFile);
    }


}

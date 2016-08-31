package com.mfh.owner.ui.settings;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.camera.CameraSessionUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.ImageUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.owner.R;
import com.mfh.owner.utils.Constants;
import com.mfh.owner.utils.MobileURLConf;
import com.mfh.owner.utils.NetProxy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;


/**
 * 个人资料
 * */
public class UserProfileActivity extends BaseActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.item_avatar) AvatarSettingItem avatarItem;
    @Bind({ R.id.item_1_0, R.id.item_1_1}) List<SettingsItem> btnItems;

    private Uri origUri;
    private Uri cropUri;
    private String theLarge;

    private String protraitPath;
    private File protraitFile;
    private Bitmap protraitBitmap;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_profile;
    }

    /**
     * 初始化导航栏视图
     * */
    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle(R.string.topbar_title_settings_profile);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set an OnMenuItemClickListener to handle menu item clicks
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_more) {
//                    showSharePopup(toolbar);
//                }
//                return true;
//            }
//        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        avatarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateHeaderDialog(UserProfileActivity.this);
            }
        });

        btnItems.get(0).init(new SettingsItemData(R.drawable.material_housekeeping,
                getString(R.string.label_settings_nickname), ""));
        btnItems.get(0).setButtonType(SettingsItem.ThemeType.THEME_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP);
        btnItems.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, SettingFragmentActivity.class);
                intent.putExtra(SettingFragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, 0);
//                startActivity(intent);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CHANGE_NICKNAME);
            }
        });

        btnItems.get(1).init(new SettingsItemData(R.drawable.material_housekeeping, getString(R.string.label_settings_sex), ""));
        btnItems.get(1).setButtonType(SettingsItem.ThemeType.THEME_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM);
        btnItems.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateSexDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        switch (requestCode){
            case Constants.ACTIVITY_REQUEST_CHANGE_NICKNAME:
                btnItems.get(0).setDetailText(MfhLoginService.get().getHumanName());
                break;
            case CameraSessionUtil.REQUEST_CODE_XIANGCE://相册
                if(intent != null){
                    startActionCrop(intent.getData());// 选图后裁剪
                }
                break;
            case CameraSessionUtil.REQUEST_CODE_CAMERA://相机
                startActionCrop(origUri);// 拍照后裁剪
                break;
            case CameraSessionUtil.REQUEST_CODE_CROP:{
                uploadNewPhoto();//上传图像
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * 刷新数据
     * */
    private void refresh(){
        updateHeaderDisplay();
        btnItems.get(0).setDetailText(MfhLoginService.get().getHumanName());

        updateSexDisplay();
    }

    private void showUpdateSexDialog() {
        final CommonDialog dialog = DialogHelper
                .getPinterestDialogCancelable(this);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                switch (id) {
                    case R.id.tv_male:
                        updateSex("0");
                        break;
                    case R.id.tv_female:
                        updateSex("1");
                        break;
                    default:
                        break;
                }
            }
        };

        View view = LayoutInflater.from(this).inflate(
                R.layout.dialog_set_sex, null);
        view.findViewById(R.id.tv_male).setOnClickListener(click);
        view.findViewById(R.id.tv_female).setOnClickListener(click);

        dialog.setContent(view);
        dialog.show();
    }

    private void uploadNewPhoto(){
        // 获取头像缩略图
        if (!StringUtils.isEmpty(protraitPath) && protraitFile.exists()) {
            protraitBitmap = ImageUtil.loadImgThumbnail(protraitPath, 200, 200);
        } else {
            DialogUtil.showHint("图像不存在，上传失败");
            return;
        }

        if (protraitBitmap != null){
            try{
                if(!NetWorkUtil.isConnect(this)){
                    DialogUtil.showHint(getString(R.string.toast_network_error));
                    return;
                }

                NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                        NetProcessor.Processor<String>>(
                        new NetProcessor.Processor<String>() {
                            @Override
                            public void processResult(IResponseData rspData) {
                                RspValue<String> retValue = (RspValue<String>) rspData;
                                if(retValue != null){
                                    //upai://132660.JPEG
                                    String imageName = retValue.getValue();
                                    ZLogger.d("imageName = " + imageName);
                                    if(imageName.contains("http://")){
                                        MfhLoginService.get().updateHeadimage(imageName.replace("upai://", ""));
                                    }
                                    else{
                                        MfhLoginService.get().updateHeadimage(imageName.replace("upai://", "http://chunchunimage.b0.upaiyun.com/user/"));
//                                        MfhLoginService.get().updateHeadimage(imageName.replace("upai://", MobileURLConf.BASE_URL_RESOURCE));
                                    }

                                    avatarItem.setHeaderBitmap(protraitBitmap);
                                }

                                Message message = new Message();
                                message.what = MSG_UPDATE_HEADER_SUCCESS;
                                uiHandler.sendMessage(message);
                            }

//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        Log.d("Nat: updateProfile.processFailure", errMsg);
//                        DialogUtil.showHint("修改性别失败");
//                    }
                        }
                        , String.class
                        , MfhApplication.getAppContext())
                {};

                NetProxy.uploadUserHeader(MfhLoginService.get().getCurrentGuId(), protraitFile, responseCallback);
            } catch (Exception e) {

                ZLogger.e("uploadNewPhoto failed, " + e.toString());
                DialogUtil.showHint("上传失败");
            }
        }
    }
    /**
     * 修改头像
     * */
    private void updateHeader(File file){
        if(!NetWorkUtil.isConnect(this)){
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        if(retValue != null){
                            //upai://132660.JPEG
                            String imageName = retValue.getValue();
                            ZLogger.d("imageName = " + imageName);
                            if(imageName.contains("http://")){
                                MfhLoginService.get().updateHeadimage(imageName.replace("upai://", ""));
                            }
                            else{
                                MfhLoginService.get().updateHeadimage(MobileURLConf.BASE_URL_RESOURCE + imageName.replace("upai://", ""));
                            }

                            updateHeaderDisplay();
                        }

                        Message message = new Message();
                        message.what = MSG_UPDATE_HEADER_SUCCESS;
                        uiHandler.sendMessage(message);
                    }

//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        Log.d("Nat: updateProfile.processFailure", errMsg);
//                        DialogUtil.showHint("修改性别失败");
//                    }
                }
                , String.class
                , MfhApplication.getAppContext())
        {};

        try{
            NetProxy.uploadUserHeader(MfhLoginService.get().getCurrentGuId(), file, responseCallback);
        }
        catch (Exception e){
            DialogUtil.showHint("修改头像失败");
        }
    }

    /**
     * 修改性别
     * */
    private void updateSex(final String sex){
        if(!NetWorkUtil.isConnect(this)){
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                                    RspValue<String> retValue = (RspValue<String>) rspData;
//                                    if(retValue != null){
//                                        Log.d("Nat: updateUserPassword.response", retValue.getValue());
//                                    }

                        MfhLoginService.get().updateSex(sex);

                        Message message = new Message();
                        message.what = MSG_UPDATE_SEX_SUCCESS;
                        uiHandler.sendMessage(message);
                    }

//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        Log.d("Nat: updateProfile.processFailure", errMsg);
//                        DialogUtil.showHint("修改性别失败");
//                    }
                }
                , String.class
                , MfhApplication.getAppContext())
        {

        };

        JSONObject object = new JSONObject();
        object.put("id", MfhLoginService.get().getCurrentGuId());
        object.put("sex", sex);
        NetProxy.updateProfile(object.toJSONString(), responseCallback);
    }


    private final static int MSG_UPDATE_HEADER_SUCCESS = 1;
    private final static int MSG_UPDATE_SEX_SUCCESS = 0;
    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SEX_SUCCESS:
                    DialogUtil.showHint(getString(R.string.toast_change_sex_success));

                    updateSexDisplay();
                    break;
                case MSG_UPDATE_HEADER_SUCCESS:
                    DialogUtil.showHint(getString(R.string.toast_change_header_success));

                    //TODO
//                    updateSexDisplay();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void updateHeaderDisplay(){
        String url = MfhLoginService.get().getHeadimage();
        ZLogger.d("imageUrl = " + url);
//        if(TextUtils.isEmpty(url)){
//            Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.chat_tmp_user_head);
//            avatarItem.setHeaderBitmap(bmp);
//        }else{
//            avatarItem.setHeaderUrl(url);
//        }
        avatarItem.setHeaderUrl(url);
//        String imageUrl = MobileURLConf.BASE_URL_RESOURCE + MfhLoginService.get().getHeadimage();
//        Log.d("Nat", "imageUrl = " + imageUrl);
//        ImageLoader.getInstance().loadImage(imageUrl,
//                new SimpleImageLoadingListener() {
//                    @Override
//                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                                    // Do whatever you want with Bitmap
//                        avatarItem.setHeaderBitmap(loadedImage);
//                    }
//                });
    }

    private void updateSexDisplay(){
        switch(MfhLoginService.get().getSex()){
            case "0":
                btnItems.get(1).setDetailText("男");
                break;
            case "1":
                btnItems.get(1).setDetailText("女");
                break;
            default:
                btnItems.get(1).setDetailText("未知");
                break;
        }
    }


    /**
     * 选择图片裁剪
     *
     * @param context
     */
    private void startImagePick(Activity context) {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    CameraSessionUtil.REQUEST_CODE_XIANGCE);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            context.startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    CameraSessionUtil.REQUEST_CODE_XIANGCE);
        }
    }

    /**
     * 打开相机
     * */
    private void startTakePhoto(Activity context, String savePath){
        if (FileUtil.IS_SDCARD_EXIST) {
            File savedir = new File(savePath);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        }
        // 没有挂载SD卡，无法保存文件
        else{
            DialogUtil.showHint("无法保存照片，请检查SD卡是否挂载");
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date());
        String fileName = "mfh_" + timeStamp + ".jpg";// 照片命名
        File out = new File(savePath, fileName);
        Uri uri = Uri.fromFile(out);
        origUri = uri;

        theLarge = savePath + fileName;// 该照片的绝对路径

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        context.startActivityForResult(intent,
                CameraSessionUtil.REQUEST_CODE_CAMERA);
    }

    /**
     * 拍照后裁剪
     *
     * @param data
     *            原始图片
     * @param output
     *            裁剪后图片
     */
    private void startActionCrop(Uri data) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("output", this.getUploadTempFile(data));
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ImageUtil.DEF_CROP);// 输出图片大小
        intent.putExtra("outputY", ImageUtil.DEF_CROP);
        intent.putExtra("scale", true);// 去黑边
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        startActivityForResult(intent,
                CameraSessionUtil.REQUEST_CODE_CROP);
    }

    // 裁剪头像的绝对路径
    private Uri getUploadTempFile(Uri uri) {
        if (FileUtil.IS_SDCARD_EXIST) {
            File savedir = new File(BizConfig.DEFAULT_SAVE_CROP_PATH);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        } else {
            DialogUtil.showHint("无法保存上传的头像，请检查SD卡是否挂载");
            return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
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
     * 选择头像
     * */
    private void showUpdateHeaderDialog(final Activity context) {
        final CommonDialog dialog = DialogHelper
                .getPinterestDialogCancelable(context);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                switch (id) {
                    case R.id.tv_option_1:
                        startImagePick(context);
                        break;
                    case R.id.tv_option_2:
                        startTakePhoto(context, BizConfig.DEFAULT_SAVE_CAMERA_PATH);
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


}

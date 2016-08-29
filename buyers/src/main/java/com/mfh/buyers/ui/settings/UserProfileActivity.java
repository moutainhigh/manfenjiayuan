package com.mfh.buyers.ui.settings;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.buyers.R;
import com.mfh.buyers.utils.MobileURLConf;
import com.mfh.buyers.utils.NetProxy;
import com.mfh.buyers.utils.Constants;
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
import com.mfh.framework.uikit.base.MultimediaActivity;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;


/**
 * 个人资料
 * */
public class UserProfileActivity extends MultimediaActivity {
    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;

    @Bind(R.id.item_avatar) AvatarSettingItem avatarItem;
    @Bind({ R.id.item_1_0, R.id.item_1_1}) List<SettingsItem> btnItems;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTopBar();

        avatarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateHeaderDialog(UserProfileActivity.this);
            }
        });

        btnItems.get(0).init(new SettingsItemData(R.drawable.material_housekeeping, getString(R.string.label_settings_nickname), ""));
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


    /**
     * 刷新数据
     * */
    private void refresh(){
        updateHeaderDisplay();
        btnItems.get(0).setSubTitle(MfhLoginService.get().getHumanName());

        updateSexDisplay();
    }

    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
        tvTopBarTitle.setText(R.string.topbar_title_settings_profile);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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

    @Override
    protected void uploadCropCameraFile(File file) {
        super.uploadCropCameraFile(file);
        if(!NetWorkUtil.isConnect(this)){
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        try{
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
                btnItems.get(1).setSubTitle("男");
                break;
            case "1":
                btnItems.get(1).setSubTitle("女");
                break;
            default:
                btnItems.get(1).setSubTitle("未知");
                break;
        }
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

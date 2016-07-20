package com.mfh.enjoycity.ui.settings;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.AvatarSettingItem;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.enjoycity.AppContext;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.MultimediaActivity;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 个人资料
 * */
public class UserProfileActivity extends MultimediaActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    @Bind(R.id.item_avatar)
    AvatarSettingItem avatarItem;
    @Bind({ R.id.item_1_0, R.id.item_1_1}) List<SettingsItem> btnItems;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_profile;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.topbar_title_settings_profile);
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserProfileActivity.this.onBackPressed();
                    }
                });
        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        avatarItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });

        btnItems.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, SettingFragmentActivity.class);
                intent.putExtra(SettingFragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, 0);
//                startActivity(intent);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CHANGE_NICKNAME);
            }
        });
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
                btnItems.get(0).setSubTitle(MfhLoginService.get().getHumanName());
                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * 刷新数据
     * */
    private void refresh(){
        updateHeaderDisplay();
        btnItems.get(0).setSubTitle(MfhLoginService.get().getHumanName());

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

    /**
     * 修改头像
     * */
    @Override
    protected void uploadCropCameraFile(File file) {
        super.uploadCropCameraFile(file);

        if (file == null){
            return;
        }

        if(!NetWorkUtil.isConnect(AppContext.getAppContext())){
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

                                avatarItem.setHeaderUrl(MfhLoginService.get().getHeadimage());
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

            EnjoycityApiProxy.uploadUserHeader(MfhLoginService.get().getCurrentGuId(), file, responseCallback);
        } catch (Exception e) {

            ZLogger.e("uploadNewPhoto failed, " + e.toString());
            DialogUtil.showHint("上传失败");
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
        EnjoycityApiProxy.updateProfile(object.toJSONString(), responseCallback);
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
    @OnClick(R.id.item_avatar)
    public void showUpdateHeaderDialog() {
        final CommonDialog dialog = DialogHelper.getPinterestDialogCancelable(UserProfileActivity.this);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                dialog.dismiss();
                switch (id) {
                    case R.id.tv_option_1:
                        startImagePick(UserProfileActivity.this);
                        break;
                    case R.id.tv_option_2:
                        startTakePhoto(UserProfileActivity.this, BizConfig.DEFAULT_SAVE_CAMERA_PATH);
                        break;
                    default:
                        break;
                }
            }
        };

        View view = LayoutInflater.from(UserProfileActivity.this).inflate(
                R.layout.dialog_select_picture, null);
        view.findViewById(R.id.tv_option_1).setOnClickListener(click);
        view.findViewById(R.id.tv_option_2).setOnClickListener(click);

        dialog.setContent(view);
        dialog.show();
    }


}

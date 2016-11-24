package com.manfenjiayuan.mixicook_vip.ui.my;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.AvatarSettingItem;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.api.constant.Sex;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.MultimediaActivity;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 个人资料
 */
public class UserProfileActivity extends MultimediaActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.item_avatar)
    AvatarSettingItem avatarItem;
    @BindView(R.id.item_nickname)
    SettingsItem itemNickname;
    @BindView(R.id.item_sex)
    SettingsItem itemSex;


    private CommonDialog changeAvatarDialog = null;
    private CommonDialog changeSexDialog = null;


    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_profile;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle("个人资料");
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

//        switch (requestCode){
//            case Constants.ACTIVITY_REQUEST_CHANGE_NICKNAME:
//                btnItems.get(0).setSubTitle(MfhLoginService.get().getHumanName());
//                break;
//        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * 刷新数据
     */
    private void refresh() {
        updateHeaderDisplay();
        itemNickname.setSubTitle(MfhLoginService.get().getHumanName());

        updateSexDisplay();
    }

    @OnClick(R.id.item_nickname)
    public void changeNickName() {
        Bundle extras = new Bundle();
        extras.putString(FragmentActivity.EXTRA_TITLE, "修改昵称");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_CHANGE_NICKNAME);
        Intent intent = new Intent(this, FragmentActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 修改性别
     */
    @OnClick(R.id.item_sex)
    public void showUpdateSexDialog() {
        if (changeSexDialog == null) {
            changeSexDialog = DialogHelper
                    .getPinterestDialogCancelable(this);

            View.OnClickListener click = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    changeSexDialog.dismiss();
                    switch (id) {
                        case R.id.tv_male:
                            updateSex(Sex.MALE);
                            break;
                        case R.id.tv_female:
                            updateSex(Sex.FEMALE);
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

            changeSexDialog.setContent(view);
        }

        changeSexDialog.show();
    }

    /**
     * 修改性别
     */
    private void updateSex(final Integer sex) {
        if (!NetworkUtils.isConnect(this)) {
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
                , MfhApplication.getAppContext()) {

        };

        JSONObject object = new JSONObject();
        object.put("id", MfhLoginService.get().getCurrentGuId());
        object.put("sex", sex);
        UserApiImpl.updateProfile(object.toJSONString(), responseCallback);
    }

    private final static int MSG_UPDATE_HEADER_SUCCESS = 1;
    private final static int MSG_UPDATE_SEX_SUCCESS = 0;
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SEX_SUCCESS:
                    DialogUtil.showHint("修改性别成功");

                    updateSexDisplay();
                    break;
                case MSG_UPDATE_HEADER_SUCCESS:
                    DialogUtil.showHint("修改性别失败");

                    //TODO
//                    updateSexDisplay();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void updateSexDisplay() {
        itemSex.setSubTitle(Sex.formatName1(MfhLoginService.get().getSex()));
    }


    /**
     * 修改头像
     */
    @Override
    protected void uploadCropCameraFile(File file) {
        super.uploadCropCameraFile(file);

        if (file == null) {
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        try {
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            if (retValue != null) {
                                //upai://132660.JPEG
                                String imageName = retValue.getValue();
                                ZLogger.d("imageName = " + imageName);
                                if (imageName.contains("http://")) {
                                    MfhLoginService.get().updateHeadimage(imageName.replace("upai://", ""));
                                } else {
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
                    , MfhApplication.getAppContext()) {
            };

            UserApiImpl.uploadUserHeader(MfhLoginService.get().getCurrentGuId(),
                    file, responseCallback);
        } catch (Exception e) {

            ZLogger.e("uploadNewPhoto failed, " + e.toString());
            DialogUtil.showHint("上传失败");
        }
    }


    private void updateHeaderDisplay() {
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


    /**
     * 选择头像
     */
    @OnClick(R.id.item_avatar)
    public void showUpdateHeaderDialog() {
        if (changeAvatarDialog == null) {
            changeAvatarDialog = DialogHelper
                    .getPinterestDialogCancelable(UserProfileActivity.this);

            View.OnClickListener click = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    changeAvatarDialog.dismiss();
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

            changeAvatarDialog.setContent(view);
        }
        changeAvatarDialog.show();
    }


}

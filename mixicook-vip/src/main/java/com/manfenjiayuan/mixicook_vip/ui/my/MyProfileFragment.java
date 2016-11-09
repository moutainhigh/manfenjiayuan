package com.manfenjiayuan.mixicook_vip.ui.my;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.AvatarSettingItem;
import com.mfh.framework.api.account.UserApiImpl;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.Constants;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.Sex;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.ImageUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.DialogHelper;
import com.mfh.framework.uikit.widget.OnTabReselectListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 个人资料
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-04-13
 * @since bingshanguxue
 */
public class MyProfileFragment extends BaseFragment implements OnTabReselectListener {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.item_avatar)
    AvatarSettingItem avatarItem;
    @Bind(R.id.item_nickname)
    SettingsItem itemNickname;
    @Bind(R.id.item_sex)
    SettingsItem itemSex;


    public MyProfileFragment() {
        super();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_myprofile;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("个人资料");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        refresh(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        ZLogger.d(String.format("requestCode=%d, resultCode=%d, intent=%s",
                requestCode,
                resultCode,
                StringUtils.decodeBundle(intent != null ? intent.getExtras() : null)));
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        switch (requestCode){
//            case Constants.ACTIVITY_REQUEST_CHANGE_NICKNAME:
//                btnItems.get(0).setDetailText(MfhLoginService.get().getHumanName());
//                break;
            case Constants.REQUEST_CODE_XIANGCE://相册
                startActionCrop(intent.getData());// 选图后裁剪
                break;
            case Constants.REQUEST_CODE_CAMERA://相机
                startActionCrop(origUri);// 拍照后裁剪
                break;
            case Constants.REQUEST_CODE_CROP:{
                uploadNewPhoto();//上传图像
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private CommonDialog changeAvatarDialog = null;
    private Uri origUri;
    private Uri cropUri;
    private String theLarge;

    private String protraitPath;
    private File protraitFile;
    private Bitmap protraitBitmap;

    private void initAvatarDialog(final Activity context){
        changeAvatarDialog = DialogHelper
                .getPinterestDialogCancelable(context);

        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                changeAvatarDialog.dismiss();
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

        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.dialog_select_picture, null);
        view.findViewById(R.id.tv_option_1).setOnClickListener(click);
        view.findViewById(R.id.tv_option_2).setOnClickListener(click);

        changeAvatarDialog.setContent(view);
    }

    @OnClick(R.id.item_avatar)
    public void changeAvatar(){
        if (changeAvatarDialog == null){
            initAvatarDialog(getActivity());
        }
        changeAvatarDialog.show();
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
                Constants.REQUEST_CODE_CAMERA);
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
                Constants.REQUEST_CODE_CROP);
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
            thePath = ImageUtil.getAbsoluteImagePath(getActivity(), uri);
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
                if(!NetworkUtils.isConnect(AppContext.getAppContext())){
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

                                // TODO: 7/14/16
//                                Message message = new Message();
//                                message.what = MSG_UPDATE_HEADER_SUCCESS;
//                                uiHandler.sendMessage(message);
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

                UserApiImpl.uploadUserHeader(MfhLoginService.get().getCurrentGuId(),
                        protraitFile, responseCallback);
            } catch (Exception e) {

                ZLogger.e("uploadNewPhoto failed, " + e.toString());
                DialogUtil.showHint("上传失败");
            }
        }
    }

    /**
     * 加载用户数据
     */
    private void refresh(boolean isAutoReload) {
        avatarItem.setHeaderUrl(MfhLoginService.get().getHeadimage());
        itemNickname.setSubTitle(MfhLoginService.get().getHumanName());
        itemSex.setSubTitle(Sex.formatName1(MfhLoginService.get().getSex()));
    }


    @Override
    public void onTabReselect() {
        refresh(true);
    }

}

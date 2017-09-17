package com.manfenjiayuan.mixicook_vip.wxapi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.jabra.utils.EmptyUtil;
import com.jabra.utils.SPFile;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.record.RecordCompleteListener;
import com.manfenjiayuan.mixicook_vip.record.Recorder;
import com.manfenjiayuan.mixicook_vip.service.MainService;
import com.manfenjiayuan.mixicook_vip.service.MyAccesibilityService;
import com.manfenjiayuan.mixicook_vip.wxapi.bean.Message;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.base.BaseActivity;
//import com.tencent.mm.opensdk.diffdev.DiffDevOAuthFactory;
//import com.tencent.mm.opensdk.diffdev.IDiffDevOAuth;
//import com.tencent.mm.opensdk.diffdev.OAuthErrCode;
//import com.tencent.mm.opensdk.diffdev.OAuthListener;
import com.tencent.mm.sdk.ext.MMOpenApiCaller;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.WXHelper;
import net.sourceforge.simcpux.wxapi.Constants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by Administrator on 2014/9/18.
 * 微信分享接口返回响应结果时调用这个类
 */
public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    @BindView(R.id.qrcode_iv)
    ImageView qrcodeIv;
    @BindView(R.id.qrcode_status_tv)
    TextView qrcodeStatusTv;
    @BindView(R.id.sign_et)
    EditText signEt;

    private Recorder mRecorder;

    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    private SPFile mSPFile;
    private boolean isAbandonAuthrized;


    @Override
    protected int getLayoutResId() {
        return R.layout.scan_qrcode_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSPFile = new SPFile(this, "APP_CONFIG");
        if (mSPFile != null) {
            mSPFile.put("KEY_AUTO_PLAY_MESSAGE", true);
        }

//        oauth = DiffDevOAuthFactory.getDiffDevOAuth();

        ZLogger.d("createWXAPI...");
        //register to wx
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        api.registerApp(Constants.APP_ID);
        api.handleIntent(getIntent(), this);

        if (!MainService.IS_RUNNING) {
            ZLogger.w("MainService is not running");
        } else {
            ZLogger.d("start MainService...");
            startService(new Intent(this, MainService.class));

            registerReceiver();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 微信发送请求到第三方应用时，会回调到该方法
     */
    @Override
    public void onReq(BaseReq req) {
        ZLogger.d("openid = " + req.transaction);
//        switch (req.getType()) {
//            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
////                goToGetMsg();
//                break;
//            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
////                goToShowMsg((ShowMessageFromWX.Req) req);
//                break;
//            case ConstantsAPI.COMMAND_LAUNCH_BY_WX:
//                DialogUtil.showHint("微信登录成功");
//                break;
//            default:
//                break;
//        }
    }


    /**
     * 发送到微信请求的响应结果
     * <p>
     * （1）用户同意授权后得到微信返回的一个code，将code替换到请求地址GetCodeRequest里的CODE，同样替换APPID和SECRET
     * （2）将新地址newGetCodeRequest通过HttpClient去请求，解析返回的JSON数据
     * （3）通过解析JSON得到里面的openid （用于获取用户个人信息）还有 access_token
     * （4）同样地，将openid和access_token替换到GetUnionIDRequest请求个人信息的地址里
     * （5）将新地址newGetUnionIDRequest通过HttpClient去请求，解析返回的JSON数据
     * （6）通过解析JSON得到该用户的个人信息，包括unionid
     */
    @Override
    public void onResp(BaseResp resp) {
        ZLogger.d(String.format("errStr = %s, errCode=%d, type=%d",
                resp.errStr, resp.errCode, resp.getType()));
        int result = 0;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK: {
                result = R.string.errcode_success;
                ZLogger.d(getString(result) + resp.getType());
                DialogUtil.showHint(result);

                if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
                    ZLogger.d("同意授权");
                    try {
                        if (mSPFile != null) {
                            mSPFile.put("KEY_WECHAT_AUTHORIZED", true);
                        }

                        SendAuth.Resp respAuth = (SendAuth.Resp) resp;
                        ZLogger.d(String.format("token = %s, state=%s, resultUrl=%s",
                                respAuth.token, respAuth.state, respAuth.resultUrl));
                        getAccessToken(respAuth.token);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ZLogger.e(e.toString());
                    }
                } else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
//                    finish();
                } else {
//                    finish();
                }
            }
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                ZLogger.d(getString(result));
                DialogUtil.showHint(result);
//                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                ZLogger.d(getString(result));
                DialogUtil.showHint(result);
//                finish();
                break;
            default:
                result = R.string.errcode_unknown;
                ZLogger.d(getString(result));
                DialogUtil.showHint(result);
//                finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZLogger.d("onDestroy");
//        if (oauth != null) {
//            oauth.removeAllListeners();
//            oauth.detach();
//        }
    }

//    @Override
//    public void onAuthGotQrcode(String qrcodeImgPath, byte[] imgBuf) {
//        Toast.makeText(this, "onAuthGotQrcode, img path:" + qrcodeImgPath, Toast.LENGTH_SHORT).show();
//
//        Bitmap bmp = BitmapFactory.decodeFile(qrcodeImgPath);
//        if (bmp == null) {
//            ZLogger.e("onAuthGotQrcode, decode bitmap is null");
//            return;
//        }
//
//        qrcodeIv.setImageBitmap(bmp); // չʾ��ά��
//        qrcodeIv.setVisibility(View.VISIBLE);
//
//        qrcodeStatusTv.setText(R.string.qrcode_wait_for_scan);
//        qrcodeStatusTv.setVisibility(View.VISIBLE);
//    }

//    @Override
//    public void onQrcodeScanned() {
//        Toast.makeText(this, "onQrcodeScanned", Toast.LENGTH_SHORT).show();
//
//        qrcodeStatusTv.setText(R.string.qrcode_scanned);
//        qrcodeStatusTv.setVisibility(View.VISIBLE);
//    }

//    @Override
//    public void onAuthFinish(OAuthErrCode errCode, String authCode) {
//        ZLogger.d(errCode.toString() + ", authCode = " + authCode);
//
//        String tips = null;
//        switch (errCode) {
//            case WechatAuth_Err_OK:
//                tips = getString(R.string.result_succ, authCode);
//                break;
//            case WechatAuth_Err_NormalErr:
//                tips = getString(R.string.result_normal_err);
//                break;
//            case WechatAuth_Err_NetworkErr:
//                tips = getString(R.string.result_network_err);
//                break;
//            case WechatAuth_Err_JsonDecodeErr:
//                tips = getString(R.string.result_json_decode_err);
//                break;
//            case WechatAuth_Err_Cancel:
//                tips = getString(R.string.result_user_cancel);
//                break;
//            case WechatAuth_Err_Timeout:
//                tips = getString(R.string.result_timeout_err);
//                break;
//            default:
//                break;
//        }
//
//        Toast.makeText(this, tips, Toast.LENGTH_LONG).show();
//
//        qrcodeIv.setVisibility(View.GONE);
//        qrcodeStatusTv.setVisibility(View.GONE);
//    }

    @OnClick(R.id.start_oauth_btn)
    public void startOauthLogin() {
        WXHelper.getInstance().sendAuthReq();
    }

    @OnClick(R.id.start_qrcode_btn)
    public void startQRLogin() {
//		accessToken("001YuKOI0M6J9k2BtIMI02OSOI0YuKOr");
//		oauth.stopAuth();
//		//boolean authRet = oauth.auth(APP_ID, "snsapi_userinfo", "helloworld", MainAct.this);
//
//		boolean authRet = oauth.auth(Constants.APP_ID, //Ӧ��Ψһ��ʶ
//				"snsapi_login", //Ӧ����Ȩ����������ж�����ö���(,)�ָ�
//				WXUtil.genNonceStr(), //�����
//				genTimestamp(), //ʱ���
//				Constants.APP_SIGNATURE, //ǩ��
//				ScanQRCodeLoginActivity.this); // ��Ȩ��ɻص��ӿڣ�OAuthListener��
//
//		ZLogger.d("authRet = " + authRet);

        MMOpenApiCaller.MMResult mmResult = MMOpenApiCaller.getUnReadMsg(this, Constants.APP_ID, 10, "");
        if (mmResult == null) {
            ZLogger.w("cannot find unread message");
        } else {
            List<Message> messages = (List<Message>) mmResult.data;
            ZLogger.d("retCode=" + mmResult.retCode);
            if (messages == null) {
                ZLogger.w("messages is null");
            } else {
                for (Message m : messages) {
                    ZLogger.d(m.toString());
                }
            }
        }
        MMOpenApiCaller.registerMsgListener(this, Constants.APP_ID, 1, 2, 6, 2);
    }

    @OnClick(R.id.stop_oauth_btn)
    public void stopQRLogin() {
//        boolean cancelRet = oauth.stopAuth();
//        DialogUtil.showHint("cancel ret = " + cancelRet);
    }

    @OnClick(R.id.btn_jumpToChattingUI)
    public void jumpToChattingUi() {
        MMOpenApiCaller.jumpToChattingUI(this, Constants.APP_ID, null);
    }

    private String genNonceStr() {
//		Random r = new Random(System.currentTimeMillis());
//		return MD5.getMessageDigest((Constants.APP_ID + r.nextInt(10000) + System.currentTimeMillis()).getBytes());
        return "noncestr";
    }

    private String genTimestamp() {
        return System.currentTimeMillis() + "";
//		return "timestamp";
    }

    /**
     * 通过code获取授权口令access_token
     */
    private void accessToken(String code) {
        try {
            Map<String, String> options = new HashMap<>();
            options.put("appid", Constants.APP_ID);
            options.put("secret", Constants.APP_SECRET);
            options.put("code", code);
            options.put("grant_type", "authorization_code");

            WxHttpManager.getInstance().accessToken(options,
                    new Subscriber<WxAccessToken>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            ZLogger.e(e.toString());
//                        finish();
                        }

                        @Override
                        public void onNext(WxAccessToken wxAccessToken) {
                            if (wxAccessToken != null) {
                                ZLogger.d(JSONObject.toJSONString(wxAccessToken));
                            } else {
                                ZLogger.w("no access token granted.");
                            }
//                        finish();
                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @OnClick(R.id.btn_recorder)
    public void recorder() {
        // TODO: 09/08/2017 权限检查
        if (mRecorder == null) {
            mRecorder = new Recorder();
        }
        mRecorder.start(new RecordCompleteListener() {
            @Override
            public void onComplete(String path) {
//                WXHelper.getInstance().sendFileToWX(path, "录音", SendMessageToWX.Req.WXSceneSession);
                ZLogger.d("准备发送语音到微信");
                MMVoiceSendManager.getInstance().step1(path);
            }

            @Override
            public void onError(int code) {

            }
        });
    }

    @OnClick(R.id.btn_open_weixin)
    public void openWeixin() {
        WXHelper.getInstance().openWXApp();
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.transmit.SelectConversationUI"));
//        startActivity(intent);
    }

    @OnClick(R.id.button_check_service)
    public void checkService() {
        if (isAccessibilitySettingsOn(this)) {
            DialogUtil.showHint("辅助功能已经开启");
        } else {
            DialogUtil.showHint("辅助功能未开启");
        }
    }

    @OnClick(R.id.button_set_service)
    public void setService() {
        if (isAccessibilitySettingsOn(this)) {
            DialogUtil.showHint("辅助功能已经开启");
            return;
        }

//        Settings.Secure.putInt(getContext().getContentResolver(),
//                android.provider.Settings.Secure.ACCESSIBILITY_ENABLED, 1);

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$AccessibilitySettingsActivity"));
        intent.setData(Uri.parse("package:" + getPackageName()));

        try {
            ResolveInfo res = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (res != null) {
                startActivity(intent);
            } else {
                ZLogger.d("cannot resolve permission activity");
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    //判断服务是否打开
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccesibilityService.class.getCanonicalName();
        try {
            ZLogger.v("service = " + service);
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            ZLogger.v("accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            ZLogger.e("Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            ZLogger.v("***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    ZLogger.v("-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        ZLogger.v("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            ZLogger.v("***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    /**
     * 通过code获取授权口令access_token
     *
     * @param code code的超时时间为10分钟，一个code只能成功换取一次access_token即失效。code的临时性和一次保障了微信授权登录的安全性。第三方可通过使用https和state参数，进一步加强自身授权登录的安全性。
     */
    private void getAccessToken(String code) {
        Map<String, String> options = new HashMap<>();
        options.put("appid", Constants.APP_ID);
        options.put("secret", Constants.APP_SECRET);
        options.put("code", code);
        options.put("grant_type", "authorization_code");

//        WxHttpManager.getInstance().accessToken(options,
//                new Subscriber<WxAccessToken>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.e(e.toString());
////                        finish();
//                    }
//
//                    @Override
//                    public void onNext(WxAccessToken wxAccessToken) {
//                        if (wxAccessToken != null) {
//                            ZLogger.d(JSONObject.toJSONString(wxAccessToken));
//                            getUserInfo(wxAccessToken.getAccess_token(), wxAccessToken.getOpenid());
//                        } else {
//                            ZLogger.w("no access token granted.");
//                        }
////                        finish();
//                    }
//
//                });
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + Constants.APP_ID +
                "&secret=" + Constants.APP_SECRET +
                "&code=" + code +
                "&grant_type=authorization_code";
        ZLogger.d(url);
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    if (response != null) {
                        ZLogger.d(response);
                        Intent localIntent = new Intent(WXEntryActivity.this, MainService.class);
                        localIntent.putExtra("EXTRA_GET_OPENID_SUCCEED", true);
                        startService(localIntent);

                        WxAccessToken wxAccessToken = JSONObject.parseObject(response, WxAccessToken.class);
                        ZLogger.d("getAccessToken: " + JSONObject.toJSONString(wxAccessToken));
                        getUserInfo(wxAccessToken.getAccess_token(), wxAccessToken.getOpenid());
                    } else {
                        ZLogger.w("no access token granted.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ZLogger.e(e.toString());
                }

            }

            @Override
            public void onError(Exception e) {
                if (mSPFile != null) {
                    mSPFile.put("KEY_WECHAT_AUTHORIZED", false);
                }
                ZLogger.e(e.toString());
                DialogUtil.showHint(e.getMessage());
            }
        });
    }

    /**
     * 通过code获取授权口令access_token
     *
     * @param accessToken 调用凭证
     * @param openid      普通用户的标识，对当前开发者帐号唯一
     */
    private void getUserInfo(final String accessToken, String openid) {
        Map<String, String> options = new HashMap<>();
        options.put("access_token", accessToken);
        options.put("openid", openid);

//        WxHttpManager.getInstance().userinfo(options,
//                new Subscriber<WxAccessToken>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        ZLogger.e(e.toString());
////                        finish();
//                    }
//
//                    @Override
//                    public void onNext(WxAccessToken wxAccessToken) {
//                        if (wxAccessToken != null) {
//                            ZLogger.d(JSONObject.toJSONString(wxAccessToken));
//                        } else {
//                            ZLogger.w("no access token granted.");
//                        }
////                        finish();
//                    }
//
//                });

        String url = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + accessToken +
                "&openid=" + openid;

        ZLogger.d(url);
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    if (response != null) {
                        ZLogger.d(response);

                        WxUserInfo wxUserInfo = JSONObject.parseObject(response, WxUserInfo.class);
                        if (wxUserInfo != null) {
                            ZLogger.d("getUserInfo: " + JSONObject.toJSONString(wxUserInfo));
                            MMVoiceSendManager.getInstance().accessToken = accessToken;
                            MMVoiceSendManager.getInstance().fromUserId = wxUserInfo.getOpenid();
                        } else {
                            ZLogger.w("find no userInfo.");
                        }
                    } else {
                        ZLogger.w("no userinfo granted.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ZLogger.e(e.toString());
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                ZLogger.e(e.toString());

            }
        });
    }

    private BluetoothProfile.ServiceListener headsetListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int paramAnonymousInt, BluetoothProfile paramAnonymousBluetoothProfile) {
            List localList = paramAnonymousBluetoothProfile.getConnectedDevices();
            ZLogger.e("onServiceConnected deviceList.SIZE:" + localList.size());
            if (EmptyUtil.isEmpty(localList)) {
//                WXEntryActivity.this.mDevice = null;
//                WXEntryActivity.this.setState(true, 4);
//                WXEntryActivity.this.setState(true, 2);
            }
            BluetoothDevice localBluetoothDevice;
            do {
                Iterator localIterator = null;
                while (!localIterator.hasNext()) {
//                    WXEntryActivity.this.updateUI();
                    BluetoothAdapter.getDefaultAdapter().closeProfileProxy(1, paramAnonymousBluetoothProfile);
//                    return;
                    localIterator = localList.iterator();
                }
                localBluetoothDevice = (BluetoothDevice) localIterator.next();
            }
            while ((!localBluetoothDevice.getName().startsWith("JABRA")) && (!localBluetoothDevice.getName().startsWith("Jabra")));
//            WXEntryActivity.this.mDevice = localBluetoothDevice;
//            WXEntryActivity.this.setState(false, 4);
            AudioManager localAudioManager = (AudioManager) WXEntryActivity.this.getSystemService(Context.AUDIO_SERVICE);
            WXEntryActivity localWXEntryActivity = WXEntryActivity.this;
            boolean bool1 = localAudioManager.isBluetoothA2dpOn();
            boolean bool2 = false;
            if (bool1) {
            }
            for (; ; ) {
//                localWXEntryActivity.setState(bool2, 2);
                break;
//                bool2 = true;
            }
        }

        public void onServiceDisconnected(int paramAnonymousInt) {
            ZLogger.d("onServiceDisconnected");
//            WXEntryActivity.this.setState(true, 4);
//            WXEntryActivity.this.setState(true, 2);
//            WXEntryActivity.this.mDevice = null;
//            WXEntryActivity.this.updateUI();
        }
    };

    private void registerReceiver() {
        ZLogger.d("registerReceiver...");

//        IntentFilter localIntentFilter = new IntentFilter();
//        localIntentFilter.addAction("com.xpg.jabra.network.change");
//        localIntentFilter.addAction("com.xpg.jabra.hs.connect.change");
//        localIntentFilter.addAction("com.xpg.jabra.hs.a2dp.change");
//        localIntentFilter.addAction("com.xpg.jabra.wechat.authorize");
//        localIntentFilter.addAction("com.xpg.jabra.wechat.not.ready");
//        registerReceiver(WXEntryActivity.this, localIntentFilter);
    }
}


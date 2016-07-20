package com.mfh.owner.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.owner.R;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by Administrator on 2014/9/18.
 * 微信分享接口返回响应结果时调用这个类
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = WXEntryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //register to wx
        IWXAPI api = WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false);
        api.registerApp(WXConstants.APP_ID);

        api.handleIntent(getIntent(), this);
    }

    /**
     * 微信发送的请求将回调到onReq方法
     * */
    @Override
    public void onReq(BaseReq req){
    }

    /**
     * 发送到微信请求的响应结果将回调到onResp方法
     * */
    @Override
    public void onResp(BaseResp resp){
        int result = 0;
        ZLogger.d(String.format("onResp, errCode=%d", resp.errCode));
        switch (resp.errCode)
        {
        case BaseResp.ErrCode.ERR_OK:
            result = R.string.errcode_success;
            if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH){
                SendAuth.Resp respAuth = (SendAuth.Resp)resp;
                WeiXinAuthService weiXinAuthService = ServiceFactory.getService(WeiXinAuthService.class,
                        this);
                weiXinAuthService.queryAccessToken(respAuth.code);
            }else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX){

            }
            finish();
            return;
        case BaseResp.ErrCode.ERR_USER_CANCEL:
            result = R.string.errcode_cancel;
            break;
        case BaseResp.ErrCode.ERR_AUTH_DENIED:
            result = R.string.errcode_deny;
            break;
        default:
            result = R.string.errcode_unknown;
            break;
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        finish();
    }

}


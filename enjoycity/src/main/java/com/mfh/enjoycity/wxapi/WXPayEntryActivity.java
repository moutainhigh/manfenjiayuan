package com.mfh.enjoycity.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.mfh.enjoycity.AppHelper;
import com.mfh.enjoycity.R;
import com.mfh.framework.core.logger.ZLogger;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler, View.OnTouchListener{

	private static final String TAG = WXPayEntryActivity.class.getSimpleName();
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wx_pay_result);

    	api = WXAPIFactory.createWXAPI(this, WXConstants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		ZLogger.d(String.format("onPayFinish, type= %d, errCode= %d", resp.getType(), resp.errCode));

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			//sendBroadcast通知处理结果
//			Intent intent = new Intent(WXConstants.ACTION_WXPAY_RESP);
////			Intent intent = new Intent();
////			intent.setAction(WXConstants.ACTION_WXPAY_RESP);
//			intent.putExtra(WXConstants.EXTRA_KEY_ERR_CODE, resp.errCode);
//			intent.putExtra(WXConstants.EXTRA_KEY_ERR_STR, resp.errStr);
//			ComnApplication.getAppContext().sendBroadcast(intent);

			AppHelper.broadcastWXPayResp(resp.errCode, resp.errStr);
//			if(RechargeActivity.getInstance() != null){
//				RechargeActivity.getInstance().parseWxpayResp(resp.errCode, resp.errStr);
//			}
//			switch(resp.errCode){
//				//成功，展示成功页面
//				case 0:{
//					//如果支付成功则去后台查询支付结果再展示用户实际支付结果。注意一定不能以客户端
//					// 返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
//					DialogUtil.showHint("微信充值成功");
//				}
//				//错误，可能的原因：签名错误、未注册APPID、项
//					//TODO,查询支付结果目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
//				case -1:{
//					DialogUtil.showHint("微信充值失败: " + resp.errStr);
//				}
//				//用户取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
//				case -2:{
//					DialogUtil.showHint("取消微信充值");
//				}
//			}
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle("提示");
//			builder.setMessage(getString(R.string.wx_pay_result_callback_msg,
//					resp.errStr + ";code=" + String.valueOf(resp.errCode)));
//			builder.show();
		}
		//返回APP页面
		finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//dismiss activity on tab
		finish();
		return false;
	}
}
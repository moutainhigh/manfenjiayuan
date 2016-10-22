package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{

    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wx_pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
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
		ZLogger.d("onPayFinish, errCode = " + resp.errCode);

		//支付完成后，微信APP会返回到商户APP并回调onResp函数
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			//判断返回错误码，如果支付成功则去后台查询支付结果再展示用户实际支付结果。
			//注意一定不能以客户端返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
//			0	成功	展示成功页面
//			-1	错误	可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
//			-2	用户取消	无需处理。发生场景：用户不支付了，点击取消，返回APP。

			// TODO: 14/10/2016 页面跳转
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage(getString(R.string.wepay_result_callback_msg, String.valueOf(resp.errCode)));
			builder.show();


			//sendBroadcast通知处理结果
//			Intent intent = new Intent(WXConstants.ACTION_WXPAY_RESP);
////			Intent intent = new Intent();
////			intent.setAction(WXConstants.ACTION_WXPAY_RESP);
//			intent.putExtra(WXConstants.EXTRA_KEY_ERR_CODE, resp.errCode);
//			intent.putExtra(WXConstants.EXTRA_KEY_ERR_STR, resp.errStr);
//			ComnApplication.getAppContext().sendBroadcast(intent);

//			AppHelper.broadcastWXPayResp(resp.errCode, resp.errStr);
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
		}
	}


//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		//dismiss activity on tab
//		finish();
//		return false;
//	}
}
package net.sourceforge.simcpux;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.wxapi.Constants;

public class AppRegister extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// IWXAPI 是第三方app和微信通信的openapi接口
		final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);

		// 将该app注册到微信
		msgApi.registerApp(Constants.APP_ID);
		ZLogger.d(String.format("注册app(%s)到微信", Constants.APP_ID));
	}
}

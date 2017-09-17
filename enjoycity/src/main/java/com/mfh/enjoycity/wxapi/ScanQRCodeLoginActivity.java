package com.mfh.enjoycity.wxapi;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mfh.enjoycity.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.tencent.mm.opensdk.diffdev.DiffDevOAuthFactory;
import com.tencent.mm.opensdk.diffdev.IDiffDevOAuth;
import com.tencent.mm.opensdk.diffdev.OAuthErrCode;
import com.tencent.mm.opensdk.diffdev.OAuthListener;


import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;


public class ScanQRCodeLoginActivity extends Activity implements OAuthListener{

	private IDiffDevOAuth oauth;
	
	private ImageView qrcodeIv;
	private TextView qrcodeStatusTv;
	@BindView(R.id.sign_et)
	EditText signEt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_qrcode_login);

		ButterKnife.bind(this);
		
		oauth = DiffDevOAuthFactory.getDiffDevOAuth();
		
		qrcodeIv = (ImageView) findViewById(R.id.qrcode_iv);
		qrcodeStatusTv = (TextView) findViewById(R.id.qrcode_status_tv);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		oauth.removeAllListeners();
		oauth.detach();
	}

	@Override
	public void onAuthGotQrcode(String qrcodeImgPath, byte[] imgBuf) {
		Toast.makeText(this, "onAuthGotQrcode, img path:" + qrcodeImgPath, Toast.LENGTH_SHORT).show();
		
		Bitmap bmp = BitmapFactory.decodeFile(qrcodeImgPath);
		if (bmp == null) {
			ZLogger.e("onAuthGotQrcode, decode bitmap is null");
			return;
		}

		qrcodeIv.setImageBitmap(bmp); // չʾ��ά��
		qrcodeIv.setVisibility(View.VISIBLE);
		
		qrcodeStatusTv.setText(R.string.qrcode_wait_for_scan);
		qrcodeStatusTv.setVisibility(View.VISIBLE);
	}

	@Override
	public void onQrcodeScanned() {
		Toast.makeText(this, "onQrcodeScanned", Toast.LENGTH_SHORT).show();

		qrcodeStatusTv.setText(R.string.qrcode_scanned);
		qrcodeStatusTv.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAuthFinish(OAuthErrCode errCode, String authCode) {
		ZLogger.d(errCode.toString() + ", authCode = " + authCode);
		
		String tips = null;
		switch (errCode) {
		case WechatAuth_Err_OK:
			tips = getString(R.string.result_succ, authCode);
			break;
		case WechatAuth_Err_NormalErr:
			tips = getString(R.string.result_normal_err);
			break;
		case WechatAuth_Err_NetworkErr:
			tips = getString(R.string.result_network_err);
			break;
		case WechatAuth_Err_JsonDecodeErr:
			tips = getString(R.string.result_json_decode_err);
			break;
		case WechatAuth_Err_Cancel:
			tips = getString(R.string.result_user_cancel);
			break;
		case WechatAuth_Err_Timeout:
			tips = getString(R.string.result_timeout_err);
			break;
		default:
			break;
		}
		
		Toast.makeText(this, tips, Toast.LENGTH_LONG).show();
		
		qrcodeIv.setVisibility(View.GONE);
		qrcodeStatusTv.setVisibility(View.GONE);
	}

	@OnClick(R.id.start_oauth_btn)
	public void startOauthLogin() {
		WXHelper.getInstance().sendAuthReq();
	}

	@OnClick(R.id.start_qrcode_btn)
	public void startQRLogin() {
		accessToken("001YuKOI0M6J9k2BtIMI02OSOI0YuKOr");
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
	}

	@OnClick(R.id.stop_oauth_btn)
	public void stopQRLogin() {
		boolean cancelRet = oauth.stopAuth();
		DialogUtil.showHint("cancel ret = " + cancelRet);
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
			options.put("appid", WXConstants.APP_ID);
			options.put("secret", WXConstants.APP_SECRET);
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
}

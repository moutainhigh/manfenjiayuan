package com.mfh.framework.core.qrcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.mfh.framework.R;
import com.mfh.framework.core.qrcode.camera.CameraManager;
import com.mfh.framework.core.qrcode.decoding.CaptureActivityHandler;
import com.mfh.framework.core.qrcode.decoding.InactivityTimer;
import com.mfh.framework.core.qrcode.view.ViewfinderView;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;

import java.io.IOException;
import java.util.Vector;

/**
 * 扫一扫
 * @author Ryan.Tang
 */
public class ScanActivity extends BaseActivity implements Callback {
	private Toolbar toolbar;

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;

	private ViewfinderView viewfinderView;
	private ImageView ivFlash;

	private boolean flashEnable;

	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private QRBeepManager beepManager;

	@Override
	public int getLayoutResId() {
		return R.layout.activity_qrcode;
	}

	@Override
	protected void initViews() {
		super.initViews();

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		ivFlash = (ImageView) findViewById(R.id.ivFlash);
	}

//	@Override
//	protected boolean isFullscreenEnabled() {
//		return true;
//	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
		CameraManager.init(getApplication());

		ivFlash.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (flashEnable) {
					flashEnable = false;
					// 开闪光灯
					CameraManager.get().openLight();
					ivFlash.setImageResource(R.drawable.ic_flashlight_on);
				} else {
					flashEnable = true;
					// 关闪光灯
					CameraManager.get().offLight();
					ivFlash.setImageResource(R.drawable.ic_flashlight_off);
				}
			}
		});
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		beepManager = new QRBeepManager(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
//			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}

		beepManager.close();
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * 初始化导航栏视图
	 * */
	@Override
	protected void initToolBar() {
		super.initToolBar();

		toolbar.setTitle(R.string.topbar_title_qrcode);//必须在setSupportActionBar(toolbar);之前设置才有效
		//import that this is set first
		setSupportActionBar(toolbar);

		toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
		toolbar.setNavigationOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ScanActivity.this.onBackPressed();
					}
				});
	}

	/**
	 * 处理扫描结果
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		beepManager.playBeepSoundAndVibrate();
		String resultString = result.getText();
		if (StringUtils.isEmpty(resultString)) {
			DialogUtil.showHint("扫描失败!");
		}else {
            //java.lang.SecurityException: Unable to find app for caller android.app.ApplicationThreadProxy
            try{
                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("result", resultString);
//                bundle.putParcelable("bitmap", barcode);
                resultIntent.putExtras(bundle);
                setResult(RESULT_OK, resultIntent);
            }
            catch(Exception ex){
                Log.e("Nat", ex.toString());
            }
		}
		finish();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(ScanActivity.this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

}
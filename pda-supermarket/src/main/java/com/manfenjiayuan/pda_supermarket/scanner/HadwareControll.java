package com.manfenjiayuan.pda_supermarket.scanner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.android.BarcodeJNI;
import com.mfh.framework.core.logger.ZLogger;

import java.io.UnsupportedEncodingException;


public class HadwareControll implements BarcodeJNI.ScanCallBack {

	public Handler m_handler = null;
	static public final int BARCODE_READ = 10;
	private static Handler mHandler = null;
	private BarcodeJNI barcodeControll;// = new BarcodeJNI(this);


	public HadwareControll(Context context){
		barcodeControll = new BarcodeJNI(context);
		barcodeControll.setmScanCB(this);
	}
	
	
	public void Open() {
		barcodeControll.Barcode_open();

	}

	public void Close() {
		barcodeControll.Barcode_Close();
	}

	public void scan_start() {
		barcodeControll.Barcode_StartScan();

	}

	

	public void scan_stop() {
		barcodeControll.Barcode_StopScan();

	}


	public void onScanResults(byte[] data) {
		// TODO Auto-generated method stub
		if (m_handler != null && data != null) {
			ZLogger.d("code :" + data);
			Message msg = new Message();
			try {
				String info = null;
				info = new String(data, "GBK");
				msg.what = BARCODE_READ;
				msg.obj = info;

				ZLogger.d("scanResult: msg.obj=" + msg.obj);
				m_handler.sendMessage(msg);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ZLogger.e("scan failed, " + e.toString());
			}
		}
	}

}

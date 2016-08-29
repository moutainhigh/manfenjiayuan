package com.manfenjiayuan.pda_supermarket.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.serialport.api.SerialPort;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.manfenjiayuan.business.PDAScanManager;
import com.manfenjiayuan.business.utils.BarcodeFactory;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;
import com.zkc.Service.CaptureService;

import de.greenrobot.event.EventBus;

/**
 * iData95v 数字终端
 * www.idatachina.com
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class IDataScanActivity extends BaseActivity {

    private BroadcastReceiver scanReceiver;

    protected boolean finishScannerWhenDestroyEnabled(){
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //开启扫描功能
        initScanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (finishScannerWhenDestroyEnabled()){
            finishScanner();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CaptureService.scanGpio.openScan();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ZLogger.d("onKeyDown = " + keyCode);
        /**指定，只能按键键值为139的物理按键（中间黄色按键）按下来触发扫描*/
        if (keyCode == KeyEvent.KEYCODE_F9) {
            ZLogger.d("按下黄色按键触发扫描");
//            SerialPort.CleanBuffer();
            CaptureService.scanGpio.openScan();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        ZLogger.d("onKeyUp = " + keyCode);
        /**按键弹起，停止扫描*/
        if (keyCode == KeyEvent.KEYCODE_F9) {
            ZLogger.d("按键弹起，停止扫描");
//            SerialPort.CleanBuffer();
//            CaptureService.scanGpio.openScan();;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 初始化扫描功能
     */
    private void initScanner() {
        ZLogger.d("initScanner");
        Intent newIntent = new Intent(this, CaptureService.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(newIntent);

        SerialPort.CleanBuffer();
        CaptureService.scanGpio.openScan();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PDAScanManager.GPIO_ACTION_SCANRESULT);
        scanReceiver = new ScannerResultReceiver();
        registerReceiver(scanReceiver, intentFilter);
    }

    /**
     * iscan设置的清理函数，清理的原则是：将iscan恢复到initScanner（）函数调用之前的状态
     * 函数调用和initScanner中的调用基本一致，只是参数设置的问题
     */
    private void finishScanner() {
        ZLogger.d("finishScanner");

        CaptureService.scanGpio.closeScan(); // �رյ�Դ
        CaptureService.scanGpio.closePower();

        if (scanReceiver != null) {
            unregisterReceiver(scanReceiver);
        }
    }

    /**
     * 扫描结果的广播接收者
     */
    private class ScannerResultReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            ZLogger.d(StringUtils.decodeBundle(intent.getExtras()));
            if (intent.getAction().equals(PDAScanManager.GPIO_ACTION_SCANRESULT)) {
                //获取扫描结果
                //扫描结果在在意图中是一个String类型的Extra， 名为“value”
                String barcode = intent.getStringExtra(PDAScanManager.GPIO_SCANRESULT_KEY);// + "\n";//默认加上一个回车操作
                String filterResullt = BarcodeFactory.filter(barcode,
                        BarcodeFactory.CHAR_ASTERISK, BarcodeFactory.CHAR_ASTERISK);
                ZLogger.d(String.format("gpio.scanResult:%s,%s", barcode, filterResullt));

                if (!StringUtils.isEmpty(filterResullt)){
                    EventBus.getDefault().post(new PDAScanManager.ScanBarcodeEvent(filterResullt));
                }
            }
        }
    }

}

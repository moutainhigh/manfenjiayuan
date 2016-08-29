package com.manfenjiayuan.pda_supermarket.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.manfenjiayuan.business.PDAScanManager;
import com.manfenjiayuan.business.utils.BarcodeFactory;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * iData95 数字终端
 * www.idatachina.com
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class IData95Activity extends BaseActivity {

    public static final String KEY_RECEIVER_ACTION = "com.idatachina.SCANKEYEVENT";

    private IntentFilter intentFilter;
    private BroadcastReceiver scanReceiver;

    public Handler mHandler = new MainHandler();
    protected MediaPlayer mediaPlayer = null;
    HadwareControll Controll = new HadwareControll(this);
    private boolean isScannerOpened = false;

    protected boolean finishScannerWhenDestroyEnabled(){
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initBeepSound();

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ZLogger.d("onKeyDown = " + keyCode);
        /**指定，只能按键键值为139的物理按键（中间黄色按键）按下来触发扫描*/
        if (keyCode == KeyEvent.KEYCODE_F9) {
            ZLogger.d("按下黄色按键触发扫描, isScannerOpened = " + isScannerOpened);
            if (!isScannerOpened){
                initScanner();
            }
            Controll.scan_start();
            ZLogger.d("isScannerOpened = " + isScannerOpened);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        ZLogger.d("onKeyUp = " + keyCode);
        /**按键弹起，停止扫描*/
        if (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10) {
            ZLogger.d("按键弹起，停止扫描");
            Controll.scan_stop();
            ZLogger.d("isScannerOpened = " + isScannerOpened);
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 初始化扫描功能
     */
    private void initScanner() {
        ZLogger.d("initScanner");
        Controll.Open();
        Controll.m_handler = mHandler;

        //初始化意图过滤器
        //扫描结果的意图过滤器的动作一定要使用"android.intent.action.SCANRESULT"
        intentFilter = new IntentFilter(KEY_RECEIVER_ACTION);
//注册广播接受者
        scanReceiver = new ScannerResultReceiver();
        registerReceiver(scanReceiver, intentFilter);
        isScannerOpened = true;
        ZLogger.d("isScannerOpened = " + isScannerOpened);
    }

    /**
     * iscan设置的清理函数，清理的原则是：将iscan恢复到initScanner（）函数调用之前的状态
     * 函数调用和initScanner中的调用基本一致，只是参数设置的问题
     */
    private void finishScanner() {
        ZLogger.d("finishScanner");
        Controll.Close();
        Controll.m_handler = null;
        //反注册广播接收者
        unregisterReceiver(scanReceiver);

    }

    /**
     * 扫描结果的广播接收者
     */
    private class ScannerResultReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String key_action = intent.getStringExtra("action");
            String key_code = intent.getStringExtra("code");
            ZLogger.d(String.format("action=%s,code=%s", key_action, key_code));
//
            Bundle extras = intent.getExtras();
            ZLogger.d("idata.extras:\n" + StringUtils.decodeBundle(extras));

//            if (intent.getAction().equals(PDAScanManager.IDATA_ACTION_SCANRESULT)) {
//                //获取扫描结果
//                //扫描结果在在意图中是一个String类型的Extra， 名为“value”
//                String scanResult = intent.getStringExtra(PDAScanManager.IDATA_SCANRESULT_KEY);// + "\n";//默认加上一个回车操作
//                String filterResullt = BarcodeFactory.filter(scanResult, BarcodeFactory.CHAR_ASTERISK, BarcodeFactory.CHAR_ASTERISK);
//                ZLogger.d(String.format("scanResult:%s,%s", scanResult, filterResullt));
//
//                EventBus.getDefault().post(new PDAScanManager.ScanBarcodeEvent(filterResullt));
//
//                //是否连续扫描.在这个示例程序里面，在keyUp里面停止扫描
//                if (isContinue) {
//                    ZLogger.d("continue to scan idata.");
//                    scanner.scan_stop();
//                    try {
//                        //这里的时间间隔建议是200ms到300ms之间，因为蜂鸣声和震动也需要一定的时间
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Controll.scan_start();
//                }
//                else{
//                    scanner.scan_stop();
//                }
//            }
        }
    }

    private class MainHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {

                case HadwareControll.BARCODE_READ: {
                    String barcode = (String) msg.obj;// + "\n";
                    //byte[]buffer=(byte[])msg.obj;

                    if (!StringUtils.isEmpty(barcode)){
                        String filterResullt = BarcodeFactory.filter(barcode, BarcodeFactory.CHAR_ASTERISK, BarcodeFactory.CHAR_ASTERISK);
                        ZLogger.d(String.format("scanResult:%s,%s", barcode, filterResullt));

                        EventBus.getDefault().post(new PDAScanManager.ScanBarcodeEvent(filterResullt));

                        playBeepSound();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };


    private String toHexString(byte[] byteArray, int size) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException(
                    "this byteArray must not be null or empty");
        final StringBuilder hexString = new StringBuilder(2 * size);
        for (int i = 0; i < size; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
            if (i != (byteArray.length - 1))
                hexString.append("");
        }
        return hexString.toString().toUpperCase();
    }
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        Log.d("012","keyCode:"+keyCode);
//        if ((keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10
//                || keyCode == KeyEvent.KEYCODE_F11)&&event.getRepeatCount()==0) {
//            Controll.scan_start();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10
//                || keyCode == KeyEvent.KEYCODE_F11) {
//            Controll.scan_stop();
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }

    private void playBeepSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }

        //Controll.playbeep();


    }

    private void initBeepSound() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.setDataSource(file.getFileDescriptor(),
                            file.getStartOffset(), file.getLength());
                    file.close();

                    mediaPlayer.prepare();
                }
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }


}

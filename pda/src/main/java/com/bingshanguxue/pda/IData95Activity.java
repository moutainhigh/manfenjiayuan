package com.bingshanguxue.pda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.android.barcodescandemo.ScannerInerface;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;

import de.greenrobot.event.EventBus;

/**
 * iData95v 数字终端
 * www.idatachina.com
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class IData95Activity extends BaseActivity {

    private BroadcastReceiver scanReceiver;

    private ScannerInerface scanner;
    private boolean isContinue = false;//第二种连续扫描的标志
    private boolean isScannerOpened = false;

    protected boolean finishScannerWhenDestroyEnabled(){
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ZLogger.d("onKeyDown = " + keyCode);
        /**指定，只能按键键值为139的物理按键（中间黄色按键）按下来触发扫描*/
        if (keyCode == KeyEvent.KEYCODE_F9 ||
                keyCode == KeyEvent.KEYCODE_F10 ||
                keyCode == KeyEvent.KEYCODE_F11) {
            ZLogger.d("按下黄色按键触发扫描，isScannerOpened = " + isScannerOpened);
            if (!isScannerOpened){
                initScanner();
            }
            //启动扫描操作,此操作执行后,扫描头会出光
            scanner.scan_start();
            ZLogger.d("isScannerOpened = " + isScannerOpened);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        ZLogger.d("onKeyUp = " + keyCode);
        /**按键弹起，停止扫描*/
        if (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10 ||
                keyCode == KeyEvent.KEYCODE_F11) {
            ZLogger.d("停止扫描操作,此操作执行后,扫描头光会灭掉");
            scanner.scan_stop();
            ZLogger.d("isScannerOpened = " + isScannerOpened);
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 初始化扫描功能
     */
    private void initScanner() {
        ZLogger.d("initScanner");
        scanner = new ScannerInerface(this);
        scanner.open();//给扫描头进行上电,扫描前一定要先执行此操作再启动扫描
        scanner.enablePlayBeep(true);//是否允许蜂鸣反馈
        scanner.enablePlayVibrate(true);//是否允许震动反馈
        scanner.enablShowAPPIcon(true);//是否允许显示iscan的应用程序图标
        scanner.enablShowNoticeIcon(true);//是否允许显示iscan通知栏图标
        //scanner.enableAddKeyValue(0);/**改函数无效*/

        /**这里采用自定义按键/button 方式来触发扫描*/
        scanner.lockScanKey();//使得iscan只能通过设备上的三个黄色按键来触发条码扫描
//        scanner.unlockScanKey();//使得iscan不能通过三个黄色按键来触发扫描，这样就允许客户通过自定义的方式来调用scan_start和scan_stop对来触发扫描

        // 设置扫描结果的输出模式，参数为0和1：
        // 0为模拟输出（在光标停留的地方输出扫描结果）；
        // 1为广播输出（由应用程序编写广播
        // 接收者来获得扫描结果，并在指定的控件上显示扫描结果）
        scanner.setOutputMode(1);

        //初始化意图过滤器
        //扫描结果的意图过滤器的动作一定要使用"android.intent.action.SCANRESULT"
        IntentFilter intentFilter = new IntentFilter(PDAScanManager.IDATA_ACTION_SCANRESULT);
        intentFilter.addAction("com.idatachina.SCANKEYEVENT");
        intentFilter.addAction("android.intent.action.BARCODESTOPSCAN");
//注册广播接受者
        scanReceiver = new ScannerResultReceiver();
        registerReceiver(scanReceiver, intentFilter);
        isScannerOpened = true;
    }

    /**
     * iscan设置的清理函数，清理的原则是：将iscan恢复到initScanner（）函数调用之前的状态
     * 函数调用和initScanner中的调用基本一致，只是参数设置的问题
     */
    private void finishScanner() {
        ZLogger.d("finishScanner");
        scanner.scan_stop();
        scanner.close();//给扫描头进行断电,与SetScannerOn 功能相反
        //反注册广播接收者
        unregisterReceiver(scanReceiver);
        //flag = false;	//程序退出了，总要把固定时间间隔的连续扫描标志置为false吧，否则线程就“根本停不下来”
        isContinue = false;    //连扫是需要停止的

        isScannerOpened = false;
    }

    /**
     * 扫描结果的广播接收者
     */
    private class ScannerResultReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PDAScanManager.IDATA_ACTION_SCANRESULT)) {

                Bundle extras = intent.getExtras();
                ZLogger.d("idata.extras:\n" + StringUtils.decodeBundle(extras));
                if (extras != null && extras.size() > 0){
                    //获取扫描结果
                    //扫描结果在在意图中是一个String类型的Extra， 名为“value”
                    String barcode = intent.getStringExtra(PDAScanManager.IDATA_SCANRESULT_KEY);// + "\n";//默认加上一个回车操作
                    String filterResullt = BarcodeFactory.filter(barcode,
                            BarcodeFactory.CHAR_ASTERISK, BarcodeFactory.CHAR_ASTERISK);
                    ZLogger.d(String.format("idata.scanResult:%s,%s", barcode, filterResullt));

                    if (!StringUtils.isEmpty(filterResullt)){
                        EventBus.getDefault().post(new PDAScanManager.ScanBarcodeEvent(filterResullt));
                    }
                }

                //是否连续扫描.在这个示例程序里面，在keyUp里面停止扫描
                if (isContinue) {
                    ZLogger.d("continue to scan idata.");
                    scanner.scan_stop();
                    try {
                        //这里的时间间隔建议是200ms到300ms之间，因为蜂鸣声和震动也需要一定的时间
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    scanner.scan_start();
                }
                else{
                    scanner.scan_stop();
                }
            }
        }
    }

}

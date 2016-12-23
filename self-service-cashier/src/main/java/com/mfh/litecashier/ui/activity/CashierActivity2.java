package com.mfh.litecashier.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.GPrinterAgent;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.service.PeripheralService;
import com.mfh.litecashier.service.TtsService;
import com.mfh.litecashier.utils.GlobalInstance;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class CashierActivity2 extends BaseActivity {
    protected PeripheralService.PeripheralBinder mPeripheralBinder;
    private ServiceConnection peripheralServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ZLogger.d("onServiceDisconnected: " + name);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ZLogger.d("onServiceConnected: " + name);
            mPeripheralBinder = (PeripheralService.PeripheralBinder) service;
//            mTtsBinder.startDownload();
        }
    };

    protected TtsService.TtsBinder mTtsBinder;
    private ServiceConnection ttsServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ZLogger.d("onServiceDisconnected: " + name);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ZLogger.d("onServiceConnected: " + name);
            mTtsBinder = (TtsService.TtsBinder) service;
//            mTtsBinder.startDownload();
        }
    };

//    protected
//    private GpService mGpService= null;
//    private PrinterServiceConnection conn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        Intent peripheralIntent = new Intent(this, PeripheralService.class);
        bindService(peripheralIntent, peripheralServiceConnection, BIND_AUTO_CREATE);

        Intent ttsIntent = new Intent(this, TtsService.class);
        bindService(ttsIntent, ttsServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        hideSystemUI();
    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        //Any time the window receives focus, simply set the IMMERSIVE mode.
//        if (hasFocus) {
//            hideSystemUI();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        unbindService(peripheralServiceConnection);
        unbindService(ttsServiceConnection);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        ZLogger.d("onConfigurationChanged" + newConfig.toString());
        if (mPeripheralBinder != null){
            mPeripheralBinder.onConfigurationChanged();
        }
    }

    /**
     * 串口
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SerialPortEvent event) {
        ZLogger.d(String.format("SerialPortEvent(%d)", event.getType()));
        //客显
        if (event.getType() == SerialPortEvent.SERIAL_TYPE_DISPLAY) {
            if (mPeripheralBinder != null){
                mPeripheralBinder.VFD(event.getCmd(), true);
            }
        } else if (event.getType() == SerialPortEvent.RINTER_PRINT_PRIMITIVE) {
            if (mPeripheralBinder != null){
                mPeripheralBinder.printer(event.getCmdBytes());
            }
        } else if (event.getType() == SerialPortEvent.UPDATE_PORT_GPRINTER) {
            if (mPeripheralBinder != null){
                mPeripheralBinder.onPrinterUpdated();
            }
        } else if (event.getType() == SerialPortEvent.UPDATE_PORT_SCALE) {
            try {
                //清空数据
                GlobalInstance.getInstance().setNetWeight(0D);

                if (mPeripheralBinder != null){
                    mPeripheralBinder.onScaleUpdated();
                }
            } catch (Exception e) {
                ZLogger.ef(e.toString());
            }
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_INIT) {
            if (mPeripheralBinder != null){
                mPeripheralBinder.onVFDUpdated();
            }

            if (mPeripheralBinder != null){
                mPeripheralBinder.VFD(GPrinterAgent.VFD("12.306"));
            }
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD) {
            if (mPeripheralBinder != null){
                mPeripheralBinder.VFD(GPrinterAgent.VFD(event.getCmd()));
            }
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_BYTE) {
            if (mPeripheralBinder != null){
                mPeripheralBinder.VFD(event.getCmdBytes());
            }
        }
    }
}

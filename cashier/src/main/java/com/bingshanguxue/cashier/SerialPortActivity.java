package com.bingshanguxue.cashier;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.mfh.framework.BizConfig;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.uikit.base.BaseActivity;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;
import de.greenrobot.event.EventBus;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class SerialPortActivity extends BaseActivity {
    DispQueueThread DispQueue;//刷新显示线程
    private SerialPortFinder mSerialPortFinder;//串口设备搜索

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        initCOM();
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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ZLogger.d("onConfigurationChanged" + newConfig.toString());

        setControls();
    }

    private class SerialControl extends SerialHelper {

        public SerialControl(String sPort, String sBaudRate) {
            super(sPort, sBaudRate);
        }

        public SerialControl(String sPort, int iBaudRate) {
            super(sPort, iBaudRate);
        }

        public SerialControl() {
        }

        protected void onDataReceived(final ComBean ComRecData) {
            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
            //最终效果差不多-_-，线程定时刷新稍好一些。
            DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
//            StringBuilder sMsg = new StringBuilder();
//            sMsg.append(ComRecData.sRecTime);
//            sMsg.append("[");
//            sMsg.append(ComRecData.sComPort);
//            sMsg.append("]");
//            sMsg.append("[Hex] ");
//            sMsg.append(DataConvertUtil.ByteArrToHex(ComRecData.bRec));
//            sMsg.append("\r\n");
//            ZLogger.d("onDataReceived: " + sMsg);
            /*
            runOnUiThread(new Runnable()//直接刷新显示
			{
				public void run()
				{
					DispRecData(ComRecData);
				}
			});*/
        }
    }

    /**
     * 刷新显示线程
     */
    private class DispQueueThread extends Thread {
        private Queue<ComBean> mComBeanQueue = new LinkedList<>();

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                final ComBean ComData;
                while ((ComData = mComBeanQueue.poll()) != null) {
                    try {
                        DispRecData(ComData);

                        Thread.sleep(50);//显示性能高的话，可以把此数值调小。
                    } catch (Exception e) {
//                        e.printStackTrace();
                        ZLogger.e(e.toString());
                    }
                    break;
                }
            }
        }

        public synchronized void AddQueue(ComBean ComData) {
            mComBeanQueue.add(ComData);
        }
    }

    /**
     * 显示接收数据
     */
    protected void DispRecData(ComBean comBean) {
        // TODO: 8/9/16 process com data 
        String sMsg = String.format("时间：<%s>\n串口：<%s>\n数据1：<%s>\n数据2:<%s>",
                comBean.sRecTime, comBean.sComPort,
                new String(comBean.bRec), DataConvertUtil.ByteArrToHex(comBean.bRec));
        ZLogger.d("COM RECV:" + sMsg);
    }

    /**
     * 初始化串口
     */
    protected void initCOM() {
        // TODO: 8/9/16 init coms 
        DispQueue = new DispQueueThread();
        DispQueue.start();

        setControls();
    }

    private class OpenPortRunnable implements Runnable{
        private SerialHelper serialHelper;

        public OpenPortRunnable(SerialHelper serialHelper) {
            this.serialHelper = serialHelper;
        }

        @Override
        public void run() {
            try {
                if (serialHelper == null || serialHelper.isOpen()){
                    return;
                }

                serialHelper.open();
            } catch (SecurityException e) {
                ZLogger.e("打开串口失败:没有串口读/写权限!" + serialHelper.getPort());
//            DialogUtil.showHint("打开串口失败:没有串口读/写权限!" + ComPort.getPort());
            } catch (IOException e) {
                ZLogger.e("打开串口失败:未知错误!" + serialHelper.getPort());
//            DialogUtil.showHint("打开串口失败:未知错误!" + ComPort.getPort());
            } catch (InvalidParameterException e) {
                ZLogger.e("打开串口失败:参数错误!");
//            DialogUtil.showHint("打开串口失败:参数错误!" + ComPort.getPort());
            }
        }
    }

    /**
     * 打开串口
     */
    public void OpenComPort(SerialHelper serialHelper) {
        //使用线程，避免界面卡顿
        new Thread(new OpenPortRunnable(serialHelper)).start();
    }

    /**
     * 关闭串口
     */
    private void CloseComPort(SerialHelper serialHelper) {
        if (serialHelper != null && serialHelper.isOpen()) {
            serialHelper.stopSend();
            serialHelper.close();
        }
    }

    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, String sOut, boolean bHex) {
        if (ComPort != null && ComPort.isOpen()) {
//            DialogUtil.showHint("发送串口数据!" + sOut);
            if (bHex) {
                ComPort.sendHex(sOut);
            } else {
                ComPort.sendTxt(sOut);
            }
        }
    }

    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, byte[] bOutArray) {
        if (ComPort != null && ComPort.isOpen()) {
            ComPort.send(bOutArray);
        }
    }

    /**
     * poslab: devices [/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttymxc4, /dev/ttymxc3, /dev/ttymxc2, /dev/ttymxc1, /dev/ttymxc0]
     * JOOYTEC: devices:[/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttyS3, /dev/ttyS1, /dev/ttyS0, /dev/ttyFIQ0]
     */
    public void setControls() {
        mSerialPortFinder = new SerialPortFinder();

        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<>();
        if (entryValues != null) {
            Collections.addAll(allDevices, entryValues);
        }
        ZLogger.d("devicePath:" + allDevices.toString());
      
        if (!BizConfig.RELEASE){
            String[] entryValues2 = mSerialPortFinder.getAllDevices();
            List<String> allDevices2 = new ArrayList<>();
            if (entryValues2 != null) {
                Collections.addAll(allDevices2, entryValues2);
            }
            ZLogger.d("devices:" + allDevices2.toString());
        }
    }
}

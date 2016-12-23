package com.mfh.litecashier.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;

import com.bingshanguxue.cashier.hardware.PoslabAgent;
import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.GPrinterAgent;
import com.bingshanguxue.cashier.hardware.printer.PrinterAgent;
import com.bingshanguxue.cashier.hardware.scale.AHScaleHelper;
import com.bingshanguxue.cashier.hardware.scale.DS781A;
import com.bingshanguxue.cashier.hardware.scale.SMScaleHelper;
import com.bingshanguxue.cashier.hardware.scale.ScaleAgent;
import com.gprinter.command.GpCom;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.service.TtsService;
import com.mfh.litecashier.utils.GlobalInstance;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;

/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class CashierActivity extends BaseActivity {
    private DispQueueThread DispQueue;//刷新显示线程
    private SerialPortFinder mSerialPortFinder;//串口设备搜索
    protected SerialControl comDisplay, comPrint, comScale;//串口

    private long lastScaleTriggle = System.currentTimeMillis();

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

        initCOM();

        Intent bindIntent = new Intent(this, TtsService.class);
        bindService(bindIntent, ttsServiceConnection, BIND_AUTO_CREATE);

        startTimer();
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

        //关闭串口
        CloseComPort(comDisplay);
        CloseComPort(comPrint);
        CloseComPort(comScale);

        if (mSerialDispatcher != null){
            mSerialDispatcher.quit();
            mSerialDispatcher = null;
        }

        if (DispQueue != null){
            DispQueue.interrupt();
            DispQueue = null;
        }

        unbindService(ttsServiceConnection);

        cancelTimer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        ZLogger.d("onConfigurationChanged" + newConfig.toString());
        CloseComPort(comDisplay);
        CloseComPort(comPrint);
        CloseComPort(comScale);

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
            // TODO: 8/9/16 java.util.NoSuchElementException 
            try {
                while (!isInterrupted()) {
                    final ComBean ComData;
                    while ((ComData = mComBeanQueue.poll()) != null) {
                        performData(ComData);

                        Thread.sleep(50);//显示性能高的话，可以把此数值调小。
                        break;
                    }
                }
            } catch (Exception e) {
//                        e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }

        public synchronized void AddQueue(ComBean ComData) {
            mComBeanQueue.add(ComData);
        }
    }


    /**
     * 显示接收数据
     */
    private void performData(ComBean comBean) {
        String port = comBean.sComPort;
        if (StringUtils.isEmpty(port)) {
            return;
        }

        if (SharedPrefesManagerFactory.isSuperPermissionGranted() || !BizConfig.RELEASE) {
            String sMsg = String.format("时间：<%s>\n串口：<%s>\n数据1：<%s>\n数据2:<%s>",
                    comBean.sRecTime, port,
                    new String(comBean.bRec), DataConvertUtil.ByteArrToHex(comBean.bRec));
            ZLogger.df("COM RECV:" + sMsg);
        }

        Long rightNow = System.currentTimeMillis();

        if (ScaleAgent.isEnabled()) {
            //接收到串口数据
            if (port.equals(ScaleAgent.getPort())) {
                if (ScaleAgent.getScaleType() == ScaleAgent.SCALE_TYPE_ACS_P215) {
                    Double netWeight = AHScaleHelper.parseACSP215(comBean.bRec);
                    if (netWeight != null) {
                        GlobalInstance.getInstance().setNetWeight(netWeight);
                    }
                } else if (ScaleAgent.getScaleType() == ScaleAgent.SCALE_TYPE_DS_781A) {
                    DS781A ds781A = SMScaleHelper.parseData(comBean.bRec);
                    if (ds781A != null) {
                        GlobalInstance.getInstance().setNetWeight(ds781A.getNetWeight());
                    }
                }

                lastScaleTriggle = rightNow;
            } else {
                Long interval = rightNow - lastScaleTriggle;
                ZLogger.d(String.format("interval＝%d, rightNow=%d, lastAhscaleTriggle=%d",
                        interval, rightNow, lastScaleTriggle));
                if (interval > 2 * MINUTE) {
                    ZLogger.df(String.format("(%s)超过1分钟没有收到电子秤串口消息，自动重新打开串口",
                            ScaleAgent.getPort()));
                    GlobalInstance.getInstance().setNetWeight(0D);

                    CloseComPort(comScale);
                    comScale = new SerialControl(ScaleAgent.getPort(), ScaleAgent.getBaudrate());
                    OpenComPort(comScale);
                    lastScaleTriggle = rightNow;
                }
            }
        }

    }

    private int comMode = 1;
    /**
     * The queue of ComBean from serial port.
     */
    private PriorityBlockingQueue<ComBean> mSerialQueue = new PriorityBlockingQueue<>();
    private SerialDispatcher mSerialDispatcher;//线程数据分发器

    /**
     * Provides a thread to process data from serial port.
     */
    private class SerialDispatcher extends Thread {
        /**
         * Used for telling us to die.
         */
        private volatile boolean mQuit = false;

        /**
         * The queue of ComBean from serial port.
         */
        private BlockingQueue<ComBean> mQueue;

        public SerialDispatcher(BlockingQueue<ComBean> queue){
            mQueue = queue;
        }

        @Override
        public void run() {
            super.run();

            while (!isInterrupted()) {
                //设置线程优先级
//                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                ComBean comBean;
                try {
                    // 取走BlockingQueue里排在首位的对象,若BlockingQueue为空,阻断进入等待状态直到BlockingQueue有新的数据被加入;
                    // Get a data from the  queue, blocking until at least one is available.
                    comBean = mQueue.take();
                } catch (InterruptedException e) {
                    // We may have been interrupted because it was time to quit.
                    if (mQuit) {
                        return;
                    }
                    continue;
                }

                try {
                    if (comBean != null) {
                        performData(comBean);
                        Thread.sleep(50);//显示性能高的话，可以把此数值调小。
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Forces this dispatcher to quit immediately.  If any requests are still in
         * the queue, they are not guaranteed to be processed.
         */
        public void quit() {
            mQuit = true;
            interrupt();
        }
    }



    /**
     * 初始化串口
     */
    private void initCOM() {
        comDisplay = new SerialControl(PoslabAgent.getPort(), PoslabAgent.getBaudrate());
        comPrint = new SerialControl(PrinterAgent.getPort(), PrinterAgent.getBaudrate());
        comScale = new SerialControl(ScaleAgent.getPort(), ScaleAgent.getBaudrate());

//        embPrinter = PrinterInstance.getPrinterInstance(new File(PrinterAgent.getPort()),
//                Integer.parseInt(PrinterAgent.getBaudrate()), 0, mHandler);

        if (comMode == 1){
            DispQueue = new DispQueueThread();
            DispQueue.start();
        }
        else{
            mSerialDispatcher = new SerialDispatcher(mSerialQueue);
            mSerialDispatcher.start();
        }

        setControls();
    }


    private class OpenPortRunnable implements Runnable {
        private SerialHelper serialHelper;

        public OpenPortRunnable(SerialHelper serialHelper) {
            this.serialHelper = serialHelper;
        }

        @Override
        public void run() {
            try {
                if (serialHelper == null || serialHelper.isOpen()) {
                    return;
                }

                ZLogger.df("准备打开串口:" + serialHelper.getPort());
                serialHelper.open();
                ZLogger.df("打开串口成功:" + serialHelper.getPort());
            } catch (SecurityException e) {
                ZLogger.ef("打开串口失败:没有串口读/写权限!" + serialHelper.getPort());
            } catch (IOException e) {
                ZLogger.ef("打开串口失败:未知错误!" + serialHelper.getPort());
            } catch (InvalidParameterException e) {
                ZLogger.ef("打开串口失败:参数错误!" + serialHelper.getPort());
            }
        }
    }

    /**
     * 打开串口
     */
    public void OpenComPort(SerialHelper serialHelper) {
        if (serialHelper == null) {
            return;
        }
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
        List<String> devicesPath = new ArrayList<>();
        if (entryValues != null) {
            Collections.addAll(devicesPath, entryValues);
        }
        ZLogger.d("devicePath:" + devicesPath.toString());
        SerialManager.getInstance().setComDevicesPath(devicesPath);//保存devices

        if (BizConfig.RELEASE) {
            String[] devices = mSerialPortFinder.getAllDevices();
            List<String> allDevices2 = new ArrayList<>();
            if (devices != null) {
                Collections.addAll(allDevices2, devices);
            }
            ZLogger.d("devices:" + allDevices2.toString());
        }

        if (devicesPath.contains(PrinterAgent.getPort()) && PrinterAgent.isEnabled()) {
            OpenComPort(comPrint);
//                if (embPrinter == null){
//                    embPrinter = PrinterInstance.getPrinterInstance(new File(PrinterAgent.getPort()),
//                            Integer.parseInt(PrinterAgent.getBaudrate()), 0, mHandler);
//                }
//                embPrinter.openConnection();
        }
        if (devicesPath.contains(PoslabAgent.getPort()) && PoslabAgent.isEnabled()) {
            OpenComPort(comDisplay);
        }
        if (devicesPath.contains(ScaleAgent.getPort()) && ScaleAgent.isEnabled()) {
            OpenComPort(comScale);
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
            if (comDisplay != null) {
                OpenComPort(comDisplay);
                sendPortData(comDisplay, event.getCmd(), true);
            }
        } else if (event.getType() == SerialPortEvent.RINTER_PRINT_PRIMITIVE) {
            if (comPrint != null) {
                OpenComPort(comPrint);
                sendPortData(comPrint, event.getCmdBytes());
            }
        } else if (event.getType() == SerialPortEvent.UPDATE_PORT_GPRINTER) {
            CloseComPort(comPrint);

            if (PrinterAgent.isEnabled()) {
                comPrint = new SerialControl(PrinterAgent.getPort(), PrinterAgent.getBaudrate());
                OpenComPort(comPrint);
//                    embPrinter = PrinterInstance.getPrinterInstance(new File(PrinterAgent.getPort()),
//                            Integer.parseInt(PrinterAgent.getBaudrate()), 0, mHandler);
//                    embPrinter.openConnection();
            }
        } else if (event.getType() == SerialPortEvent.UPDATE_PORT_SCALE) {
            try {
                //清空数据
                GlobalInstance.getInstance().setNetWeight(0D);
                CloseComPort(comScale);

                if (ScaleAgent.isEnabled()) {
                    comScale = new SerialControl(ScaleAgent.getPort(), ScaleAgent.getBaudrate());
                    OpenComPort(comScale);
                }
            } catch (Exception e) {
                ZLogger.ef(e.toString());
            }
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_INIT) {
            CloseComPort(comDisplay);

            comDisplay = new SerialControl(PoslabAgent.getPort(), PoslabAgent.getBaudrate());
            OpenComPort(comDisplay);
            sendPortData(comDisplay, GPrinterAgent.VFD("12.306"));
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD) {
            OpenComPort(comDisplay);
            sendPortData(comDisplay, GPrinterAgent.VFD(event.getCmd()));
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_BYTE) {
            OpenComPort(comDisplay);
            sendPortData(comDisplay, event.getCmdBytes());
        }
    }


    public int sendEscCommand(String b64) {
        ZLogger.d("sendEscCommand:\n" + b64);
        GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
        byte[] datas = Base64.decode(b64, Base64.DEFAULT);
        Vector vector = new Vector();
        byte[] var9 = datas;
        int var8 = datas.length;

        for (int var7 = 0; var7 < var8; ++var7) {
            byte b = var9[var7];
            vector.add(Byte.valueOf(b));
        }

        retval = sendDataImmediately(vector);

        return retval.ordinal();
    }

    public GpCom.ERROR_CODE sendDataImmediately(Vector<Byte> Command) {
        ZLogger.d("sendDataImmediately:\n" + Command.toString());
        GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
        Vector data = new Vector(Command.size());
        for (int k = 0; k < Command.size(); ++k) {
            if (data.size() >= 1024) {
                retval = writeDataImmediately(data);
                data.clear();
                if (retval != GpCom.ERROR_CODE.SUCCESS) {
                    return retval;
                }
            }

            data.add((Byte) Command.get(k));
        }

        retval = writeDataImmediately(data);

        return retval;
    }

    public GpCom.ERROR_CODE writeDataImmediately(Vector<Byte> data) {
        ZLogger.d("writeDataImmediately:\n" + data.toString());

        GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
        if (data != null && data.size() > 0) {
            byte[] sendData = new byte[data.size()];

            for (int e = 0; e < data.size(); ++e) {
                sendData[e] = ((Byte) data.get(e)).byteValue();
            }

            sendPortData(comPrint, sendData);
        }

        return retval;
    }

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * 1000;
    private static final int HOUR = 60 * 60 * 1000;
    private static Timer comMonitorTimer = new Timer();

    /**
     * 关闭定时器
     */
    private void startTimer() {
        cancelTimer();
        if (comMonitorTimer == null) {
            comMonitorTimer = new Timer();
        }
        comMonitorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Long rightNow = System.currentTimeMillis();
                if (ScaleAgent.isEnabled()) {
                    Long interval1 = rightNow - lastScaleTriggle;
                    if (interval1 > 1000 * 60) {
                        ZLogger.df(String.format("(%s)超过1分钟没有收到电子秤串口消息，自动重新打开串口", ScaleAgent.getPort()));
                        CloseComPort(comScale);
                        comScale = new SerialControl(ScaleAgent.getPort(), ScaleAgent.getBaudrate());
                        OpenComPort(comScale);
                        lastScaleTriggle = rightNow;
                    }
                }
            }
        }, 10 * SECOND, 5 * MINUTE);
    }

    /**
     * 取消定时器
     */
    private void cancelTimer() {
        if (comMonitorTimer != null) {
            comMonitorTimer.cancel();
        }
        comMonitorTimer = null;
    }

}

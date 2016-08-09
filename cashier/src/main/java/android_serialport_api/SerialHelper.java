package android_serialport_api;


import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/**
 * @author benjaminwan
 *         串口辅助工具类
 */
public abstract class SerialHelper {
    private static final String PORT_DEF = "/dev/s3c2410_serial0";
    private static final int BAUDRATE_FEF = 9600;//波特率
    private static final int MAX_READ_BUFFER_SIZE = 4096;

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private String sPort = PORT_DEF;
    private int iBaudRate = BAUDRATE_FEF;
    private boolean _isOpen = false;
    private byte[] _bLoopData = new byte[]{0x30};
    private int iDelay = 500;


    public SerialHelper(String sPort, int iBaudRate) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
    }

    public SerialHelper() {
        this(PORT_DEF, BAUDRATE_FEF);
    }

    public SerialHelper(String sPort) {
        this(sPort, BAUDRATE_FEF);
    }

    public SerialHelper(String sPort, String sBaudRate) {
        this(sPort, Integer.parseInt(sBaudRate));
    }

    public void open() throws SecurityException, IOException, InvalidParameterException {
        mSerialPort = new SerialPort(new File(sPort), iBaudRate, 0);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        mReadThread = new ReadThread();
        mReadThread.start();
        mSendThread = new SendThread();
        mSendThread.setSuspendFlag();
        mSendThread.start();
        _isOpen = true;
    }


    public void close() {
        if (mReadThread != null)
            mReadThread.interrupt();
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        _isOpen = false;
    }

    public void send(byte[] bOutArray) {
        try {
//			ZLogger.d("串口发送" + DataConvertUtil.ByteArrToHex(bOutArray));
            mOutputStream.write(bOutArray);
        } catch (IOException e) {
//			e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    public void sendHex(String sHex) {
        byte[] bOutArray = DataConvertUtil.HexToByteArr(sHex);
        send(bOutArray);
    }

    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }


    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    if (mInputStream == null) return;
                    byte[] buffer = new byte[MAX_READ_BUFFER_SIZE];
                    int size = mInputStream.read(buffer);
                    if (size > 0) {
                        ComBean ComRecData = new ComBean(sPort, buffer, size);
                        onDataReceived(ComRecData);
                    }
                    try {
                        Thread.sleep(50);//延时50ms
                    } catch (InterruptedException e) {
//						e.printStackTrace();
                        ZLogger.e(e.toString());
                    }
                } catch (Throwable e) {
//					e.printStackTrace();
                    ZLogger.e(e.toString());
                    return;
                }
            }
        }
    }


    private class SendThread extends Thread {
        public boolean suspendFlag = true;// 控制线程的执行

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            ZLogger.e(e.toString());
//							e.printStackTrace();
                        }
                    }
                }
                send(getbLoopData());
                try {
                    Thread.sleep(iDelay);
                } catch (InterruptedException e) {
                    ZLogger.e(e.toString());
//					e.printStackTrace();
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }
    }


    public int getBaudRate() {
        return iBaudRate;
    }

    public boolean setBaudRate(int iBaud) {
        if (_isOpen) {
            return false;
        } else {
            iBaudRate = iBaud;
            return true;
        }
    }

    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }


    public String getPort() {
        return sPort;
    }

    public boolean setPort(String sPort) {
        if (_isOpen) {
            return false;
        } else {
            this.sPort = sPort;
            return true;
        }
    }

    public boolean isOpen() {
        return _isOpen;
    }

    public byte[] getbLoopData() {
        return _bLoopData;
    }

    public void setbLoopData(byte[] bLoopData) {
        this._bLoopData = bLoopData;
    }

    public void setTxtLoopData(String sTxt) {
        this._bLoopData = sTxt.getBytes();
    }

    public void setHexLoopData(String sHex) {
        this._bLoopData = DataConvertUtil.HexToByteArr(sHex);
    }

    public int getiDelay() {
        return iDelay;
    }

    public void setiDelay(int iDelay) {
        this.iDelay = iDelay;
    }

    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }

    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }

    protected abstract void onDataReceived(ComBean ComRecData);
}
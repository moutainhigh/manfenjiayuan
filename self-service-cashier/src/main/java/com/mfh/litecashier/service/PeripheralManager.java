package com.mfh.litecashier.service;

import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.mfh.framework.anlaysis.logger.ZLogger;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bingshanguxue on 09/07/2017.
 */

public class PeripheralManager {
    private static PeripheralManager instance = null;

    private Queue<byte[]> mQueue = new LinkedList<>();

    /**
     * 返回 DataDownloadManager 实例
     *
     * @return
     */
    public static PeripheralManager getInstance() {
        if (instance == null) {
            synchronized (PeripheralManager.class) {
                if (instance == null) {
                    instance = new PeripheralManager();
                }
            }
        }
        return instance;
    }

    public void add(byte[] data) {
        synchronized(mQueue) {
            mQueue.add(data);
        }
    }

    public byte[] poll(byte[] data) {
        synchronized(mQueue) {
            return mQueue.poll();
        }
    }

    private class PrintQueueThread extends Thread {
        private Queue<byte[]> mQueue = new LinkedList<>();

        @Override
        public void run() {
            super.run();
            // TODO: 8/9/16 java.util.NoSuchElementException
            try {
                while (!isInterrupted()) {
                    final byte[] data;
                    while ((data = mQueue.poll()) != null) {
                        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.RINTER_PRINT_PRIMITIVE, data));

                        Thread.sleep(150);//显示性能高的话，可以把此数值调小。
                        break;
                    }
                }
            } catch (Exception e) {
//                        e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }

        public synchronized void addQueue(byte[] data) {
            mQueue.add(data);
        }
    }

}

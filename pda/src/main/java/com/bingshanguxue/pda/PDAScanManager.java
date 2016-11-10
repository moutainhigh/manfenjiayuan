package com.bingshanguxue.pda;

import android.os.Bundle;

/**
 * com.android.auto.iscan
 *
 * 在开发的时候，一定要先将scannerInterface.jar导入到libs文件夹下，这个扫描操作的接口
 * Created by bingshanguxue on 5/17/16.
 */
public class PDAScanManager {
    //iData数字终端
    public static final String IDATA_ACTION_SCANRESULT = "android.intent.action.SCANRESULT";
    public static final String IDATA_SCANRESULT_KEY = "value";
    //GPIO数字终端
    public static final String GPIO_ACTION_SCANRESULT = "com.zkc.scancode";
    public static final String GPIO_SCANRESULT_KEY = "code";


    public static class ScanBarcodeEvent {
        public static final int EVENT_ID_START_ZXING = 0X01;//开始摄像头扫描
        public static final int EVENT_ID_SCAN_NEXT = 0X02;//扫描结束

        public static final String KEY_EVENTID = "eventId";
        public static final String KEY_BARCODE = "barcode";

        private Bundle args;

        public ScanBarcodeEvent(Bundle args) {
            this.args = args;
        }

        public Bundle getArgs() {
            return args;
        }
    }

}

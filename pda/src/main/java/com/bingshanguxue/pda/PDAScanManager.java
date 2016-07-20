package com.bingshanguxue.pda;

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
//        public static final int EVENT_ID_SCAN_BARCODE = 0X01;//扫描成功
//        public static final int EVENT_ID_SYNC_FAILED = 0X02;//同步失败

        private String barcode;

        public ScanBarcodeEvent(String barcode) {
            this.barcode = barcode;
        }

        public String getBarcode() {
            return barcode;
        }
    }

}

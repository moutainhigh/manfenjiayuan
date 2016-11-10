package com.bingshanguxue.pda;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;

import de.greenrobot.event.EventBus;


/**
 * 有扫描功能的Fragment基类
 * Created by Nat.ZZN(bingshanguxue) on 15/11/06.
 */
public abstract class PDAScanFragment extends BaseFragment {

    //扫描到条码
    protected abstract void onScanCode(String code);

    protected boolean isAcceptBarcodeEnabled = true;

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 处理条码
     * */
    private void processBarcode(String barcode){
        ZLogger.d(String.format("isAcceptBarcodeEnabled=%b, barcode=%s",
                isAcceptBarcodeEnabled, barcode));
        if (!isAcceptBarcodeEnabled || StringUtils.isEmpty(barcode)){
            return;
        }

        if (barcode.startsWith("2") && barcode.length() == 13) {
            String plu = barcode.substring(1, 7);
            try{
                String weightStr = String.format("%s.%s",
                        barcode.substring(7, 9), barcode.substring(9, 12));
                Double weight = Double.valueOf(weightStr);
                ZLogger.d(String.format("扫描生鲜商品条码：%s, PLU码：%s, 重量：%f",
                        barcode, plu, weight));
                onScanCode(plu);
            }
            catch (Exception e){
                ZLogger.e(e.toString());
            }
        } else {
            ZLogger.d(String.format("扫描标准商品条码:%s", barcode));
            onScanCode(barcode);
        }
    }

    public void onEventMainThread(PDAScanManager.ScanBarcodeEvent event) {
        Bundle bundle = event.getArgs();
        if (bundle == null){
            return;
        }
        int eventId = bundle.getInt(PDAScanManager.ScanBarcodeEvent.KEY_EVENTID);
        String barcode = bundle.getString(PDAScanManager.ScanBarcodeEvent.KEY_BARCODE);
        if (PDAScanManager.ScanBarcodeEvent.EVENT_ID_SCAN_NEXT == eventId){
            processBarcode(barcode);
        }
        else if (PDAScanManager.ScanBarcodeEvent.EVENT_ID_START_ZXING == eventId){
            zxingSweep();
        }
    }

    /**
     * 扫描条码或二维码
     * */
    protected void zxingSweep(){
        Intent intent = new Intent(getActivity(), ScanActivity.class);
        startActivityForResult(intent, UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.d(String.format("requestCode=%d, resultCode=%d", requestCode, resultCode));

        switch (requestCode) {
            case UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Bundle bundle = data.getExtras();
                    String resultText = bundle.getString("result", "");
                    processBarcode(resultText);
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}

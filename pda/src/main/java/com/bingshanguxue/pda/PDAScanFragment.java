package com.bingshanguxue.pda;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
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

    public void onEventMainThread(PDAScanManager.ScanBarcodeEvent event) {
        String barcode = event.getBarcode();
        ZLogger.d(String.format("isAcceptBarcodeEnabled=%b, barcode=%s",
                isAcceptBarcodeEnabled, barcode));

        if (isAcceptBarcodeEnabled && !StringUtils.isEmpty(barcode)) {
            if (barcode.startsWith("2") && barcode.length() == 13) {
                String plu = barcode.substring(1, 7);
                String weightStr = String.format("%s.%s", barcode.substring(7, 9), barcode.substring(9, 12));
                Double weight = Double.valueOf(weightStr);
                ZLogger.d(String.format("扫描生鲜商品条码：%s, PLU码：%s, 重量：%f",
                        barcode, plu, weight));
                onScanCode(plu);
            } else {
                ZLogger.d(String.format("扫描标准商品条码:%s", barcode));
                onScanCode(barcode);
            }
        }
    }

}

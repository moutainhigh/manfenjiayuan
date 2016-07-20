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

    private static final String TAG = "PDAScanFragment";

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

        if (isAcceptBarcodeEnabled && !StringUtils.isEmpty(barcode)){
            onScanCode(barcode);
        }
    }

}

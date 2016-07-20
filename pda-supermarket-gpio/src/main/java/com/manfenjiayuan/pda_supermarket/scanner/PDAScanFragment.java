package com.manfenjiayuan.pda_supermarket.scanner;

import com.manfenjiayuan.business.PDAScanManager;
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

    protected boolean isAcceptBarcodeEnabled = false;


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
        if (!isAcceptBarcodeEnabled){
            return;
        }
        String barcode = event.getBarcode();
        if (!StringUtils.isEmpty(barcode)){
            ZLogger.d(String.format("PDAScanFragment: ScanBarcodeEvent(%s)", barcode));
            onScanCode(barcode);
        }
        else {
            ZLogger.d("条码格式不正确");
        }

    }
}

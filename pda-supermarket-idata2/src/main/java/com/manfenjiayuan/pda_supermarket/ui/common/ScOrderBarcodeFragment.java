package com.manfenjiayuan.pda_supermarket.ui.common;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.QrCodeUtils;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 商城订单--条码
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScOrderBarcodeFragment extends BaseFragment {
    @Bind(R.id.iv_code_128)
    ImageView ivCode128;
    @Bind(R.id.tv_barcode)
    TextView tvBarcode;
    @Bind(R.id.iv_qr_code)
    ImageView ivQRCode;

    private ScOrder mScOrder = null;

    public static ScOrderBarcodeFragment newInstance(Bundle args) {
        ScOrderBarcodeFragment fragment = new ScOrderBarcodeFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invfind_barcode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        refresh(mScOrder);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 刷新条码
     * */
    private void refresh(ScOrder scOrder){
        mScOrder = scOrder;
        try {
            String barcode = scOrder != null ? mScOrder.getBarcode() : "000000000000000000";

            tvBarcode.setText(barcode);
            ivCode128.setImageBitmap(QrCodeUtils
                    .Create2DCode(barcode,
                            BarcodeFormat.CODE_128, 600, 200,
                            null));
            Drawable logo = ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher);
            ivQRCode.setImageBitmap(QrCodeUtils
                    .Create2DCode(barcode,
                            BarcodeFormat.QR_CODE, 400, 400,
                            null));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    /**
     * 验证
     */
    public void onEventMainThread(ScOrderEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ScOrderEvent(%d)", eventId));
        switch (eventId) {
            case ScOrderEvent.EVENT_ID_UPDATE: {
                ScOrder scOrder = (ScOrder) args.getSerializable(ScOrderEvent.EXTRA_KEY_SCORDER);
                refresh(scOrder);
            }
            break;

        }
    }

}

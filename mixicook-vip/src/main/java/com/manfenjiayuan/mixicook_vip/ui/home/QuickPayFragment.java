package com.manfenjiayuan.mixicook_vip.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.QrCodeUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * QuickPay
 * Created by bingshanguxue on 6/28/16.
 */
public class QuickPayFragment extends BaseFragment {

    @Bind(R.id.iv_code_128)
    ImageView ivCode128;
    @Bind(R.id.iv_qr_code)
    ImageView ivQRCode;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_quickpay;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        toolbar.setTitle("支付");

        try {
            ZLogger.d(String.format("userid=%d, guid=%d", MfhLoginService.get().getUserId(),
                    MfhLoginService.get().getCurrentGuId()));
            ivCode128.setImageBitmap(QrCodeUtils
                    .Create2DCode(String.valueOf(MfhLoginService.get().getCurrentGuId()),
                            BarcodeFormat.CODE_128, 800, 240,
                            null));
            Drawable logo = ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher);
            ivQRCode.setImageBitmap(QrCodeUtils
                    .Create2DCode(String.valueOf(MfhLoginService.get().getCurrentGuId()),
                            BarcodeFormat.QR_CODE, 500, 500,
                            null));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_topup)
    public void topup(){
        DialogUtil.showHint("充值");
    }


}

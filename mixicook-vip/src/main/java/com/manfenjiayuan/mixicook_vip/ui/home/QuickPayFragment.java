package com.manfenjiayuan.mixicook_vip.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.mfh.framework.core.utils.QrCodeUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * QuickPay
 * Created by bingshanguxue on 6/28/16.
 */
public class QuickPayFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.iv_code_128)
    ImageView ivCode128;
    @BindView(R.id.tv_barcode)
    TextView tvBarcode;
    @BindView(R.id.iv_qr_code)
    ImageView ivQRCode;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_quickpay;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("支付");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        try {
            String barcode = MUtils.genQuickpamentCode(String.valueOf(MfhLoginService.get().getHumanId()), 15);
            ivCode128.setImageBitmap(QrCodeUtils
                    .Create2DCode(barcode, BarcodeFormat.CODE_128, 800, 240, null));
            tvBarcode.setText(barcode);
            Drawable logo = ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher);
            ivQRCode.setImageBitmap(QrCodeUtils
                    .Create2DCode(barcode, BarcodeFormat.QR_CODE, 500, 500, null));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_topup)
    public void topup(){
        ActivityRoute.redirect2Topup(getActivity());
    }


}

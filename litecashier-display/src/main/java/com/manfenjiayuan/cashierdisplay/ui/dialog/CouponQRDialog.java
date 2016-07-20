package com.manfenjiayuan.cashierdisplay.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.WriterException;
import com.manfenjiayuan.cashierdisplay.R;
import com.mfh.framework.core.utils.BitmapUtils;
import com.mfh.framework.core.utils.QrCodeUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;


/**
 * 输入手机号
 *
 * @author NAT.ZZN(bingshanguxue)
 */
public class CouponQRDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onQuantityChanged(Double quantity);
    }

    private OnResponseCallback mListener;
    private View rootView;
    private ImageView ivQR;

    private CouponQRDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private CouponQRDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_coupon, null);
//        ButterKnife.bind(rootView);

        ivQR = (ImageView) rootView.findViewById(R.id.iv_qr);
        try {
//            ivQR.setImageBitmap(QrCodeUtils.Create2DCode("http://weibo.com/bingshanguxue"));
            Drawable logo = context.getResources().getDrawable(R.mipmap.ic_launcher);
            ivQR.setImageBitmap(QrCodeUtils.Create2DCode("http://www.manfenjiayuan.cn", 300, 300,
                    null));
        } catch (WriterException e) {
            e.printStackTrace();
        }

        setContent(rootView, 0);
    }


    public CouponQRDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);

//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void init(OnResponseCallback callback) {
        this.mListener = callback;
    }

    /**
     * drawable 转换成bitmap
     */
    static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();// 取drawable的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中
        return bitmap;
    }
}

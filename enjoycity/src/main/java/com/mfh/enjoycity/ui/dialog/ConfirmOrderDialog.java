package com.mfh.enjoycity.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.framework.uikit.dialog.CommonDialog;


/**
 * 对话框－－确认订单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class ConfirmOrderDialog extends CommonDialog {

    private View rootView;
    private TextView tvContent;

    private static final int[] ATTRS = new int[]{
            android.R.attr.actionBarSize
    };

    private ConfirmOrderDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ConfirmOrderDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialog_confirm_order, null);
//        ButterKnife.bind(rootView);

        tvContent = (TextView)rootView.findViewById(R.id.tv_content);

        setContent(rootView, 0);
    }

    public ConfirmOrderDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.TOP);


        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
//        p.width = d.getWidth() * 2 / 3;
//        p.y = DensityUtil.dip2px(getContext(), 44);

        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
        p.y = (int)a.getDimension(0, 44);
        a.recycle();

        getWindow().setAttributes(p);
    }

    public void init(String text) {
        this.tvContent.setText(text);
    }
}

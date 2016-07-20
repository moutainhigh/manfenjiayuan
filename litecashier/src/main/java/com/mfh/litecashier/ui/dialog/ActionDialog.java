package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.dialog.CommonDialog;

import org.apache.commons.lang3.StringUtils;


/**
 * 
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class ActionDialog extends CommonDialog {

    private Button btnAction1, btnAction2, btnAction3;

    private View rootView;

    private ActionDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ActionDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_action, null);
//        ButterKnife.bind(rootView);

        btnAction1 = (Button) rootView.findViewById(R.id.button_action1);
        btnAction2 = (Button) rootView.findViewById(R.id.button_action2);
        btnAction3 = (Button) rootView.findViewById(R.id.button_action3);

        setContent(rootView, 0);
    }

    public ActionDialog(Context context) {
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
    }

    public void setAction1(String name, View.OnClickListener listener){
        if (StringUtils.isEmpty(name)){
            btnAction1.setVisibility(View.GONE);
            return;
        }

        btnAction1.setVisibility(View.VISIBLE);
        btnAction1.setText(name);
        btnAction1.setOnClickListener(listener);
    }

    public void setAction2(String name, View.OnClickListener listener){
        if (StringUtils.isEmpty(name)){
            btnAction2.setVisibility(View.GONE);
        }

        btnAction2.setText(name);
        btnAction2.setVisibility(View.VISIBLE);
        btnAction2.setOnClickListener(listener);
    }

    public void setAction3(String name, View.OnClickListener listener){
        if (StringUtils.isEmpty(name)){
            btnAction3.setVisibility(View.GONE);
        }

        btnAction3.setText(name);
        btnAction3.setVisibility(View.VISIBLE);
        btnAction3.setOnClickListener(listener);
    }
}

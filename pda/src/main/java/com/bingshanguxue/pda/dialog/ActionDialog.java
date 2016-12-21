package com.bingshanguxue.pda.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;

/**
 * <h1>快捷帐号支付：采购收货单</h1><br>
 *
 * 1.支付完成 {@link DialogClickListener#onAction1Click()}<br>
 * 2.支付异常 {@link DialogClickListener#onAction2Click()}<br>
 * 2.支付异常 {@link DialogClickListener#onAction3Click()}<br>
 *
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class ActionDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle, tvSubTitle;
    private Button btnAction1, btnAction2, btnAction3;

    public interface DialogClickListener {
        /**Action1*/
        void onAction1Click();
        /**Action2*/
        void onAction2Click();
        /**Action3*/
        void onAction3Click();
    }

    private DialogClickListener mListener;

    private ActionDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ActionDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_action, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvSubTitle = (TextView) rootView.findViewById(R.id.tv_subtitle);
        btnAction1 = (Button) rootView.findViewById(R.id.button_action1);
        btnAction2 = (Button) rootView.findViewById(R.id.button_action2);
        btnAction3 = (Button) rootView.findViewById(R.id.button_action3);

        btnAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onAction1Click();
                }
            }
        });
        btnAction2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onAction2Click();
                }
            }
        });
        btnAction3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onAction3Click();
                }
            }
        });

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

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth() * 2 / 3;
        p.height = DensityUtil.dip2px(getContext(), 420);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();
    }

    /**
     * 初始化
     * */
    public void init(String title, String subTitle, DialogClickListener callback) {
        this.tvTitle.setText(title);
        this.tvSubTitle.setText(subTitle);
        this.mListener = callback;
    }

    public void registerActions(String action1, String action2, String action3){
        if (!StringUtils.isEmpty(action1)){
            btnAction1.setText(action1);
            btnAction1.setVisibility(View.VISIBLE);
        }
        else{
            btnAction1.setVisibility(View.GONE);
        }
        if (!StringUtils.isEmpty(action2)){
            btnAction2.setText(action2);
            btnAction2.setVisibility(View.VISIBLE);
        }
        else{
            btnAction2.setVisibility(View.GONE);
        }
        if (!StringUtils.isEmpty(action3)){
            btnAction3.setText(action3);
            btnAction3.setVisibility(View.VISIBLE);
        }
        else{
            btnAction3.setVisibility(View.GONE);
        }
    }
}

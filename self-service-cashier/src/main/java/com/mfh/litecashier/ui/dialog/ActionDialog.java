package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;


/**
 * @author NAT.ZZN(bingshanguxue)
 */
public class ActionDialog extends CommonDialog {

    private TextView tvTitle, tvSubTitle;
    private FloatingActionButton ivHeader;
    private Button btnAction1, btnAction2, btnAction3;

    public interface OnActionClickListener {
        void onAction1();

        void onAction2();

        void onAction3();
    }

    private OnActionClickListener mOnActionClickListener;

    private View rootView;

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
        ivHeader = (FloatingActionButton) rootView.findViewById(R.id.iv_header);
        btnAction1 = (Button) rootView.findViewById(R.id.button_action1);
        btnAction2 = (Button) rootView.findViewById(R.id.button_action2);
        btnAction3 = (Button) rootView.findViewById(R.id.button_action3);

        btnAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mOnActionClickListener != null) {
                    mOnActionClickListener.onAction1();
                }
            }
        });
        btnAction2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mOnActionClickListener != null) {
                    mOnActionClickListener.onAction2();
                }
            }
        });
        btnAction3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mOnActionClickListener != null) {
                    mOnActionClickListener.onAction3();
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

    public void initialize(String title, String subTitle,
                           OnActionClickListener onActionClickListener) {
        initialize(title, subTitle, -1, onActionClickListener);
    }

    public void initialize(String title, String subTitle, int headResId,
                           OnActionClickListener onActionClickListener) {
        this.tvTitle.setText(title);
        if (!StringUtils.isEmpty(subTitle)) {
            this.tvSubTitle.setText(subTitle);
            this.tvSubTitle.setVisibility(View.VISIBLE);
        } else {
            this.tvSubTitle.setVisibility(View.GONE);
        }

        if (headResId != -1) {
            this.ivHeader.setImageResource(headResId);
            this.ivHeader.setVisibility(View.VISIBLE);
        } else {
            this.ivHeader.setVisibility(View.GONE);
        }

        this.mOnActionClickListener = onActionClickListener;
    }

    public void setActions(String action1, String action2, String action3) {
        if (StringUtils.isEmpty(action1)) {
            btnAction1.setVisibility(View.GONE);
        } else {
            btnAction1.setVisibility(View.VISIBLE);
            btnAction1.setText(action1);
        }

        if (StringUtils.isEmpty(action2)) {
            btnAction2.setVisibility(View.GONE);
        } else {
            btnAction2.setVisibility(View.VISIBLE);
            btnAction2.setText(action2);
        }

        if (StringUtils.isEmpty(action3)) {
            btnAction3.setVisibility(View.GONE);
        } else {
            btnAction3.setVisibility(View.VISIBLE);
            btnAction3.setText(action3);
        }

    }

}

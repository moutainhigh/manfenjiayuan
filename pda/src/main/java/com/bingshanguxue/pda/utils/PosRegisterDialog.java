package com.bingshanguxue.pda.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.manfenjiayuan.business.presenter.PosRegisterPresenter;
import com.manfenjiayuan.business.view.IPosRegisterView;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.widget.AvatarView;


/**
 * <h1>设备注册</h1><br>
 *
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class PosRegisterDialog extends CommonDialog implements IPosRegisterView {

    private View rootView;
    private TextView tvTitle, tvSubTitle;
    private AvatarView ivHeader;
    private ImageButton btnClose;
    private Button btnCreate, btnCancel;
    private ProgressBar progressBar;

    @Override
    public void onRegisterPlatProcess() {
        progressBar.setVisibility(View.VISIBLE);
        if (mListener != null){
            mListener.onProcess();
        }
    }

    @Override
    public void onRegisterPlatError(String errorMsg) {
        btnCreate.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        if (mListener != null){
            mListener.onError();
        }
    }

    @Override
    public void onRegisterPlatSuccess(String terminalId) {
        btnCreate.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        if (mListener != null){
            mListener.onSuccess();
        }
        dismiss();
    }

    @Override
    public void onPlatUpdate() {

    }

    public interface DialogClickListener {
        void onProcess();
        /**注册*/
        void onSuccess();
        /**更新*/
        void onError();
        void onCancel();
    }

    private DialogClickListener mListener;

    private PosRegisterPresenter mPosRegisterPresenter;

    private PosRegisterDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private PosRegisterDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_posregister, null);
//        ButterKnife.bind(rootView);

        mPosRegisterPresenter = new PosRegisterPresenter(this);


        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        tvSubTitle = (TextView) rootView.findViewById(R.id.tv_subtitle);
        btnCreate = (Button) rootView.findViewById(R.id.button_create);
        btnCancel = (Button) rootView.findViewById(R.id.button_cancel);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreate.setEnabled(false);
                mPosRegisterPresenter.create();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onCancel();
                }
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mListener != null){
                    mListener.onCancel();
                }
            }
        });

        setContent(rootView, 0);
    }

    public PosRegisterDialog(Context context) {
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
        p.width = DensityUtil.dip2px(getContext(), 400);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);
    }

    @Override
    public void show() {
        super.show();
//        ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
    }

    public void init(String title, boolean isCancelEnabled, DialogClickListener callback) {
//        this.dialogType = type;
        this.mListener = callback;
        this.tvTitle.setText(title);

        String terminalId = SharedPreferencesManager.getTerminalId();
        if (StringUtils.isEmpty(terminalId)) {
            this.tvSubTitle.setText("检测到您的设备还没有注册，可能会影响POS机的正常使用，请尽快注册");
            btnCreate.setText("注册");
            btnCancel.setVisibility(View.GONE);
        } else {
            this.tvSubTitle.setText(String.format("设备编号: %s,\n是否重新注册设备？", terminalId));
            btnCreate.setText("重置");
            btnCancel.setVisibility(View.VISIBLE);
        }

        if (isCancelEnabled){
            btnClose.setVisibility(View.VISIBLE);
        }
        else{
            btnClose.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
    }
}

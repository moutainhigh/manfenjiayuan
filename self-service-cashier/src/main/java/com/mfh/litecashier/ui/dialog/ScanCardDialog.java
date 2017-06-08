package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;


/**
 * <h1>刷卡对话框</h1><br>
 *
 * 1.收银员离开 {@link DialogClickListener#onLock()}<br>
 * 2.收银员交接班 {@link DialogClickListener#onHandOver()}<br>
 * 3.收银员日结 {@link DialogClickListener#onDailySettle()}<br>
 * 4.收银员退出账号 {@link DialogClickListener#onLogout()}<br>
 *
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class ScanCardDialog extends CommonDialog {

    private EditText etCardNo;
    private ImageButton ibClose;

    public interface DialogViewListener {
        void onCardDetected(String cardNo);
        void onCancel();
    }
    private DialogViewListener mListener;

    private View rootView;


    private ScanCardDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ScanCardDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_scancard, null);
//        ButterKnife.bind(rootView);

        etCardNo = (EditText) rootView.findViewById(R.id.et_card_id);
        ibClose = (ImageButton) rootView.findViewById(R.id.button_close);
        etCardNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (mListener != null){
                            mListener.onCardDetected(etCardNo.getText().toString());
                        }
                        dismiss();
                    }
                    return true;
                }
//                return true;
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null){
                    mListener.onCancel();
                }
            }
        });

        setContent(rootView, 0);
    }

    public ScanCardDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }
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

    @Override
    public void show() {
        super.show();
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        etCardNo.getText().clear();
    }

    public void init(DialogViewListener callback) {
        this.mListener = callback;
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }
}

package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;


/**
 * 文本输入
 *
 * @author NAT.ZZN(bingshanguxue)
 */
public class TextInputDialog extends CommonDialog {

   private boolean isEmptyAllowed = false;//空内容是否允许提交

   public interface OnTextInputListener {
        void onCancel();
        void onConfirm(String text);
    }

    private OnTextInputListener mListener;
    private View rootView;
    private TextView tvTitle;
    private EditText etContent;


    private TextInputDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private TextInputDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_inputtext, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTitle.setText("数量");
        etContent = (EditText) rootView.findViewById(R.id.et_content);
//        etContent.setCursorVisible(false);//隐藏光标
        etContent.setFocusable(true);
        etContent.setFocusableInTouchMode(true);
        etContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submit();
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (SharedPrefesManagerFactory.isSoftKeyboardEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etContent);
//                    } else {
//                        DeviceUtils.hideSoftInput(getContext(), etContent);
//                    }
                }
                etContent.requestFocus();
                etContent.setSelection(etContent.length());
                //返回true,不再继续传递事件
                return true;
            }
        });


        rootView.findViewById(R.id.button_header_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
        rootView.findViewById(R.id.button_footer_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        setContent(rootView, 0);
    }

    public TextInputDialog(Context context) {
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
//        p.width = d.getWidth() * 2 / 3;
        p.width = DensityUtil.dip2px(getContext(), 400);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();

//        DeviceUtils.showSoftInput(getContext(), etContent);

        etContent.requestFocus();
        etContent.setSelection(etContent.length());
        DeviceUtils.toggleSoftInput(getContext());
    }

    public void initialize(String title, String hint, boolean isEmptyAllowed, OnTextInputListener listener) {
        this.isEmptyAllowed = isEmptyAllowed;
        this.mListener = listener;

        tvTitle.setText(title);
        etContent.setHint(hint);
        etContent.getText().clear();
        etContent.setSelection(etContent.length());
        etContent.requestFocus();
    }


    private void submit() {
        DeviceUtils.hideSoftInput(getContext(), etContent);
        String content = etContent.getText().toString();
        if (StringUtils.isEmpty(content) && !isEmptyAllowed) {
            DialogUtil.showHint("输入内容不能为空");
            return;
        }

        dismiss();
        if (mListener != null) {
            mListener.onConfirm(content);
        }
    }

}

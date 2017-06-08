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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;


/**
 * 对话框－－修改前台类目
 * 
 * @author bingshanguxue
 * 
 */
public class ModifyLocalCategoryDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private EditText etName;
    private Button btnUpdate, btnDelete;
    private ImageButton btnClose;

    private PosLocalCategoryEntity mCategoryEntity = null;
    private String rawText;

    public interface DialogListener{
        void onUpdate(PosLocalCategoryEntity categoryEntity, final String nameCn);
        void onDelete(PosLocalCategoryEntity categoryEntity);

    }
    private DialogListener listener;


    private ModifyLocalCategoryDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ModifyLocalCategoryDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_modify_localcategory, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        etName = (EditText) rootView.findViewById(R.id.et_name);
        btnUpdate = (Button) rootView.findViewById(R.id.button_update);
        btnDelete = (Button) rootView.findViewById(R.id.button_delete);

        tvTitle.setText("编辑栏目");

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etName, InputMethodManager.SHOW_IMPLICIT);
        etName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.showSoftInput(getContext(), etName);
                }
                etName.requestFocus();
                etName.setSelection(etName.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        //获取焦点后自动弹出键盘
//        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    // request keyboard// request keyboard
//                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                }
//            }
//        });
        etName.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("setOnKeyListener(etQuery):keyCode=%d, action=%d",
                        keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //按下回车键后会执行两次，
                    // 猜测一，输入框会自动捕获回车按键，自动切换焦点到下一个控件；
                    // 猜测二，通过打印日志观察发现，每次按下按键，都会监听到两次键盘事件，重复导致。
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        doUpdate();
                        DeviceUtils.hideSoftInput(getContext(), etName);
                    }
                    return true;
                }


                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String queryText = etName.getText().toString();

                if (ObjectsCompact.equals(rawText, queryText)){
                    dismiss();
                }
                else{
                    dismiss();

                    if (listener != null){
                        listener.onUpdate(mCategoryEntity, queryText);
                    }
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (listener != null){
                    listener.onDelete(mCategoryEntity);
                }
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public ModifyLocalCategoryDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
//        p.width = d.getWidth() * 2 / 3;
        p.width = DensityUtil.dip2px(getContext(), 400);
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void init(PosLocalCategoryEntity categoryEntity, String rawText, DialogListener dialogListener){
        this.mCategoryEntity = categoryEntity;
        this.rawText = rawText;
        this.listener = dialogListener;

        this.etName.setText(rawText);
        this.etName.setHint(rawText);
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (etName != null){
            etName.getText().clear();
        }
    }

    @Override
    public void show() {
        super.show();

        etName.requestFocus();
        etName.setSelection(etName.length());
//        DeviceUtils.showSoftInput(getContext(), etName);
//        DeviceUtils.hideSoftInput(getOwnerActivity());
//        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(etName, InputMethodManager.SHOW_IMPLICIT);

        DeviceUtils.toggleSoftInput(getContext());
    }

}

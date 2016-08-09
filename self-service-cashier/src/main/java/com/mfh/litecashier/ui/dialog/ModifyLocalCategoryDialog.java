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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.bingshanguxue.cashier.database.service.PosLocalCategoryService;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
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

    public interface DialogListener{
        void onComplete();
    }
    private DialogListener listener;


    private ModifyLocalCategoryDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ModifyLocalCategoryDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_modify_localcategory, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        etName = (EditText) rootView.findViewById(R.id.et_name);
        btnUpdate = (Button) rootView.findViewById(R.id.button_update);
        btnDelete = (Button) rootView.findViewById(R.id.button_delete);

        tvTitle.setText("编辑栏目");

        etName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()){
                        DeviceUtils.showSoftInput(getContext(), etName);
                    }
                    else{
                        DeviceUtils.hideSoftInput(getContext(), etName);
                    }
                }
                etName.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etName.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etQuery):keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //按下回车键后会执行两次，
                    // 猜测一，输入框会自动捕获回车按键，自动切换焦点到下一个控件；
                    // 猜测二，通过打印日志观察发现，每次按下按键，都会监听到两次键盘事件，重复导致。
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        doUpdate();
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
                doUpdate();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDelete();
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

        getWindow().setGravity(Gravity.CENTER);


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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void init(PosLocalCategoryEntity categoryEntity, DialogListener dialogListener){
        this.mCategoryEntity = categoryEntity;
        this.listener = dialogListener;

        this.etName.setText(mCategoryEntity.getName());
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
        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    /**
     * 修改栏目名称
     * */
    private void doUpdate(){
        final String queryText = etName.getText().toString();
        if (StringUtils.isEmpty(queryText)){
            DialogUtil.showHint("栏目名称不能为空");
            return;
        }


        if (mCategoryEntity == null){
            DialogUtil.showHint("类目无效");
            return;
        }

        mCategoryEntity.setName(queryText);
        PosLocalCategoryService.get().saveOrUpdate(mCategoryEntity);
        DialogUtil.showHint("修改成功");
        dismiss();
        if (listener != null){
            listener.onComplete();
        }
    }

    /**
     * 删除栏目
     * */
    private void doDelete(){
        if (mCategoryEntity == null){
            DialogUtil.showHint("类目无效");
            return;
        }


        PosLocalCategoryService.get().deleteById(String.valueOf(mCategoryEntity.getId()));
        DialogUtil.showHint("删除成功");
        dismiss();

        if (listener != null){
            listener.onComplete();
        }
    }

}

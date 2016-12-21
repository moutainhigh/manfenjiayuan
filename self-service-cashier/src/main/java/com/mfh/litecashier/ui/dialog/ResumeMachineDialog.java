package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.database.entity.CompanyHumanEntity;
import com.mfh.framework.core.utils.EncryptUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.litecashier.R;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.bingshanguxue.vector_uikit.widget.AvatarView;


/**
 * 恢复/切换账号(交接班)
 * 
 * @author NAT.ZZN
 * 
 */
public class ResumeMachineDialog extends CommonDialog {


    public static final int DTYPE_LOCK = 0;//锁定
    public static final int DTYPE_HANDOVER = 1;//交接班

    private View rootView;

    private TextView tvTitle;
    private ImageButton ibClose;

    private AvatarView ivHeader;
    private TextView tvUsername;
    private EditText etPassword;


    private int dialogType = DTYPE_LOCK;
    private CompanyHumanEntity human;

    public interface DialogClickListener {
        void onChangeHuman();
    }
    private DialogClickListener mListener;


    private ResumeMachineDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ResumeMachineDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_resume_machine, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTitle.setText("锁定");
        ibClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ibClose.setVisibility(View.GONE);
        rootView.findViewById(R.id.button_footer_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        tvUsername = (TextView) rootView.findViewById(R.id.tv_username);
        etPassword = (EditText) rootView.findViewById(R.id.et_password);

        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
//        etPassword.setCursorVisible(false);//隐藏光标
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etPassword);
                }
                etPassword.requestFocus();
                etPassword.setSelection(etPassword.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                etBarCode.requestFocus();
//                etBarCode.setFocusableInTouchMode(true);

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP){
                        submit();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });

        setContent(rootView, 0);
    }

    public ResumeMachineDialog(Context context) {
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

    @Override
    public void show() {
        super.show();
        if (dialogType == DTYPE_LOCK){
            tvTitle.setText("锁定");
            ibClose.setVisibility(View.GONE);
        }
        else if (dialogType == DTYPE_HANDOVER){
            tvTitle.setText("交接班");
            ibClose.setVisibility(View.VISIBLE);
        }

        if (human != null){
            ivHeader.setAvatarUrl(human.getHeaderUrl());
            tvUsername.setText(human.getName());
        }
        else{
            ivHeader.setImageResource(R.drawable.chat_tmp_user_head);
            tvUsername.setText("");
        }
        etPassword.getText().clear();

        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        etPassword.getText().clear();
    }

    public void init(int dialogType, CompanyHumanEntity human, DialogClickListener listener){
        this.human = human;
        this.dialogType = dialogType;
        this.mListener = listener;
    }

    private void submit(){
        String password = etPassword.getText().toString();
        if (StringUtils.isEmpty(password)){
            DialogUtil.showHint(CashierApp.getAppContext(), "密码不能为空");
            return;
        }

        if (dialogType == DTYPE_LOCK){
            if (password.equals(human.getPassword())) {
                //密码正确，恢复机器使用
                dismiss();
            }else{
                DialogUtil.showHint("密码错误！");
            }
        }else if (dialogType == DTYPE_HANDOVER){
            //验证密码
            if (EncryptUtil.validPwd(human.getPassword(), human.getSalt(), password)){

                MfhLoginService.get().changeHuman(human.getHumanId(), human.getUserId(),
                        human.getUserName(), password);

                dismiss();

                if (mListener != null){
                    mListener.onChangeHuman();
                }
            }
            else{
                DialogUtil.showHint(CashierApp.getAppContext(), "密码不正确");
            }
        }
    }

}

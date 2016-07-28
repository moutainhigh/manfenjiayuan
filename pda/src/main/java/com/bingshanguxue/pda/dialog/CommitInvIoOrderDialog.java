package com.bingshanguxue.pda.dialog;


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

import com.bingshanguxue.pda.R;
import com.bingshanguxue.vector_user.bean.Human;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.dialog.CommonDialog;

/**
 * 提交出入库单
 * 
 * @author bingshanguxue
 * 
 */
public class CommitInvIoOrderDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private EditText etVehicle;
    private EditText etPhonenumber;
    private Button btnSubmit;
    private ImageButton btnClose;

    private Human mHuman;//快递代收，快递员信息

    public interface DialogListener{
        void onCancel();
        void onNextStep(String vehicle, String phonenumber);
    }
    private DialogListener listener;

    private CommitInvIoOrderDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private CommitInvIoOrderDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_commit_invioorder, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        etVehicle = (EditText) rootView.findViewById(R.id.et_vehicle);
        etPhonenumber = (EditText) rootView.findViewById(R.id.et_phonenumber);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

//        etVehicle.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                //自动搜索
//                doQuery();
//            }
//        });
        etVehicle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()){
                        DeviceUtils.showSoftInput(getContext(), etVehicle);
                    }
                    else{
                        DeviceUtils.hideSoftInput(getContext(), etVehicle);
                    }
                }
                etVehicle.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPhonenumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()){
                        DeviceUtils.showSoftInput(getContext(), etPhonenumber);
                    }
                    else{
                        DeviceUtils.hideSoftInput(getContext(), etPhonenumber);
                    }
                }
                etPhonenumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPhonenumber.setOnKeyListener(new EditText.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etQuery):keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //按下回车键后会执行两次，
                    // 猜测一，输入框会自动捕获回车按键，自动切换焦点到下一个控件；
                    // 猜测二，通过打印日志观察发现，每次按下按键，都会监听到两次键盘事件，重复导致。
                    if (event.getAction() == MotionEvent.ACTION_UP && btnSubmit.isEnabled()){
//                        doSubmit();
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        btnSubmit.setVisibility(View.VISIBLE);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onCancel();
                }
                dismiss();
            }
        });

        tvTitle.setText("提交订单");
        setContent(rootView, 0);
    }

    public CommitInvIoOrderDialog(Context context) {
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
        p.height = d.getHeight();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    public void init(DialogListener listener){
        this.listener = listener;

        this.mHuman = null;
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (etVehicle != null){
            etVehicle.getText().clear();
        }
        if (etPhonenumber != null){
            etPhonenumber.getText().clear();
        }
    }

    @Override
    public void show() {
        super.show();


        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    private void doSubmit(){
        String vehicle = etVehicle.getText().toString();
        if (StringUtils.isEmpty(vehicle)){
            DialogUtil.showHint("车牌号不能为空");
            return;
        }

        String phoneNumber = etPhonenumber.getText().toString();
        if (listener != null){
            listener.onNextStep(vehicle, phoneNumber);
        }

        dismiss();
    }

//    //查询会员信息
//    private NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
//            NetProcessor.Processor<Human>>(
//            new NetProcessor.Processor<Human>() {
//                @Override
//                public void processResult(final IResponseData rspData) {
//                    if (rspData == null){
//                        ZLogger.d("未查询到结果");
//                        btnSubmit.setEnabled(false);
//                        return;
//                    }
//
//                    RspBean<Human> retValue = (RspBean<Human>) rspData;
//
//                    mHuman = retValue.getValue();
//                    refreshHumanInfo();
//
////                    final Human human = retValue.getValue();
////
////                    if(human != null){
////                        ZLogger.d("查询用户成功");
////                        DataCacheHelper.getInstance().setMfMemberInfo(human);
////
////                        Message msg = new Message();
////                        msg.what = MSG_REFRESH_HUMANINFO;
////                        msg.obj = human;
////                        uiHandler.sendMessage(msg);
////                    }else {
////                        ZLogger.d("查询用户失败");
////                        btnSubmit.setEnabled(false);
////                        DataCacheHelper.getInstance().setMfMemberInfo(null);
////                    }
//                }
//
//                @Override
//                protected void processFailure(Throwable t, String errMsg) {
//                    super.processFailure(t, errMsg);
//                    btnSubmit.setEnabled(false);
//                }
//            }
//            , Human.class
//            , CashierApp.getAppContext())
//    {
//    };

//    //查询
//    private NetCallBack.NetTaskCallBack findHumanResponseCallback = new NetCallBack.NetTaskCallBack<Human,
//            NetProcessor.Processor<Human>>(
//            new NetProcessor.Processor<Human>() {
//                @Override
//                public void processResult(final IResponseData rspData) {
//                    if (rspData == null){
//                        mHuman = null;
//                        refreshHumanInfo();
//                        return;
//                    }
//
//                    try{
//                        RspBean<Human> retValue = (RspBean<Human>) rspData;
//                        mHuman = retValue.getValue();
//                        refreshHumanInfo();
//
////                        getOwnerActivity().runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                if (human != null) {
////                                    ZLogger.d("查询用户che成功");
////                                    DataCacheHelper.getInstance().setCourier(human);
////
////                                    tvSubTitle.setText(human.getName());
////                                    ivHeader.setAvatarUrl(human.getHeadimageUrl());
//////                            etQuery.getText().clear();
////
////                                    btnSubmit.setEnabled(true);
////                                } else {
////                                    ZLogger.d("查询用户失败");
////                                }
////                            }
////                        });
//
//                    }catch(Exception ex){
//                        ZLogger.e("findHumanResponseCallback, " + ex.toString());
//                    }
//                }
//
//                @Override
//                protected void processFailure(Throwable t, String errMsg) {
//                    super.processFailure(t, errMsg);
//                    btnSubmit.setEnabled(false);
//                    mHuman = null;
//                }
//            }
//            , Human.class
//            , AppContext.getAppContext())
//    {
//    };

//    public void refreshHumanInfo(){
//        if (mHuman != null) {
//            ZLogger.d("查询用户成功");
//            tvSubTitle.setText(mHuman.getName());
//            ivHeader.setAvatarUrl(mHuman.getHeadimageUrl());
////                            etQuery.getText().clear();
//
//            btnSubmit.setEnabled(true);
//        } else {
//            ZLogger.d("查询用户失败");
//            tvSubTitle.setText("");
//            ivHeader.setAvatarUrl("");
//            btnSubmit.setEnabled(false);
//        }
//    }

//    private static final int MSG_REFRESH_HUMANINFO = 1;
//    private static final Handler uiHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case MSG_REFRESH_HUMANINFO:{
//                    refreshHumanInfo();
//                }
//                break;
//            }
//        }
//    };

//    private static class MyHandler extends Handler {
//        private final WeakReference<CommitInvIoOrderDialog> mDialog;
//        // ...
//        public MyHandler(CommitInvIoOrderDialog dialog) {
//            mDialog = new WeakReference<>(dialog);
//            //...
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case MSG_REFRESH_HUMANINFO:{
//                    Human human = (Human)msg.obj;
//                    mDialog.refreshHumanInfo(human);
//                }
//                break;
//            }
//        }
//        //...
//    }


}

package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.CashierApp;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.utils.DataCacheHelper;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.widget.AvatarView;


/**
 * 查询－－会员卡信息/快递代收
 * 
 * @author NAT.ZZN
 * 
 */
public class QueryDialog extends CommonDialog {

    public static final int DT_MEMBER_CARD = 0;
    public static final int DT_STOCK_DETAIL = 1;//物品明细
    public static final int DT_EXPRESS_TAKE = 2;
    public static final int DT_EXPRESS_COLLECTION = 3;
    public static final int DT_EXPRESS_PAY = 4;//收取快递保管费

    private View rootView;
    private TextView tvTitle;
    private AvatarView ivHeader;
    private TextView tvSubTitle;
    private TextView tvQueryTag;
    private EditText etQuery;
    private Button btnSubmit;
    private ImageButton btnClose;

    private Human mHuman;//快递代收，快递员信息

    public interface DialogListener{
        void query(String text);
        void onNextStep(String fee);
        void onNextStep(Human human);
        void onNextStep();
    }
    private DialogListener listener;


    private int dialogType = DT_MEMBER_CARD;

    private QueryDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private QueryDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_query, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_subHeader);
        tvSubTitle = (TextView) rootView.findViewById(R.id.tv_subtitle);
        tvQueryTag = (TextView) rootView.findViewById(R.id.tv_query_tag);
        etQuery = (EditText) rootView.findViewById(R.id.et_query_content);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);

        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //自动搜索
                doQuery();
            }
        });
        etQuery.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etQuery);
                }
                etQuery.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etQuery.setOnKeyListener(new EditText.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etQuery):keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //按下回车键后会执行两次，
                    // 猜测一，输入框会自动捕获回车按键，自动切换焦点到下一个控件；
                    // 猜测二，通过打印日志观察发现，每次按下按键，都会监听到两次键盘事件，重复导致。
                    if (event.getAction() == MotionEvent.ACTION_UP && btnSubmit.isEnabled()){
                        doSubmit();
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
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public QueryDialog(Context context) {
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

    public void init(int dialogType){
        init(dialogType, null);
    }

    public void init(int dialogType, DialogListener listener){
        this.dialogType = dialogType;
        this.listener = listener;

        this.mHuman = null;
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (etQuery != null){
            etQuery.getText().clear();
        }
    }

    @Override
    public void show() {
        super.show();

        //设置标题
        if (dialogType == DT_MEMBER_CARD){
            tvTitle.setText("会员");
            tvQueryTag.setText("用户账号");
            ivHeader.setBorderWidth(0);
            ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
            ivHeader.setImageResource(R.drawable.chat_tmp_user_head);
            tvSubTitle.setText("");
            etQuery.setInputType(InputType.TYPE_CLASS_NUMBER);
            etQuery.setHint("手机号");

            btnSubmit.setEnabled(false);
        }
        else if (dialogType == DT_EXPRESS_COLLECTION){
            tvTitle.setText("代收快递");
            tvQueryTag.setText("用户账号");
            ivHeader.setBorderWidth(0);
            ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
            ivHeader.setImageResource(R.drawable.chat_tmp_user_head);
            tvSubTitle.setText("");
            etQuery.setInputType(InputType.TYPE_CLASS_PHONE);
            etQuery.setHint("手机号");
            btnSubmit.setEnabled(false);
        }
        else if (dialogType == DT_EXPRESS_PAY){
            tvTitle.setText("付款");
            tvQueryTag.setText("收费金额");
            ivHeader.setBorderWidth(0);
            ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
            ivHeader.setImageResource(R.mipmap.ic_marker_money_large);
            tvSubTitle.setText("快递保管费");
            etQuery.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }else{
            tvTitle.setText("选择用户");
            tvQueryTag.setText("用户账号");
            ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
            tvSubTitle.setText(MfhLoginService.get().getHumanName());
        }

        etQuery.requestFocus();
        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    private void doQuery(){
        String queryText = etQuery.getText().toString();
        if (StringUtils.isEmpty(queryText)){
            return;
        }

        if (dialogType == DT_MEMBER_CARD) {
            //手机号11位
            if (queryText.length() == 11) {
                CashierApiImpl.findHumanByPhone(queryText, findMemberResponseCallback);
            }
        } else if (dialogType == DT_EXPRESS_COLLECTION) {
            //手机号11位
            if (queryText.length() == 11) {
                CashierApiImpl.findHumanByPhone(queryText, findHumanResponseCallback);
            }
        } else if (dialogType == DT_EXPRESS_PAY) {
            //手机号11位
//            String amount = s.toString();
        }
    }

    private void doSubmit(){
        if (dialogType == DT_EXPRESS_PAY){
            String fee = etQuery.getText().toString();
            if (!StringUtils.isEmpty(fee)){
                if (listener != null){
                    listener.onNextStep(fee);
                }
            }
        }
        else if (dialogType == DT_MEMBER_CARD || dialogType == DT_EXPRESS_COLLECTION){
            if (listener != null){
                listener.onNextStep(mHuman);
            }
        }else{
            if (listener != null){
                listener.onNextStep();
            }
        }

        dismiss();
    }

    /**
     * 查询取货码
     * */
//    private NetCallBack.NetTaskCallBack findHumanBySecretResponseCallback = new NetCallBack.NetTaskCallBack<String,
//            NetProcessor.Processor<String>>(
//            new NetProcessor.Processor<String>() {
//                @Override
//                public void processResult(IResponseData rspData) {
//                    try{
////                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
////                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"132880"}}
//                        RspValue<String> retValue = (RspValue<String>) rspData;
//                        String retStr = retValue.getValue();
//                        if (!StringUtils.isEmpty(retStr)){
//                            CashierApiImpl.findHumanByHumanId(retStr, findHumanResponseCallback);
//                        }else{
//                            ZLogger.d("查询取货码失败");
////                            DialogUtil.showHint("查询失败,取货码无效");
//                        }
//                    }catch(Exception ex){
//                        ZLogger.e("findHumanBySecretResponseCallback, " + ex.toString());
//                    }finally{
//                    }
//                }
//
//                @Override
//                protected void processFailure(Throwable t, String errMsg) {
//                    super.processFailure(t, errMsg);
//                    btnSubmit.setEnabled(false);
//                    DataCacheHelper.getInstance().setCourier(null);
//                }
//
//
//            }
//            , String.class
//            , CashierApp.getAppContext())
//    {
//    };


    //查询会员信息
    private NetCallBack.NetTaskCallBack findMemberResponseCallback = new NetCallBack.NetTaskCallBack<Human,
            NetProcessor.Processor<Human>>(
            new NetProcessor.Processor<Human>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    if (rspData == null){
                        ZLogger.d("未查询到结果");
                        btnSubmit.setEnabled(false);
                        DataCacheHelper.getInstance().setMfMemberInfo(null);
                        return;
                    }

                    RspBean<Human> retValue = (RspBean<Human>) rspData;

                    mHuman = retValue.getValue();
                    refreshHumanInfo();

//                    final Human human = retValue.getValue();
//
//                    if(human != null){
//                        ZLogger.d("查询用户成功");
//                        DataCacheHelper.getInstance().setMfMemberInfo(human);
//
//                        Message msg = new Message();
//                        msg.what = MSG_REFRESH_HUMANINFO;
//                        msg.obj = human;
//                        uiHandler.sendMessage(msg);
//                    }else {
//                        ZLogger.d("查询用户失败");
//                        btnSubmit.setEnabled(false);
//                        DataCacheHelper.getInstance().setMfMemberInfo(null);
//                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    btnSubmit.setEnabled(false);
                    DataCacheHelper.getInstance().setMfMemberInfo(null);
                }
            }
            , Human.class
            , CashierApp.getAppContext())
    {
    };

    //查询
    private NetCallBack.NetTaskCallBack findHumanResponseCallback = new NetCallBack.NetTaskCallBack<Human,
            NetProcessor.Processor<Human>>(
            new NetProcessor.Processor<Human>() {
                @Override
                public void processResult(final IResponseData rspData) {
                    if (rspData == null){
                        mHuman = null;
                        refreshHumanInfo();
                        return;
                    }

                    try{
                        RspBean<Human> retValue = (RspBean<Human>) rspData;
                        mHuman = retValue.getValue();
                        refreshHumanInfo();

//                        getOwnerActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (human != null) {
//                                    ZLogger.d("查询用户che成功");
//                                    DataCacheHelper.getInstance().setCourier(human);
//
//                                    tvSubTitle.setText(human.getName());
//                                    ivHeader.setAvatarUrl(human.getHeadimageUrl());
////                            etQuery.getText().clear();
//
//                                    btnSubmit.setEnabled(true);
//                                } else {
//                                    ZLogger.d("查询用户失败");
//                                }
//                            }
//                        });

                    }catch(Exception ex){
                        ZLogger.e("findHumanResponseCallback, " + ex.toString());
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    btnSubmit.setEnabled(false);
                    mHuman = null;
                }
            }
            , Human.class
            , CashierApp.getAppContext())
    {
    };

    public void refreshHumanInfo(){
        if (mHuman != null) {
            ZLogger.d("查询用户成功");
            tvSubTitle.setText(mHuman.getName());
            ivHeader.setAvatarUrl(mHuman.getHeadimageUrl());
//                            etQuery.getText().clear();

            btnSubmit.setEnabled(true);
        } else {
            ZLogger.d("查询用户失败");
            tvSubTitle.setText("");
            ivHeader.setAvatarUrl("");
            btnSubmit.setEnabled(false);
        }

        DataCacheHelper.getInstance().setMfMemberInfo(mHuman);
    }

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
//        private final WeakReference<QueryDialog> mDialog;
//        // ...
//        public MyHandler(QueryDialog dialog) {
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

package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.utils.DataCacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;


/**
 * 银联参数设置
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class UmsipsDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private EditText etIp, etPort, etMchtId, etTermId;
    private Spinner mPortSpinner, mBaudrateSpinner;
    private Button btnSubmit;
    private ImageButton btnClose;
    private ProgressBar progressBar;

    private ArrayAdapter<String> aspnDevices;
    private ArrayAdapter<CharSequence> adapter;

    public interface onDialogClickListener {
        void onDatasetChanged();
    }
    private onDialogClickListener mListener;


    private UmsipsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private UmsipsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_umsips, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        etIp = (EditText) rootView.findViewById(R.id.et_ip);
        etPort = (EditText) rootView.findViewById(R.id.et_port);
        etMchtId = (EditText) rootView.findViewById(R.id.et_mchtId);
        etTermId = (EditText) rootView.findViewById(R.id.et_termId);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        mPortSpinner = (Spinner) rootView.findViewById(R.id.spinner_port);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
//        mPortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view,
//                                       int position, long id) {
//                String str = parent.getItemAtPosition(position).toString();
//                mPortSpinner.setPrompt(str);
//                ZLogger.d("onItemSelected " + position);
//                if (position == 0) {
//                    timeAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mTimes1);
//                } else {
//                    timeAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mTimes2);
//                }
//
//                mBaudrateSpinner.setAdapter(timeAdapter);
//                mBaudrateSpinner.setPrompt(timeAdapter.getItem(0));
////                timeAdapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO Auto-generated method stub
//                ZLogger.d("onNothingSelected ");
//            }
//        });
        mBaudrateSpinner = (Spinner) rootView.findViewById(R.id.spinner_baudrate);
//        mBaudrateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view,
//                                       int position, long id) {
//                String str = parent.getItemAtPosition(position).toString();
//                mBaudrateSpinner.setPrompt(str);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO Auto-generated method stub
//            }
//        });

        aspnDevices = new ArrayAdapter<>(context,
                R.layout.mfh_spinner_item_text, DataCacheHelper.getInstance().getComDevicesPath());
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPortSpinner.setAdapter(aspnDevices);
        mPortSpinner.setSelection(0);

        adapter = ArrayAdapter.createFromResource(context,
                R.array.baudrates_value, R.layout.mfh_spinner_item_text);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBaudrateSpinner.setAdapter(adapter);
        mBaudrateSpinner.setSelection(0);

        tvTitle.setText("银联参数设置");

//        etIp.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    DeviceUtils.hideSoftInput(getContext(), etIp);
//                }
//                etIp.requestFocus();
////                etInput.setSelection(etInput.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
//        etAmount.setFilters(new InputFilter[]{new DecimalInputFilter(2)});
//        etPort.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    DeviceUtils.hideSoftInput(getContext(), etPort);
//                }
//                etPort.requestFocus();
////                etInput.setSelection(etInput.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
//        etTermId.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    DeviceUtils.hideSoftInput(getContext(), etTermId);
//                }
//                etTermId.requestFocus();
////                etInput.setSelection(etInput.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
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

    public UmsipsDialog(Context context) {
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

    @Override
    public void show() {
        super.show();

        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    public void init(onDialogClickListener listener){
        this.mListener = listener;
        refresh();
    }

    /**
     * 刷新会员信息
     */
    private void refresh(){
        etIp.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_IP, "10.139.93.98"));
        etPort.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_PORT, "19003"));
        etMchtId.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_MCHTID, "898320554115217"));
        etTermId.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_TERMID));

        mPortSpinner.setSelection(aspnDevices.getPosition(SerialManager.getUmsipsPort()));
        mBaudrateSpinner.setSelection(adapter.getPosition(SerialManager.getUmsipsBaudrate()));
    }

    private void submit() {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        String ip = etIp.getText().toString();
        if (StringUtils.isEmpty(ip)) {
            DialogUtil.showHint("请输入IP");
            btnSubmit.setEnabled(true);
            return;
        }

        String port = etPort.getText().toString();
        if (StringUtils.isEmpty(port)) {
            DialogUtil.showHint("请输入端口号");
            btnSubmit.setEnabled(true);
            return;
        }

        String mchtId = etMchtId.getText().toString();
        if (StringUtils.isEmpty(mchtId)) {
            DialogUtil.showHint("请输入商户号");
            btnSubmit.setEnabled(true);
            return;
        }

        String termId = etTermId.getText().toString();
        if (StringUtils.isEmpty(termId)) {
            DialogUtil.showHint("请输入终端编号");
            btnSubmit.setEnabled(true);
            return;
        }

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_IP, ip);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_MCHTID, mchtId);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_PORT, port);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_TERMID, termId);
        SerialManager.setUmsipsPort(mPortSpinner.getSelectedItem().toString());
        SerialManager.setUmsipsBaudrate(mBaudrateSpinner.getSelectedItem().toString());

        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);

        if (mListener != null){
            mListener.onDatasetChanged();
        }

        dismiss();
    }

}

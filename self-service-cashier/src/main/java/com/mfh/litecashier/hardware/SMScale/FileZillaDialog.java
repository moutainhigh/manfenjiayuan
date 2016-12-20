package com.mfh.litecashier.hardware.SMScale;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.Encoding;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.utils.IpInputFilter;
import com.mfh.litecashier.R;


/**
 * FileZilla参数设置
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class FileZillaDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private EditText etIp, etPort, etUsername, etPassword;
    private Spinner mEncodingSpinner;
    private ArrayAdapter<String> soapSpinnerAdapter;
    private TextView tvSmscaleCursor;
    private SwitchCompat mSwitchSyncMode;
    private Button btnSubmit;
    private ImageButton btnClose;
    private ProgressBar progressBar;


    public interface DialogViewClickListener{
        void onSubmit();
    }
    private DialogViewClickListener mDialogViewClickListener;

    private FileZillaDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private FileZillaDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_filezilla, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        etIp = (EditText) rootView.findViewById(R.id.et_ip);
        etPort = (EditText) rootView.findViewById(R.id.et_port);
        etUsername = (EditText) rootView.findViewById(R.id.et_username);
        etPassword = (EditText) rootView.findViewById(R.id.et_password);
        mEncodingSpinner = (Spinner) rootView.findViewById(R.id.spinner_encoding);
        tvSmscaleCursor = (TextView) rootView.findViewById(R.id.tv_smscale_cursor);
        mSwitchSyncMode = (SwitchCompat) rootView.findViewById(R.id.switchCompat_sync_mode);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
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

        tvTitle.setText("File Zilla");

        etIp.setFilters(new IpInputFilter[]{new IpInputFilter()});
        etIp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etIp);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etIp);
                    }
                }
                etIp.requestFocus();
                etIp.setSelection(etIp.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        soapSpinnerAdapter = new ArrayAdapter<>(context, R.layout.mfh_spinner_item_text,
                SMScaleSyncManager2.ENCODING_CHARSET_SET);
        soapSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEncodingSpinner.setAdapter(soapSpinnerAdapter);
        mEncodingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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
        etPort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etPort);
                }
                etPort.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        mSwitchSyncMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    tvSmscaleCursor.setText("");
                }
                else{
                    tvSmscaleCursor.setText(SharedPrefesManagerFactory.getString(SMScaleSyncManager2.PREF_SMSCALE,
                            SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR, ""));
                }
            }
        });
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

    public FileZillaDialog(Context context) {
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
//        p.height = d.getHeight();
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


    public void init(String title, DialogViewClickListener mDialogViewClickListener){
        this.tvTitle.setText(title);
        this.mDialogViewClickListener = mDialogViewClickListener;

        refresh();
    }

    /**
     * 刷新会员信息
     */
    private void refresh() {
        etIp.setText(SMScaleSyncManager2.FTP_HOST);
        etPort.setText(String.valueOf(SMScaleSyncManager2.FTP_PORT));
        etUsername.setText(SMScaleSyncManager2.FTP_USER);
        etPassword.setText(SMScaleSyncManager2.FTP_PASS);
        switch (SMScaleSyncManager2.PK_S_SMSCALE_ENCODING) {
            case Encoding.CHARSET_GBK:
                mEncodingSpinner.setSelection(0);
                break;
            case Encoding.CHARSET_UTF_8:
                mEncodingSpinner.setSelection(1);
                break;
            default:
                mEncodingSpinner.setSelection(0);
                break;
        }
        mSwitchSyncMode.setChecked(false);
    }

    private void submit() {
        DeviceUtils.hideSoftInput(getContext(), btnSubmit);
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        String host = etIp.getText().toString();
        if (StringUtils.isEmpty(host)) {
            DialogUtil.showHint("请输入IP");
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            etIp.requestFocus();
            return;
        }

        String port = etPort.getText().toString();
        if (StringUtils.isEmpty(port)) {
            DialogUtil.showHint("请输入端口号");
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            etPort.requestFocus();
            return;
        }

        String username = etUsername.getText().toString();
        if (StringUtils.isEmpty(username)) {
            DialogUtil.showHint("请输入用户名");
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            etUsername.requestFocus();
            return;
        }
        String password = etPassword.getText().toString();
        if (StringUtils.isEmpty(password)) {
            DialogUtil.showHint("请输入密码");
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            etPassword.requestFocus();
            return;
        }

        SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                SMScaleSyncManager2.PK_S_SMSCALE_HOST, host);
        SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                SMScaleSyncManager2.PK_I_SMSCALE_PORT, Integer.parseInt(port));
        SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                SMScaleSyncManager2.PK_S_SMSCALE_USERNAME, username);
        SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                SMScaleSyncManager2.PK_S_SMSCALE_PASSWORD, password);

        String encoding = mEncodingSpinner.getSelectedItem().toString();
        SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                SMScaleSyncManager2.PK_S_SMSCALE_ENCODING, encoding);
        if (mSwitchSyncMode.isChecked()) {
            SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                    SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR, "");
        }

        SMScaleSyncManager2.initialize();
        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);

        if (mDialogViewClickListener != null){
            mDialogViewClickListener.onSubmit();
        }

        dismiss();
    }

}

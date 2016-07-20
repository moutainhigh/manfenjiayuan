package org.century;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.manfenjiayuan.business.R;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.utils.IpInputFilter;

import org.ksoap2.SoapEnvelope;


/**
 * 绿泰电子价签参数设置
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GreenTagsSettingsDialog extends CommonDialog {

    static String[] SOAP_VERSIONS = new String[]{"SOAP 1.0", "SOAP 1.1", "SOAP 1.2"};

    private View rootView;
    private TextView tvTitle;
    private EditText etIp, etPort;
    private Spinner mSoapSpinner;
    private ArrayAdapter<String> soapSpinnerAdapter;
    private SwitchCompat mSwitchSyncMode;

    private Button btnSubmit;
    private ImageButton btnClose;
    private ProgressBar progressBar;

    public interface DialogViewClickListener{
        void onSubmit();
    }
    private DialogViewClickListener mDialogViewClickListener;

    private GreenTagsSettingsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private GreenTagsSettingsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_greentags_settings, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        etIp = (EditText) rootView.findViewById(R.id.et_ip);
        etPort = (EditText) rootView.findViewById(R.id.et_port);
        mSoapSpinner = (Spinner) rootView.findViewById(R.id.spinner_soap_version);
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

        tvTitle.setText("电子价签参数设置");

        soapSpinnerAdapter = new ArrayAdapter<>(context, R.layout.mfh_spinner_item_text, SOAP_VERSIONS);
        soapSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSoapSpinner.setAdapter(soapSpinnerAdapter);
        mSoapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        etIp.setFilters(new IpInputFilter[]{new IpInputFilter()});
        etIp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()) {
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
        etIp.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d(String.format("setOnKeyListener(etIp): keyCode=%d, action=%d",
                        keyCode, event.getAction()));
//                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        submitOrder();
//
//                        etPassword.requestFocus();
//                        etPassword.setSelection(etPassword.length());
//                    }
//                    return true;
//                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etPort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPreferencesManager.isSoftKeyboardEnabled()) {
                        DeviceUtils.showSoftInput(getContext(), etPort);
                    } else {
                        DeviceUtils.hideSoftInput(getContext(), etPort);
                    }
                }
                etPort.requestFocus();
                etPort.setSelection(etPort.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
//        mSwitchSyncMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (listener != null) {
//                    listener.onToggleChanged(isChecked);
//                }
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

    public GreenTagsSettingsDialog(Context context) {
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


    public void init(DialogViewClickListener mDialogViewClickListener){
        this.mDialogViewClickListener = mDialogViewClickListener;

        refresh();
    }

    /**
     * 刷新会员信息
     */
    public void refresh() {
        etIp.setText(GreenTagsApi.LOCAL_SERVER_IP);
        etPort.setText(String.valueOf(GreenTagsApi.LOCAL_PORT));
        switch (GreenTagsApi.SOAP_VERSION) {
            case SoapEnvelope.VER10:
                mSoapSpinner.setSelection(0);
                break;
            case SoapEnvelope.VER11:
                mSoapSpinner.setSelection(1);
                break;
            case SoapEnvelope.VER12:
                mSoapSpinner.setSelection(2);
                break;
            default:
                mSoapSpinner.setSelection(0);
                break;
        }
        mSwitchSyncMode.setChecked(GreenTagsApi.FULLSCALE_ENABLED);
    }

    private void submit() {
        DeviceUtils.hideSoftInput(getContext(), btnSubmit);
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        String ip = etIp.getText().toString();
        if (StringUtils.isEmpty(ip)) {
            DialogUtil.showHint("请输入IP");
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            return;
        }

        String port = etPort.getText().toString();
        if (StringUtils.isEmpty(port)) {
            DialogUtil.showHint("请输入端口号");
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            return;
        }

        SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                GreenTagsApi.PK_S_GREENTAGS_IP, ip);
        SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                GreenTagsApi.PK_I_GREENTAGS_PORT, Integer.parseInt(port));

        String soapVersion = mSoapSpinner.getSelectedItem().toString();
        switch (soapVersion) {
            case "SOAP 1.0":
                SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                        GreenTagsApi.PK_I_GREENTAGS_SOAPVERSION, SoapEnvelope.VER10);
                break;
            case "SOAP 1.1":
                SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                        GreenTagsApi.PK_I_GREENTAGS_SOAPVERSION, SoapEnvelope.VER11);
                break;
            case "SOAP 1.2":
                SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                        GreenTagsApi.PK_I_GREENTAGS_SOAPVERSION, SoapEnvelope.VER12);
                break;
            default:
                break;
        }

        // TODO: 7/18/16 不保存同步方式，后面考虑一次性修改。
        SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                GreenTagsApi.PK_B_GREENTAGS_FULLSCALE, mSwitchSyncMode.isChecked());
        if (GreenTagsApi.FULLSCALE_ENABLED || mSwitchSyncMode.isChecked()) {
            SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                    GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
        }

        GreenTagsApi.initialize();
        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);

        if (mDialogViewClickListener != null){
            mDialogViewClickListener.onSubmit();
        }

        dismiss();
    }

}

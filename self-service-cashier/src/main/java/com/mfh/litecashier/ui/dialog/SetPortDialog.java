package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.DataCacheHelper;


/**
 * 设置串口
 * 
 * @author bingshanguxue
 * 
 */
public class SetPortDialog extends CommonDialog {

    public interface onDialogClickListener {
        void onSetPort(String port, String baudrate);
    }
    private onDialogClickListener mListener;

    private View rootView;
    private TextView tvTitle;
    private ImageButton btnClose;
    private Spinner mPortSpinner, mBaudrateSpinner;
    private Button btnSubmit;

    private ArrayAdapter<String> aspnDevices;
    private ArrayAdapter<CharSequence> adapter;


    private SetPortDialog(Context context, boolean flag, DialogInterface.OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SetPortDialog(final Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_setport, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTitle.setText("配置串口");
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        btnSubmit.setVisibility(View.VISIBLE);
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
        mPortSpinner = (Spinner) rootView.findViewById(R.id.spinner_port);
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

        setContent(rootView, 0);
    }

    public SetPortDialog(Context context) {
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
//        p.width = d.getWidth();
////        p.y = DensityUtil.dip2px(getContext(), 44);
//        getWindow().setAttributes(p);
    }

    public void setOnDialogClickListener(onDialogClickListener listener){
        this.mListener = listener;
    }


    public void init(String port, String baudrate, onDialogClickListener listener){
        if (StringUtils.isEmpty(port)) {
            mPortSpinner.setSelection(0);
        }
        else{
            mPortSpinner.setSelection(aspnDevices.getPosition(port));
        }
        if (StringUtils.isEmpty(baudrate)) {
            mBaudrateSpinner.setSelection(0);
        }
        else{
            mBaudrateSpinner.setSelection(adapter.getPosition(baudrate));
        }
        this.mListener = listener;
    }

    private void submit(){

        dismiss();

        if (mListener != null) {
            mListener.onSetPort(mPortSpinner.getSelectedItem().toString(), mBaudrateSpinner.getSelectedItem().toString());
        }
    }
}

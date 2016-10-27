package com.mfh.enjoycity.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mfh.enjoycity.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * 选择时间
 * 
 * @author NAT.ZZN
 * 
 */
public class SelectTimeDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onSelectTime(String text);
    }

    private OnResponseCallback mListener;
    private View rootView;
    private Spinner mDateSpinner, mTimeSpinner;
    private ArrayAdapter<String> timeAdapter;
    private int mLastDateSelIndex = -1;

    private int minHour = 9;//营业时间9点开始
    private int maxHour = 21;//营业时间22点结束，1小时送货时间，超过21点默认当前不再配送
    private int halfHourInMinuter = 30;//

    private String[] mDates;
    private String[] mTimes1, mTimes2;


    private SelectTimeDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectTimeDialog(final Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.view_select_time, null);
//        ButterKnife.bind(rootView);

        mDateSpinner = (Spinner) rootView.findViewById(R.id.spinner_date);
        mDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str = parent.getItemAtPosition(position).toString();
                mDateSpinner.setPrompt(str);
                ZLogger.d("onItemSelected " + position);
                if (position == 0){
                    timeAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mTimes1);
                }
                else{
                    timeAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mTimes2);
                }

                mTimeSpinner.setAdapter(timeAdapter);
                mTimeSpinner.setPrompt(timeAdapter.getItem(0));
//                timeAdapter.notifyDataSetChanged();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
                ZLogger.d("onNothingSelected ");
            }
        });
        mDateSpinner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ZLogger.d("onFocusChange ");
            }
        });
        mTimeSpinner = (Spinner) rootView.findViewById(R.id.spinner_time);
        mTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str = parent.getItemAtPosition(position).toString();
                mTimeSpinner.setPrompt(str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        List<String> dateList = new ArrayList<>();


        int halfHourInMinuter = 30;//
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(calendar.HOUR_OF_DAY);
        int minute = calendar.get(calendar.MINUTE);
//        if (hour == 23 && minute >= 30){
        //TODO
        if (hour < maxHour){//1小时送货时间，超过21点默认当前不再配送
            dateList.add("今天");
            mTimes1 = generateTimeData(Math.max(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), minHour),
                    Calendar.getInstance().get(Calendar.MINUTE));//今天

            dateList.add("明天");
            mTimes2 = generateTimeData(minHour, 0);//明天
        }
        else{
            dateList.add("明天");
            mTimes1 = generateTimeData(minHour, 0);//明天
        }

        // 建立数据源
        mDates = dateList.toArray(new String[dateList.size()]);
//        mDates = context.getResources().getStringArray(R.array.spinner_date);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mDates);
        //绑定 Adapter到控件
        mDateSpinner.setAdapter(dateAdapter);
        mDateSpinner.setPrompt(mDates[0]);

//        mTimes = context.getResources().getStringArray(R.array.spinner_time);
        timeAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mTimes1);
        mTimeSpinner.setAdapter(timeAdapter);
        mTimeSpinner.setPrompt(timeAdapter.getItem(0));


        setContent(rootView, 0);

        this.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDateSpinner.getSelectedItemPosition() >= 0 && mTimeSpinner.getSelectedItemPosition() >= 0) {
//                    String text = mDates[mDateSpinner.getSelectedItemPosition()] + mTimes[mTimeSpinner.getSelectedItemPosition()];
                    String text = mDateSpinner.getPrompt().toString() + mTimeSpinner.getPrompt().toString();
                    ZLogger.d("Spinner: " + text);
                    if (mListener != null) {
                        mListener.onSelectTime(text);
                    }
                }
                dialog.dismiss();
            }
        });
        this.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public SelectTimeDialog(Context context) {
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
        p.width = d.getWidth();
//        p.y = DensityUtil.dip2px(getContext(), 44);
        getWindow().setAttributes(p);
    }

    public void setResponseCallback(OnResponseCallback responseCallback){
        this.mListener = responseCallback;
    }

    /**
     * 生成时间段
     * */
    private String[] generateTimeData(int hour, int minute){
        ZLogger.d(String.format("generateTimeData: %d:%d", hour, minute));
        List<String> timeList = new ArrayList<>();
        if (minute == 0){//临界点，从当前小时开始配送
            for (int h = hour; h <= maxHour; h++){
                String timeStr = String.format("%2d:00-%2d:00", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }
        else if (minute > 0 && minute < halfHourInMinuter){//未超过30分从当前小时的30分开始
            for (int h = hour; h < maxHour; h++){
                String timeStr = String.format("%2d:30-%2d:30", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }
        else{//超过30分从下一个小时开始
            for (int h = hour+1; h <= maxHour; h++){
                String timeStr = String.format("%2d:00-%2d:00", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }

        return timeList.toArray(new String[timeList.size()]);
    }
}

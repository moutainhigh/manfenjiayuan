package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;

import java.util.Calendar;


/**
 * 对话框 -- 设置日期和时间
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class DateTimePickerDialog extends CommonDialog {

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private Button btnSubmit;

    private Calendar calendar;
    private OnDateTimeSetListener mTimeSetCallback;

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Done' button).
     */
    public interface OnDateTimeSetListener {

        /**
         * @param year The initial year.
         * @param monthOfYear The initial month <strong>starting from zero</strong>.
         * @param dayOfMonth The initial day of the month.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        void onDateTimeSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute);
    }

    private DateTimePickerDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private DateTimePickerDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_datetime_picker, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        mDatePicker = (DatePicker) rootView.findViewById(R.id.datePicker);
        mTimePicker = (TimePicker) rootView.findViewById(R.id.timePicker);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);

        tvTitle.setText("日期和时间");
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mTimeSetCallback != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        mTimeSetCallback.onDateTimeSet(mDatePicker.getYear(),
                                mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),
                                mTimePicker.getHour(), mTimePicker.getMinute());
                    }
                    else{
                        mTimeSetCallback.onDateTimeSet(mDatePicker.getYear(),
                                mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),
                                mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                    }
                }

            }
        });

        setContent(rootView, 0);
    }

    public DateTimePickerDialog(Context context) {
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
    }

    public void init(Calendar calendar, OnDateTimeSetListener mTimeSetCallback){
        this.calendar = calendar;
        this.mTimeSetCallback = mTimeSetCallback;

        mDatePicker.setCalendarViewShown(false);
        mDatePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            }
        });
        mTimePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            mTimePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setMinute(calendar.get(Calendar.MINUTE));
        }
        else {
            mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

            }
        });
    }

}

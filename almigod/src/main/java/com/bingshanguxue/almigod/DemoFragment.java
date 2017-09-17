package com.bingshanguxue.almigod;


import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.base.BaseFragment;

import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class DemoFragment extends BaseFragment {
    @BindView(R.id.editText_milliseconds)
    EditText etMilliseconds;
    @BindView(R.id.textView_date1)
    TextView tvDate1;
    @BindView(R.id.textView_date2)
    TextView tvDate2;


    public static DemoFragment newInstance(Bundle args) {
        DemoFragment fragment = new DemoFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_demo;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
    }

    @OnClick(R.id.button_convert_milliseconds_2_date)
    public void convertMilliseconds2Date(){
        String milliseconds = etMilliseconds.getText().toString().trim();
        if (StringUtils.isEmpty(milliseconds)) {
            DialogUtil.showHint("请输入时间");
            return;
        }
        Date retDate = new Date();
        tvDate1.setText(TimeUtil.format(retDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        retDate.setTime(Long.parseLong(milliseconds));
        tvDate2.setText(TimeUtil.format(retDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
    }


}

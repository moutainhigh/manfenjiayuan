package com.mfh.enjoycity.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.enjoycity.R;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * 小伙伴
 */
public class FloatParterView extends RelativeLayout {
	@Bind(R.id.tv_tip)
	TextView tvTip;

	public FloatParterView(Context context) {
		super(context);
        init();
	}

	public FloatParterView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.view_float_mfparter, this);

		ButterKnife.bind(rootView);
	}

    public void setTip(String text){
		tvTip.setText(text);
    }
}

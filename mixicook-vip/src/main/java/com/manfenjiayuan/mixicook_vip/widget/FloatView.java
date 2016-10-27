package com.manfenjiayuan.mixicook_vip.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;


/**
 *
 */
public class FloatView extends RelativeLayout {
	private TextView tvBadge;

	public FloatView(Context context) {
		super(context);
        init();
	}

	public FloatView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.widget_floatview, this);

		tvBadge = (TextView) rootView.findViewById(R.id.tv_badge);
	}


	public void setBadgeNumber(Double number) {
		if (number == null || number.compareTo(0D) <= 0){
			tvBadge.setVisibility(GONE);
		}
		else{
			tvBadge.setText(String.format("%.0f", number));
		}
	}

}

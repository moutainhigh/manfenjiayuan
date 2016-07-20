package com.mfh.framework.uikit.compound;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.R;


/**
 * 地址
 */
public class NaviAddressView extends RelativeLayout {
	private TextView tvAddress;

	public NaviAddressView(Context context) {
		this(context, null);

	}

	public NaviAddressView(Context context, AttributeSet attrs) {
		super(context, attrs);

		View rootView = View.inflate(getContext(), R.layout.view_navi_address, this);
		tvAddress = (TextView) rootView.findViewById(R.id.tv_address);

		if (attrs != null){
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NaviAddressView);
			String addressText = ta.getString(R.styleable.NaviAddressView_text);

			tvAddress.setText(ta.getString(R.styleable.NaviAddressView_text));
			tvAddress.setTextColor(ta.getColor(R.styleable.NaviAddressView_textColor, Color.BLACK));
			tvAddress.setTextSize(ta.getDimension(R.styleable.NaviAddressView_textSize, 16));
			tvAddress.setText(ta.getString(R.styleable.NaviAddressView_hint));
			tvAddress.setHintTextColor(ta.getColor(R.styleable.NaviAddressView_textColorHint, Color.LTGRAY));
			tvAddress.setText(addressText);

			ta.recycle();
		}
	}

    public void setText(String text){
        tvAddress.setText(text);
    }
}

package com.bingshanguxue.vector_uikit.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.R;
import com.mfh.framework.core.utils.DensityUtil;


/**
 * 地址
 */
public class NaviAddressView extends RelativeLayout {
	private TextView tvStart;
	private TextView tvAddress;

	public NaviAddressView(Context context) {
		this(context, null);

	}

	public NaviAddressView(Context context, AttributeSet attrs) {
		super(context, attrs);

		View rootView = View.inflate(getContext(), R.layout.widget_naviaddress, this);
		tvStart = (TextView) rootView.findViewById(R.id.tv_start);
		tvAddress = (TextView) rootView.findViewById(R.id.tv_address);

		if (attrs != null){
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NaviAddressView);

			tvStart.setText(ta.getString(R.styleable.NaviAddressView_startText));
			tvStart.setTextColor(ta.getColor(R.styleable.NaviAddressView_startTextColor, Color.BLACK));
			int startTextSizeInPx = ta.getDimensionPixelSize(R.styleable.NaviAddressView_startTextSize, 16);
			int startTextSizeInSp = DensityUtil.px2sp(getContext(), startTextSizeInPx);
			tvStart.setTextSize(startTextSizeInSp);

			tvAddress.setText(ta.getString(R.styleable.NaviAddressView_text));
			tvAddress.setTextColor(ta.getColor(R.styleable.NaviAddressView_textColor, Color.BLACK));
			int textSizeInPx = ta.getDimensionPixelSize(R.styleable.NaviAddressView_textSize, 16);
			int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
			tvAddress.setTextSize(textSizeInSp);
			tvAddress.setHint(ta.getString(R.styleable.NaviAddressView_hint));
			tvAddress.setHintTextColor(ta.getColor(R.styleable.NaviAddressView_textColorHint, Color.LTGRAY));
			tvAddress.setText(ta.getString(R.styleable.NaviAddressView_text));

			ta.recycle();
		}
	}

    public void setText(String text){
        tvAddress.setText(text);
    }
}

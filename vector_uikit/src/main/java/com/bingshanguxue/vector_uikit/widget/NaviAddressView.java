package com.bingshanguxue.vector_uikit.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.R;
import com.mfh.framework.core.utils.DensityUtil;


/**
 * 地址
 */
public class NaviAddressView extends RelativeLayout {
	private ImageView ivStart, ivEnd;
	private TextView tvStart, tvEnd;

	public NaviAddressView(Context context) {
		this(context, null);

	}

	public NaviAddressView(Context context, AttributeSet attrs) {
		super(context, attrs);

		View rootView = View.inflate(getContext(), R.layout.widget_naviaddress, this);
		ivStart = (ImageView) rootView.findViewById(R.id.iv_start);
		tvStart = (TextView) rootView.findViewById(R.id.tv_start);
		tvEnd = (TextView) rootView.findViewById(R.id.tv_address);
		ivEnd = (ImageView) rootView.findViewById(R.id.iv_end);


		if (attrs != null){
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NaviAddressView);
			this.ivStart.setImageResource(ta.getResourceId(R.styleable.NaviAddressView_startSrc, R.mipmap.ic_location));
			if (ta.getBoolean(R.styleable.NaviAddressView_startSrcVisible, false)) {
				ivStart.setVisibility(View.VISIBLE);
			} else {
				ivStart.setVisibility(View.GONE);
			}

			tvStart.setText(ta.getString(R.styleable.NaviAddressView_startText));
			tvStart.setTextColor(ta.getColor(R.styleable.NaviAddressView_startTextColor, Color.BLACK));
			int startTextSizeInPx = ta.getDimensionPixelSize(R.styleable.NaviAddressView_startTextSize, 16);
			int startTextSizeInSp = DensityUtil.px2sp(getContext(), startTextSizeInPx);
			tvStart.setTextSize(startTextSizeInSp);

			tvEnd.setText(ta.getString(R.styleable.NaviAddressView_text));
			tvEnd.setTextColor(ta.getColor(R.styleable.NaviAddressView_textColor, Color.BLACK));
			int textSizeInPx = ta.getDimensionPixelSize(R.styleable.NaviAddressView_textSize, 16);
			int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
			tvEnd.setTextSize(textSizeInSp);
			tvEnd.setHint(ta.getString(R.styleable.NaviAddressView_hint));
			tvEnd.setHintTextColor(ta.getColor(R.styleable.NaviAddressView_textColorHint, Color.LTGRAY));
			tvEnd.setText(ta.getString(R.styleable.NaviAddressView_text));

			this.ivEnd.setImageResource(ta.getResourceId(R.styleable.NaviAddressView_endSrc,
					R.mipmap.ic_changelocation));
			if (ta.getBoolean(R.styleable.NaviAddressView_endSrcVisible, false)) {
				ivEnd.setVisibility(View.VISIBLE);
			} else {
				ivEnd.setVisibility(View.GONE);
			}

			ta.recycle();
		}
	}

    public void setText(String text){
		tvEnd.setText(text);
		tvEnd.requestFocus();
    }
}

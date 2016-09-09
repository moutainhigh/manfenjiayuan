package com.mfh.enjoycity.view;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.enjoycity.R;

import butterknife.Bind;
import butterknife.ButterKnife;


/***
 * 账户信息
 */
public class SimpleLabel extends LinearLayout {
	@Bind(R.id.tv_title)
	TextView tvTitle;
	@Bind(R.id.tv_detail)
	TextView tvDetail;

	public SimpleLabel(Context context) {
		super(context);
        init();
	}

	public SimpleLabel(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.view_simple_label, this);

		ButterKnife.bind(rootView);
	}

	public void setTvTitle(String text){
		this.tvTitle.setText(text);
	}

	public void setTvDetail(String text){
		this.tvDetail.setText(text);
	}

	public void setTvDetailTextColor(int color){
		this.tvDetail.setTextColor(color);
	}

	public void setTvDetailText(String text, int color){
		this.tvDetail.setText(text);
		this.tvDetail.setTextColor(color);
	}

	/**
	 * 显示价格文本
	 * */
	public void setTvDetailForPrice(double price, int positiveColor, int negativeColor){
		this.tvDetail.setText(getContext().getString(R.string.mf_format_price_1, price));
		this.tvDetail.setTextColor((price > 0 ? positiveColor : negativeColor));
		this.tvDetail.setTextColor((price > 0 ? ContextCompat.getColor(getContext(), positiveColor)
				: ContextCompat.getColor(getContext(), negativeColor)));
	}



}

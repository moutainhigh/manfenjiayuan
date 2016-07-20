package com.mfh.petitestock.widget.compound;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.petitestock.R;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * 复合控件－－（文本＋文本）
 * 支持自定义属性，可以直接在xml文件中配置。
 * {@link TextLabelView }
 */
public class TextLabelView extends LinearLayout {
	@Bind(R.id.tv_lefttext)
	TextView tvLeftText;
	@Bind(R.id.tv_righttext)
	TextView tvRightText;

	public TextLabelView(Context context) {
		this(context, null);
	}

	public TextLabelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		View rootView = View.inflate(getContext(), R.layout.itemview_text, this);

		ButterKnife.bind(rootView);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextLabelView);
		String leftText = ta.getString(R.styleable.EditLabelView_editLabelView_leftText);
//		int leftTextSize = ta.getDimensionPixelSize(R.styleable.TextLabelView_textLabelView_leftTextSize, 12);//px
		float leftTextSize = ta.getDimension(R.styleable.TextLabelView_textLabelView_leftTextSize, 12);//sp
		int leftTextColor = ta.getColor(R.styleable.TextLabelView_textLabelView_leftTextColor, 0);
		int leftTextWidth = ta.getDimensionPixelSize(R.styleable.TextLabelView_textLabelView_leftTextWidth, 80);//px
		String rightText = ta.getString(R.styleable.TextLabelView_textLabelView_rightText);
//		int rightTextSize = ta.getDimensionPixelSize(R.styleable.TextLabelView_textLabelView_rightTextSize, rightTextSize);
		float rightTextSize = (int)ta.getDimension(R.styleable.TextLabelView_textLabelView_leftTextSize, 12);
		int rightTextColor = ta.getColor(R.styleable.TextLabelView_textLabelView_rightTextColor, 0);
		ta.recycle();

		//
		tvLeftText.setText(leftText);
		tvLeftText.setTextSize(leftTextSize);
		tvLeftText.setTextColor(leftTextColor);
		tvLeftText.setWidth(leftTextWidth);
		tvRightText.setText(rightText);
		tvRightText.setTextSize(rightTextSize);
		tvRightText.setTextColor(rightTextColor);
	}


	public void setTvTitle(String text) {
		this.tvLeftText.setText(text);
	}

	public void setTvSubTitle(String text) {
		this.tvRightText.setText(text);
	}
}

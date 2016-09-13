package com.bingshanguxue.vector_uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.R;
import com.mfh.framework.core.utils.DensityUtil;


/**
 * 复合控件－－（文本＋文本）
 * 支持自定义属性，可以直接在xml文件中配置。
 * {@link TextLabelView }
 */
public class TextLabelView extends LinearLayout {
	private TextView tvStartText;
	private TextView tvEndText;

	public TextLabelView(Context context) {
		this(context, null);
	}

	public TextLabelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		View rootView = View.inflate(getContext(), R.layout.widget_textlabelview, this);

//		ButterKnife.bind(rootView);

		tvStartText = (TextView) rootView.findViewById(R.id.tv_lefttext);
		tvEndText = (TextView) rootView.findViewById(R.id.tv_righttext);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextLabelView);

		//像素
		int textSizeInPx = ta.getDimensionPixelSize(R.styleable.TextLabelView_startTextSize, 16);
		int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
		int endTextSizeInPx = ta.getDimensionPixelSize(R.styleable.TextLabelView_endTextSize, 16);
		int endTextSizeInSp = DensityUtil.px2sp(getContext(), endTextSizeInPx);

		tvStartText.setText(ta.getString(R.styleable.TextLabelView_startText));
//		int leftTextSize = ta.getDimensionPixelSize(R.styleable.TextLabelView_textLabelView_leftTextSize, 12);//px
		tvStartText.setTextSize(textSizeInSp);
		tvStartText.setTextColor(ta.getColor(R.styleable.TextLabelView_startTextColor, 0x59000000));
		int startTextWidth = ta.getDimensionPixelSize(R.styleable.TextLabelView_startTextWidth, 80);//px
		ViewGroup.LayoutParams stLayoutParams = tvStartText.getLayoutParams();
		stLayoutParams.width = startTextWidth;
//		ZLogger.d(String.format("startText, %s=%d", leftText, startTextWidth));
		tvStartText.setLayoutParams(stLayoutParams);

		tvEndText.setText(ta.getString(R.styleable.TextLabelView_endText));
		tvEndText.setTextSize(endTextSizeInSp);
		tvEndText.setTextColor(ta.getColor(R.styleable.TextLabelView_endTextColor, 0xFF000000));
		tvEndText.setSingleLine(ta.getBoolean(R.styleable.TextLabelView_endTextSingleLine, true));
		ta.recycle();
	}

	public void setStartText(String text) {
		this.tvStartText.setText(text);
	}

	public void setEndText(String text) {
		this.tvEndText.setText(text);
	}

	public String getEndText(){
		return this.tvEndText.getText().toString();
	}

	public void setTvSubTitle(String text) {
		this.tvEndText.setText(text);
	}
}

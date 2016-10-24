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
 * 垂直分布的文字
 */
public class MultiLayerLabel extends RelativeLayout {

	private TextView tvTop;
    private TextView tvBottom;

	public MultiLayerLabel(Context context) {
        this(context, null);
	}

	public MultiLayerLabel(Context context, AttributeSet attrs) {
		super(context, attrs);
        View.inflate(context, R.layout.view_multi_layer_label, this);
        tvTop = (TextView) this.findViewById(R.id.tv_top);
        tvBottom = (TextView) this.findViewById(R.id.tv_bottom);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiLayerLabel);

            tvTop.setText(a.getString(R.styleable.MultiLayerLabel_topText));
            tvTop.setTextColor(a.getColor(R.styleable.MultiLayerLabel_topTextColor, Color.BLACK));
            int textSizeInPx = a.getDimensionPixelSize(R.styleable.MultiLayerLabel_topTextSize, 12);
            int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
            tvTop.setTextSize(textSizeInSp);


            tvBottom.setText(a.getString(R.styleable.MultiLayerLabel_bottomText));
            tvBottom.setTextColor(a.getColor(R.styleable.MultiLayerLabel_bottomTextColor, Color.BLACK));
            int bottomTextSizeInPx = a.getDimensionPixelSize(R.styleable.MultiLayerLabel_bottomTextSize, 12);
            int bottomTextSizeInSp = DensityUtil.px2sp(getContext(), bottomTextSizeInPx);
            tvBottom.setTextSize(bottomTextSizeInSp);
            a.recycle();
        }
	}

    public void setTopText(String text){
        tvTop.setText(text);
    }

    public void setTopText(String text, int color){
        tvTop.setText(text);
        tvTop.setTextColor(color);
    }

}

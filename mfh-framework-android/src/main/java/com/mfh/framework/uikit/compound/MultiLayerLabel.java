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
            tvTop.setTextSize(a.getDimension(R.styleable.MultiLayerLabel_topTextSize, 12));

            tvBottom.setText(a.getString(R.styleable.MultiLayerLabel_bottomText));
            tvBottom.setTextColor(a.getColor(R.styleable.MultiLayerLabel_bottomTextColor, Color.BLACK));
            tvBottom.setTextSize(a.getDimension(R.styleable.MultiLayerLabel_bottomTextSize, 12));
            a.recycle();
        }
	}

    public void setTopText(String topText){
        tvTop.setText(topText);
    }

}

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
 * 购物车
 */
public class FloatShopcartView extends RelativeLayout {
	@Bind(R.id.tv_product_count)
	TextView tvProductCount;

	public FloatShopcartView(Context context) {
		super(context);
        init();
	}

	public FloatShopcartView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.view_float_shopcart, this);

		ButterKnife.bind(rootView);
	}

    public void setNumber(int number){
		if (number > 0){
			tvProductCount.setText(String.valueOf(number));
		}
		else {
			tvProductCount.setText("");
		}
    }


}
